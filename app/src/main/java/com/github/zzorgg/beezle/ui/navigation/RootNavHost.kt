package com.github.zzorgg.beezle.ui.navigation

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.zzorgg.beezle.MainViewModel
import com.github.zzorgg.beezle.ui.screens.duel.DuelScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.OnboardingScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.SplashScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.WalletOnboardingScreenRoot
import com.github.zzorgg.beezle.ui.screens.onboarding.components.OnboardingAuthScreenRoot
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
                navController.navigate(Route.OnboardingAuth) {
                    popUpTo(Route.Onboarding) { inclusive = true }
                }
            })
        }
        composable<Route.OnboardingAuth> {
            OnboardingAuthScreenRoot(
                onSignedIn = {
                    // Mark onboarding as finished after successful sign-in
                    mainViewModel.finishOnBoarding()
                    navController.navigate(Route.OnboardingWallet) {
                        popUpTo(Route.OnboardingAuth) { inclusive = true }
                    }
                }
            )
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                DuelScreen(
                    onNavigateBack = { navController.popBackStack() },
                    initialMode = duelRoute.initialMode
                )
            } else {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Duels require Android 11 (API 30) or higher.",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go back")
                        }
                    }
                }
            }
        }
        composable<Route.TopLevelRoutes> {
            TopLevelNavHost(
                sender = sender,
                navigateToRootCallback = { navController.navigate(it) }
            )
        }
    }
}