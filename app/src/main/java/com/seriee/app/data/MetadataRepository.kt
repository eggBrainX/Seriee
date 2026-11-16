package com.seriee.app.data

import com.seriee.app.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

data class MediaMetadata(
    val title: String,
    val type: MediaType,
    val posterUrl: String,
    val totalEp: Int,
    val seasonCount: Int?,
    val seasonNumber: Int?,
    val rating5: Int,
    val description: String,
    val tags: List<String>,
    val streamUrl: String,
    val releaseDate: Long?,
    val source: String,
    val sourceId: String,
)

object MetadataRepository {
    private val seasonRegexes = listOf(
        Regex("""\bS(\d{1,2})\b""", RegexOption.IGNORE_CASE),
        Regex("""\bSeason\s+(\d{1,2})\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(\d{1,2})(?:st|nd|rd|th)\s+Season\b""", RegexOption.IGNORE_CASE),
        Regex("""\bPart\s+(\d{1,2})\b""", RegexOption.IGNORE_CASE),
    )

    fun parseSeasonNumberFromText(title: String): Int? {
        for (rx in seasonRegexes) {
            val match = rx.find(title) ?: continue
            val n = match.groupValues.getOrNull(1)?.toIntOrNull() ?: continue
            if (n in 1..30) return n
        }
        return null
    }

