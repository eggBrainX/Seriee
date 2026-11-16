package com.seriee.app.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.GZIPInputStream

object MalXmlParser {
    fun parseStream(input: InputStream): List<MalRawEntry> {
        val buffered = BufferedInputStream(input)
        buffered.mark(4)
        val header = ByteArray(2)
        val read = buffered.read(header)
        buffered.reset()
        val isGzip = read == 2 && header[0] == 0x1F.toByte() && header[1] == 0x8B.toByte()
        val xmlStream = if (isGzip) GZIPInputStream(buffered) else buffered
        val xmlText = xmlStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parseXmlString(xmlText)
    }

    fun parseXmlString(xml: String): List<MalRawEntry> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var sawRoot = false
        val out = ArrayList<MalRawEntry>()
        var inAnime = false
        var currentTag = ""
        var id = ""
        var title = ""
        var type = "TV"
        var episodes = 0
        var watched = 0
        var status = "Plan to Watch"
        var score = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name ?: ""
                    if (!sawRoot) {
                        if (!tag.equals("myanimelist", ignoreCase = true)) {
                            throw IllegalArgumentException("Invalid MAL XML root: $tag")
                        }
                        sawRoot = true
                    } else if (tag == "anime") {
                        inAnime = true
                        id = ""; title = ""; type = "TV"; episodes = 0; watched = 0; status = "Plan to Watch"; score = 0
                    } else if (inAnime) {
                        currentTag = tag
                    }
                }
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    if (inAnime && currentTag.isNotEmpty()) {
                        val txt = parser.text?.trim() ?: ""
                        when (currentTag) {
                            "series_animedb_id" -> id = txt
                            "series_title" -> title = txt
                            "series_type" -> if (txt.isNotEmpty()) type = txt
                            "series_episodes" -> episodes = txt.toIntOrNull() ?: 0
                            "my_watched_episodes" -> watched = txt.toIntOrNull() ?: 0
                            "my_status" -> if (txt.isNotEmpty()) status = txt
                            "my_score" -> score = txt.toIntOrNull() ?: 0
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name ?: ""
                    if (tag == "anime" && inAnime) {
                        if (title.isNotBlank()) {
                            out.add(MalRawEntry(id, title, type, episodes, watched, status, score))
                        }
                        inAnime = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
        if (!sawRoot) throw IllegalArgumentException("Empty XML")
        return out
    }
}
