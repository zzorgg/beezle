package com.github.zzorgg.beezle.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.profile.UserProfile
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import java.util.Locale

@Composable
fun ProfileStatsCard(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
) {
    val aggregatedLevel = userProfile?.let { (it.mathLevel + it.csLevel) / 2 }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = colors,
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Wins",
                    value = userProfile?.duelStats?.wins?.toString() ?: "0",
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
                val winRate = userProfile?.duelStats?.let {
                    if (it.total > 0) String.format(Locale.US, "%.0f%%", it.winRate * 100)
                    else "0%"
                } ?: "0%"
                StatItem(
                    icon = Icons.Default.Star,
                    label = "Win Rate",
                    value = winRate,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
                StatItem(
                    icon = Icons.Default.QueryStats,
                    label = "XP",
                    value = aggregatedLevel.toString(),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
                StatItem(
                    icon = Icons.Default.PlayArrow,
                    label = "Total Duels",
                    value = "0",
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .border(CardDefaults.outlinedCardBorder(), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .padding(end = 4.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.W300,
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = "icon for $label",
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileStatsCardPreview() {
    BeezleTheme {
        ProfileStatsCard(
            userProfile = UserProfile()
        )
    }
}
