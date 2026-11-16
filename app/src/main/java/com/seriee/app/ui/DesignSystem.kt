package com.seriee.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.data.WatchStatus

object SerieeType {
    val display = androidx.compose.ui.text.TextStyle(
        fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 34.sp
    )
    val title = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp
    )
    val section = androidx.compose.ui.text.TextStyle(
        fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 21.sp
    )
    val body = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp
    )
    val label = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp
    )
    val caption = androidx.compose.ui.text.TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp
    )
}

object SerieeShape {
    val pill = RoundedCornerShape(50)
    val chip = RoundedCornerShape(14.dp)
    val field = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(22.dp)
    val sheet = RoundedCornerShape(28.dp)
}

object SerieeSpace {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

object SerieeMotion {
    const val quick = 160
    const val standard = 240
    const val emphasized = 320

    val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val StandardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecel = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 520f
    )
    val SmoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 380f
    )
    val SleekSpring = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 420f
    )
}

/** Status colours shared by cards, badges and the detail sheet. */
@Composable
fun statusColor(status: WatchStatus): Color {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        return when (status) {
            WatchStatus.Watching -> NeoBrutalistColors.SkyBlue
            WatchStatus.Completed -> NeoBrutalistColors.Mint
            WatchStatus.PlanToWatch -> NeoBrutalistColors.OffWhite
            WatchStatus.ComingSoon -> NeoBrutalistColors.Yellow
            WatchStatus.OnHold -> NeoBrutalistColors.Pink
            WatchStatus.Dropped -> NeoBrutalistColors.CoralRed
        }
    }
    return when (status) {
        WatchStatus.Watching -> p.primary
        WatchStatus.Completed -> p.success
        WatchStatus.PlanToWatch -> p.muted
        WatchStatus.ComingSoon -> p.warning
        WatchStatus.OnHold -> p.reminder
        WatchStatus.Dropped -> p.danger
    }
}

/** Specular highlight used on glass borders so they catch the light. */
private fun SeriePalette.specularColors(): List<Color> {
    val strong = if (isLight) 0.92f else 0.42f
    val soft = if (isLight) 0.50f else 0.14f
    return listOf(
        Color.White.copy(alpha = strong),
        Color.White.copy(alpha = 0.06f),
        Color.White.copy(alpha = soft)
    )
}

/**
 * Theme-aware outline: a brushed specular edge on glass styles, the palette
 * border everywhere else.
 */
fun Modifier.themedBorder(p: SeriePalette, shape: Shape, width: Dp = 1.dp): Modifier =
    border(
        width = if (p.isNeoBrutalist) 2.5.dp else width,
        brush = if (p.isGlass) Brush.linearGradient(p.specularColors()) else SolidColor(p.border),
        shape = shape
    )

/**
 * Pure Compose translucent glass surface for Frost/Liquid/Custom styles, or crisp
 * Neo-Brutalist offset-shadow surface when [SeriePalette.isNeoBrutalist] is active.
 */
@Suppress("UNUSED_PARAMETER")
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color? = null,
    blurRadius: Dp = 18.dp,
    refractionHeight: Dp = 12.dp,
    refractionAmount: Dp = 24.dp,
    chromaticAberration: Boolean = true,
    elevated: Boolean = false,
    withHighlight: Boolean = true,
    withBorder: Boolean = true
): Modifier = composed {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        val bgColor = (tint ?: p.card).let {
            if (it.alpha < 0.99f) it.copy(alpha = 1f) else it
        }
        return@composed this.neoBrutalist(
            backgroundColor = bgColor,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = if (withBorder) 2.5.dp else 0.dp,
            shadowOffset = 4.dp,
            cornerRadius = 8.dp
        )
    }

    val resolvedTint = tint ?: if (p.isGlass) {
        if (p.isLight) Color.White.copy(alpha = 0.56f)
        else Color(0xFF0E1729).copy(alpha = 0.62f)
    } else {
        p.card
    }
    val surface = if (p.isGlass) resolvedTint.compositeOver(p.bg.copy(alpha = 0.35f)) else resolvedTint

    this
        .then(
            if (elevated && p.isGlass) Modifier.shadow(8.dp, shape, clip = false) else Modifier
        )
        .clip(shape)
        .background(surface)
        .then(
            if (p.isGlass && withHighlight) {
                Modifier.drawWithCache {
                    val sheen = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = if (p.isLight) 0.34f else 0.14f),
                        0.45f to Color.White.copy(alpha = if (p.isLight) 0.06f else 0.02f),
                        1f to Color.Transparent
                    )
                    onDrawBehind {
                        drawRect(sheen)
                    }
                }
            } else {
                Modifier
            }
        )
        .then(
            if (withBorder) {
                Modifier.themedBorder(p, shape, width = if (p.isGlass) 0.85.dp else 1.dp)
            } else {
                Modifier
            }
        )
}

