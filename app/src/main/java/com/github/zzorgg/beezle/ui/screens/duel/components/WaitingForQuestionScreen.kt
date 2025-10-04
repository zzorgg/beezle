package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelRoom
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelStatus
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.model.duel.Question
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.PlayerCard
import com.github.zzorgg.beezle.ui.theme.BeezleTheme


@Composable
fun WaitingForQuestionScreen(duelState: DuelState) {
    duelState.currentRoom?.let { room ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Match Found!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Player vs Player UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerCard(
                    user = room.player1,
                    isCurrentPlayer = true,
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PlayerCard(
                    user = room.player2,
                    isCurrentPlayer = false,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Get Ready!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WaitingForQuestionScreenPreview() {
    BeezleTheme {
        WaitingForQuestionScreen(
            DuelState(
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
            )
        )
    }
}