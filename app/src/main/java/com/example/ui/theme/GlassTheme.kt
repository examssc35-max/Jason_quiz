package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class GlassColors(
    val isDark: Boolean,
    val primaryAccent: Color,
    val primaryGradient: Brush,
    val cardBackground: Color,
    val cardBackgroundStrong: Color,
    val cardBorder: Color,
    val cardBorderGradient: Brush,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val optionDefaultBg: Color,
    val optionDefaultBorder: Color,
    val optionSelectedBg: Color,
    val optionSelectedBorder: Color,
    val optionCorrectBg: Color,
    val optionWrongBg: Color,
    val greenAccent: Color = Color(0xFF10AC84),
    val purpleAccent: Color = Color(0xFF5F27CD),
    val pinkAccent: Color = Color(0xFFFF4757),
    val cyanAccent: Color = Color(0xFF00CEC9),
    val yellowAccent: Color = Color(0xFFFFA502)
)

val LocalGlassColors = staticCompositionLocalOf<GlassColors> {
    error("No GlassColors provided")
}

val AccentColors = listOf(
    Color(0xFF2E86DE), // Royal Blue (Default)
    Color(0xFF00CEC9), // Cyan Glow
    Color(0xFF5F27CD), // Electric Purple
    Color(0xFF10AC84)  // Emerald
)

@Composable
fun getGlassColors(isDark: Boolean, accentIndex: Int = 0): GlassColors {
    val accent = AccentColors.getOrElse(accentIndex) { AccentColors[0] }
    val gradient = Brush.horizontalGradient(
        colors = listOf(accent, accent.copy(alpha = 0.85f))
    )

    return if (isDark) {
        GlassColors(
            isDark = true,
            primaryAccent = accent,
            primaryGradient = gradient,
            cardBackground = Color(0x380F1E36),
            cardBackgroundStrong = Color(0x550B1528),
            cardBorder = Color(0x40FFFFFF),
            cardBorderGradient = Brush.verticalGradient(
                colors = listOf(Color(0x60FFFFFF), Color(0x18FFFFFF))
            ),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xD9FFFFFF),
            textMuted = Color(0x8AFFFFFF),
            optionDefaultBg = Color(0x28132742),
            optionDefaultBorder = Color(0x38FFFFFF),
            optionSelectedBg = Color(0x661E6FD9),
            optionSelectedBorder = Color(0xB354A0FF),
            optionCorrectBg = Color(0x4D10AC84),
            optionWrongBg = Color(0x4DFF4757)
        )
    } else {
        GlassColors(
            isDark = false,
            primaryAccent = accent,
            primaryGradient = gradient,
            cardBackground = Color(0x3DFFFFFF),
            cardBackgroundStrong = Color(0x59FFFFFF),
            cardBorder = Color(0x66FFFFFF),
            cardBorderGradient = Brush.verticalGradient(
                colors = listOf(Color(0x80FFFFFF), Color(0x33FFFFFF))
            ),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xE6FFFFFF),
            textMuted = Color(0x99FFFFFF),
            optionDefaultBg = Color(0x33FFFFFF),
            optionDefaultBorder = Color(0x55FFFFFF),
            optionSelectedBg = Color(0x801E6FD9),
            optionSelectedBorder = Color(0xE6FFFFFF),
            optionCorrectBg = Color(0x6610AC84),
            optionWrongBg = Color(0x66FF4757)
        )
    }
}
