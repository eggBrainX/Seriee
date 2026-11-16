package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MalXmlParserTest {
    private val sampleXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <myanimelist>
            <anime>
                <series_animedb_id>5114</series_animedb_id>
                <series_title>Fullmetal Alchemist: Brotherhood</series_title>
                <series_type>TV</series_type>
                <series_episodes>64</series_episodes>
                <my_watched_episodes>32</my_watched_episodes>
                <my_status>Watching</my_status>
                <my_score>9</my_score>
            </anime>
        </myanimelist>
    """.trimIndent()

    @Test
    fun parseXmlString_parsesAnimeEntry() {
        val result = MalXmlParser.parseXmlString(sampleXml)
        assertEquals(1, result.size)
        assertEquals("5114", result[0].id)
        assertEquals("Fullmetal Alchemist: Brotherhood", result[0].title)
        assertEquals(64, result[0].episodes)
        assertEquals(32, result[0].watched)
        assertEquals("Watching", result[0].status)
        assertEquals(9, result[0].score)
    }

    @Test
    fun parseStream_supportsGzippedXml() {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { it.write(sampleXml.toByteArray(Charsets.UTF_8)) }
        val result = MalXmlParser.parseStream(ByteArrayInputStream(baos.toByteArray()))
        assertEquals(1, result.size)
        assertEquals("Fullmetal Alchemist: Brotherhood", result[0].title)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseXmlString_rejectsInvalidRoot() {
        MalXmlParser.parseXmlString("<notmal><anime/></notmal>")
    }
}
