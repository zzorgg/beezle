package com.github.zzorgg.beezle.ui.screens.main

import android.content.res.Configuration
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.BannerMedia
import com.github.zzorgg.beezle.ui.components.BannerVideoPlayer
import com.github.zzorgg.beezle.ui.components.MonochromeAsyncImage
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
import com.github.zzorgg.beezle.ui.screens.profile.components.LevelBadge
import com.github.zzorgg.beezle.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.github.zzorgg.beezle.R
import com.github.zzorgg.beezle.ui.components.ProfileIcon

private enum class Subject { MATH, CS }

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
    androidx.compose.runtime.LaunchedEffect(
        walletState.publicKey,
        profileViewState.firebaseAuthStatus
    ) {
        profileViewModel.refresh(walletState.publicKey)
    }

    val aggregatedLevel = profileDataState.userProfile?.let { (it.mathLevel + it.csLevel) / 2 }

    val bannerItems = listOf(
        BannerMedia.AssetGif(R.drawable.maths_banner),
        BannerMedia.AssetGif(R.drawable.cs_banner),
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
    bannerItems: List<BannerMedia>,
    aggregatedLevel: Int?,
    avatarUrl: String?,
    navigateToCallback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectLabels = mapOf(Subject.MATH to "Math", Subject.CS to "CS")
    var selectedSubject by remember { mutableStateOf(Subject.MATH) }
    // Remove preferredWidth shrink; use full width banners like duel card
    val pagerState = rememberPagerState(pageCount = { bannerItems.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title */ },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        ProfileIcon(model = avatarUrl)
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
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                .clickable { navigateToCallback("wallet") }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Wallet",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .clickable { navigateToCallback("wallet") }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Connect",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(
                            alpha = 0.95f
                        )
                    ),
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Duels",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Profile",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
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
                                tint = if (walletState.isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (walletState.isConnected) "Wallet" else "Connect",
                                color = if (walletState.isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Column {
                val view = LocalView.current
                val preferredWidth: Dp
                val density = LocalDensity.current
                with(density) {
                    preferredWidth = (view.width / 0.85).toInt().toDp()
                }
                HorizontalMultiBrowseCarousel(
                    state = rememberCarouselState { bannerItems.size },
                    modifier = Modifier.fillMaxWidth(),
                    preferredItemWidth = preferredWidth,
                    itemSpacing = 8.dp,
                ) { page ->
                    val item = bannerItems[page]
                    val playing = (page == pagerState.currentPage)
                    val itemModifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .maskClip(MaterialTheme.shapes.large)
                    when (item) {
                        is BannerMedia.RemoteImage -> {
                            MonochromeAsyncImage(
                                item.url,
                                contentDescription = "Banner $page",
                                contentScale = ContentScale.Crop,
                                modifier = itemModifier,
                                alternateImageModifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                        }

                        is BannerMedia.AssetGif -> {
                            MonochromeAsyncImage(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(item.assetFile)
                                    .decoderFactory(
                                        if (Build.VERSION.SDK_INT >= 28) {
                                            ImageDecoderDecoder.Factory()
                                        } else {
                                            GifDecoder.Factory()
                                        }
                                    )
                                    .build(),
                                contentDescription = "GIF $page",
                                contentScale = ContentScale.Crop,
                                modifier = itemModifier,
                                alternateImageModifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                        }

                        is BannerMedia.AssetVideo -> {
                            BannerVideoPlayer(
                                assetFile = item.assetFile,
                                autoplay = item.autoplay,
                                loop = item.loop,
                                playing = playing,
                                modifier = itemModifier
                            )
                        }
                    }
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

            // Replace Welcome header + Subject toggle with only colored subject tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Subject.entries.forEach { subject ->
                    val selected = subject == selectedSubject
                    val baseColor =
                        if (subject == Subject.MATH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    val bgColor by animateColorAsState(
                        if (selected) baseColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        label = "subjectBg"
                    )
                    val textColor by animateColorAsState(
                        if (selected) baseColor else baseColor.copy(alpha = 0.75f),
                        label = "subjectText"
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable { selectedSubject = subject }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            subjectLabels[subject]!!,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Two cards: Duel Mode & Practice Mode for selected subject
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigateToCallback("duels") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsMartialArts,
                                contentDescription = null,
                                tint = if (selectedSubject == Subject.MATH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${subjectLabels[selectedSubject]} Duel Mode",
                                color = if (selectedSubject == Subject.MATH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Real-time competitive play",
//                            color = MaterialTheme.colorScheme.secondary,
                            color = (if (selectedSubject == Subject.MATH) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.85f
                            ) else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigateToCallback("practice/${selectedSubject.name.lowercase()}") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (selectedSubject == Subject.MATH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${subjectLabels[selectedSubject]} Practice Mode",
                                color = if (selectedSubject == Subject.MATH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Single-player training & streaks",
                            color = (if (selectedSubject == Subject.MATH) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.85f
                            ) else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(96.dp)) // bottom padding to avoid cropping behind bottom bar
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainAppScreenPreview() {
    BeezleTheme {
        MainAppScreen(
            walletState = WalletState(),
            bannerItems = listOf(
                BannerMedia.AssetGif(R.drawable.cs_banner),
                BannerMedia.RemoteImage("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_5384f9f8b96a0b9934b2bc35a4058376211636d2.600x338.jpg?t=1695270428"),
                BannerMedia.AssetGif(R.drawable.cs_banner),
            ),
            aggregatedLevel = 2,
            avatarUrl = null,
            navigateToCallback = {},
        )
    }
}
