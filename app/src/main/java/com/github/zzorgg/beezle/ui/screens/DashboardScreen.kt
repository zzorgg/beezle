package com.github.zzorgg.beezle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.github.zzorgg.beezle.ui.components.ModernAvatar
import com.github.zzorgg.beezle.ui.components.ModernCard
import com.github.zzorgg.beezle.ui.components.StatsCard

@Suppress("unused")
@Composable
fun DashboardScreen() {
    // Background animation
    val backgroundComposition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets1.lottiefiles.com/packages/lf20_jcikwtux.json")
    )
    val backgroundProgress by animateLottieCompositionAsState(
        composition = backgroundComposition,
        iterations = LottieConstants.IterateForever
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated Background
            LottieAnimation(
                composition = backgroundComposition,
                progress = { backgroundProgress },
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.03f)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Header with user profile
                item {
                    ModernCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ModernAvatar(
                                imageUrl = null,
                                name = "John Doe",
                                size = 56
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back!",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "John Doe",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Level 12 • 1,247 SOL earned",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsCard(
                            title = "Wins",
                            value = "127",
                            subtitle = "This month",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            title = "SOL Earned",
                            value = "12.47",
                            subtitle = "Total",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Quick Duel",
                            icon = Icons.Default.SportsMartialArts,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Quick duel action
                        }

                        QuickActionCard(
                            title = "Tournament",
                            icon = Icons.Default.EmojiEvents,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Tournament action
                        }
                    }
                }

                // Recent Matches
                item {
                    Text(
                        text = "Recent Matches",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) { index ->
                            MatchHistoryCard(
                                opponent = "Player${index + 1}",
                                category = if (index % 2 == 0) "Math" else "Solana",
                                result = if (index == 0) "Won" else if (index == 1) "Lost" else "Draw",
                                reward = if (index == 0) "+0.05 SOL" else if (index == 1) "-0.05 SOL" else "0 SOL",
                                time = "${index + 1} hours ago"
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val onColor = when (color) {
                    MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.onPrimary
                    MaterialTheme.colorScheme.secondary -> MaterialTheme.colorScheme.onSecondary
                    MaterialTheme.colorScheme.tertiary -> MaterialTheme.colorScheme.onTertiary
                    else -> MaterialTheme.colorScheme.onPrimary
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = onColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MatchHistoryCard(
    opponent: String,
    category: String,
    result: String,
    reward: String,
    time: String
) {
    ModernCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernAvatar(
                imageUrl = null,
                name = opponent,
                size = 40
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "vs $opponent",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$category • $time",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = result,
                    color = when (result) {
                        "Won" -> MaterialTheme.colorScheme.onTertiary
                        "Lost" -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onSecondary
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = reward,
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