    suspend fun searchAll(
        query: String,
        tmdbKey: String,
        omdbKey: String,
    ): List<MediaMetadata> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext emptyList()
        val out = ArrayList<MediaMetadata>()
        runCatching { out.addAll(searchAniList(q)) }.onFailure { AppLogger.e("Metadata", "AniList search failed", it) }
        runCatching { out.addAll(searchTvMaze(q)) }.onFailure { AppLogger.e("Metadata", "TVmaze search failed", it) }
        if (tmdbKey.isNotBlank()) runCatching { out.addAll(searchTmdb(q, tmdbKey)) }.onFailure { AppLogger.e("Metadata", "TMDB search failed", it) }
        if (omdbKey.isNotBlank()) runCatching { out.addAll(searchOmdb(q, omdbKey)) }.onFailure { AppLogger.e("Metadata", "OMDb search failed", it) }
        out
    }

    private fun stripHtml(s: String): String =
        s.replace(Regex("<[^>]*>"), " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun searchAniList(query: String): List<MediaMetadata> {
        val gql = """
            query (${'$'}search: String) {
              Page(perPage: 5) {
                media(search: ${'$'}search, type: ANIME, sort: POPULARITY_DESC) {
                  id format episodes averageScore description(asHtml: false) genres
                  title { romaji english native }
                  coverImage { extraLarge large }
                  nextAiringEpisode { airingAt }
                }
              }
            }
        """.trimIndent()
        val payload = JSONObject().apply {
            put("query", gql)
            put("variables", JSONObject().put("search", query))
        }.toString()
        val conn = (URL("https://graphql.anilist.co").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
        }
        conn.outputStream.use { it.write(payload.toByteArray()) }
        if (conn.responseCode !in 200..299) return emptyList()
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val mediaArr = JSONObject(body)
            .optJSONObject("data")
            ?.optJSONObject("Page")
            ?.optJSONArray("media") ?: return emptyList()
        val out = ArrayList<MediaMetadata>()
        for (i in 0 until mediaArr.length()) {
            val m = mediaArr.optJSONObject(i) ?: continue
            val tObj = m.optJSONObject("title")
            val title = listOf("english", "romaji", "native")
                .firstNotNullOfOrNull { k -> tObj?.optString(k)?.takeIf { it.isNotBlank() && it != "null" } }
                ?: query
            val format = m.optString("format", "")
            val type = if (format == "MOVIE") MediaType.Movie else MediaType.Series
            val cover = m.optJSONObject("coverImage")
            val poster = cover?.optString("extraLarge")?.takeIf { it.isNotBlank() && it != "null" }
                ?: cover?.optString("large")?.takeIf { it.isNotBlank() && it != "null" }
                ?: ""
            val eps = m.optInt("episodes", 1).coerceAtLeast(1)
            val score = m.optInt("averageScore", 0)
            val rating5 = if (score > 0) (score / 20f).roundToInt().coerceIn(1, 5) else 0
            val gArr = m.optJSONArray("genres") ?: JSONArray()
            val tags = (0 until minOf(4, gArr.length())).mapNotNull { gArr.optString(it).takeIf { s -> s.isNotBlank() } }
            val nextAir = m.optJSONObject("nextAiringEpisode")?.optLong("airingAt", 0L) ?: 0L
            out.add(
                MediaMetadata(
                    title = title,
                    type = type,
                    posterUrl = poster,
                    totalEp = if (type == MediaType.Movie) 1 else eps,
                    seasonCount = if (type == MediaType.Series) 1 else null,
                    seasonNumber = if (type == MediaType.Series) parseSeasonNumberFromText(title) else null,
                    rating5 = rating5,
                    description = stripHtml(m.optString("description", "")),
                    tags = tags,
                    streamUrl = "https://anilist.co/anime/${m.optInt("id")}",
                    releaseDate = if (nextAir > 0L) nextAir * 1000L else null,
                    source = "anilist",
                    sourceId = m.optInt("id").toString(),
                )
            )
        }
        return out
    }

    private fun getUrl(urlStr: String): String? {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        if (conn.responseCode !in 200..299) return null
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun searchTvMaze(query: String): List<MediaMetadata> {
        val enc = URLEncoder.encode(query, "UTF-8")
        val body = getUrl("https://api.tvmaze.com/search/shows?q=$enc") ?: return emptyList()
        val arr = JSONArray(body)
        val out = ArrayList<MediaMetadata>()
        val now = System.currentTimeMillis()
        for (i in 0 until minOf(4, arr.length())) {
            val show = arr.optJSONObject(i)?.optJSONObject("show") ?: continue
            val id = show.optInt("id")
            val title = show.optString("name", query)
            val img = show.optJSONObject("image")
            val poster = img?.optString("original")?.takeIf { it.isNotBlank() && it != "null" }
                ?: img?.optString("medium")?.takeIf { it.isNotBlank() && it != "null" }
                ?: ""
            val avg = show.optJSONObject("rating")?.optDouble("average", 0.0) ?: 0.0
            val rating5 = if (!avg.isNaN() && avg > 0.0) (avg / 2.0).roundToInt().coerceIn(1, 5) else 0
            val gArr = show.optJSONArray("genres") ?: JSONArray()
            val tags = (0 until minOf(4, gArr.length())).mapNotNull { gArr.optString(it).takeIf { s -> s.isNotBlank() } }

            var totalEp = 12
            var seasonsCount = 1
            var nextRelease: Long? = null
            runCatching {
                val epBody = getUrl("https://api.tvmaze.com/shows/$id/episodes")
                if (epBody != null) {
                    val eps = JSONArray(epBody)
                    if (eps.length() > 0) {
                        totalEp = eps.length()
                        val sSet = HashSet<Int>()
                        val upcoming = ArrayList<Long>()
                        for (j in 0 until eps.length()) {
                            val ep = eps.optJSONObject(j) ?: continue
                            sSet.add(ep.optInt("season", 1))
                            val air = ep.optString("airstamp", "")
                            if (air.isNotBlank()) {
                                runCatching { Instant.parse(air).toEpochMilli() }.getOrNull()?.let { ms ->
                                    if (ms > now) upcoming.add(ms)
                                }
                            }
                        }
                        seasonsCount = sSet.size.coerceAtLeast(1)
                        nextRelease = upcoming.minOrNull()
                    }
                }
            }

            out.add(
                MediaMetadata(
                    title = title,
                    type = MediaType.Series,
                    posterUrl = poster,
                    totalEp = totalEp,
                    seasonCount = seasonsCount,
                    seasonNumber = parseSeasonNumberFromText(title),
                    rating5 = rating5,
                    description = stripHtml(show.optString("summary", "")),
                    tags = tags,
                    streamUrl = show.optString("officialSite", "").ifBlank { show.optString("url", "") },
                    releaseDate = nextRelease,
                    source = "tvmaze",
                    sourceId = id.toString(),
                )
            )
        }
        return out
    }

    private fun parseDateMs(iso: String): Long? {
        if (iso.isBlank() || iso == "null") return null
        return runCatching {
            LocalDate.parse(iso.take(10)).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun searchTmdb(query: String, apiKey: String): List<MediaMetadata> {
        val encQ = URLEncoder.encode(query, "UTF-8")
        val encK = URLEncoder.encode(apiKey, "UTF-8")
        val body = getUrl("https://api.themoviedb.org/3/search/multi?api_key=$encK&query=$encQ&include_adult=false")
            ?: return emptyList()
        val arr = JSONObject(body).optJSONArray("results") ?: return emptyList()
        val out = ArrayList<MediaMetadata>()
        val now = System.currentTimeMillis()
        for (i in 0 until minOf(4, arr.length())) {
            val item = arr.optJSONObject(i) ?: continue
            val mType = item.optString("media_type", "")
            if (mType != "movie" && mType != "tv") continue
            val isMovie = mType == "movie"
            val id = item.optInt("id")
            val title = item.optString("title").ifBlank { item.optString("name", query) }
            val pPath = item.optString("poster_path", "")
            val poster = if (pPath.isNotBlank() && pPath != "null") "https://image.tmdb.org/t/p/w500$pPath" else ""
            val vote = item.optDouble("vote_average", 0.0)
            val rating5 = if (!vote.isNaN() && vote > 0.0) (vote / 2.0).roundToInt().coerceIn(1, 5) else 0
            var totalEp = 1
            var seasonCount: Int? = null
            var tags: List<String> = emptyList()
            var streamUrl = "https://www.themoviedb.org/$mType/$id"
            var release = parseDateMs(item.optString("release_date").ifBlank { item.optString("first_air_date") })

            runCatching {
                val detBody = getUrl("https://api.themoviedb.org/3/$mType/$id?api_key=$encK&append_to_response=watch/providers")
                if (detBody != null) {
                    val det = JSONObject(detBody)
                    val gArr = det.optJSONArray("genres")
                    if (gArr != null) {
                        tags = (0 until minOf(4, gArr.length())).mapNotNull {
                            gArr.optJSONObject(it)?.optString("name")?.takeIf { n -> n.isNotBlank() }
                        }
                    }
                    if (!isMovie) {
                        totalEp = det.optInt("number_of_episodes", 1).coerceAtLeast(1)
                        seasonCount = det.optInt("number_of_seasons", 1).coerceAtLeast(1)
                        det.optJSONObject("next_episode_to_air")?.optString("air_date")?.let { air ->
                            parseDateMs(air)?.let { release = it }
                        }
                    }
                    val prov = det.optJSONObject("watch/providers")?.optJSONObject("results")
                    val link = prov?.optJSONObject("ID")?.optString("link")?.takeIf { it.isNotBlank() }
                        ?: prov?.optJSONObject("US")?.optString("link")?.takeIf { it.isNotBlank() }
                    if (link != null) streamUrl = link
                }
            }

            out.add(
                MediaMetadata(
                    title = title,
                    type = if (isMovie) MediaType.Movie else MediaType.Series,
                    posterUrl = poster,
                    totalEp = totalEp,
                    seasonCount = seasonCount,
                    seasonNumber = if (isMovie) null else parseSeasonNumberFromText(title),
                    rating5 = rating5,
                    description = item.optString("overview", "").trim(),
                    tags = tags,
                    streamUrl = streamUrl,
                    releaseDate = release?.takeIf { it > now },
                    source = "tmdb",
                    sourceId = id.toString(),
                )
            )
        }
        return out
    }

    private fun searchOmdb(query: String, apiKey: String): List<MediaMetadata> {
        val encQ = URLEncoder.encode(query, "UTF-8")
        val encK = URLEncoder.encode(apiKey, "UTF-8")
        val body = getUrl("https://www.omdbapi.com/?apikey=$encK&s=$encQ") ?: return emptyList()
        val obj = JSONObject(body)
        if (obj.optString("Response") != "True") return emptyList()
        val arr = obj.optJSONArray("Search") ?: return emptyList()
        val out = ArrayList<MediaMetadata>()
        for (i in 0 until minOf(3, arr.length())) {
            val entry = arr.optJSONObject(i) ?: continue
            val imdbId = entry.optString("imdbID", "")
            if (imdbId.isBlank()) continue
            val detBody = getUrl("https://www.omdbapi.com/?apikey=$encK&i=$imdbId&plot=short") ?: continue
            val d = JSONObject(detBody)
            if (d.optString("Response") != "True") continue
            val isMovie = d.optString("Type") == "movie"
            val type = if (isMovie) MediaType.Movie else MediaType.Series
            val title = d.optString("Title", entry.optString("Title", query))
            val imdbRating = d.optString("imdbRating", "").toDoubleOrNull() ?: 0.0
            val rating5 = if (imdbRating > 0.0) (imdbRating / 2.0).roundToInt().coerceIn(1, 5) else 0
            val tags = d.optString("Genre", "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != "N/A" }
                .take(4)
            val poster = d.optString("Poster", "").takeIf { it != "N/A" } ?: ""
            val seasons = if (isMovie) null else d.optString("totalSeasons", "1").toIntOrNull()?.coerceAtLeast(1) ?: 1
            out.add(
                MediaMetadata(
                    title = title,
                    type = type,
                    posterUrl = poster,
                    totalEp = if (isMovie) 1 else (seasons ?: 1) * 10,
                    seasonCount = seasons,
                    seasonNumber = if (isMovie) null else parseSeasonNumberFromText(title),
                    rating5 = rating5,
                    description = d.optString("Plot", "").takeIf { it != "N/A" } ?: "",
                    tags = tags,
                    streamUrl = "https://www.imdb.com/title/$imdbId/",
                    releaseDate = null,
                    source = "omdb",
                    sourceId = imdbId,
                )
            )
        }
        return out
    }
}
