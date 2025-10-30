package com.github.zzorgg.beezle.data.wallet

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.BuildConfig
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.common.signin.SignInWithSolana
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// DataStore delegate at file level
private val Context.walletDataStore by preferencesDataStore(name = "wallet_prefs")

// Lightweight Base58 encoder for Solana public keys
private object Base58 {
    private const val ALPHABET_STR = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val ALPHABET = ALPHABET_STR.toCharArray()

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        // Count leading zeros
        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) zeros++
        // Copy input since we are going to modify it in-place
        val inputCopy = input.copyOf()
        val encoded = StringBuilder()
        var startAt = zeros
        while (startAt < inputCopy.size) {
            var carry = 0
            for (i in startAt until inputCopy.size) {
                val x = (inputCopy[i].toInt() and 0xFF)
                val num = (carry shl 8) or x
                inputCopy[i] = (num / 58).toByte()
                carry = num % 58
            }
            encoded.append(ALPHABET[carry])
            while (startAt < inputCopy.size && inputCopy[startAt].toInt() == 0) startAt++
        }
        repeat(zeros) { encoded.append('1') }
        return encoded.reverse().toString()
    }
}

data class WalletState(
    val isConnected: Boolean = false,
    val publicKey: String? = null,
    val authToken: String? = null,
    val walletName: String? = null,
    val balance: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val wasRestored: Boolean = false // indicates restored from persistence
)

class SolanaWalletManager(application: Application) : AndroidViewModel(application) {

    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState

    private lateinit var walletAdapter: MobileWalletAdapter

    // Solana RPC
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    // Default to testnet endpoint; can be overridden via setRpcEndpoint()
    private var rpcEndpoint = "https://api.testnet.solana.com"

    // DataStore keys
    private val DS_CONNECTED = booleanPreferencesKey("wallet_connected")
    private val DS_PUBLIC_KEY = stringPreferencesKey("wallet_public_key")
    private val DS_WALLET_NAME = stringPreferencesKey("wallet_name")
    private val DS_AUTH_TOKEN = stringPreferencesKey("wallet_auth_token")

