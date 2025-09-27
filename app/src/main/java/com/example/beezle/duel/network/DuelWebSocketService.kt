package com.example.beezle.duel.network

import android.util.Log
import com.example.beezle.duel.data.WebSocketMessage
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

    private val _messageChannel = Channel<WebSocketMessage>(Channel.UNLIMITED)
    val messages: Flow<WebSocketMessage> = _messageChannel.receiveAsFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val TAG = "DuelWebSocketService"
        private const val WS_URL = "ws://localhost:8080/ws"
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
                    // Parse the message based on its type
                    when {
                        text.contains("\"JoinQueue\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.JoinQueue>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"MatchFound\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.MatchFound>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"QuestionReceived\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.QuestionReceived>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"AnswerSubmitted\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.AnswerSubmitted>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"RoundResult\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.RoundResult>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"DuelComplete\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.DuelComplete>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"OpponentLeft\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.OpponentLeft>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"Error\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.Error>(text)
                            _messageChannel.trySend(message)
                        }
                        text.contains("\"Pong\"") -> {
                            val message = json.decodeFromString<WebSocketMessage.Pong>(text)
                            _messageChannel.trySend(message)
                        }
                        else -> {
                            Log.w(TAG, "Unknown message type: $text")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: $text", e)
                    _messageChannel.trySend(WebSocketMessage.Error("Failed to parse message"))
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
                _messageChannel.trySend(WebSocketMessage.Error("Connection failed: ${t.message}"))
                this@DuelWebSocketService.webSocket = null
            }
        })
    }

    fun sendMessage(message: WebSocketMessage) {
        webSocket?.let { ws ->
            try {
                val jsonString = json.encodeToString(message)
                Log.d(TAG, "Sending message: $jsonString")
                ws.send(jsonString)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _messageChannel.trySend(WebSocketMessage.Error("Failed to send message"))
            }
        } ?: run {
            Log.w(TAG, "WebSocket not connected, cannot send message")
            _messageChannel.trySend(WebSocketMessage.Error("Not connected to server"))
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
