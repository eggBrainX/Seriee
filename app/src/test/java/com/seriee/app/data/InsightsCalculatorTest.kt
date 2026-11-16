package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class InsightsCalculatorTest {
    @Test
    fun calculate_computesStatsTagsAndHeatmap() {
        val today = LocalDate.of(2026, 5, 10)
        val tsToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 3600_000L

        val items = listOf(
            MediaItem(
                id = "1",
                title = "Show A",
                type = MediaType.Series,
                status = WatchStatus.Completed,
                seasons = listOf(Season(1, 10, 10)),
                rating = 5,
                tags = listOf("Sci-Fi", "Action"),
            ),
            MediaItem(
                id = "2",
                title = "Show B",
                type = MediaType.Series,
                status = WatchStatus.Watching,
                seasons = listOf(Season(1, 5, 12)),
                rating = 3,
                tags = listOf("sci-fi"),
            ),
            MediaItem(
                id = "3",
                title = "Future Show",
                type = MediaType.Series,
                status = WatchStatus.PlanToWatch,
                seasons = listOf(Season(1, 4, 12)),
                rating = 0,
                tags = listOf("Drama"),
            ),
            MediaItem(
                id = "4",
                title = "Movie C",
                type = MediaType.Movie,
                status = WatchStatus.Completed,
                currentEp = 1,
                totalEp = 1,
                rating = 4,
            ),
        )
        val history = listOf(
            WatchEvent("1", "Show A", MediaType.Series, 1, 10, 2, tsToday),
        )

        val snap = InsightsCalculator.calculate(items, history, today = today)
        assertEquals(4, snap.totalItems)
        assertEquals(3, snap.seriesCount)
        assertEquals(1, snap.moviesCount)
        assertEquals(15, snap.watchedEpisodes) // 10 + 5 (PlanToWatch excluded)
        assertEquals(15 * 24, snap.estimatedMinutes)
        assertEquals(2, snap.completedTitles)
        assertEquals(0.5f, snap.completionRate, 0.001f)
        assertEquals(4.0f, snap.averageRating ?: 0f, 0.001f)
        assertEquals("Sci-Fi", snap.topTags.first().tag)
        assertEquals(2, snap.topTags.first().count)
        assertEquals(84, snap.dailyCounts.size)
        assertEquals(2, snap.dailyCounts.last().episodes)
    }
}
