package com.seriee.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class MalXmlParserTest {

    private val sampleXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <myanimelist>
          <myinfo><user_name>tester</user_name></myinfo>
          <anime>
            <entry>
              <series_animedb_id>21</series_animedb_id>
              <series_title><![CDATA[One Piece]]></series_title>
              <series_type>TV</series_type>
              <series_episodes>1000</series_episodes>
              <my_watched_episodes>42</my_watched_episodes>
              <my_score>8</my_score>
              <my_status>Watching</my_status>
              <series_image>https://img/op.jpg</series_image>
              <my_tags>Action, Adventure</my_tags>
              <my_comments><![CDATA[Great &amp; fun]]></my_comments>
            </entry>
          </anime>
          <anime>
            <entry>
              <series_animedb_id>9</series_animedb_id>
              <series_title>A &amp; B</series_title>
              <series_type>Movie</series_type>
              <series_episodes>0</series_episodes>
              <my_watched_episodes>0</my_watched_episodes>
              <my_score>0</my_score>
              <my_status>On-Hold</my_status>
              <series_image></series_image>
              <my_tags></my_tags>
              <my_comments></my_comments>
            </entry>
          </anime>
        </myanimelist>
    """.trimIndent()

    @Test
    fun `parses entries with CDATA and entities`() {
        val entries = MalXmlParser.parseBytes(sampleXml.toByteArray(Charsets.UTF_8))

        assertEquals(2, entries.size)

        val first = entries[0]
        assertEquals("21", first.id)
        assertEquals("One Piece", first.title)
        assertEquals("TV", first.type)
        assertEquals(1000, first.totalEpisodes)
        assertEquals(42, first.watchedEpisodes)
        assertEquals(8, first.score)
        assertEquals("Watching", first.status)
        assertEquals("https://img/op.jpg", first.image)
        assertEquals(listOf("Action", "Adventure"), first.tags)
        assertEquals("Great & fun", first.comments)

        val second = entries[1]
        assertEquals("A & B", second.title)
        assertEquals("Movie", second.type)
        assertEquals(0, second.totalEpisodes)
        assertEquals("On-Hold", second.status)
    }

    @Test
    fun `reads gzip exports`() {
        val gz = ByteArrayOutputStream().also { out ->
            GZIPOutputStream(out).use { it.write(sampleXml.toByteArray(Charsets.UTF_8)) }
        }.toByteArray()

        assertTrue(MalXmlParser.isGzip(gz))
        assertEquals(2, MalXmlParser.parseBytes(gz).size)
    }

    @Test
    fun `decodes numeric entities`() {
        val xml = """
            <myanimelist><anime><entry>
              <series_animedb_id>1</series_animedb_id>
              <series_title>Pok&#233;mon</series_title>
              <series_type>TV</series_type>
              <series_episodes>10</series_episodes>
              <my_watched_episodes>1</my_watched_episodes>
              <my_score>0</my_score>
              <my_status>Watching</my_status>
            </entry></anime></myanimelist>
        """.trimIndent()
        assertEquals("Pokémon", MalXmlParser.parseBytes(xml.toByteArray()).single().title)
    }

    @Test
    fun `rejects files that are not MAL exports`() {
        val error = runCatching {
            MalXmlParser.parseBytes("just some text".toByteArray())
        }.exceptionOrNull()

        assertTrue(error is ImportException)
        assertEquals(ImportError.FileInvalid, (error as ImportException).error)
    }

    @Test
    fun `rejects blank files`() {
        val error = runCatching {
            MalXmlParser.parseBytes(ByteArray(0))
        }.exceptionOrNull()

        assertTrue(error is ImportException)
    }

    @Test
    fun `empty but valid export returns no entries`() {
        val empty = "<myanimelist><anime></anime></myanimelist>"
        assertTrue(MalXmlParser.parseBytes(empty.toByteArray()).isEmpty())
    }
}
