package com.seriee.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import com.seriee.app.reminder.ReminderScheduler
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchStatus

@Composable
fun MediaFormDialog(
    initial: MediaItem?,
    onDismiss: () -> Unit,
    onSave: (MediaItem) -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: MediaType.Series) }
    var status by remember { mutableStateOf(initial?.status ?: WatchStatus.Watching) }
    var poster by remember { mutableStateOf(initial?.poster ?: "") }
    var season by remember { mutableStateOf((initial?.season ?: 1).toString()) }
    var currentEp by remember { mutableStateOf((initial?.currentEp ?: 1).toString()) }
    var totalEp by remember { mutableStateOf((initial?.totalEp ?: 12).toString()) }
    var streamUrl by remember { mutableStateOf(initial?.streamUrl ?: "") }
    var videoPath by remember { mutableStateOf(initial?.videoPath ?: "") }
    var videoUri by remember { mutableStateOf(initial?.videoUri ?: "") }
    var releaseDate by remember { mutableStateOf(initial?.releaseDate) }
    var reminderEnabled by remember {
        mutableStateOf(initial?.reminderEnabled ?: (initial?.status == WatchStatus.ComingSoon))
    }
    var tags by remember { mutableStateOf(initial?.tags ?: emptyList()) }
    var rating by remember { mutableStateOf(initial?.rating ?: 0) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // permission may not be persistable for some providers; ignore
            }
            videoUri = uri.toString()
            videoPath = displayName(context, uri) ?: uri.lastPathSegment ?: "video"
        }
    }

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
            border = androidx.compose.foundation.BorderStroke(1.dp, p.border)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (initial == null) "Tambah Ke Seriee" else "Edit Media",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.text,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = p.muted)
                    }
                }

                Column {
                    FieldLabel("Judul Film / Serial *")
                    SField(title, { title = it }, placeholder = "Contoh: Arcane")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownField(
                        label = "Tipe",
                        valueLabel = type.label,
                        options = MediaType.values().map { it to it.label },
                        onSelect = { type = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = "Status",
                        valueLabel = status.label,
                        options = WatchStatus.values().map { it to it.label },
                        onSelect = {
                            status = it
                            if (it == WatchStatus.ComingSoon) {
                                reminderEnabled = true
                                if (releaseDate == null || releaseDate!! <= System.currentTimeMillis()) {
                                    releaseDate = System.currentTimeMillis() + 3 * 86400000L
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rating Bintang (1 - 5 Stars)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FieldLabel("Rating Series / Film")
                    StarRatingBar(
                        rating = rating,
                        onRatingChange = { rating = it },
                        starSize = 26.dp
                    )
                }

                // Tag / Genre Input
                TagInputField(
                    tags = tags,
                    onTagsChanged = { tags = it }
                )

                // Coming Soon & Jadwal Tayang / Alarm Reminder
                val showReminderSection = status == WatchStatus.ComingSoon || status == WatchStatus.PlanToWatch || releaseDate != null
                if (showReminderSection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (status == WatchStatus.ComingSoon) SerieColors.Orange.copy(alpha = 0.08f) else p.card)
                            .border(1.dp, if (status == WatchStatus.ComingSoon) SerieColors.Orange.copy(alpha = 0.35f) else p.border, RoundedCornerShape(16.dp))
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
                                    imageVector = if (reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.Alarm,
                                    contentDescription = null,
                                    tint = if (reminderEnabled) SerieColors.Orange else p.muted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Alarm Pengingat Tayang",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.text
                                    )
                                    Text(
                                        if (reminderEnabled) "Notifikasi aktif saat waktu tayang tiba" else "Notifikasi alarm mati",
                                        fontSize = 11.sp,
                                        color = p.muted
                                    )
                                }
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = {
                                    reminderEnabled = it
                                    if (it && releaseDate == null) {
                                        releaseDate = System.currentTimeMillis() + 3 * 86400000L
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SerieColors.Orange
                                )
                            )
                        }

                        if (reminderEnabled || status == WatchStatus.ComingSoon) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Jadwal & Tanggal Rilis:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.muted)

                                // Tombol Pilih Tanggal di Kalender
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SerieColors.Orange.copy(alpha = 0.15f))
                                        .border(1.dp, SerieColors.Orange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            ReminderScheduler.showDateTimePicker(context, releaseDate) { selected ->
                                                releaseDate = selected
                                                reminderEnabled = true
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = SerieColors.Orange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Pilih Tanggal Rilis di Kalender",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SerieColors.Orange
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Preset:", fontSize = 11.sp, color = p.muted)
                                    val now = System.currentTimeMillis()
                                    PresetChip("Besok") { releaseDate = now + 86400000L }
                                    PresetChip("3 Hari") { releaseDate = now + 3 * 86400000L }
                                    PresetChip("1 Minggu") { releaseDate = now + 7 * 86400000L }
                                    PresetChip("1 Bulan") { releaseDate = now + 30 * 86400000L }
                                }

                                if (releaseDate != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(p.chip)
                                            .border(1.dp, p.border, RoundedCornerShape(10.dp))
                                            .clickable {
                                                ReminderScheduler.showDateTimePicker(context, releaseDate) { selected ->
                                                    releaseDate = selected
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.EditCalendar,
                                            contentDescription = "Ganti Tanggal",
                                            tint = SerieColors.Orange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                ReminderScheduler.formatReleaseDate(releaseDate),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = p.text
                                            )
                                            Text(
                                                "Ketuk untuk ubah tanggal/jam rilis",
                                                fontSize = 10.sp,
                                                color = p.muted
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SerieColors.Orange.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                ReminderScheduler.timeUntilRelease(releaseDate),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SerieColors.Orange
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    FieldLabel("URL Poster Cover (Opsional)")
                    SField(poster, { poster = it }, placeholder = "https://...", keyboardType = KeyboardType.Uri)
                }

                if (type == MediaType.Series) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            FieldLabel("Season")
                            SField(season, { season = it.filter(Char::isDigit).take(3) }, keyboardType = KeyboardType.Number)
                        }
                        Column(Modifier.weight(1f)) {
                            FieldLabel("Ep Saat Ini")
                            SField(currentEp, { currentEp = it.filter(Char::isDigit).take(4) }, keyboardType = KeyboardType.Number)
                        }
                        Column(Modifier.weight(1f)) {
                            FieldLabel("Total Ep")
                            SField(totalEp, { totalEp = it.filter(Char::isDigit).take(4) }, keyboardType = KeyboardType.Number)
                        }
                    }
                }

                // Streaming link
                Column {
                    FieldLabel("Link Streaming / Aplikasi (Opsional)")
                    SField(
                        streamUrl, { streamUrl = it },
                        placeholder = "https://www.netflix.com/title/...",
                        keyboardType = KeyboardType.Uri
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Preset:", fontSize = 11.sp, color = p.muted)
                        PresetChip("Netflix") { streamUrl = "https://www.netflix.com" }
                        PresetChip("Bilibili") { streamUrl = "https://www.bilibili.tv" }
                        PresetChip("YouTube") { streamUrl = "https://www.youtube.com" }
                    }
                }

                // Offline file
                Column {
                    FieldLabel("File Video Offline / Local Path (Opsional)")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SField(
                            videoPath, { videoPath = it },
                            placeholder = "Nama video...",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(p.chip)
                                .border(1.dp, p.border, RoundedCornerShape(12.dp))
                                .clickable { picker.launch(arrayOf("video/*")) }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.FolderOpen, null, tint = SerieColors.Blue400, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pilih File", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                        }
                    }
                }

                error?.let { Text(it, color = SerieColors.Red, fontSize = 12.sp) }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.chip)
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 18.dp, vertical = 11.dp)
                    ) { Text("Batal", color = p.muted, fontSize = 14.sp) }

                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600)
                            .clickable {
                                if (title.isBlank()) {
                                    error = "Judul wajib diisi"
                                } else {
                                    val total = if (type == MediaType.Movie) 1
                                    else (totalEp.toIntOrNull() ?: 1).coerceAtLeast(1)
                                    val current = (currentEp.toIntOrNull() ?: 1).coerceIn(0, total)
                                    val seasonNo = (season.toIntOrNull() ?: 1).coerceAtLeast(1)
                                    val base = initial ?: MediaItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = "", type = type, status = status, poster = ""
                                    )
                                    onSave(
                                        base.copy(
                                            title = title.trim(),
                                            type = type,
                                            status = status,
                                            poster = poster.trim(),
                                            season = seasonNo,
                                            currentEp = current,
                                            totalEp = total,
                                            streamUrl = streamUrl.trim(),
                                            videoPath = videoPath.trim(),
                                            videoUri = videoUri,
                                            releaseDate = if (status == WatchStatus.ComingSoon || reminderEnabled) releaseDate else null,
                                            reminderEnabled = reminderEnabled && (releaseDate != null),
                                            tags = tags,
                                            rating = rating,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                            .padding(horizontal = 22.dp, vertical = 11.dp)
                    ) { Text("Simpan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(p.chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) { Text(label, fontSize = 11.sp, color = p.text) }
}
