package com.seriee.app.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.PowerManager
import java.util.Locale
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.seriee.app.data.AppThemeMode
import com.seriee.app.data.AppThemeStyle

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
 * [accent] mirrors [primary] for every built-in style so the wordmark, badges,
 * selection tints and gradients stay in sync. It only diverges for
 * [AppThemeStyle.Custom], where the user picks their own accent.
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
    /** Informational blue, used for badges that are not the primary CTA. */
    val info: Color,
    /** Reminder / release-date orange. */
    val reminder: Color,
    /** Rating stars and highlights. */
    val star: Color,
    val isLight: Boolean,
    val gradient: List<Color>? = null,
    val isGlass: Boolean = false,
    /** Liquid Glass style: enables optical lens refraction & chromatic dispersion. */
    val isLiquidGlass: Boolean = false,
    /** Liquid style: the background gradient slowly drifts. */
    val animatedGradient: Boolean = false,
    /** Neo-Brutalism style: crisp borders, hard offset shadows, tactile press physics. */
    val isNeoBrutalist: Boolean = false
)

/** User-controlled colours for [AppThemeStyle.Custom]. */
@Immutable
data class CustomThemeColors(
    val accent: Color = SerieColors.Blue500,
    val backgroundStart: Color = Color(0xFF1D4ED8),
    val backgroundEnd: Color = Color(0xFF7C2A64),
    val gradient: Boolean = false
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
    info: Color = primary,
    reminder: Color = warning,
    star: Color = warning,
    gradient: List<Color>? = null,
    isGlass: Boolean = false,
    isLiquidGlass: Boolean = false,
    animatedGradient: Boolean = false,
    isNeoBrutalist: Boolean = false
) = SeriePalette(
    bg = bg,
    card = card,
    surface = surface,
    elevated = elevated,
    chip = chip,
    border = border,
    primary = primary,
    accent = accent,
    accentSoft = accent.copy(alpha = if (isNeoBrutalist) 1f else 0.16f),
    onAccent = if (isNeoBrutalist) neoContrastOn(accent) else contrastOn(accent),
    text = text,
    muted = muted,
    success = success,
    warning = warning,
    danger = danger,
    info = info,
    reminder = reminder,
    star = star,
    isLight = isLight,
    gradient = gradient,
    isGlass = isGlass,
    isLiquidGlass = isLiquidGlass,
    animatedGradient = animatedGradient,
    isNeoBrutalist = isNeoBrutalist
)

// ------------------------------------------------------------------ palettes

private val ClassicDark = basePalette(
    bg = Color(0xFF0A0F18),
    card = Color(0xFF151E2D),
    surface = Color(0xFF111826),
    elevated = Color(0xFF1A2434),
    chip = Color(0xFF1C2739),
    border = Color(0xFF233047),
    primary = Color(0xFF3B82F6),
    accent = Color(0xFF3B82F6),
    text = Color(0xFFF2F6FC),
    muted = Color(0xFF8C9EB5),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    info = Color(0xFF60A5FA),
    reminder = Color(0xFFFB923C),
    star = Color(0xFFFBBF24),
    isLight = false
)

private val ClassicLight = basePalette(
    bg = Color(0xFFF7F9FC),
    card = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    elevated = Color(0xFFFFFFFF),
    chip = Color(0xFFEEF2F7),
    border = Color(0xFFE3E9F2),
    primary = Color(0xFF2563EB),
    accent = Color(0xFF2563EB),
    text = Color(0xFF0F172A),
    muted = Color(0xFF64748B),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
    info = Color(0xFF2563EB),
    reminder = Color(0xFFEA580C),
    star = Color(0xFFD97706),
    isLight = true
)

private val AmoledDark = basePalette(
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
    info = Color(0xFF60A5FA),
    reminder = Color(0xFFFB923C),
    star = Color(0xFFFBBF24),
    isLight = false
)

private val AmoledLight = basePalette(
    bg = Color(0xFFFFFFFF),
    card = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    elevated = Color(0xFFFFFFFF),
    chip = Color(0xFFF2F2F2),
    border = Color(0xFFD4D4D4),
    primary = Color(0xFF1D4ED8),
    accent = Color(0xFF1D4ED8),
    text = Color(0xFF000000),
    muted = Color(0xFF5B5B5B),
    success = Color(0xFF047857),
    warning = Color(0xFFB45309),
    danger = Color(0xFFB91C1C),
    info = Color(0xFF1D4ED8),
    reminder = Color(0xFFC2410C),
    star = Color(0xFFB45309),
    isLight = true
)