    private val prefs = application.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "SolanaWalletManager"
        private const val KEY_CONNECTED = "wallet_connected"
        private const val KEY_PUBLIC_KEY = "wallet_public_key"
        private const val KEY_WALLET_NAME = "wallet_name"
        private const val KEY_AUTH_TOKEN = "wallet_auth_token"
    }

    init {
        initializeWalletAdapter()
        restoreWalletIfNeeded()
    }

    private suspend fun persistWithDataStore(connected: Boolean, publicKey: String?, walletName: String?, authToken: String?) {
        try {
            getApplication<Application>().walletDataStore.edit { mutablePrefs ->
                mutablePrefs[DS_CONNECTED] = connected
                if (connected) {
                    publicKey?.let { mutablePrefs[DS_PUBLIC_KEY] = it }
                    walletName?.let { mutablePrefs[DS_WALLET_NAME] = it }
                    authToken?.let { mutablePrefs[DS_AUTH_TOKEN] = it }
                } else {
                    mutablePrefs.remove(DS_PUBLIC_KEY)
                    mutablePrefs.remove(DS_WALLET_NAME)
                    mutablePrefs.remove(DS_AUTH_TOKEN)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "DataStore persist failed, falling back to SharedPreferences", e)
            persistWalletState(connected, publicKey, walletName, authToken)
        }
    }

    private suspend fun restoreFromDataStore(): Boolean {
        return try {
            val prefs = getApplication<Application>().walletDataStore.data.first()
            val connected = prefs[DS_CONNECTED] ?: false
            if (connected) {
                _walletState.value = _walletState.value.copy(
                    isConnected = true,
                    publicKey = prefs[DS_PUBLIC_KEY],
                    walletName = prefs[DS_WALLET_NAME],
                    authToken = prefs[DS_AUTH_TOKEN],
                    balance = null,
                    wasRestored = true
                )
                // Fetch actual balance after restore
                prefs[DS_PUBLIC_KEY]?.let { fetchBalance(it) }
            }
            connected
        } catch (e: Exception) {
            Log.w(TAG, "DataStore restore failed, trying SharedPreferences", e)
            false
        }
    }

    private fun restoreWalletIfNeeded() {
        viewModelScope.launch {
            val dsRestored = restoreFromDataStore()
            if (!dsRestored) {
                // fallback to previous SharedPreferences logic
                try {
                    if (prefs.getBoolean(KEY_CONNECTED, false)) {
                        val publicKey = prefs.getString(KEY_PUBLIC_KEY, null)
                        val walletName = prefs.getString(KEY_WALLET_NAME, null)
                        val authToken = prefs.getString(KEY_AUTH_TOKEN, null)
                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = publicKey,
                            walletName = walletName,
                            authToken = authToken,
                            balance = null,
                            wasRestored = true
                        )
                        Log.d(TAG, "Restored wallet from SharedPreferences fallback")
                        publicKey?.let { fetchBalance(it) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore wallet state", e)
                }
            }
        }
    }

    private fun persistWalletState(connected: Boolean, publicKey: String?, walletName: String?, authToken: String?) {
        try {
            prefs.edit().apply {
                putBoolean(KEY_CONNECTED, connected)
                if (connected) {
                    putString(KEY_PUBLIC_KEY, publicKey)
                    putString(KEY_WALLET_NAME, walletName)
                    putString(KEY_AUTH_TOKEN, authToken)
                } else {
                    remove(KEY_PUBLIC_KEY)
                    remove(KEY_WALLET_NAME)
                    remove(KEY_AUTH_TOKEN)
                }
            }.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist wallet state", e)
        }
    }

    private fun initializeWalletAdapter() {
        try {
            val beezleUri = BuildConfig.BEEZLE_WEBPAGE_URL.toUri()
            val iconUri = "/logo.png".toUri()
            val identityName = "Beezle - Solana Dueling Game"
            walletAdapter = MobileWalletAdapter(
                connectionIdentity = ConnectionIdentity(
                    identityUri = beezleUri,
                    iconUri = iconUri,
                    identityName = identityName
                )
            )
            Log.d(TAG, "Wallet adapter initialized successfully with iconUri=$iconUri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize wallet adapter", e)
            _walletState.value = _walletState.value.copy(
                error = "Failed to initialize wallet: ${e.message}"
            )
        }
    }

    fun connectWallet(sender: ActivityResultSender) {
        viewModelScope.launch {
            Log.d(TAG, "Starting wallet connection...")
            _walletState.value = _walletState.value.copy(isLoading = true, error = null)

            try {
                val result = walletAdapter.connect(sender)
                Log.d(TAG, "Connect result received: ${result::class.simpleName}")

                when (result) {
                    is TransactionResult.Success -> {
                        val authResult = result.authResult
                        // Prefer the first account returned by the wallet (the selected/authorized one)
                        val account = authResult.accounts.firstOrNull()
                        val pubKeyBase58 = account?.publicKey?.let { bytes ->
                            try { Base58.encode(bytes) } catch (e: Exception) { Log.e(TAG, "Error base58 encoding public key", e); null }
                        }
                        val walletName = authResult.walletUriBase?.host ?: "Phantom"
                        val authToken = authResult.authToken
                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = pubKeyBase58,
                            authToken = authToken,
                            walletName = walletName,
                            isLoading = false,
                            error = null,
                            wasRestored = false // This is a fresh connection, not a restore
                        )
                        persistWithDataStore(true, pubKeyBase58, walletName, authToken)
                        pubKeyBase58?.let { fetchBalance(it) }
                    }
                    is TransactionResult.NoWalletFound -> {
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "No MWA wallet found! Please install Phantom wallet from Google Play Store and try again."
                        )
                    }
                    is TransactionResult.Failure -> {
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "Connection failed: ${result.e.localizedMessage ?: result.e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection error", e)
                _walletState.value = _walletState.value.copy(
                    isLoading = false,
                    error = "Connection error: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    fun signInWithSolana(sender: ActivityResultSender) {
        viewModelScope.launch {
            Log.d(TAG, "Starting Sign In with Solana...")
            _walletState.value = _walletState.value.copy(isLoading = true, error = null)

            try {
                val result = walletAdapter.signIn(
                    sender,
                    SignInWithSolana.Payload(
                        "beezle.app",
                        "Sign in to Beezle - Your Solana Dueling Game"
                    )
                )

                when (result) {
                    is TransactionResult.Success -> {
                        val authResult = result.authResult
                        val account = authResult.accounts.firstOrNull()
                        val pubKeyBase58 = account?.publicKey?.let { bytes ->
                            try { Base58.encode(bytes) } catch (e: Exception) { Log.e(TAG, "Error base58 encoding public key", e); null }
                        }
                        val walletName = authResult.walletUriBase?.host ?: "Phantom"
                        val authToken = authResult.authToken
                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = pubKeyBase58,
                            authToken = authToken,
                            walletName = walletName,
                            isLoading = false,
                            error = null
                        )
                        persistWithDataStore(true, pubKeyBase58, walletName, authToken)
                        pubKeyBase58?.let { fetchBalance(it) }
                    }
                    is TransactionResult.NoWalletFound -> {
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "No MWA wallet found! Please install Phantom wallet and try again."
                        )
                    }
                    is TransactionResult.Failure -> {
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "Sign in failed: ${result.e.localizedMessage ?: result.e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
                _walletState.value = _walletState.value.copy(
                    isLoading = false,
                    error = "Sign-in error: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    fun disconnectWallet(sender: ActivityResultSender) {
        viewModelScope.launch {
            try {
                walletAdapter.disconnect(sender)
            } catch (e: Exception) {
                Log.e(TAG, "Disconnect error", e)
            } finally {
                _walletState.value = WalletState()
                persistWithDataStore(false, null, null, null)
            }
        }
    }

    fun signMessage(sender: ActivityResultSender, message: String) {
        if (!_walletState.value.isConnected) {
            _walletState.value = _walletState.value.copy(error = "Please connect your wallet first")
            return
        }
        viewModelScope.launch {
            _walletState.value = _walletState.value.copy(isLoading = true, error = null)
            try {
                val result = walletAdapter.transact(sender) { authResult ->
                    signMessagesDetached(
                        arrayOf(message.toByteArray()),
                        arrayOf(authResult.accounts.first().publicKey)
                    )
                }
                when (result) {
                    is TransactionResult.Success -> {
                        _walletState.value = _walletState.value.copy(isLoading = false, error = "Message signed successfully!")
                    }
                    is TransactionResult.Failure -> {
                        _walletState.value = _walletState.value.copy(isLoading = false, error = "Failed to sign message: ${result.e.localizedMessage ?: result.e.message}")
                    }
                    else -> {
                        _walletState.value = _walletState.value.copy(isLoading = false, error = "Unexpected error during message signing")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Message signing error", e)
                _walletState.value = _walletState.value.copy(isLoading = false, error = "Message signing error: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    private fun fetchBalance(address: String) {
        viewModelScope.launch {
            try {
                val sol = getSolBalance(address)
                _walletState.value = _walletState.value.copy(balance = sol)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching balance", e)
            }
        }
    }

    private suspend fun getSolBalance(address: String): Double? = withContext(Dispatchers.IO) {
        try {
            val mediaType = "application/json".toMediaType()
            val bodyString = "{" +
                    "\"jsonrpc\":\"2.0\"," +
                    "\"id\":1," +
                    "\"method\":\"getBalance\"," +
                    "\"params\":[\"$address\"]" +
                    "}"
            val request = Request.Builder()
                .url(rpcEndpoint)
                .post(bodyString.toRequestBody(mediaType))
                .build()
            httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "RPC getBalance failed: HTTP ${'$'}{resp.code}")
                    return@withContext null
                }
                val text = resp.body.string()
                val root = json.parseToJsonElement(text).jsonObject
                val resultObj = root["result"]?.jsonObject ?: return@withContext null
                val value = resultObj["value"]?.jsonPrimitive?.longOrNull
                return@withContext value?.let { it.toDouble() / 1_000_000_000.0 }
            }
        } catch (e: Exception) {
            Log.e(TAG, "RPC error", e)
            null
        }
    }

    fun clearError() {
        _walletState.value = _walletState.value.copy(error = null)
    }

    fun setRpcEndpoint(url: String) {
        rpcEndpoint = url
    }
}
