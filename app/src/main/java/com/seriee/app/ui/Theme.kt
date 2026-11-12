package com.seriee.app.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.seriee.app.data.AppTheme

object SerieColors {
    val Blue400 = Color(0xFF60A5FA)
    val Blue500 = Color(0xFF3B82F6)
    val Blue600 = Color(0xFF2563EB)
    val Indigo500 = Color(0xFF6366F1)
    val Emerald = Color(0xFF34D399)
    val Amber = Color(0xFFFBBF24)
    val Red = Color(0xFFF87171)
    val Orange = Color(0xFFFB923C)
    val Magenta = Color(0xFFF0559B)
    val Violet = Color(0xFF8B5CF6)
    val Cyan = Color(0xFF06B6D4)
}

/**
 * Resolved design tokens for the active theme.
 *
 * [accent] is independent from [primary]: primary stays blue for selection and
 * CTAs, while accent is used for the wordmark, badges and the custom theme.
 */
@Immutable
data class SeriePalette(
    val bg: Color,
    val card: Color,
    val surface: Color,
    val elevated: Color,
    val chip: Color,
    val border: Color,
    val primary: Color,
    val accent: Color,
    val accentSoft: Color,
    /** Text/icon colour that stays readable on [accent] and [primary]. */
    val onAccent: Color,
    val text: Color,
    val muted: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val isLight: Boolean,
    val gradient: List<Color>? = null,
    val isGlass: Boolean = false
)

private fun basePalette(
    bg: Color,
    card: Color,
    surface: Color,
    elevated: Color,
    chip: Color,
    border: Color,
    primary: Color,
    accent: Color,
    text: Color,
    muted: Color,
    success: Color,
    warning: Color,
    danger: Color,
    isLight: Boolean,
    gradient: List<Color>? = null,
    isGlass: Boolean = false
) = SeriePalette(
    bg = bg,
    card = card,
    surface = surface,
    elevated = elevated,
    chip = chip,
    border = border,
    primary = primary,
    accent = accent,
    accentSoft = accent.copy(alpha = 0.16f),
    onAccent = if (accent.luminance() > 0.6f) Color(0xFF0F172A) else Color.White,
    text = text,
    muted = muted,
    success = success,
    warning = warning,
    danger = danger,
    isLight = isLight,
    gradient = gradient,
    isGlass = isGlass
)

private val DarkPalette = basePalette(
    bg = Color(0xFF0A0F18),
    card = Color(0xFF151E2D),
    surface = Color(0xFF111826),
    elevated = Color(0xFF1A2434),
    chip = Color(0xFF1C2739),
    border = Color(0xFF233047),
    primary = Color(0xFF3B82F6),
    accent = Color(0xFFF0559B),
    text = Color(0xFFF2F6FC),
    muted = Color(0xFF8C9EB5),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    isLight = false
)

private val AmoledPalette = basePalette(
    bg = Color(0xFF000000),
    card = Color(0xFF0A0A0A),
    surface = Color(0xFF070707),
    elevated = Color(0xFF111111),
    chip = Color(0xFF141414),
    border = Color(0xFF222222),
    primary = Color(0xFF3B82F6),
    accent = Color(0xFF3B82F6),
    text = Color(0xFFFFFFFF),
    muted = Color(0xFF9CA3AF),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    isLight = false
)

private val GlassPalette = basePalette(
    bg = Color(0xFF0B1220),
    card = Color(0x99151E2D),
    surface = Color(0xCC111826),
    elevated = Color(0xF21A2434),
    chip = Color(0x661C2739),
    border = Color(0x1FFFFFFF),
    primary = Color(0xFF3B82F6),
    accent = Color(0xFF3B82F6),
    text = Color(0xFFF8FAFC),
    muted = Color(0xFFA5B4C8),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    isLight = false,
    gradient = listOf(Color(0xFF1E3A8A), Color(0xFF0B1220), Color(0xFF4C1D3D)),
    isGlass = true
)

private val LiquidGlassPalette = basePalette(
    bg = Color(0xFF070C16),
    card = Color(0x8C1A2436),
    surface = Color(0xD90D1420),
    elevated = Color(0xF21A2434),
    chip = Color(0x521C2739),
    border = Color(0x2EFFFFFF),
    primary = Color(0xFF4F8DF7),
    accent = Color(0xFFF0559B),
    text = Color(0xFFF6F9FF),
    muted = Color(0xFFADC0D8),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    isLight = false,
    gradient = listOf(Color(0xFF1D4ED8), Color(0xFF0B1020), Color(0xFF7C2A64)),
    isGlass = true
)

private val LightPalette = basePalette(
    bg = Color(0xFFF7F9FC),
    card = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    elevated = Color(0xFFFFFFFF),
    chip = Color(0xFFEEF2F7),
    border = Color(0xFFE3E9F2),
    primary = Color(0xFF2563EB),
    accent = Color(0xFF3B82F6),
    text = Color(0xFF0F172A),
    muted = Color(0xFF64748B),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
    isLight = true
)

/** Resolves the concrete palette for a theme, including follow-system and custom. */
fun resolvePalette(
    theme: AppTheme,
    systemDark: Boolean = false,
    customAccent: Color = SerieColors.Blue500,
    customGradient: Boolean = false
): SeriePalette = when (theme) {
    AppTheme.FollowSystem -> if (systemDark) DarkPalette else LightPalette
    AppTheme.Dark -> DarkPalette
    AppTheme.Amoled -> AmoledPalette
    AppTheme.Glass -> GlassPalette
    AppTheme.LiquidGlass -> LiquidGlassPalette
    AppTheme.Light -> LightPalette
    AppTheme.Custom -> DarkPalette.copy(
        primary = customAccent,
        accent = customAccent,
        accentSoft = customAccent.copy(alpha = 0.16f),
        onAccent = if (customAccent.luminance() > 0.6f) Color(0xFF0F172A) else Color.White,
        gradient = if (customGradient) {
            listOf(customAccent.copy(alpha = 0.42f), DarkPalette.bg, Color(0xFF070B12))
        } else null
    )
}

/** Preview palette for the theme picker swatches. */
fun previewPalette(theme: AppTheme, systemDark: Boolean): SeriePalette =
    resolvePalette(theme, systemDark = systemDark)

@Deprecated("Use resolvePalette", ReplaceWith("resolvePalette(theme)"))
fun paletteFor(theme: AppTheme): SeriePalette = resolvePalette(theme)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

@Composable
fun SerieeTheme(
    theme: AppTheme,
    customAccent: Color = SerieColors.Blue500,
    customGradient: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val p = resolvePalette(theme, systemDark, customAccent, customGradient)

    val scheme = if (p.isLight) {
        lightColorScheme(
            primary = p.primary,
            secondary = p.accent,
            background = p.bg,
            surface = p.surface,
            surfaceVariant = p.chip,
            onSurface = p.text,
            onBackground = p.text,
            outline = p.border,
            surfaceContainer = p.surface,
            surfaceContainerHigh = p.surface,
            surfaceContainerHighest = p.surface
        )
    } else {
        darkColorScheme(
            primary = p.primary,
            secondary = p.accent,
            background = p.bg,
            surface = p.surface,
            surfaceVariant = p.chip,
            onSurface = p.text,
            onBackground = p.text,
            outline = p.border,
            surfaceContainer = p.surface,
            surfaceContainerHigh = p.surface,
            surfaceContainerHighest = p.surface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = p.isLight
            controller.isAppearanceLightNavigationBars = p.isLight
        }
    }

    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
