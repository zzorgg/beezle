package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.data.model.duel.WebSocketMessage
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun GameOverDialog(
    result: WebSocketMessage.GameOver,
    myId: String,
    onDismiss: () -> Unit
) {
    val isWinner = result.data.winner_id == myId
    val myScore = result.data.scores[myId] ?: 0
    val opponentScore = result.data.scores.filterKeys { it != myId }.values.firstOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isWinner) "🏆 Victory!" else "💪 Good Try!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", fontWeight = FontWeight.Bold)
                        Text(
                            myScore.toString(),
                            fontSize = 32.sp,
                            color = if (isWinner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent", fontWeight = FontWeight.Bold)
                        Text(
                            opponentScore.toString(),
                            fontSize = 32.sp,
                            color = if (!isWinner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWinner) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GameOverDialogPreview() {
    BeezleTheme {
        GameOverDialog(
            result = WebSocketMessage.GameOver(
                data = WebSocketMessage.GameOverData(
                    match_id = "asda",
                    winner_id = "saahdkjs",
                    reason = "asjdals",
                    scores = mapOf("one" to 4, "two" to 6)
                )
            ),
            myId = "one"
        ) { }
    }
}
