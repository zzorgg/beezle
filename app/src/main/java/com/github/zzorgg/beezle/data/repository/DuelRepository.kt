package com.github.zzorgg.beezle.data.repository

import android.util.Log
import com.github.zzorgg.beezle.BuildConfig
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelRoom
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelStatus
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.model.duel.Question
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import com.github.zzorgg.beezle.data.remote.DuelWebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuelRepository @Inject constructor(
    private val webSocketService: DuelWebSocketService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _duelState = MutableStateFlow(DuelState())
    val duelState: StateFlow<DuelState> = _duelState.asStateFlow()

    private var currentUser: DuelUser? = null
    private var questionStartTime: Long = 0
    private var questionTimerJob: Job? = null
    private var lastQueueJoinTime: Long? = null
    private var lastRequeueAttempt: Long = 0

    private val QUEUE_STUCK_TIMEOUT_MS = 30_000L // 30s before auto requeue attempt
    private val REQUEUE_COOLDOWN_MS = 15_000L
    private val LOCAL_FALLBACK_THRESHOLD_MS = 10_000L // 10s waiting at position 1 -> start local bot duel (debug only)

    private var localBotActive = false

    companion object {
        private const val TAG = "DuelRepository"
        private const val DEFAULT_QUESTION_TIME_LIMIT = 15
    }

    init {
        observeWebSocketMessages()
        observeConnectionStatus()
    }

    private fun observeConnectionStatus() {
        scope.launch {
            webSocketService.isConnected.collect { isConnected ->
                _duelState.value = _duelState.value.copy(
                    isConnected = isConnected,
                    connectionStatus = if (isConnected) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
                )
            }
        }
    }

    private fun observeWebSocketMessages() {
        scope.launch {
            webSocketService.messages.collect { message ->
                Log.d(TAG, "Received message: $message")
                handleWebSocketMessage(message)
            }
        }
        // Queue watchdog
        scope.launch {
            while (true) {
                delay(5000)
                val state = _duelState.value
                if (state.isInQueue && state.currentRoom == null) {
                    val since = state.queueSince
                    if (since != null) {
                        val elapsed = System.currentTimeMillis() - since
                        if (elapsed > QUEUE_STUCK_TIMEOUT_MS && (System.currentTimeMillis() - lastRequeueAttempt) > REQUEUE_COOLDOWN_MS) {
                            lastRequeueAttempt = System.currentTimeMillis()
                            currentUser?.let { user ->
                                Log.w(TAG, "Queue seems stuck (elapsed=${elapsed}ms, position=${state.queuePosition}). Re-sending join_queue.")
                                // Resend join_queue (idempotent on server or will refresh position)
                                val joinQueueData = WebSocketMessage.JoinQueueData(
                                    player_id = user.id,
                                    display_name = user.username
                                )
                                webSocketService.sendMessage(WebSocketMessage.JoinQueue(data = joinQueueData))
                            }
                        }
                        // Local fallback (debug builds only) to allow UI testing
                        if (BuildConfig.DEBUG && !localBotActive && state.queuePosition == 1 && elapsed > LOCAL_FALLBACK_THRESHOLD_MS) {
                            Log.w(TAG, "Starting local bot duel fallback for UI testing (elapsed=${elapsed}ms)")
                            startLocalBotMatch()
                        }
                    }
                }
            }
        }
    }

    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message) {
            is WebSocketMessage.Queued -> {
                _duelState.value = _duelState.value.copy(
                    isInQueue = true,
                    isSearching = true,
                    error = null,
                    queuePosition = message.data.position,
                    queueSince = _duelState.value.queueSince ?: System.currentTimeMillis()
                )
            }

            is WebSocketMessage.MatchFound -> {
                // Create a DuelRoom from the match data
                val player1 = DuelUser(
                    id = message.data.player_id,
                    username = currentUser?.username ?: "Player",
                    avatarUrl = currentUser?.avatarUrl
                )
                val player2 = DuelUser(
                    id = message.data.opponent_id,
                    username = message.data.opponent_name,
                    avatarUrl = null // TODO Fetch opponent avatar
                )

                val room = DuelRoom(
                    id = message.data.match_id,
                    player1 = player1,
                    player2 = player2,
                    status = DuelStatus.IN_PROGRESS
                )

                _duelState.value = _duelState.value.copy(
                    isInQueue = false,
                    isSearching = false,
                    currentRoom = room,
                    error = null,
                    queuePosition = null,
                    queueSince = null
                )

                // Inform server this client is ready (if protocol expects it)
                try {
                    val readyData = WebSocketMessage.Ready.ReadyData(
                        match_id = message.data.match_id,
                        player_id = message.data.player_id
                    )
                    webSocketService.sendMessage(WebSocketMessage.Ready(data = readyData))
                    Log.d(TAG, "Sent ready acknowledgment for match ${message.data.match_id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send ready message", e)
                }
            }

            is WebSocketMessage.QuestionReceived -> {
                questionStartTime = System.currentTimeMillis()

                val question = Question(
                    id = message.data.question_id,
                    text = message.data.question_text,
                    options = message.data.options,
                    correctAnswer = 0, // Placeholder; not known yet
                    timeLimit = message.data.time_limit
                )

                _duelState.value = _duelState.value.copy(
                    currentQuestion = question,
                    timeRemaining = message.data.time_limit,
                    selectedAnswer = null,
                    hasAnswered = false,
                    error = null
                )
                startQuestionTimer(message.data.time_limit)
            }

            is WebSocketMessage.RoundResult -> {
                cancelQuestionTimer()
                _duelState.value = _duelState.value.copy(
                    lastRoundResult = message,
                    hasAnswered = false,
                    selectedAnswer = null,
                    currentQuestion = null,
                    timeRemaining = 0
                )
            }

            is WebSocketMessage.DuelComplete -> {
                cancelQuestionTimer()
                _duelState.value = _duelState.value.copy(
                    currentRoom = null,
                    currentQuestion = null,
                    timeRemaining = 0,
                    selectedAnswer = null,
                    hasAnswered = false,
                    isInQueue = false,
                    isSearching = false,
                    queuePosition = null,
                    queueSince = null
                )
            }

            is WebSocketMessage.OpponentLeft -> {
                cancelQuestionTimer()
                _duelState.value = _duelState.value.copy(
                    error = "Opponent left the duel: ${message.data.reason}",
                    currentRoom = null,
                    currentQuestion = null,
                    isInQueue = false,
                    isSearching = false,
                    queuePosition = null,
                    queueSince = null
                )
            }

            is WebSocketMessage.Error -> {
                _duelState.value = _duelState.value.copy(
                    error = message.data.message,
                    isInQueue = false,
                    isSearching = false
                )
            }

            else -> {
                Log.d(TAG, "Unhandled message type: ${message::class.simpleName}")
            }
        }
    }

    private fun startQuestionTimer(limit: Int?) {
        cancelQuestionTimer()
        val timeLimit = limit ?: _duelState.value.currentQuestion?.timeLimit ?: DEFAULT_QUESTION_TIME_LIMIT
        questionTimerJob = scope.launch {
            for (remaining in (timeLimit - 1) downTo 0) {
                delay(1000)
                _duelState.value = _duelState.value.copy(timeRemaining = remaining)
                if (remaining <= 0) {
                    if (!_duelState.value.hasAnswered) {
                        submitAnswer(-1) // timeout
                    }
                }
            }
        }
    }

    private fun cancelQuestionTimer() {
        questionTimerJob?.cancel()
        questionTimerJob = null
    }

    fun connectToServer() {
        _duelState.value = _duelState.value.copy(
            connectionStatus = ConnectionStatus.CONNECTING,
            error = null
        )
        webSocketService.connect()
    }

    fun disconnect() {
        webSocketService.disconnect()
        _duelState.value = DuelState()
        lastQueueJoinTime = null
    }

    fun joinQueue(user: DuelUser) {
        if (!_duelState.value.isConnected) {
            _duelState.value = _duelState.value.copy(
                error = "Not connected to server"
            )
            return
        }

        currentUser = user
        val now = System.currentTimeMillis()
        lastQueueJoinTime = now
        _duelState.value = _duelState.value.copy(
            isInQueue = true,
            isSearching = true,
            error = null,
            queuePosition = null,
            queueSince = now
        )

        // Send the correct message format that matches the server expectations
        val joinQueueData = WebSocketMessage.JoinQueueData(
            player_id = user.id,
            display_name = user.username
        )
        webSocketService.sendMessage(WebSocketMessage.JoinQueue(data = joinQueueData))
    }

    fun leaveQueue() {
        _duelState.value = _duelState.value.copy(
            isInQueue = false,
            isSearching = false,
            queuePosition = null,
            queueSince = null
        )
        // TODO: Optionally send a leave_queue action if server supports it
    }

    fun submitAnswer(answerIndex: Int) {
        val question = _duelState.value.currentQuestion
        val user = currentUser

        if (question == null || user == null || _duelState.value.hasAnswered) {
            return
        }

        _duelState.value = _duelState.value.copy(
            selectedAnswer = answerIndex,
            hasAnswered = true
        )

        // Send answer in the correct format
        val answerData = WebSocketMessage.AnswerData(
            player_id = user.id,
            question_id = question.id,
            answer_index = answerIndex
        )
        webSocketService.sendMessage(WebSocketMessage.AnswerSubmitted(data = answerData))
    }

    fun clearError() {
        _duelState.value = _duelState.value.copy(error = null)
    }

    fun clearLastRoundResult() {
        _duelState.value = _duelState.value.copy(lastRoundResult = null)
    }

    fun getCurrentUser() = currentUser

    fun setCurrentUser(user: DuelUser) {
        currentUser = user
    }

    private fun startLocalBotMatch() {
        val user = currentUser ?: return
        localBotActive = true
        val bot = DuelUser(id = "bot_${System.currentTimeMillis()}", username = "Bot", avatarUrl = null)
        val room = DuelRoom(
            id = "local_${System.currentTimeMillis()}",
            player1 = DuelUser(id = user.id, username = user.username, avatarUrl = user.avatarUrl),
            player2 = bot,
            status = DuelStatus.IN_PROGRESS
        )
        _duelState.value = _duelState.value.copy(
            currentRoom = room,
            isInQueue = false,
            isSearching = false,
            queuePosition = null,
            queueSince = null
        )
        scope.launch { runLocalBotRounds(botId = bot.id, rounds = 3) }
    }

    private suspend fun runLocalBotRounds(botId: String, rounds: Int) {
        repeat(rounds) { roundIndex ->
            if (!_duelState.value.isConnected && !BuildConfig.DEBUG) return
            val question = generateLocalQuestion(roundIndex)
            // Show question
            _duelState.value = _duelState.value.copy(
                currentQuestion = question,
                timeRemaining = question.timeLimit,
                selectedAnswer = null,
                hasAnswered = false
            )
            questionStartTime = System.currentTimeMillis()
            startQuestionTimer(question.timeLimit)
            // Wait for either answer or timeout
            val roundDuration = question.timeLimit * 1000L
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < roundDuration && _duelState.value.hasAnswered.not()) {
                delay(250)
            }
            cancelQuestionTimer()
            val playerAnswer = _duelState.value.selectedAnswer
            val playerCorrect = playerAnswer != null && playerAnswer == question.correctAnswer
            val botCorrect = (0..100).random() < 55 // 55% correctness for bot
            val roundResult = WebSocketMessage.RoundResult(
                action = "round_result",
                data = WebSocketMessage.RoundResultData(
                    player1_correct = playerCorrect,
                    player2_correct = botCorrect,
                    correct_answer = question.correctAnswer
                )
            )
            _duelState.value = _duelState.value.copy(
                lastRoundResult = roundResult,
                currentQuestion = null,
                selectedAnswer = null,
                hasAnswered = false,
                timeRemaining = 0
            )
            delay(3000) // brief result display
            _duelState.value = _duelState.value.copy(lastRoundResult = null)
        }
        // Complete duel
        val playerWins = _duelState.value.lastRoundResult?.data?.player1_correct == true
        val duelComplete = WebSocketMessage.DuelComplete(
            action = "duel_complete",
            data = WebSocketMessage.DuelCompleteData(
                winner_id = if (playerWins) currentUser?.id else botId
            )
        )
        _duelState.value = _duelState.value.copy(
            currentRoom = null,
            currentQuestion = null,
            isInQueue = false,
            isSearching = false,
            queuePosition = null,
            queueSince = null,
            lastRoundResult = null
        )
        localBotActive = false
        Log.i(TAG, "Local bot duel finished")
    }

    private fun generateLocalQuestion(index: Int): Question {
        val samples = listOf(
            Triple("What is 2 + 2?", listOf("1", "2", "3", "4"), 3),
            Triple("Capital of France?", listOf("Berlin", "Paris", "Madrid", "Rome"), 1),
            Triple("Kotlin is a...", listOf("Database", "Language", "OS", "Library"), 1),
            Triple("HTTP status 404 means?", listOf("Unauthorized", "Not Found", "Forbidden", "OK"), 1)
        )
        val (text, options, correct) = samples[index % samples.size]
        return Question(
            id = "local_q_$index",
            text = text,
            options = options,
            correctAnswer = correct,
            timeLimit = 15
        )
    }

    fun startLocalTestDuel() {
        if (BuildConfig.DEBUG && !localBotActive) {
            Log.i(TAG, "Manually starting local test duel (debug mode)")
            startLocalBotMatch()
        }
    }
}