package com.seriee.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val chip = RoundedCornerShape(10.dp)
    val field = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(18.dp)
    val sheet = RoundedCornerShape(24.dp)
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
    const val quick = 180
    const val standard = 240
    const val emphasized = 320
}

/** Status colours shared by cards, badges and the detail sheet. */
@Composable
fun statusColor(status: WatchStatus): Color {
    val p = LocalPalette.current
    return when (status) {
        WatchStatus.Watching -> p.primary
        WatchStatus.Completed -> p.success
        WatchStatus.PlanToWatch -> p.muted
        WatchStatus.ComingSoon -> p.warning
        WatchStatus.OnHold -> Color(0xFFFB923C)
        WatchStatus.Dropped -> p.danger
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
        modifier = modifier
            .clip(shape)
            .background(bg)
            .then(if (level > 0) Modifier.border(1.dp, p.border, shape) else Modifier)
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
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.text)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = p.muted)
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
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(SerieeShape.chip)
            .background(p.chip)
            .border(1.dp, p.border, SerieeShape.chip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint ?: p.muted, modifier = Modifier.size(18.dp))
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
        Icon(icon, contentDescription = null, tint = tint ?: p.muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = p.text,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                value,
                fontSize = 12.sp,
                color = p.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(4.dp))
        }
        if (showChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = p.muted, modifier = Modifier.size(18.dp))
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SerieeShape.field)
            .background(if (selected) p.primary.copy(alpha = 0.12f) else p.chip)
            .border(1.dp, if (selected) p.primary.copy(alpha = 0.55f) else p.border, SerieeShape.field)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SerieeShape.field)
            .background(p.chip)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(SerieeShape.chip)
                    .background(if (active) p.primary else Color.Transparent)
                    .selectable(selected = active, role = Role.Tab) { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    color = if (active) p.onAccent else p.muted,
                    maxLines = 1
                )
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
    val brush = if (gradient) {
        Brush.horizontalGradient(listOf(p.primary, p.accent))
    } else {
        Brush.horizontalGradient(listOf(p.primary, p.primary))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(SerieeShape.field)
            .background(if (enabled) brush else Brush.horizontalGradient(listOf(p.chip, p.chip)))
            .clickable(enabled = enabled, onClick = onClick)
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(SerieeShape.field)
            .background(p.chip)
            .border(1.dp, p.border, SerieeShape.field)
            .clickable(onClick = onClick)
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
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(p.chip)
                .border(1.dp, p.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = p.accent, modifier = Modifier.size(26.dp))
        }
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.text, textAlign = TextAlign.Center)
        Text(hint, fontSize = 12.sp, color = p.muted, textAlign = TextAlign.Center, lineHeight = 17.sp)
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(SerieeShape.field)
                    .background(p.primary)
                    .clickable(onClick = onCta)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
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
            .padding(top = 10.dp, bottom = 6.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(p.border)
    )
}

@Composable
fun GradientProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    track: Color = LocalPalette.current.chip
) {
    val p = LocalPalette.current
    val brush = Brush.horizontalGradient(colors ?: listOf(p.primary, p.accent))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(brush)
        )
    }
}

@Composable
fun StatusBadge(status: WatchStatus, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
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
