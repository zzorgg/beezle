package com.github.zzorgg.beezle.ui.screens.main

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.zzorgg.beezle.R
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.AppBottomBar
import com.github.zzorgg.beezle.ui.components.BannerMedia
import com.github.zzorgg.beezle.ui.components.BannerVideoPlayer
import com.github.zzorgg.beezle.ui.components.MonochromeAsyncImage
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
import com.github.zzorgg.beezle.ui.screens.profile.components.LevelBadge
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.google.firebase.auth.FirebaseAuth

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

    MainAppScreen(
        walletState = walletState,
        bannerItems = bannerItems,
        aggregatedLevel = aggregatedLevel,
        avatarUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString(),
        navigateToCallback = { navController.navigate(it) }
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

    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
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
                            val chipBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(chipBg)
                                    .clickable { navigateToCallback("profile") }
                                    .padding(8.dp)
                            )
                        }
                        if (aggregatedLevel != null) {
                            Spacer(Modifier.width(8.dp))
                            LevelBadge("Level $aggregatedLevel", fontSize = 14.sp)
                        }
                    }
                },
                actions = {
                    if (walletState.isConnected) {
                        val chipBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(chipBg)
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
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        val chipBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(chipBg)
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
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = { AppBottomBar( currentRoute = "main", onNavigate = navigateToCallback ) },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = WindowInsets(
            top = WindowInsets.systemBars.getTop(density),
            left = WindowInsets.systemBars.getLeft(density, LocalLayoutDirection.current),
            right = WindowInsets.systemBars.getRight(density, LocalLayoutDirection.current),
            bottom = WindowInsets.systemBars.getBottom(density) / 3
        )
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
                            fontSize = 14.sp,
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
                        .clickable {
                            // Navigate to duel screen with selected mode
                            val mode = if (selectedSubject == Subject.MATH) "math" else "cs"
                            navigateToCallback("duel/$mode")
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Real-time competitive play",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(96.dp)) // bottom padding to avoid cropping behind bottom bar
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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
