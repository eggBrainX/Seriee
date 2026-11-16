package com.seriee.app.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.Season
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (WatchStatus) -> Unit,
    onRatingChange: (Int) -> Unit,
    onSeasonEpisodeChange: (Int, Int) -> Unit,
    onToggleReminder: () -> Unit,
    onReleaseDateChange: (Long?) -> Unit,
    onTestReminder: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var jumpPickerSeason by remember { mutableStateOf<Season?>(null) }

    var pendingWatchedEpisode by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var launchedExternalPlayer by remember { mutableStateOf(false) }
    var showWatchedPrompt by remember { mutableStateOf(false) }
    var resumeTick by remember { mutableIntStateOf(0) }

    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeTick++
                if (launchedExternalPlayer && pendingWatchedEpisode != null) {
                    launchedExternalPlayer = false
                    showWatchedPrompt = true
                }
            }
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    val hasExactAlarmPermission = remember(item.reminderEnabled, resumeTick) {
        ReminderScheduler.canScheduleExactAlarms(context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.surface,
        contentColor = p.text,
        dragHandle = { SheetDragHandle() },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Poster(
                    poster = item.poster,
                    type = item.type,
                    title = item.title,
                    modifier = Modifier.size(width = 92.dp, height = 134.dp),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = item.title,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                        }
                    }
                    StatusBadge(status = item.status)
                    Text(
                        text = if (item.type == MediaType.Movie) stringResource(R.string.type_movie)
                        else stringResource(R.string.episode_colon, item.watchedEpisodes, item.totalEpisodes),
                        fontSize = 13.sp,
                        color = p.muted,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (item.tags.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(p.chip)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(tag, fontSize = 11.sp, color = p.text, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            if (item.description.isNotBlank()) {
                Text(item.description, fontSize = 13.sp, color = p.muted, lineHeight = 19.sp)
            }

            val pending = pendingWatchedEpisode
            if (showWatchedPrompt && pending != null) {
                SerieeCard(containerColor = p.accent.copy(alpha = 0.14f), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = p.accent, modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.watched_prompt_title, pending.first, pending.second),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                onSeasonEpisodeChange(pending.first, pending.second)
                                showWatchedPrompt = false
                                pendingWatchedEpisode = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                        ) {
                            Text(stringResource(R.string.watched_prompt_action), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        IconButton(
                            onClick = {
                                showWatchedPrompt = false
                                pendingWatchedEpisode = null
                            },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.status_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatchStatus.entries.forEach { st ->
                        FilterChipButton(
                            label = stringResource(st.labelRes),
                            selected = item.status == st,
                            accent = statusColor(st, p),
                            onClick = { onStatusChange(st) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.rating_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                StarRatingBar(rating = item.rating, onRatingChanged = onRatingChange)
            }

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (item.type == MediaType.Movie) {
                        val watched = item.currentEp > 0
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (watched) stringResource(R.string.movie_watched) else stringResource(R.string.movie_not_watched),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.text,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = { onSeasonEpisodeChange(1, if (watched) 0 else 1) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                            ) {
                                Text(if (watched) stringResource(R.string.movie_not_watched) else stringResource(R.string.movie_watched), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        item.effectiveSeasons.forEach { sec ->
                            val frac = if (sec.totalEp <= 0) 0f else (sec.currentEp.toFloat() / sec.totalEp.toFloat()).coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.season_format, sec.number),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = p.text,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = { onSeasonEpisodeChange(sec.number, (sec.currentEp - 1).coerceAtLeast(0)) },
                                        enabled = sec.currentEp > 0,
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Icon(Icons.Rounded.Remove, contentDescription = null, tint = p.text)
                                    }
                                    Text(
                                        text = "${sec.currentEp} / ${sec.totalEp}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = p.text,
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                    )
                                    IconButton(
                                        onClick = { onSeasonEpisodeChange(sec.number, (sec.currentEp + 1).coerceAtMost(sec.totalEp)) },
                                        enabled = sec.currentEp < sec.totalEp,
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, tint = p.text)
                                    }
                                }
                                GradientProgressBar(fraction = frac, color = p.accent, height = 8.dp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { jumpPickerSeason = sec },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.2.dp, p.border),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Rounded.FastForward, contentDescription = null, tint = p.text, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(R.string.jump_to_episode), color = p.text, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = { onSeasonEpisodeChange(sec.number, sec.totalEp) },
                                        enabled = sec.currentEp < sec.totalEp,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.2.dp, p.border),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Rounded.DoneAll, contentDescription = null, tint = if (sec.currentEp < sec.totalEp) p.accent else p.muted, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            stringResource(R.string.mark_season_complete),
                                            color = if (sec.currentEp < sec.totalEp) p.text else p.muted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                        )
                                    }
                                }
                                if (sec.episodes.isNotEmpty()) {
                                    val nextEp = sec.episodes.firstOrNull { it.number == (sec.currentEp + 1).coerceAtMost(sec.totalEp) }
                                        ?: sec.episodes.firstOrNull()
                                    if (nextEp != null) {
                                        OutlinedButton(
                                            onClick = {
                                                pendingWatchedEpisode = sec.number to nextEp.number
                                                launchedExternalPlayer = true
                                                openVideoUri(context, nextEp.uri)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.5.dp, p.border),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = p.text, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "Play E${nextEp.number}: ${nextEp.displayName}",
                                                color = p.text,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.schedule_and_reminder), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = ReminderScheduler.formatReleaseDate(context, item.releaseDate),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.text,
                            )
                            Text(
                                text = ReminderScheduler.countdownDetailedText(context, item.releaseDate),
                                fontSize = 12.sp,
                                color = p.muted,
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                ReminderScheduler.showDateTimePicker(context, item.releaseDate) { ms ->
                                    onReleaseDateChange(ms)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, p.border),
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = p.text, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(stringResource(R.string.calendar), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onToggleReminder,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, p.border),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                if (item.reminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = if (item.reminderEnabled) p.reminder else p.muted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (item.reminderEnabled) stringResource(R.string.turn_off_alarm) else stringResource(R.string.turn_on_alarm),
                                color = p.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                onTestReminder()
                                Toast.makeText(context, context.getString(R.string.notification_sent), Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, p.border),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.test_alarm_now), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (item.reminderEnabled && !hasExactAlarmPermission) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(p.warning.copy(alpha = 0.16f))
                                .padding(10.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = p.warning, modifier = Modifier.size(16.dp))
                                Text(
                                    text = stringResource(R.string.exact_alarm_disabled_hint),
                                    fontSize = 12.sp,
                                    color = p.text,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 16.sp,
                                )
                            }
                            OutlinedButton(
                                onClick = { ReminderScheduler.openExactAlarmSettings(context) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, p.border),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.enable_exact_alarm), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (item.streamUrl.isNotBlank() || item.videoUri.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.streamUrl.isNotBlank()) {
                        Button(
                            onClick = { openUrl(context, item.streamUrl) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                            border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.streaming_online), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (item.videoUri.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                if (item.type == MediaType.Movie) {
                                    pendingWatchedEpisode = 1 to 1
                                } else {
                                    val targetSec = item.effectiveSeasons.firstOrNull { it.currentEp < it.totalEp }
                                        ?: item.effectiveSeasons.firstOrNull()
                                    if (targetSec != null) {
                                        pendingWatchedEpisode = targetSec.number to (targetSec.currentEp + 1).coerceAtMost(targetSec.totalEp)
                                    }
                                }
                                launchedExternalPlayer = true
                                openVideo(context, item)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = p.text, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.open_with_other_player) + " · " + item.videoPath.ifBlank { "Video" },
                                color = p.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = p.text, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.edit_details), color = p.text, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showDeleteConfirm = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.danger, contentColor = Color.White),
                    border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.delete), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    jumpPickerSeason?.let { sec ->
        JumpToEpisodeDialog(
            season = sec,
            onDismiss = { jumpPickerSeason = null },
            onSelectEpisode = { targetEp ->
                onSeasonEpisodeChange(sec.number, targetEp)
                jumpPickerSeason = null
            },
        )
    }

    if (showDeleteConfirm) {
        NeoBrutalistConfirmDialog(
            title = stringResource(R.string.delete_media_title),
            message = stringResource(R.string.delete_media_message, item.title),
            confirmText = stringResource(R.string.delete),
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                onDelete()
                onDismiss()
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JumpToEpisodeDialog(
    season: Season,
    onDismiss: () -> Unit,
    onSelectEpisode: (Int) -> Unit,
) {
    val p = LocalPalette.current
    var inputText by remember(season.number, season.currentEp) {
        mutableStateOf(season.currentEp.toString())
    }
    val total = season.totalEp.coerceAtLeast(1)

    Dialog(onDismissRequest = onDismiss) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.border,
            shadowOffset = 6.dp,
            cornerRadius = 14.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Text(
                    text = "${stringResource(R.string.jump_to_episode)} · ${stringResource(R.string.season_format, season.number)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                )

                if (total <= 40) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        for (ep in 0..total) {
                            val selected = ep == season.currentEp
                            val watched = ep in 1..season.currentEp
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            selected -> p.accent
                                            watched -> p.accent.copy(alpha = 0.2f)
                                            else -> p.cardAlt
                                        }
                                    )
                                    .border(1.2.dp, if (selected) p.border else p.border.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onSelectEpisode(ep) },
                            ) {
                                Text(
                                    text = "$ep",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selected) p.onAccent else p.text,
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { v -> inputText = v.filter { it.isDigit() }.take(4) },
                        label = { Text("0 – $total", fontSize = 12.sp, color = p.muted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.accent,
                            unfocusedBorderColor = p.border,
                            focusedTextColor = p.text,
                            unfocusedTextColor = p.text,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, total / 4, total / 2, (total * 3) / 4, total).distinct().forEach { milestone ->
                            FilterChipButton(
                                label = "E$milestone",
                                selected = season.currentEp == milestone,
                                accent = p.accent,
                                onClick = { onSelectEpisode(milestone) },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, p.border),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.cancel), color = p.text, fontWeight = FontWeight.Bold)
                    }
                    if (total > 40) {
                        Button(
                            onClick = {
                                val parsed = (inputText.toIntOrNull() ?: season.currentEp).coerceIn(0, total)
                                onSelectEpisode(parsed)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.save), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
