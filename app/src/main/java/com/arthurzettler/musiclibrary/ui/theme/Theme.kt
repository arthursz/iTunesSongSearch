package com.arthurzettler.musiclibrary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    primaryContainer = AccentOrange.copy(alpha = 0.24f),
    onPrimaryContainer = AccentOrange,
    secondary = Color(0xFF8E8E93),
    onSecondary = TextPrimary,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun MusicLibraryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
