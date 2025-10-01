package com.github.zzorgg.beezle.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.zzorgg.beezle.data.model.profile.UserProfile
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.EphemeralGreenTick
import com.github.zzorgg.beezle.ui.screens.profile.components.AuthPrompt
import com.github.zzorgg.beezle.ui.screens.profile.components.LevelBadge
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import com.github.zzorgg.beezle.ui.theme.SurfaceDark
import com.github.zzorgg.beezle.ui.theme.TextPrimary
import com.github.zzorgg.beezle.ui.theme.TextSecondary
import com.github.zzorgg.beezle.ui.theme.TextTertiary
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenRoot(navController: NavController) {
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

    // Flags to control when to show success tick
    val signInInitiated = rememberSaveable { mutableStateOf(false) }
    val hasShownSignInTick = rememberSaveable { mutableStateOf(false) }
    val showTick = remember { mutableStateOf(false) }

    // Trigger tick only after explicit sign-in action and first Success
    LaunchedEffect(uiState.firebaseAuthStatus, signInInitiated.value) {
        if (signInInitiated.value && !hasShownSignInTick.value && uiState.firebaseAuthStatus is AuthStatus.Success) {
            showTick.value = true
            hasShownSignInTick.value = true
        }
    }

    Box { // wrap original ProfileScreen
        val blurModifier = if (showTick.value) Modifier.blur(22.dp) else Modifier
        Box(modifier = blurModifier) {
            ProfileScreen(
                uiState = uiState,
                dataState = dataState,
                signInCallback = {
                    // Mark that a sign-in attempt was initiated
                    signInInitiated.value = true
                    profileViewModel.signin()
                },
                signOutCallback = {
                    profileViewModel.signout()
                    // Reset flags so next sign-in will show tick again
                    signInInitiated.value = false
                    hasShownSignInTick.value = false
                    showTick.value = false
                },
                walletState = walletState,
                linkWalletCallback = {
                    if (walletState.publicKey != null) {
                        profileViewModel.linkWallet(walletState.publicKey!!)
                    }
                },
                usernameInput = usernameInput,
                isEditingUsername = editing,
                editUsernameCallback = {
                    usernameInput = it
                },
                editUsernameButtonCallback = {
                    if (editing) {
                        if (usernameInput.isNotBlank()) {
                            profileViewModel.setUsername(usernameInput.trim())
                        }
                    } else {
                        usernameInput = dataState.userProfile!!.username ?: ""
                    }
                    editing = !editing
                },
                navigateBackCallback = { navController.popBackStack() },
            )
        }
        EphemeralGreenTick(
            visible = showTick.value,
            fullScreen = true,
            modifier = Modifier
                .fillMaxSize(),
            onFinished = { showTick.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileViewState,
    dataState: ProfileDataState,
    walletState: WalletState,
    signInCallback: () -> Unit,
    signOutCallback: () -> Unit,
    linkWalletCallback: () -> Unit,
    isEditingUsername: Boolean,
    usernameInput: String,
    editUsernameCallback: (String) -> Unit,
    editUsernameButtonCallback: () -> Unit,
    navigateBackCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = navigateBackCallback) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.firebaseAuthStatus is AuthStatus.Success) {
                        TextButton(onClick = signOutCallback) {
                            Text(
                                "Sign out",
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(4.dp, 8.dp)
        ) {
            when (uiState.firebaseAuthStatus) {
                AuthStatus.Waiting -> {
                    AuthPrompt(signingIn = false, onSignIn = signInCallback)
                }

                AuthStatus.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                is AuthStatus.Error -> {
                    val msg = uiState.firebaseAuthStatus.message
                    Text(msg, color = Color.Red)
                    Spacer(Modifier.height(12.dp))
                    AuthPrompt(signingIn = false, onSignIn = signInCallback)
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
                            val msg = uiState.userProfileStatus.message
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
                                            Button(onClick = linkWalletCallback) {
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
                                            if (isEditingUsername) {
                                                OutlinedTextField(
                                                    value = usernameInput,
                                                    onValueChange = editUsernameCallback,
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
                                        IconButton(onClick = editUsernameButtonCallback) {
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

@Preview
@Composable
private fun ProfileScreenPreview() {
    BeezleTheme {
        ProfileScreen(
            uiState = ProfileViewState(),
            dataState = ProfileDataState(),
            walletState = WalletState(),
            signInCallback = { },
            signOutCallback = {},
            linkWalletCallback = {},
            isEditingUsername = false,
            usernameInput = "",
            editUsernameCallback = {},
            editUsernameButtonCallback = {},
            navigateBackCallback = {}
        )
    }
}

@Preview
@Composable
private fun ProfileScreenPreview_SignedIn() {
    BeezleTheme {
        ProfileScreen(
            uiState = ProfileViewState(
                firebaseAuthStatus = AuthStatus.Success,
                userProfileStatus = AuthStatus.Success
            ),
            dataState = ProfileDataState(
                userProfile = UserProfile(
                    uid = "23412e",
                    walletPublicKey = "7asdababshdjkabhdjkag778",
                    username = "Test User"
                )
            ),
            walletState = WalletState(),
            signInCallback = { },
            signOutCallback = {},
            linkWalletCallback = {},
            isEditingUsername = false,
            usernameInput = "",
            editUsernameCallback = {},
            editUsernameButtonCallback = {},
            navigateBackCallback = {}
        )
    }
}
