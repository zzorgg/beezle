package com.github.zzorgg.beezle.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.zzorgg.beezle.MainViewModel
import com.github.zzorgg.beezle.ui.screens.duel.DuelScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.OnboardingScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.SplashScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.WalletOnboardingScreenRoot
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@Composable
fun RootNavHost(
    mainViewModel: MainViewModel,
    sender: ActivityResultSender,
    modifier: Modifier = Modifier
) {
    val localData by mainViewModel.localData.collectAsState()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        modifier = modifier,
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
        composable<Route.Splash> {
            SplashScreen(onFinished = {
                val destination =
                    if (localData.hasOnboarded) Route.TopLevelRoutes else Route.Onboarding
                mainViewModel.resetWelcomeGifStatus()
                navController.navigate(destination) {
                    popUpTo(Route.Splash) { inclusive = true }
                }
            })
        }
        composable<Route.Onboarding> {
            OnboardingScreen(onGetStarted = {
                navController.navigate(Route.OnboardingWallet) {
                    popUpTo(Route.Onboarding) { inclusive = true }
                }
                mainViewModel.finishOnBoarding()
            })
        }
        composable<Route.OnboardingWallet> {
            WalletOnboardingScreenRoot(
                onWalletConnected = {
                    navController.navigate(Route.TopLevelRoutes) {
                        popUpTo(Route.OnboardingWallet) { inclusive = true }
                    }
                    mainViewModel.connectedWallet()
                },
                sender = sender,
            )
        }
        composable<Route.Duels> { backStackEntry ->
            val duelRoute = backStackEntry.toRoute<Route.Duels>()
            DuelScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                initialMode = duelRoute.initialMode
            )
        }
        composable<Route.TopLevelRoutes> {
            TopLevelNavHost(
                sender = sender,
                navigateToRootCallback = { navController.navigate(it) }
            )
        }
    }
}