private val FrostDark = basePalette(
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
    info = Color(0xFF7DD3FC),
    reminder = Color(0xFFFDBA74),
    star = Color(0xFFFCD34D),
    isLight = false,
    gradient = listOf(Color(0xFF1E3A8A), Color(0xFF0B1220), Color(0xFF4C1D3D)),
    isGlass = true
)

private val FrostLight = basePalette(
    bg = Color(0xFFEAF1FA),
    card = Color(0xB3FFFFFF),
    surface = Color(0xCCFFFFFF),
    elevated = Color(0xF2FFFFFF),
    chip = Color(0x8CE8EEF7),
    border = Color(0xFFC7D4E6),
    primary = Color(0xFF2563EB),
    accent = Color(0xFF2563EB),
    text = Color(0xFF0F172A),
    muted = Color(0xFF52627A),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
    info = Color(0xFF0369A1),
    reminder = Color(0xFFEA580C),
    star = Color(0xFFD97706),
    isLight = true,
    gradient = listOf(Color(0xFFDBEAFE), Color(0xFFF5F8FD), Color(0xFFFCE7F3)),
    isGlass = true
)

private val LiquidDark = basePalette(
    bg = Color(0xFF050914),
    card = Color(0x521E293B),
    surface = Color(0xCC0D1526),
    elevated = Color(0xD9172238),
    chip = Color(0x47FFFFFF),
    border = Color(0x3DFFFFFF),
    primary = Color(0xFF0A84FF),
    accent = Color(0xFF38BDF8),
    text = Color(0xFFF8FAFF),
    muted = Color(0xFFC2D0E6),
    success = Color(0xFF30D158),
    warning = Color(0xFFFFD60A),
    danger = Color(0xFFFF453A),
    info = Color(0xFF64D2FF),
    reminder = Color(0xFFFF9F0A),
    star = Color(0xFFFFD60A),
    isLight = false,
    gradient = listOf(Color(0xFF0F3D91), Color(0xFF070D1D), Color(0xFF155E75), Color(0xFF6B1D5C)),
    isGlass = true,
    isLiquidGlass = true,
    animatedGradient = true
)

private val LiquidLight = basePalette(
    bg = Color(0xFFEAF2FF),
    card = Color(0x88FFFFFF),
    surface = Color(0xD9F8FBFF),
    elevated = Color(0xEBFFFFFF),
    chip = Color(0x73FFFFFF),
    border = Color(0xB8FFFFFF),
    primary = Color(0xFF007AFF),
    accent = Color(0xFF0EA5E9),
    text = Color(0xFF0A1220),
    muted = Color(0xFF475569),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFFF3B30),
    info = Color(0xFF0284C7),
    reminder = Color(0xFFFF6B00),
    star = Color(0xFFF59E0B),
    isLight = true,
    gradient = listOf(Color(0xFFBAE6FD), Color(0xFFF5F8FF), Color(0xFFE0E7FF), Color(0xFFFBCFE8)),
    isGlass = true,
    isLiquidGlass = true,
    animatedGradient = true
)

private val NeoBrutalistLight = basePalette(
    bg = NeoBrutalistColors.Cream,
    card = NeoBrutalistColors.PureWhite,
    surface = NeoBrutalistColors.PureWhite,
    elevated = NeoBrutalistColors.OffWhite,
    chip = NeoBrutalistColors.OffWhite,
    border = NeoBrutalistColors.Black,
    primary = NeoBrutalistColors.Yellow,
    accent = NeoBrutalistColors.Pink,
    text = NeoBrutalistColors.Black,
    muted = NeoBrutalistColors.LightMuted,
    success = NeoBrutalistColors.Mint,
    warning = NeoBrutalistColors.Yellow,
    danger = NeoBrutalistColors.CoralRed,
    info = NeoBrutalistColors.SkyBlue,
    reminder = NeoBrutalistColors.Pink,
    star = NeoBrutalistColors.Yellow,
    isLight = true,
    isNeoBrutalist = true
)

private val NeoBrutalistDark = basePalette(
    bg = NeoBrutalistColors.DarkBackground,
    card = NeoBrutalistColors.DarkSurface,
    surface = NeoBrutalistColors.DarkSurface,
    elevated = NeoBrutalistColors.DarkElevated,
    chip = NeoBrutalistColors.DarkElevated,
    border = NeoBrutalistColors.Black,
    primary = NeoBrutalistColors.Yellow,
    accent = NeoBrutalistColors.Pink,
    text = NeoBrutalistColors.Cream,
    muted = NeoBrutalistColors.DarkMuted,
    success = NeoBrutalistColors.Mint,
    warning = NeoBrutalistColors.Yellow,
    danger = NeoBrutalistColors.CoralRed,
    info = NeoBrutalistColors.SkyBlue,
    reminder = NeoBrutalistColors.Pink,
    star = NeoBrutalistColors.Yellow,
    isLight = false,
    isNeoBrutalist = true
)

