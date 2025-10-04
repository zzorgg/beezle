package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun DuelCompleteScreen(
    finalResult: WebSocketMessage.GameOver,
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
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "VICTORY!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = "Congratulations! You won the duel!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            isDraw -> {
                Icon(
                    Icons.Default.Balance,
                    contentDescription = "Draw",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DRAW!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Great match! You're evenly matched!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                Icon(
                    Icons.Default.SentimentDissatisfied,
                    contentDescription = "Defeat",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DEFEAT",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Better luck next time! Keep practicing!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Final Scores
        Text(
            text = "Match Complete",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f)
            ) {
                Text("Exit")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Play Again",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DuelCompleteScreenPreview() {
    BeezleTheme {
        DuelCompleteScreen(
            finalResult = WebSocketMessage.GameOver(
                data = WebSocketMessage.GameOverData(
                    match_id = "m1",
                    winner_id = "same",
                    reason = "time_up",
                    scores = mapOf("same" to 5, "opponent" to 3)
                )
            ),
            currentUserId = "same",
            onPlayAgain = {},
            onExit = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DuelCompleteScreenDefeatPreview() {
    BeezleTheme {
        DuelCompleteScreen(
            finalResult = WebSocketMessage.GameOver(
                data = WebSocketMessage.GameOverData(
                    match_id = "m2",
                    winner_id = "opponent",
                    reason = "score",
                    scores = mapOf("same" to 2, "opponent" to 4)
                )
            ),
            currentUserId = "same",
            onPlayAgain = {},
            onExit = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DuelCompleteScreenDrawPreview() {
    BeezleTheme {
        DuelCompleteScreen(
            finalResult = WebSocketMessage.GameOver(
                data = WebSocketMessage.GameOverData(
                    match_id = "m3",
                    winner_id = null,
                    reason = "draw",
                    scores = mapOf("same" to 3, "opponent" to 3)
                )
            ),
            currentUserId = "same",
            onPlayAgain = {},
            onExit = {}
        )
    }
}
