package com.example.beezle.profile

import java.util.Date

// Data model for a user profile
// mathLevel / csLevel: simple progression indicator for difficulty scaling
// duel stats will expand later with on-chain escrow references

data class DuelStats(
    val wins: Int = 0,
    val losses: Int = 0,
) {
    val total get() = wins + losses
    val winRate get() = if (total == 0) 0.0 else wins.toDouble() / total
}

data class UserProfile(
    val walletPublicKey: String,
    val username: String? = null,
    val createdAt: Long = Date().time,
    val mathLevel: Int = 1,
    val csLevel: Int = 1,
    val duelStats: DuelStats = DuelStats(),
)
