package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val GlassDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E86DE),
    secondary = Color(0xFF00CEC9),
    tertiary = Color(0xFF5F27CD),
    background = Color(0xFF0A111F),
    surface = Color(0xFF101B2E)
)

private val GlassLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E86DE),
    secondary = Color(0xFF00CEC9),
    tertiary = Color(0xFF5F27CD),
    background = Color(0xFF1E2D4A),
    surface = Color(0xFF283B5E)
)

@Composable
fun MyApplicationTheme(
    themePreference: String = "system",
    accentIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        "dark" -> true
        "light" -> false
        else -> systemInDark
    }

    val glassColors = getGlassColors(isDark = isDark, accentIndex = accentIndex)
    val colorScheme = if (isDark) GlassDarkColorScheme else GlassLightColorScheme

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

