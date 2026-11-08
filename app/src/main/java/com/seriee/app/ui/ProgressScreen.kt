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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
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
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler

@Composable
fun ProgressScreen(
    list: List<MediaItem>,
    padding: PaddingValues,
    onOpen: (MediaItem) -> Unit,
    onIncrement: (String) -> Unit
) {
    val p = LocalPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }

    // Filter labels need localized strings, so they stay in composition (cheap, 4 items).
    val filterLabels = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_watching),
        stringResource(R.string.filter_completed),
        stringResource(R.string.filter_coming_soon)
    )
    val filterCounts = remember(list) {
        listOf(
            list.size,
            list.count { it.status == WatchStatus.Watching },
            list.count { it.status == WatchStatus.Completed },
            list.count { it.status == WatchStatus.ComingSoon }
        )
    }

    val filtered by remember(list, query, filter) {
        derivedStateOf {
            list.filter { it.title.contains(query.trim(), ignoreCase = true) }
                .filter {
                    when (filter) {
                        1 -> it.status == WatchStatus.Watching
                        2 -> it.status == WatchStatus.Completed
                        3 -> it.status == WatchStatus.ComingSoon
                        else -> true
                    }
                }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                stringResource(R.string.active_progress),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = p.text
            )
            Text(
                stringResource(R.string.active_progress_subtitle),
                fontSize = 12.sp,
                color = p.muted
            )

            SField(
                value = query,
                onChange = { query = it },
                placeholder = stringResource(R.string.search_progress_placeholder),
                leading = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailing = if (query.isNotBlank()) {
                    {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear_search),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" },
                            tint = p.muted
                        )
                    }
                } else null
            )

            // Top Status Filter Bar for All, Watching, Completed, Coming Soon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterLabels.forEachIndexed { index, label ->
                    FilterChip(
                        label = "$label (${filterCounts[index]})",
                        selected = filter == index,
                        onClick = { filter = index }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(top = 48.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    if (query.isNotBlank()) stringResource(R.string.no_search_results, query)
                    else stringResource(R.string.no_shows),
                    color = p.muted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }, contentType = { it.type }) { item ->
                    ProgressCard(item, onClick = { onOpen(item) }, onIncrement = { onIncrement(item.id) })
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(item: MediaItem, onClick: () -> Unit, onIncrement: () -> Unit) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val shape = remember { RoundedCornerShape(16.dp) }
    // Memoize date-derived strings (they call SimpleDateFormat etc.)
    val badgeText = remember(item.releaseDate, item.status) {
        if (item.status == WatchStatus.ComingSoon) ReminderScheduler.countdownShortBadge(context, item.releaseDate) else ""
    }
    val badgeLong = remember(item.releaseDate, item.status) {
        if (item.status == WatchStatus.ComingSoon) ReminderScheduler.countdownBadgeText(context, item.releaseDate) else ""
    }
    val detailedText = remember(item.releaseDate, item.status) {
        if (item.status == WatchStatus.ComingSoon) ReminderScheduler.countdownDetailedText(context, item.releaseDate) else ""
    }
    val relTime = remember(item.updatedAt) { relativeTime(context, item.updatedAt) }
    val visualProgress = remember(item.releaseDate) {
        val daysLeft = ReminderScheduler.daysUntilRelease(item.releaseDate)
        ((30 - daysLeft).coerceIn(1, 30) / 30f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(p.card)
            .border(1.dp, p.border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(width = 68.dp, height = 86.dp)) {
            Poster(item.poster, Modifier.fillMaxSize())
            if (item.status == WatchStatus.ComingSoon) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SerieColors.Orange)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        badgeText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
            if (item.rating > 0) {
                StarRatingBadge(
                    rating = item.rating,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (item.type == MediaType.Series) stringResource(R.string.season_format, item.season)
                    else stringResource(R.string.movie),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.status == WatchStatus.ComingSoon) SerieColors.Orange else SerieColors.Blue400
                )
                Text(relTime, fontSize = 10.sp, color = p.muted)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            if (item.tags.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.tags.take(3).forEach { tag ->
                        GenreTagBadge(tag)
                    }
                }
            }
            if (item.status == WatchStatus.ComingSoon) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.HourglassTop,
                            contentDescription = null,
                            tint = SerieColors.Orange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            badgeLong,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SerieColors.Orange
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SerieColors.Orange.copy(alpha = 0.2f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SerieColors.Orange
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { visualProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = SerieColors.Orange,
                    trackColor = SerieColors.Orange.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    detailedText,
                    fontSize = 11.sp,
                    color = p.muted
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (item.type == MediaType.Movie) {
                            stringResource(if (item.watchedEpisodes > 0) R.string.movie_watched else R.string.movie_not_watched)
                        } else {
                            stringResource(R.string.episode_progress, item.watchedEpisodes, item.totalEpisodes)
                        },
                        fontSize = 12.sp, color = p.muted
                    )
                    Text("${item.percent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.text)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { item.fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = SerieColors.Blue500,
                    trackColor = p.chip
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        if (item.status == WatchStatus.ComingSoon) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.reminderEnabled) SerieColors.Orange.copy(alpha = 0.2f) else p.chip),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.Alarm,
                    contentDescription = stringResource(R.string.reminder),
                    tint = if (item.reminderEnabled) SerieColors.Orange else p.muted,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else if (item.watchedEpisodes < item.totalEpisodes) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SerieColors.Blue600.copy(alpha = 0.2f))
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_one_episode), tint = SerieColors.Blue400, modifier = Modifier.size(18.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SerieColors.Emerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.completed), tint = SerieColors.Emerald, modifier = Modifier.size(18.dp))
            }
        }
    }
}
