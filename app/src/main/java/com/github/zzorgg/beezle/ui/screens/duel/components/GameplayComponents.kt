package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.data.model.duel.DuelRoom
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.DuelStatus
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.data.model.duel.Question
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.AnswerOptions
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.QuestionCard
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.TimerCircle
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun GameplayScreen(
    duelState: DuelState,
    onAnswerSelected: (Int) -> Unit,
    onClearRoundResult: () -> Unit
) {
    val question = duelState.currentQuestion ?: return

    // Show round result if available
    duelState.lastRoundResult?.let { result ->
        RoundResultScreen(
            result = result,
            duelState = duelState,
            onContinue = onClearRoundResult
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Timer and Score
        GameHeader(duelState = duelState)

        // Question
        QuestionCard(question = question)

        // Answer Options
        AnswerOptions(
            question = question,
            selectedAnswer = duelState.selectedAnswer,
            hasAnswered = duelState.hasAnswered,
            onAnswerSelected = onAnswerSelected
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GameplayScreenPreview() {
    BeezleTheme {
        GameplayScreen(
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
//                lastRoundResult = WebSocketMessage.RoundResult(
//                    data = WebSocketMessage.RoundResultData(
//                        true,
//                        false,
//                        2
//                    )
//                )
            ),
            onAnswerSelected = {}
        ) { }
    }
}

@Composable
private fun GameHeader(duelState: DuelState) {
    duelState.currentRoom?.let { _ ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Timer
            TimerCircle(timeRemaining = duelState.timeRemaining)
        }
    }
}

