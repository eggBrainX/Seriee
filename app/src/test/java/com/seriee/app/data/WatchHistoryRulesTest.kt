package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WatchHistoryRulesTest {
    @Test
    fun append_andTrimRespectRetentionAndLimit() {
        val now = 1_000_000_000_000L
        val old = now - (WatchHistoryRules.RETENTION_DAYS + 2L) * 86_400_000L
        val events = listOf(
            WatchEvent("a", "Old", MediaType.Series, 1, 1, 1, old),
            WatchEvent("b", "Zero", MediaType.Series, 1, 1, 0, now),
        )
        val added = WatchHistoryRules.append(
            events,
            WatchEvent("c", "Fresh", MediaType.Series, 1, 2, 1, now),
            nowMs = now,
        )
        assertEquals(1, added.size)
        assertEquals("c", added[0].itemId)
    }

    @Test
    fun merge_deduplicatesAndSortsDescending() {
        val now = 1_000_000_000_000L
        val e1 = WatchEvent("1", "Show", MediaType.Series, 1, 1, 1, now - 5000)
        val e2 = WatchEvent("1", "Show", MediaType.Series, 1, 2, 1, now - 1000)

        val merged = WatchHistoryRules.merge(listOf(e1), listOf(e1, e2), nowMs = now)
        assertEquals(2, merged.size)
        assertEquals(2, merged[0].episode)
        assertEquals(1, merged[1].episode)
    }
}
