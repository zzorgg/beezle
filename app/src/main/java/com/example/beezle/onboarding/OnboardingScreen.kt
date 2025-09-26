package com.example.beezle.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beezle.ui.components.GradientButton
import com.example.beezle.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    // Content animation
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, delayMillis = 300),
        label = "content_alpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(contentAlpha)
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // Pager Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    OnboardingSlide(
                        title = when (page) {
                            0 -> "Welcome to Beezle"
                            1 -> "Compete & Win"
                            else -> "Earn SOL Rewards"
                        },
                        description = when (page) {
                            0 -> "The ultimate Solana-powered duel platform. Compete in real-time challenges and earn SOL instantly."
                            1 -> "Challenge opponents worldwide. Answer questions under time pressure and prove your skills."
                            else -> "Every victory earns you SOL tokens. Fast, secure, and transparent payouts powered by Solana blockchain."
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        color = PrimaryBlue,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Text(
                                    text = when (page) {
                                        0 -> "⚡"
                                        1 -> "🎯"
                                        else -> "💰"
                                    },
                                    fontSize = 64.sp,
                                    color = Color.White
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Page Indicators
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (index == pagerState.currentPage) 24.dp else 8.dp,
                                    height = 8.dp
                                )
                                .background(
                                    color = if (index == pagerState.currentPage) PrimaryBlue else TextTertiary,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = if (pagerState.currentPage < 2) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.SpaceBetween // Keep consistent arrangement
                    },
                    verticalAlignment = Alignment.CenterVertically // Add vertical alignment
                ) {
                    if (pagerState.currentPage < 2) {
                        // Skip Button
                        androidx.compose.material3.TextButton(
                            onClick = onGetStarted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = "Skip",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // Next Button
                        GradientButton(
                            text = "Next",
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            icon = Icons.AutoMirrored.Filled.ArrowForward
                        )
                    } else {
                        // Empty spacer to maintain alignment
                        Spacer(modifier = Modifier.width(1.dp))

                        // Get Started Button - centered but with proper width
                        GradientButton(
                            text = "Get Started",
                            onClick = onGetStarted,
                            modifier = Modifier.width(200.dp) // Fixed width instead of fillMaxWidth
                        )

                        // Empty spacer to maintain alignment
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }
            }
        }
    }
}
