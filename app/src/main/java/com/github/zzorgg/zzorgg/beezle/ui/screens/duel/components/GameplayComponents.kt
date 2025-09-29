package com.github.zzorgg.beezle.ui.screens.duel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.DuelState
import com.github.zzorgg.beezle.data.model.duel.Question

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer and Score
        GameHeader(duelState = duelState)

        Spacer(modifier = Modifier.height(32.dp))

        // Question
        QuestionCard(question = question)

        Spacer(modifier = Modifier.height(32.dp))

        // Answer Options
        AnswerOptions(
            question = question,
            selectedAnswer = duelState.selectedAnswer,
            hasAnswered = duelState.hasAnswered,
            onAnswerSelected = onAnswerSelected
        )
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

@Composable
private fun TimerCircle(timeRemaining: Int) {
    val progress = timeRemaining / 15f
    val color = when {
        timeRemaining > 10 -> Color.Green
        timeRemaining > 5 -> Color.Yellow
        else -> Color.Red
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 6.dp
        )

        Text(
            text = timeRemaining.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
private fun QuestionCard(question: Question) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = question.text,
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun AnswerOptions(
    question: Question,
    selectedAnswer: Int?,
    hasAnswered: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        question.options.forEachIndexed { index, option ->
            AnswerButton(
                text = option,
                index = index,
                isSelected = selectedAnswer == index,
                hasAnswered = hasAnswered,
                onSelected = { if (!hasAnswered) onAnswerSelected(index) }
            )
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    index: Int,
    isSelected: Boolean,
    hasAnswered: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = when {
        isSelected && hasAnswered -> Color(0xFF3B82F6)
        isSelected -> Color(0xFF1E40AF)
        else -> Color.Black.copy(alpha = 0.2f)
    }

    val borderColor = if (isSelected) Color(0xFF60A5FA) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(enabled = !hasAnswered) { onSelected() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Letter (A, B, C, D)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White else Color(0xFF374151)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('A' + index).toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PlayerCard(
    username: String,
    isCurrentPlayer: Boolean
) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlayer)
                Color(0xFF1E3A8A).copy(alpha = 0.8f)
            else
                Color(0xFF1F2937).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE94560),
                                Color(0xFFF27121)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Player",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
