package com.github.zzorgg.beezle.ui.screens.duel.components.gameplay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TimerCircle(timeRemaining: Int) {
    val progress = timeRemaining / 15f
    val ringColor = when {
        timeRemaining > 10 -> MaterialTheme.colorScheme.tertiary
        timeRemaining > 5 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            strokeWidth = 6.dp
        )
        Text(
            text = timeRemaining.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = ringColor
        )
    }
}
