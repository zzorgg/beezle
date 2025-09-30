package com.github.zzorgg.beezle.ui.screens.duel.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import kotlinx.coroutines.delay

@Composable
fun RoundResultScreen(
    result: WebSocketMessage.RoundResult,
    duelState: DuelState,
    onContinue: () -> Unit
) {
    val room = duelState.currentRoom

    LaunchedEffect(Unit) {
        delay(3000) // Auto continue after 3 seconds
        onContinue()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Round Result Title
        Text(
            text = "ROUND COMPLETE",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Player Results
        room?.let { r ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerResultCard(
                    username = r.player1.username,
                    wasCorrect = result.data.player1_correct,
                    isCurrentPlayer = true
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560)
                    )
                )

                PlayerResultCard(
                    username = r.player2?.username ?: "Unknown",
                    wasCorrect = result.data.player2_correct,
                    isCurrentPlayer = false
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3) // Vivid blue
            )
        ) {
            Text(
                text = "CONTINUE",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PlayerResultCard(
    username: String,
    wasCorrect: Boolean?,
    isCurrentPlayer: Boolean
) {
    val cardColor = when {
        wasCorrect == true -> Color(0xFF10B981).copy(alpha = 0.2f)
        wasCorrect == false -> Color(0xFFEF4444).copy(alpha = 0.2f)
        else -> Color(0xFF6B7280).copy(alpha = 0.2f)
    }

    val iconColor = when {
        wasCorrect == true -> Color(0xFF10B981)
        wasCorrect == false -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val icon = when {
        wasCorrect == true -> Icons.Default.CheckCircle
        wasCorrect == false -> Icons.Default.Cancel
        else -> Icons.AutoMirrored.Filled.HelpOutline
    }

    // Animation for result reveal
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result Icon
            Icon(
                icon,
                contentDescription = when {
                    wasCorrect == true -> "Correct"
                    wasCorrect == false -> "Incorrect"
                    else -> "No answer"
                },
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            if (isCurrentPlayer) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

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
