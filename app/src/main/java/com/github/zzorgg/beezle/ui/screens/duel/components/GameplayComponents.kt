package com.github.zzorgg.beezle.ui.screens.duel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue
import com.github.zzorgg.beezle.ui.theme.SurfaceDark

// Legacy component - All gameplay logic has been moved to DuelScreen.kt
// This file is kept only for the PlayerCard component used elsewhere

@Composable
fun PlayerCard(
    username: String,
    isCurrentPlayer: Boolean
) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlayer)
                Color(0xFF1E3A8A).copy(alpha = 0.8f)
            else
                Color(0xFF1F2937).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCurrentPlayer) PrimaryBlue else SurfaceDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Player",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            if (isCurrentPlayer) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
