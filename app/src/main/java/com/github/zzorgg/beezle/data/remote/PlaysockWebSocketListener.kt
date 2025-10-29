package com.github.zzorgg.beezle.data.remote

import android.util.Log
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

private const val TAG = "DuelWebSocketListener"

class PlaysockWebSocketListener(
    private val messageChannel: Channel<WebSocketMessage>,
    private val connectionStatusCallback: (ConnectionStatus) -> Unit,
) : WebSocketListener() {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "✅ WebSocket connected successfully")
        connectionStatusCallback(ConnectionStatus.CONNECTED)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "📩 Received: $text")
        try {
            val element = json.parseToJsonElement(text)
            val action = element.jsonObject["action"]?.jsonPrimitive?.content

            when (action) {
                "queued" -> {
                    val message = json.decodeFromString<WebSocketMessage.Queued>(text)
                    messageChannel.trySend(message)
                }

                "match_found" -> {
                    val message = json.decodeFromString<WebSocketMessage.MatchFound>(text)
                    messageChannel.trySend(message)
                }

                "score_update" -> {
                    val message = json.decodeFromString<WebSocketMessage.ScoreUpdate>(text)
                    messageChannel.trySend(message)
                }

                "opponent_answer" -> {
                    val message =
                        json.decodeFromString<WebSocketMessage.OpponentAnswer>(text)
                    messageChannel.trySend(message)
                }

                "game_over" -> {
                    val message = json.decodeFromString<WebSocketMessage.GameOver>(text)
                    messageChannel.trySend(message)
                }

                "opponent_left" -> {
                    val message = json.decodeFromString<WebSocketMessage.OpponentLeft>(text)
                    messageChannel.trySend(message)
                }

                "error" -> {
                    val message = json.decodeFromString<WebSocketMessage.Error>(text)
                    messageChannel.trySend(message)
                }

                "current_question" -> {
                    val message =
                        json.decodeFromString<WebSocketMessage.CurrentQuestion>(text)
                    messageChannel.trySend(message)
                }

                null -> {
                    Log.w(TAG, "⚠️ Message missing action field: $text")
                }

                else -> {
                    Log.w(TAG, "⚠️ Unknown action '$action': $text")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing message: $text", e)
            messageChannel.trySend(
                WebSocketMessage.Error(
                    data = WebSocketMessage.ErrorData("Failed to parse server message: ${e.message}")
                )
            )
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "🔌 WebSocket closing: $code $reason")
        connectionStatusCallback(ConnectionStatus.DISCONNECTING)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "🔌 WebSocket closed: $code $reason")
        connectionStatusCallback(ConnectionStatus.DISCONNECTED)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "❌ WebSocket error", t)
        connectionStatusCallback(ConnectionStatus.ERROR)
        messageChannel.trySend(
            WebSocketMessage.Error(
                data = WebSocketMessage.ErrorData("Connection failed: ${t.message}")
            )
        )
    }
}