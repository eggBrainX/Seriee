package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.seriee.app.R
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchStatus
import java.io.File

@Composable
fun SerieeCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    cornerRadius: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val p = LocalPalette.current
    val bg = containerColor ?: p.card
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            containerColor = bg,
            borderColor = p.border,
            borderWidth = 2.dp,
            shadowOffset = 4.dp,
            cornerRadius = cornerRadius,
            modifier = modifier.then(clickMod),
            content = content,
        )
    } else {
        val shape = RoundedCornerShape(cornerRadius)
        Surface(
            color = bg,
            shape = shape,
            border = BorderStroke(if (p.isGlass) 1.dp else 1.dp, p.border),
            tonalElevation = if (p.isGlass) 0.dp else 1.dp,
            modifier = modifier
                .clip(shape)
                .then(clickMod),
        ) {
            Box(content = content)
        }
    }
}

@Composable
fun StatusBadge(
    status: WatchStatus,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    val c = statusColor(status, p)
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (p.isNeoBrutalist) c else c.copy(alpha = 0.16f))
            .then(if (p.isNeoBrutalist) Modifier.border(1.5.dp, p.border, shape) else Modifier)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = stringResource(status.labelRes),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (p.isNeoBrutalist) Color.White else c,
        )
    }
}

@Composable
fun GradientProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistProgressBar(fraction = fraction, color = color, modifier = modifier, height = height)
    } else {
        val shape = RoundedCornerShape(height / 2)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(p.chip)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(shape)
                    .background(color)
            )
        }
    }
}

@Composable
fun Poster(
    poster: String,
    type: MediaType,
    title: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)
    val borderMod = if (p.isNeoBrutalist) {
        Modifier.border(2.dp, p.border, shape)
    } else {
        Modifier.border(1.dp, p.border.copy(alpha = 0.5f), shape)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .background(p.cardAlt)
            .then(borderMod)
    ) {
        if (poster.isNotBlank()) {
            val modelData: Any = if (poster.startsWith("/")) File(poster) else poster
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(modelData)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = if (type == MediaType.Movie) Icons.Rounded.Movie else Icons.Rounded.Tv,
                    contentDescription = title,
                    tint = p.muted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun SheetDragHandle() {
    val p = LocalPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(p.muted.copy(alpha = 0.45f))
        )
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    hint: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    val p = LocalPalette.current
    SerieeCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.primaryContainer)
                    .then(if (p.isNeoBrutalist) Modifier.border(2.dp, p.border, RoundedCornerShape(14.dp)) else Modifier)
            ) {
                Icon(icon, contentDescription = null, tint = p.text, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = p.text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(hint, fontSize = 13.sp, color = p.muted, textAlign = TextAlign.Center, lineHeight = 19.sp)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                        border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(actionLabel, fontWeight = FontWeight.Bold)
                    }
                    if (secondaryLabel != null && onSecondary != null) {
                        OutlinedButton(
                            onClick = onSecondary,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border),
                        ) {
                            Text(secondaryLabel, color = p.text, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
