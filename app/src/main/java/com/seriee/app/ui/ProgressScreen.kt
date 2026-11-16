package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.StreakState
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    items: List<MediaItem>,
    streak: StreakState,
    showSearch: Boolean,
    listState: LazyListState,
    onIncrement: (String) -> Unit,
    onSelect: (MediaItem) -> Unit,
    onResetStreak: () -> Unit,
    onAddClick: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<WatchStatus?>(null) }

    val statusCounts = remember(items) {
        WatchStatus.entries.associateWith { st -> items.count { it.status == st } }
    }

    val filtered = remember(items, query, filter) {
        val q = query.trim().lowercase()
        items.filter { item ->
            val matchFilter = filter == null || item.status == filter
            val matchQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.tags.any { it.lowercase().contains(q) }
            matchFilter && matchQuery
        }.sortedWith(
            compareByDescending<MediaItem> { it.pinned }
                .thenBy { if (it.status == WatchStatus.Watching) 0 else if (it.status == WatchStatus.ComingSoon) 1 else 2 }
                .thenByDescending { it.updatedAt }
        )
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.active_progress),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.active_progress_subtitle),
                    fontSize = 13.sp,
                    color = p.muted,
                )
            }
        }

        if (streak.streakDays > 0 || streak.bestStreak > 0 || streak.watchDays.isNotEmpty()) {
            item {
                StreakBanner(streak = streak, onResetStreak = onResetStreak)
            }
        }

        if (showSearch) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_progress_placeholder), color = p.muted, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = p.muted) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search), tint = p.muted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.accent,
                        unfocusedBorderColor = p.border,
                        focusedContainerColor = p.card,
                        unfocusedContainerColor = p.card,
                        focusedTextColor = p.text,
                        unfocusedTextColor = p.text,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChipButton(
                    label = "${stringResource(R.string.filter_all)} (${items.size})",
                    selected = filter == null,
                    accent = p.accent,
                    onClick = { filter = null },
                )
                WatchStatus.entries.forEach { st ->
                    val count = statusCounts[st] ?: 0
                    if (count > 0 || filter == st) {
                        FilterChipButton(
                            label = "${stringResource(st.filterRes)} ($count)",
                            selected = filter == st,
                            accent = statusColor(st, p),
                            onClick = { filter = if (filter == st) null else st },
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Tv,
                    title = stringResource(R.string.empty_progress_title),
                    hint = stringResource(R.string.empty_progress_hint),
                    actionLabel = stringResource(R.string.add_first_title),
                    onAction = onAddClick,
                )
            }
        } else if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Search,
                    title = if (query.isNotBlank()) stringResource(R.string.no_search_results, query)
                    else stringResource(R.string.no_shows),
                    hint = stringResource(R.string.active_progress_subtitle),
                    actionLabel = stringResource(R.string.clear_filters),
                    onAction = {
                        query = ""
                        filter = null
                    },
                )
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                ProgressItemCard(
                    item = item,
                    countdownText = if (item.status == WatchStatus.ComingSoon) {
                        ReminderScheduler.countdownBadgeText(context, item.releaseDate)
                    } else null,
                    onIncrement = { onIncrement(item.id) },
                    onClick = { onSelect(item) },
                )
            }
        }
    }
}

@Composable
private fun StreakBanner(
    streak: StreakState,
    onResetStreak: () -> Unit,
) {
    val p = LocalPalette.current
    val today = remember { LocalDate.now() }
    SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(p.warning.copy(alpha = 0.22f))
                        .then(if (p.isNeoBrutalist) Modifier.border(1.5.dp, p.border, RoundedCornerShape(10.dp)) else Modifier)
                ) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = p.warning, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.streak_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.muted,
                    )
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.streak_days, streak.streakDays),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                        )
                        Text(
                            text = stringResource(R.string.best_streak, streak.bestStreak),
                            fontSize = 12.sp,
                            color = p.muted,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                IconButton(onClick = onResetStreak, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.reset_streak),
                        tint = p.muted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                (6 downTo 0).forEach { offset ->
                    val d = today.minusDays(offset.toLong())
                    val active = d.toString() in streak.watchDays
                    val label = d.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (active) p.warning else p.chip)
                                .then(if (p.isNeoBrutalist) Modifier.border(1.5.dp, p.border, CircleShape) else Modifier)
                        ) {
                            if (active) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressItemCard(
    item: MediaItem,
    countdownText: String?,
    onIncrement: () -> Unit,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    val stColor = statusColor(item.status, p)
    val isDone = item.watchedEpisodes >= item.totalEpisodes && item.totalEpisodes > 0
    SerieeCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Poster(
                poster = item.poster,
                type = item.type,
                title = item.title,
                modifier = Modifier.size(width = 62.dp, height = 88.dp),
                cornerRadius = 10.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    StatusBadge(status = item.status)
                    if (item.reminderEnabled && item.releaseDate != null) {
                        Icon(
                            Icons.Rounded.NotificationsActive,
                            contentDescription = stringResource(R.string.reminder),
                            tint = p.reminder,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                val sub = if (item.type == MediaType.Movie) {
                    stringResource(R.string.movie) + " · " + if (item.watchedEpisodes > 0) stringResource(R.string.movie_watched) else stringResource(R.string.movie_not_watched)
                } else {
                    val activeSec = item.effectiveSeasons.firstOrNull { it.currentEp < it.totalEp } ?: item.effectiveSeasons.last()
                    stringResource(R.string.season_format, activeSec.number) + " · " + stringResource(R.string.episode_progress, item.watchedEpisodes, item.totalEpisodes)
                }
                Text(sub, fontSize = 12.sp, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)

                if (countdownText != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = countdownText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.warning,
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradientProgressBar(
                            fraction = item.fraction,
                            color = stColor,
                            modifier = Modifier.weight(1f),
                            height = 9.dp,
                        )
                        Text(
                            text = "${item.percent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                        )
                    }
                }
            }
            if (!isDone) {
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_one_episode), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun FilterChipButton(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(9.dp)
    val bg = when {
        selected -> accent
        else -> p.card
    }
    val fg = when {
        selected && p.isNeoBrutalist -> Color.White
        selected -> if (accent == p.accent) p.onAccent else Color.White
        else -> p.text
    }
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(if (p.isNeoBrutalist) 2.dp else 1.dp, if (selected && !p.isNeoBrutalist) accent else p.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}
