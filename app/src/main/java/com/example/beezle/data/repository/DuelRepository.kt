package com.example.beezle.data.repository

import android.util.Log
import com.example.beezle.data.model.duel.ConnectionStatus
import com.example.beezle.data.model.duel.DuelState
import com.example.beezle.data.model.duel.DuelUser
import com.example.beezle.data.model.duel.WebSocketMessage
import com.example.beezle.data.remote.DuelWebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    }

    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message) {
            is WebSocketMessage.MatchFound -> {
                _duelState.value = _duelState.value.copy(
                    isInQueue = false,
                    isSearching = false,
                    currentRoom = message.room,
                    error = null
                )
            }

            is WebSocketMessage.QuestionReceived -> {
                questionStartTime = System.currentTimeMillis()
                _duelState.value = _duelState.value.copy(
                    currentQuestion = message.question,
                    timeRemaining = message.timeRemaining,
                    selectedAnswer = null,
                    hasAnswered = false,
                    error = null
                )
                startQuestionTimer()
            }

            is WebSocketMessage.RoundResult -> {
                _duelState.value = _duelState.value.copy(
                    lastRoundResult = message,
                    hasAnswered = false,
                    selectedAnswer = null,
                    currentQuestion = null,
                    timeRemaining = 0
                )

                // Update room scores if we have a room
                _duelState.value.currentRoom?.let { room ->
                    val updatedRoom = room.copy(
                        player1Score = message.player1Score,
                        player2Score = message.player2Score
                    )
                    _duelState.value = _duelState.value.copy(currentRoom = updatedRoom)
                }
            }

            is WebSocketMessage.DuelComplete -> {
                _duelState.value = _duelState.value.copy(
                    currentRoom = null,
                    currentQuestion = null,
                    timeRemaining = 0,
                    selectedAnswer = null,
                    hasAnswered = false,
                    isInQueue = false,
                    isSearching = false
                )
            }

            is WebSocketMessage.OpponentLeft -> {
                _duelState.value = _duelState.value.copy(
                    error = "Opponent left the duel: ${message.reason}",
                    currentRoom = null,
                    currentQuestion = null,
                    isInQueue = false,
                    isSearching = false
                )
            }

            is WebSocketMessage.Error -> {
                _duelState.value = _duelState.value.copy(
                    error = message.message,
                    isInQueue = false,
                    isSearching = false
                )
            }

            else -> {
                Log.d(TAG, "Unhandled message type: ${message::class.simpleName}")
            }
        }
    }

    private fun startQuestionTimer() {
        scope.launch {
            repeat(QUESTION_TIME_LIMIT) { second ->
                delay(1000)
                val remaining = QUESTION_TIME_LIMIT - second - 1
                _duelState.value = _duelState.value.copy(timeRemaining = remaining)

                if (remaining <= 0) {
                    // Time's up, submit null answer if not already answered
                    if (!_duelState.value.hasAnswered) {
                        submitAnswer(-1) // -1 indicates no answer/timeout
                    }
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
        webSocketService.disconnect()
        _duelState.value = DuelState()
    }

    fun joinQueue(user: DuelUser) {
        if (!_duelState.value.isConnected) {
            _duelState.value = _duelState.value.copy(
                error = "Not connected to server"
            )
            return
        }

        currentUser = user
        _duelState.value = _duelState.value.copy(
            isInQueue = true,
            isSearching = true,
            error = null
        )

        webSocketService.sendMessage(WebSocketMessage.JoinQueue(user))
    }

    fun leaveQueue() {
        _duelState.value = _duelState.value.copy(
            isInQueue = false,
            isSearching = false
        )
        // Send leave queue message to server if needed
    }

    fun submitAnswer(answerIndex: Int) {
        val question = _duelState.value.currentQuestion
        val user = currentUser

        if (question == null || user == null || _duelState.value.hasAnswered) {
            return
        }

        val isCorrect = answerIndex == question.correctAnswer

        _duelState.value = _duelState.value.copy(
            selectedAnswer = answerIndex,
            hasAnswered = true
        )

        webSocketService.sendMessage(
            WebSocketMessage.AnswerSubmitted(
                userId = user.id,
                answer = answerIndex,
                isCorrect = isCorrect
            )
        )
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
}