/** Translucent floating chrome (nav bar, top bar) or Neo-Brutalist solid chrome. */
fun Modifier.glassChrome(
    p: SeriePalette,
    shape: Shape
): Modifier = composed {
    if (p.isNeoBrutalist) {
        return@composed this.neoBrutalist(
            backgroundColor = p.surface,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 8.dp
        )
    }
    val surfaceTint = if (p.isGlass) {
        if (p.isLight) Color.White.copy(alpha = 0.68f)
        else Color(0xFF0B1324).copy(alpha = 0.72f)
    } else {
        p.surface
    }
    liquidGlass(
        shape = shape,
        tint = surfaceTint,
        blurRadius = 12.dp,
        refractionHeight = 18.dp,
        refractionAmount = 36.dp,
        chromaticAberration = true,
        elevated = true,
        withHighlight = true,
        withBorder = true
    )
}

/**
 * Provides an ambient glass background inside standalone Android windows
 * such as [androidx.compose.material3.ModalBottomSheet] and [androidx.compose.ui.window.Dialog],
 * or a crisp solid surface for Neo-Brutalism.
 */
@Composable
fun LiquidSheetHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(p.surface)
        ) {
            content()
        }
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(if (p.isGlass) p.surface.copy(alpha = if (p.isLight) 0.92f else 0.88f) else p.surface)
                if (p.isGlass) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                p.primary.copy(alpha = if (p.isLight) 0.16f else 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.12f),
                            radius = size.maxDimension * 0.65f
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.12f),
                        radius = size.maxDimension * 0.65f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                p.accent.copy(alpha = if (p.isLight) 0.14f else 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.72f),
                            radius = size.maxDimension * 0.6f
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.72f),
                        radius = size.maxDimension * 0.6f
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * Full-screen backdrop.
 *
 * The cached base gradient + luminous orbs live on [modifier] (the captured
 * [LayerBackdrop]) so glass cards never re-blur every idle frame. When
 * [SeriePalette.animatedGradient] is active, a lightweight GPU-translated glow
 * layer drifts smoothly above it without invalidating the backdrop cache.
 */
@Composable
fun SerieeBackground(p: SeriePalette, modifier: Modifier = Modifier) {
    val gradient = p.gradient
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(p.bg)
                .then(
                    if (gradient != null && !p.isNeoBrutalist) {
                        Modifier.drawWithCache {
                            val linear = Brush.linearGradient(colors = gradient)
                            val orbA = if (p.isGlass) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        p.primary.copy(alpha = if (p.isLight) 0.28f else 0.32f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.22f, size.height * 0.16f),
                                    radius = size.width * 0.65f
                                )
                            } else null
                            val orbB = if (p.isGlass) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        p.accent.copy(alpha = if (p.isLight) 0.22f else 0.24f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.82f, size.height * 0.50f),
                                    radius = size.width * 0.60f
                                )
                            } else null
                            val orbC = if (p.isGlass) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        (gradient.lastOrNull() ?: p.accent).copy(
                                            alpha = if (p.isLight) 0.26f else 0.30f
                                        ),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.38f, size.height * 0.86f),
                                    radius = size.width * 0.70f
                                )
                            } else null
                            onDrawBehind {
                                drawRect(linear)
                                orbA?.let {
                                    drawCircle(
                                        brush = it,
                                        center = Offset(size.width * 0.22f, size.height * 0.16f),
                                        radius = size.width * 0.65f
                                    )
                                }
                                orbB?.let {
                                    drawCircle(
                                        brush = it,
                                        center = Offset(size.width * 0.82f, size.height * 0.50f),
                                        radius = size.width * 0.60f
                                    )
                                }
                                orbC?.let {
                                    drawCircle(
                                        brush = it,
                                        center = Offset(size.width * 0.38f, size.height * 0.86f),
                                        radius = size.width * 0.70f
                                    )
                                }
                            }
                        }
                    } else Modifier
                )
        )

        if (gradient != null && !p.isNeoBrutalist && p.animatedGradient && p.isGlass) {
            val transition = rememberInfiniteTransition(label = "liquidBackground")
            val drift by transition.animateFloat(
                initialValue = -1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 14_000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "liquidDrift"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = size.width * 0.08f * drift
                        translationY = size.height * 0.04f * drift
                    }
                    .drawWithCache {
                        val glowA = Brush.radialGradient(
                            colors = listOf(
                                p.primary.copy(alpha = if (p.isLight) 0.14f else 0.16f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.30f, size.height * 0.22f),
                            radius = size.width * 0.55f
                        )
                        val glowB = Brush.radialGradient(
                            colors = listOf(
                                (gradient.lastOrNull() ?: p.accent).copy(
                                    alpha = if (p.isLight) 0.12f else 0.15f
                                ),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.72f, size.height * 0.76f),
                            radius = size.width * 0.58f
                        )
                        onDrawBehind {
                            drawCircle(
                                brush = glowA,
                                center = Offset(size.width * 0.30f, size.height * 0.22f),
                                radius = size.width * 0.55f
                            )
                            drawCircle(
                                brush = glowB,
                                center = Offset(size.width * 0.72f, size.height * 0.76f),
                                radius = size.width * 0.58f
                            )
                        }
                    }
            )
        }
    }
}

