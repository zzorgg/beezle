package com.github.zzorgg.beezle.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@Composable
private fun NavIcon(
    route: String,
    icon: ImageVector,
    contentDesc: String,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val view = LocalView.current
    val isSelected = currentRoute == route
    val bgColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(
            alpha = 0f
        ),
        label = "iconBg"
    )
    val tint by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconTint"
    )
    IconButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onNavigate(route)
        },
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bgColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    Card(
        modifier = Modifier.clip(CircleShape),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .heightIn(min = 54.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIcon("main", Icons.Default.Home, "Home", currentRoute, onNavigate)
            NavIcon("profile", Icons.Default.Person, "Profile", currentRoute, onNavigate)
            NavIcon("leaderboards", Icons.Default.EmojiEvents, "Leaderboards", currentRoute, onNavigate)
            NavIcon("practice", Icons.Default.School, "Practice", currentRoute, onNavigate)
        }
    }
}
