package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seriee.app.R

object NeoBrutalistColors {
    val Black = Color(0xFF121212)
    val Cream = Color(0xFFFFF8E7)
    val CardWhite = Color(0xFFFFFFFF)
    val SunYellow = Color(0xFFFFD60A)
    val CoralPink = Color(0xFFFF4D6D)
    val ElectricBlue = Color(0xFF3A86FF)
    val MintGreen = Color(0xFF06D6A0)
    val VividPurple = Color(0xFF8338EC)
    val WarmOrange = Color(0xFFFF9F1C)
    val MutedGray = Color(0xFF5A5A5A)

    val DarkBg = Color(0xFF141418)
    val DarkCard = Color(0xFF1E1E24)
    val DarkCardAlt = Color(0xFF282832)
    val DarkBorder = Color(0xFFF4F1DE)
    val DarkText = Color(0xFFFDFCF5)
    val DarkMuted = Color(0xFFB5B2A8)
}

@Composable
fun NeoBrutalistBox(
    modifier: Modifier = Modifier,
    containerColor: Color = LocalPalette.current.card,
    borderColor: Color = LocalPalette.current.border,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    cornerRadius: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val shadowColor = if (LocalPalette.current.isLight) NeoBrutalistColors.Black else Color(0xFF000000)
    Box(
        modifier = modifier
            .drawBehind {
                val off = shadowOffset.toPx()
                val rad = cornerRadius.toPx()
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(off, off),
                    size = size,
                    cornerRadius = CornerRadius(rad, rad),
                )
            }
            .clip(shape)
            .background(containerColor, shape)
            .border(BorderStroke(borderWidth, borderColor), shape),
        content = content,
    )
}

@Composable
fun NeoBrutalistProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(p.chip)
            .border(1.5.dp, p.border, shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

@Composable
fun NeoBrutalistConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color = LocalPalette.current.danger,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val p = LocalPalette.current
    Dialog(onDismissRequest = onDismiss) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.border,
            shadowOffset = 6.dp,
            cornerRadius = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 14.sp, color = p.muted, lineHeight = 20.sp)
                Spacer(Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, p.border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel), color = p.text, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor, contentColor = Color.White),
                        border = BorderStroke(2.dp, p.border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(confirmText, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
