package com.seriee.app.ui

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.seriee.app.data.AppThemeMode
import com.seriee.app.data.AppThemeStyle
import com.seriee.app.data.CustomThemeConfig
import com.seriee.app.data.WatchStatus

@Immutable
data class SerieePalette(
    val isLight: Boolean,
    val isGlass: Boolean,
    val isNeoBrutalist: Boolean,
    val bg: Color,
    val bgGradStart: Color,
    val bgGradEnd: Color,
    val useGradient: Boolean,
    val surface: Color,
    val card: Color,
    val cardAlt: Color,
    val chip: Color,
    val primary: Color,
    val primaryContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val reminder: Color,
) {
    val backgroundBrush: Brush
        get() = if (useGradient) {
            Brush.verticalGradient(listOf(bgGradStart, bgGradEnd))
        } else {
            Brush.verticalGradient(listOf(bg, bg))
        }
}

val LocalPalette = staticCompositionLocalOf {
    buildPalette(
        isDark = false,
        style = AppThemeStyle.NeoBrutalism,
        custom = CustomThemeConfig(0xFFFFD60AL, 0xFFFFF8E7L, 0xFFFFE5B4L, false),
        reduceTransparency = false,
        materialAccent = null,
    )
}

private fun contrastOn(c: Color): Color = if (c.luminance() > 0.45f) Color(0xFF121212) else Color(0xFFFFFFFF)

