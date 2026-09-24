package com.example.nova.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBlack,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = TextCyan,
    secondary = NeonViolet,
    onSecondary = CyberBlack,
    secondaryContainer = NeonPurpleDark,
    onSecondaryContainer = TextWhite,
    tertiary = NeonBlue,
    onTertiary = TextWhite,
    background = CyberBlack,
    onBackground = TextWhite,
    surface = CyberDarkSurface,
    onSurface = TextWhite,
    surfaceVariant = CyberSurfaceCard,
    onSurfaceVariant = TextGray,
    outline = CyberSurfaceBorder,
    error = NeonRed,
    onError = TextWhite
)

@Composable
fun NovaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = CyberTypography,
        content = content
    )
}
