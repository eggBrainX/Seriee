package com.seriee.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val MascotBody = Color(0xFF26282F)
private val MascotLeaf = Color(0xFF79A84E)
private val MascotLeafLight = Color(0xFF93BC60)
private val MascotBackdrop = Color(0xFFF3D9AE)

/** Diagonal white stripes on a transparent (dark) band, Vetro-mascot style. */
@Composable
private fun StripeBand(modifier: Modifier = Modifier) {
    Box(modifier.clipToBounds()) {
        Row(Modifier.fillMaxSize().offset(x = (-8).dp)) {
            repeat(6) {
                Box(
                    Modifier
                        .width(13.dp)
                        .fillMaxHeight()
                        .graphicsLayer { rotationZ = 24f }
                        .background(Color.White)
                )
                Spacer(Modifier.width(13.dp))
            }
        }
    }
}

@Composable
private fun MascotEye(blink: Float) {
    Box(
        Modifier
            .size(21.dp)
            .graphicsLayer {
                scaleY = blink
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(MascotBody),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                Modifier
                    .padding(top = 2.dp, end = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Seriee's clapperboard mascot (design B), drawn entirely in Compose so it can
 * move: the clapper snaps shut and springs back open, the eyes blink, the arms
 * wave, the leaf sways and the whole body breathes with a soft bounce.
 */
@Composable
fun SerieeMascot(modifier: Modifier = Modifier) {
    val clap = remember { Animatable(-17f) }
    val blink = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2600)
            clap.animateTo(0f, tween(110, easing = EaseIn))
            delay(120)
            clap.animateTo(-17f, spring(dampingRatio = 0.42f, stiffness = 320f))
        }
    }
    LaunchedEffect(Unit) {
        var cycle = 0
        while (true) {
            delay(2500L + (cycle % 3) * 350L)
            blink.animateTo(0.08f, tween(70))
            blink.animateTo(1f, tween(130))
            cycle++
        }
    }

    val infinite = rememberInfiniteTransition(label = "mascot")
    val bounce by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "bounce"
    )
    val armWave by infinite.animateFloat(
        initialValue = -9f,
        targetValue = 11f,
        animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
        label = "armWave"
    )
    val leafSway by infinite.animateFloat(
        initialValue = -5f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1150), RepeatMode.Reverse),
        label = "leafSway"
    )

    Box(
        modifier
            .size(190.dp, 196.dp)
            .graphicsLayer { translationY = bounce }
    ) {
        // Soft peach disc behind the character (also keeps it readable on dark themes).
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = 8.dp)
                .size(168.dp)
                .clip(CircleShape)
                .background(MascotBackdrop.copy(alpha = 0.5f))
        )

        // Arms (behind the body, waving from the shoulder pivot).
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = 4.dp, y = 20.dp)
                .size(40.dp, 14.dp)
                .graphicsLayer {
                    rotationZ = armWave
                    transformOrigin = TransformOrigin(1f, 0.5f)
                }
                .clip(RoundedCornerShape(7.dp))
                .background(MascotBody)
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-4).dp, y = 20.dp)
                .size(40.dp, 14.dp)
                .graphicsLayer {
                    rotationZ = -armWave
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .clip(RoundedCornerShape(7.dp))
                .background(MascotBody)
        )

        // Feet.
        Row(Modifier.align(Alignment.BottomCenter)) {
            Box(
                Modifier
                    .size(24.dp, 13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MascotBody)
            )
            Spacer(Modifier.width(26.dp))
            Box(
                Modifier
                    .size(24.dp, 13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MascotBody)
            )
        }

        // Body.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 9.dp)
                .size(148.dp, 116.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MascotBody)
        ) {
            StripeBand(Modifier.fillMaxWidth().height(25.dp))
            // Face: eyes, smile, play button.
            Box(Modifier.fillMaxSize().padding(top = 36.dp), contentAlignment = Alignment.TopCenter) {
                Row {
                    MascotEye(blink.value)
                    Spacer(Modifier.width(30.dp))
                    MascotEye(blink.value)
                }
            }
            Canvas(Modifier.align(Alignment.Center).offset(y = 2.dp).size(26.dp, 13.dp)) {
                drawArc(
                    color = Color.White,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Canvas(Modifier.align(Alignment.Center).offset(y = 30.dp).size(27.dp, 30.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.08f)
                    lineTo(size.width * 0.18f, size.height * 0.92f)
                    lineTo(size.width * 0.88f, size.height * 0.5f)
                    close()
                }
                drawPath(path, Color.White)
            }
        }

        // Clapper top: striped bar hinged at the bottom-left, leaf riding on top.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = 40.dp)
                .size(150.dp, 30.dp)
                .graphicsLayer {
                    rotationZ = clap.value
                    transformOrigin = TransformOrigin(0.02f, 1f)
                }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(7.dp))
                    .background(MascotBody)
            ) {
                StripeBand(Modifier.fillMaxSize())
            }
            // Leaf cluster, swaying from its stem.
            Box(
                Modifier
                    .offset(x = 36.dp, y = (-17).dp)
                    .size(48.dp, 26.dp)
                    .graphicsLayer {
                        rotationZ = leafSway
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
            ) {
                Box(
                    Modifier
                        .offset(x = 2.dp, y = 4.dp)
                        .size(30.dp, 17.dp)
                        .graphicsLayer { rotationZ = -16f }
                        .clip(RoundedCornerShape(topStartPercent = 100, bottomEndPercent = 100))
                        .background(MascotLeaf)
                )
                Box(
                    Modifier
                        .offset(x = 22.dp, y = 0.dp)
                        .size(24.dp, 15.dp)
                        .graphicsLayer { rotationZ = 24f }
                        .clip(RoundedCornerShape(topStartPercent = 100, bottomEndPercent = 100))
                        .background(MascotLeafLight)
                )
            }
        }
    }
}
