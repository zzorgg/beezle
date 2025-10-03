package com.github.zzorgg.beezle.data.model.duel

import kotlinx.serialization.Serializable

@Serializable
data class DuelUser(
    val id: String,
    val username: String
)

// Duel mode selection
enum class DuelMode {
    MATH,
    CS,
    GENERAL
}

@Serializable
data class DuelRoom(
    val id: String,
    val player1: DuelUser,
    val player2: DuelUser? = null,
    val status: DuelStatus,
    val currentQuestion: Question? = null,
    val timeRemaining: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val scores: Map<String, Int> = emptyMap(),
    val betAmount: Double = 0.0,
    val betToken: String = "SOL"
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswer: Int,
    val timeLimit: Int = 15,
    val roundNumber: Int = 1
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
    data class JoinQueue(
        val action: String = "join_queue",
        val data: JoinQueueData
    ) : WebSocketMessage()

    @Serializable
    data class JoinQueueData(
        val player_id: String,
        val display_name: String,
        val bet_amount: Double = 0.0,
        val bet_token: String = "SOL"
    )

    @Serializable
    data class MatchFound(
        val action: String = "match_found",
        val data: MatchFoundData
    ) : WebSocketMessage()

    @Serializable
    data class MatchFoundData(
        val match_id: String,
        val player_id: String,
        val opponent_id: String,
        val opponent_name: String,
        val bet_amount: Double,
        val bet_token: String,
        val queue_delta_ms: Long? = null
    )

    @Serializable
    data class ScoreUpdate(
        val action: String = "score_update",
        val data: ScoreUpdateData
    ) : WebSocketMessage()

    @Serializable
    data class ScoreUpdateData(
        val match_id: String,
        val scores: Map<String, Int>,
        val updated_player_id: String,
        val correct: Boolean,
        val question_id: String,
        val round_number: Int
    )

    @Serializable
    data class OpponentAnswer(
        val action: String = "opponent_answer",
        val data: OpponentAnswerData
    ) : WebSocketMessage()

    @Serializable
    data class OpponentAnswerData(
        val match_id: String,
        val player_id: String,
        val question_id: String,
        val answer: String,
        val round_number: Int,
        val correct: Boolean
    )

    @Serializable
    data class SubmitAnswer(
        val action: String = "submit_answer",
        val data: SubmitAnswerData
    ) : WebSocketMessage()

    @Serializable
    data class SubmitAnswerData(
        val match_id: String,
        val player_id: String,
        val question_id: String,
        val answer: String,
        val correct: Boolean,
        val score_delta: Int,
        val final: Boolean = false,
        val round_number: Int
    )

    @Serializable
    data class GameOver(
        val action: String = "game_over",
        val data: GameOverData
    ) : WebSocketMessage()

    @Serializable
    data class GameOverData(
        val match_id: String,
        val winner_id: String?,
        val reason: String,
        val scores: Map<String, Int>,
        val bet_amount: Double,
        val bet_token: String
    )

    @Serializable
    data class OpponentLeft(
        val action: String = "opponent_left",
        val data: OpponentLeftData
    ) : WebSocketMessage()

    @Serializable
    data class OpponentLeftData(
        val match_id: String,
        val opponent_id: String
    )

    @Serializable
    data class Error(
        val action: String = "error",
        val data: ErrorData
    ) : WebSocketMessage()

    @Serializable
    data class ErrorData(
        val message: String
    )

    @Serializable
    data class Queued(
        val action: String = "queued",
        val data: QueuedData
    ) : WebSocketMessage()

    @Serializable
    data class QueuedData(
        val position: Int
    )
}

data class DuelState(
    val isConnected: Boolean = false,
    val isInQueue: Boolean = false,
    val currentRoom: DuelRoom? = null,
    val currentQuestion: Question? = null,
    val timeRemaining: Int = 15,
    val selectedAnswer: Int? = null,
    val hasAnswered: Boolean = false,
    val lastGameResult: WebSocketMessage.GameOver? = null,
    val error: String? = null,
    val isSearching: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val queuePosition: Int? = null,
    val queueSince: Long? = null,
    val selectedMode: DuelMode? = null,
    val currentRound: Int = 0,
    val totalRounds: Int = 5,
    val myScore: Int = 0,
    val opponentScore: Int = 0,
    val opponentAnswered: Boolean = false
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
    RECONNECTING
}
