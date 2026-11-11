package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportMergeTest {

    private fun item(
        id: String,
        title: String = id,
        source: String = "AniList",
        sourceId: String = id,
        currentEp: Int = 0,
        totalEp: Int = 12,
        status: WatchStatus = WatchStatus.PlanToWatch,
        rating: Int = 0,
        tags: List<String> = emptyList(),
        pinned: Boolean = false
    ) = MediaItem(
        id = id,
        title = title,
        type = MediaType.Series,
        status = status,
        poster = "local://poster",
        currentEp = currentEp,
        totalEp = totalEp,
        pinned = pinned,
        tags = tags,
        rating = rating,
        source = source,
        sourceId = sourceId
    )

    @Test
    fun `new titles are inserted`() {
        val imported = item("anilist-1")
        val (items, result) = ImportMapper.merge(listOf(imported), emptyList())

        assertEquals(1, items.size)
        assertEquals(1, result.added)
        assertEquals(0, result.updated)
    }

    @Test
    fun `progress only ever grows`() {
        val local = item("anilist-1", currentEp = 5, totalEp = 12)
        val higher = item("anilist-1", currentEp = 8, totalEp = 24)
        val (grown, _) = ImportMapper.merge(listOf(higher), listOf(local))
        assertEquals(8, grown.single().currentEp)
        assertEquals(24, grown.single().totalEp)

        val lower = item("anilist-1", currentEp = 2, totalEp = 12)
        val (kept, _) = ImportMapper.merge(listOf(lower), listOf(local))
        assertEquals(5, kept.single().currentEp)
        assertEquals(12, kept.single().totalEp)
    }

    @Test
    fun `status only upgrades`() {
        val local = item("anilist-1", status = WatchStatus.PlanToWatch)
        val imported = item("anilist-1", status = WatchStatus.Watching)
        val (upgraded, _) = ImportMapper.merge(listOf(imported), listOf(local))
        assertEquals(WatchStatus.Watching, upgraded.single().status)

        val completed = item("anilist-1", status = WatchStatus.Completed)
        val downgrade = item("anilist-1", status = WatchStatus.Watching)
        val (kept, _) = ImportMapper.merge(listOf(downgrade), listOf(completed))
        assertEquals(WatchStatus.Completed, kept.single().status)
    }

    @Test
    fun `rating fills blank only`() {
        val local = item("anilist-1", rating = 4)
        val imported = item("anilist-1", rating = 5)
        val (kept, _) = ImportMapper.merge(listOf(imported), listOf(local))
        assertEquals(4, kept.single().rating)

        val blank = item("anilist-2", rating = 0)
        val (filled, _) = ImportMapper.merge(listOf(item("anilist-2", rating = 3)), listOf(blank))
        assertEquals(3, filled.single().rating)
    }

    @Test
    fun `tags union without duplicates`() {
        val local = item("anilist-1", tags = listOf("Action", "Drama"))
        val imported = item("anilist-1", tags = listOf("action", "Comedy"))
        val (merged, _) = ImportMapper.merge(listOf(imported), listOf(local))
        assertEquals(listOf("Action", "Drama", "Comedy"), merged.single().tags)
    }

    @Test
    fun `local-only fields survive the merge`() {
        val local = item("anilist-1", pinned = true).copy(streamUrl = "https://stream", poster = "posters/local.jpg")
        val imported = item("anilist-1").copy(streamUrl = "", poster = "https://remote/poster.jpg")
        val (merged, _) = ImportMapper.merge(listOf(imported), listOf(local))
        val row = merged.single()

        assertTrue(row.pinned)
        assertEquals("https://stream", row.streamUrl)
        assertEquals("posters/local.jpg", row.poster)
    }

    @Test
    fun `re-import is idempotent`() {
        val imported = item("anilist-1", currentEp = 4, status = WatchStatus.Watching)
        val (first, _) = ImportMapper.merge(listOf(imported), emptyList())
        val (second, result) = ImportMapper.merge(listOf(imported), first)

        assertEquals(1, second.size)
        assertEquals(0, result.added)
        assertEquals(0, result.updated)
        assertEquals(1, result.skipped)
    }

    @Test
    fun `falls back to normalized title when ids are missing`() {
        val local = item("local-1", title = "Some: Show", source = "", sourceId = "", currentEp = 1)
        val imported = item("anilist-9", title = "Some Show", source = "AniList", sourceId = "9", currentEp = 2)
        val (merged, result) = ImportMapper.merge(listOf(imported), listOf(local))

        assertEquals(1, merged.size)
        assertEquals(0, result.added)
        assertEquals(1, result.updated)
        assertEquals("local-1", merged.single().id)
        assertEquals(2, merged.single().currentEp)
    }

    @Test
    fun `replace keeps only the imported set`() {
        val local = item("local-1", title = "Keep?", sourceId = "1")
        val imported = listOf(item("anilist-2", sourceId = "2"), item("anilist-3", sourceId = "3"))
        val (items, result) = ImportMapper.replace(imported)

        assertEquals(2, items.size)
        assertEquals(2, result.added)
        assertTrue(items.none { it.id == local.id })
    }

    @Test
    fun `preview counts new updated and unchanged`() {
        val local = item("anilist-1", currentEp = 5)
        val imported = listOf(
            item("anilist-1", currentEp = 8),
            item("anilist-2", currentEp = 1)
        )
        val preview = ImportMapper.preview(imported, listOf(local))

        assertEquals(2, preview.total)
        assertEquals(1, preview.newCount)
        assertEquals(1, preview.updatedCount)
        assertEquals(0, preview.unchangedCount)
    }
}
