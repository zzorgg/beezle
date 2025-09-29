package com.github.zzorgg.beezle.ui.screens.profile

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.zzorgg.beezle.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

sealed interface AuthUiState {
    object SignedOut : AuthUiState
    object Loading : AuthUiState
    data class SignedIn(val uid: String, val email: String?, val displayName: String?) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.SignedOut)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Instantiate a Google sign-in request

//    init {
//        auth.currentUser?.let { user ->
//            _authState.value = AuthUiState.SignedIn(user.uid, user.email, user.displayName)
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun handleSignIn(
        firebaseAuth: FirebaseAuth,
        credentialManager: CredentialManager,
        context: Context
    ) {
        try {
            // Instantiate a Google sign-in request
            val googleIdOption = GetGoogleIdOption.Builder()
                // Your server's client ID, not your Android client ID.
                .setServerClientId(context.getString(R.string.default_web_client_id))
                // Only show accounts previously used to sign in.
                .setFilterByAuthorizedAccounts(false)
                .build()

            // Create the Credential Manager request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context,
                request
            )
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                // Create Google ID Token
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // Sign in to Firebase with using the token
                firebaseAuthWithGoogle(firebaseAuth, context, googleIdTokenCredential.idToken)
            } else {
                throw IllegalArgumentException("Credential is not of type Google ID!")
            }
        } catch (e: Exception) {
            Log.e(TAG, e.stackTrace.toString())
            _authState.update {
                AuthUiState.Error(e.localizedMessage ?: "Google sign-in failed")
            }
        }
    }

    suspend fun signOut(auth: FirebaseAuth, credentialManager: CredentialManager) {
        // Firebase sign out
        auth.signOut()

        // When a user signs out, clear the current user credential state from all credential providers.
        viewModelScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                _authState.update { AuthUiState.SignedOut }
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun firebaseAuthWithGoogle(auth: FirebaseAuth, context: Context, idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(context.mainExecutor) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    auth.currentUser?.let { user ->
                        _authState.update {
                            AuthUiState.SignedIn(
                                uid = user.uid,
                                email = user.email,
                                displayName = user.displayName
                            )
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    _authState.update { AuthUiState.SignedOut }
                }
            }
    }
}

