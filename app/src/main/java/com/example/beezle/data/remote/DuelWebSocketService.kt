package com.example.beezle.data.remote

import android.util.Log
import com.example.beezle.BuildConfig
import com.example.beezle.data.model.duel.WebSocketMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuelWebSocketService @Inject constructor() {

    private val client = OkHttpClient.Builder().build()
    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messageChannel = Channel<WebSocketMessage>(Channel.Factory.UNLIMITED)
    val messages: Flow<WebSocketMessage> = _messageChannel.receiveAsFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "#type" // Change from default to avoid conflicts
    }

    companion object {
        private const val TAG = "DuelWebSocketService"
        // Use value provided via Gradle BuildConfig so it can differ per build type
        private val WS_URL: String = BuildConfig.WEBSOCKET_URL
    }

    fun connect() {
        if (webSocket != null) {
            Log.d(TAG, "WebSocket already connected")
            return
        }

        val request = Request.Builder()
            .url(WS_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                _isConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                try {
                    // Parse the message based on the action field
                    when {
                        text.contains("\"action\":\"join_queue\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.JoinQueue>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"queued\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.Queued>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"match_found\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.MatchFound>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"question\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.QuestionReceived>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"submit_answer\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.AnswerSubmitted>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"round_result\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.RoundResult>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"duel_complete\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.DuelComplete>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"opponent_left\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.OpponentLeft>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"error\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.Error>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"action\":\"pong\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.Pong>(text)
                            _messageChannel.trySend(message)
                        }
                        else -> {
                            Log.w(TAG, "Unknown message type: $text")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: $text", e)
                    _messageChannel.trySend(
                        WebSocketMessage.Error(
                            data = WebSocketMessage.ErrorData("Failed to parse message")
                        )
                    )
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessage(webSocket, bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code $reason")
                _isConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                _isConnected.value = false
                this@DuelWebSocketService.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error", t)
                _isConnected.value = false
                _messageChannel.trySend(
                    WebSocketMessage.Error(
                        data = WebSocketMessage.ErrorData("Connection failed: ${t.message}")
                    )
                )
                this@DuelWebSocketService.webSocket = null
            }
        })
    }

    fun sendMessage(message: WebSocketMessage) {
        webSocket?.let { ws ->
            try {
                // Create the JSON manually to ensure correct format
                val jsonString = when (message) {
                    is WebSocketMessage.JoinQueue -> {
                        """{"action":"join_queue","data":{"player_id":"${message.data.player_id}","display_name":"${message.data.display_name}"}}"""
                    }
                    is WebSocketMessage.AnswerSubmitted -> {
                        """{"action":"submit_answer","data":{"player_id":"${message.data.player_id}","question_id":"${message.data.question_id}","answer_index":${message.data.answer_index}}}"""
                    }
                    else -> {
                        // For other messages, use regular serialization
                        json.encodeToString(message)
                    }
                }

                Log.d(TAG, "Sending message: $jsonString")
                ws.send(jsonString)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _messageChannel.trySend(
                    WebSocketMessage.Error(
                        data = WebSocketMessage.ErrorData("Failed to send message")
                    )
                )
            }
        } ?: run {
            Log.w(TAG, "WebSocket not connected, cannot send message")
            _messageChannel.trySend(
                WebSocketMessage.Error(
                    data = WebSocketMessage.ErrorData("Not connected to server")
                )
            )
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _isConnected.value = false
    }

    fun reconnect() {
        disconnect()
        connect()
    }
}