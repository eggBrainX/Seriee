package com.seriee.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
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

private enum class LibrarySort(val labelRes: Int) {
    RECENT(R.string.sort_recent),
    TITLE(R.string.sort_title),
    RATING(R.string.sort_rating),
    PROGRESS(R.string.sort_progress),
}

private fun releaseYearOf(ts: Long): Int {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = ts
    return cal.get(java.util.Calendar.YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    list: List<MediaItem>,
    gridView: Boolean,
    padding: PaddingValues,
    onSetGrid: (Boolean) -> Unit,
    onOpen: (MediaItem) -> Unit,
    onPin: (String) -> Unit,
    onUpdateRating: (MediaItem, Int) -> Unit,
    onAdd: () -> Unit
) {
    val p = LocalPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableIntStateOf(0) }
    var statusFilter by rememberSaveable { mutableStateOf(LibraryFilter.All) }
    var tagFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var listFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var sortBy by rememberSaveable { mutableStateOf(LibrarySort.RECENT) }
    // Vetro P3a: rating / release-year range filters (year 0f = unbounded).
    var ratingMin by rememberSaveable { mutableFloatStateOf(0f) }
    var ratingMax by rememberSaveable { mutableFloatStateOf(5f) }
    var yearMin by rememberSaveable { mutableFloatStateOf(0f) }
    var yearMax by rememberSaveable { mutableFloatStateOf(0f) }
    // Vetro P3c: title whose rating is being edited in place from its card.
    var ratingEditItem by remember { mutableStateOf<MediaItem?>(null) }

    val yearDataBounds = remember(list) {
        val years = list.mapNotNull { it.releaseDate?.let { ts -> releaseYearOf(ts) } }
        if (years.isEmpty()) null else (years.min() to years.max())
    }

    val typeLabels = listOf(
        stringResource(R.string.all_types),
        stringResource(R.string.type_series),
        stringResource(R.string.type_movie)
    )

    val statusOptions = libraryFilters.map { it to stringResource(it.labelRes) }
    val statusCounts = remember(list) {
        libraryFilters.associate { it to list.countMatching(it) }
    }
    val statusValueLabel = statusOptions.firstOrNull { it.first == statusFilter }?.second ?: ""

    val sortOptions = LibrarySort.entries.map { it to stringResource(it.labelRes) }

    val availableTags = remember(list) {
        list.flatMap { it.tags }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    // F6: custom lists present in the library.
    val availableLists = remember(list) {
        list.flatMap { it.lists }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    val filtered by remember(list, query, typeFilter, statusFilter, tagFilter, listFilter, sortBy, ratingMin, ratingMax, yearMin, yearMax) {
        derivedStateOf {
            val matched = list.filter { it.title.contains(query.trim(), ignoreCase = true) }
                .filter {
                    when (typeFilter) {
                        1 -> it.type == MediaType.Series
                        2 -> it.type == MediaType.Movie
                        else -> true
                    }
                }
                .filter { statusFilter.matches(it) }
                .filter { tagFilter == null || it.tags.any { tag -> tag.equals(tagFilter, ignoreCase = true) } }
                .filter { listFilter == null || it.lists.any { name -> name.equals(listFilter, ignoreCase = true) } }
                // Vetro P3a: rating range (unrated titles count as 0).
                .filter { it.rating.toFloat() in ratingMin..ratingMax }
                // Vetro P3a: release-year range; titles without a date pass
                // unless the user actually narrowed the range.
                .filter { item ->
                    if (yearMin == 0f && yearMax == 0f) true
                    else {
                        val ts = item.releaseDate ?: return@filter true
                        val yr = releaseYearOf(ts).toFloat()
                        val lo = if (yearMin == 0f) yr else yearMin
                        val hi = if (yearMax == 0f) yr else yearMax
                        yr in lo..hi
                    }
                }
            val secondary: Comparator<MediaItem> = when (sortBy) {
                LibrarySort.RECENT -> compareByDescending { it.updatedAt }
                LibrarySort.TITLE -> compareBy { it.title.lowercase() }
                LibrarySort.RATING -> compareByDescending<MediaItem> { it.rating }.thenByDescending { it.updatedAt }
                LibrarySort.PROGRESS -> compareByDescending<MediaItem> { it.fraction }.thenByDescending { it.updatedAt }
            }
            matched.sortedWith(compareByDescending<MediaItem> { it.pinned }.then(secondary))
        }
    }

    val topInset = padding.calculateTopPadding()
    val bottomInset = padding.calculateBottomPadding()

    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = topInset + 4.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SField(
                value = query,
                onChange = { query = it },
                placeholder = stringResource(R.string.search_library_placeholder),
                filled = true,
                leading = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailing = if (query.isNotBlank()) {
                    {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.clear),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { query = "" },
                            tint = if (p.isNeoBrutalist) p.text else p.muted
                        )
                    }
                } else null
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                DropdownField(
                    label = "",
                    valueLabel = typeLabels.getOrElse(typeFilter) { typeLabels.first() },
                    options = typeLabels.mapIndexed { i, s -> i to s },
                    onSelect = { typeFilter = it },
                    accessibleLabel = stringResource(R.string.type_label),
                    dense = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library-filter-type")
                )
                Spacer(Modifier.width(10.dp))
                DropdownField(
                    label = "",
                    valueLabel = statusValueLabel,
                    options = statusOptions.map { (filter, label) -> filter to "$label (${statusCounts[filter] ?: 0})" },
                    onSelect = { statusFilter = it },
                    accessibleLabel = stringResource(R.string.filter_status_label),
                    dense = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library-filter-status")
                )
                Spacer(Modifier.width(10.dp))
                DropdownField(
                    label = "",
                    valueLabel = stringResource(sortBy.labelRes),
                    options = sortOptions,
                    onSelect = { sortBy = it },
                    accessibleLabel = stringResource(R.string.sort_label),
                    dense = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library-filter-sort")
                )
                Spacer(Modifier.width(10.dp))
                Row(
                    modifier = if (p.isNeoBrutalist) {
                        Modifier
                            .neoBrutalist(
                                backgroundColor = p.surface,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.5.dp,
                                shadowOffset = 4.dp,
                                cornerRadius = 8.dp
                            )
                            .padding(4.dp)
                    } else {
                        Modifier
                            .liquidGlass(
                                shape = RoundedCornerShape(16.dp),
                                tint = p.card,
                                blurRadius = 12.dp,
                                refractionHeight = 8.dp,
                                refractionAmount = 12.dp,
                                chromaticAberration = false
                            )
                            .padding(4.dp)
                    },
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

            // Tag filter, only when at least one tag exists
            if (availableTags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        label = stringResource(R.string.all_tags),
                        selected = tagFilter == null,
                        onClick = { tagFilter = null }
                    )
                    availableTags.forEach { tag ->
                        FilterChip(
                            label = "#$tag",
                            selected = tagFilter.equals(tag, ignoreCase = true),
                            onClick = {
                                tagFilter = if (tagFilter.equals(tag, ignoreCase = true)) null else tag
                            }
                        )
                    }
                }
            }

            // F6: custom-list filter, only when at least one list exists
            if (availableLists.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        label = "Semua Daftar",
                        selected = listFilter == null,
                        onClick = { listFilter = null }
                    )
                    availableLists.forEach { name ->
                        FilterChip(
                            label = name,
                            selected = listFilter.equals(name, ignoreCase = true),
                            onClick = {
                                listFilter = if (listFilter.equals(name, ignoreCase = true)) null else name
                            }
                        )
                    }
                }
            }

            // Vetro P3a: rating & release-year range sliders.
            if (list.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.filter_rating_range),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = p.text,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${ratingMin.toInt()} – ${ratingMax.toInt()} ★",
                            fontSize = 12.sp,
                            color = p.muted
                        )
                    }
                    RangeSlider(
                        value = ratingMin..ratingMax,
                        onValueChange = { range ->
                            ratingMin = range.start
                            ratingMax = range.endInclusive
                        },
                        valueRange = 0f..5f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val bounds = yearDataBounds
                    if (bounds != null && bounds.first < bounds.second) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.filter_year_range),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = p.text,
                                modifier = Modifier.weight(1f)
                            )
                            val lo = if (yearMin == 0f) bounds.first else yearMin.toInt()
                            val hi = if (yearMax == 0f) bounds.second else yearMax.toInt()
                            Text("$lo – $hi", fontSize = 12.sp, color = p.muted)
                        }
                        RangeSlider(
                            value = (if (yearMin == 0f) bounds.first.toFloat() else yearMin)..
                                (if (yearMax == 0f) bounds.second.toFloat() else yearMax),
                            onValueChange = { range ->
                                yearMin = range.start
                                yearMax = range.endInclusive
                            },
                            valueRange = bounds.first.toFloat()..bounds.second.toFloat(),
                            steps = (bounds.second - bounds.first - 1).coerceAtLeast(0),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        val viewMode = when {
            filtered.isEmpty() -> 0
            gridView -> 1
            else -> 2
        }
        AnimatedContent(
            targetState = viewMode,
            modifier = Modifier.weight(1f),
            label = "libraryViewMode",
            transitionSpec = {
                (fadeIn(tween(200, easing = SerieeMotion.StandardEasing)) +
                    scaleIn(initialScale = 0.97f, animationSpec = tween(220, easing = SerieeMotion.EaseOutExpo))) togetherWith
                    fadeOut(tween(140, easing = SerieeMotion.StandardEasing))
            }
        ) { mode ->
            when (mode) {
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (list.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.VideoLibrary,
                            title = stringResource(R.string.empty_library_title),
                            hint = stringResource(R.string.empty_library_hint),
                            ctaLabel = stringResource(R.string.add_first_title),
                            onCta = onAdd,
                            mascot = true
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = if (query.isNotBlank()) {
                                stringResource(R.string.no_library_results, query)
                            } else {
                                stringResource(R.string.no_library_items)
                            },
                            hint = if (list.isEmpty()) {
                                stringResource(R.string.empty_library_hint)
                            } else {
                                // Titles exist; they are only hidden by the
                                // active search/type/status/tag filters.
                                stringResource(R.string.empty_library_filters_hint)
                            },
                            ctaLabel = stringResource(R.string.clear_filters),
                            onCta = {
                                query = ""
                                typeFilter = 0
                                statusFilter = LibraryFilter.All
                                tagFilter = null
                                ratingMin = 0f
                                ratingMax = 5f
                                yearMin = 0f
                                yearMax = 0f
                            }
                        )
                    }
                }
                1 -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomInset + 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filtered, key = { it.id }, contentType = { it.status }) { item ->
                        GridItem(
                            item = item,
                            onClick = { onOpen(item) },
                            onPin = { onPin(item.id) },
                            onRate = { ratingEditItem = item },
                            modifier = Modifier.animateItem(
                                fadeInSpec = SerieeMotion.enterTween(),
                                placementSpec = SerieeMotion.PlacementSpring,
                                fadeOutSpec = SerieeMotion.exitTween()
                            )
                        )
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomInset + 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }, contentType = { it.status }) { item ->
                        ListItem(
                            item = item,
                            onClick = { onOpen(item) },
                            onPin = { onPin(item.id) },
                            onRate = { ratingEditItem = item },
                            modifier = Modifier.animateItem(
                                fadeInSpec = SerieeMotion.enterTween(),
                                placementSpec = SerieeMotion.PlacementSpring,
                                fadeOutSpec = SerieeMotion.exitTween()
                            )
                        )
                    }
                }
            }
        }
    }

    // Vetro P3c: in-place rating dialog opened from a card's score badge.
    ratingEditItem?.let { target ->
        var draft by remember(target.id) { mutableIntStateOf(target.rating) }
        AlertDialog(
            onDismissRequest = { ratingEditItem = null },
            containerColor = p.surface,
            title = {
                Text(
                    stringResource(R.string.rating_edit_title),
                    fontWeight = FontWeight.Bold,
                    color = p.text
                )
            },
            text = {
                Column {
                    Text(target.title, fontSize = 13.sp, color = p.muted, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    StarRatingBar(
                        rating = draft,
                        onRatingChange = { draft = it },
                        starSize = 32.dp,
                        showLabel = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateRating(target, draft)
                    ratingEditItem = null
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingEditItem = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ViewToggle(selected: Boolean, onClick: () -> Unit, icon: @Composable (Color) -> Unit) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        val toggleShape = RoundedCornerShape(6.dp)
        val neoBg by animateColorAsState(
            targetValue = if (selected) NeoBrutalistColors.Yellow else Color.Transparent,
            animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
            label = "neoToggleBg"
        )
        Box(
            modifier = Modifier
                .clip(toggleShape)
                .background(neoBg)
                .then(
                    if (selected) Modifier.border(2.dp, NeoBrutalistColors.Black, toggleShape)
                    else Modifier
                )
                .clickable(onClick = onClick)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(if (selected) NeoBrutalistColors.Black else p.text)
        }
        return
    }
    val toggleBg by animateColorAsState(
        targetValue = if (selected) p.primary else Color.Transparent,
        animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
        label = "toggleBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) p.onAccent else p.muted,
        animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
        label = "toggleTint"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(toggleBg)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        icon(iconColor)
    }
}

@Composable
private fun GridItem(
    item: MediaItem,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onRate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val gradientBrush = remember {
        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
    }
    val now = rememberNow()
    val badgeText = remember(item.releaseDate, now) { ReminderScheduler.countdownBadgeText(context, item.releaseDate, now) }
    val airLabel = libraryBadgeLabel(context, item, now)
    val badgeLine = libraryBadgeLine(context, item, now).orEmpty()
    val showTopBadge = badgeLine.isNotEmpty()
    val badgeColor = if (airLabel != null) p.info else p.reminder

    val posterOverlay: @Composable () -> Unit = {
        val posterShape = RoundedCornerShape(if (p.isNeoBrutalist) 8.dp else 18.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .then(
                    if (p.isGlass) Modifier.border(0.75.dp, Color.White.copy(alpha = if (p.isLight) 0.7f else 0.25f), posterShape)
                    else Modifier
                )
        ) {
            Poster(item.poster, Modifier.fillMaxSize(), posterShape)
            if (showTopBadge) {
                val isAiring = airLabel != null
                val badgeShape = RoundedCornerShape(if (isAiring) 4.dp else 6.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        // Leave room for the TopEnd pin button so a full airing
                        // label can never run underneath it.
                        .padding(start = 6.dp, top = 6.dp, end = 44.dp)
                        .clip(badgeShape)
                        .background(if (p.isNeoBrutalist && item.status == WatchStatus.ComingSoon) NeoBrutalistColors.Yellow else badgeColor)
                        .then(
                            if (p.isNeoBrutalist) {
                                Modifier.border(if (isAiring) 1.dp else 1.5.dp, NeoBrutalistColors.Black, badgeShape)
                            } else Modifier
                        )
                        .padding(
                            horizontal = if (isAiring) 4.dp else 6.dp,
                            vertical = if (isAiring) 1.dp else 2.dp
                        )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.reminderEnabled) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                null,
                                tint = if (p.isNeoBrutalist) NeoBrutalistColors.Black else contrastOn(badgeColor),
                                modifier = Modifier.size(if (isAiring) 8.dp else 10.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(
                            badgeLine,
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontSize = if (isAiring) 8.sp else 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else contrastOn(badgeColor)
                        )
                    }
                }

                if (showCountdownStrip(item)) {
                // Bottom Countdown Overlay Strip
                if (p.isNeoBrutalist) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeoBrutalistColors.Pink)
                            .border(1.5.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.HourglassTop,
                                contentDescription = null,
                                tint = NeoBrutalistColors.Black,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                badgeText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeoBrutalistColors.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
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
                                tint = p.reminder,
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
                }
            }
            if (item.rating > 0) {
                StarRatingBadge(
                    item.rating,
                    modifier = Modifier
                        .align(if (item.status == WatchStatus.ComingSoon) Alignment.BottomEnd else Alignment.BottomStart)
                        .padding(4.dp)
                        // Vetro P3c: tap the score to change it in place.
                        .clickable { onRate() }
                )
            }
            if (p.isNeoBrutalist) {
                NeoBrutalistBox(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp),
                    onClick = onPin,
                    role = Role.Button,
                    backgroundColor = if (item.pinned) NeoBrutalistColors.Yellow else NeoBrutalistColors.PureWhite,
                    borderColor = NeoBrutalistColors.Black,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    cornerRadius = 6.dp,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.pin),
                        tint = NeoBrutalistColors.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = if (p.isGlass) 0.45f else 0.5f))
                        .then(
                            if (p.isGlass) Modifier.border(0.75.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                            else Modifier
                        )
                        .clickable(onClick = onPin),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.pin),
                        tint = if (item.pinned) p.star else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }

    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            modifier = modifier,
            onClick = onClick,
            backgroundColor = p.card,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(8.dp)
        ) {
            Column {
                posterOverlay()
                Spacer(Modifier.size(8.dp))
                Text(
                    item.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp).basicMarquee()
                )
                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    ) {
                        item.tags.take(2).forEach { tag ->
                            GenreTagBadge(tag, modifier = Modifier.weight(1f, fill = false))
                        }
                        if (item.tags.size > 2) {
                            GenreTagBadge("+${item.tags.size - 2}", color = NeoBrutalistColors.Yellow)
                        }
                    }
                }
            }
        }
        return
    }

    val gridInteraction = remember { MutableInteractionSource() }
    val gridPressed by gridInteraction.collectIsPressedAsState()
    val gridScale by animateFloatAsState(
        targetValue = if (gridPressed) 0.96f else 1f,
        animationSpec = SerieeMotion.PressSpring,
        label = "gridCardScale"
    )
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = gridScale
                scaleY = gridScale
            }
            .clickable(
                interactionSource = gridInteraction,
                indication = null,
                onClick = onClick
            )
    ) {
        posterOverlay()
        Spacer(Modifier.size(6.dp))
        Text(
            item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = p.text,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp).basicMarquee()
        )
        if (item.tags.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                item.tags.take(2).forEach { tag ->
                    GenreTagBadge(tag, modifier = Modifier.weight(1f, fill = false))
                }
                if (item.tags.size > 2) {
                    GenreTagBadge("+${item.tags.size - 2}")
                }
            }
        }
    }
}

