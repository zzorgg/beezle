package com.github.zzorgg.beezle.ui.screens.home

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.zzorgg.beezle.R
import com.github.zzorgg.beezle.data.model.duel.DuelMode
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.ui.components.BannerMedia
import com.github.zzorgg.beezle.ui.components.BannerVideoPlayer
import com.github.zzorgg.beezle.ui.components.MonochromeAsyncImage
import com.github.zzorgg.beezle.ui.components.PlayerAvatarIcon
import com.github.zzorgg.beezle.ui.components.ProfileStatsCard
import com.github.zzorgg.beezle.ui.navigation.Route
import com.github.zzorgg.beezle.ui.screens.home.components.DuelCard
import com.github.zzorgg.beezle.ui.screens.profile.ProfileDataState
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
import com.github.zzorgg.beezle.ui.screens.profile.components.LevelBadge
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreenRoot(
    navigateToRootCallback: (Route) -> Unit,
    navigateToTopLevelCallback: (Route) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileViewState by profileViewModel.profileViewState.collectAsStateWithLifecycle()
    val profileDataState by profileViewModel.profileDataState.collectAsStateWithLifecycle()
    
    val localData by homeViewModel.localData.collectAsStateWithLifecycle()

    LaunchedEffect(
        walletState.publicKey,
        profileViewState.firebaseAuthStatus
    ) {
        profileViewModel.refresh(walletState.publicKey)
    }

    val bannerItems = listOf(
        BannerMedia.AssetGif(R.drawable.maths_banner),
        BannerMedia.AssetGif(R.drawable.cs_banner),
    )

    MainAppScreen(
        profileDataState = profileDataState,
        bannerItems = bannerItems,
        navigateToRootCallback = navigateToRootCallback,
        navigateToTopLevelCallback = navigateToTopLevelCallback,
        showWelcomeGif = !localData.hasWelcomeGifCompleted,
        onWelcomeGifComplete = { homeViewModel.finishWelcomeGif() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    profileDataState: ProfileDataState,
    bannerItems: List<BannerMedia>,
    navigateToRootCallback: (Route) -> Unit,
    navigateToTopLevelCallback: (Route) -> Unit,
    showWelcomeGif: Boolean,
    onWelcomeGifComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { bannerItems.size })
    val aggregatedLevel = profileDataState.userProfile?.let { (it.mathLevel + it.csLevel) / 2 }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { /* No title */ },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        PlayerAvatarIcon(
                            model = profileDataState.userProfile?.avatarUrl,
                            fallbackUsername = profileDataState.userProfile?.username ?: "Player",
                            fallbackTextColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { navigateToTopLevelCallback(Route.Profile) },
                            error = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Image(
                                        Icons.Default.Person,
                                        contentDescription = "Default person icon, please sign in...",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        LevelBadge("Level $aggregatedLevel", fontSize = 14.sp)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(8.dp, 2.dp)
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

            Column {
                val duelBannerComposition by rememberLottieComposition(LottieCompositionSpec.Asset("XO7TbUiuBv.json"))
                val progress by animateLottieCompositionAsState(
                    duelBannerComposition,
                    restartOnPlay = false,
                    reverseOnRepeat = false,
                    speed = 1.5f
                )
                AnimatedVisibility(visible = showWelcomeGif && progress < 1f) {
                    LottieAnimation(
                        composition = duelBannerComposition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(vertical = 8.dp)
                    )
                }
                if(showWelcomeGif && progress >= 1f) {
                    onWelcomeGifComplete()
                }
                AnimatedVisibility(visible = !showWelcomeGif || (progress >= 1f && profileDataState.userProfile != null)) {
                    ProfileStatsCard(
                        modifier = Modifier.padding(vertical = 8.dp),
                        userProfile = profileDataState.userProfile,
                        beezleCoins = 0, // TODO: Implement in-app currency
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val duels = listOf(
                Triple<String, DuelMode, String?>(
                    "Math Duel",
                    DuelMode.MATH,
                    "Real-time competitive play"
                ),
                Triple<String, DuelMode, String?>(
                    "General CS Duel",
                    DuelMode.CS,
                    "Real-time competitive play"
                ),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                duels.forEach {
                    DuelCard(
                        name = it.first,
                        type = it.second,
                        description = it.third,
                        modifier = Modifier.clickable { navigateToRootCallback(Route.Duels(it.second)) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainAppScreenPreview() {
    BeezleTheme {
        MainAppScreen(
            bannerItems = listOf(
                BannerMedia.AssetGif(R.drawable.cs_banner),
                BannerMedia.RemoteImage("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_5384f9f8b96a0b9934b2bc35a4058376211636d2.600x338.jpg?t=1695270428"),
                BannerMedia.AssetGif(R.drawable.cs_banner),
            ),
            navigateToRootCallback = { },
            navigateToTopLevelCallback = {},
            onWelcomeGifComplete = {},
            profileDataState = ProfileDataState(),
            showWelcomeGif = false,
        )
    }
}
