package com.github.zzorgg.beezle.data.repository

import android.util.Log
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelMode
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
import kotlin.random.Random

@Singleton
class DuelRepository @Inject constructor(
    private val webSocketService: DuelWebSocketService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _duelState = MutableStateFlow(DuelState())
    val duelState: StateFlow<DuelState> = _duelState.asStateFlow()

    private var currentUser: DuelUser? = null
    private var currentMatchId: String? = null
    private var currentRound: Int = 0
    private val totalRounds = 5

    // Question bank for client-side question generation
    private val mathQuestions = listOf(
        Triple("15 + 27 = ?", listOf("40", "41", "42", "43"), 2),
        Triple("8 × 9 = ?", listOf("64", "72", "81", "56"), 1),
        Triple("100 - 37 = ?", listOf("63", "73", "67", "53"), 0),
        Triple("144 ÷ 12 = ?", listOf("11", "12", "13", "14"), 1),
        Triple("5² = ?", listOf("10", "15", "20", "25"), 3),
        Triple("√64 = ?", listOf("6", "7", "8", "9"), 2),
        Triple("25 × 4 = ?", listOf("90", "95", "100", "105"), 2),
        Triple("3³ = ?", listOf("9", "18", "27", "36"), 2),
    )

    private val csQuestions = listOf(
        Triple("Time complexity of binary search?", listOf("O(n)", "O(log n)", "O(n²)", "O(1)"), 1),
        Triple("FIFO data structure?", listOf("Stack", "Queue", "Tree", "Graph"), 1),
        Triple("Which is not OOP principle?", listOf("Encapsulation", "Polymorphism", "Recursion", "Inheritance"), 2),
        Triple("REST API uses which protocol?", listOf("FTP", "SMTP", "HTTP", "SSH"), 2),
        Triple("SQL stands for?", listOf("Standard Query Language", "Structured Query Language", "Sequential Query Language", "System Query Language"), 1),
        Triple("Git command to save changes?", listOf("git save", "git push", "git commit", "git update"), 2),
        Triple("Which stores key-value pairs?", listOf("Array", "HashMap", "LinkedList", "Tree"), 1),
        Triple("TCP connection uses how many handshakes?", listOf("1", "2", "3", "4"), 2),
    )

    companion object {
        private const val TAG = "DuelRepository"
        private const val QUESTION_TIME_LIMIT = 15
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

                if (!isConnected) {
                    // Handle disconnection during match
                    if (_duelState.value.currentRoom != null) {
                        _duelState.value = _duelState.value.copy(
                            error = "Connection lost. Attempting to reconnect..."
                        )
                    }
                }
            }
        }
    }

    private fun observeWebSocketMessages() {
        scope.launch {
            webSocketService.messages.collect { message ->
                Log.d(TAG, "Processing message: ${message::class.simpleName}")
                handleWebSocketMessage(message)
            }
        }
    }

    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message) {
            is WebSocketMessage.Queued -> {
                Log.d(TAG, "✅ Queued at position: ${message.data.position}")
                _duelState.value = _duelState.value.copy(
                    isInQueue = true,
                    isSearching = true,
                    error = null,
                    queuePosition = message.data.position,
                    queueSince = _duelState.value.queueSince ?: System.currentTimeMillis()
                )
            }

            is WebSocketMessage.MatchFound -> {
                Log.d(TAG, "🎮 Match found! ID: ${message.data.match_id}")
                currentMatchId = message.data.match_id
                currentRound = 0

                val player1 = DuelUser(
                    id = message.data.player_id,
                    username = currentUser?.username ?: "You",
                    avatarUrl = currentUser?.avatarUrl
                )
                val player2 = DuelUser(
                    id = message.data.opponent_id,
                    username = message.data.opponent_name,
                    avatarUrl = message.data.opponent_id
                )

                val room = DuelRoom(
                    id = message.data.match_id,
                    player1 = player1,
                    player2 = player2,
                    status = DuelStatus.IN_PROGRESS,
                    betAmount = message.data.bet_amount,
                    betToken = message.data.bet_token
                )

                _duelState.value = _duelState.value.copy(
                    isInQueue = false,
                    isSearching = false,
                    currentRoom = room,
                    error = null,
                    queuePosition = null,
                    queueSince = null,
                    myScore = 0,
                    opponentScore = 0,
                    currentRound = 0
                )

                // Start the first round
                scope.launch {
                    delay(1500) // Brief delay to show "Match Found" screen
                    startNextRound()
                }
            }

            is WebSocketMessage.ScoreUpdate -> {
                Log.d(TAG, "📊 Score update: ${message.data.scores}")
                val myId = currentUser?.id ?: return
                val myScore = message.data.scores[myId] ?: 0
                val opponentScore = message.data.scores.filterKeys { it != myId }.values.firstOrNull() ?: 0

                _duelState.value = _duelState.value.copy(
                    myScore = myScore,
                    opponentScore = opponentScore,
                    currentRound = message.data.round_number
                )

                // Show round result briefly, then move to next round
                scope.launch {
                    delay(2000)
                    if (message.data.round_number < totalRounds) {
                        startNextRound()
                    }
                }
            }

            is WebSocketMessage.OpponentAnswer -> {
                Log.d(TAG, "👤 Opponent answered: ${if (message.data.correct) "correct" else "incorrect"}")
                _duelState.value = _duelState.value.copy(
                    opponentAnswered = true
                )
            }

            is WebSocketMessage.GameOver -> {
                Log.d(TAG, "🏁 Game over! Winner: ${message.data.winner_id}")
                _duelState.value = _duelState.value.copy(
                    lastGameResult = message,
                    currentRoom = null,
                    currentQuestion = null,
                    isInQueue = false,
                    isSearching = false,
                    queuePosition = null,
                    queueSince = null
                )
            }

            is WebSocketMessage.OpponentLeft -> {
                Log.d(TAG, "🚪 Opponent left the match")
                _duelState.value = _duelState.value.copy(
                    error = "Opponent disconnected. You win by forfeit!",
                    currentRoom = null,
                    currentQuestion = null,
                    isInQueue = false,
                    isSearching = false
                )
            }

            is WebSocketMessage.Error -> {
                Log.e(TAG, "❌ Server error: ${message.data.message}")
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

    private fun startNextRound() {
        currentRound++

        if (currentRound > totalRounds) {
            Log.d(TAG, "All rounds completed")
            return
        }

        val question = generateQuestion()
        _duelState.value = _duelState.value.copy(
            currentQuestion = question,
            timeRemaining = QUESTION_TIME_LIMIT,
            selectedAnswer = null,
            hasAnswered = false,
            opponentAnswered = false,
            currentRound = currentRound
        )

        startQuestionTimer()
    }

    private fun generateQuestion(): Question {
        val mode = _duelState.value.selectedMode ?: DuelMode.MATH
        val (text, options, correctIndex) = when (mode) {
            DuelMode.MATH -> mathQuestions.random()
            DuelMode.CS -> csQuestions.random()
            DuelMode.GENERAL -> if (Random.nextBoolean()) mathQuestions.random() else csQuestions.random()
        }

        return Question(
            id = "q_${currentMatchId}_$currentRound",
            text = text,
            options = options,
            correctAnswer = correctIndex,
            timeLimit = QUESTION_TIME_LIMIT,
            roundNumber = currentRound
        )
    }

    private var questionTimerJob: Job? = null

    private fun startQuestionTimer() {
        questionTimerJob?.cancel()
        questionTimerJob = scope.launch {
            for (remaining in QUESTION_TIME_LIMIT downTo 0) {
                _duelState.value = _duelState.value.copy(timeRemaining = remaining)
                delay(1000)

                if (remaining == 0 && !_duelState.value.hasAnswered) {
                    // Auto-submit timeout (incorrect answer)
                    submitAnswer(-1)
                }
            }
        }
    }

    fun connectToServer() {
        _duelState.value = _duelState.value.copy(
            connectionStatus = ConnectionStatus.CONNECTING,
            error = null
        )
        webSocketService.connect()
    }

    fun disconnect() {
        questionTimerJob?.cancel()
        webSocketService.disconnect()
        _duelState.value = DuelState()
        currentMatchId = null
        currentRound = 0
    }

    fun startDuel(username: String, mode: DuelMode) {
        val user = DuelUser(
            id = generateUserId(),
            username = username,
            avatarUrl = null
        )

        currentUser = user

        _duelState.value = _duelState.value.copy(
            selectedMode = mode
        )

        if (!_duelState.value.isConnected) {
            connectToServer()
            scope.launch {
                // Wait for connection
                delay(2000)
                if (_duelState.value.isConnected) {
                    joinQueue(user)
                } else {
                    _duelState.value = _duelState.value.copy(
                        error = "Failed to connect to server"
                    )
                }
            }
        } else {
            joinQueue(user)
        }
    }

    private fun joinQueue(user: DuelUser) {
        if (!_duelState.value.isConnected) {
            _duelState.value = _duelState.value.copy(
                error = "Not connected to server"
            )
            return
        }

        val now = System.currentTimeMillis()
        _duelState.value = _duelState.value.copy(
            isInQueue = true,
            isSearching = true,
            error = null,
            queuePosition = null,
            queueSince = now
        )

        val joinQueueData = WebSocketMessage.JoinQueueData(
            player_id = user.id,
            display_name = user.username,
            bet_amount = 0.0,
            bet_token = "SOL"
        )

        webSocketService.sendMessage(WebSocketMessage.JoinQueue(data = joinQueueData))
        Log.d(TAG, "📤 Sent join_queue for player: ${user.username}")
    }

    fun leaveQueue() {
        _duelState.value = _duelState.value.copy(
            isInQueue = false,
            isSearching = false,
            queuePosition = null,
            queueSince = null
        )
    }

    fun submitAnswer(answerIndex: Int) {
        val question = _duelState.value.currentQuestion ?: return
        val user = currentUser ?: return
        val matchId = currentMatchId ?: return

        if (_duelState.value.hasAnswered) {
            Log.w(TAG, "Already answered this question")
            return
        }

        val correct = answerIndex == question.correctAnswer
        val scoreDelta = if (correct) 1 else 0
        val isFinal = currentRound >= totalRounds

        _duelState.value = _duelState.value.copy(
            selectedAnswer = answerIndex,
            hasAnswered = true
        )

        questionTimerJob?.cancel()

        val answerData = WebSocketMessage.SubmitAnswerData(
            match_id = matchId,
            player_id = user.id,
            question_id = question.id,
            answer = if (answerIndex >= 0) question.options[answerIndex] else "TIMEOUT",
            correct = correct,
            score_delta = scoreDelta,
            final = isFinal,
            round_number = currentRound
        )

        webSocketService.sendMessage(WebSocketMessage.SubmitAnswer(data = answerData))
        Log.d(TAG, "📤 Submitted answer: $answerIndex (${if (correct) "correct" else "incorrect"})")
    }

    fun clearError() {
        _duelState.value = _duelState.value.copy(error = null)
    }

    fun clearGameResult() {
        _duelState.value = _duelState.value.copy(lastGameResult = null)
    }

    private fun generateUserId(): String {
        return "player_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }

    fun getCurrentUser() = currentUser
}