package com.github.zzorgg.beezle.ui.screens.main

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.zzorgg.beezle.R
import com.github.zzorgg.beezle.data.wallet.SolanaWalletManager
import com.github.zzorgg.beezle.data.wallet.WalletState
import com.github.zzorgg.beezle.ui.components.MonochromeAsyncImage
import com.github.zzorgg.beezle.ui.theme.AccentGreen
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import com.github.zzorgg.beezle.ui.theme.SurfaceDark
import com.github.zzorgg.beezle.ui.theme.TextPrimary
import com.github.zzorgg.beezle.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreenRoot(
    navController: NavController,
) {
    val walletManager: SolanaWalletManager = viewModel()
    val walletState by walletManager.walletState.collectAsState()
    val bannerItems = listOf(
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_5384f9f8b96a0b9934b2bc35a4058376211636d2.600x338.jpg?t=1695270428",
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_d5b6edd94e77ba6db31c44d8a3c09d807ab27751.600x338.jpg?t=1695270428",
        "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_a81e4231cc8d55f58b51a4a938898af46503cae5.600x338.jpg?t=1695270428",
    )
    val view = LocalView.current

    MainAppScreen(
        walletState = walletState,
        bannerItems = bannerItems,
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
    navigateToCallback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val preferredWidth: Dp
    val density = LocalDensity.current
    with(density) {
        preferredWidth = (view.width / 0.85).toInt().toDp()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wallet",
                                color = AccentGreen,
                                fontSize = 12.sp
                            )
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
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connect",
                                color = PrimaryBlue,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp, 16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navigateToCallback("duels") },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navigateToCallback("profile") },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profile", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Stats & levels", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

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
                            .aspectRatio(16f / 9f) // 1920 x 1080
                            .maskClip(MaterialTheme.shapes.large),
                        alternateImageModifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(16f / 9f) // 1920 x 1080
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainAppScreenPreview() {
    BeezleTheme {
        MainAppScreen(
            walletState = WalletState(),
            bannerItems = listOf(
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_5384f9f8b96a0b9934b2bc35a4058376211636d2.600x338.jpg?t=1695270428",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_d5b6edd94e77ba6db31c44d8a3c09d807ab27751.600x338.jpg?t=1695270428",
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/367520/ss_a81e4231cc8d55f58b51a4a938898af46503cae5.600x338.jpg?t=1695270428",
            ),
            navigateToCallback = {},
        )
    }
}