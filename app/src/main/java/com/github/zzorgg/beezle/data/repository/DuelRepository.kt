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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DuelRepository @Inject constructor(
    private val webSocketService: DuelWebSocketService,
    private val authRepository: AuthRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _duelState = MutableStateFlow(DuelState())
    val duelState: StateFlow<DuelState> = _duelState.asStateFlow()

    private var currentUser: DuelUser? = null
    private var currentMatchId: String? = null
    private var currentRound: Int = 0
    private val totalRounds = 5

    // Debounce join attempts to avoid spamming the server and duplicate queued responses
    private var lastJoinAttemptAt: Long = 0L

    companion object {
        private const val TAG = "DuelRepository"
        private const val JOIN_QUEUE_DEBOUNCE_MS = 3000L
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
                    avatarUrl = message.data.opponent_avatar_url,
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
                    queueSince = null,
                    myScore = 0,
                    opponentScore = 0,
                    currentRound = 0
                )
            }

            is WebSocketMessage.CurrentQuestion -> {
                Log.d(TAG, "Receive question with id: ${message.data.question_id}")
                _duelState.update {
                    it.copy(
                        currentQuestion = Question(
                            id = message.data.question_id,
                            text = message.data.text,
                            roundNumber = message.data.round_number,
                        ),
                    )
                }
                Log.d(TAG, "HERE")
            }

            is WebSocketMessage.ScoreUpdate -> {
                Log.d(TAG, "📊 Score update: ${message.data.scores}")
                val myId = currentUser?.id ?: return
                val myScore = message.data.scores[myId] ?: 0
                val opponentScore =
                    message.data.scores.filterKeys { it != myId }.values.firstOrNull() ?: 0

                _duelState.update {
                    it.copy(
                        myScore = myScore,
                        opponentScore = opponentScore,
                        currentRound = message.data.round_number,
                        lastAnswerCorrect = if (message.data.updated_player_id == myId) message.data.correct else it.lastAnswerCorrect,
                        answerAttempt = if (message.data.updated_player_id == myId) it.answerAttempt + 1 else it.answerAttempt,
                    )
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
                val alreadyRegistered =
                    message.data.message.contains("already registered", ignoreCase = true)
                val unsupportedAction =
                    message.data.message.contains("unsupported action", ignoreCase = true)
                if (unsupportedAction) {
                    // Server may respond with this to periodic ping/no-op messages; don't surface to UI
                    return
                }
                _duelState.value = _duelState.value.copy(
                    error = if (alreadyRegistered) null else message.data.message,
                    isInQueue = alreadyRegistered,
                    isSearching = alreadyRegistered,
                )
            }

            else -> {
                Log.d(TAG, "Unhandled message type: ${message::class.simpleName}")
            }
        }
    }

    private var questionTimerJob: Job? = null

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
        // Use Firebase UID and display name when available
        val fbUser = authRepository.currentUser()
        val resolvedUsername = (fbUser?.displayName?.takeIf { it.isNotBlank() }
            ?: username.ifBlank { "Player" }).trim()
        val resolvedId = fbUser?.uid ?: generateUserId()
        val avatar = fbUser?.photoUrl?.toString()

        val user = DuelUser(
            id = resolvedId,
            username = resolvedUsername,
            avatarUrl = avatar
        )

        currentUser = user

        _duelState.update { it -> it.copy(selectedMode = mode) }

        if (!_duelState.value.isConnected) {
            connectToServer()
            scope.launch {
                // Wait for connection
//                delay(2000)
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
        val now = System.currentTimeMillis()
        if (now - lastJoinAttemptAt < JOIN_QUEUE_DEBOUNCE_MS) {
            Log.d(TAG, "⏱️ Skipping join_queue due to debounce")
            return
        }

        if (!_duelState.value.isConnected) {
            _duelState.value = _duelState.value.copy(
                error = "Not connected to server"
            )
            return
        }
        if (_duelState.value.isInQueue || _duelState.value.isSearching) {
            Log.d(TAG, "🚫 Already queued/searching, skipping join_queue")
            return
        }

        lastJoinAttemptAt = now

        val queuedAt = System.currentTimeMillis()
        _duelState.value = _duelState.value.copy(
            isInQueue = true,
            isSearching = true,
            error = null,
            queuePosition = null,
            queueSince = queuedAt
        )

        val joinQueueData = WebSocketMessage.JoinQueueData(
            player_id = user.id,
            display_name = user.username,
            avatar_url = user.avatarUrl ?: "",
        )

        webSocketService.sendMessage(WebSocketMessage.JoinQueue(data = joinQueueData))
        Log.d(TAG, "📤 Sent join_queue for player: ${user.username}")
    }

    fun leaveQueue() {
        lastJoinAttemptAt = 0L
        disconnect()
        _duelState.value = _duelState.value.copy(
            isInQueue = false,
            isSearching = false,
            queuePosition = null,
            queueSince = null
        )
        connectToServer()
    }

    fun submitAnswer(answer: String) {
        val question = _duelState.value.currentQuestion ?: return
        val user = currentUser ?: return
        val matchId = currentMatchId ?: return

        val isFinal = currentRound >= totalRounds

        questionTimerJob?.cancel()

        val answerData = WebSocketMessage.SubmitAnswerData(
            match_id = matchId,
            player_id = user.id,
            question_id = question.id,
            answer = answer,
            correct = false,
            score_delta = 1,
            final = isFinal,
            round_number = currentRound
        )

        webSocketService.sendMessage(WebSocketMessage.SubmitAnswer(data = answerData))
        Log.d(TAG, "📤 Submitted answer: $answer to question id: ${question.id}")
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
}