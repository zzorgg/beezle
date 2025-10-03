package com.github.zzorgg.beezle.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.github.zzorgg.beezle.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "FirebaseAuthRepository"

interface AuthRepository {
    fun currentUser(): FirebaseUser?

    suspend fun signin(activity: Activity): FirebaseUser?

    suspend fun signout()
}

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager,
    @field:ApplicationContext private val context: Context,
) : AuthRepository {
    override fun currentUser(): FirebaseUser? = auth.currentUser

    override suspend fun signin(activity: Activity): FirebaseUser? =
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Use the Activity context instead of Application context
            val credential = credentialManager.getCredential(activity, request).credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                // Create Google ID Token
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                // Sign in to Firebase with using the token
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } else {
                throw IllegalArgumentException("Credential is not of type Google ID!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            null
        }

    override suspend fun signout() {
        try {
            auth.signOut()
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String): FirebaseUser =
        suspendCancellableCoroutine { cont ->
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.user?.let { cont.resume(it) }
                            ?: cont.resumeWithException(IllegalStateException("User is null"))
                    } else {
                        cont.resumeWithException(task.exception ?: Exception("Sign-in failed"))
                    }
                }
        }
}