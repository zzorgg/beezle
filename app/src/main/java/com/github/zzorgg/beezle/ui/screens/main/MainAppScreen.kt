package com.github.zzorgg.beezle.ui.screens.main

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // Added for floating bottom bar container
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.MonochromeAsyncImage
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
import com.github.zzorgg.beezle.ui.screens.profile.components.LevelBadge
import com.github.zzorgg.beezle.ui.theme.AccentGreen
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import com.github.zzorgg.beezle.ui.theme.SurfaceDark
import com.github.zzorgg.beezle.ui.theme.TextPrimary
import com.github.zzorgg.beezle.ui.theme.TextSecondary
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.airbnb.lottie.compose.* // Lottie compose imports remain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreenRoot(
    navController: NavController,
) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileViewState by profileViewModel.profileViewState.collectAsStateWithLifecycle()
    val profileDataState by profileViewModel.profileDataState.collectAsStateWithLifecycle()

    // Refresh when wallet public key or auth status changes
    androidx.compose.runtime.LaunchedEffect(walletState.publicKey, profileViewState.firebaseAuthStatus) {
        profileViewModel.refresh(walletState.publicKey)
    }

    val aggregatedLevel = profileDataState.userProfile?.let { (it.mathLevel + it.csLevel) / 2 }

    val bannerItems = listOf(
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_5384f9f8b96a0b9934b2bc35a4058376211636d2.600x338.jpg?t=1695270428",
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_d5b6edd94e77ba6db31c44d8a3c09d807ab27751.600x338.jpg?t=1695270428",
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_a81e4231cc8d55f58b51a4a938898af46503cae5.600x338.jpg?t=1695270428",
    )
    val view = LocalView.current

    MainAppScreen(
        walletState = walletState,
        bannerItems = bannerItems,
        aggregatedLevel = aggregatedLevel,
        avatarUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString(),
        navigateToCallback = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            navController.navigate(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    walletState: WalletState,
    bannerItems: List<String>,
    aggregatedLevel: Int?,
    avatarUrl: String?,
    navigateToCallback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val preferredWidth: Dp
    val density = LocalDensity.current
    with(density) { preferredWidth = (view.width / 0.85).toInt().toDp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title */ },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { navigateToCallback("profile") }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.15f))
                                    .clickable { navigateToCallback("profile") }
                                    .padding(8.dp)
                            )
                        }
                        if (aggregatedLevel != null) {
                            Spacer(Modifier.width(8.dp))
                            LevelBadge("Level $aggregatedLevel")
                        }
                    }
                },
                actions = {
                    if (walletState.isConnected) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f))
                                .clickable { navigateToCallback("wallet") }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Wallet", color = AccentGreen, fontSize = 12.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.15f))
                                .clickable { navigateToCallback("wallet") }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Connect", color = PrimaryBlue, fontSize = 12.sp)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Floating pill-style bottom bar only on this screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp) // spacing from bottom edge
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 28.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Duels
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { navigateToCallback("duels") }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsMartialArts,
                                contentDescription = "Duels",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Duels", color = TextPrimary, fontSize = 13.sp)
                        }
                        // Profile
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { navigateToCallback("profile") }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Profile", color = TextPrimary, fontSize = 13.sp)
                        }
                        // Wallet / Connect
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { navigateToCallback("wallet") }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = if (walletState.isConnected) AccentGreen else PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (walletState.isConnected) "Wallet" else "Connect",
                                color = if (walletState.isConnected) AccentGreen else TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp, 16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            // Carousel moved above duel card
            Column {
                HorizontalMultiBrowseCarousel(
                    state = rememberCarouselState { bannerItems.count() },
                    preferredItemWidth = preferredWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    itemSpacing = 8.dp,
                ) { i ->
                    MonochromeAsyncImage(
                        bannerItems[i],
                        contentDescription = "Image $i",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(16f / 9f)
                            .maskClip(MaterialTheme.shapes.large),
                        alternateImageModifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(16f / 9f)
                    )
                }
            }

            // Simple Lottie animation (XO7TbUiuBv.json) without glow effect
            val duelBannerComposition by rememberLottieComposition(LottieCompositionSpec.Asset("XO7TbUiuBv.json"))
            LottieAnimation(
                composition = duelBannerComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navigateToCallback("duels") },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.SportsMartialArts,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Duels", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Practice & compete", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun MainAppScreenPreview() {
    BeezleTheme {
        MainAppScreen(
            walletState = WalletState(),
            bannerItems = listOf(
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_5384f9f8b96a0b9934b2bc35a4058376211636d2.600x338.jpg?t=1695270428",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_d5b6edd94e77ba6db31c44d8a3c09d807ab27751.600x338.jpg?t=1695270428",
            ),
            aggregatedLevel = 2,
            avatarUrl = null,
            navigateToCallback = {},
        )
    }
}