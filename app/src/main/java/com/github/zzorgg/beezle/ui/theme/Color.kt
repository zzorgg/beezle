package com.github.zzorgg.beezle.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Based on #155dfc
val PrimaryBlue = Color(0xFF155dfc) // Your specified blue
val PrimaryBlueLight = Color(0xFF4A7EFD) // Lighter variant
val PrimaryBlueDark = Color(0xFF0D4AD4) // Darker variant

// Background Colors - Modern dark theme like Wise
val BackgroundDark = Color(0xFF0B0E13) // Deep dark blue-black
val SurfaceDark = Color(0xFF161B22) // Card/elevated surface
val SurfaceVariant = Color(0xFF1E242C) // Secondary surfaces

// Modern Accent Colors
val AccentGreen = Color(0xFF00D26A) // Success/positive actions
val AccentOrange = Color(0xFFFF8A00) // Warning/attention
val AccentRed = Color(0xFFFF4757) // Error/negative actions
val AccentPurple = Color(0xFF7C4DFF) // Secondary accent

// Text Colors - High contrast for accessibility
val TextPrimary = Color(0xFFFFFFFF) // Primary text on dark
val TextSecondary = Color(0xFFB8BCC8) // Secondary text
val TextTertiary = Color(0xFF6C7293) // Tertiary/disabled text

// Gradient Colors for backgrounds
val GradientStart = Color(0xFF155dfc)
val GradientEnd = Color(0xFF7C4DFF)

// UI Component Colors
val CardBackground = Color(0xFF1A1F27)
val DividerColor = Color(0xFF2A2F37)
val ShimmerBase = Color(0xFF2A2F37)
val ShimmerHighlight = Color(0xFF3A3F47)

// Legacy colors for backwards compatibility
val Night900 = BackgroundDark
val Night800 = SurfaceDark
val Night700 = SurfaceVariant
val ElectricBlue = PrimaryBlue
val IceBlue = PrimaryBlueLight
val CyanAqua = AccentPurple
val OnDarkHigh = TextPrimary
val OnDarkMed = TextSecondary
val SuccessGreen = AccentGreen
val WarningAmber = AccentOrange
val ErrorRed = AccentRed
