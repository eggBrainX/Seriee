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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleReminder: (() -> Unit)? = null,
    onTestReminder: (() -> Unit)? = null,
    onUpdateReleaseDate: ((Long) -> Unit)? = null,
    onUpdateRating: ((Int) -> Unit)? = null,
    onSelectEpisode: ((Int, Int) -> Unit)? = null
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    var selectedSeasonIdx by remember { mutableIntStateOf(0) }
    val seasons = item.effectiveSeasons
    LaunchedEffect(seasons.size) {
        if (selectedSeasonIdx >= seasons.size) selectedSeasonIdx = 0
    }
    val safeSeasonIdx = selectedSeasonIdx.coerceIn(0, (seasons.size - 1).coerceAtLeast(0))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = p.surface,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = { SheetDragHandle() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
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
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = p.muted, modifier = Modifier.size(18.dp))
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
                            ) { Text(stringResource(item.type.labelRes), fontSize = 11.sp, color = SerieColors.Blue400) }
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(item.status)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (item.type == MediaType.Movie) {
                                stringResource(if (item.watchedEpisodes > 0) R.string.movie_watched else R.string.movie_not_watched)
                            } else {
                                stringResource(R.string.episode_colon, item.watchedEpisodes, item.totalEpisodes)
                            },
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text
                        )
                        if (item.type == MediaType.Series) {
                            val active = seasons.getOrNull(safeSeasonIdx) ?: seasons.firstOrNull()
                            if (active != null) {
                                Text(stringResource(R.string.season_format, active.number), fontSize = 12.sp, color = p.muted)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        StarRatingBar(
                            rating = item.rating,
                            onRatingChange = onUpdateRating,
                            starSize = 16.dp,
                            showLabel = true,
                            labelBelow = true
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

                if (item.description.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    SectionTitle(stringResource(R.string.description_label))
                    Text(
                        item.description,
                        fontSize = 12.sp, color = p.text, lineHeight = 17.sp
                    )
                }

                if (item.status == WatchStatus.ComingSoon || item.releaseDate != null) {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle(stringResource(R.string.schedule_and_reminder))
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
                                    if (item.reminderEnabled) stringResource(R.string.reminder_alarm_on) else stringResource(R.string.reminder_alarm_off),
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
                                    ReminderScheduler.timeUntilRelease(context, item.releaseDate),
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
                                contentDescription = stringResource(R.string.calendar),
                                tint = SerieColors.Orange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    ReminderScheduler.formatReleaseDate(context, item.releaseDate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.text
                                )
                                Text(
                                    stringResource(R.string.tap_to_change_release_calendar),
                                    fontSize = 10.sp,
                                    color = p.muted
                                )
                            }
                            Icon(
                                Icons.Filled.EditCalendar,
                                contentDescription = stringResource(R.string.change_date),
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
                                        if (item.reminderEnabled) stringResource(R.string.turn_off_alarm) else stringResource(R.string.turn_on_alarm),
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
                                            android.widget.Toast.makeText(context, context.getString(R.string.notification_sent), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(R.string.test_alarm_now),
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
                    SectionTitle(stringResource(R.string.streaming_online))
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

                val hasOffline = seasons.any { it.episodes.isNotEmpty() } || item.videoUri.isNotBlank() || item.videoPath.isNotBlank()
                if (hasOffline) {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle(stringResource(R.string.offline_player))

                    if (item.type == MediaType.Series && seasons.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            seasons.forEachIndexed { idx, s ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (idx == safeSeasonIdx) SerieColors.Blue600 else p.chip)
                                        .clickable { selectedSeasonIdx = idx }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.season_format, s.number),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        color = if (idx == safeSeasonIdx) Color.White else p.muted
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (item.type == MediaType.Series) {
                        val season = seasons.getOrNull(safeSeasonIdx) ?: seasons.firstOrNull()
                        if (season != null) {
                            // Map lookup avoids O(n^2) find-in-loop for 1000+ ep shows.
                            val episodeByNumber = remember(season.episodes) {
                                season.episodes.associateBy { it.number }
                            }
                            val totalEpSafe = season.totalEp.coerceAtLeast(1)
                            val epListState = rememberLazyListState()
                            // Start at current episode instead of E1.
                            LaunchedEffect(season.number, season.currentEp, totalEpSafe) {
                                val targetIdx = (season.currentEp - 1).coerceIn(0, (totalEpSafe - 1).coerceAtLeast(0))
                                epListState.scrollToItem(targetIdx)
                            }
                            Text(
                                stringResource(R.string.select_episode_title),
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.muted
                            )
                            Spacer(Modifier.height(6.dp))
                            // LazyRow composes only visible chips — smooth for 100+ / 1000+ episodes.
                            LazyRow(
                                state = epListState,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(count = totalEpSafe, key = { "ep$it" }, contentType = { 0 }) { idx ->
                                    val ep = idx + 1
                                    val file = episodeByNumber[ep]
                                    val isCurrent = ep == season.currentEp
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isCurrent -> SerieColors.Blue600
                                                    file != null -> p.chip
                                                    else -> p.chip.copy(alpha = 0.5f)
                                                }
                                            )
                                            .clickable {
                                                if (file != null) {
                                                    onSelectEpisode?.invoke(season.number, ep)
                                                    openVideoUri(context, file.uri, null)
                                                } else {
                                                    android.widget.Toast.makeText(context, context.getString(R.string.file_not_picked), android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(horizontal = 11.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            "E$ep",
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Color.White else if (file != null) p.text else p.muted
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            val currentFile = episodeByNumber[season.currentEp]
                            Text(
                                "S${season.number}E${season.currentEp} — ${currentFile?.displayName ?: stringResource(R.string.episode_missing)}",
                                fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SerieColors.Blue400,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            stringResource(R.string.file_label, item.videoPath.ifBlank { "video" }),
                            fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SerieColors.Blue400,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Player buttons operate on currently displayed episode (series) or single file (movie)
                    val playUri = if (item.type == MediaType.Series) {
                        val season = seasons.getOrNull(safeSeasonIdx) ?: seasons.firstOrNull()
                        season?.episodes?.find { it.number == season.currentEp }?.uri ?: item.videoUri
                    } else item.videoUri
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlayerButton("VLC Player", SerieColors.Orange, Modifier.weight(1f)) {
                            openVideoUri(context, playUri, VLC_PACKAGE)
                        }
                        PlayerButton("MX Player", SerieColors.Indigo500, Modifier.weight(1f)) {
                            openVideoUri(context, playUri, MX_PACKAGE)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    PlayerButton(stringResource(R.string.open_with_other_player), p.muted, Modifier.fillMaxWidth()) {
                        openVideoUri(context, playUri, null)
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
                        Text(stringResource(R.string.delete), fontSize = 12.sp, color = SerieColors.Red)
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600)
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(stringResource(R.string.edit_details), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
            title = { Text(stringResource(R.string.delete_media_title)) },
            text = { Text(stringResource(R.string.delete_media_message, item.title)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.delete), color = SerieColors.Red) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel), color = p.muted) }
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
