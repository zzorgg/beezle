package com.example.beezle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AccentPurple,
    onSecondary = Color.White,
    secondaryContainer = AccentPurple,
    onSecondaryContainer = Color.White,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = DividerColor,
    surfaceContainer = CardBackground,
    surfaceContainerHigh = SurfaceVariant,
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = Color.White,
    secondary = AccentPurple,
    onSecondary = Color.White,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFF),
    onBackground = Color(0xFF0A0D10),
    surface = Color.White,
    onSurface = Color(0xFF0A0D10),
    surfaceVariant = Color(0xFFF1F3F7),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFFE2E8F0),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun BeezleTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme || isSystemInDarkTheme()) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}