package com.example.beezle.wallet

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solana.mobilewalletadapter.clientlib.*
import com.solana.mobilewalletadapter.common.signin.SignInWithSolana
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletState(
    val isConnected: Boolean = false,
    val publicKey: String? = null,
    val authToken: String? = null,
    val walletName: String? = null,
    val balance: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SolanaWalletManager : ViewModel() {

    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private lateinit var walletAdapter: MobileWalletAdapter

    companion object {
        private const val TAG = "SolanaWalletManager"
    }

    init {
        initializeWalletAdapter()
    }

    // SolanaWalletManager.kt

    private fun initializeWalletAdapter() {
        try {
            // Define dApp's identity metadata for Android app
            val solanaUri = Uri.parse("https://solana.com")
            val iconUri = Uri.parse("/ic_launcher.png") // Changed to relative URI
            val identityName = "Beezle - Solana Dueling Game"

            // Construct the MWA client
            walletAdapter = MobileWalletAdapter(
                // CORRECTED: Use named parameters to ensure the URI is passed correctly
                connectionIdentity = ConnectionIdentity(
                    identityUri = solanaUri,
                    iconUri = iconUri, // Explicitly use the 'iconUri' parameter for absolute URIs
                    identityName = identityName
                )
            )
            Log.d(TAG, "Wallet adapter initialized successfully")
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
                Log.d(TAG, "Created , calling connect...")

                val result = walletAdapter.connect(sender)
                Log.d(TAG, "Connect result received: ${result::class.simpleName}")

                when (result) {
                    is TransactionResult.Success -> {
                        Log.d(TAG, "Connection successful!")
                        val authResult = result.authResult
                        val publicKey = authResult.accounts.firstOrNull()?.publicKey

                        // Convert byte array to hex string for display
                        val publicKeyString = publicKey?.let {
                            try {
                                it.joinToString("") { byte -> "%02x".format(byte) }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error encoding public key", e)
                                "Invalid Key"
                            }
                        }

                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = publicKeyString,
                            authToken = authResult.authToken,
                            walletName = authResult.walletUriBase?.host ?: "Phantom",
                            isLoading = false,
                            error = null
                        )

                        Log.d(TAG, "Wallet connected: ${_walletState.value.walletName}")
                        Log.d(TAG, "Public key: ${publicKeyString?.take(16)}...")

                        // Fetch balance after successful connection
                        fetchBalance()
                    }
                    is TransactionResult.NoWalletFound -> {
                        Log.w(TAG, "No wallet found")
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "No MWA wallet found! Please install Phantom wallet from Google Play Store and try again."
                        )
                    }
                    is TransactionResult.Failure -> {
                        Log.e(TAG, "Connection failed: ${result.e.message}", result.e)
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
                        Log.d(TAG, "Sign in successful!")
                        val authResult = result.authResult
                        val publicKey = authResult.accounts.firstOrNull()?.publicKey

                        val publicKeyString = publicKey?.let {
                            try {
                                it.joinToString("") { byte -> "%02x".format(byte) }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error encoding public key", e)
                                "Invalid Key"
                            }
                        }

                        _walletState.value = _walletState.value.copy(
                            isConnected = true,
                            publicKey = publicKeyString,
                            authToken = authResult.authToken,
                            walletName = authResult.walletUriBase?.host ?: "Phantom",
                            isLoading = false,
                            error = null
                        )

                        fetchBalance()
                    }
                    is TransactionResult.NoWalletFound -> {
                        Log.w(TAG, "No wallet found for sign in")
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "No MWA wallet found! Please install Phantom wallet and try again."
                        )
                    }
                    is TransactionResult.Failure -> {
                        Log.e(TAG, "Sign in failed: ${result.e.message}", result.e)
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
                Log.d(TAG, "Disconnecting wallet...")
                walletAdapter.disconnect(sender)

                _walletState.value = WalletState() // Reset to initial state
                Log.d(TAG, "Wallet disconnected successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Disconnect error", e)
                _walletState.value = _walletState.value.copy(
                    error = "Disconnect error: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    fun signMessage(sender: ActivityResultSender, message: String) {
        if (!_walletState.value.isConnected) {
            _walletState.value = _walletState.value.copy(
                error = "Please connect your wallet first"
            )
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "Starting message signing...")
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
                        Log.d(TAG, "Message signed successfully!")
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "Message signed successfully!"
                        )
                    }
                    is TransactionResult.Failure -> {
                        Log.e(TAG, "Message signing failed: ${result.e.message}", result.e)
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "Failed to sign message: ${result.e.localizedMessage ?: result.e.message}"
                        )
                    }
                    else -> {
                        Log.w(TAG, "Unexpected result during message signing")
                        _walletState.value = _walletState.value.copy(
                            isLoading = false,
                            error = "Unexpected error during message signing"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Message signing error", e)
                _walletState.value = _walletState.value.copy(
                    isLoading = false,
                    error = "Message signing error: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    private fun fetchBalance() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching wallet balance...")
                // For now, set a demo balance - in production you'd call Solana RPC
                _walletState.value = _walletState.value.copy(balance = 1.25) // Demo balance
                Log.d(TAG, "Balance updated to demo value")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching balance", e)
            }
        }
    }

    fun clearError() {
        _walletState.value = _walletState.value.copy(error = null)
        Log.d(TAG, "Error cleared")
    }
}
