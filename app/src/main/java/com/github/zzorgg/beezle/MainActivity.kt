package com.github.zzorgg.beezle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.zzorgg.beezle.ui.screens.duel.DuelScreen
import com.github.zzorgg.beezle.ui.screens.duel.components.DuelsPracticeScreenRoot
import com.github.zzorgg.beezle.ui.screens.duel.components.Category
import com.github.zzorgg.beezle.ui.screens.main.MainAppScreenRoot
import com.github.zzorgg.beezle.ui.screens.onboarding.OnboardingScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.SplashScreen
import com.github.zzorgg.beezle.ui.screens.profile.ProfileScreenRoot
import com.github.zzorgg.beezle.ui.screens.onboarding.components.WalletOnboardingScreen
import com.github.zzorgg.beezle.ui.screens.wallet.WalletScreenRoot
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var sender: ActivityResultSender

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
                    startDestination = "splash",
                ) {
                    composable("splash") {
                        SplashScreen(onFinished = {
                            val destination =
                                if (localData.hasOnboarded) "main" else "onboarding"
                            navController.navigate(destination) {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    composable("onboarding") {
                        OnboardingScreen(onGetStarted = {
                            navController.navigate("wallet-onboarding") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                            mainViewModel.finishOnBoarding()
                        })
                    }
                    composable("wallet-onboarding") {
                        WalletOnboardingScreen(
                            onWalletConnected = {
                                navController.navigate("main") {
                                    popUpTo("wallet-onboarding") { inclusive = true }
                                }
                                mainViewModel.connectedWallet()
                            },
                            sender = sender,
                        )
                    }
                    composable("main") {
                        MainAppScreenRoot(navController)
                    }
                    composable("profile") {
                        ProfileScreenRoot(navController = navController)
                    }
                    composable("duels") {
                        DuelScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("practice/{subject}") { backStackEntry ->
                        val subject = backStackEntry.arguments?.getString("subject")?.uppercase() ?: "MATH"
                        val cat = if (subject == "CS") Category.CS else Category.MATH
                        DuelsPracticeScreenRoot(navController = navController, initialCategory = cat)
                    }
                    composable("wallet") {
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
