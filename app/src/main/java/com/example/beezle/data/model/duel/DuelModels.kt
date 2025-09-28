package com.example.beezle.data.model.duel

import kotlinx.serialization.Serializable

@Serializable
data class DuelUser(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val score: Int = 0
)

@Serializable
data class DuelRoom(
    val id: String,
    val player1: DuelUser,
    val player2: DuelUser? = null,
    val status: DuelStatus,
    val currentQuestion: Question? = null,
    val timeRemaining: Int = 15,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswer: Int,
    val timeLimit: Int = 15
)

@Serializable
enum class DuelStatus {
    WAITING_FOR_PLAYER,
    IN_PROGRESS,
    QUESTION_DISPLAYED,
    WAITING_FOR_ANSWERS,
    ROUND_COMPLETE,
    FINISHED
}

@Serializable
sealed class WebSocketMessage {
    @Serializable
    data class JoinQueue(val user: DuelUser) : WebSocketMessage()

    @Serializable
    data class MatchFound(val room: DuelRoom) : WebSocketMessage()

    @Serializable
    data class QuestionReceived(val question: Question, val timeRemaining: Int) : WebSocketMessage()

    @Serializable
    data class AnswerSubmitted(val userId: String, val answer: Int, val isCorrect: Boolean) : WebSocketMessage()

    @Serializable
    data class RoundResult(
        val player1Correct: Boolean?,
        val player2Correct: Boolean?,
        val player1Score: Int,
        val player2Score: Int,
        val timeUp: Boolean = false
    ) : WebSocketMessage()

    @Serializable
    data class DuelComplete(val winnerId: String?, val finalScores: Map<String, Int>) : WebSocketMessage()

    @Serializable
    data class OpponentLeft(val reason: String) : WebSocketMessage()

    @Serializable
    data class Error(val message: String) : WebSocketMessage()

    @Serializable
    data class Ping(val timestamp: Long = System.currentTimeMillis()) : WebSocketMessage()

    @Serializable
    data class Pong(val timestamp: Long) : WebSocketMessage()
}

data class DuelState(
    val isConnected: Boolean = false,
    val isInQueue: Boolean = false,
    val currentRoom: DuelRoom? = null,
    val currentQuestion: Question? = null,
    val timeRemaining: Int = 15,
    val selectedAnswer: Int? = null,
    val hasAnswered: Boolean = false,
    val lastRoundResult: WebSocketMessage.RoundResult? = null,
    val error: String? = null,
    val isSearching: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
    RECONNECTING
}
