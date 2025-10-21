package com.github.zzorgg.beezle.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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

    @Serializable
    object TopLevelRoutes : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object Leaderboard : Route

    @Serializable
    data object Practice : Route
}

val NAVBAR_ROUTES = listOf(
    Triple(Route.Home, Icons.Default.Home, "Home"),
    Triple(Route.Profile, Icons.Default.Person, "Profile"),
    Triple(Route.Leaderboard, Icons.Default.EmojiEvents, "Leaderboard"),
    Triple(Route.Practice, Icons.Default.School, "Practice"),
)