package com.seriee.app.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.R
import com.seriee.app.debug.AppLogger
import com.seriee.app.debug.CrashReport
import com.seriee.app.debug.LogEntry
import com.seriee.app.debug.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebugOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val logs by AppLogger.logs.collectAsState()
    var errorsOnly by remember { mutableStateOf(false) }
    var expandedIndex by remember { mutableStateOf<Int?>(0) }

    val displayed = remember(logs, errorsOnly) {
        if (errorsOnly) logs.filter { it.level == LogLevel.ERROR || it.level == LogLevel.CRASH || it.level == LogLevel.WARN }
        else logs
    }

    val panelBg = Color(0xEE11131A)
    val panelBorder = Color(0xFF334155)

    Surface(
        color = panelBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, panelBorder),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 380.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.BugReport, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${stringResource(R.string.debug_overlay_title)} (${displayed.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(AppLogger.formatAllLogs()))
                        Toast.makeText(context, context.getString(R.string.debug_logs_copied), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.debug_copy_logs), tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = {
                        AppLogger.clearLogs()
                        expandedIndex = null
                    },
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = stringResource(R.string.debug_clear_logs), tint = Color(0xFFFCA5A5), modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = Color(0xFFCBD5E1), modifier = Modifier.size(17.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OverlayFilterChip(
                    label = stringResource(R.string.debug_filter_all),
                    selected = !errorsOnly,
                    onClick = { errorsOnly = false },
                )
                OverlayFilterChip(
                    label = stringResource(R.string.debug_filter_errors),
                    selected = errorsOnly,
                    onClick = { errorsOnly = true },
                )
            }

            if (displayed.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                ) {
                    Text(stringResource(R.string.debug_no_logs), fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(displayed) { idx, entry ->
                        LogRow(
                            entry = entry,
                            expanded = expandedIndex == idx,
                            onToggle = {
                                expandedIndex = if (expandedIndex == idx) null else idx
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFF38BDF8) else Color(0xFF1E293B)
    val fg = if (selected) Color(0xFF091324) else Color(0xFFCBD5E1)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun LogRow(
    entry: LogEntry,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val levelColor = when (entry.level) {
        LogLevel.CRASH -> Color(0xFFFF4D6D)
        LogLevel.ERROR -> Color(0xFFF87171)
        LogLevel.WARN -> Color(0xFFFBBF24)
        LogLevel.INFO -> Color(0xFF34D399)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1E29))
            .border(1.dp, Color(0xFF293142), RoundedCornerShape(8.dp))
            .clickable(enabled = entry.stackTrace.isNotBlank(), onClick = onToggle)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = timeFmt.format(Date(entry.timestampMs)),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(levelColor.copy(alpha = 0.2f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(entry.level.name, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = levelColor)
            }
            Text(
                text = entry.tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2E8F0),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (entry.stackTrace.isNotBlank()) {
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = entry.message,
            fontSize = 11.sp,
            color = Color(0xFFF1F5F9),
            lineHeight = 15.sp,
        )
        if (expanded && entry.stackTrace.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0B0D13))
                    .horizontalScroll(rememberScrollState())
                    .padding(6.dp)
            ) {
                Text(
                    text = entry.stackTrace.trimEnd(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFCA5A5),
                    lineHeight = 13.sp,
                )
            }
        }
    }
}

@Composable
fun CrashErrorDialog(
    report: CrashReport,
    onDismiss: () -> Unit,
    onOpenOverlay: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showTrace by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
    ) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.danger,
            shadowOffset = 6.dp,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(p.danger.copy(alpha = 0.16f))
                            .border(1.5.dp, p.danger, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = p.danger, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.crash_dialog_title), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                        Text(
                            text = stringResource(R.string.crash_dialog_thread, report.threadName),
                            fontSize = 11.sp,
                            color = p.muted,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.crash_dialog_subtitle),
                    fontSize = 13.sp,
                    color = p.muted,
                    lineHeight = 19.sp,
                )

                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(report.exceptionType, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = p.danger)
                        Text(report.message, fontSize = 12.sp, color = p.text, lineHeight = 17.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showTrace = !showTrace }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        if (showTrace) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = p.accent,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(if (showTrace) R.string.crash_dialog_hide_trace else R.string.crash_dialog_show_trace),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.accent,
                    )
                }

                if (showTrace) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F1219))
                            .border(1.dp, p.border, RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = report.stackTrace.trimEnd(),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 14.sp,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val formatted = "${report.exceptionType}: ${report.message}\nThread: ${report.threadName}\n\n${report.stackTrace}"
                            clipboard.setText(AnnotatedString(formatted))
                            Toast.makeText(context, context.getString(R.string.crash_copied_toast), Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, p.border),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = p.text, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.crash_dialog_copy), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenOverlay,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, p.border),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.BugReport, contentDescription = null, tint = p.text, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.crash_dialog_open_overlay), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.crash_dialog_continue), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
