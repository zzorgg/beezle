package com.github.zzorgg.beezle.data.model.profile

import java.util.Date

// Data model for a user profile stored in Firestore.
// Document ID will be the Firebase Auth UID. "walletPublicKey" remains optional if user hasn't linked wallet yet.
// mathLevel / csLevel: simple progression indicator for difficulty scaling.
// duel stats will expand later with on-chain escrow references.

data class DuelStats(
    val wins: Int = 0,
    val losses: Int = 0,
) {
    val total get() = wins + losses
    val winRate get() = if (total == 0) 0.0 else wins.toDouble() / total
}

data class UserProfile(
    val uid: String = "", // Firebase Auth UID
    val walletPublicKey: String? = null,
    val username: String? = null,
    val createdAt: Long = Date().time,
    val mathLevel: Int = 1,
    val csLevel: Int = 1,
    val duelStats: DuelStats = DuelStats(),
)
