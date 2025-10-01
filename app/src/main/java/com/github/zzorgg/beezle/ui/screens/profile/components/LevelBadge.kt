package com.github.zzorgg.beezle.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zzorgg.beezle.ui.theme.PrimaryBlue

@Composable
fun LevelBadge(text: String) {
    Box(
        modifier = Modifier.Companion
            .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
