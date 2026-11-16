package com.seriee.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class TagStat(val tag: String, val count: Int)
data class DayActivity(val dayKey: String, val episodes: Int)

data class InsightsSnapshot(
    val totalItems: Int,
    val seriesCount: Int,
    val moviesCount: Int,
    val watchedEpisodes: Int,
    val moviesWatched: Int,
    val completedTitles: Int,
    val completionRate: Float,
    val averageRating: Float?,
    val ratedTitles: Int,
    val statusCounts: Map<WatchStatus, Int>,
    val ratingCounts: IntArray,
    val topTags: List<TagStat>,
    val estimatedMinutes: Int,
    val dailyCounts: List<DayActivity>,
    val recent: List<WatchEvent>,
)

object InsightsCalculator {
    private const val MINUTES_PER_EP = 24
    private const val HEATMAP_DAYS = 84

    fun calculate(
        items: List<MediaItem>,
        history: List<WatchEvent>,
        watchDays: Set<String> = emptySet(),
        today: LocalDate = LocalDate.now(),
    ): InsightsSnapshot {
        val series = items.filter { it.type == MediaType.Series }
        val movies = items.filter { it.type == MediaType.Movie }

        val watchedEps = series
            .filter { it.status != WatchStatus.PlanToWatch && it.status != WatchStatus.ComingSoon }
            .sumOf { it.watchedEpisodes }

        val moviesWatched = movies.count { it.status == WatchStatus.Completed || it.currentEp > 0 }
        val completed = items.count { it.status == WatchStatus.Completed }
        val rate = if (items.isEmpty()) 0f else completed.toFloat() / items.size.toFloat()

        val rated = items.filter { it.rating > 0 }
        val avgRating = if (rated.isEmpty()) null else rated.sumOf { it.rating }.toFloat() / rated.size.toFloat()

        val statusCounts = WatchStatus.entries.associateWith { st -> items.count { it.status == st } }
        val ratingCounts = IntArray(6)
        for (it in items) {
            if (it.rating in 1..5) ratingCounts[it.rating] += 1
        }

        val tagBuckets = LinkedHashMap<String, Pair<String, Int>>()
        for (it in items) {
            val seenItem = HashSet<String>()
            for (raw in it.tags) {
                val trimmed = raw.trim()
                if (trimmed.isEmpty()) continue
                val key = trimmed.lowercase()
                if (!seenItem.add(key)) continue
                val prev = tagBuckets[key]
                tagBuckets[key] = if (prev == null) trimmed to 1 else prev.first to (prev.second + 1)
            }
        }
        val topTags = tagBuckets.values
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
            .take(8)
            .map { TagStat(it.first, it.second) }

        val zone = ZoneId.systemDefault()
        val byDay = HashMap<String, Int>()
        for (ev in history) {
            val dayKey = Instant.ofEpochMilli(ev.watchedAt).atZone(zone).toLocalDate().toString()
            byDay[dayKey] = (byDay[dayKey] ?: 0) + ev.count.coerceAtLeast(0)
        }
        for (day in watchDays) {
            if (!byDay.containsKey(day)) byDay[day] = 1
        }

        val daily = (HEATMAP_DAYS - 1 downTo 0).map { offset ->
            val key = today.minusDays(offset.toLong()).toString()
            DayActivity(key, byDay[key] ?: 0)
        }

        return InsightsSnapshot(
            totalItems = items.size,
            seriesCount = series.size,
            moviesCount = movies.size,
            watchedEpisodes = watchedEps,
            moviesWatched = moviesWatched,
            completedTitles = completed,
            completionRate = rate,
            averageRating = avgRating,
            ratedTitles = rated.size,
            statusCounts = statusCounts,
            ratingCounts = ratingCounts,
            topTags = topTags,
            estimatedMinutes = watchedEps * MINUTES_PER_EP,
            dailyCounts = daily,
            recent = history.sortedByDescending { it.watchedAt }.take(10),
        )
    }
}
