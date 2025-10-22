package com.github.zzorgg.beezle.ui.screens.main.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.DuelMode
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun DuelCard(
    name: String,
    type: DuelMode,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                Row(
                    modifier = Modifier
                        .background(
                            color =
                                if (type == DuelMode.MATH) MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.25f
                                )
                                else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = type.name,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        color =
                            if (type == DuelMode.MATH) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                var displayName by remember { mutableStateOf(name) }
                Text(
                    text = displayName.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    onTextLayout = { result ->
                        if (result.lineCount < 2) {
                            displayName = name.substring(0, name.lastIndexOf(' ')) +
                                    "\n" +
                                    name.substring(name.lastIndexOf(' ') + 1)
                        }
                    }
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.W300,
                    )
                }
            }
            Icon(Icons.Default.PlayArrow, "Start Duel")
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DuelCardPreview() {
    BeezleTheme {
        DuelCard(
            name = "Math Duel",
            type = DuelMode.MATH,
            description = "Basic test of math skills"
        )
    }
}
