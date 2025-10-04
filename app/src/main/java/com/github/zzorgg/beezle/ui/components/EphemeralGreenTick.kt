package com.github.zzorgg.beezle.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme

/**
 * Ephemeral one-shot green tick animation. Plays once when visible becomes true,
 * then invokes onFinished exactly once at end of animation.
 */
@Composable
fun EphemeralGreenTick(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
    fullScreen: Boolean = false,
) {
    if (!visible) return
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Green tick.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true,
        restartOnPlay = false,
    )
    LaunchedEffect(progress) {
        if (progress >= 1f) {
            onFinished()
        }
    }
    if (fullScreen) {
        // Full screen overlay with dim + central enlarged tick
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            // Subtle glowing container behind tick
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            )
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(180.dp)
            )
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(72.dp)
            )
        }
    }
}
