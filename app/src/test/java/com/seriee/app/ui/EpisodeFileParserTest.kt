package com.seriee.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpisodeFileParserTest {

    @Test
    fun `parses SxxExx filenames`() {
        assertEquals(1 to 5, EpisodeFileParser.parseSeasonEpisode("Show.S01E05.1080p.mkv"))
        assertEquals(4 to 12, EpisodeFileParser.parseSeasonEpisode("Show - s4e12 - title.mp4"))
        assertEquals(1 to 5, EpisodeFileParser.parseSeasonEpisode("Show.S01.E05.mkv"))
    }

    @Test
    fun `returns null when no season and episode pair exists`() {
        assertNull(EpisodeFileParser.parseSeasonEpisode("Show.E05.mkv"))
        assertNull(EpisodeFileParser.parseSeasonEpisode("random-file.mkv"))
    }

    @Test
    fun `parses episode markers`() {
        assertEquals(7, EpisodeFileParser.parseEpisode("Show.EP07.mkv"))
        assertEquals(7, EpisodeFileParser.parseEpisode("Show.Episode 7.mkv"))
    }

    @Test
    fun `parses trailing episode numbers`() {
        assertEquals(12, EpisodeFileParser.parseEpisode("Show - 12.mkv"))
        assertEquals(3, EpisodeFileParser.parseEpisode("Show_03.mp4"))
    }

    @Test
    fun `returns null when no episode number exists`() {
        assertNull(EpisodeFileParser.parseEpisode("Show.mkv"))
        assertNull(EpisodeFileParser.parseEpisode("Show.Special.mkv"))
    }
}
