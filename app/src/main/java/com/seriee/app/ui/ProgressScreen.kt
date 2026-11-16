package com.seriee.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    list: List<MediaItem>,
    padding: PaddingValues,
    onOpen: (MediaItem) -> Unit,
    onIncrement: (String) -> Unit,
    streakDays: Int = 0,
    bestStreak: Int = 0,
    watchDays: Set<String> = emptySet(),
    onAdd: () -> Unit = {},
    searchFocusTick: Int = 0,
    comingSoonTick: Int = 0,
    onSearchHandled: () -> Unit = {},
    onComingSoonHandled: () -> Unit = {}
) {
    val p = LocalPalette.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    // The ticks are one-shot requests from the top bar. Consume them so that
    // returning to Progress later keeps whatever filter/search the user left.
    LaunchedEffect(searchFocusTick) {
        if (searchFocusTick > 0) {
            runCatching { focusRequester.requestFocus() }
            onSearchHandled()
        }
    }
    LaunchedEffect(comingSoonTick) {
        if (comingSoonTick > 0) {
            filter = 4
            onComingSoonHandled()
        }
    }

    val filters = listOf(
        stringResource(R.string.filter_all) to null,
        stringResource(R.string.filter_watching) to WatchStatus.Watching,
        stringResource(R.string.filter_completed) to WatchStatus.Completed,
        stringResource(R.string.filter_plan_to_watch) to WatchStatus.PlanToWatch,
        stringResource(R.string.filter_coming_soon) to WatchStatus.ComingSoon,
        stringResource(R.string.filter_on_hold) to WatchStatus.OnHold,
        stringResource(R.string.filter_dropped) to WatchStatus.Dropped
    )
    val filterCounts = remember(list) {
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

    val filtered by remember(list, query, filter) {
        derivedStateOf {
            val status = filters.getOrNull(filter)?.second
            list.filter { it.title.contains(query.trim(), ignoreCase = true) }
                .filter { status == null || it.status == status }
        }
    }

    val topInset = padding.calculateTopPadding()
    val bottomInset = padding.calculateBottomPadding()

    // One scroll container for the whole screen. On short/narrow devices the
    // header alone can be taller than the viewport; keeping it fixed would push
    // the empty state (and its CTA) off-screen and underneath the floating nav
    // bar. The bottom content padding reserves that chrome so the last element
    // can always be scrolled above it.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = topInset + 4.dp,
            bottom = bottomInset + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "progress-header") {
            Column(
                Modifier.fillMaxWidth(),
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
                    fontWeight = if (p.isNeoBrutalist) FontWeight.SemiBold else FontWeight.Normal,
                    color = p.muted
                )

                StreakCard(streakDays = streakDays, bestStreak = bestStreak, watchDays = watchDays)

                SField(
                    value = query,
                    onChange = { query = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    placeholder = stringResource(R.string.search_progress_placeholder),
                    filled = true,
                    leading = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailing = if (query.isNotBlank()) {
                        {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.clear_search),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { query = "" },
                                tint = if (p.isNeoBrutalist) p.text else p.muted
                            )
                        }
                    } else null
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.forEachIndexed { index, (label, _) ->
                        FilterChip(
                            label = "$label (${filterCounts[index]})",
                            selected = filter == index,
                            onClick = { filter = index }
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item(key = "progress-empty") {
                if (query.isBlank() && list.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.LiveTv,
                        title = stringResource(R.string.empty_progress_title),
                        hint = stringResource(R.string.empty_progress_hint),
                        ctaLabel = stringResource(R.string.add_first_title),
                        onCta = onAdd,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                } else {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = if (query.isNotBlank()) {
                            stringResource(R.string.no_search_results, query)
                        } else {
                            stringResource(R.string.no_shows)
                        },
                        hint = stringResource(R.string.empty_progress_hint),
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }, contentType = { it.type }) { item ->
                ProgressCard(
                    item = item,
                    onClick = { onOpen(item) },
                    onIncrement = { onIncrement(item.id) },
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

@Composable
private fun ProgressCard(
    item: MediaItem,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val shape = remember { RoundedCornerShape(22.dp) }
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

    val cardContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(width = 68.dp, height = 86.dp)) {
                Poster(item.poster, Modifier.fillMaxSize())
                if (item.status == WatchStatus.ComingSoon) {
                    val badgeShape = RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 4.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(3.dp)
                            .clip(badgeShape)
                            .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.reminder)
                            .then(
                                if (p.isNeoBrutalist) Modifier.border(1.5.dp, NeoBrutalistColors.Black, badgeShape)
                                else Modifier
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            badgeText,
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else contrastOn(p.reminder)
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
                    if (p.isNeoBrutalist) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor(item.status))
                                .border(1.5.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                (if (item.type == MediaType.Series) stringResource(R.string.season_format, item.season)
                                else stringResource(R.string.movie)).uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeoBrutalistColors.Black
                            )
                        }
                    } else {
                        Text(
                            if (item.type == MediaType.Series) stringResource(R.string.season_format, item.season)
                            else stringResource(R.string.movie),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor(item.status)
                        )
                    }
                    Text(
                        relTime,
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 10.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                        color = p.muted
                    )
                }

                Spacer(Modifier.height(3.dp))
                Text(
                    item.title,
                    fontSize = 14.sp,
                    fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                    color = p.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
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
                                tint = if (p.isNeoBrutalist) p.text else p.reminder,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                badgeLong,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (p.isNeoBrutalist) p.text else p.reminder
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.reminder.copy(alpha = 0.2f))
                                .then(
                                    if (p.isNeoBrutalist) Modifier.border(1.5.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                badgeText,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.reminder
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    GradientProgressBar(
                        fraction = visualProgress,
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (p.isNeoBrutalist) listOf(p.reminder) else listOf(p.reminder, p.star),
                        track = if (p.isNeoBrutalist) p.chip else p.reminder.copy(alpha = 0.15f),
                        height = if (p.isNeoBrutalist) 12.dp else 6.dp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        detailedText,
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
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
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontSize = 12.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                            color = p.muted
                        )
                        Text(
                            "${item.percent}%",
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    GradientProgressBar(
                        fraction = item.fraction,
                        modifier = Modifier.fillMaxWidth(),
                        height = if (p.isNeoBrutalist) 12.dp else 6.dp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            if (p.isNeoBrutalist) {
                when {
                    item.status == WatchStatus.ComingSoon -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (item.reminderEnabled) NeoBrutalistColors.Pink else p.chip)
                                .border(2.dp, p.border, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.Alarm,
                                contentDescription = stringResource(R.string.reminder),
                                tint = if (item.reminderEnabled) NeoBrutalistColors.Black else p.text,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    item.watchedEpisodes < item.totalEpisodes -> {
                        NeoBrutalistBox(
                            modifier = Modifier.size(44.dp),
                            onClick = onIncrement,
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.Yellow,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            cornerRadius = 6.dp,
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource(R.string.add_one_episode),
                                tint = NeoBrutalistColors.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeoBrutalistColors.Mint)
                                .border(2.dp, p.border, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = stringResource(R.string.completed),
                                tint = NeoBrutalistColors.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                if (item.status == WatchStatus.ComingSoon) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.chip),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.Alarm,
                            contentDescription = stringResource(R.string.reminder),
                            tint = if (item.reminderEnabled) p.warning else p.muted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (item.watchedEpisodes < item.totalEpisodes) {
                    val incInteraction = remember { MutableInteractionSource() }
                    val incPressed by incInteraction.collectIsPressedAsState()
                    val incScale by animateFloatAsState(
                        targetValue = if (incPressed) 0.88f else 1f,
                        animationSpec = SerieeMotion.SnappySpring,
                        label = "incBtnScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = incScale
                                scaleY = incScale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.primary.copy(alpha = 0.2f))
                            .clickable(
                                interactionSource = incInteraction,
                                indication = null,
                                role = Role.Button,
                                onClick = onIncrement
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_one_episode), tint = p.primary, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.success.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.completed), tint = p.success, modifier = Modifier.size(18.dp))
                    }
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
            contentPadding = PaddingValues(14.dp)
        ) {
            cardContent()
        }
    } else {
        val cardInteraction = remember { MutableInteractionSource() }
        val cardPressed by cardInteraction.collectIsPressedAsState()
        val cardScale by animateFloatAsState(
            targetValue = if (cardPressed) 0.985f else 1f,
            animationSpec = SerieeMotion.SnappySpring,
            label = "progressCardScale"
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
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
                    interactionSource = cardInteraction,
                    indication = null,
                    onClick = onClick
                )
                .padding(14.dp)
        ) {
            cardContent()
        }
    }
}

@Composable
private fun StreakCard(streakDays: Int, bestStreak: Int, watchDays: Set<String>) {
    val p = LocalPalette.current
    val configuration = LocalConfiguration.current
    val shape = remember { RoundedCornerShape(22.dp) }
    // The seven day circles take ~192dp of fixed width; below ~400dp (or with
    // enlarged fonts) the text column is squeezed to ~60dp and wraps mid-word.
    val stacked = configuration.screenWidthDp < 400 || configuration.fontScale > 1.15f
    val cardModifier = if (p.isNeoBrutalist) {
        Modifier
            .fillMaxWidth()
            .neoBrutalist(
                backgroundColor = p.card,
                borderColor = p.border,
                shadowColor = NeoBrutalistColors.Black,
                borderWidth = 2.5.dp,
                shadowOffset = 4.dp,
                cornerRadius = 8.dp
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = shape,
                tint = p.card,
                blurRadius = 12.dp,
                refractionHeight = 10.dp,
                refractionAmount = 16.dp,
                chromaticAberration = false
            )
            .padding(horizontal = 14.dp, vertical = 11.dp)
    }

    if (stacked) {
        Column(cardModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StreakSummary(
                streakDays = streakDays,
                bestStreak = bestStreak,
                modifier = Modifier.fillMaxWidth()
            )
            StreakDays(watchDays = watchDays)
        }
    } else {
        Row(cardModifier, verticalAlignment = Alignment.CenterVertically) {
            StreakSummary(
                streakDays = streakDays,
                bestStreak = bestStreak,
                modifier = Modifier.weight(1f)
            )
            StreakDays(watchDays = watchDays)
        }
    }
}

@Composable
private fun StreakSummary(streakDays: Int, bestStreak: Int, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val boxShape = RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 12.dp)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(boxShape)
                .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.reminder.copy(alpha = 0.15f))
                .then(
                    if (p.isNeoBrutalist) Modifier.border(2.dp, NeoBrutalistColors.Black, boxShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Whatshot,
                contentDescription = stringResource(R.string.streak_label),
                tint = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.reminder,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                stringResource(R.string.streak_label).let { if (p.isNeoBrutalist) it.uppercase() else it },
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 11.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (streakDays > 0) stringResource(R.string.streak_days, streakDays) else "—",
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = p.text,
                maxLines = 1
            )
            Text(
                stringResource(R.string.best_streak, bestStreak),
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 11.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StreakDays(watchDays: Set<String>) {
    val p = LocalPalette.current
    val configuration = LocalConfiguration.current
    val today = remember { java.time.LocalDate.now() }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        for (offset in 6 downTo 0) {
            val date = today.minusDays(offset.toLong())
            val active = watchDays.contains(date.toString())
            val letter = date.dayOfWeek.getDisplayName(
                java.time.format.TextStyle.NARROW,
                configuration.primaryLocale()
            ).uppercase().take(1)
            val dayShape = if (p.isNeoBrutalist) RoundedCornerShape(6.dp) else RoundedCornerShape(50)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(dayShape)
                    .background(
                        when {
                            p.isNeoBrutalist && active -> NeoBrutalistColors.Mint
                            p.isNeoBrutalist && date == today -> NeoBrutalistColors.Yellow
                            p.isNeoBrutalist -> p.chip
                            active -> p.reminder
                            else -> p.chip
                        }
                    )
                    .border(
                        width = if (p.isNeoBrutalist) (if (date == today) 2.5.dp else 2.dp) else 1.dp,
                        color = when {
                            p.isNeoBrutalist -> p.border
                            date == today -> p.reminder
                            else -> Color.Transparent
                        },
                        shape = dayShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letter,
                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        p.isNeoBrutalist && (active || date == today) -> NeoBrutalistColors.Black
                        p.isNeoBrutalist -> p.text
                        active -> contrastOn(p.reminder)
                        else -> p.muted
                    }
                )
            }
        }
    }
}
