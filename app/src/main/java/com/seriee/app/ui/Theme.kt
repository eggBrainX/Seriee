package com.seriee.app.ui

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
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
}

@Immutable
data class SeriePalette(
    val bg: Color,
    val card: Color,
    val surface: Color,
    val chip: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val isLight: Boolean
)

private val DarkPalette = SeriePalette(
    bg = Color(0xFF0B0F17), card = Color(0xFF151C28), surface = Color(0xFF151C28),
    chip = Color(0xFF1E2738), border = Color(0xFF232D3F),
    text = Color(0xFFF1F5F9), muted = Color(0xFF8899A6), isLight = false
)
private val AmoledPalette = SeriePalette(
    bg = Color(0xFF000000), card = Color(0xFF0A0A0A), surface = Color(0xFF0A0A0A),
    chip = Color(0xFF141414), border = Color(0xFF222222),
    text = Color(0xFFFFFFFF), muted = Color(0xFF888888), isLight = false
)
private val GlassPalette = SeriePalette(
    bg = Color(0xFF0F172A), card = Color(0x991E293B), surface = Color(0xFF1E293B),
    chip = Color(0xFF334155), border = Color(0x1AFFFFFF),
    text = Color(0xFFF8FAFC), muted = Color(0xFF94A3B8), isLight = false
)
private val LightPalette = SeriePalette(
    bg = Color(0xFFF8FAFC), card = Color(0xFFFFFFFF), surface = Color(0xFFFFFFFF),
    chip = Color(0xFFF1F5F9), border = Color(0xFFE2E8F0),
    text = Color(0xFF0F172A), muted = Color(0xFF64748B), isLight = true
)

fun paletteFor(theme: AppTheme): SeriePalette = when (theme) {
    AppTheme.Dark -> DarkPalette
    AppTheme.Amoled -> AmoledPalette
    AppTheme.Glass -> GlassPalette
    AppTheme.Light -> LightPalette
}

val LocalPalette = staticCompositionLocalOf { DarkPalette }

@Composable
fun SerieeTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val p = paletteFor(theme)

    val scheme = if (p.isLight) {
        lightColorScheme(
            primary = SerieColors.Blue600,
            background = p.bg,
            surface = p.surface,
            onSurface = p.text,
            onBackground = p.text,
            surfaceContainer = p.surface,
            surfaceContainerHigh = p.surface,
            surfaceContainerHighest = p.surface
        )
    } else {
        darkColorScheme(
            primary = SerieColors.Blue500,
            background = p.bg,
            surface = p.surface,
            onSurface = p.text,
            onBackground = p.text,
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
