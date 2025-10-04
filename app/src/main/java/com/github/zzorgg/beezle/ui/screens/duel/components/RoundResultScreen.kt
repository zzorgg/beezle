package com.github.zzorgg.beezle.ui.screens.duel.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Legacy file - All round result handling is now done in DuelScreen.kt
// This file is kept for backward compatibility but is not used in the new implementation

@Composable
fun DuelCompleteScreen(
    isWinner: Boolean,
    isDraw: Boolean,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Result Animation
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        // Result Icon and Text
        when {
            isWinner -> {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Victory",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "VICTORY!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                )

                Text(
                    text = "Congratulations! You won the duel!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            isDraw -> {
                Icon(
                    Icons.Default.Balance,
                    contentDescription = "Draw",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DRAW!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                    )
                )

                Text(
                    text = "Great match! You're evenly matched!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                Icon(
                    Icons.Default.SentimentDissatisfied,
                    contentDescription = "Defeat",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DEFEAT",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                )

                Text(
                    text = "Better luck next time! Keep practicing!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onExit,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Exit")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "Play Again",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
