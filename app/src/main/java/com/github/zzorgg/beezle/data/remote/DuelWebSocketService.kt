package com.github.zzorgg.beezle.data.remote

import android.util.Log
import com.github.zzorgg.beezle.BuildConfig
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuelWebSocketService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messageChannel = Channel<WebSocketMessage>(Channel.UNLIMITED)
    val messages: Flow<WebSocketMessage> = _messageChannel.receiveAsFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    companion object {
        private const val TAG = "DuelWebSocketService"
        private val WS_URL: String = BuildConfig.WEBSOCKET_URL
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY_MS = 2000L
    }

    private var reconnectJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectAttempts = 0

    fun connect() {
        if (webSocket != null && _isConnected.value) {
            Log.d(TAG, "WebSocket already connected")
            return
        }

        val request = Request.Builder()
            .url(WS_URL)
            .addHeader("Origin", "playsock-mobile://app")
            .build()

        Log.d(TAG, "Connecting to WebSocket: $WS_URL")
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "✅ WebSocket connected successfully")
                _isConnected.value = true
                reconnectAttempts = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "📩 Received: $text")
                try {
                    val element = json.parseToJsonElement(text)
                    val action = element.jsonObject["action"]?.jsonPrimitive?.content

                    when (action) {
                        "queued" -> {
                            val message = json.decodeFromString<WebSocketMessage.Queued>(text)
                            _messageChannel.trySend(message)
                        }
                        "match_found" -> {
                            val message = json.decodeFromString<WebSocketMessage.MatchFound>(text)
                            _messageChannel.trySend(message)
                        }
                        "score_update" -> {
                            val message = json.decodeFromString<WebSocketMessage.ScoreUpdate>(text)
                            _messageChannel.trySend(message)
                        }
                        "opponent_answer" -> {
                            val message = json.decodeFromString<WebSocketMessage.OpponentAnswer>(text)
                            _messageChannel.trySend(message)
                        }
                        "game_over" -> {
                            val message = json.decodeFromString<WebSocketMessage.GameOver>(text)
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
                        null -> {
                            Log.w(TAG, "⚠️ Message missing action field: $text")
                        }
                        else -> {
                            Log.w(TAG, "⚠️ Unknown action '$action': $text")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error parsing message: $text", e)
                    _messageChannel.trySend(
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
                _isConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "🔌 WebSocket closed: $code $reason")
                _isConnected.value = false
                this@DuelWebSocketService.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ WebSocket error", t)
                _isConnected.value = false
                _messageChannel.trySend(
                    WebSocketMessage.Error(
                        data = WebSocketMessage.ErrorData("Connection failed: ${t.message}")
                    )
                )
                this@DuelWebSocketService.webSocket = null

                // Auto-reconnect with exponential backoff
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    attemptReconnect()
                }
            }
        })
    }

    private fun attemptReconnect() {
        reconnectJob?.cancel()
        reconnectJob = serviceScope.launch {
            reconnectAttempts++
            val delayMs = RECONNECT_DELAY_MS * reconnectAttempts
            Log.d(TAG, "🔄 Reconnect attempt $reconnectAttempts in ${delayMs}ms")
            delay(delayMs)
            connect()
        }
    }

    fun sendMessage(message: WebSocketMessage) {
        webSocket?.let { ws ->
            try {
                val jsonString = when (message) {
                    is WebSocketMessage.JoinQueue -> {
                        json.encodeToString(WebSocketMessage.JoinQueue.serializer(), message)
                    }
                    is WebSocketMessage.SubmitAnswer -> {
                        json.encodeToString(WebSocketMessage.SubmitAnswer.serializer(), message)
                    }
                    else -> {
                        json.encodeToString(WebSocketMessage.serializer(), message)
                    }
                }

                Log.d(TAG, "📤 Sending: $jsonString")
                ws.send(jsonString)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending message", e)
                _messageChannel.trySend(
                    WebSocketMessage.Error(
                        data = WebSocketMessage.ErrorData("Failed to send message: ${e.message}")
                    )
                )
            }
        } ?: run {
            Log.w(TAG, "⚠️ WebSocket not connected, cannot send message")
            _messageChannel.trySend(
                WebSocketMessage.Error(
                    data = WebSocketMessage.ErrorData("Not connected to server")
                )
            )
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 Disconnecting WebSocket")
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _isConnected.value = false
        reconnectAttempts = 0
    }

    fun shutdown() {
        disconnect()
        serviceScope.cancel()
    }
}