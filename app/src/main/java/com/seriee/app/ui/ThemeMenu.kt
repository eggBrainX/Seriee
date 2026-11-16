package com.seriee.app.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.AppThemeMode
import com.seriee.app.data.AppThemeStyle
import com.seriee.app.data.CustomThemeConfig
import com.seriee.app.data.MediaItem
import com.seriee.app.data.ThemePreset
import com.seriee.app.data.ThemePresetCodec
import kotlinx.coroutines.launch

private val ACCENT_SWATCHES = listOf(
    0xFFFFD60AL,
    0xFF3A86FFL,
    0xFFFF4D6DL,
    0xFF06D6A0L,
    0xFF8338ECL,
    0xFFFF9F1CL,
    0xFF0EA5E9L,
    0xFFEC4899L,
)

private val BG_PRESETS = listOf(
    Triple(0xFFFFF8E7L, 0xFFFFE5B4L, "Cream"),
    Triple(0xFF0F172AL, 0xFF1E293BL, "Slate"),
    Triple(0xFF120B29L, 0xFF2B1055L, "Midnight"),
    Triple(0xFF091A16L, 0xFF12352BL, "Forest"),
    Triple(0xFFF3E8FFL, 0xFFE0F2FEL, "Pastel"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeMenu(
    mode: AppThemeMode,
    style: AppThemeStyle,
    custom: CustomThemeConfig,
    reduceTransparency: Boolean,
    batteryAmoled: Boolean,
    items: List<MediaItem>,
    onModeChange: (AppThemeMode) -> Unit,
    onStyleChange: (AppThemeStyle) -> Unit,
    onCustomAccentChange: (Long) -> Unit,
    onCustomBackgroundChange: (Long, Long, Boolean) -> Unit,
    onApplyPreset: (ThemePreset) -> Unit,
    onReduceTransparencyChange: (Boolean) -> Unit,
    onBatteryAmoledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customizeOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.surface,
        contentColor = p.text,
        dragHandle = { SheetDragHandle() },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
        ) {
            if (!customizeOpen) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = p.accent)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.theme), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                    }
                }

                Text(stringResource(R.string.theme_appearance), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AppThemeMode.entries.forEach { m ->
                        val selected = mode == m
                        FilterChipButton(
                            label = stringResource(m.labelRes),
                            selected = selected,
                            accent = p.accent,
                            onClick = { onModeChange(m) },
                        )
                    }
                }

                Text(stringResource(R.string.theme_style), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.muted)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeStyle.entries.forEach { s ->
                        if (s == AppThemeStyle.MaterialYou && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@forEach
                        val selected = style == s
                        SerieeCard(
                            containerColor = if (selected) p.primaryContainer else p.cardAlt,
                            onClick = {
                                onStyleChange(s)
                                if (s == AppThemeStyle.Custom) customizeOpen = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                RadioButton(selected = selected, onClick = {
                                    onStyleChange(s)
                                    if (s == AppThemeStyle.Custom) customizeOpen = true
                                })
                                Column(Modifier.weight(1f)) {
                                    Text(stringResource(s.labelRes), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                                    Text(stringResource(s.descRes), fontSize = 12.sp, color = p.muted)
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        onStyleChange(AppThemeStyle.Custom)
                        customizeOpen = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.ColorLens, contentDescription = null, tint = p.text, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.theme_customize), color = p.text, fontWeight = FontWeight.Bold)
                }

                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.theme_reduce_transparency), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                                Text(stringResource(R.string.theme_reduce_transparency_desc), fontSize = 11.sp, color = p.muted)
                            }
                            Switch(checked = reduceTransparency, onCheckedChange = onReduceTransparencyChange)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.theme_battery_amoled), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                                Text(stringResource(R.string.theme_battery_amoled_desc), fontSize = 11.sp, color = p.muted)
                            }
                            Switch(checked = batteryAmoled, onCheckedChange = onBatteryAmoledChange)
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { customizeOpen = false }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back), tint = p.text)
                    }
                    Text(stringResource(R.string.customize_theme_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                    }
                }

                Text(stringResource(R.string.accent), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.muted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ACCENT_SWATCHES.forEach { argb ->
                        val c = Color(argb)
                        val selected = custom.accentArgb == argb
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(if (selected) 3.dp else 1.5.dp, p.border, CircleShape)
                                .clickable { onCustomAccentChange(argb) }
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (c.luminance() > 0.45f) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                val samplePoster = remember(items) { items.firstOrNull { it.poster.isNotBlank() }?.poster }
                if (samplePoster != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val extracted = accentFromPoster(context, samplePoster)
                                if (extracted != null) {
                                    onCustomAccentChange(extracted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, p.border),
                    ) {
                        Text(stringResource(R.string.theme_accent_from_artwork), color = p.text, fontWeight = FontWeight.Bold)
                    }
                }

                val accentLum = Color(custom.accentArgb).luminance()
                val bgLum = Color(custom.bgStartArgb).luminance()
                if (kotlin.math.abs(accentLum - bgLum) < 0.14f) {
                    Text(stringResource(R.string.theme_accent_low_contrast), fontSize = 12.sp, color = p.warning, fontWeight = FontWeight.Bold)
                }

                Text(stringResource(R.string.background), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipButton(
                        label = stringResource(R.string.background_solid),
                        selected = !custom.useGradient,
                        accent = p.accent,
                        onClick = { onCustomBackgroundChange(custom.bgStartArgb, custom.bgEndArgb, false) },
                    )
                    FilterChipButton(
                        label = stringResource(R.string.background_gradient),
                        selected = custom.useGradient,
                        accent = p.accent,
                        onClick = { onCustomBackgroundChange(custom.bgStartArgb, custom.bgEndArgb, true) },
                    )
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BG_PRESETS.forEach { (s, e, label) ->
                        val selected = custom.bgStartArgb == s && custom.bgEndArgb == e
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(s), Color(e))))
                                .border(if (selected) 3.dp else 1.5.dp, p.border, RoundedCornerShape(10.dp))
                                .clickable { onCustomBackgroundChange(s, e, custom.useGradient) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (Color(s).luminance() > 0.45f) Color.Black else Color.White,
                            )
                        }
                    }
                }

                Text(stringResource(R.string.theme_preview), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.muted)
                SerieeCard(containerColor = p.card, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.theme_preview_card_title), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                            Spacer(Modifier.height(4.dp))
                            StatusBadge(status = com.seriee.app.data.WatchStatus.Watching)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(custom.accentArgb))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.theme_preview_chip),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (Color(custom.accentArgb).luminance() > 0.45f) Color.Black else Color.White,
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val code = ThemePresetCodec.encode(
                                ThemePreset(
                                    mode = mode,
                                    style = style,
                                    accentArgb = custom.accentArgb,
                                    bgStartArgb = custom.bgStartArgb,
                                    bgEndArgb = custom.bgEndArgb,
                                    useGradient = custom.useGradient,
                                )
                            )
                            clipboard.setText(AnnotatedString(code))
                            Toast.makeText(context, context.getString(R.string.theme_code_copied), Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, p.border),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = p.text, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(stringResource(R.string.theme_copy_code), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val raw = clipboard.getText()?.text ?: ""
                            val decoded = ThemePresetCodec.decode(raw)
                            if (decoded != null) {
                                onApplyPreset(decoded)
                                Toast.makeText(context, context.getString(R.string.theme_code_applied), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.theme_code_invalid), Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(stringResource(R.string.theme_paste_code), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
