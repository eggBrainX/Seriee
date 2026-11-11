package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportMapperTest {

    @Test
    fun `maps an AniList series`() {
        val item = ImportMapper.fromAniList(
            listOf(
                AniListEntry(
                    id = 21L,
                    title = "One Piece",
                    format = "TV",
                    totalEpisodes = 1000,
                    progress = 42,
                    score10 = 8.0,
                    status = "CURRENT",
                    poster = "https://img/op.jpg",
                    genres = listOf("Action", "Comedy"),
                    description = "<p>Hi<br>there</p>",
                    notes = ""
                )
            )
        ).single()

        assertEquals("anilist-21", item.id)
        assertEquals("One Piece", item.title)
        assertEquals(MediaType.Series, item.type)
        assertEquals(WatchStatus.Watching, item.status)
        assertEquals(42, item.currentEp)
        assertEquals(1000, item.totalEp)
        assertEquals(4, item.rating)
        assertEquals(listOf("Action", "Comedy"), item.tags)
        assertEquals("Hi\nthere", item.description)
        assertEquals("AniList", item.source)
        assertEquals("21", item.sourceId)
    }

    @Test
    fun `maps an AniList movie with one episode`() {
        val item = ImportMapper.fromAniList(
            listOf(
                AniListEntry(
                    id = 100L,
                    title = "A Silent Voice",
                    format = "MOVIE",
                    totalEpisodes = 0,
                    progress = 1,
                    score10 = 9.0,
                    status = "COMPLETED",
                    poster = "",
                    genres = emptyList(),
                    description = "",
                    notes = ""
                )
            )
        ).single()

        assertEquals(MediaType.Movie, item.type)
        assertEquals(1, item.totalEp)
        assertEquals(1, item.currentEp)
        assertEquals(WatchStatus.Completed, item.status)
        assertEquals(5, item.rating)
    }

    @Test
    fun `planning with zero episodes falls back to one`() {
        val item = ImportMapper.fromAniList(
            listOf(
                AniListEntry(
                    id = 3L,
                    title = "Unknown Show",
                    format = "TV",
                    totalEpisodes = 0,
                    progress = 0,
                    score10 = 0.0,
                    status = "PLANNING",
                    poster = "",
                    genres = emptyList(),
                    description = "",
                    notes = ""
                )
            )
        ).single()

        assertEquals(WatchStatus.PlanToWatch, item.status)
        assertEquals(0, item.currentEp)
        assertEquals(1, item.totalEp)
        assertEquals(0, item.rating)
    }

    @Test
    fun `maps every AniList status`() {
        val pairs = listOf(
            "CURRENT" to WatchStatus.Watching,
            "REPEATING" to WatchStatus.Watching,
            "COMPLETED" to WatchStatus.Completed,
            "PLANNING" to WatchStatus.PlanToWatch,
            "PAUSED" to WatchStatus.OnHold,
            "DROPPED" to WatchStatus.Dropped
        )
        pairs.forEach { (raw, expected) ->
            assertEquals(expected, ImportMapper.anilistStatus(raw))
        }
    }

    @Test
    fun `maps every MyAnimeList status`() {
        val pairs = listOf(
            "Watching" to WatchStatus.Watching,
            "Completed" to WatchStatus.Completed,
            "Plan to Watch" to WatchStatus.PlanToWatch,
            "On-Hold" to WatchStatus.OnHold,
            "Dropped" to WatchStatus.Dropped
        )
        pairs.forEach { (raw, expected) ->
            assertEquals(expected, ImportMapper.malStatus(raw))
        }
    }

    @Test
    fun `maps a MyAnimeList series`() {
        val item = ImportMapper.fromMal(
            listOf(
                MalEntry(
                    id = "5",
                    title = "Cowboy Bebop",
                    type = "TV",
                    totalEpisodes = 26,
                    watchedEpisodes = 13,
                    score = 9,
                    status = "On-Hold",
                    image = "https://img/cb.jpg",
                    tags = listOf("Space", "Action"),
                    comments = "Halfway through"
                )
            )
        ).single()

        assertEquals("mal-5", item.id)
        assertEquals(WatchStatus.OnHold, item.status)
        assertEquals(13, item.currentEp)
        assertEquals(26, item.totalEp)
        assertEquals(5, item.rating)
        assertEquals(MediaType.Series, item.type)
        assertEquals(listOf("Space", "Action"), item.tags)
        assertEquals("MyAnimeList", item.source)
    }

    @Test
    fun `maps a MyAnimeList movie`() {
        val item = ImportMapper.fromMal(
            listOf(
                MalEntry(
                    id = "9",
                    title = "Your Name",
                    type = "Movie",
                    totalEpisodes = 0,
                    watchedEpisodes = 0,
                    score = 0,
                    status = "Plan to Watch",
                    image = "",
                    tags = emptyList(),
                    comments = ""
                )
            )
        ).single()

        assertEquals(MediaType.Movie, item.type)
        assertEquals(1, item.totalEp)
        assertEquals(0, item.currentEp)
        assertEquals(0, item.rating)
    }

    @Test
    fun `parse season from imported title`() {
        val item = ImportMapper.fromAniList(
            listOf(
                AniListEntry(
                    id = 42L,
                    title = "Some Show Season 3",
                    format = "TV",
                    totalEpisodes = 12,
                    progress = 1,
                    score10 = 0.0,
                    status = "WATCHING",
                    poster = "",
                    genres = emptyList(),
                    description = "",
                    notes = ""
                )
            )
        ).single()
        assertEquals(3, item.season)
    }

    @Test
    fun `strips HTML and decodes entities`() {
        assertEquals(
            "A & B\nnext line",
            ImportMapper.stripHtml("<b>A &amp; B</b><br/>next line")
        )
        assertEquals("", ImportMapper.stripHtml(null))
        assertTrue(ImportMapper.normalize("Some: Show!!").startsWith("some show"))
    }
}
