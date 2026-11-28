package com.seriee.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen splash shown once per cold start. The logo animation is Egi's
 * own Lottie file (assets/splash_logo.json, 1.5 s, plays once); Aya's part is
 * the layout and the "Seriee" wordmark, whose letters spring in one by one
 * under the logo. The lockup is one centred column so logo and text always
 * line up. Deliberately NOT clickable: touches pass through to the UI
 * underneath (a covering clickable layer breaks the MainActivity UI tests,
 * which tap through the splash).
 */
@Composable
fun SerieeSplash(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("splash_logo.json"))
    val lottieState = animateLottieCompositionAsState(composition = composition, iterations = 1)

    val letters = remember { "Seriee".map { Animatable(0f) } }
    LaunchedEffect(Unit) {
        launch {
            delay(500)
            letters.forEachIndexed { i, anim ->
                launch {
                    delay(i * 55L)
                    anim.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
                }
            }
        }
        // Safety net: never trap the user (or a test) on the splash if the
        // Lottie composition fails to load or finish.
        launch {
            delay(2800)
            onFinished()
        }
    }
    LaunchedEffect(lottieState.isAtEnd) {
        if (lottieState.isAtEnd) {
            delay(300)
            onFinished()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.bg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { lottieState.progress },
            modifier = Modifier.size(272.dp)
        )
        Spacer(Modifier.height(10.dp))
        Row {
            "Seriee".forEachIndexed { i, c ->
                val a = letters[i].value
                Text(
                    c.toString(),
                    style = TextStyle(
                        color = p.text,
                        fontSize = 40.sp,
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
