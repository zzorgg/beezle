package com.github.zzorgg.beezle.ui.screens.profile.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun LevelBadge(
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                RoundedCornerShape(20.dp)
            )
            .clip(CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = textStyle,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LevelBadgePreview() {
    BeezleTheme {
        LevelBadge("Test")
    }
}