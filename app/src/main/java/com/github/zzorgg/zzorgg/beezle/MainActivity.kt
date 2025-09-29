package com.github.zzorgg.beezle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.zzorgg.beezle.ui.screens.main.MainAppScreen
import com.github.zzorgg.beezle.ui.screens.duel.DuelScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.OnboardingScreen
import com.github.zzorgg.beezle.ui.screens.onboarding.components.SplashScreen
import com.github.zzorgg.beezle.ui.screens.wallet.WalletScreen
import com.github.zzorgg.beezle.ui.screens.profile.ProfileScreen
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var sender: ActivityResultSender

    private lateinit var auth: FirebaseAuth

    private lateinit var credentialManager: CredentialManager

    // Create the Credential Manager request
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sender = ActivityResultSender(this)
        auth = Firebase.auth
        credentialManager = CredentialManager.create(baseContext)
        enableEdgeToEdge()
        setContent {
            BeezleTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                val isSplashFinished by mainViewModel.isSplashFinished.collectAsState()
                val localData by mainViewModel.localData.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination =
                        if (isSplashFinished)
                            if (localData.hasOnboarded)
                                if (localData.hasConnectedWallet) "main" else "wallet"
                            else "onboarding"
                        else "splash"
                ) {
                    composable("splash") {
                        SplashScreen(onFinished = {
                            navController.navigate("onboarding") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    composable("onboarding") {
                        OnboardingScreen(onGetStarted = {
                            navController.navigate("wallet") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                            mainViewModel.finishOnBoarding()
                        })
                    }
                    composable("wallet") {
                        WalletScreen(
                            onWalletConnected = {
                                navController.navigate("main") {
                                    popUpTo("wallet") { inclusive = true }
                                }
                                mainViewModel.connectedWallet()
                            },
                            sender = sender,
                        )
                    }
                    composable("main") {
                        MainAppScreen(sender, navController)
                    }
                    composable("profile") {
                        ProfileScreen(
                            navController = navController,
                            firebaseAuth = auth,
                            credentialManager = credentialManager,
                            baseContext = baseContext,
                        )
                    }
                    composable("duels") {
                        DuelScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
