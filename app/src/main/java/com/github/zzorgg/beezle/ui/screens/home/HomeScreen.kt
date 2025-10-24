package com.github.zzorgg.beezle.ui.screens.home

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.withStyle
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
import com.github.zzorgg.beezle.ui.screens.profile.ProfileDataState
import com.github.zzorgg.beezle.ui.screens.profile.ProfileViewModel
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

    // Track selected duel mode for the single large card
    var selectedMode by remember { mutableStateOf(DuelMode.MATH) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    // Show the signed-in user's name when available
                    profileDataState.userProfile?.username?.let { username ->
                        Text(text = username, style = MaterialTheme.typography.titleLarge)
                    }
                },
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

            // Welcome Lottie + Stats area with reserved space to prevent shifting
            val duelBannerComposition by rememberLottieComposition(LottieCompositionSpec.Asset("XO7TbUiuBv.json"))
            val progress by animateLottieCompositionAsState(
                duelBannerComposition,
                restartOnPlay = false,
                reverseOnRepeat = false,
                speed = 1.5f
            )
            val lottieVisible = showWelcomeGif && progress < 1f
            val hasStats = profileDataState.userProfile != null

            if (hasStats) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ProfileStatsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (lottieVisible) 0f else 1f),
                        userProfile = profileDataState.userProfile,
                        beezleCoins = 0,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    )
                    if (lottieVisible) {
                        LottieAnimation(
                            composition = duelBannerComposition,
                            progress = { progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                AnimatedVisibility(visible = lottieVisible) {
                    LottieAnimation(
                        composition = duelBannerComposition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(vertical = 4.dp)
                    )
                }
            }
            if (showWelcomeGif && progress >= 1f) {
                onWelcomeGifComplete()
            }

            // Toggle between Math and CS cards
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val primary = MaterialTheme.colorScheme.primary
                val tertiary = MaterialTheme.colorScheme.tertiary

                Button(
                    onClick = { selectedMode = DuelMode.MATH },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (selectedMode == DuelMode.MATH) primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = primary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Math")
                }
                Button(
                    onClick = { selectedMode = DuelMode.CS },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (selectedMode == DuelMode.CS) tertiary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = tertiary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "CS")
                }
            }

            // Increase spacing to shift the card a bit down from the buttons
            Spacer(Modifier.height(10.dp))

            // Single large duel card with slide animation between modes
            AnimatedContent(
                targetState = selectedMode,
                transitionSpec = {
                    val toIndex = when (targetState) {
                        DuelMode.MATH -> 0
                        DuelMode.CS -> 1
                        DuelMode.GENERAL -> 2
                    }
                    val fromIndex = when (initialState) {
                        DuelMode.MATH -> 0
                        DuelMode.CS -> 1
                        DuelMode.GENERAL -> 2
                    }
                    if (toIndex > fromIndex) {
                        // Slide in from right, slide out to left
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        // Slide in from left, slide out to right
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                modifier = Modifier.fillMaxWidth()
            ) { mode ->
                LargeDuelCard(
                    mode = mode,
                    onClick = { navigateToRootCallback(Route.Duels(mode)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LargeDuelCard(
    mode: DuelMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val (title, chipColor, onChipColor) = when (mode) {
        DuelMode.MATH -> Triple("Math Duel", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        DuelMode.CS, DuelMode.GENERAL -> Triple("CS Duel", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
    }
    val punchline = when (mode) {
        DuelMode.MATH -> "Sharpen your mind with rapid-fire challenges"
        DuelMode.CS, DuelMode.GENERAL -> "Battle algorithms and concepts in real-time"
    }
    val gifAsset = when (mode) {
        DuelMode.MATH -> "file:///android_asset/mathduel.gif"
        DuelMode.CS, DuelMode.GENERAL -> "file:///android_asset/csduel.gif"
    }

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top media (GIF)
            MonochromeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(gifAsset)
                    .decoderFactory(
                        if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory() else GifDecoder.Factory()
                    )
                    .build(),
                contentDescription = "$title preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7f),
                alternateImageModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 7f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced gradient title text
                    val gradient = if (mode == DuelMode.MATH) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            ),
                            tileMode = TileMode.Clamp
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.secondary
                            ),
                            tileMode = TileMode.Clamp
                        )
                    }
                    val styledTitle = buildAnnotatedString {
                        withStyle(SpanStyle(brush = gradient)) {
                            append(title.uppercase())
                        }
                    }
                    Text(
                        text = styledTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = chipColor,
                            contentColor = onChipColor
                        )
                    ) {
                        Text(text = "Start Duel")
                    }
                }
                // New punchline below the title + button
                Text(
                    text = punchline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
