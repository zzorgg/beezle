package com.example.beezle.wallet

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solana.mobilewalletadapter.clientlib.*
import com.solana.mobilewalletadapter.common.signin.SignInWithSolana
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.beezle.R

// DataStore delegate at file level
private val Context.walletDataStore by preferencesDataStore(name = "wallet_prefs")

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
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private lateinit var walletAdapter: MobileWalletAdapter

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
                    balance = 1.25,
                    wasRestored = true
                )
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
                            balance = 1.25,
                            wasRestored = true
                        )
                        Log.d(TAG, "Restored wallet from SharedPreferences fallback")
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
            val solanaUri = Uri.parse("https://solana.com")
            val app = getApplication<Application>()
            val res = app.resources
            val iconResId = R.mipmap.ic_launcher
            val iconUri = try {
                val type = res.getResourceTypeName(iconResId)
                val name = res.getResourceEntryName(iconResId)
                // /<type>/<name>
                Uri.parse("/$type/$name")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to build android.resource icon URI, falling back to resource ID format", e)
                // Fallback: use resource ID format which is also relative
                Uri.parse("/${iconResId}")
            }
            Log.d(TAG, "Built icon URI: $iconUri (scheme: ${iconUri.scheme}, isAbsolute: ${iconUri.isAbsolute})")

            val identityName = "Beezle - Solana Dueling Game"
            walletAdapter = MobileWalletAdapter(
                connectionIdentity = ConnectionIdentity(
                    identityUri = solanaUri,
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
                        val publicKey = authResult.accounts.firstOrNull()?.publicKey
                        val publicKeyString = publicKey?.let {
                            try { it.joinToString("") { b -> "%02x".format(b) } } catch (e: Exception) { Log.e(TAG, "Error encoding public key", e); "Invalid Key" }
                        }
                        val walletName = authResult.walletUriBase?.host ?: "Phantom"
                        val authToken = authResult.authToken
                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = publicKeyString,
                            authToken = authToken,
                            walletName = walletName,
                            isLoading = false,
                            error = null
                        )
                        persistWithDataStore(true, publicKeyString, walletName, authToken)
                        fetchBalance()
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
                        "solana.com",
                        "Sign in to Beezle - Your Solana Dueling Game"
                    )
                )

                when (result) {
                    is TransactionResult.Success -> {
                        val authResult = result.authResult
                        val publicKey = authResult.accounts.firstOrNull()?.publicKey
                        val publicKeyString = publicKey?.let {
                            try { it.joinToString("") { b -> "%02x".format(b) } } catch (e: Exception) { Log.e(TAG, "Error encoding public key", e); "Invalid Key" }
                        }
                        val walletName = authResult.walletUriBase?.host ?: "Phantom"
                        val authToken = authResult.authToken
                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = publicKeyString,
                            authToken = authToken,
                            walletName = walletName,
                            isLoading = false,
                            error = null
                        )
                        persistWithDataStore(true, publicKeyString, walletName, authToken)
                        fetchBalance()
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

    private fun fetchBalance() {
        viewModelScope.launch {
            try {
                // Placeholder balance
                _walletState.value = _walletState.value.copy(balance = 1.25)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching balance", e)
            }
        }
    }

    fun clearError() {
        _walletState.value = _walletState.value.copy(error = null)
    }
}
