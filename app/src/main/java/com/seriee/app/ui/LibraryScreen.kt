package com.seriee.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
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
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler

private enum class LibrarySort(val labelRes: Int) {
    RECENT(R.string.sort_recent),
    TITLE(R.string.sort_title),
    RATING(R.string.sort_rating),
    PROGRESS(R.string.sort_progress),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    items: List<MediaItem>,
    gridView: Boolean,
    showSearch: Boolean,
    gridState: LazyGridState,
    listState: LazyListState,
    onToggleGrid: (Boolean) -> Unit,
    onTogglePin: (String) -> Unit,
    onSelect: (MediaItem) -> Unit,
    onAddClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf<MediaType?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<WatchStatus?>(null) }
    var tagFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var sortBy by rememberSaveable { mutableStateOf(LibrarySort.RECENT) }

    val availableTags = remember(items) {
        items.flatMap { it.tags }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    val filtered = remember(items, query, typeFilter, statusFilter, tagFilter, sortBy) {
        val q = query.trim().lowercase()
        val matched = items.filter { item ->
            val mType = typeFilter == null || item.type == typeFilter
            val mStatus = statusFilter == null || item.status == statusFilter
            val mTag = tagFilter == null || item.tags.any { it.equals(tagFilter, ignoreCase = true) }
            val mQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.tags.any { it.lowercase().contains(q) }
            mType && mStatus && mTag && mQuery
        }
        val secondary: Comparator<MediaItem> = when (sortBy) {
            LibrarySort.RECENT -> compareByDescending { it.updatedAt }
            LibrarySort.TITLE -> compareBy { it.title.lowercase() }
            LibrarySort.RATING -> compareByDescending<MediaItem> { it.rating }.thenByDescending { it.updatedAt }
            LibrarySort.PROGRESS -> compareByDescending<MediaItem> { it.fraction }.thenByDescending { it.updatedAt }
        }
        matched.sortedWith(compareByDescending<MediaItem> { it.pinned }.then(secondary))
    }

    @Composable
    fun HeaderBlock() {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${stringResource(R.string.nav_library)} (${filtered.size})",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(p.card)
                        .border(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border, RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (gridView) p.accent else Color.Transparent)
                            .clickable { onToggleGrid(true) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.GridView,
                            contentDescription = stringResource(R.string.grid),
                            tint = if (gridView) p.onAccent else p.muted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!gridView) p.accent else Color.Transparent)
                            .clickable { onToggleGrid(false) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ViewList,
                            contentDescription = stringResource(R.string.list),
                            tint = if (!gridView) p.onAccent else p.muted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_library_placeholder), color = p.muted, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = p.muted) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear), tint = p.muted)
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

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChipButton(
                    label = stringResource(R.string.all_types),
                    selected = typeFilter == null,
                    accent = p.accent,
                    onClick = { typeFilter = null },
                )
                MediaType.entries.forEach { t ->
                    FilterChipButton(
                        label = stringResource(t.labelRes),
                        selected = typeFilter == t,
                        accent = p.accent,
                        onClick = { typeFilter = if (typeFilter == t) null else t },
                    )
                }
                WatchStatus.entries.forEach { st ->
                    FilterChipButton(
                        label = stringResource(st.labelRes),
                        selected = statusFilter == st,
                        accent = statusColor(st, p),
                        onClick = { statusFilter = if (statusFilter == st) null else st },
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                LibrarySort.entries.forEach { option ->
                    FilterChipButton(
                        label = stringResource(option.labelRes),
                        selected = sortBy == option,
                        accent = p.accent,
                        onClick = { sortBy = option },
                    )
                }
            }

            if (availableTags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    FilterChipButton(
                        label = stringResource(R.string.all_tags),
                        selected = tagFilter == null,
                        accent = p.accent,
                        onClick = { tagFilter = null },
                    )
                    availableTags.forEach { tag ->
                        FilterChipButton(
                            label = "#$tag",
                            selected = tagFilter.equals(tag, ignoreCase = true),
                            accent = p.accent,
                            onClick = { tagFilter = if (tagFilter.equals(tag, ignoreCase = true)) null else tag },
                        )
                    }
                }
            }
        }
    }

    if (items.isEmpty()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { HeaderBlock() }
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Movie,
                    title = stringResource(R.string.empty_library_title),
                    hint = stringResource(R.string.empty_library_hint),
                    actionLabel = stringResource(R.string.add),
                    onAction = onAddClick,
                    secondaryLabel = stringResource(R.string.import_collection),
                    onSecondary = onImportClick,
                )
            }
        }
        return
    }

    if (filtered.isEmpty()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { HeaderBlock() }
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.Search,
                    title = if (query.isNotBlank()) stringResource(R.string.no_library_results, query) else stringResource(R.string.no_library_items),
                    hint = stringResource(R.string.empty_library_hint),
                    actionLabel = stringResource(R.string.clear_filters),
                    onAction = {
                        query = ""
                        typeFilter = null
                        statusFilter = null
                        tagFilter = null
                    },
                )
            }
        }
        return
    }

    if (gridView) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 148.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeaderBlock()
            }
            items(filtered, key = { it.id }) { item ->
                LibraryGridCard(
                    item = item,
                    shortBadge = if (item.status == WatchStatus.ComingSoon && item.releaseDate != null) {
                        ReminderScheduler.countdownShortBadge(context, item.releaseDate)
                    } else "",
                    onTogglePin = { onTogglePin(item.id) },
                    onClick = { onSelect(item) },
                )
            }
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { HeaderBlock() }
            items(filtered, key = { it.id }) { item ->
                LibraryListCard(
                    item = item,
                    onTogglePin = { onTogglePin(item.id) },
                    onClick = { onSelect(item) },
                )
            }
        }
    }
}

@Composable
private fun LibraryGridCard(
    item: MediaItem,
    shortBadge: String,
    onTogglePin: () -> Unit,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    SerieeCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Poster(
                    poster = item.poster,
                    type = item.type,
                    title = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                    cornerRadius = 8.dp,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.pinned) p.accent else Color.Black.copy(alpha = 0.55f))
                        .clickable(onClick = onTogglePin)
                        .padding(5.dp)
                ) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = stringResource(R.string.pin),
                        tint = if (item.pinned) p.onAccent else Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
                if (item.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.72f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${item.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                if (shortBadge.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(p.warning)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(shortBadge, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = p.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (item.type == MediaType.Movie) stringResource(R.string.type_movie)
                else stringResource(R.string.episode_progress, item.watchedEpisodes, item.totalEpisodes),
                fontSize = 11.sp,
                color = p.muted,
                maxLines = 1,
            )
            Spacer(Modifier.height(5.dp))
            StatusBadge(status = item.status)
        }
    }
}

@Composable
private fun LibraryListCard(
    item: MediaItem,
    onTogglePin: () -> Unit,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    SerieeCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Poster(
                poster = item.poster,
                type = item.type,
                title = item.title,
                modifier = Modifier.size(width = 52.dp, height = 74.dp),
                cornerRadius = 8.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(status = item.status)
                    if (item.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${item.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.text)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (item.type == MediaType.Movie) stringResource(R.string.type_movie)
                    else stringResource(R.string.episode_progress, item.watchedEpisodes, item.totalEpisodes),
                    fontSize = 12.sp,
                    color = p.muted,
                )
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    Icons.Rounded.PushPin,
                    contentDescription = stringResource(R.string.pin),
                    tint = if (item.pinned) p.accent else p.muted,
                )
            }
        }
    }
}
