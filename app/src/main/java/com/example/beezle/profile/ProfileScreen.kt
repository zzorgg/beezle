package com.example.beezle.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.beezle.ui.theme.*
import com.example.beezle.wallet.SolanaWalletManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val walletManager: SolanaWalletManager = androidx.lifecycle.viewmodel.compose.viewModel()
    val walletState by walletManager.walletState.collectAsState()

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val authState by authViewModel.authState.collectAsState()

    // When auth or wallet changes, refresh profile
    LaunchedEffect(authState, walletState.publicKey) {
        val walletKey = walletState.publicKey
        if (authState is AuthUiState.SignedIn) {
            profileViewModel.refresh(walletKey)
        }
    }

    var editing by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authViewModel.handleSignInResult(result.data) {
            profileViewModel.refresh(walletState.publicKey)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Profile",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                when (authState) {
                    is AuthUiState.SignedIn -> {
                        TextButton(onClick = { authViewModel.signOut() }) { Text("Sign out", color = PrimaryBlue) }
                    }
                    else -> {}
                }
            }
            Spacer(Modifier.height(16.dp))

            when (authState) {
                is AuthUiState.SignedOut -> {
                    AuthPrompt(signingIn = authState is AuthUiState.Loading, onSignIn = {
                        val intent = authViewModel.buildSignInIntent()
                        signInLauncher.launch(intent)
                    })
                }
                is AuthUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is AuthUiState.Error -> {
                    val msg = (authState as AuthUiState.Error).message
                    Text(msg, color = Color.Red)
                    Spacer(Modifier.height(12.dp))
                    AuthPrompt(signingIn = false, onSignIn = {
                        val intent = authViewModel.buildSignInIntent()
                        signInLauncher.launch(intent)
                    })
                }
                is AuthUiState.SignedIn -> {
                    // Show profile states
                    when (uiState) {
                        ProfileUiState.Idle, ProfileUiState.Loading -> {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryBlue)
                            }
                        }
                        is ProfileUiState.Error -> {
                            val msg = (uiState as ProfileUiState.Error).message
                            Text(msg, color = Color.Red)
                        }
                        ProfileUiState.SignedOut -> {
                            // Should not happen because auth is signed in
                            Text("Signed out", color = TextSecondary)
                        }
                        is ProfileUiState.Loaded -> {
                            val profile = (uiState as ProfileUiState.Loaded).profile
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Firebase UID", color = TextSecondary, fontSize = 12.sp)
                                    Text(profile.uid.take(8) + "...", color = TextPrimary, fontSize = 14.sp)
                                    Spacer(Modifier.height(12.dp))

                                    // Wallet linking section
                                    Text("Wallet", color = TextSecondary, fontSize = 12.sp)
                                    if (profile.walletPublicKey != null) {
                                        Text(profile.walletPublicKey.take(8) + "..." + profile.walletPublicKey.takeLast(8), color = TextPrimary, fontSize = 14.sp)
                                    } else {
                                        if (walletState.isConnected && !walletState.publicKey.isNullOrBlank()) {
                                            Button(onClick = { profileViewModel.linkWallet(walletState.publicKey!!) }) {
                                                Text("Link Connected Wallet")
                                            }
                                        } else {
                                            Text("No wallet linked", color = TextSecondary, fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text("Username", color = TextSecondary, fontSize = 12.sp)
                                            if (editing) {
                                                OutlinedTextField(
                                                    value = usernameInput,
                                                    onValueChange = { usernameInput = it },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                Text(profile.username ?: "Not set", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue)
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
            Text("Coming soon: on-chain duel history & escrow settlement.", color = TextTertiary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AuthPrompt(signingIn: Boolean, onSignIn: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Sign in to create your profile", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSignIn, enabled = !signingIn) {
            if (signingIn) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (signingIn) "Signing in..." else "Sign in with Google")
        }
    }
}

@Composable
private fun LevelBadge(text: String) {
    Box(
        modifier = Modifier
            .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
