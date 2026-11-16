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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    list: List<MediaItem>,
    gridView: Boolean,
    padding: PaddingValues,
    onSetGrid: (Boolean) -> Unit,
    onOpen: (MediaItem) -> Unit,
    onPin: (String) -> Unit,
    onAdd: () -> Unit,
    searchFocusTick: Int = 0
) {
    val p = LocalPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableIntStateOf(0) }
    var statusFilter by rememberSaveable { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchFocusTick) {
        if (searchFocusTick > 0) runCatching { focusRequester.requestFocus() }
    }

    val typeLabels = listOf(
        stringResource(R.string.all_types),
        stringResource(R.string.type_series),
        stringResource(R.string.type_movie)
    )

    val statusCounts = remember(list) {
        listOf(
            list.size,
            list.count { it.status == WatchStatus.Watching },
            list.count { it.status == WatchStatus.Completed },
            list.count { it.status == WatchStatus.PlanToWatch },
            list.count { it.status == WatchStatus.ComingSoon },
            list.count { it.status == WatchStatus.OnHold },
            list.count { it.status == WatchStatus.Dropped }
        )
    }

    val statusChips = listOf(
        Triple(stringResource(R.string.filter_all), null, statusCounts[0]),
        Triple(stringResource(R.string.filter_watching), WatchStatus.Watching, statusCounts[1]),
        Triple(stringResource(R.string.filter_completed), WatchStatus.Completed, statusCounts[2]),
        Triple(stringResource(R.string.filter_plan_to_watch), WatchStatus.PlanToWatch, statusCounts[3]),
        Triple(stringResource(R.string.filter_coming_soon), WatchStatus.ComingSoon, statusCounts[4]),
        Triple(stringResource(R.string.filter_on_hold), WatchStatus.OnHold, statusCounts[5]),
        Triple(stringResource(R.string.filter_dropped), WatchStatus.Dropped, statusCounts[6])
    )

    val filtered by remember(list, query, typeFilter, statusFilter) {
        derivedStateOf {
            val status = statusChips.getOrNull(statusFilter)?.second
            list.filter { it.title.contains(query.trim(), ignoreCase = true) }
                .filter {
                    when (typeFilter) {
                        1 -> it.type == MediaType.Series
                        2 -> it.type == MediaType.Movie
                        else -> true
                    }
                }
                .filter { status == null || it.status == status }
                .sortedByDescending { it.pinned }
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
                modifier = Modifier.focusRequester(focusRequester),
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
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
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

            // Status filter chips, wrapping so the pager still receives swipes
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                statusChips.forEachIndexed { index, (label, _, count) ->
                    FilterChip(
                        label = "$label ($count)",
                        selected = statusFilter == index,
                        onClick = { statusFilter = index }
                    )
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
                            onCta = onAdd
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Filled.SearchOff,
                            title = if (query.isNotBlank()) {
                                stringResource(R.string.no_library_results, query)
                            } else {
                                stringResource(R.string.no_library_items)
                            },
                            hint = stringResource(R.string.empty_library_hint),
                            ctaLabel = stringResource(R.string.clear_filters),
                            onCta = {
                                query = ""
                                typeFilter = 0
                                statusFilter = 0
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
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(200, easing = SerieeMotion.StandardEasing),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = 380f
                                ),
                                fadeOutSpec = tween(150, easing = SerieeMotion.StandardEasing)
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
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(200, easing = SerieeMotion.StandardEasing),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = 380f
                                ),
                                fadeOutSpec = tween(150, easing = SerieeMotion.StandardEasing)
                            )
                        )
                    }
                }
            }
        }
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
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val gradientBrush = remember {
        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
    }
    val badgeShort = remember(item.releaseDate) { ReminderScheduler.countdownShortBadge(context, item.releaseDate) }
    val badgeText = remember(item.releaseDate) { ReminderScheduler.countdownBadgeText(context, item.releaseDate) }

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
            if (item.status == WatchStatus.ComingSoon) {
                val badgeShape = RoundedCornerShape(6.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(badgeShape)
                        .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.reminder)
                        .then(
                            if (p.isNeoBrutalist) Modifier.border(1.5.dp, NeoBrutalistColors.Black, badgeShape)
                            else Modifier
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.reminderEnabled) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                null,
                                tint = if (p.isNeoBrutalist) NeoBrutalistColors.Black else contrastOn(p.reminder),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                        }
                        Text(
                            badgeShort,
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else contrastOn(p.reminder)
                        )
                    }
                }

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
            if (item.rating > 0) {
                StarRatingBadge(
                    item.rating,
                    modifier = Modifier
                        .align(if (item.status == WatchStatus.ComingSoon) Alignment.BottomEnd else Alignment.BottomStart)
                        .padding(4.dp)
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                )
                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    ) {
                        item.tags.take(2).forEach { tag ->
                            GenreTagBadge(tag)
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
        animationSpec = SerieeMotion.SnappySpring,
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )
        if (item.tags.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                item.tags.take(2).forEach { tag ->
                    GenreTagBadge(tag)
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
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val shape = remember { RoundedCornerShape(22.dp) }
    val detailedCountdown = remember(item.releaseDate) {
        ReminderScheduler.countdownDetailedText(context, item.releaseDate)
    }
    val shortBadge = remember(item.releaseDate) {
        ReminderScheduler.countdownShortBadge(context, item.releaseDate)
    }

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
                        "${stringResource(item.type.labelRes)} • $progressLabel",
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
            animationSpec = SerieeMotion.SnappySpring,
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
