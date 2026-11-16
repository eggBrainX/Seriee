package com.seriee.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.Insights
import com.seriee.app.data.InsightsCalculator
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightsScreen(
    list: List<MediaItem>,
    history: List<WatchEvent>,
    watchDays: Set<String>,
    padding: PaddingValues,
    onOpen: (MediaItem) -> Unit,
    onAdd: () -> Unit,
    onClearHistory: () -> Unit
) {
    val p = LocalPalette.current
    val insights = remember(list, history, watchDays) {
        InsightsCalculator.calculate(list, history, watchDays)
    }
    var confirmClear by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 4.dp,
            bottom = padding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "insights-header") {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.insights_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text
                )
                Text(
                    stringResource(R.string.insights_subtitle),
                    fontSize = 12.sp,
                    fontWeight = if (p.isNeoBrutalist) FontWeight.SemiBold else FontWeight.Normal,
                    color = p.muted
                )
            }
        }

        if (list.isEmpty()) {
            item(key = "insights-empty") {
                EmptyState(
                    icon = Icons.Filled.Insights,
                    title = stringResource(R.string.empty_library_title),
                    hint = stringResource(R.string.empty_library_hint),
                    ctaLabel = stringResource(R.string.add_first_title),
                    onCta = onAdd,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
            if (insights.recent.isNotEmpty()) {
                item(key = "insights-empty-recent") {
                    RecentActivityCard(
                        insights = insights,
                        items = list,
                        onOpen = onOpen,
                        onClear = { confirmClear = true }
                    )
                }
            }
        } else {
            item(key = "insights-hero") { HeroCard(insights) }
            item(key = "insights-tiles") { QuickTiles(insights) }
            item(key = "insights-status") { StatusCard(insights) }
            item(key = "insights-ratings") { RatingsCard(insights) }
            if (insights.topTags.isNotEmpty()) {
                item(key = "insights-tags") { TagsCard(insights) }
            }
            item(key = "insights-activity") { ActivityCard(insights) }
            item(key = "insights-recent") {
                RecentActivityCard(
                    insights = insights,
                    items = list,
                    onOpen = onOpen,
                    onClear = { confirmClear = true }
                )
            }
            item(key = "insights-footer") {
                Text(
                    stringResource(R.string.insights_watch_time_note),
                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                    color = p.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }
    }

    if (confirmClear) {
        if (p.isNeoBrutalist) {
            NeoBrutalistConfirmDialog(
                title = stringResource(R.string.insights_clear_history_title),
                message = stringResource(R.string.insights_clear_history_message),
                confirmLabel = stringResource(R.string.insights_clear_history_confirm),
                dismissLabel = stringResource(R.string.cancel),
                confirmColor = NeoBrutalistColors.CoralRed,
                onConfirm = {
                    confirmClear = false
                    onClearHistory()
                },
                onDismiss = { confirmClear = false }
            )
        } else {
            AlertDialog(
                onDismissRequest = { confirmClear = false },
                containerColor = p.elevated,
                titleContentColor = p.text,
                textContentColor = p.muted,
                title = { Text(stringResource(R.string.insights_clear_history_title)) },
                text = { Text(stringResource(R.string.insights_clear_history_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirmClear = false
                        onClearHistory()
                    }) {
                        Text(stringResource(R.string.insights_clear_history_confirm), color = p.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmClear = false }) {
                        Text(stringResource(R.string.cancel), color = p.muted)
                    }
                }
            )
        }
    }
}

@Composable
private fun HeroCard(insights: Insights) {
    val p = LocalPalette.current
    val hours = insights.estimatedMinutes / 60f
    val hoursLabel = String.format(Locale.getDefault(), "%.1f", hours)
    val heroDescription = pluralStringResource(
        R.plurals.insights_a11y_hero,
        insights.watchedEpisodes,
        insights.watchedEpisodes,
        insights.totalSeries
    )
    val completedLabel = pluralStringResource(
        R.plurals.insights_completed_titles,
        insights.totalTitles,
        insights.completedTitles,
        insights.totalTitles
    )

    SerieeSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = heroDescription }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "${insights.watchedEpisodes}",
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = p.text
            )
            Text(
                stringResource(R.string.insights_stat_episodes).let { if (p.isNeoBrutalist) it.uppercase() else it },
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 12.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(
                    label = stringResource(R.string.insights_stat_hours),
                    value = stringResource(R.string.insights_hours_value, hoursLabel),
                    neoFill = NeoBrutalistColors.SkyBlue,
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    label = stringResource(R.string.insights_stat_completion),
                    value = "${insights.completionRate}%",
                    neoFill = NeoBrutalistColors.Pink,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    completedLabel,
                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                    color = p.muted
                )
                Text(
                    "${insights.completionRate}%",
                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text
                )
            }
            GradientProgressBar(
                fraction = insights.completionRate / 100f,
                modifier = Modifier.fillMaxWidth(),
                colors = if (p.isNeoBrutalist) listOf(p.primary) else null,
                height = if (p.isNeoBrutalist) 14.dp else 7.dp
            )
        }
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    neoFill: Color = NeoBrutalistColors.SkyBlue,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        val shape = RoundedCornerShape(6.dp)
        Column(
            modifier
                .clip(shape)
                .background(neoFill)
                .border(2.dp, NeoBrutalistColors.Black, shape)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                label.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeoBrutalistColors.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                value,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeoBrutalistColors.Black
            )
        }
        return
    }
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (p.isGlass) p.chip.copy(alpha = 0.55f) else p.chip)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 10.sp, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.text)
    }
}

