package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelRoom
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelStatus
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.model.duel.Question
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.PlayerBadge
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun MatchFoundScreen(duelState: DuelState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Match Found!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        duelState.currentRoom?.let { room ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerBadge(
                    player = room.player1,
                    score = duelState.myScore,
                    isCurrentPlayer = true
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                PlayerBadge(
                    player = room.player2,
                    score = duelState.opponentScore,
                    isCurrentPlayer = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Get ready...",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WaitingForQuestionScreenPreview() {
    BeezleTheme {
        MatchFoundScreen(
            DuelState(
                connectionStatus = ConnectionStatus.CONNECTED,
                isInQueue = false,
                currentRoom = DuelRoom(
                    id = "1234",
                    player1 = DuelUser(id = "adsa", username = "test", null),
                    status = DuelStatus.WAITING_FOR_ANSWERS
                ),
                currentQuestion = Question(
                    id = "sewd",
                    text = "What is the what",
                ),
                error = null,
            )
        )
    }
}