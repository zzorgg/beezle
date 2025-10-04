package com.github.zzorgg.beezle.data.repository

import com.github.zzorgg.beezle.data.model.profile.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
) {
    private val collection get() = firestore.collection("users")

    suspend fun getCurrentUid(): String? = authRepository.currentUser()?.uid

    suspend fun getProfile(uid: String?): UserProfile? {
        if(uid == null) return null
        return try {
            val snap = collection.document(uid).get().await()
            if (snap.exists()) snap.toObject(UserProfile::class.java) else null
        } catch (e: Exception) { null }
    }

    suspend fun createProfile(uid: String, suggestedUsername: String?): UserProfile {
        val profile = UserProfile(
            uid = uid,
            username = suggestedUsername,
            createdAt = System.currentTimeMillis(),
        )
        collection.document(uid).set(profile).await()
        return profile
    }

    suspend fun upsert(profile: UserProfile) {
        // Merge to avoid overwriting fields unexpectedly
        collection.document(profile.uid).set(profile).await()
    }

    suspend fun updateUsername(uid: String, username: String) {
        collection.document(uid).update("username", username).await()
    }

    suspend fun linkWallet(uid: String, walletPublicKey: String) {
        collection.document(uid).update("walletPublicKey", walletPublicKey).await()
    }

    suspend fun incrementWin(uid: String, won: Boolean) {
        val updates = if (won) mapOf("duelStats.wins" to FieldValue.increment(1)) else mapOf("duelStats.losses" to FieldValue.increment(1))
        collection.document(uid).update(updates).await()
    }
}