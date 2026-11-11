package com.seriee.app.data

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * Streaming reader for MyAnimeList XML exports.
 *
 * MAL downloads are gzip files (`myanimelist_<user>.xml.gz`), so the first two
 * bytes are sniffed and the stream is wrapped in [GZIPInputStream]. The parsed
 * data never leaves the device.
 *
 * A small tag scanner is used instead of `XmlPullParser` so this stays a pure
 * JVM class that unit tests can exercise without Robolectric.
 */
object MalXmlParser {

    fun parse(input: InputStream): List<MalEntry> = parseBytes(input.readBytes())

    fun parseBytes(bytes: ByteArray): List<MalEntry> {
        val decoded = try {
            if (isGzip(bytes)) {
                GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes() }
            } else {
                bytes
            }
        } catch (e: Exception) {
            throw ImportException(ImportError.FileInvalid, e)
        }

        val text = decoded.toString(Charsets.UTF_8)
            .removePrefix("\uFEFF")
            .trim()

        if (text.isBlank()) throw ImportException(ImportError.FileInvalid)
        if (!text.contains("<myanimelist", ignoreCase = true) &&
            !text.contains("<anime", ignoreCase = true)
        ) {
            throw ImportException(ImportError.FileInvalid)
        }
        if (text.contains("<anime", ignoreCase = true) &&
            !text.contains("</anime>", ignoreCase = true) &&
            !text.contains("</entry>", ignoreCase = true)
        ) {
            throw ImportException(ImportError.FileInvalid)
        }

        val animeRegex = Regex("<anime(?:\\s[^>]*)?>(.*?)</anime>", RegexOption.DOT_MATCHES_ALL)
        val entryRegex = Regex("<entry>(.*?)</entry>", RegexOption.DOT_MATCHES_ALL)
        val entries = ArrayList<MalEntry>()

        animeRegex.findAll(text).forEach { anime ->
            entryRegex.findAll(anime.groupValues[1]).forEach { match ->
                val block = match.groupValues[1]
                val title = tag(block, "series_title")
                if (title.isBlank()) return@forEach
                entries.add(
                    MalEntry(
                        id = tag(block, "series_animedb_id"),
                        title = title,
                        type = tag(block, "series_type"),
                        totalEpisodes = tag(block, "series_episodes").toIntOrNull() ?: 0,
                        watchedEpisodes = tag(block, "my_watched_episodes").toIntOrNull() ?: 0,
                        score = tag(block, "my_score").toIntOrNull() ?: 0,
                        status = tag(block, "my_status"),
                        image = tag(block, "series_image"),
                        tags = tag(block, "my_tags")
                            .split(',')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() },
                        comments = tag(block, "my_comments")
                    )
                )
            }
        }

        return entries
    }

    fun isGzip(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()

    private fun tag(block: String, name: String): String {
        val match = Regex("<$name(?:\\s[^>]*)?>(.*?)</$name>", RegexOption.DOT_MATCHES_ALL)
            .find(block) ?: return ""
        var value = match.groupValues[1].trim()
        if (value.startsWith("<![CDATA[")) {
            value = value.removePrefix("<![CDATA[")
            val end = value.indexOf("]]>")
            if (end >= 0) value = value.substring(0, end)
        }
        return decodeEntities(value).trim()
    }

    private fun decodeEntities(value: String): String {
        var out = value
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
        out = Regex("&#x([0-9a-fA-F]+);").replace(out) { match ->
            match.groupValues[1].toIntOrNull(16)?.let { String(Character.toChars(it)) } ?: match.value
        }
        out = Regex("&#(\\d+);").replace(out) { match ->
            match.groupValues[1].toIntOrNull()?.let { String(Character.toChars(it)) } ?: match.value
        }
        return out
    }
}
