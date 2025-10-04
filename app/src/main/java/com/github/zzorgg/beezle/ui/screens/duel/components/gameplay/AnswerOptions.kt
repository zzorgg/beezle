package com.github.zzorgg.beezle.ui.screens.duel.components.gameplay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.Question


@Composable
fun AnswerOptions(
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