private fun buildPalette(
    isDark: Boolean,
    style: AppThemeStyle,
    custom: CustomThemeConfig,
    reduceTransparency: Boolean,
    materialAccent: Pair<Color, Color>?,
): SerieePalette {
    val isLight = !isDark
    return when (style) {
        AppThemeStyle.NeoBrutalism -> if (isLight) {
            SerieePalette(
                isLight = true,
                isGlass = false,
                isNeoBrutalist = true,
                bg = NeoBrutalistColors.Cream,
                bgGradStart = NeoBrutalistColors.Cream,
                bgGradEnd = NeoBrutalistColors.Cream,
                useGradient = false,
                surface = NeoBrutalistColors.CardWhite,
                card = NeoBrutalistColors.CardWhite,
                cardAlt = Color(0xFFFFF1C1),
                chip = Color(0xFFF3ECE0),
                primary = NeoBrutalistColors.SunYellow,
                primaryContainer = Color(0xFFFFF099),
                accent = NeoBrutalistColors.SunYellow,
                onAccent = NeoBrutalistColors.Black,
                text = NeoBrutalistColors.Black,
                muted = NeoBrutalistColors.MutedGray,
                border = NeoBrutalistColors.Black,
                success = NeoBrutalistColors.MintGreen,
                danger = NeoBrutalistColors.CoralPink,
                warning = NeoBrutalistColors.WarmOrange,
                reminder = NeoBrutalistColors.VividPurple,
            )
        } else {
            SerieePalette(
                isLight = false,
                isGlass = false,
                isNeoBrutalist = true,
                bg = NeoBrutalistColors.DarkBg,
                bgGradStart = NeoBrutalistColors.DarkBg,
                bgGradEnd = NeoBrutalistColors.DarkBg,
                useGradient = false,
                surface = NeoBrutalistColors.DarkCard,
                card = NeoBrutalistColors.DarkCard,
                cardAlt = NeoBrutalistColors.DarkCardAlt,
                chip = Color(0xFF2D2D38),
                primary = NeoBrutalistColors.SunYellow,
                primaryContainer = Color(0xFF4B3F00),
                accent = NeoBrutalistColors.SunYellow,
                onAccent = NeoBrutalistColors.Black,
                text = NeoBrutalistColors.DarkText,
                muted = NeoBrutalistColors.DarkMuted,
                border = NeoBrutalistColors.DarkBorder,
                success = NeoBrutalistColors.MintGreen,
                danger = NeoBrutalistColors.CoralPink,
                warning = NeoBrutalistColors.WarmOrange,
                reminder = Color(0xFFB388FF),
            )
        }

        AppThemeStyle.Classic -> if (isLight) {
            SerieePalette(
                isLight = true, isGlass = false, isNeoBrutalist = false,
                bg = Color(0xFFF4F6FB), bgGradStart = Color(0xFFF4F6FB), bgGradEnd = Color(0xFFF4F6FB), useGradient = false,
                surface = Color.White, card = Color.White, cardAlt = Color(0xFFEAF0FA), chip = Color(0xFFE4E9F2),
                primary = Color(0xFF2563EB), primaryContainer = Color(0xFFDBEAFE), accent = Color(0xFF2563EB), onAccent = Color.White,
                text = Color(0xFF0F172A), muted = Color(0xFF64748B), border = Color(0xFFCBD5E1),
                success = Color(0xFF10B981), danger = Color(0xFFEF4444), warning = Color(0xFFF59E0B), reminder = Color(0xFF8B5CF6),
            )
        } else {
            SerieePalette(
                isLight = false, isGlass = false, isNeoBrutalist = false,
                bg = Color(0xFF0F1420), bgGradStart = Color(0xFF0F1420), bgGradEnd = Color(0xFF0F1420), useGradient = false,
                surface = Color(0xFF182032), card = Color(0xFF1B2438), cardAlt = Color(0xFF232E47), chip = Color(0xFF28344F),
                primary = Color(0xFF60A5FA), primaryContainer = Color(0xFF1E3A8A), accent = Color(0xFF60A5FA), onAccent = Color(0xFF091324),
                text = Color(0xFFF1F5F9), muted = Color(0xFF94A3B8), border = Color(0xFF334155),
                success = Color(0xFF34D399), danger = Color(0xFFF87171), warning = Color(0xFFFBBF24), reminder = Color(0xFFA78BFA),
            )
        }

        AppThemeStyle.Amoled -> SerieePalette(
            isLight = false, isGlass = false, isNeoBrutalist = false,
            bg = Color(0xFF000000), bgGradStart = Color(0xFF000000), bgGradEnd = Color(0xFF000000), useGradient = false,
            surface = Color(0xFF0B0B0D), card = Color(0xFF101014), cardAlt = Color(0xFF18181D), chip = Color(0xFF1F1F26),
            primary = Color(0xFFFFFFFF), primaryContainer = Color(0xFF2A2A32), accent = Color(0xFFE4E4E7), onAccent = Color(0xFF000000),
            text = Color(0xFFFAFAFA), muted = Color(0xFFA1A1AA), border = Color(0xFF27272A),
            success = Color(0xFF22C55E), danger = Color(0xFFEF4444), warning = Color(0xFFF59E0B), reminder = Color(0xFFA855F7),
        )

        AppThemeStyle.Frost -> {
            val alphaCard = if (reduceTransparency) 1f else 0.78f
            val alphaAlt = if (reduceTransparency) 1f else 0.88f
            if (isLight) {
                SerieePalette(
                    isLight = true, isGlass = !reduceTransparency, isNeoBrutalist = false,
                    bg = Color(0xFFE8EEF5), bgGradStart = Color(0xFFEBF0F7), bgGradEnd = Color(0xFFDCE5F0), useGradient = true,
                    surface = Color.White.copy(alpha = alphaAlt), card = Color.White.copy(alpha = alphaCard),
                    cardAlt = Color(0xFFF1F5F9).copy(alpha = alphaAlt), chip = Color(0xFFE2E8F0).copy(alpha = alphaAlt),
                    primary = Color(0xFF0EA5E9), primaryContainer = Color(0xFFE0F2FE), accent = Color(0xFF0EA5E9), onAccent = Color.White,
                    text = Color(0xFF0F172A), muted = Color(0xFF475569), border = Color.White.copy(alpha = 0.85f),
                    success = Color(0xFF10B981), danger = Color(0xFFF43F5E), warning = Color(0xFFF59E0B), reminder = Color(0xFF8B5CF6),
                )
            } else {
                SerieePalette(
                    isLight = false, isGlass = !reduceTransparency, isNeoBrutalist = false,
                    bg = Color(0xFF0E131B), bgGradStart = Color(0xFF0F172A), bgGradEnd = Color(0xFF161E2E), useGradient = true,
                    surface = Color(0xFF182234).copy(alpha = alphaAlt), card = Color(0xFF1E293B).copy(alpha = alphaCard),
                    cardAlt = Color(0xFF263349).copy(alpha = alphaAlt), chip = Color(0xFF2C3A52).copy(alpha = alphaAlt),
                    primary = Color(0xFF38BDF8), primaryContainer = Color(0xFF0C4A6E), accent = Color(0xFF38BDF8), onAccent = Color(0xFF071B26),
                    text = Color(0xFFF8FAFC), muted = Color(0xFF94A3B8), border = Color.White.copy(alpha = 0.14f),
                    success = Color(0xFF34D399), danger = Color(0xFFFB7185), warning = Color(0xFFFBBF24), reminder = Color(0xFFA78BFA),
                )
            }
        }

        AppThemeStyle.Liquid -> {
            val alphaCard = if (reduceTransparency) 1f else 0.72f
            val alphaAlt = if (reduceTransparency) 1f else 0.84f
            if (isLight) {
                SerieePalette(
                    isLight = true, isGlass = !reduceTransparency, isNeoBrutalist = false,
                    bg = Color(0xFFF2ECFE), bgGradStart = Color(0xFFE6EEFF), bgGradEnd = Color(0xFFFBE8F6), useGradient = true,
                    surface = Color.White.copy(alpha = alphaAlt), card = Color.White.copy(alpha = alphaCard),
                    cardAlt = Color(0xFFF5F3FF).copy(alpha = alphaAlt), chip = Color(0xFFEDE9FE).copy(alpha = alphaAlt),
                    primary = Color(0xFF7C3AED), primaryContainer = Color(0xFFEDE9FE), accent = Color(0xFF7C3AED), onAccent = Color.White,
                    text = Color(0xFF1E1B4B), muted = Color(0xFF5B557D), border = Color.White.copy(alpha = 0.9f),
                    success = Color(0xFF10B981), danger = Color(0xFFEC4899), warning = Color(0xFFF59E0B), reminder = Color(0xFF6366F1),
                )
            } else {
                SerieePalette(
                    isLight = false, isGlass = !reduceTransparency, isNeoBrutalist = false,
                    bg = Color(0xFF100B22), bgGradStart = Color(0xFF110C28), bgGradEnd = Color(0xFF1E1136), useGradient = true,
                    surface = Color(0xFF1E1638).copy(alpha = alphaAlt), card = Color(0xFF231A42).copy(alpha = alphaCard),
                    cardAlt = Color(0xFF2D2252).copy(alpha = alphaAlt), chip = Color(0xFF33275C).copy(alpha = alphaAlt),
                    primary = Color(0xFFC084FC), primaryContainer = Color(0xFF4C1D95), accent = Color(0xFFC084FC), onAccent = Color(0xFF1B0930),
                    text = Color(0xFFF5F3FF), muted = Color(0xFFB7AED1), border = Color.White.copy(alpha = 0.16f),
                    success = Color(0xFF34D399), danger = Color(0xFFF472B6), warning = Color(0xFFFBBF24), reminder = Color(0xFFA5B4FC),
                )
            }
        }

        AppThemeStyle.MaterialYou -> {
            val acc = materialAccent?.first ?: Color(0xFF6750A4)
            val container = materialAccent?.second ?: if (isLight) Color(0xFFEADDFF) else Color(0xFF4F378B)
            if (isLight) {
                SerieePalette(
                    isLight = true, isGlass = false, isNeoBrutalist = false,
                    bg = Color(0xFFFEF7FF), bgGradStart = Color(0xFFFEF7FF), bgGradEnd = Color(0xFFFEF7FF), useGradient = false,
                    surface = Color(0xFFF7F2FA), card = Color(0xFFFFFFFF), cardAlt = Color(0xFFF3EDF7), chip = Color(0xFFE8DEF8),
                    primary = acc, primaryContainer = container, accent = acc, onAccent = contrastOn(acc),
                    text = Color(0xFF1D1B20), muted = Color(0xFF49454F), border = Color(0xFFCAC4D0),
                    success = Color(0xFF16A34A), danger = Color(0xFFDC2626), warning = Color(0xFFD97706), reminder = Color(0xFF7C3AED),
                )
            } else {
                SerieePalette(
                    isLight = false, isGlass = false, isNeoBrutalist = false,
                    bg = Color(0xFF141218), bgGradStart = Color(0xFF141218), bgGradEnd = Color(0xFF141218), useGradient = false,
                    surface = Color(0xFF1D1B20), card = Color(0xFF211F26), cardAlt = Color(0xFF2B2930), chip = Color(0xFF36343B),
                    primary = acc, primaryContainer = container, accent = acc, onAccent = contrastOn(acc),
                    text = Color(0xFFE6E0E9), muted = Color(0xFFCAC4D0), border = Color(0xFF49454F),
                    success = Color(0xFF4ADE80), danger = Color(0xFFF87171), warning = Color(0xFFFBBF24), reminder = Color(0xFFC4B5FD),
                )
            }
        }

        AppThemeStyle.Custom -> {
            val acc = Color(custom.accentArgb)
            val s = Color(custom.bgStartArgb)
            val e = Color(custom.bgEndArgb)
            val lightBg = s.luminance() > 0.45f
            val cardColor = if (lightBg) Color.White.copy(alpha = if (reduceTransparency) 1f else 0.90f) else Color(0xFF1A1B24).copy(alpha = if (reduceTransparency) 1f else 0.88f)
            val altColor = if (lightBg) Color(0xFFF1F5F9) else Color(0xFF242634)
            SerieePalette(
                isLight = lightBg,
                isGlass = custom.useGradient && !reduceTransparency,
                isNeoBrutalist = false,
                bg = s,
                bgGradStart = s,
                bgGradEnd = e,
                useGradient = custom.useGradient,
                surface = cardColor,
                card = cardColor,
                cardAlt = altColor,
                chip = altColor,
                primary = acc,
                primaryContainer = acc.copy(alpha = 0.22f),
                accent = acc,
                onAccent = contrastOn(acc),
                text = if (lightBg) Color(0xFF111827) else Color(0xFFF8FAFC),
                muted = if (lightBg) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                border = if (lightBg) Color(0xFFCBD5E1) else Color(0xFF374151),
                success = Color(0xFF10B981),
                danger = Color(0xFFEF4444),
                warning = Color(0xFFF59E0B),
                reminder = acc,
            )
        }
    }
}