@Composable
fun SerieeSurface(
    modifier: Modifier = Modifier,
    shape: Shape = SerieeShape.card,
    level: Int = 1,
    content: @Composable () -> Unit
) {
    val p = LocalPalette.current
    val bg = when (level) {
        0 -> p.bg
        2 -> p.elevated
        else -> p.card
    }
    Box(
        modifier = when {
            p.isNeoBrutalist && level > 0 -> modifier.neoBrutalist(
                backgroundColor = bg,
                borderColor = p.border,
                shadowColor = NeoBrutalistColors.Black,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp,
                cornerRadius = 8.dp
            )
            level > 0 && p.isGlass -> modifier.liquidGlass(
                shape = shape,
                tint = bg,
                blurRadius = 12.dp,
                refractionHeight = 10.dp,
                refractionAmount = 16.dp,
                chromaticAberration = false
            )
            else -> modifier
                .clip(shape)
                .background(bg)
                .then(if (level > 0) Modifier.themedBorder(p, shape) else Modifier)
        }
    ) {
        content()
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                color = p.text
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                    color = p.muted
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun IconPillButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color? = null,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            modifier = modifier.size(44.dp),
            onClick = onClick,
            role = Role.Button,
            backgroundColor = NeoBrutalistColors.OffWhite,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.dp,
            shadowOffset = 3.dp,
            cornerRadius = 8.dp,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = tint ?: p.text,
                modifier = Modifier.size(18.dp)
            )
        }
        return
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = SerieeMotion.SnappySpring,
        label = "iconPillScale"
    )
    Box(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .liquidGlass(
                shape = CircleShape,
                tint = if (p.isGlass) {
                    if (p.isLight) Color.White.copy(alpha = 0.52f)
                    else Color.White.copy(alpha = 0.14f)
                } else p.chip,
                blurRadius = 12.dp,
                refractionHeight = 8.dp,
                refractionAmount = 14.dp,
                chromaticAberration = false
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint ?: p.text.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    showChevron: Boolean = true,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint ?: p.text, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Medium,
            color = p.text,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                value,
                fontSize = 11.sp,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(4.dp))
        }
        if (showChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = if (p.isNeoBrutalist) p.text else p.muted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun RadioCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            role = Role.RadioButton,
            backgroundColor = if (selected) NeoBrutalistColors.Yellow else p.surface,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.5.dp,
            shadowOffset = 3.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = NeoBrutalistColors.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selected) NeoBrutalistColors.Black else p.text
                    )
                    Text(
                        subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) NeoBrutalistColors.Black.copy(alpha = 0.8f) else p.muted,
                        lineHeight = 15.sp
                    )
                }
            }
        }
        return
    }
    val tintColor by animateColorAsState(
        targetValue = if (selected) {
            p.primary.copy(alpha = if (p.isGlass) 0.22f else 0.12f)
        } else if (p.isGlass) {
            if (p.isLight) Color.White.copy(alpha = 0.44f) else Color.White.copy(alpha = 0.08f)
        } else {
            p.chip
        },
        animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
        label = "radioTint"
    )
    val cardShape = SerieeShape.field
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(tintColor)
            .border(
                width = if (selected) 1.dp else if (p.isGlass) 0.75.dp else 1.dp,
                color = if (selected) p.primary.copy(alpha = 0.65f) else p.border,
                shape = cardShape
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) p.primary else p.muted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.text)
            Text(subtitle, fontSize = 11.sp, color = p.muted, lineHeight = 15.sp)
        }
    }
}

