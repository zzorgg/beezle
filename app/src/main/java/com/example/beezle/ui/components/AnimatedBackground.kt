package com.example.beezle.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.beezle.ui.theme.CyanAqua
import com.example.beezle.ui.theme.ElectricBlue
import com.example.beezle.ui.theme.Night800
import com.example.beezle.ui.theme.Night900

@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bg")
    val shift1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift1"
    )
    val shift2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift2"
    )

    Box(
        modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Night900, Night800),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Electric blue glow
            val center1 = Offset(w * (0.2f + 0.6f * shift1), h * 0.3f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.35f), Color.Transparent),
                    center = center1,
                    radius = w * 0.5f
                ),
                radius = w * 0.5f,
                center = center1
            )

            // Cyan glow
            val center2 = Offset(w * (0.8f - 0.6f * shift2), h * 0.75f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanAqua.copy(alpha = 0.25f), Color.Transparent),
                    center = center2,
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = center2
            )
        }
    }
}
