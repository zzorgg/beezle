package com.github.zzorgg.beezle.ui.screens.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.github.zzorgg.beezle.ui.theme.BackgroundDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var showLogo by remember { mutableStateOf(true) }
    var showBackground by remember { mutableStateOf(false) }

    // Beezle Logo animation
    val logoComposition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Beezle_Logo.json")
    )
    val logoProgress by animateLottieCompositionAsState(
        composition = logoComposition,
        iterations = 1,
        speed = 0.6f, // Slightly faster for better performance
        isPlaying = showLogo
    )

    // Background animation (Bg.json) - only load when needed
//    val bgComposition by rememberLottieComposition(
//        LottieCompositionSpec.Asset("Bg.json")
//    )
//    val bgProgress by animateLottieCompositionAsState(
//        composition = bgComposition,
//        iterations = LottieConstants.IterateForever,
//        speed = 0.8f, // Faster speed for smoother animation
//        isPlaying = showBackground
//    )

    // Handle animation sequencing with better timing
    LaunchedEffect(logoProgress) {
        // Start background animation when logo is 30% complete to reduce initial load
        if (logoProgress >= 0.3f && !showBackground) {
            showBackground = true
        }

        if (logoProgress == 1f && showLogo) {
            delay(2000) // Reduced delay for better user experience
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Background animation (Bg.json) - only render when needed
//        if (showBackground) {
//            LottieAnimation(
//                composition = bgComposition,
//                progress = { bgProgress },
//                modifier = Modifier.fillMaxSize()
//            )
//        }

        // Logo animation (Beezle_Logo) - centered on top of background
        if (showLogo) {
            LottieAnimation(
                composition = logoComposition,
                progress = { logoProgress },
                modifier = Modifier.size(3000.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewSplashScreen() {

}
