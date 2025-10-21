package com.github.zzorgg.beezle.ui.screens.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.data.model.profile.UserProfile
import com.github.zzorgg.beezle.ui.components.PlayerAvatarIcon
import com.github.zzorgg.beezle.ui.screens.profile.components.LevelBadge
import com.github.zzorgg.beezle.ui.theme.BeezleTheme
import java.util.Locale

@Composable
fun ProfileStatsCard(
    modifier: Modifier = Modifier,
    userProfile: UserProfile?,
    beezleCoins: Int = 0, // TODO: Add to UserProfile when implemented
) {
    val aggregatedLevel = userProfile?.let { (it.mathLevel + it.csLevel) / 2 }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors( containerColor = MaterialTheme.colorScheme.surfaceContainerLow )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Avatar and username
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatarIcon(
                    model = userProfile?.avatarUrl,
                    fallbackUsername = userProfile?.username ?: "Player",
                    fallbackTextColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                LevelBadge("Level $aggregatedLevel", fontSize = 14.sp)
            }

            // Right side: Stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Wins",
                    value = userProfile?.duelStats?.wins?.toString() ?: "0",
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(8.dp))
                val winRate = userProfile?.duelStats?.let {
                    if (it.total > 0) String.format(Locale.US, "%.0f%%", it.winRate * 100)
                    else "0%"
                } ?: "0%"
                StatItem(
                    icon = Icons.Default.Star,
                    label = "Win Rate",
                    value = winRate,
                    iconTint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                StatItem(
                    icon = Icons.Default.Star,
                    label = "Beezle Coins",
                    value = beezleCoins.toString(),
                    iconTint = MaterialTheme.colorScheme.secondary
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
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "$label: ",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun ProfileStatsCardPreview() {
    BeezleTheme {
        ProfileStatsCard(
            userProfile = null
        )
    }
}
