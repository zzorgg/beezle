package com.github.zzorgg.beezle.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zzorgg.beezle.ui.theme.BackgroundDark
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import com.github.zzorgg.beezle.ui.theme.SurfaceDark
import com.github.zzorgg.beezle.ui.theme.TextPrimary
import com.github.zzorgg.beezle.ui.theme.TextSecondary
import com.github.zzorgg.beezle.ui.theme.TextTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val uiState by profileViewModel.profileViewState.collectAsStateWithLifecycle()
    val dataState by profileViewModel.profileDataState.collectAsStateWithLifecycle()

    // When auth or wallet changes, refresh profile
    LaunchedEffect(walletState.publicKey, uiState.firebaseAuthStatus) {
        profileViewModel.refresh(walletState.publicKey)
    }

    var editing by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

//    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundDark) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.firebaseAuthStatus is AuthStatus.Success) {
                        TextButton(onClick = {
                            scope.launch {
                                profileViewModel.signout()
                            }
                        }) {
                            Text(
                                "Sign out",
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(4.dp, 8.dp)
        ) {
            when (uiState.firebaseAuthStatus) {
                AuthStatus.Waiting -> {
                    AuthPrompt(signingIn = false, onSignIn = {
                        profileViewModel.signin()
                    })
                }

                AuthStatus.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                is AuthStatus.Error -> {
                    val msg = (uiState.firebaseAuthStatus as AuthStatus.Error).message
                    Text(msg, color = Color.Red)
                    Spacer(Modifier.height(12.dp))
                    AuthPrompt(signingIn = false, onSignIn = {
                        profileViewModel.signin()
                    })
                }

                AuthStatus.Success -> {
                    // Show profile states
                    when (uiState.userProfileStatus) {
                        AuthStatus.Waiting, AuthStatus.Loading -> {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryBlue)
                            }
                        }

                        is AuthStatus.Error -> {
                            val msg = (uiState.userProfileStatus as AuthStatus.Error).message
                            Text(msg, color = Color.Red)
                        }

                        AuthStatus.Success -> {
                            val profile = dataState.userProfile!!
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Firebase UID", color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        profile.uid.take(8) + "...",
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    // Wallet linking section
                                    Text("Wallet", color = TextSecondary, fontSize = 12.sp)
                                    if (profile.walletPublicKey != null) {
                                        Text(
                                            profile.walletPublicKey.take(8) + "..." + profile.walletPublicKey.takeLast(
                                                8
                                            ), color = TextPrimary, fontSize = 14.sp
                                        )
                                    } else {
                                        if (walletState.isConnected && !walletState.publicKey.isNullOrBlank()) {
                                            Button(onClick = {
                                                profileViewModel.linkWallet(
                                                    walletState.publicKey!!
                                                )
                                            }) {
                                                Text("Link Connected Wallet")
                                            }
                                        } else {
                                            Text(
                                                "No wallet linked",
                                                color = TextSecondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "Username",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                            if (editing) {
                                                OutlinedTextField(
                                                    value = usernameInput,
                                                    onValueChange = { usernameInput = it },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                Text(
                                                    profile.username ?: "Not set",
                                                    color = TextPrimary,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        IconButton(onClick = {
                                            if (editing) {
                                                if (usernameInput.isNotBlank()) {
                                                    profileViewModel.setUsername(usernameInput.trim())
                                                }
                                            } else {
                                                usernameInput = profile.username ?: ""
                                            }
                                            editing = !editing
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = PrimaryBlue
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        LevelBadge("Math Lv ${profile.mathLevel}")
                                        LevelBadge("CS Lv ${profile.csLevel}")
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Duels: ${profile.duelStats.wins}W / ${profile.duelStats.losses}L  (Win ${(profile.duelStats.winRate * 100).toInt()}%)",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Coming soon: on-chain duel history & escrow settlement.",
                color = TextTertiary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AuthPrompt(signingIn: Boolean, onSignIn: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Sign in to create your profile",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSignIn, enabled = !signingIn) {
            if (signingIn) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(if (signingIn) "Signing in..." else "Sign in with Google")
        }
    }
}

@Composable
private fun LevelBadge(text: String) {
    Box(
        modifier = Modifier.Companion
            .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
