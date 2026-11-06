package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler

@Composable
fun DetailDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleReminder: (() -> Unit)? = null,
    onTestReminder: (() -> Unit)? = null,
    onUpdateReleaseDate: ((Long) -> Unit)? = null,
    onUpdateRating: ((Int) -> Unit)? = null
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = p.surface,
            border = BorderStroke(1.dp, p.border)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(p.chip)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = p.muted, modifier = Modifier.size(18.dp))
                    }
                }

                Row {
                    Poster(item.poster, Modifier.size(width = 112.dp, height = 144.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.text)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SerieColors.Blue600.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) { Text(item.type.label, fontSize = 11.sp, color = SerieColors.Blue400) }
                            Spacer(Modifier.width(8.dp))
                            Text("Status: ${item.status.label}", fontSize = 12.sp, color = p.muted)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Episode: ${item.currentEp} / ${item.totalEp}",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text
                        )
                        if (item.type == MediaType.Series) {
                            Text("Season ${item.season}", fontSize = 12.sp, color = p.muted)
                        }
                        Spacer(Modifier.height(8.dp))
                        StarRatingBar(
                            rating = item.rating,
                            onRatingChange = onUpdateRating,
                            starSize = 18.dp,
                            showLabel = true
                        )
                    }
                }

                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.tags.forEach { tag ->
                            GenreTagBadge(tag)
                        }
                    }
                }

                if (item.status == WatchStatus.ComingSoon || item.releaseDate != null) {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Jadwal Tayang & Pengingat")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SerieColors.Orange.copy(alpha = 0.08f))
                            .border(1.dp, SerieColors.Orange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (item.reminderEnabled) SerieColors.Orange else p.muted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (item.reminderEnabled) "Alarm Pengingat Aktif" else "Alarm Pengingat Mati",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.reminderEnabled) SerieColors.Orange else p.muted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SerieColors.Orange.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    ReminderScheduler.timeUntilRelease(item.releaseDate),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SerieColors.Orange
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(p.chip)
                                .clickable {
                                    ReminderScheduler.showDateTimePicker(context, item.releaseDate) { newDate ->
                                        onUpdateReleaseDate?.invoke(newDate)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = "Kalender",
                                tint = SerieColors.Orange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    ReminderScheduler.formatReleaseDate(item.releaseDate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.text
                                )
                                Text(
                                    "Ketuk untuk ubah tanggal rilis di kalender",
                                    fontSize = 10.sp,
                                    color = p.muted
                                )
                            }
                            Icon(
                                Icons.Filled.EditCalendar,
                                contentDescription = "Ubah Tanggal",
                                tint = SerieColors.Orange,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onToggleReminder != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (item.reminderEnabled) p.chip else SerieColors.Orange.copy(alpha = 0.2f))
                                        .clickable(onClick = onToggleReminder)
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (item.reminderEnabled) "Matikan Alarm" else "Nyalakan Alarm",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (item.reminderEnabled) p.muted else SerieColors.Orange
                                    )
                                }
                            }
                            if (onTestReminder != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SerieColors.Blue600.copy(alpha = 0.18f))
                                        .clickable {
                                            onTestReminder()
                                            android.widget.Toast.makeText(context, "Notifikasi pengingat dikirim!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Uji Alarm Sekarang",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SerieColors.Blue400
                                    )
                                }
                            }
                        }
                    }
                }

                if (item.streamUrl.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Streaming Online")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600.copy(alpha = 0.2f))
                            .clickable { openUrl(context, item.streamUrl) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Public, null, tint = SerieColors.Blue400, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item.streamUrl,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SerieColors.Blue400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = SerieColors.Blue400, modifier = Modifier.size(16.dp))
                    }
                }

                if (item.videoPath.isNotBlank() || item.videoUri.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("Player Offline (3rd Party)")
                    Text(
                        "File: ${item.videoPath.ifBlank { "video" }}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SerieColors.Blue400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlayerButton("VLC Player", SerieColors.Orange, Modifier.weight(1f)) {
                            openVideo(context, item, VLC_PACKAGE)
                        }
                        PlayerButton("MX Player", SerieColors.Indigo500, Modifier.weight(1f)) {
                            openVideo(context, item, MX_PACKAGE)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    PlayerButton("Buka dengan player lain", p.muted, Modifier.fillMaxWidth()) {
                        openVideo(context, item, null)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { confirmDelete = true }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Delete, null, tint = SerieColors.Red, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hapus", fontSize = 12.sp, color = SerieColors.Red)
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600)
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text("Edit Detail", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = p.surface,
            titleContentColor = p.text,
            textContentColor = p.muted,
            title = { Text("Hapus media?") },
            text = { Text("Hapus \"${item.title}\" dari Seriee?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Hapus", color = SerieColors.Red) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Batal", color = p.muted) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val p = LocalPalette.current
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = p.muted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun PlayerButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
    }
}
