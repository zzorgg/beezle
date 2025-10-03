package com.github.zzorgg.beezle.ui.screens.duel.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage


@Composable
fun DuelCompleteScreen(
    finalResult: WebSocketMessage.DuelComplete,
    currentUserId: String,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val isWinner = finalResult.data.winner_id == currentUserId
    val isDraw = finalResult.data.winner_id == null

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

        Spacer(modifier = Modifier.height(32.dp))

        // Final Scores
        Text(
            text = "Match Complete",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )

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
                    containerColor = Color(0xFF2196F3) // Vivid blue
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