@Composable
fun SegmentedTabs(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    if (labels.isEmpty()) return
    val animatedIndex by animateFloatAsState(
        targetValue = selected.coerceIn(0, labels.lastIndex).toFloat(),
        animationSpec = SerieeMotion.SleekSpring,
        label = "segmentedTabIndex"
    )

    if (p.isNeoBrutalist) {
        val neoContainerModifier = modifier
            .fillMaxWidth()
            .neoBrutalist(
                backgroundColor = p.chip,
                borderColor = p.border,
                shadowColor = NeoBrutalistColors.Black,
                borderWidth = 2.5.dp,
                shadowOffset = 3.dp,
                cornerRadius = 8.dp
            )
            .padding(4.dp)
        BoxWithConstraints(modifier = neoContainerModifier) {
            val tabWidth = (maxWidth / labels.size).coerceAtLeast(0.dp)
            val pillShape = RoundedCornerShape(6.dp)
            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .height(34.dp)
                    .graphicsLayer {
                        translationX = tabWidth.toPx() * animatedIndex
                    }
                    .clip(pillShape)
                    .background(NeoBrutalistColors.Yellow)
                    .border(2.dp, NeoBrutalistColors.Black, pillShape)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                labels.forEachIndexed { index, label ->
                    val active = index == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(pillShape)
                            .selectable(selected = active, role = Role.Tab) { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (active) NeoBrutalistColors.Black else p.text,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        return
    }

    val containerTint = if (p.isGlass) {
        if (p.isLight) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.10f)
    } else p.chip

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(SerieeShape.pill)
            .background(containerTint)
            .themedBorder(p, SerieeShape.pill, width = if (p.isGlass) 0.75.dp else 1.dp)
            .padding(4.dp)
    ) {
        val tabWidth = (maxWidth / labels.size).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .width(tabWidth)
                .height(36.dp)
                .graphicsLayer {
                    translationX = tabWidth.toPx() * animatedIndex
                }
                .clip(SerieeShape.pill)
                .background(Brush.horizontalGradient(listOf(p.primary, p.accent)))
                .then(
                    if (p.isGlass) {
                        Modifier.border(0.75.dp, Color.White.copy(alpha = 0.45f), SerieeShape.pill)
                    } else Modifier
                )
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                val active = index == selected
                val textColor by animateColorAsState(
                    targetValue = if (active) p.onAccent else p.muted,
                    animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
                    label = "segText_$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(SerieeShape.pill)
                        .selectable(
                            selected = active,
                            role = Role.Tab,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Boolean = false
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = onClick,
            enabled = enabled,
            role = Role.Button,
            backgroundColor = if (enabled) NeoBrutalistColors.Yellow else p.chip,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (enabled) NeoBrutalistColors.Black else p.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        return
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = SerieeMotion.SnappySpring,
        label = "primaryBtnScale"
    )
    val tintColor = if (enabled) {
        p.primary.copy(alpha = if (p.isGlass) 0.78f else 1f)
    } else {
        p.chip
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .liquidGlass(
                shape = SerieeShape.pill,
                tint = tintColor,
                blurRadius = 12.dp,
                refractionHeight = 10.dp,
                refractionAmount = 16.dp,
                chromaticAberration = false,
                elevated = enabled && p.isGlass,
                withBorder = true
            )
            .then(
                if (enabled && gradient) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            listOf(
                                p.primary.copy(alpha = if (p.isGlass) 0.68f else 1f),
                                p.accent.copy(alpha = if (p.isGlass) 0.68f else 1f)
                            )
                        )
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) p.onAccent else p.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = onClick,
            role = Role.Button,
            backgroundColor = p.surface,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = p.text
            )
        }
        return
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = SerieeMotion.SnappySpring,
        label = "secondaryBtnScale"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .liquidGlass(
                shape = SerieeShape.pill,
                tint = if (p.isGlass) {
                    if (p.isLight) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.12f)
                } else p.chip,
                blurRadius = 12.dp,
                refractionHeight = 10.dp,
                refractionAmount = 16.dp,
                chromaticAberration = false
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.text)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    hint: String,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (p.isNeoBrutalist) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .neoBrutalist(
                        backgroundColor = NeoBrutalistColors.SkyBlue,
                        borderColor = p.border,
                        shadowColor = NeoBrutalistColors.Black,
                        borderWidth = 2.5.dp,
                        shadowOffset = 4.dp,
                        cornerRadius = 8.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(28.dp))
            }
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = p.text, textAlign = TextAlign.Center)
            Text(hint, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = p.muted, textAlign = TextAlign.Center, lineHeight = 17.sp)
            if (ctaLabel != null && onCta != null) {
                Spacer(Modifier.height(4.dp))
                NeoBrutalistBox(
                    modifier = Modifier.heightIn(min = 48.dp),
                    onClick = onCta,
                    role = Role.Button,
                    backgroundColor = NeoBrutalistColors.Yellow,
                    borderColor = p.border,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        ctaLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeoBrutalistColors.Black
                    )
                }
            }
            return
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .liquidGlass(
                    shape = CircleShape,
                    tint = if (p.isGlass) {
                        if (p.isLight) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.12f)
                    } else p.chip,
                    blurRadius = 16.dp,
                    refractionHeight = 14.dp,
                    refractionAmount = 24.dp,
                    elevated = true
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = p.accent, modifier = Modifier.size(28.dp))
        }
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.text, textAlign = TextAlign.Center)
        Text(hint, fontSize = 12.sp, color = p.muted, textAlign = TextAlign.Center, lineHeight = 17.sp)
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .liquidGlass(
                        shape = SerieeShape.pill,
                        tint = p.primary.copy(alpha = if (p.isGlass) 0.80f else 1f),
                        blurRadius = 14.dp,
                        refractionHeight = 10.dp,
                        refractionAmount = 18.dp,
                        elevated = p.isGlass
                    )
                    .clickable(onClick = onCta)
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(ctaLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.onAccent)
            }
        }
    }
}

