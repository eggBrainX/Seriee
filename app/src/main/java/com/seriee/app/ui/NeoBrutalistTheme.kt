package com.seriee.app.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Pure Neo-Brutalism color tokens.
 *
 * High-contrast cream/off-white backgrounds, pure white surfaces, vivid pop
 * accents, and absolute black (#000000) borders, hard shadows, and text.
 */
object NeoBrutalistColors {
    val Cream = Color(0xFFFFFDF5)
    val OffWhite = Color(0xFFF4F4F0)
    val PureWhite = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // Pop accents
    val Yellow = Color(0xFFFFDE59)
    val SkyBlue = Color(0xFF5CE1E6)
    val Pink = Color(0xFFFF90E8)
    val Mint = Color(0xFF7ED957)
    val CoralRed = Color(0xFFFF6B6B)

    // Dark-mode Neo-Brutalism counterparts
    val DarkBackground = Color(0xFF16161A)
    val DarkSurface = Color(0xFF232329)
    val DarkElevated = Color(0xFF2E2E36)
    val DarkMuted = Color(0xFFD4D4D8)
    val LightMuted = Color(0xFF2E2E32)
}

@Immutable
data class NeoBrutalistColorScheme(
    val background: Color,
    val backgroundAlt: Color,
    val surface: Color,
    val accentYellow: Color,
    val accentBlue: Color,
    val accentPink: Color,
    val accentMint: Color,
    val danger: Color,
    val border: Color,
    val shadow: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val isLight: Boolean
) {
    fun toMaterialColorScheme(): ColorScheme = if (isLight) {
        lightColorScheme(
            primary = accentYellow,
            onPrimary = NeoBrutalistColors.Black,
            primaryContainer = accentYellow,
            onPrimaryContainer = NeoBrutalistColors.Black,
            secondary = accentBlue,
            onSecondary = NeoBrutalistColors.Black,
            secondaryContainer = accentBlue,
            onSecondaryContainer = NeoBrutalistColors.Black,
            tertiary = accentPink,
            onTertiary = NeoBrutalistColors.Black,
            tertiaryContainer = accentPink,
            onTertiaryContainer = NeoBrutalistColors.Black,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = backgroundAlt,
            onSurfaceVariant = textMuted,
            surfaceTint = Color.Transparent,
            error = danger,
            onError = NeoBrutalistColors.Black,
            errorContainer = danger,
            onErrorContainer = NeoBrutalistColors.Black,
            outline = border,
            outlineVariant = border,
            scrim = NeoBrutalistColors.Black.copy(alpha = 0.65f),
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surface,
            surfaceContainerHigh = backgroundAlt,
            surfaceContainerHighest = backgroundAlt
        )
    } else {
        darkColorScheme(
            primary = accentYellow,
            onPrimary = NeoBrutalistColors.Black,
            primaryContainer = accentYellow,
            onPrimaryContainer = NeoBrutalistColors.Black,
            secondary = accentBlue,
            onSecondary = NeoBrutalistColors.Black,
            secondaryContainer = accentBlue,
            onSecondaryContainer = NeoBrutalistColors.Black,
            tertiary = accentPink,
            onTertiary = NeoBrutalistColors.Black,
            tertiaryContainer = accentPink,
            onTertiaryContainer = NeoBrutalistColors.Black,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = backgroundAlt,
            onSurfaceVariant = textMuted,
            surfaceTint = Color.Transparent,
            error = danger,
            onError = NeoBrutalistColors.Black,
            errorContainer = danger,
            onErrorContainer = NeoBrutalistColors.Black,
            outline = border,
            outlineVariant = border,
            scrim = NeoBrutalistColors.Black.copy(alpha = 0.75f),
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surface,
            surfaceContainerHigh = backgroundAlt,
            surfaceContainerHighest = backgroundAlt
        )
    }
}

val NeoBrutalistLightColorScheme = NeoBrutalistColorScheme(
    background = NeoBrutalistColors.Cream,
    backgroundAlt = NeoBrutalistColors.OffWhite,
    surface = NeoBrutalistColors.PureWhite,
    accentYellow = NeoBrutalistColors.Yellow,
    accentBlue = NeoBrutalistColors.SkyBlue,
    accentPink = NeoBrutalistColors.Pink,
    accentMint = NeoBrutalistColors.Mint,
    danger = NeoBrutalistColors.CoralRed,
    border = NeoBrutalistColors.Black,
    shadow = NeoBrutalistColors.Black,
    textPrimary = NeoBrutalistColors.Black,
    textMuted = NeoBrutalistColors.LightMuted,
    isLight = true
)

val NeoBrutalistDarkColorScheme = NeoBrutalistColorScheme(
    background = NeoBrutalistColors.DarkBackground,
    backgroundAlt = NeoBrutalistColors.DarkElevated,
    surface = NeoBrutalistColors.DarkSurface,
    accentYellow = NeoBrutalistColors.Yellow,
    accentBlue = NeoBrutalistColors.SkyBlue,
    accentPink = NeoBrutalistColors.Pink,
    accentMint = NeoBrutalistColors.Mint,
    danger = NeoBrutalistColors.CoralRed,
    border = NeoBrutalistColors.Black,
    shadow = NeoBrutalistColors.Black,
    textPrimary = NeoBrutalistColors.Cream,
    textMuted = NeoBrutalistColors.DarkMuted,
    isLight = false
)

val LocalNeoBrutalistColors = compositionLocalOf { NeoBrutalistLightColorScheme }

private val NeoShape6 = RoundedCornerShape(6.dp)
private val NeoShape8 = RoundedCornerShape(8.dp)
private val NeoPressEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

private fun cachedNeoShape(cornerRadius: Dp): RoundedCornerShape = when (cornerRadius) {
    6.dp -> NeoShape6
    8.dp -> NeoShape8
    else -> RoundedCornerShape(cornerRadius)
}

val NeoBrutalistShapes = Shapes(
    extraSmall = NeoShape6,
    small = NeoShape6,
    medium = NeoShape8,
    large = NeoShape8,
    extraLarge = NeoShape8
)

val NeoBrutalistTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 13.sp
    )
)

