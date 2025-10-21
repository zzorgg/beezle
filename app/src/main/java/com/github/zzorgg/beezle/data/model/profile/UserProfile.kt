package com.github.zzorgg.beezle.data.model.profile

import java.util.Date

data class DuelStats(
    val wins: Int = 0,
    val losses: Int = 0,
) {
    val total get() = wins + losses
    val winRate get() = if (total == 0) 0.0 else wins.toDouble() / total
}

data class UserProfile(
    val uid: String = "",
    val username: String? = null,
    val walletPublicKey: String? = null,
    val avatarUrl: String = "",
    val createdAt: Long = Date().time,
    val mathLevel: Int = 1,
    val csLevel: Int = 1,
    val duelStats: DuelStats = DuelStats(),
)
