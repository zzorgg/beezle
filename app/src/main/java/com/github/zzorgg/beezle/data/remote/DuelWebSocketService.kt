package com.github.zzorgg.beezle.data.remote

import android.util.Log
import com.github.zzorgg.beezle.BuildConfig
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    private var pingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val HEARTBEAT_INTERVAL_MS = 20_000L

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
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
                try {
                    val element = json.parseToJsonElement(text)
                    val action = element.jsonObject["action"]?.jsonPrimitive?.content
                    when (action) {
                        "join_queue" -> {
                            val message = json.decodeFromString<WebSocketMessage.JoinQueue>(text)
                            _messageChannel.trySend(message)
                        }
                        "queued" -> {
                            val message = json.decodeFromString<WebSocketMessage.Queued>(text)
                            _messageChannel.trySend(message)
                        }
                        "match_found" -> {
                            val message = json.decodeFromString<WebSocketMessage.MatchFound>(text)
                            _messageChannel.trySend(message)
                        }
                        "question" -> {
                            val message = json.decodeFromString<WebSocketMessage.QuestionReceived>(text)
                            _messageChannel.trySend(message)
                        }
                        "submit_answer" -> {
                            val message = json.decodeFromString<WebSocketMessage.AnswerSubmitted>(text)
                            _messageChannel.trySend(message)
                        }
                        "round_result" -> {
                            val message = json.decodeFromString<WebSocketMessage.RoundResult>(text)
                            _messageChannel.trySend(message)
                        }
                        "duel_complete" -> {
                            val message = json.decodeFromString<WebSocketMessage.DuelComplete>(text)
                            _messageChannel.trySend(message)
                        }
                        "opponent_left" -> {
                            val message = json.decodeFromString<WebSocketMessage.OpponentLeft>(text)
                            _messageChannel.trySend(message)
                        }
                        "error" -> {
                            val message = json.decodeFromString<WebSocketMessage.Error>(text)
                            _messageChannel.trySend(message)
                        }
                        "pong" -> {
                            val message = json.decodeFromString<WebSocketMessage.Pong>(text)
                            _messageChannel.trySend(message)
                        }
                        "ping" -> {
                            // Optionally auto-respond with pong if needed in future
                            Log.d(TAG, "Ping received (no auto-response implemented)")
                        }
                        "ready" -> {
                            val message = json.decodeFromString<WebSocketMessage.Ready>(text)
                            _messageChannel.trySend(message)
                        }
                        null -> {
                            Log.w(TAG, "Message missing action field: $text")
                        }
                        else -> {
                            Log.w(TAG, "Unknown action '$action': $text")
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
                stopHeartbeat()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                _isConnected.value = false
                this@DuelWebSocketService.webSocket = null
                stopHeartbeat()
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
                stopHeartbeat()
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

    fun sendRaw(jsonString: String) {
        webSocket?.let { ws ->
            try {
                Log.d(TAG, "Sending raw: $jsonString")
                ws.send(jsonString)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending raw message", e)
                _messageChannel.trySend(
                    WebSocketMessage.Error(
                        data = WebSocketMessage.ErrorData("Failed to send raw message")
                    )
                )
            }
        } ?: run {
            Log.w(TAG, "WebSocket not connected, cannot send raw message")
        }
    }

    private fun startHeartbeat() {
        if (pingJob?.isActive == true) return
        pingJob = serviceScope.launch {
            while (true) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (_isConnected.value) {
                    try {
                        val pingJson = """{"action":"ping"}"""
                        webSocket?.send(pingJson)
                        Log.d(TAG, "Sent ping")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send ping", e)
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun stopHeartbeat() {
        pingJob?.cancel()
        pingJob = null
    }

    fun shutdown() {
        stopHeartbeat()
        serviceScope.cancel()
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _isConnected.value = false
        stopHeartbeat()
    }

    fun reconnect() {
        disconnect()
        connect()
    }
}