/**
 * Draws an unblurred, solid offset Neo-Brutalist shadow behind a clipped,
 * bordered card/container, while reserving [shadowOffset] space on the right
 * and bottom so the shadow is never clipped inside lists, rows, or screens.
 */
fun Modifier.neoBrutalist(
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Black,
    shadowColor: Color = Color.Black,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 8.dp
): Modifier {
    val shape = cachedNeoShape(cornerRadius)
    return this
        .padding(end = shadowOffset, bottom = shadowOffset)
        .drawBehind {
            val offsetPx = shadowOffset.toPx()
            val radiusPx = cornerRadius.toPx()
            if (offsetPx > 0f) {
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(offsetPx, offsetPx),
                    size = size,
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }
        }
        .clip(shape)
        .background(backgroundColor)
        .border(borderWidth, borderColor, shape)
}

/**
 * Tactile Neo-Brutalist interactive wrapper.
 *
 * When [onClick] is non-null and [enabled] is true, pressing down smoothly shifts
 * the surface toward the bottom-right by [shadowOffset] via GPU [graphicsLayer]
 * while collapsing the hard shadow down to 0.dp, simulating a physical button
 * press without layout thrash or Material ripple feedback.
 */
@Composable
fun NeoBrutalistBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    role: Role? = null,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Black,
    shadowColor: Color = Color.Black,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val activelyPressed = isPressed && onClick != null && enabled

    val pressProgress by animateFloatAsState(
        targetValue = if (activelyPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 125, easing = NeoPressEasing),
        label = "neoPressProgress"
    )
    val shape = remember(cornerRadius) { cachedNeoShape(cornerRadius) }

    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .graphicsLayer {
                val shiftPx = shadowOffset.toPx() * pressProgress
                translationX = shiftPx
                translationY = shiftPx
            }
            .drawBehind {
                val remainingPx = shadowOffset.toPx() * (1f - pressProgress)
                val radiusPx = cornerRadius.toPx()
                if (remainingPx > 0.25f) {
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(remainingPx, remainingPx),
                        size = size,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                }
            }
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = role,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Standalone Neo-Brutalist theme wrapper providing [NeoBrutalistColorScheme],
 * [NeoBrutalistTypography], and [NeoBrutalistShapes].
 */
@Composable
fun NeoBrutalistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) NeoBrutalistDarkColorScheme else NeoBrutalistLightColorScheme
    CompositionLocalProvider(LocalNeoBrutalistColors provides scheme) {
        MaterialTheme(
            colorScheme = scheme.toMaterialColorScheme(),
            typography = NeoBrutalistTypography,
            shapes = NeoBrutalistShapes,
            content = content
        )
    }
}

/**
 * High-contrast Neo-Brutalist confirmation dialog with zero soft elevation.
 */
