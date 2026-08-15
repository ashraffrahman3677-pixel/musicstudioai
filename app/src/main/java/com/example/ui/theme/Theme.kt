package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FrostedGlassColorScheme = lightColorScheme(
    primary = FrostedPrimary,
    onPrimary = Color.White,
    primaryContainer = FrostedPrimaryLight,
    onPrimaryContainer = FrostedPrimaryDark,
    secondary = FrostedSecondary,
    onSecondary = Color.White,
    secondaryContainer = FrostedSecondaryContainer,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = FrostedTertiary,
    onTertiary = Color.White,
    tertiaryContainer = FrostedTertiaryContainer,
    onTertiaryContainer = Color(0xFF31111D),
    background = FrostedCanvasBackground,
    onBackground = FrostedTextPrimary,
    surface = FrostedGlassSurface,
    onSurface = FrostedTextPrimary,
    surfaceVariant = FrostedGlassSurfaceVariant,
    onSurfaceVariant = FrostedTextSecondary,
    outline = FrostedBorder,
    outlineVariant = FrostedBorderSubtle,
    error = FrostedAccentCrimson,
    onError = Color.White
)

@Composable
fun AIMusicStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrostedGlassColorScheme,
        typography = Typography,
        content = content
    )
}
