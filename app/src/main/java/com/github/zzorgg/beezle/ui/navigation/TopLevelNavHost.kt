package com.github.zzorgg.beezle.ui.navigation

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
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
            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .offset(y = 2.dp)
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
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(topLevelNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.second, item.third) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy( alpha = 0.58f )
                        )
                    )
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