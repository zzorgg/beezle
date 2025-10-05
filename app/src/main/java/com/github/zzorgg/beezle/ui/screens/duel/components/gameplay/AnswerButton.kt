package com.github.zzorgg.beezle.ui.screens.duel.components.gameplay

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.ui.theme.BeezleTheme


@Composable
fun AnswerButton(
    text: String,
    index: Int,
    isSelected: Boolean,
    hasAnswered: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = when {
        isSelected && hasAnswered -> MaterialTheme.colorScheme.secondaryContainer
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(enabled = !hasAnswered) { onSelected() }
            .border(
                width = if (isSelected) 1.dp else 0.dp,
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
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ('A' + index).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnswerButtonPreview() {
    BeezleTheme {
        AnswerButton(
            text = "test",
            index = 1,
            isSelected = false,
            hasAnswered =false
        ) { }
    }
}