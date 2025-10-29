package com.github.zzorgg.beezle.ui.screens.profile

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.github.zzorgg.beezle.data.model.profile.UserProfile
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.EphemeralGreenTick
import com.github.zzorgg.beezle.ui.components.ProfileStatsCard
import com.github.zzorgg.beezle.ui.screens.profile.components.AuthPrompt
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenRoot(
    sender: ActivityResultSender,
    navigateBackCallback: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val uiState by profileViewModel.profileViewState.collectAsStateWithLifecycle()
    val dataState by profileViewModel.profileDataState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    // Track firebase user for display (name, email, photo)
    val firebaseUserState = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    LaunchedEffect(uiState.firebaseAuthStatus) {
        if (uiState.firebaseAuthStatus is AuthStatus.Success) {
            firebaseUserState.value = FirebaseAuth.getInstance().currentUser
        } else if (uiState.firebaseAuthStatus is AuthStatus.Waiting) {
            firebaseUserState.value = null
        }
    }

    // When auth or wallet changes, refresh profile
    LaunchedEffect(walletState.publicKey, uiState.firebaseAuthStatus) {
        profileViewModel.refresh(walletState.publicKey)
    }

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
                firebaseUser = firebaseUserState.value,
                signInCallback = {
                    if (activity != null) {
                        // Mark that a sign-in attempt was initiated
                        signInInitiated.value = true
                        profileViewModel.signin(activity)
                    }
                },
                signOutCallback = {
                    profileViewModel.signout()
                    // Reset flags so next sign-in will show tick again
                    signInInitiated.value = false
                    hasShownSignInTick.value = false
                    showTick.value = false
                },
                walletState = walletState,
                connectWalletCallback = { walletManager.connectWallet(sender) },
                linkWalletCallback = {
                    if (walletState.publicKey != null) {
                        profileViewModel.linkWallet(walletState.publicKey!!)
                        // Proactively refresh to reflect link in UI (hide Link button, update status)
                        scope.launch { profileViewModel.refresh(walletState.publicKey) }
                    }
                },
                navigateBackCallback = navigateBackCallback,
            )
        }
        EphemeralGreenTick(
            visible = showTick.value,
            fullScreen = true,
            modifier = Modifier.fillMaxSize(),
            onFinished = { showTick.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileViewState,
    dataState: ProfileDataState,
    firebaseUser: FirebaseUser?,
    walletState: WalletState,
    signInCallback: () -> Unit,
    signOutCallback: () -> Unit,
    connectWalletCallback: () -> Unit,
    linkWalletCallback: () -> Unit,
    navigateBackCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBackCallback) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Logout moved to the top bar per request
                    IconButton(onClick = signOutCallback) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState.firebaseAuthStatus) {
                AuthStatus.Waiting -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AuthPrompt(signingIn = false, onSignIn = signInCallback)
                    }
                }

                AuthStatus.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                AuthStatus.NoGoogleAccount -> {
                    NoGoogleAccountScreen()
                }

                is AuthStatus.Error -> {
                    val msg = uiState.firebaseAuthStatus.message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(msg, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        AuthPrompt(signingIn = false, onSignIn = signInCallback)
                    }
                }

                AuthStatus.Success -> {
                    // Signed in UI
                    when (uiState.userProfileStatus) {
                        AuthStatus.Waiting, AuthStatus.Loading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        is AuthStatus.Error -> {
                            val msg = uiState.userProfileStatus.message
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(msg, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        AuthStatus.Success -> {
                            val profile = dataState.userProfile!!
                            // Updated layout: scrollable content; logout moved to top bar
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    HeroProfileSection(firebaseUser, profile)
                                    WalletCard(
                                        profile = profile,
                                        walletState = walletState,
                                        connectWalletCallback = connectWalletCallback,
                                        linkWalletCallback = linkWalletCallback
                                    )
                                    if (walletState.isConnected) {
                                        ProfileStatsCard(userProfile = profile)
                                    }
                                }
                            }
                        }

                        AuthStatus.NoGoogleAccount -> TODO()
                    }
                }
            }
        }
    }
}