@Composable
private fun QuickTiles(insights: Insights) {
    val locale = LocalConfiguration.current.primaryLocale()
    val seriesDescription = pluralStringResource(
        R.plurals.insights_title_count,
        insights.totalSeries,
        insights.totalSeries
    )
    val moviesDescription = pluralStringResource(
        R.plurals.insights_title_count,
        insights.totalMovies,
        insights.totalMovies
    )
    val ratingDescription = if (insights.ratedTitles == 0) {
        stringResource(R.string.rating_none)
    } else {
        String.format(locale, "%.1f", insights.averageRating)
    }
    val ratingValue = if (insights.ratedTitles == 0) "—" else ratingDescription

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tile(
            label = stringResource(R.string.insights_stat_series),
            value = "${insights.totalSeries}",
            description = seriesDescription,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        Tile(
            label = stringResource(R.string.insights_stat_movies),
            value = "${insights.totalMovies}",
            description = moviesDescription,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        Tile(
            label = stringResource(R.string.insights_stat_avg_rating),
            value = ratingValue,
            description = ratingDescription,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun Tile(
    label: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    SerieeSurface(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$label: $description" }
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = p.text,
                maxLines = 1
            )
            Text(
                label.let { if (p.isNeoBrutalist) it.uppercase() else it },
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 10.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun StatusCard(insights: Insights) {
    val p = LocalPalette.current
    val total = insights.statusCounts.values.sum()

    SerieeSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(stringResource(R.string.insights_status_title))

            val barShape = RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 50.dp)
            if (p.isNeoBrutalist) {
                val activeSegments = insights.statusCounts.entries.filter { it.value > 0 }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 2.dp, bottom = 2.dp)
                        .height(16.dp)
                        .drawBehind {
                            val shadowPx = 2.dp.toPx()
                            val radiusPx = 6.dp.toPx()
                            drawRoundRect(
                                color = NeoBrutalistColors.Black,
                                topLeft = Offset(shadowPx, shadowPx),
                                size = size,
                                cornerRadius = CornerRadius(radiusPx, radiusPx)
                            )
                        }
                        .clip(barShape)
                        .background(p.chip)
                        .border(2.5.dp, p.border, barShape)
                ) {
                    if (total > 0) {
                        activeSegments.forEachIndexed { idx, (status, count) ->
                            Box(
                                Modifier
                                    .weight(count.toFloat())
                                    .fillMaxHeight()
                                    .background(statusColor(status))
                            )
                            if (idx < activeSegments.lastIndex) {
                                Box(
                                    Modifier
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(p.border)
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(barShape)
                        .background(p.chip)
                ) {
                    if (total > 0) {
                        insights.statusCounts.forEach { (status, count) ->
                            if (count > 0) {
                                Box(
                                    Modifier
                                        .weight(count.toFloat())
                                        .fillMaxHeight()
                                        .background(statusColor(status))
                                )
                            }
                        }
                    }
                }
            }

            insights.statusCounts.forEach { (status, count) ->
                val percent = if (total == 0) 0 else count * 100 / total
                val rowDescription = stringResource(
                    R.string.insights_status_row,
                    stringResource(status.labelRes),
                    count,
                    percent
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) { contentDescription = rowDescription },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotShape = if (p.isNeoBrutalist) RoundedCornerShape(3.dp) else CircleShape
                    Box(
                        Modifier
                            .size(if (p.isNeoBrutalist) 11.dp else 8.dp)
                            .clip(dotShape)
                            .background(statusColor(status).copy(alpha = if (count == 0) 0.35f else 1f))
                            .then(
                                if (p.isNeoBrutalist) Modifier.border(1.5.dp, NeoBrutalistColors.Black, dotShape)
                                else Modifier
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(status.labelRes),
                        fontSize = 12.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                        color = p.text,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$count",
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = p.text
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "$percent%",
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                        color = p.muted,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingsCard(insights: Insights) {
    val p = LocalPalette.current
    val maxCount = (1..5).maxOfOrNull { insights.ratingCounts[it] } ?: 0

    SerieeSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(stringResource(R.string.insights_ratings_title))
            for (rating in 5 downTo 1) {
                val count = insights.ratingCounts.getOrElse(rating) { 0 }
                val fraction = if (maxCount == 0) 0f else count.toFloat() / maxCount
                val rowDescription = stringResource(R.string.insights_rating_row, rating, count)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) { contentDescription = rowDescription },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (p.isNeoBrutalist) p.text else p.star,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "$rating",
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = p.text,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    GradientProgressBar(
                        fraction = fraction,
                        modifier = Modifier.weight(1f),
                        colors = if (p.isNeoBrutalist) listOf(p.star) else listOf(p.primary, p.accent),
                        track = p.chip,
                        height = if (p.isNeoBrutalist) 12.dp else 6.dp
                    )
                    Text(
                        "$count",
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                        color = p.muted,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsCard(insights: Insights) {
    val p = LocalPalette.current

    SerieeSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(stringResource(R.string.insights_tags_title))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                insights.topTags.forEachIndexed { idx, (tag, count) ->
                    if (p.isNeoBrutalist) {
                        val chipFill = when (idx % 3) {
                            0 -> NeoBrutalistColors.SkyBlue
                            1 -> NeoBrutalistColors.Yellow
                            else -> NeoBrutalistColors.Pink
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(chipFill)
                                .border(2.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                stringResource(R.string.insights_tag_chip, tag, count),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeoBrutalistColors.Black
                            )
                        }
                    } else {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(
                                    if (p.isGlass) {
                                        if (p.isLight) Color.White.copy(alpha = 0.56f) else Color.White.copy(alpha = 0.12f)
                                    } else p.chip
                                )
                                .border(
                                    width = if (p.isGlass) 0.75.dp else 1.dp,
                                    color = if (p.isGlass) Color.White.copy(alpha = if (p.isLight) 0.7f else 0.22f) else p.border,
                                    shape = CircleShape
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                stringResource(R.string.insights_tag_chip, tag, count),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = p.text
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(insights: Insights) {
    val p = LocalPalette.current
    val configuration = LocalConfiguration.current
    val locale = configuration.primaryLocale()
    val today = remember { LocalDate.now() }
    val gridStart = remember(today) {
        val thisMonday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        thisMonday.minusWeeks(11)
    }
    val weeks = remember(gridStart) {
        (0 until 12).map { week ->
            (0 until 7).map { day -> gridStart.plusDays(week * 7L + day) }
        }
    }
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("d MMM", locale) }
    val totalInWindow = insights.dailyCounts.values.sum()
    val activityTotal = pluralStringResource(
        R.plurals.insights_episode_count,
        totalInWindow,
        totalInWindow
    )

    SerieeSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(
                title = stringResource(R.string.insights_activity_title),
                subtitle = stringResource(R.string.insights_activity_subtitle)
            )

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cell = ((maxWidth - 55.dp) / 12f).coerceIn(10.dp, 22.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        Modifier.padding(start = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        weeks.forEachIndexed { index, week ->
                            val monday = week.first()
                            val previous = weeks.getOrNull(index - 1)?.first()
                            Box(Modifier.size(cell)) {
                                if (previous == null || previous.month != monday.month) {
                                    Text(
                                        monday.month.getDisplayName(TextStyle.SHORT, locale),
                                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                        fontSize = 7.sp,
                                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                        color = p.muted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            (0 until 7).forEach { day ->
                                Box(
                                    Modifier
                                        .width(19.dp)
                                        .height(cell),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        gridStart.plusDays(day.toLong()).dayOfWeek
                                            .getDisplayName(TextStyle.NARROW, locale),
                                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                        fontSize = 8.sp,
                                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                        color = p.muted
                                    )
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            (0 until 7).forEach { day ->
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    weeks.forEach { week ->
                                        val date = week[day]
                                        val count = insights.dailyCounts[date] ?: 0
                                        HeatCell(
                                            count = count,
                                            isToday = date == today,
                                            size = cell,
                                            description = stringResource(
                                                R.string.insights_heatmap_cell,
                                                date.format(formatter),
                                                count
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                activityTotal,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 11.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted
            )
            Text(
                stringResource(R.string.insights_activity_note),
                fontSize = 10.sp,
                color = p.muted
            )
        }
    }
}

@Composable
private fun HeatCell(
    count: Int,
    isToday: Boolean,
    size: Dp,
    description: String
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        val fill = when {
            count <= 0 -> p.chip
            count == 1 -> NeoBrutalistColors.SkyBlue
            count <= 3 -> NeoBrutalistColors.Mint
            else -> NeoBrutalistColors.Yellow
        }
        val shape = RoundedCornerShape(3.dp)
        Box(
            Modifier
                .size(size)
                .clip(shape)
                .background(fill)
                .border(
                    width = if (isToday) 2.dp else 1.dp,
                    color = p.border,
                    shape = shape
                )
                .semantics { contentDescription = description }
        )
        return
    }
    val alpha = when {
        count <= 0 -> 0f
        count == 1 -> 0.24f
        count <= 3 -> 0.45f
        count <= 6 -> 0.68f
        else -> 1f
    }
    val shape = RoundedCornerShape(3.dp)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(if (count <= 0) p.chip else p.primary.copy(alpha = alpha))
            .then(if (isToday) Modifier.border(1.dp, p.primary, shape) else Modifier)
            .semantics { contentDescription = description }
    )
}

@Composable
private fun RecentActivityCard(
    insights: Insights,
    items: List<MediaItem>,
    onOpen: (MediaItem) -> Unit,
    onClear: () -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val byId = remember(items) { items.associateBy { it.id } }
    val recentCount = insights.recent.size

    SerieeSurface(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(
                title = stringResource(R.string.insights_recent_title),
                subtitle = if (recentCount > 0) {
                    pluralStringResource(R.plurals.insights_recent_count, recentCount, recentCount)
                } else null,
                trailing = if (recentCount > 0) {
                    {
                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = onClear,
                                role = Role.Button,
                                backgroundColor = NeoBrutalistColors.CoralRed,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp,
                                cornerRadius = 6.dp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier.heightIn(min = 44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.insights_clear_history),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoBrutalistColors.Black
                                )
                            }
                        } else {
                            Box(
                                Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(SerieeShape.pill)
                                    .background(if (p.isGlass) p.chip.copy(alpha = 0.55f) else p.chip)
                                    .clickable(role = Role.Button, onClick = onClear)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.insights_clear_history),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.danger
                                )
                            }
                        }
                    }
                } else null
            )

            if (insights.recent.isEmpty()) {
                Text(
                    stringResource(R.string.insights_recent_empty),
                    fontSize = 12.sp,
                    color = p.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            } else {
                insights.recent.forEach { event ->
                    val item = byId[event.itemId]
                    val relative = remember(event.watchedAt) { relativeTime(context, event.watchedAt) }
                    val meta = when {
                        event.count > 1 -> pluralStringResource(
                            R.plurals.insights_episode_count,
                            event.count,
                            event.count
                        )
                        event.type == MediaType.Movie -> stringResource(R.string.movie_watched)
                        else -> stringResource(
                            R.string.insights_event_season_episode,
                            event.season,
                            event.episode
                        )
                    }
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .semantics(mergeDescendants = true) {},
                            onClick = if (item != null) ({ onOpen(item) }) else null,
                            role = if (item != null) Role.Button else null,
                            backgroundColor = p.chip,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            cornerRadius = 6.dp,
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Poster(
                                    url = item?.poster ?: "",
                                    modifier = Modifier.size(width = 40.dp, height = 54.dp),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        item?.title ?: event.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = p.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        meta,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.text
                                    )
                                    Text(
                                        relative,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = p.muted
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (p.isGlass) p.chip.copy(alpha = 0.45f) else Color.Transparent)
                                .then(
                                    if (item != null) {
                                        Modifier.clickable(role = Role.Button) { onOpen(item) }
                                    } else Modifier
                                )
                                .padding(8.dp)
                                .semantics(mergeDescendants = true) {},
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Poster(
                                url = item?.poster ?: "",
                                modifier = Modifier.size(width = 40.dp, height = 54.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    item?.title ?: event.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(meta, fontSize = 11.sp, color = p.primary)
                                Text(relative, fontSize = 10.sp, color = p.muted)
                            }
                        }
                    }
                }
            }
        }
    }
}
