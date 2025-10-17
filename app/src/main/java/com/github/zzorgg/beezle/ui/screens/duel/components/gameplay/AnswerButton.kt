package com.github.zzorgg.beezle.ui.screens.duel.components.gameplay

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.ui.theme.BeezleTheme


@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg: Color = when {
        isSelected && isCorrect -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val border: Color = when {
        isSelected && isCorrect -> MaterialTheme.colorScheme.tertiary
        isSelected && !isCorrect -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(
                if (border != Color.Transparent) Modifier.border(
                    2.dp,
                    border,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            disabledContainerColor = bg
        ),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnswerButtonPreview() {
    BeezleTheme {
        Column {
            AnswerButton(
                text = "test",
                isSelected = false,
                isCorrect = false,
                enabled = true,
                onClick = {}
            )
            AnswerButton(
                text = "test",
                isSelected = true,
                isCorrect = true,
                enabled = false,
                onClick = {}
            )
            AnswerButton(
                text = "test",
                isSelected = true,
                isCorrect = false,
                enabled = true,
                onClick = {}
            )
        }
    }
}