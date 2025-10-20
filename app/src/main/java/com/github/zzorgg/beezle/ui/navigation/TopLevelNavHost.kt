package com.github.zzorgg.beezle.ui.navigation

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.zzorgg.beezle.ui.screens.duel.components.Category
import com.github.zzorgg.beezle.ui.screens.duel.components.DuelsPracticeScreenRoot
import com.github.zzorgg.beezle.ui.screens.leaderboards.LeaderboardsScreen
import com.github.zzorgg.beezle.ui.screens.main.MainAppScreenRoot
import com.github.zzorgg.beezle.ui.screens.profile.ProfileScreenRoot
import com.github.zzorgg.beezle.ui.screens.wallet.WalletScreenRoot
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TopLevelNavHost(
    sender: ActivityResultSender,
    navigateToRootCallback: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    val topLevelNavController = rememberNavController()
    val navigateBackCallback: () -> Unit = topLevelNavController::popBackStack

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val navBackStackEntry = topLevelNavController.currentBackStackEntryAsState().value
            val currentDestination = navBackStackEntry?.destination
            val view = LocalView.current
            val chipBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.05f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    tonalElevation = 3.dp
                ) {
                    NAVBAR_ROUTES.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.first::class)
                        } == true
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                if (isSelected) return@NavigationBarItem
                                topLevelNavController.navigate(item.first) {
                                    popUpTo(topLevelNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.second, item.third) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = chipBg,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) {
        NavHost(
            navController = topLevelNavController,
            startDestination = Route.Home,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioLowBouncy,
                    )
                ) { it / 3 }
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween()) { -it }
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioLowBouncy,
                    )
                ) { -it / 3 }
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween()) { it }
            }
        ) {
            composable<Route.Home> {
                MainAppScreenRoot(
                    navigateToRootCallback = navigateToRootCallback,
                    navigateToTopLevelCallback = { topLevelNavController.navigate(it) }
                )
            }
            composable<Route.Profile> {
                ProfileScreenRoot(
                    sender = sender,
                    navigateBackCallback = navigateBackCallback,
                )
            }
            composable<Route.Practice> {
                DuelsPracticeScreenRoot(
                    navigateBackCallback = navigateBackCallback,
                    initialCategory = Category.MATH
                )
            }
            composable<Route.Leaderboard> {
                LeaderboardsScreen()
            }
            composable<Route.Wallet> {
                WalletScreenRoot(
                    sender = sender,
                    navigateBackCallback = navigateBackCallback,
                )
            }
        }
    }
}