// ------------------------------------------------------------------ resolving

/** True when a palette would use translucent surfaces. */
private fun SeriePalette.isTranslucentStyle(): Boolean =
    isGlass || card.alpha < 0.999f || surface.alpha < 0.999f || chip.alpha < 0.999f

/** Accessibility: flatten every translucent surface onto [bg]. */
fun SeriePalette.opaque(): SeriePalette {
    if (!isTranslucentStyle()) return this
    return copy(
        card = card.compositeOver(bg),
        surface = surface.compositeOver(bg),
        elevated = elevated.compositeOver(bg),
        chip = chip.compositeOver(bg),
        border = border.copy(alpha = 1f).compositeOver(bg),
        isGlass = false,
        isLiquidGlass = false
    )
}

/** WCAG relative-contrast text colour for a filled surface. */
fun contrastOn(background: Color): Color =
    if (background.luminance() > 0.55f) Color(0xFF0F172A) else Color.White

/** Neo-Brutalist contrast: pure black on all bright & pastel pop surfaces. */
fun neoContrastOn(background: Color): Color =
    if (background.luminance() > 0.25f) NeoBrutalistColors.Black else Color.White

/** WCAG contrast ratio between two opaque colours (1..21). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    val lighter = maxOf(la, lb)
    val darker = minOf(la, lb)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** Minimum 3:1 keeps accents readable as UI colours. */
fun SeriePalette.hasReadableAccent(): Boolean =
    contrastRatio(accent, bg) >= 3f

