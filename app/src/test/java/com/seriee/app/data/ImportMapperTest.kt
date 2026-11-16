package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ImportMapperTest {
    @Test
    fun fromAniList_mapsMovieAndSeriesAccurately() {
        val rows = listOf(
            AniListListRow(
                mediaId = 101,
                title = "Arcane",
                format = "TV",
                episodes = 18,
                status = "CURRENT",
                progress = 9,
                score100 = 90,
                poster = "https://example.com/arcane.jpg",
                genres = listOf("Action", "Sci-Fi"),
                description = "Two sisters.",
            ),
            AniListListRow(
                mediaId = 202,
                title = "Your Name",
                format = "MOVIE",
                episodes = 1,
                status = "COMPLETED",
                progress = 1,
                score100 = 80,
                poster = "",
                genres = listOf("Romance"),
                description = "",
            ),
        )

        val entries = ImportMapper.fromAniList(rows)
        assertEquals(2, entries.size)
        assertEquals(MediaType.Series, entries[0].type)
        assertEquals(WatchStatus.Watching, entries[0].status)
        assertEquals(9, entries[0].currentEp)
        assertEquals(18, entries[0].totalEp)
        assertEquals(5, entries[0].rating)

        assertEquals(MediaType.Movie, entries[1].type)
        assertEquals(WatchStatus.Completed, entries[1].status)
        assertEquals(1, entries[1].currentEp)
        assertEquals(4, entries[1].rating)
    }

    @Test
    fun merge_neverLowersExistingProgress() {
        val existing = listOf(
            MediaItem(
                id = "anilist-101",
                title = "Arcane",
                type = MediaType.Series,
                status = WatchStatus.Completed,
                currentEp = 18,
                totalEp = 18,
                seasons = listOf(Season(1, 18, 18)),
                rating = 5,
                source = "ANILIST",
                sourceId = "101",
            )
        )
        val incoming = listOf(
            ImportedEntry(
                title = "Arcane",
                type = MediaType.Series,
                status = WatchStatus.Watching,
                currentEp = 4,
                totalEp = 18,
                rating = 3,
                poster = "https://example.com/p.jpg",
                tags = listOf("Drama"),
                description = "Added desc",
                source = ImportSource.ANILIST,
                sourceId = "101",
            )
        )

        val (merged, summary) = ImportMapper.merge(existing, incoming, now = 1000L)
        assertEquals(1, merged.size)
        assertEquals(WatchStatus.Completed, merged[0].status)
        assertEquals(18, merged[0].watchedEpisodes)
        assertEquals(5, merged[0].rating)
        assertEquals(1, summary.updated) // poster, tags, description filled in
    }

    @Test
    fun preview_andReplaceWorkAsExpected() {
        val entries = ImportMapper.fromMal(
            listOf(
                MalRawEntry("1", "Cowboy Bebop", "TV", 26, 26, "Completed", 10),
                MalRawEntry("2", "Spirited Away", "Movie", 1, 1, "Completed", 9),
            )
        )
        val preview = ImportMapper.preview(emptyList(), entries)
        assertEquals(2, preview.total)
        assertEquals(2, preview.newCount)
        assertEquals(1, preview.seriesCount)
        assertEquals(1, preview.movieCount)

        val (replaced, summary) = ImportMapper.replace(entries, now = 500L)
        assertEquals(2, replaced.size)
        assertEquals(2, summary.added)
        assertEquals("mal-1", replaced[0].id)
    }
}
