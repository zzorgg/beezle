package com.github.zzorgg.beezle.ui.screens.duel.components

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.data.model.duel.ConnectionStatus
import com.github.zzorgg.beezle.ui.theme.BeezleTheme


@Composable
fun ConnectionStatusIndicator(status: ConnectionStatus) {
    val dotColor = when (status) {
        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.tertiary
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.secondary
        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
    }

    val scale by animateFloatAsState(
        targetValue = if (status == ConnectionStatus.CONNECTING || status == ConnectionStatus.RECONNECTING) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = "statusPulse"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(dotColor)
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConnectionStatusIndicatorPreview_Connected() {
    BeezleTheme {
        ConnectionStatusIndicator(ConnectionStatus.CONNECTED)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConnectionStatusIndicatorPreview_Connecting() {
    BeezleTheme {
        ConnectionStatusIndicator(ConnectionStatus.CONNECTING)
    }
}
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConnectionStatusIndicatorPreview_Disconnected() {
    BeezleTheme {
        ConnectionStatusIndicator(ConnectionStatus.DISCONNECTED)
    }
}