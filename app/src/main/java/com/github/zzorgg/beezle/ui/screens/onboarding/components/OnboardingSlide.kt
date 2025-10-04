package com.github.zzorgg.beezle.ui.screens.onboarding.components

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.R
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.github.zzorgg.beezle.ui.theme.primaryBlue

@Composable
fun OnboardingSlide(
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    // Animation for slide content
    val slideAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, delayMillis = 200),
        label = "slide_alpha"
    )

    val title = when (pageIndex) {
        0 -> "Welcome to Beezle"
        1 -> "Compete & Win"
        else -> "Earn SOL Rewards"
    }
    val description = when (pageIndex) {
        0 -> "The ultimate Solana-powered duel platform. Compete in real-time challenges and earn SOL instantly."
        1 -> "Challenge opponents worldwide. Answer questions under time pressure and prove your skills."
        else -> "Every victory earns you SOL tokens. Fast, secure, and transparent payouts powered by Solana blockchain."
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .alpha(slideAlpha)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon/Animation Container
        Box(
            modifier = Modifier.padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(color = primaryBlue, shape = CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (pageIndex) {
                    0 -> Image(
                        painter = painterResource(R.drawable.bee),
                        contentDescription = "Bee icon",
                        modifier = Modifier.size(120.dp)
                    )
                    1 -> Icon(
                        imageVector = Icons.Default.SportsMartialArts,
                        contentDescription = "Duel icon",
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                    2 -> Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Rewards wallet icon",
                        tint = Color.White,
                        modifier = Modifier.size(66.dp)
                    )
                    else -> Text(
                        text = "💰",
                        fontSize = 64.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingSlidePreview() {
    BeezleTheme {
        OnboardingSlide(
            pageIndex = 0
        )
    }
}