@Composable
fun NeoBrutalistConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    confirmColor: Color = NeoBrutalistColors.CoralRed,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val neo = LocalNeoBrutalistColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .neoBrutalist(
                    backgroundColor = neo.surface,
                    borderColor = neo.border,
                    shadowColor = neo.shadow,
                    borderWidth = 2.5.dp,
                    shadowOffset = 6.dp,
                    cornerRadius = 8.dp
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = neo.textPrimary
                )
                Text(
                    text = message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = neo.textMuted,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    NeoBrutalistBox(
                        onClick = onDismiss,
                        role = Role.Button,
                        backgroundColor = neo.backgroundAlt,
                        borderColor = neo.border,
                        shadowColor = neo.shadow,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        cornerRadius = 6.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.heightIn(min = 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dismissLabel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = neo.textPrimary
                        )
                    }
                    NeoBrutalistBox(
                        onClick = onConfirm,
                        role = Role.Button,
                        backgroundColor = confirmColor,
                        borderColor = neo.border,
                        shadowColor = neo.shadow,
                        borderWidth = 2.dp,
                        shadowOffset = 3.dp,
                        cornerRadius = 6.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.heightIn(min = 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmLabel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeoBrutalistColors.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Neo-Brutalist determinate progress bar styled with the active color
 * palette, a thick solid black border, a hard unblurred offset shadow, and a
 * solid animated fill with an inked leading divider.
 */
@Composable
fun NeoBrutalistProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    fillColor: Color? = null,
    trackColor: Color? = null,
    borderColor: Color? = null,
    shadowColor: Color? = null,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 2.dp,
    cornerRadius: Dp = 6.dp,
    height: Dp = 13.dp,
    showLeadingDivider: Boolean = true
) {
    val p = LocalPalette.current
    val neo = LocalNeoBrutalistColors.current
    val resolvedFill = fillColor ?: if (p.isNeoBrutalist) p.success else neo.accentMint
    val resolvedTrack = trackColor ?: if (p.isNeoBrutalist) p.chip else neo.backgroundAlt
    val resolvedBorder = borderColor ?: if (p.isNeoBrutalist) p.border else neo.border
    val resolvedShadow = shadowColor ?: neo.shadow

    val clamped = fraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = clamped,
        animationSpec = SerieeMotion.SmoothSpring,
        label = "neoProgressFill"
    )
    val shape = remember(cornerRadius) { cachedNeoShape(cornerRadius) }

    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .progressSemantics(clamped)
                .drawBehind {
                    val shadowPx = shadowOffset.toPx()
                    val radiusPx = cornerRadius.toPx()
                    if (shadowPx > 0f) {
                        drawRoundRect(
                            color = resolvedShadow,
                            topLeft = Offset(shadowPx, shadowPx),
                            size = size,
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                    }
                }
                .clip(shape)
                .background(resolvedTrack)
                .drawBehind {
                    val fillWidth = (size.width * animatedFraction).coerceIn(0f, size.width)
                    if (fillWidth > 0f) {
                        drawRect(
                            color = resolvedFill,
                            size = Size(fillWidth, size.height)
                        )
                        if (showLeadingDivider && animatedFraction > 0.02f && animatedFraction < 0.99f) {
                            val dividerW = borderWidth.toPx()
                            val dividerX = (fillWidth - dividerW / 2f)
                                .coerceIn(0f, (size.width - dividerW).coerceAtLeast(0f))
                            drawRect(
                                color = resolvedBorder,
                                topLeft = Offset(dividerX, 0f),
                                size = Size(dividerW, size.height)
                            )
                        }
                    }
                }
                .border(borderWidth, resolvedBorder, shape)
        )
    }
}

/**
 * Custom Neo-Brutalist indeterminate progress bar for loading states,
 * featuring a solid sliding accent block framed by thick black borders and a
 * hard offset shadow.
 */
@Composable
fun NeoBrutalistIndeterminateProgressBar(
    modifier: Modifier = Modifier,
    fillColor: Color? = null,
    trackColor: Color? = null,
    borderColor: Color? = null,
    shadowColor: Color? = null,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 2.dp,
    cornerRadius: Dp = 6.dp,
    height: Dp = 14.dp
) {
    val p = LocalPalette.current
    val neo = LocalNeoBrutalistColors.current
    val resolvedFill = fillColor ?: if (p.isNeoBrutalist) p.primary else neo.accentYellow
    val resolvedTrack = trackColor ?: if (p.isNeoBrutalist) p.chip else neo.backgroundAlt
    val resolvedBorder = borderColor ?: if (p.isNeoBrutalist) p.border else neo.border
    val resolvedShadow = shadowColor ?: neo.shadow

    val transition = rememberInfiniteTransition(label = "neoIndeterminateProgress")
    val travel by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neoIndeterminateTravel"
    )
    val shape = remember(cornerRadius) { cachedNeoShape(cornerRadius) }

    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .progressSemantics()
                .drawBehind {
                    val shadowPx = shadowOffset.toPx()
                    val radiusPx = cornerRadius.toPx()
                    if (shadowPx > 0f) {
                        drawRoundRect(
                            color = resolvedShadow,
                            topLeft = Offset(shadowPx, shadowPx),
                            size = size,
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                    }
                }
                .clip(shape)
                .background(resolvedTrack)
                .drawBehind {
                    val blockWidth = size.width * 0.34f
                    val startX = (size.width - blockWidth) * travel
                    val dividerW = borderWidth.toPx()
                    drawRect(
                        color = resolvedFill,
                        topLeft = Offset(startX, 0f),
                        size = Size(blockWidth, size.height)
                    )
                    if (startX > dividerW) {
                        drawRect(
                            color = resolvedBorder,
                            topLeft = Offset(startX, 0f),
                            size = Size(dividerW, size.height)
                        )
                    }
                    val endX = startX + blockWidth - dividerW
                    if (endX < size.width - dividerW) {
                        drawRect(
                            color = resolvedBorder,
                            topLeft = Offset(endX, 0f),
                            size = Size(dividerW, size.height)
                        )
                    }
                }
                .border(borderWidth, resolvedBorder, shape)
        )
    }
}