fun statusColor(status: WatchStatus, palette: SerieePalette): Color = when (status) {
    WatchStatus.Watching -> if (palette.isNeoBrutalist) NeoBrutalistColors.ElectricBlue else palette.accent
    WatchStatus.Completed -> palette.success
    WatchStatus.PlanToWatch -> if (palette.isNeoBrutalist) NeoBrutalistColors.VividPurple else Color(0xFF8B5CF6)
    WatchStatus.ComingSoon -> palette.warning
    WatchStatus.OnHold -> Color(0xFFEA580C)
    WatchStatus.Dropped -> palette.danger
}

@Composable
fun SerieeTheme(
    mode: AppThemeMode,
    style: AppThemeStyle,
    custom: CustomThemeConfig,
    reduceTransparency: Boolean,
    batteryAmoled: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val isDark = when (mode) {
        AppThemeMode.System -> systemDark
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val powerSaver = if (batteryAmoled) {
        runCatching {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isPowerSaveMode == true
        }.getOrDefault(false)
    } else false

    val effectiveStyle = if (powerSaver) AppThemeStyle.Amoled else style
    val effectiveDark = if (effectiveStyle == AppThemeStyle.Amoled) true else isDark

    val materialAccent = if (effectiveStyle == AppThemeStyle.MaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dyn = if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dyn.primary to dyn.primaryContainer
    } else null

    val palette = buildPalette(
        isDark = effectiveDark,
        style = effectiveStyle,
        custom = custom,
        reduceTransparency = reduceTransparency,
        materialAccent = materialAccent,
    )

    val m3Scheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.primaryContainer,
            background = palette.bg,
            surface = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
            error = palette.danger,
        )
    } else {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            primaryContainer = palette.primaryContainer,
            background = palette.bg,
            surface = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
            error = palette.danger,
        )
    }

    val typography = Typography().let { base ->
        base.copy(
            titleLarge = base.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }

    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(colorScheme = m3Scheme, typography = typography, content = content)
    }
}