/** Resolves the concrete palette for a mode/style pair. */
fun resolvePalette(
    mode: AppThemeMode,
    style: AppThemeStyle,
    systemDark: Boolean = false,
    custom: CustomThemeColors = CustomThemeColors(),
    dynamicScheme: ColorScheme? = null,
    reduceTransparency: Boolean = false
): SeriePalette {
    val dark = when (mode) {
        AppThemeMode.System -> systemDark
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val resolved = when (style) {
        AppThemeStyle.Classic -> if (dark) ClassicDark else ClassicLight
        AppThemeStyle.Amoled -> if (dark) AmoledDark else AmoledLight
        AppThemeStyle.Frost -> if (dark) FrostDark else FrostLight
        AppThemeStyle.Liquid -> if (dark) LiquidDark else LiquidLight
        AppThemeStyle.MaterialYou ->
            dynamicScheme?.let { paletteFromScheme(it) }
                ?: (if (dark) ClassicDark else ClassicLight)
        AppThemeStyle.Custom -> {
            val base = if (dark) ClassicDark else ClassicLight
            base.copy(
                primary = custom.accent,
                accent = custom.accent,
                accentSoft = custom.accent.copy(alpha = 0.16f),
                onAccent = contrastOn(custom.accent),
                gradient = if (custom.gradient) {
                    listOf(custom.backgroundStart, custom.backgroundEnd)
                } else null
            )
        }
        AppThemeStyle.NeoBrutalism -> if (dark) NeoBrutalistDark else NeoBrutalistLight
    }
    return if (reduceTransparency) resolved.opaque() else resolved
}

/** Preview palette for the theme picker swatches. */
fun previewPalette(
    mode: AppThemeMode,
    style: AppThemeStyle,
    systemDark: Boolean,
    custom: CustomThemeColors = CustomThemeColors(),
    dynamicScheme: ColorScheme? = null
): SeriePalette = resolvePalette(mode, style, systemDark, custom, dynamicScheme)

// ------------------------------------------------------------- Material You

/** Builds Seriee tokens from a wallpaper-derived Material 3 scheme. */
fun paletteFromScheme(scheme: ColorScheme): SeriePalette {
    val dark = scheme.background.luminance() < 0.45f
    return SeriePalette(
        bg = scheme.background,
        card = scheme.surfaceContainer,
        surface = scheme.surface,
        elevated = scheme.surfaceContainerHigh,
        chip = scheme.surfaceContainerHighest,
        border = scheme.outlineVariant,
        primary = scheme.primary,
        accent = scheme.primary,
        accentSoft = scheme.primary.copy(alpha = 0.16f),
        onAccent = contrastOn(scheme.primary),
        text = scheme.onBackground,
        muted = scheme.onSurfaceVariant,
        success = if (dark) Color(0xFF34D399) else Color(0xFF059669),
        warning = if (dark) Color(0xFFFBBF24) else Color(0xFFD97706),
        danger = scheme.error,
        info = scheme.primary,
        reminder = if (dark) Color(0xFFFB923C) else Color(0xFFEA580C),
        star = if (dark) Color(0xFFFBBF24) else Color(0xFFD97706),
        isLight = !dark
    )
}

/**
 * Dynamic colour scheme for [AppThemeStyle.MaterialYou]. Returns null on
 * Android 11 and below, where the picker falls back to Classic colours.
 */
@Composable
fun rememberMaterialYouScheme(enabled: Boolean, dark: Boolean): ColorScheme? {
    if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return remember(context, dark) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
}

// ------------------------------------------------------------- Material theme

private fun Color.onto(base: Color, alpha: Float): Color =
    copy(alpha = alpha).compositeOver(base)

/** Complete Material 3 scheme so stock components never fall back to purple. */
private fun schemeFor(p: SeriePalette): ColorScheme {
    if (p.isNeoBrutalist) {
        return (if (p.isLight) NeoBrutalistLightColorScheme else NeoBrutalistDarkColorScheme)
            .toMaterialColorScheme()
    }
    val onPrimary = contrastOn(p.primary)
    val onDanger = contrastOn(p.danger)
    val inverseSurface = if (p.isLight) Color(0xFF0F172A) else Color(0xFFF2F6FC)
    val inverseOnSurface = contrastOn(inverseSurface)
    return if (p.isLight) {
        lightColorScheme(
            primary = p.primary,
            onPrimary = onPrimary,
            primaryContainer = p.primary.onto(p.surface, 0.14f),
            onPrimaryContainer = p.text,
            inversePrimary = p.primary.onto(p.bg, 0.7f),
            secondary = p.accent,
            onSecondary = p.onAccent,
            secondaryContainer = p.accent.onto(p.surface, 0.14f),
            onSecondaryContainer = p.text,
            tertiary = p.info,
            onTertiary = contrastOn(p.info),
            tertiaryContainer = p.info.onto(p.surface, 0.14f),
            onTertiaryContainer = p.text,
            background = p.bg,
            onBackground = p.text,
            surface = p.surface,
            onSurface = p.text,
            surfaceVariant = p.chip,
            onSurfaceVariant = p.muted,
            surfaceTint = p.primary,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            error = p.danger,
            onError = onDanger,
            errorContainer = p.danger.onto(p.surface, 0.14f),
            onErrorContainer = p.danger,
            outline = p.border,
            outlineVariant = p.border.copy(alpha = 0.6f).compositeOver(p.bg),
            scrim = Color.Black,
            surfaceBright = p.elevated,
            surfaceDim = p.bg,
            surfaceContainerLowest = p.bg,
            surfaceContainerLow = p.surface,
            surfaceContainer = p.card,
            surfaceContainerHigh = p.elevated,
            surfaceContainerHighest = p.chip
        )
    } else {
        darkColorScheme(
            primary = p.primary,
            onPrimary = onPrimary,
            primaryContainer = p.primary.onto(p.bg, 0.22f),
            onPrimaryContainer = p.text,
            inversePrimary = p.primary.onto(p.bg, 0.6f),
            secondary = p.accent,
            onSecondary = p.onAccent,
            secondaryContainer = p.accent.onto(p.bg, 0.22f),
            onSecondaryContainer = p.text,
            tertiary = p.info,
            onTertiary = contrastOn(p.info),
            tertiaryContainer = p.info.onto(p.bg, 0.22f),
            onTertiaryContainer = p.text,
            background = p.bg,
            onBackground = p.text,
            surface = p.surface,
            onSurface = p.text,
            surfaceVariant = p.chip,
            onSurfaceVariant = p.muted,
            surfaceTint = p.primary,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            error = p.danger,
            onError = onDanger,
            errorContainer = p.danger.onto(p.bg, 0.22f),
            onErrorContainer = p.danger,
            outline = p.border,
            outlineVariant = p.border.copy(alpha = 0.6f).compositeOver(p.bg),
            scrim = Color.Black,
            surfaceBright = p.elevated,
            surfaceDim = p.bg,
            surfaceContainerLowest = p.bg,
            surfaceContainerLow = p.surface,
            surfaceContainer = p.card,
            surfaceContainerHigh = p.elevated,
            surfaceContainerHighest = p.chip
        )
    }
}

/** Cross-fades every colour token so switching themes never hard-cuts. */
@Composable
private fun SeriePalette.animatedColors(): SeriePalette {
    val spec = tween<Color>(
        durationMillis = SerieeMotion.quick,
        easing = SerieeMotion.StandardEasing
    )

    @Composable
    fun anim(target: Color): Color =
        animateColorAsState(targetValue = target, animationSpec = spec, label = "palette").value

    val aBg = anim(bg)
    val aCard = anim(card)
    val aSurface = anim(surface)
    val aElevated = anim(elevated)
    val aChip = anim(chip)
    val aBorder = anim(border)
    val aPrimary = anim(primary)
    val aAccent = anim(accent)
    val aText = anim(text)
    val aMuted = anim(muted)
    val aSuccess = anim(success)
    val aWarning = anim(warning)
    val aDanger = anim(danger)
    val aInfo = anim(info)
    val aReminder = anim(reminder)
    val aStar = anim(star)

    val animatedGradient = gradient?.let { colors ->
        colors.map { anim(it) }
    }

    return copy(
        bg = aBg,
        card = aCard,
        surface = aSurface,
        elevated = aElevated,
        chip = aChip,
        border = aBorder,
        primary = aPrimary,
        accent = aAccent,
        accentSoft = if (isNeoBrutalist) aAccent else aAccent.copy(alpha = 0.16f),
        onAccent = if (isNeoBrutalist) neoContrastOn(aAccent) else contrastOn(aAccent),
        text = aText,
        muted = aMuted,
        success = aSuccess,
        warning = aWarning,
        danger = aDanger,
        info = aInfo,
        reminder = aReminder,
        star = aStar,
        gradient = animatedGradient
    )
}

private val FallbackPalette = ClassicDark

val LocalPalette = compositionLocalOf { FallbackPalette }

/** True while the system is in battery-saver mode; drives the smart theme. */
@Composable
fun rememberPowerSaveMode(): Boolean {
    val context = LocalContext.current
    val powerManager = remember(context) {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }
    var saving by remember(powerManager) { mutableStateOf(powerManager?.isPowerSaveMode == true) }
    DisposableEffect(context, powerManager) {
        if (powerManager == null) return@DisposableEffect onDispose { }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                saving = powerManager.isPowerSaveMode
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return saving
}

@Composable
fun SerieeTheme(
    mode: AppThemeMode,
    style: AppThemeStyle,
    customAccent: Color = SerieColors.Blue500,
    customBackgroundStart: Color = Color(0xFF1D4ED8),
    customBackgroundEnd: Color = Color(0xFF7C2A64),
    customGradient: Boolean = false,
    reduceTransparency: Boolean = false,
    batterySaverAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val powerSave = rememberPowerSaveMode()
    val isDark = when (mode) {
        AppThemeMode.System -> systemDark
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val effectiveStyle = if (batterySaverAmoled && powerSave && isDark && style == AppThemeStyle.Classic) {
        AppThemeStyle.Amoled
    } else {
        style
    }
    val dynamicScheme = rememberMaterialYouScheme(effectiveStyle == AppThemeStyle.MaterialYou, isDark)

    val target = resolvePalette(
        mode = mode,
        style = effectiveStyle,
        systemDark = systemDark,
        custom = CustomThemeColors(
            accent = customAccent,
            backgroundStart = customBackgroundStart,
            backgroundEnd = customBackgroundEnd,
            gradient = customGradient
        ),
        dynamicScheme = dynamicScheme,
        reduceTransparency = reduceTransparency
    )
    val p = target.animatedColors()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            view.context.findActivity()?.window?.let { window ->
                runCatching {
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = p.isLight
                    controller.isAppearanceLightNavigationBars = p.isLight
                }
            }
        }
    }

    val neoScheme = if (p.isLight) NeoBrutalistLightColorScheme else NeoBrutalistDarkColorScheme
    val materialScheme = remember(p) { schemeFor(p) }
    CompositionLocalProvider(
        LocalPalette provides p,
        LocalNeoBrutalistColors provides neoScheme
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = if (p.isNeoBrutalist) NeoBrutalistTypography else Typography(),
            shapes = if (p.isNeoBrutalist) NeoBrutalistShapes else Shapes(),
            content = content
        )
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext?.findActivity()
    else -> null
}

fun Configuration.primaryLocale(): Locale =
    if (!locales.isEmpty) locales[0] else Locale.getDefault()
