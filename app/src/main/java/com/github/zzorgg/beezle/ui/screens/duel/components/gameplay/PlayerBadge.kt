package com.github.zzorgg.beezle.ui.screens.duel.components.gameplay

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.data.model.duel.DuelUser
import com.github.zzorgg.beezle.ui.components.PlayerAvatarIcon
import com.github.zzorgg.beezle.ui.theme.BeezleTheme

@Composable
fun PlayerBadge(
    player: DuelUser?,
    isCurrentPlayer: Boolean,
    score: Int,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerAvatarIcon(
            model = player?.avatarUrl,
            fallbackUsername = player?.username ?: "Opponent",
            fallbackTextColor = if (isCurrentPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = (if (isCurrentPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary).copy(
                        alpha = 0.2f
                    ),
                    shape = CircleShape
                )
                .border(
                    2.dp,
                    if (isCurrentPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    CircleShape
                ),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = player?.username ?: "Opponent",
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isCurrentPlayer) {
            Text(
                text = "(You)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Score: $score",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlayerBadgePrev() {
    BeezleTheme {
        PlayerBadge(
            player = DuelUser(
                "asda",
                username = "test",
                avatarUrl = "TOD",
            ),
            isCurrentPlayer = true,
            score = 1
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlayerBadgePrev2() {
    BeezleTheme {
        PlayerBadge(
            player = null,
            isCurrentPlayer = true,
            score = 1
        )
    }
}
