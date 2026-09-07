package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StarsninCyan,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBBE9FF),
    secondary = StarsninIndigo,
    onSecondary = Color(0xFF1E2260),
    secondaryContainer = Color(0xFF353A7A),
    onSecondaryContainer = Color(0xFFE0E0FF),
    tertiary = StarsninPurple,
    background = StarsninDarkBg,
    onBackground = StarsninTextPrimary,
    surface = StarsninSurface,
    onSurface = StarsninTextPrimary,
    surfaceVariant = StarsninSurfaceVariant,
    onSurfaceVariant = StarsninTextSecondary,
    outline = StarsninBorder
)

private val LightColorScheme = DarkColorScheme // Default to sleek tech dark mode for the Starsnin ecosystem

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
