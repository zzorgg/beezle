package com.github.zzorgg.beezle.data.remote

import android.util.Log
import com.github.zzorgg.beezle.BuildConfig
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaysockWebSocketService @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

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
        private const val WS_URL: String = BuildConfig.WEBSOCKET_URL
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY_MS = 2000L
    }

    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0

    private val connectionStatusCallback: (ConnectionStatus) -> Unit = { status ->
        _connectionStatus.update { status }
        when (status) {
            ConnectionStatus.CONNECTED -> {
                reconnectAttempts = 0
                Log.d(TAG, "✔ Websocket connected")
            }
            ConnectionStatus.DISCONNECTED -> {
                reconnectAttempts = 0
                webSocket = null
            }
            ConnectionStatus.ERROR -> {
                webSocket = null
                // Auto-reconnect with exponential backoff
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    attemptReconnect()
                }
            }
            else -> {}
        }
    }

    fun connect() {
        if (webSocket != null && _connectionStatus.value == ConnectionStatus.CONNECTED) {
            Log.d(TAG, "WebSocket already connected")
            return
        }

        if (_connectionStatus.value == ConnectionStatus.CONNECTING) {
            Log.d(TAG, "WebSocket is busy connecting...")
            return
        }

        Log.d(TAG, "Initializing websocket connection...")
        _connectionStatus.update { ConnectionStatus.CONNECTING }

        val request = Request.Builder()
            .url(WS_URL)
            .addHeader("Origin", "playsock-mobile://app")
            .build()

        webSocket = client.newWebSocket(request, PlaysockWebSocketListener(_messageChannel, connectionStatusCallback))
    }

    private fun attemptReconnect() {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) return
        if (_connectionStatus.value == ConnectionStatus.RECONNECTING) return

        _connectionStatus.update { ConnectionStatus.RECONNECTING }
        CoroutineScope(Dispatchers.IO).launch {
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
                    WebSocketMessage.Error(data = WebSocketMessage.ErrorData("Failed to send message: ${e.message}"))
                )
            }
        } ?: run {
            Log.w(TAG, "⚠️ WebSocket not connected, cannot send message")
            _messageChannel.trySend(
                WebSocketMessage.Error(data = WebSocketMessage.ErrorData("Not connected to server"))
            )
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 Disconnecting WebSocket")
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
    }
}