// Hero Section displaying circular photo, name & email
@Composable
private fun HeroProfileSection(firebaseUser: FirebaseUser?, profile: UserProfile) {
    val displayName = firebaseUser?.displayName ?: profile.username ?: "Anonymous"
    val email = firebaseUser?.email ?: "" // email may be null for some providers

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = firebaseUser?.photoUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        CircleShape
                    ) // updated to PrimaryBlue border per requirement
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            )
            Spacer(Modifier.height(14.dp))
            Text(
                displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (email.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            // Removed Math/CS level badges per request
        }
    }
}

@Composable
private fun WalletCard(
    profile: UserProfile,
    walletState: WalletState,
    connectWalletCallback: () -> Unit,
    linkWalletCallback: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = remember(context) { context.getSystemService(ClipboardManager::class.java) }

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500); copied = false
        }
    }

    val linkedAddress = profile.walletPublicKey
    val isLinked = linkedAddress != null
    val isConnected = walletState.isConnected
    val connectedButUnlinkedAddress = if (isConnected && !isLinked) walletState.publicKey else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 190.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Precompute themed color outside Canvas draw scope
            val gridColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            // Subtle dots background
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = 28.dp.toPx()
                val radius = 2.dp.toPx()
                var y = radius
                while (y < size.height) {
                    var x = radius
                    while (x < size.width) {
                        drawCircle(gridColor, radius, androidx.compose.ui.geometry.Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }

            if (!isConnected) {
                // Disconnected: show left-side message + Connect button, and sleeping GIF on the right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Wallet not connected",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Connect your Phantom wallet to continue.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = connectWalletCallback,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Connect Wallet") }
                    }
                    Spacer(Modifier.width(16.dp))
                    AsyncImage(
                        model = "file:///android_asset/sleeping.gif",
                        contentDescription = "Wallet status animation",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    )
                }
            } else {
                // Connected: normal content, but remove any 'Linked' status label
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header without status text
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "WALLET",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // Main content row: Left info, Right GIF
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // LEFT COLUMN
                        Column(Modifier.weight(1f)) {
                            if (isLinked) {
                                // Balance
                                Text(
                                    "Balance",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                val bal = walletState.balance
                                if (bal != null) {
                                    Text(
                                        String.format(java.util.Locale.US, "%.4f SOL", bal),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        "— SOL",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                // Copy chip (enabled only when connected+linked)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30))
                                            .clickable {
                                                clipboard?.setPrimaryClip(
                                                    ClipData.newPlainText(
                                                        "Wallet address",
                                                        linkedAddress
                                                    )
                                                )
                                                copied = true
                                            }
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 18.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = if (copied) "Copied" else "Copy wallet address",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (copied) "Copied" else "Copy",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                // Connected but not linked: suggest linking; no copy available
                                Text(
                                    "Wallet connected",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Link your wallet to your Beezle profile.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                if (connectedButUnlinkedAddress != null) {
                                    Button(
                                        onClick = linkWalletCallback,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text("Link Wallet") }
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        // RIGHT GIF: phantom when linked, sleeping when not linked
                        AsyncImage(
                            model = if (isLinked) "file:///android_asset/phantom.gif" else "file:///android_asset/sleeping.gif",
                            contentDescription = "Wallet status animation",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview() {
    BeezleTheme {
        ProfileScreen(
            uiState = ProfileViewState(),
            dataState = ProfileDataState(),
            firebaseUser = null,
            walletState = WalletState(),
            signInCallback = {},
            signOutCallback = {},
            connectWalletCallback = {},
            linkWalletCallback = {},
            navigateBackCallback = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview_SignedIn_Wallet_Disconnected() {
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
            firebaseUser = null,
            walletState = WalletState(),
            signInCallback = { },
            signOutCallback = {},
            connectWalletCallback = {},
            linkWalletCallback = {},
            navigateBackCallback = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview_SignedIn_Wallet_Connected() {
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
            firebaseUser = null,
            walletState = WalletState(
                isConnected = true,
                walletName = "Test wallet",
                authToken = "Asdajkldsaj"
            ),
            signInCallback = { },
            signOutCallback = {},
            connectWalletCallback = {},
            linkWalletCallback = {},
            navigateBackCallback = {}
        )
    }
}
