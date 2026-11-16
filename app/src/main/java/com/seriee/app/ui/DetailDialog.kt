package com.seriee.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.ui.semantics.Role
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
        shape = RoundedCornerShape(
            topStart = if (p.isNeoBrutalist) 8.dp else 28.dp,
            topEnd = if (p.isNeoBrutalist) 8.dp else 28.dp
        ),
        containerColor = if (p.isGlass) p.surface.copy(alpha = if (p.isLight) 0.90f else 0.84f) else p.surface,
        tonalElevation = if (p.isNeoBrutalist) 0.dp else BottomSheetDefaults.Elevation,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = { SheetDragHandle() }
    ) {
        LiquidSheetHost {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = onDismiss,
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.OffWhite,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 2.dp,
                            cornerRadius = 6.dp,
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = NeoBrutalistColors.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
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
                }

                Row {
                    Poster(item.poster, Modifier.size(width = 112.dp, height = 144.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.title,
                            fontSize = 20.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                            color = p.text
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (p.isNeoBrutalist) NeoBrutalistColors.SkyBlue else p.primary.copy(alpha = 0.25f))
                                    .then(
                                        if (p.isNeoBrutalist) {
                                            Modifier.border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    stringResource(item.type.labelRes),
                                    fontSize = 11.sp,
                                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                    fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.primary
                                )
                            }
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
                            fontSize = 12.sp,
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.SemiBold,
                            color = p.text
                        )
                        if (item.type == MediaType.Series) {
                            val active = seasons.getOrNull(safeSeasonIdx) ?: seasons.firstOrNull()
                            if (active != null) {
                                Text(
                                    stringResource(R.string.season_format, active.number),
                                    fontSize = 12.sp,
                                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                    color = p.muted
                                )
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
                    val scheduleCardModifier = if (p.isNeoBrutalist) {
                        Modifier
                            .fillMaxWidth()
                            .neoBrutalist(
                                backgroundColor = if (p.isLight) NeoBrutalistColors.Cream else p.card,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.5.dp,
                                shadowOffset = 4.dp,
                                cornerRadius = 8.dp
                            )
                            .padding(14.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(20.dp),
                                tint = p.reminder.copy(alpha = if (p.isGlass) 0.14f else 0.08f),
                                blurRadius = 12.dp,
                                refractionHeight = 8.dp,
                                refractionAmount = 14.dp,
                                chromaticAberration = false,
                                withBorder = false
                            )
                            .border(1.dp, p.reminder.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    }
                    Column(
                        modifier = scheduleCardModifier,
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
                                    tint = when {
                                        p.isNeoBrutalist -> p.text
                                        item.reminderEnabled -> p.reminder
                                        else -> p.muted
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (item.reminderEnabled) stringResource(R.string.reminder_alarm_on) else stringResource(R.string.reminder_alarm_off),
                                    fontSize = 13.sp,
                                    fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = when {
                                        p.isNeoBrutalist -> p.text
                                        item.reminderEnabled -> p.reminder
                                        else -> p.muted
                                    }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.reminder.copy(alpha = 0.2f))
                                    .then(
                                        if (p.isNeoBrutalist) {
                                            Modifier.border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    ReminderScheduler.timeUntilRelease(context, item.releaseDate),
                                    fontSize = 11.sp,
                                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.reminder
                                )
                            }
                        }

                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = {
                                    ReminderScheduler.showDateTimePicker(context, item.releaseDate) { newDate ->
                                        onUpdateReleaseDate?.invoke(newDate)
                                    }
                                },
                                role = Role.Button,
                                backgroundColor = p.chip,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                cornerRadius = 8.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = stringResource(R.string.calendar),
                                        tint = p.text,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            ReminderScheduler.formatReleaseDate(context, item.releaseDate),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
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
                                        tint = p.text,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
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
                                    tint = p.reminder,
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
                                    tint = p.reminder,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onToggleReminder != null) {
                                if (p.isNeoBrutalist) {
                                    NeoBrutalistBox(
                                        onClick = onToggleReminder,
                                        role = Role.Button,
                                        backgroundColor = if (item.reminderEnabled) NeoBrutalistColors.OffWhite else NeoBrutalistColors.Pink,
                                        borderColor = p.border,
                                        shadowColor = NeoBrutalistColors.Black,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp,
                                        cornerRadius = 6.dp,
                                        contentPadding = PaddingValues(vertical = 9.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            if (item.reminderEnabled) stringResource(R.string.turn_off_alarm) else stringResource(R.string.turn_on_alarm),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = NeoBrutalistColors.Black
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (item.reminderEnabled) p.chip else p.reminder.copy(alpha = 0.2f))
                                            .clickable(onClick = onToggleReminder)
                                            .padding(vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (item.reminderEnabled) stringResource(R.string.turn_off_alarm) else stringResource(R.string.turn_on_alarm),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (item.reminderEnabled) p.muted else p.reminder
                                        )
                                    }
                                }
                            }
                            if (onTestReminder != null) {
                                if (p.isNeoBrutalist) {
                                    NeoBrutalistBox(
                                        onClick = {
                                            onTestReminder()
                                            android.widget.Toast.makeText(context, context.getString(R.string.notification_sent), android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        role = Role.Button,
                                        backgroundColor = NeoBrutalistColors.Yellow,
                                        borderColor = p.border,
                                        shadowColor = NeoBrutalistColors.Black,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp,
                                        cornerRadius = 6.dp,
                                        contentPadding = PaddingValues(vertical = 9.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            stringResource(R.string.test_alarm_now),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = NeoBrutalistColors.Black
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(p.primary.copy(alpha = 0.18f))
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
                                            color = p.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (item.streamUrl.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle(stringResource(R.string.streaming_online))
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = { openUrl(context, item.streamUrl) },
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.SkyBlue,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Public, null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    item.streamUrl,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NeoBrutalistColors.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlass(
                                    shape = RoundedCornerShape(16.dp),
                                    tint = p.primary.copy(alpha = if (p.isGlass) 0.22f else 0.2f),
                                    blurRadius = 12.dp,
                                    refractionHeight = 8.dp,
                                    refractionAmount = 14.dp,
                                    chromaticAberration = false
                                )
                                .clickable { openUrl(context, item.streamUrl) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Public, null, tint = p.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                item.streamUrl,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = p.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = p.primary, modifier = Modifier.size(16.dp))
                        }
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
                                val isSel = idx == safeSeasonIdx
                                if (p.isNeoBrutalist) {
                                    NeoBrutalistBox(
                                        onClick = { selectedSeasonIdx = idx },
                                        role = Role.Tab,
                                        backgroundColor = if (isSel) NeoBrutalistColors.Yellow else p.chip,
                                        borderColor = p.border,
                                        shadowColor = NeoBrutalistColors.Black,
                                        borderWidth = 2.dp,
                                        shadowOffset = 2.dp,
                                        cornerRadius = 6.dp,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.season_format, s.number),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSel) NeoBrutalistColors.Black else p.text
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(if (isSel) p.primary else p.chip)
                                            .clickable { selectedSeasonIdx = idx }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.season_format, s.number),
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = if (isSel) p.onAccent else p.muted
                                        )
                                    }
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
                                fontSize = 11.sp,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.SemiBold,
                                color = p.muted
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
                                    if (p.isNeoBrutalist) {
                                        NeoBrutalistBox(
                                            onClick = {
                                                if (file != null) {
                                                    onSelectEpisode?.invoke(season.number, ep)
                                                    openVideoUri(context, file.uri, null)
                                                } else {
                                                    android.widget.Toast.makeText(context, context.getString(R.string.file_not_picked), android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            role = Role.Button,
                                            backgroundColor = when {
                                                isCurrent -> NeoBrutalistColors.Yellow
                                                file != null -> NeoBrutalistColors.Mint
                                                else -> p.chip
                                            },
                                            borderColor = p.border,
                                            shadowColor = NeoBrutalistColors.Black,
                                            borderWidth = 2.dp,
                                            shadowOffset = 2.dp,
                                            cornerRadius = 6.dp,
                                            contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                "E$ep",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when {
                                                    isCurrent || file != null -> NeoBrutalistColors.Black
                                                    else -> p.muted
                                                }
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    when {
                                                        isCurrent -> p.primary
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
                                                color = if (isCurrent) p.onAccent else if (file != null) p.text else p.muted
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            val currentFile = episodeByNumber[season.currentEp]
                            Text(
                                "S${season.number}E${season.currentEp} — ${currentFile?.displayName ?: stringResource(R.string.episode_missing)}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                color = if (p.isNeoBrutalist) p.text else p.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            stringResource(R.string.file_label, item.videoPath.ifBlank { "video" }),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                            color = if (p.isNeoBrutalist) p.text else p.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Player buttons operate on currently displayed episode (series) or single file (movie)
                    val playUri = if (item.type == MediaType.Series) {
                        val season = seasons.getOrNull(safeSeasonIdx) ?: seasons.firstOrNull()
                        season?.episodes?.find { it.number == season.currentEp }?.uri ?: item.videoUri
                    } else item.videoUri
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlayerButton("VLC Player", if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.reminder, Modifier.weight(1f)) {
                            openVideoUri(context, playUri, VLC_PACKAGE)
                        }
                        PlayerButton("MX Player", if (p.isNeoBrutalist) NeoBrutalistColors.SkyBlue else p.info, Modifier.weight(1f)) {
                            openVideoUri(context, playUri, MX_PACKAGE)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    PlayerButton(stringResource(R.string.open_with_other_player), if (p.isNeoBrutalist) NeoBrutalistColors.OffWhite else p.muted, Modifier.fillMaxWidth()) {
                        openVideoUri(context, playUri, null)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = { confirmDelete = true },
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.CoralRed,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Delete, null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.delete),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoBrutalistColors.Black
                                )
                            }
                        }
                        NeoBrutalistBox(
                            onClick = onEdit,
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.Yellow,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 11.dp)
                        ) {
                            Text(
                                stringResource(R.string.edit_details),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeoBrutalistColors.Black
                            )
                        }
                    } else {
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { confirmDelete = true }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Delete, null, tint = p.danger, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.delete), fontSize = 12.sp, color = p.danger)
                        }
                        Box(
                            Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    tint = p.primary.copy(alpha = if (p.isGlass) 0.80f else 1f),
                                    blurRadius = 12.dp,
                                    refractionHeight = 8.dp,
                                    refractionAmount = 16.dp,
                                    chromaticAberration = false
                                )
                                .clickable(onClick = onEdit)
                                .padding(horizontal = 20.dp, vertical = 11.dp)
                        ) {
                            Text(stringResource(R.string.edit_details), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.onAccent)
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        if (p.isNeoBrutalist) {
            NeoBrutalistConfirmDialog(
                title = stringResource(R.string.delete_media_title),
                message = stringResource(R.string.delete_media_message, item.title),
                confirmLabel = stringResource(R.string.delete),
                dismissLabel = stringResource(R.string.cancel),
                confirmColor = NeoBrutalistColors.CoralRed,
                onConfirm = {
                    confirmDelete = false
                    onDelete()
                },
                onDismiss = { confirmDelete = false }
            )
        } else {
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
                    }) { Text(stringResource(R.string.delete), color = p.danger) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel), color = p.muted) }
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val p = LocalPalette.current
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
        color = if (p.isNeoBrutalist) p.text else p.muted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun PlayerButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            onClick = onClick,
            role = Role.Button,
            backgroundColor = color,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.dp,
            shadowOffset = 3.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(vertical = 11.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = NeoBrutalistColors.Black
            )
        }
    } else {
        Box(
            modifier = modifier
                .liquidGlass(
                    shape = CircleShape,
                    tint = color.copy(alpha = if (p.isGlass) 0.22f else 0.18f),
                    blurRadius = 12.dp,
                    refractionHeight = 8.dp,
                    refractionAmount = 14.dp,
                    chromaticAberration = false
                )
                .clickable(onClick = onClick)
                .padding(vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}
