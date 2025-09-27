package com.example.beezle.profile

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beezle.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object SignedOut : AuthUiState
    object Loading : AuthUiState
    data class SignedIn(val uid: String, val email: String?, val displayName: String?) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.SignedOut)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private var googleClient: GoogleSignInClient? = null

    init {
        auth.currentUser?.let { user ->
            _authState.value = AuthUiState.SignedIn(user.uid, user.email, user.displayName)
        }
    }

    private fun ensureClient(): GoogleSignInClient {
        val ctx = getApplication<Application>()
        if (googleClient == null) {
            val webClientId = ctx.getString(R.string.default_web_client_id)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build()
            googleClient = GoogleSignIn.getClient(ctx, gso)
        }
        return googleClient!!
    }

    fun buildSignInIntent(): Intent {
        _authState.value = AuthUiState.Loading
        return ensureClient().signInIntent
    }

    fun handleSignInResult(data: Intent?, onCredentialFinished: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account, onCredentialFinished)
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.localizedMessage ?: "Google sign-in failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, onCredentialFinished: (() -> Unit)?) {
        viewModelScope.launch {
            try {
                _authState.value = AuthUiState.Loading
                val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            _authState.value = AuthUiState.SignedIn(user.uid, user.email, user.displayName)
                        } else {
                            _authState.value = AuthUiState.Error("User is null after sign-in")
                        }
                        onCredentialFinished?.invoke()
                    } else {
                        _authState.value = AuthUiState.Error(task.exception?.localizedMessage ?: "Auth failed")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.localizedMessage ?: "Auth exception")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                ensureClient().signOut()
                auth.signOut()
            } catch (_: Exception) { }
            _authState.value = AuthUiState.SignedOut
        }
    }
}

