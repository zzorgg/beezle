package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.Question
import com.github.zzorgg.beezle.ui.screens.duel.components.gameplay.QuestionCard
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import com.google.rpc.context.AttributeContext

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun GameplayScreen(
    duelState: DuelState,
    sendAnswerCallback: (String) -> Unit
) {
    val view = LocalView.current

    val question = duelState.currentQuestion ?: return
    var answerInput by remember { mutableStateOf("") }
    val density = LocalDensity.current
    var width = 98.dp
    with(density) {
        width = (view.measuredWidth / 3.5).toInt().toDp()
        width = if (view.measuredWidth == 0) 98.dp else width
    }

    LaunchedEffect(question) {
        answerInput = ""
    }

    LaunchedEffect(duelState) {
        if (duelState.lastAnswerCorrect == true) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else if (duelState.lastAnswerCorrect == false) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScoreCard(
                name = "You",
                score = duelState.myScore,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Round ${duelState.currentRound}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ScoreCard(
                name = "Opponent",
                score = duelState.opponentScore,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        QuestionCard(question = question)

        Spacer(Modifier.weight(1f))

        Column {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .fillMaxWidth(0.65f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = answerInput.ifBlank { "Enter Answer" },
                    style = if (answerInput.isNotBlank()) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(if (answerInput.isNotBlank()) 8.dp else 12.dp),
                    textAlign = TextAlign.Center,
                )
                if (answerInput.isNotBlank()) {
                    IconButton({
                        answerInput = answerInput.substring(0, answerInput.length - 1)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            modifier = Modifier.fillMaxSize(0.65f),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        MathButtonsRow(
            answerInputCallback = {
                answerInput = answerInput + it
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            },
            submitCallback = {
                if (answerInput.toIntOrNull() != null) {
                    sendAnswerCallback(answerInput)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                }
            },
            buttonWidth = width,
        )
    }
}

@Composable
private fun MathButtonsRow(
    answerInputCallback: (Char) -> Unit,
    submitCallback: () -> Unit,
    buttonWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val buttonslist: List<List<Any>> = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('.', '0', "Submit"),
    )
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            buttonslist.forEach { buttonRow ->
                Row {
                    buttonRow.forEach {
                        if (it is Char) {
                            MathButton(
                                model = "$it",
                                onClick = { answerInputCallback(it) },
                                width = buttonWidth,
                            )
                        } else {
                            MathButton(
                                model = Icons.Default.KeyboardDoubleArrowRight,
                                onClick = submitCallback,
                                width = buttonWidth,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.6f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MathButton(
    model: String,
    onClick: () -> Unit,
    width: Dp,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .padding(4.dp),
        colors = colors,
    ) {
        Text(
            text = model,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MathButton(
    model: ImageVector,
    onClick: () -> Unit,
    width: Dp,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .padding(4.dp),
        colors = colors,
    ) {
        Icon(
            imageVector = model,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ScoreCard(
    name: String,
    score: Int,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = score.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GameplayScreenPreview() {
    BeezleTheme {
        GameplayScreen(
            duelState = DuelState(
                isConnected = true,
                currentQuestion = Question(
                    id = "wadsjkdmsna",
                    text = "Test question",
                    roundNumber = 3,
                )
            ),
            sendAnswerCallback = {}
        )
    }
}