/** Shared drag handle for every [androidx.compose.material3.ModalBottomSheet]. */
@Composable
fun SheetDragHandle() {
    val p = LocalPalette.current
    Box(
        Modifier
            .padding(top = 12.dp, bottom = 6.dp)
            .size(width = if (p.isNeoBrutalist) 48.dp else 40.dp, height = if (p.isNeoBrutalist) 6.dp else 5.dp)
            .clip(RoundedCornerShape(if (p.isNeoBrutalist) 2.dp else 50.dp))
            .background(
                when {
                    p.isNeoBrutalist -> p.border
                    p.isGlass -> Color.White.copy(alpha = if (p.isLight) 0.7f else 0.35f)
                    else -> p.border
                }
            )
    )
}

@Composable
fun GradientProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    track: Color? = null,
    height: Dp? = null
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistProgressBar(
            fraction = fraction,
            modifier = modifier,
            fillColor = colors?.firstOrNull() ?: p.success,
            trackColor = track ?: p.chip,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            height = height ?: 13.dp
        )
        return
    }
    val clamped = fraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = clamped,
        animationSpec = SerieeMotion.SmoothSpring,
        label = "progressFill"
    )
    val resolvedTrack = track ?: if (p.isGlass) {
        if (p.isLight) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.14f)
    } else p.chip
    val fillColors = colors ?: listOf(p.primary, p.accent)
    val pillShape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(height ?: 6.dp)
            .clip(pillShape)
            .background(resolvedTrack)
            .drawBehind {
                if (animatedFraction > 0f) {
                    val fillW = (size.width * animatedFraction).coerceAtLeast(size.height.coerceAtMost(size.width))
                    drawRoundRect(
                        brush = Brush.horizontalGradient(fillColors),
                        size = Size(fillW, size.height),
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                }
            }
    )
}

@Composable
fun StatusBadge(status: WatchStatus, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val color = statusColor(status)
    if (p.isNeoBrutalist) {
        val shape = RoundedCornerShape(6.dp)
        Box(
            modifier = modifier
                .clip(shape)
                .background(color)
                .border(2.dp, NeoBrutalistColors.Black, shape)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status == WatchStatus.Completed) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(3.dp))
                }
                Text(
                    stringResource(status.labelRes).uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeoBrutalistColors.Black
                )
            }
        }
        return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = if (p.isGlass) 0.22f else 0.16f))
            .then(
                if (p.isGlass) Modifier.border(0.75.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
                else Modifier
            )
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (status == WatchStatus.Completed) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(3.dp))
            }
            Text(
                stringResource(status.labelRes),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
