package com.seriee.app.data

import com.seriee.app.ui.EpisodeFileParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeasonParsingTest {
    @Test
    fun parseSeasonNumberFromText_detectsCommonFormats() {
        assertEquals(2, MetadataRepository.parseSeasonNumberFromText("Jujutsu Kaisen Season 2"))
        assertEquals(3, MetadataRepository.parseSeasonNumberFromText("Attack on Titan S03"))
        assertEquals(4, MetadataRepository.parseSeasonNumberFromText("My Hero 4th Season"))
        assertEquals(2, MetadataRepository.parseSeasonNumberFromText("Spy x Family Part 2"))
        assertNull(MetadataRepository.parseSeasonNumberFromText("Frieren: Beyond Journey's End"))
    }

    @Test
    fun extractEpisodeNumber_parsesVideoFilenames() {
        assertEquals(12, EpisodeFileParser.extractEpisodeNumber("Arcane.S01E12.1080p.mkv"))
        assertEquals(5, EpisodeFileParser.extractEpisodeNumber("Show 2x05 x265.mp4"))
        assertEquals(7, EpisodeFileParser.extractEpisodeNumber("[Group] Anime - 07 (720p).mkv"))
        assertEquals(3, EpisodeFileParser.extractEpisodeNumber("Episode 03.mp4"))
    }
}
