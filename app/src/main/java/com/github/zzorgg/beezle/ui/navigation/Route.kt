package com.github.zzorgg.beezle.ui.navigation

import com.github.zzorgg.beezle.data.model.duel.DuelMode
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Splash : Route
    @Serializable
    data object Onboarding : Route
    @Serializable
    data object OnboardingWallet : Route

    @Serializable
    data class Duels(val initialMode: DuelMode) : Route

    @Serializable object TopLevelRoutes : Route

    @Serializable
    data object Home : Route
    @Serializable
    data object Profile : Route
    @Serializable
    data object Wallet : Route
    @Serializable
    data object Leaderboard : Route
    @Serializable
    data object Practice : Route
}