@Composable
private fun ListItem(
    item: MediaItem,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onRate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val shape = remember { RoundedCornerShape(22.dp) }
    val now = rememberNow()
    val detailedCountdown = remember(item.releaseDate, now) {
        ReminderScheduler.countdownDetailedText(context, item.releaseDate, now)
    }
    val shortBadge = remember(item.releaseDate, now) {
        ReminderScheduler.countdownShortBadge(context, item.releaseDate, now)
    }
    val airLabel = libraryBadgeLabel(context, item, now)

    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Poster(item.poster, Modifier.size(width = 48.dp, height = 72.dp))
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
                                tint = if (p.isNeoBrutalist) p.text else p.star,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            item.title,
                            fontSize = 14.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                            color = p.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (item.rating > 0) {
                        Spacer(Modifier.width(6.dp))
                        StarRatingBadge(
                            rating = item.rating,
                            // Vetro P3c: tap the score to change it in place.
                            modifier = Modifier.clickable { onRate() }
                        )
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
                            tint = if (p.isNeoBrutalist) p.text else p.reminder,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${stringResource(item.type.labelRes)} • $detailedCountdown",
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontSize = 12.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Medium,
                            color = if (p.isNeoBrutalist) p.text else p.reminder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    val progressLabel = if (item.type == MediaType.Movie) {
                        stringResource(if (item.watchedEpisodes > 0) R.string.movie_watched else R.string.movie_not_watched)
                    } else {
                        stringResource(R.string.episode_progress, item.watchedEpisodes, item.totalEpisodes)
                    }
                    Text(
                        "${stringResource(item.type.labelRes)} • $progressLabel${airLabel?.let { " · $it" } ?: ""}",
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                        color = p.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
            val badgeColor = statusColor(item.status)
            if (p.isNeoBrutalist) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor)
                        .border(2.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.status == WatchStatus.ComingSoon && item.reminderEnabled) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            (if (item.status == WatchStatus.ComingSoon) shortBadge
                            else stringResource(item.status.labelRes)).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeoBrutalistColors.Black
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                NeoBrutalistBox(
                    modifier = Modifier.size(40.dp),
                    onClick = onPin,
                    role = Role.Button,
                    backgroundColor = if (item.pinned) NeoBrutalistColors.Yellow else p.chip,
                    borderColor = p.border,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    cornerRadius = 6.dp,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.pin),
                        tint = if (item.pinned) NeoBrutalistColors.Black else p.text,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.status == WatchStatus.ComingSoon && item.reminderEnabled) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = badgeColor, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            if (item.status == WatchStatus.ComingSoon) shortBadge
                            else stringResource(item.status.labelRes),
                            fontSize = 11.sp,
                            fontWeight = if (item.status == WatchStatus.ComingSoon) FontWeight.Bold else FontWeight.Medium,
                            color = badgeColor
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
                        tint = if (item.pinned) p.star else p.muted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            backgroundColor = p.card,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.5.dp,
            shadowOffset = 4.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(12.dp)
        ) {
            rowContent()
        }
    } else {
        val listInteraction = remember { MutableInteractionSource() }
        val listPressed by listInteraction.collectIsPressedAsState()
        val listScale by animateFloatAsState(
            targetValue = if (listPressed) 0.985f else 1f,
            animationSpec = SerieeMotion.PressSpring,
            label = "listCardScale"
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = listScale
                    scaleY = listScale
                }
                .liquidGlass(
                    shape = shape,
                    tint = p.card,
                    blurRadius = 12.dp,
                    refractionHeight = 10.dp,
                    refractionAmount = 16.dp,
                    chromaticAberration = false
                )
                .clickable(
                    interactionSource = listInteraction,
                    indication = null,
                    onClick = onClick
                )
                .padding(12.dp)
        ) {
            rowContent()
        }
    }
}
