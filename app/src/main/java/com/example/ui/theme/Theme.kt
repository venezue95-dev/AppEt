package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MathPrimaryDark,
    onPrimary = MathOnPrimaryDark,
    primaryContainer = MathPrimaryContainerDark,
    onPrimaryContainer = MathOnPrimaryContainerDark,
    secondary = MathSecondaryDark,
    onSecondary = MathOnSecondaryDark,
    secondaryContainer = MathSecondaryContainerDark,
    onSecondaryContainer = MathOnSecondaryContainerDark,
    tertiary = MathTertiaryDark,
    onTertiary = MathOnTertiaryDark,
    tertiaryContainer = MathTertiaryContainerDark,
    onTertiaryContainer = MathOnTertiaryContainerDark,
    background = MathBackgroundDark,
    onBackground = MathOnBackgroundDark,
    surface = MathSurfaceDark,
    onSurface = MathOnSurfaceDark,
    surfaceVariant = MathSurfaceVariantDark,
    onSurfaceVariant = MathOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF312E81),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek calculator dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
