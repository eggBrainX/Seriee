package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.InsightsCalculator
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.WatchEvent
import com.seriee.app.data.WatchStatus
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightsScreen(
    items: List<MediaItem>,
    history: List<WatchEvent>,
    watchDays: Set<String>,
    listState: LazyListState,
    onClearHistory: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val snap = remember(items, history, watchDays) {
        InsightsCalculator.calculate(items, history, watchDays)
    }
    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.insights_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.insights_subtitle),
                    fontSize = 13.sp,
                    color = p.muted,
                )
            }
        }

        item {
            val hoursStr = String.format(Locale.getDefault(), "%.1f", snap.estimatedMinutes / 60f)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatTile(
                    label = stringResource(R.string.insights_stat_episodes),
                    value = "${snap.watchedEpisodes}",
                    sub = pluralStringResource(R.plurals.insights_episode_count, snap.watchedEpisodes, snap.watchedEpisodes),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.insights_stat_hours),
                    value = stringResource(R.string.insights_hours_value, hoursStr),
                    sub = stringResource(R.string.insights_stat_completion) + " ${(snap.completionRate * 100).roundToInt()}%",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            val avgText = snap.averageRating?.let { String.format(Locale.getDefault(), "%.1f ★", it) } ?: "—"
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatTile(
                    label = stringResource(R.string.insights_stat_series),
                    value = "${snap.seriesCount}",
                    sub = pluralStringResource(R.plurals.insights_completed_titles, snap.totalItems, snap.completedTitles, snap.totalItems),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.insights_stat_movies),
                    value = "${snap.moviesCount}",
                    sub = "${snap.moviesWatched} ${stringResource(R.string.movie_watched).lowercase()}",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.insights_stat_avg_rating),
                    value = avgText,
                    sub = pluralStringResource(R.plurals.insights_title_count, snap.ratedTitles, snap.ratedTitles),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.insights_watch_time_note),
                fontSize = 11.sp,
                color = p.muted,
            )
        }

        if (snap.totalItems > 0) {
            item {
                SerieeCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.insights_status_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(p.chip)
                                .then(if (p.isNeoBrutalist) Modifier.border(1.5.dp, p.border, RoundedCornerShape(7.dp)) else Modifier)
                        ) {
                            WatchStatus.entries.forEach { st ->
                                val c = snap.statusCounts[st] ?: 0
                                if (c > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(c.toFloat())
                                            .fillMaxHeight()
                                            .background(statusColor(st, p))
                                    )
                                }
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            WatchStatus.entries.forEach { st ->
                                val c = snap.statusCounts[st] ?: 0
                                if (c > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(statusColor(st, p))
                                        )
                                        Text(
                                            text = "${stringResource(st.labelRes)} ($c)",
                                            fontSize = 12.sp,
                                            color = p.text,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (snap.ratedTitles > 0) {
            item {
                val maxRatingBucket = (1..5).maxOf { snap.ratingCounts[it] }.coerceAtLeast(1)
                SerieeCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.insights_ratings_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                        )
                        (5 downTo 1).forEach { stars ->
                            val count = snap.ratingCounts[stars]
                            val frac = count.toFloat() / maxRatingBucket.toFloat()
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(34.dp)) {
                                    Text("$stars", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.text)
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(13.dp))
                                }
                                GradientProgressBar(
                                    fraction = frac,
                                    color = p.accent,
                                    modifier = Modifier.weight(1f),
                                    height = 10.dp,
                                )
                                Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.muted, modifier = Modifier.width(24.dp))
                            }
                        }
                    }
                }
            }
        }

        if (snap.topTags.isNotEmpty()) {
            item {
                SerieeCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.insights_tags_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            snap.topTags.forEach { tag ->
                                val shape = RoundedCornerShape(8.dp)
                                Box(
                                    modifier = Modifier
                                        .clip(shape)
                                        .background(p.primaryContainer)
                                        .border(1.dp, p.border, shape)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.insights_tag_chip, tag.tag, tag.count),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.text,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SerieeCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.insights_activity_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.insights_activity_subtitle),
                            fontSize = 12.sp,
                            color = p.muted,
                        )
                    }
                    val weeks = snap.dailyCounts.chunked(7)
                    val maxDay = snap.dailyCounts.maxOfOrNull { it.episodes }?.coerceAtLeast(1) ?: 1
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        weeks.forEach { week ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                week.forEach { day ->
                                    val intensity = if (day.episodes <= 0) 0f else (0.28f + 0.72f * (day.episodes.toFloat() / maxDay.toFloat())).coerceIn(0.28f, 1f)
                                    val cellDesc = context.getString(R.string.insights_heatmap_cell, day.dayKey, day.episodes)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (intensity == 0f) p.chip else p.accent.copy(alpha = intensity))
                                            .then(if (p.isNeoBrutalist) Modifier.border(1.dp, p.border.copy(alpha = 0.45f), RoundedCornerShape(3.dp)) else Modifier)
                                            .semantics { contentDescription = cellDesc }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.insights_activity_note),
                        fontSize = 11.sp,
                        color = p.muted,
                    )
                }
            }
        }

        item {
            SerieeCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.insights_recent_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = p.text,
                            modifier = Modifier.weight(1f),
                        )
                        if (snap.recent.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showClearConfirm = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, p.danger),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = p.danger),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.insights_clear_history), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (snap.recent.isEmpty()) {
                        Text(
                            text = stringResource(R.string.insights_recent_empty),
                            fontSize = 13.sp,
                            color = p.muted,
                        )
                    } else {
                        snap.recent.forEach { ev ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(p.cardAlt)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = ev.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = p.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    val detail = if (ev.type == MediaType.Movie) stringResource(R.string.movie_watched)
                                    else stringResource(R.string.insights_event_season_episode, ev.season, ev.episode)
                                    Text(
                                        text = "$detail · ${relativeTime(context, ev.watchedAt)}",
                                        fontSize = 11.sp,
                                        color = p.muted,
                                    )
                                }
                                Text(
                                    text = "+${ev.count}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = p.accent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        NeoBrutalistConfirmDialog(
            title = stringResource(R.string.insights_clear_history_title),
            message = stringResource(R.string.insights_clear_history_message),
            confirmText = stringResource(R.string.insights_clear_history_confirm),
            onDismiss = { showClearConfirm = false },
            onConfirm = onClearHistory,
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    SerieeCard(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(sub, fontSize = 11.sp, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
