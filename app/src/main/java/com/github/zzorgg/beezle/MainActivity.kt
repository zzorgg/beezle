package com.github.zzorgg.beezle

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.zzorgg.beezle.ui.navigation.Route
import com.github.zzorgg.beezle.ui.screens.duel.DuelScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.Category
import com.github.zzorgg.beezle.ui.screens.duel.components.DuelsPracticeScreenRoot
import com.github.zzorgg.beezle.ui.screens.leaderboards.LeaderboardsScreen
import com.github.zzorgg.beezle.ui.screens.main.MainAppScreenRoot
import com.github.zzorgg.beezle.ui.screens.onboarding.OnboardingScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.SplashScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.WalletOnboardingScreenRoot
import com.github.zzorgg.beezle.ui.screens.profile.ProfileScreenRoot
import com.github.zzorgg.beezle.ui.screens.wallet.WalletScreenRoot
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var sender: ActivityResultSender

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sender = ActivityResultSender(this)
        enableEdgeToEdge()
        setContent {
            BeezleTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                val localData by mainViewModel.localData.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = Route.Splash,
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
                        Scaffold(

                        ) {
                            NavHost(
                                navController = rememberNavController(),
                                startDestination = Route.Home
                            ) {
                                composable<Route.Home> {
                                    MainAppScreenRoot(navController)
                                }
                                composable<Route.Profile> {
                                    ProfileScreenRoot(
                                        navController = navController,
                                        sender = sender
                                    )
                                }
                                composable<Route.Practice> {
                                    DuelsPracticeScreenRoot(
                                        navController = navController,
                                        initialCategory = Category.MATH
                                    )
                                }
                                composable<Route.Leaderboard> {
                                    LeaderboardsScreen(
                                        onNavigate = { route -> navController.navigate(route) }
                                    )
                                }
                                composable<Route.Wallet> {
                                    WalletScreenRoot(
                                        sender = sender,
                                        navController = navController,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
