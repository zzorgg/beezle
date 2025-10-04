package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelRoom
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelStatus
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.model.duel.Question
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        ) {
            Text(
                text = "CONTINUE",
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
    val cardColor = when (wasCorrect) {
        true -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }

    val iconColor = when (wasCorrect) {
        true -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    val icon = when (wasCorrect) {
        true -> Icons.Default.CheckCircle
        false -> Icons.Default.Cancel
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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result Icon
            Icon(
                icon,
                contentDescription = when (wasCorrect) {
                    true -> "Correct"
                    false -> "Incorrect"
                    else -> "No answer"
                },
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge,
                color = if (wasCorrect == true) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (isCurrentPlayer) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "(You)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (wasCorrect == true) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoundResultScreenPreview() {
    BeezleTheme {
        RoundResultScreen(
            result = WebSocketMessage.RoundResult(
                data = WebSocketMessage.RoundResultData(
                    player1_correct = true, player2_correct = false, correct_answer = 2
                )
            ),
            duelState = DuelState(
                isConnected = true,
                isInQueue = false,
                currentRoom = DuelRoom(
                    id = "1234",
                    player1 = DuelUser(id = "adsa", username = "test", null),
                    status = DuelStatus.WAITING_FOR_ANSWERS
                ),
                currentQuestion = Question(
                    id = "sewd",
                    text = "What is the what",
                    options = listOf("Wh", "Wha", "W", "What"),
                    correctAnswer = 3
                ),
                selectedAnswer = 2,
                hasAnswered = false,
                error = null,
                isSearching = false,
                connectionStatus = ConnectionStatus.CONNECTED,
            ),
            onContinue = { }
        )
    }
}