package com.github.zzorgg.beezle.data.model.duel

import kotlinx.serialization.Serializable

@Serializable
data class DuelUser(
    val id: String,
    val username: String,
    val avatarUrl: String?
)

@Serializable
data class DuelRoom(
    val id: String,
    val player1: DuelUser,
    val player2: DuelUser? = null,
    val status: DuelStatus,
    val currentQuestion: Question? = null,
    val timeRemaining: Int = 15,
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
    data class JoinQueue(
        val action: String = "join_queue",
        val data: JoinQueueData
    ) : WebSocketMessage()

    @Serializable
    data class JoinQueueData(
        val player_id: String,
        val display_name: String
    )

    @Serializable
    data class MatchFound(
        val action: String = "match_found",
        val data: MatchFoundData
    ) : WebSocketMessage()

    @Serializable
    data class MatchFoundData(
        val match_id: String,
        val opponent_id: String,
        val opponent_name: String,
        val player_id: String
    )

    @Serializable
    data class QuestionReceived(
        val action: String = "question",
        val data: QuestionData
    ) : WebSocketMessage()

    @Serializable
    data class QuestionData(
        val question_id: String,
        val question_text: String,
        val options: List<String>,
        val time_limit: Int
    )

    @Serializable
    data class AnswerSubmitted(
        val action: String = "submit_answer",
        val data: AnswerData
    ) : WebSocketMessage()

    @Serializable
    data class AnswerData(
        val player_id: String,
        val question_id: String,
        val answer_index: Int
    )

    @Serializable
    data class RoundResult(
        val action: String = "round_result",
        val data: RoundResultData
    ) : WebSocketMessage()

    @Serializable
    data class RoundResultData(
        val player1_correct: Boolean?,
        val player2_correct: Boolean?,
        val correct_answer: Int
    )

    @Serializable
    data class DuelComplete(
        val action: String = "duel_complete",
        val data: DuelCompleteData
    ) : WebSocketMessage()

    @Serializable
    data class DuelCompleteData(
        val winner_id: String?
    )

    @Serializable
    data class OpponentLeft(
        val action: String = "opponent_left",
        val data: OpponentLeftData
    ) : WebSocketMessage()

    @Serializable
    data class OpponentLeftData(
        val reason: String
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

    @Serializable
    data class Ping(val action: String = "ping") : WebSocketMessage()

    @Serializable
    data class Pong(val action: String = "pong") : WebSocketMessage()

    @Serializable
    data class Ready(
        val action: String = "ready",
        val data: ReadyData
    ) : WebSocketMessage() {
        @Serializable
        data class ReadyData(
            val match_id: String,
            val player_id: String
        )
    }
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
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val queuePosition: Int? = null,
    val queueSince: Long? = null
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
    RECONNECTING
}
