package com.example.beezle.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.beezle.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Lottie animation state with slower speed
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Beezle_Logo.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.3f // Slower animation speed
    )

    LaunchedEffect(key1 = true) {
        delay(5000) // Show logo for 3 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Only the Lottie logo animation - no additional animations
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(500.dp)
        )
    }
}
