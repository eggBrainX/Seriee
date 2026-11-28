package com.seriee.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colours and geometry come straight from Egi's logo SVG (viewBox 345x345):
// cream tile, ink clapperboard, gradient leaf, cream play triangle.
private val LogoInk = Color(0xFF222222)
private val LogoCream = Color(0xFFF7F1EA)
private val LeafVein = Color(0xFF14602A)
private const val LOGO_REST_ANGLE = -17f

/**
 * Full-screen animated splash shown once per cold start: the leaf sweeps in,
 * the clapper snaps shut and springs back open, the play triangle pulses and
 * the "Seriee" letters spring in one by one. It is deliberately NOT clickable:
 * touches pass through to the UI underneath (a covering clickable layer also
 * breaks the MainActivity UI tests, which tap through the splash).
 */
@Composable
fun SerieeSplash(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current

    val logoScale = remember { Animatable(0.9f) }
    val leafIn = remember { Animatable(0f) }
    val clapper = remember { Animatable(-32f) }
    val playScale = remember { Animatable(1f) }
    val letters = remember { "Seriee".map { Animatable(0f) } }

    val swayTransition = rememberInfiniteTransition(label = "splashSway")
    val sway by swayTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "sway"
    )

    LaunchedEffect(Unit) {
        launch { logoScale.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 300f)) }
        launch { leafIn.animateTo(1f, spring(dampingRatio = 0.58f, stiffness = 260f)) }
        launch { clapper.animateTo(LOGO_REST_ANGLE, tween(430)) }
        launch {
            delay(430)
            clapper.animateTo(0f, tween(110, easing = EaseIn))
            delay(110)
            clapper.animateTo(LOGO_REST_ANGLE, spring(dampingRatio = 0.42f, stiffness = 320f))
        }
        launch {
            delay(880)
            playScale.animateTo(1.18f, tween(90))
            playScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 380f))
        }
        launch {
            delay(560)
            letters.forEachIndexed { i, anim ->
                launch {
                    delay(i * 55L)
                    anim.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
                }
            }
        }
        launch {
            delay(1950)
            onFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(p.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SplashLogo(
                clapperAngle = clapper.value + sway * 0.7f,
                leafIn = leafIn.value,
                leafSway = sway * 1.4f,
                playScale = playScale.value,
                modifier = Modifier
                    .size(216.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            )
            Spacer(Modifier.height(24.dp))
            Row {
                "Seriee".forEachIndexed { i, c ->
                    val a = letters[i].value
                    Text(
                        c.toString(),
                        style = TextStyle(
                            color = p.text,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.graphicsLayer {
                            alpha = a.coerceIn(0f, 1f)
                            scaleX = 0.5f + 0.5f * a
                            scaleY = 0.5f + 0.5f * a
                            translationY = (1f - a) * 26f
                        }
                    )
                }
            }
        }
    }
}

/**
 * Egi's Seriee logo redrawn in Compose from his SVG, split into layers so the
 * clapper top, leaf and play triangle can move independently.
 */
@Composable
fun SplashLogo(
    clapperAngle: Float,
    leafIn: Float,
    leafSway: Float,
    playScale: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        // Tile, shadow and clapper body.
        Canvas(Modifier.fillMaxSize()) {
            logoSpace {
                drawRoundRect(LogoCream, Offset(15f, 15f), Size(315f, 315f), CornerRadius(72f, 72f))
                drawOval(Color.Black.copy(alpha = 0.08f), Offset(85f, 279f), Size(200f, 16f))
                drawRoundRect(LogoInk, Offset(135f, 158f), Size(160f, 108f), CornerRadius(12f, 12f))
                drawPath(polygon(198f, 166f, 222f, 166f, 210f, 186f, 186f, 186f), LogoCream)
                drawPath(polygon(236f, 166f, 260f, 166f, 248f, 186f, 224f, 186f), LogoCream)
                drawPath(polygon(274f, 166f, 292f, 166f, 288f, 186f, 262f, 186f), LogoCream)
            }
        }
        // Clapper top, hinged at the SVG's (100, 138).
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = clapperAngle
                    transformOrigin = TransformOrigin(100f / 345f, 138f / 345f)
                }
        ) {
            logoSpace {
                drawRoundRect(LogoInk, Offset(100f, 116f), Size(190f, 44f), CornerRadius(8f, 8f))
                drawPath(polygon(136f, 124f, 160f, 124f, 148f, 152f, 124f, 152f), LogoCream)
                drawPath(polygon(180f, 124f, 204f, 124f, 192f, 152f, 168f, 152f), LogoCream)
                drawPath(polygon(224f, 124f, 248f, 124f, 236f, 152f, 212f, 152f), LogoCream)
            }
        }
        // Play triangle.
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = playScale
                    scaleY = playScale
                    transformOrigin = TransformOrigin(238f / 345f, 232f / 345f)
                }
        ) {
            logoSpace {
                val play = polygon(224f, 212f, 224f, 252f, 260f, 232f)
                drawPath(play, LogoCream)
                drawPath(play, LogoCream, style = Stroke(width = 6f, join = StrokeJoin.Round))
            }
        }
        // Leaf, sweeping in from the left and swaying around its bottom tip.
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -12f * (1f - leafIn) + leafSway * leafIn.coerceIn(0f, 1f)
                    translationX = -30f * (1f - leafIn) * (size.width / 345f)
                    alpha = leafIn.coerceIn(0f, 1f)
                    transformOrigin = TransformOrigin(213f / 345f, 285f / 345f)
                }
        ) {
            logoSpace {
                val leafShape = Path().apply {
                    moveTo(62f, 122f)
                    cubicTo(150f, 118f, 202f, 172f, 206f, 232f)
                    cubicTo(207f, 254f, 210f, 270f, 213f, 285f)
                    cubicTo(128f, 282f, 52f, 232f, 62f, 122f)
                    close()
                }
                drawPath(
                    leafShape,
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFF7BD65A),
                            0.55f to Color(0xFF3FB04A),
                            1f to Color(0xFF1E8A3A)
                        ),
                        start = Offset(68f, 118f),
                        end = Offset(197f, 285f)
                    )
                )
                val highlight = Path().apply {
                    moveTo(62f, 122f)
                    cubicTo(120f, 128f, 170f, 160f, 190f, 215f)
                    cubicTo(140f, 190f, 92f, 170f, 62f, 122f)
                    close()
                }
                drawPath(
                    highlight,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF9BE87A).copy(alpha = 0.9f),
                            Color(0xFF9BE87A).copy(alpha = 0f)
                        ),
                        start = Offset(52f, 118f),
                        end = Offset(213f, 285f)
                    )
                )
                val vein = Path().apply {
                    moveTo(112f, 212f)
                    cubicTo(150f, 222f, 185f, 245f, 205f, 280f)
                }
                drawPath(vein, LeafVein, style = Stroke(width = 5f, cap = StrokeCap.Round))
            }
        }
    }
}

/** Runs [block] with the draw scope scaled so coordinates are SVG units (345 across). */
private inline fun DrawScope.logoSpace(block: DrawScope.() -> Unit) {
    val k = size.width / 345f
    withTransform({ scale(k, k) }) { block() }
}

private fun polygon(vararg pts: Float): Path {
    val path = Path()
    path.moveTo(pts[0], pts[1])
    var i = 2
    while (i < pts.size) {
        path.lineTo(pts[i], pts[i + 1])
        i += 2
    }
    path.close()
    return path
}
