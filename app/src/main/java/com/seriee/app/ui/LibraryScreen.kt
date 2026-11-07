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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler

@Composable
fun LibraryScreen(
    list: List<MediaItem>,
    gridView: Boolean,
    padding: PaddingValues,
    onSetGrid: (Boolean) -> Unit,
    onOpen: (MediaItem) -> Unit,
    onPin: (String) -> Unit
) {
    val p = LocalPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableIntStateOf(0) }
    var statusFilter by rememberSaveable { mutableIntStateOf(0) }

    val typeLabels = listOf(
        stringResource(R.string.all_types),
        stringResource(R.string.type_series),
        stringResource(R.string.type_movie)
    )

    val filtered by remember(list, query, typeFilter, statusFilter) {
        derivedStateOf {
            list.filter { it.title.contains(query.trim(), ignoreCase = true) }
                .filter {
                    when (typeFilter) {
                        1 -> it.type == MediaType.Series
                        2 -> it.type == MediaType.Movie
                        else -> true
                    }
                }
                .filter {
                    when (statusFilter) {
                        1 -> it.status == WatchStatus.Watching
                        2 -> it.status == WatchStatus.Completed
                        3 -> it.status == WatchStatus.ComingSoon
                        4 -> it.status == WatchStatus.PlanToWatch
                        else -> true
                    }
                }
                .sortedByDescending { it.pinned }
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
            SField(
                value = query,
                onChange = { query = it },
                placeholder = stringResource(R.string.search_library_placeholder),
                leading = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailing = if (query.isNotBlank()) {
                    {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" },
                            tint = p.muted
                        )
                    }
                } else null
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                DropdownField(
                    label = "",
                    valueLabel = typeLabels[typeFilter],
                    options = typeLabels.mapIndexed { i, s -> i to s },
                    onSelect = { typeFilter = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ViewToggle(selected = gridView, onClick = { onSetGrid(true) }) {
                        Icon(Icons.Filled.GridView, contentDescription = stringResource(R.string.grid), modifier = Modifier.size(18.dp), tint = it)
                    }
                    ViewToggle(selected = !gridView, onClick = { onSetGrid(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = stringResource(R.string.list), modifier = Modifier.size(18.dp), tint = it)
                    }
                }
            }

            // Status filter chips: All, Watching, Completed, Coming Soon, Plan to Watch
            val statusChips = listOf(
                stringResource(R.string.filter_all) to list.size,
                stringResource(R.string.filter_watching) to list.count { it.status == WatchStatus.Watching },
                stringResource(R.string.filter_completed) to list.count { it.status == WatchStatus.Completed },
                stringResource(R.string.filter_coming_soon) to list.count { it.status == WatchStatus.ComingSoon },
                stringResource(R.string.filter_plan_to_watch) to list.count { it.status == WatchStatus.PlanToWatch }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusChips.forEachIndexed { index, (label, count) ->
                    FilterChip(
                        label = "$label ($count)",
                        selected = statusFilter == index,
                        onClick = { statusFilter = index }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(top = 48.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    if (query.isNotBlank()) stringResource(R.string.no_library_results, query)
                    else stringResource(R.string.no_library_items),
                    color = p.muted,
                    fontSize = 14.sp
                )
            }
        } else if (gridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filtered, key = { it.id }, contentType = { it.status }) { item ->
                    GridItem(item, onClick = { onOpen(item) }, onPin = { onPin(item.id) })
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }, contentType = { it.status }) { item ->
                    ListItem(item, onClick = { onOpen(item) }, onPin = { onPin(item.id) })
                }
            }
        }
    }
}

@Composable
private fun ViewToggle(selected: Boolean, onClick: () -> Unit, icon: @Composable (Color) -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SerieColors.Blue600 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        icon(if (selected) Color.White else p.muted)
    }
}

@Composable
private fun GridItem(item: MediaItem, onClick: () -> Unit, onPin: () -> Unit) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val gradientBrush = remember {
        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
    }
    val badgeShort = remember(item.releaseDate) { ReminderScheduler.countdownShortBadge(context, item.releaseDate) }
    val badgeText = remember(item.releaseDate) { ReminderScheduler.countdownBadgeText(context, item.releaseDate) }
    Column(Modifier.clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
            Poster(item.poster, Modifier.fillMaxSize(), RoundedCornerShape(16.dp))
            if (item.status == WatchStatus.ComingSoon) {
                // Top-Start Days Until Release Badge (e.g. D-5)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SerieColors.Orange)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.reminderEnabled) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(
                            badgeShort,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Bottom Countdown Overlay Strip
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(gradientBrush)
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.HourglassTop,
                            contentDescription = null,
                            tint = SerieColors.Orange,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (item.rating > 0) {
                StarRatingBadge(
                    item.rating,
                    modifier = Modifier
                        .align(if (item.status == WatchStatus.ComingSoon) Alignment.BottomEnd else Alignment.BottomStart)
                        .padding(4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onPin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PushPin,
                    contentDescription = stringResource(R.string.pin),
                    tint = if (item.pinned) SerieColors.Amber else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(Modifier.size(6.dp))
        Text(
            item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = p.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )
        if (item.tags.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                GenreTagBadge(item.tags.first())
            }
        }
    }
}

@Composable
private fun ListItem(item: MediaItem, onClick: () -> Unit, onPin: () -> Unit) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val shape = remember { RoundedCornerShape(16.dp) }
    val detailedCountdown = remember(item.releaseDate) {
        ReminderScheduler.countdownDetailedText(context, item.releaseDate)
    }
    val shortBadge = remember(item.releaseDate) {
        ReminderScheduler.countdownShortBadge(context, item.releaseDate)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(p.card)
            .border(1.dp, p.border, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Poster(item.poster, Modifier.size(48.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (item.pinned) {
                        Icon(
                            Icons.Filled.PushPin, contentDescription = null,
                            tint = SerieColors.Amber, modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.rating > 0) {
                    Spacer(Modifier.width(6.dp))
                    StarRatingBadge(rating = item.rating)
                }
            }
            if (item.status == WatchStatus.ComingSoon) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Filled.HourglassTop,
                        contentDescription = null,
                        tint = SerieColors.Orange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${stringResource(item.type.labelRes)} • $detailedCountdown",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SerieColors.Orange
                    )
                }
            } else {
                Text(
                    "${stringResource(item.type.labelRes)} • ${stringResource(R.string.episode_progress, item.watchedEpisodes, item.totalEpisodes)}",
                    fontSize = 12.sp,
                    color = p.muted
                )
            }
            if (item.tags.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.tags.take(3).forEach { tag ->
                        GenreTagBadge(tag)
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (item.status == WatchStatus.ComingSoon) SerieColors.Orange.copy(alpha = 0.18f) else p.chip)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.status == WatchStatus.ComingSoon && item.reminderEnabled) {
                    Icon(Icons.Filled.NotificationsActive, null, tint = SerieColors.Orange, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    if (item.status == WatchStatus.ComingSoon) shortBadge
                    else stringResource(item.status.labelRes),
                    fontSize = 11.sp,
                    fontWeight = if (item.status == WatchStatus.ComingSoon) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.status == WatchStatus.ComingSoon) SerieColors.Orange else p.text
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onPin),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PushPin, contentDescription = stringResource(R.string.pin),
                tint = if (item.pinned) SerieColors.Amber else p.muted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
