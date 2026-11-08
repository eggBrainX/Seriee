package com.seriee.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class StreamCandidate(val name: String, val url: String)

private val ROMAN_SEASONS = mapOf(
    "i" to 1, "ii" to 2, "iii" to 3, "iv" to 4, "v" to 5,
    "vi" to 6, "vii" to 7, "viii" to 8, "ix" to 9, "x" to 10
)

/**
 * Extracts the season number a source entry *is* from its title / query text.
 * AniList splits later seasons into separate entries ("… - Season 4"); TMDB
 * titles usually don't, so the search query is checked too.
 * Matches: "Season 4", "Season IV", "S4", "Part 2", "4th Season", "Season-4".
 * Returns null when no explicit season is present (caller defaults to 1 / all seasons).
 */
fun parseSeasonNumberFromText(text: String?): Int? {
    if (text.isNullOrBlank()) return null
    fun num(s: String?): Int? = s?.toIntOrNull()?.takeIf { it in 1..50 }
    fun roman(s: String?): Int? = s?.lowercase()?.let { ROMAN_SEASONS[it] }
    // "4th Season" / "2nd Season"
    Regex("""\b0*(\d{1,2})(?:st|nd|rd|th)\s*season\b""", RegexOption.IGNORE_CASE)
        .find(text)?.let { if (num(it.groupValues[1]) != null) return num(it.groupValues[1]) }
    // "Season 4", "Season-4", "Season: 4"
    Regex("""\bseason\s*[-:=]?\s*0*(\d{1,2})\b""", RegexOption.IGNORE_CASE)
        .find(text)?.let { if (num(it.groupValues[1]) != null) return num(it.groupValues[1]) }
    // "Season IV"
    Regex("""\bseason\s*[-:=]?\s*(viii|vii|vi|iv|ix|iii|ii|x|v|i)\b""", RegexOption.IGNORE_CASE)
        .find(text)?.let { if (roman(it.groupValues[1]) != null) return roman(it.groupValues[1]) }
    // "Part 2" (split-cour continuation treated as next season)
    Regex("""\bpart\s*[-:=]?\s*0*(\d{1,2})\b""", RegexOption.IGNORE_CASE)
        .find(text)?.let { if (num(it.groupValues[1]) != null) return num(it.groupValues[1]) }
    // "S4" / " S04 " (standalone code, not inside a word)
    Regex("""(?:^|[\s\-:])S0*(\d{1,2})(?=$|[\s\-:.])""", RegexOption.IGNORE_CASE)
        .find(text)?.let { if (num(it.groupValues[1]) != null) return num(it.groupValues[1]) }
    return null
}

data class MetadataResult(
    val source: String,
    val sourceId: String,
    val title: String,
    val posterUrl: String,
    val description: String,
    val tags: List<String>,
    val seasonEpisodeCounts: List<Int>,
    val streamCandidates: List<StreamCandidate>,
    val year: String = "",
    val type: MediaType = MediaType.Series,
    /** Season number per entry in [seasonEpisodeCounts] (parallel list). Empty = [1]. */
    val seasonNumbers: List<Int> = emptyList(),
    /** Epoch millis of the first air time when the source exposes it (e.g. AniList `airingAt`). */
    val releaseAt: Long? = null,
    /** Source says the title has not aired yet (e.g. AniList status NOT_YET_RELEASED). */
    val upcoming: Boolean = false
)

class MetadataRepository {

    suspend fun search(query: String, tmdbKey: String? = null, region: String = "ID"): Result<List<MetadataResult>> =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return@withContext Result.failure(IllegalArgumentException("blank query"))
            val results = mutableListOf<MetadataResult>()
            val anilist = runCatching { searchAniList(trimmed) }
            anilist.getOrNull()?.let { results.addAll(it) }
            // TVMaze: free, no key. Best source of precise next-episode airtimes
            // for Western TV, so it sits between AniList and TMDB.
            val tvmaze = runCatching { searchTVMaze(trimmed) }
            tvmaze.getOrNull()?.let { results.addAll(it) }
            if (!tmdbKey.isNullOrBlank()) {
                val tmdb = runCatching { searchTmdb(trimmed, tmdbKey.trim(), region) }
                tmdb.getOrNull()?.let { results.addAll(it) }
            }
            if (results.isEmpty()) {
                Result.failure(IllegalStateException("No results"))
            } else Result.success(results)
        }

    private fun searchAniList(query: String): List<MetadataResult> {
        val body = JSONObject().apply {
            put("query", """
                query (${'$'}search: String) {
                  Media(search: ${'$'}search, type: ANIME) {
                    id title { english romaji }
                    coverImage { large } description genres episodes format
                    status
                    startDate { year month day }
                    nextAiringEpisode { airingAt episode }
                    externalLinks { url site type }
                  }
                }
            """.trimIndent())
            put("variables", JSONObject().put("search", query))
        }
        val json = postJson("https://graphql.anilist.co", body)
        val media = json.optJSONObject("data")?.optJSONObject("Media") ?: return emptyList()
        val titleObj = media.optJSONObject("title")
        val title = titleObj?.optString("english")?.takeIf { it.isNotBlank() }
            ?: titleObj?.optString("romaji") ?: ""
        val poster = media.optJSONObject("coverImage")?.optString("large") ?: ""
        val descRaw = media.optString("description")
        val desc = stripHtml(descRaw)
        val tags = mutableListOf<String>()
        val genres = media.optJSONArray("genres")
        if (genres != null) for (i in 0 until genres.length()) tags.add(genres.optString(i))
        val total = media.optInt("episodes", 0)
        val isMovie = media.optString("format").equals("MOVIE", ignoreCase = true)
        val mediaType = if (isMovie) MediaType.Movie else MediaType.Series
        // AniList models each season as its own entry ("…Season 4"). Its `season`
        // field is the release season (WINTER/SPRING…), not the sequel number,
        // so the number comes from the entry's title (then the search query).
        val detectedSeason = if (isMovie) null
            else parseSeasonNumberFromText(title) ?: parseSeasonNumberFromText(query)
        val seasons = when {
            isMovie -> listOf(1)
            total > 0 -> listOf(total)
            else -> emptyList()
        }
        val seasonNumbers = when {
            isMovie -> listOf(1)
            seasons.isNotEmpty() -> listOf(detectedSeason ?: 1)
            else -> emptyList()
        }
        val streams = mutableListOf<StreamCandidate>()
        val links = media.optJSONArray("externalLinks")
        if (links != null) for (i in 0 until links.length()) {
            val link = links.optJSONObject(i) ?: continue
            val site = link.optString("site")
            val url = link.optString("url")
            // AniList ExternalLinkType: INFO | STREAMING | SOCIAL.
            // Only STREAMING links are playback candidates — INFO (wiki/official sites)
            // or SOCIAL links must not become the item's streamUrl via applyFetch.
            val linkType = link.optString("type")
            if (!linkType.equals("STREAMING", ignoreCase = true)) continue
            if (url.isNotBlank()) streams.add(StreamCandidate(site.ifBlank { "Official" }, url))
        }
        val year = media.optJSONObject("startDate")?.optInt("year", 0)?.takeIf { it > 0 }?.toString() ?: ""
        // AniList knows the real schedule: `nextAiringEpisode.airingAt` is the exact
        // epoch timestamp of the upcoming episode (episode 1 for a not-yet-released
        // title). Only use it for unreleased entries — once a show is airing it points
        // at a later episode, not at its release date.
        val sourceStatus = media.optString("status")
        val upcoming = sourceStatus.equals("NOT_YET_RELEASED", ignoreCase = true)
        val nextAirAt = media.optJSONObject("nextAiringEpisode")
            ?.optLong("airingAt", 0L)
            ?.takeIf { it > 0L }
            ?.times(1000L)
        val startAt = startDateMillis(media.optJSONObject("startDate"))
        val releaseAt = if (upcoming) nextAirAt ?: startAt else null
        return listOf(
            MetadataResult(
                source = "AniList",
                sourceId = media.optString("id"),
                title = title,
                posterUrl = poster,
                description = desc,
                tags = tags,
                seasonEpisodeCounts = seasons,
                streamCandidates = streams,
                year = year,
                type = mediaType,
                seasonNumbers = seasonNumbers,
                releaseAt = releaseAt,
                upcoming = upcoming
            )
        )
    }

    private fun startDateMillis(date: JSONObject?): Long? {
        if (date == null) return null
        val year = date.optInt("year", 0)
        val month = date.optInt("month", 0)
        val day = date.optInt("day", 0)
        if (year <= 0 || month !in 1..12 || day !in 1..31) return null
        return runCatching {
            java.time.LocalDate.of(year, month, day)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private fun parseIsoDateMillis(text: String?): Long? {
        val raw = text?.trim().orEmpty()
        if (raw.length < 10) return null
        return runCatching {
            java.time.LocalDate.parse(raw.substring(0, 10))
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    /**
     * TVMaze search (free, no key): GET /search/shows?q=...
     * For each hit we fetch /shows/{id}/episodes once to derive
     * (a) real season/episode counts and (b) the next future airstamp —
     * the countdown source for Coming Soon when AniList/TMDB dates are coarse.
     */
    private fun searchTVMaze(query: String): List<MetadataResult> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val array = getJsonArray("https://api.tvmaze.com/search/shows?q=$encoded")
        val list = mutableListOf<MetadataResult>()
        for (i in 0 until minOf(3, array.length())) {
            val entry = array.optJSONObject(i) ?: continue
            val show = entry.optJSONObject("show") ?: entry
            val id = show.optString("id")
            if (id.isBlank()) continue
            val title = show.optString("name")
            if (title.isBlank()) continue
            val image = show.optJSONObject("image")
            val poster = image?.optString("original")
                ?: image?.optString("medium") ?: ""
            val description = stripHtml(show.optString("summary"))
            val tags = mutableListOf<String>()
            val genres = show.optJSONArray("genres")
            if (genres != null) {
                for (g in 0 until genres.length()) {
                    genres.optString(g).trim().takeIf { it.isNotEmpty() }?.let { tags.add(it) }
                }
            }
            val status = show.optString("status")
            val premiered = show.optString("premiered")
            val year = if (premiered.length >= 4) premiered.take(4) else ""

            // Episodes give both season structure and the next airtime.
            val (seasonCounts, nextAir) = fetchTvmazeEpisodes(id)
            val seasonEpisodeCounts = if (seasonCounts.isEmpty()) listOf(1) else seasonCounts.map { it.second }
            val seasonNumbers = if (seasonCounts.isEmpty()) listOf(1) else seasonCounts.map { it.first }
            val upcoming = status.equals("Running", ignoreCase = true) &&
                nextAir != null && nextAir > System.currentTimeMillis()

            list.add(
                MetadataResult(
                    source = "TVMaze",
                    sourceId = id,
                    title = title,
                    posterUrl = poster,
                    description = description,
                    tags = tags,
                    seasonEpisodeCounts = seasonEpisodeCounts,
                    streamCandidates = emptyList(),
                    year = year,
                    type = MediaType.Series,
                    seasonNumbers = seasonNumbers,
                    releaseAt = nextAir,
                    upcoming = upcoming
                )
            )
        }
        return list
    }

    /** Returns ((season, episodeCount) sorted, next future airstamp millis or null). */
    private fun fetchTvmazeEpisodes(showId: String): Pair<List<Pair<Int, Int>>, Long?> {
        return try {
            val arr = getJsonArray("https://api.tvmaze.com/shows/$showId/episodes")
            val bySeason = mutableMapOf<Int, Int>()
            var nextAir: Long? = null
            val now = System.currentTimeMillis()
            for (i in 0 until arr.length()) {
                val ep = arr.optJSONObject(i) ?: continue
                val season = ep.optInt("season", 0)
                // season 0 = specials; skip so Pick Folder targets regular seasons.
                if (season > 0) bySeason[season] = (bySeason[season] ?: 0) + 1
                val airstamp = ep.optString("airstamp")
                if (airstamp.isNotBlank()) {
                    val air = parseAirstampMillis(airstamp)
                    if (air != null && air > now && (nextAir == null || air < nextAir)) nextAir = air
                }
            }
            Pair(bySeason.toList().sortedBy { it.first }, nextAir)
        } catch (_: Exception) {
            Pair(emptyList(), null)
        }
    }

    /** TVMaze airstamp is ISO-8601 with offset, e.g. 2027-01-17T15:00:00+00:00. */
    private fun parseAirstampMillis(text: String): Long? {
        return runCatching {
            java.time.OffsetDateTime.parse(text.trim()).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun searchTmdb(query: String, key: String, region: String): List<MetadataResult> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val list = mutableListOf<MetadataResult>()
        // search movie
        val movieJson = getJson("https://api.themoviedb.org/3/search/movie?api_key=${URLEncoder.encode(key, "UTF-8")}&query=$encoded&include_adult=false")
        movieJson.optJSONArray("results")?.let { arr ->
            for (i in 0 until minOf(5, arr.length())) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id")
                val title = item.optString("title") ?: item.optString("original_title") ?: ""
                val posterPath = item.optString("poster_path")
                val poster = if (posterPath.isNotBlank()) "https://image.tmdb.org/t/p/w500$posterPath" else ""
                val desc = item.optString("overview")
                val tags = mutableListOf<String>()
                // TMDB search does not include genre names, only ids — skip.
                val releaseAt = parseIsoDateMillis(item.optString("release_date"))
                val year = (item.optString("release_date").take(4))
                val providers = fetchTmdbProviders(key, "movie", id, region)
                list.add(
                    MetadataResult(
                        source = "TMDB",
                        sourceId = id,
                        title = title,
                        posterUrl = poster,
                        description = desc,
                        tags = tags,
                        seasonEpisodeCounts = listOf(1),
                        streamCandidates = providers,
                        year = year,
                        type = MediaType.Movie,
                        seasonNumbers = listOf(1),
                        releaseAt = releaseAt,
                        upcoming = releaseAt != null && releaseAt > System.currentTimeMillis()
                    )
                )
            }
        }
        // search tv
        val tvJson = getJson("https://api.themoviedb.org/3/search/tv?api_key=${URLEncoder.encode(key, "UTF-8")}&query=$encoded&include_adult=false")
        tvJson.optJSONArray("results")?.let { arr ->
            for (i in 0 until minOf(5, arr.length())) {
                val item = arr.optJSONObject(i) ?: continue
                val id = item.optString("id")
                val title = item.optString("name") ?: item.optString("original_name") ?: ""
                val posterPath = item.optString("poster_path")
                val poster = if (posterPath.isNotBlank()) "https://image.tmdb.org/t/p/w500$posterPath" else ""
                val desc = item.optString("overview")
                val firstAir = item.optString("first_air_date")
                val releaseAt = parseIsoDateMillis(firstAir)
                val year = (firstAir.take(4))
                // episode count requires detail call — keep (season_number, episode_count)
                val allSeasons = fetchTmdbSeasons(key, id)
                // TMDB returns the whole show; if the query/title names one season
                // ("re:zero season 4") expose only that season so Pick Folder lands on it.
                val wantedSeason = parseSeasonNumberFromText(title)
                    ?: parseSeasonNumberFromText(query)
                val pickedSeasons = if (wantedSeason != null && allSeasons.any { it.first == wantedSeason }) {
                    allSeasons.filter { it.first == wantedSeason }
                } else allSeasons
                val providers = fetchTmdbProviders(key, "tv", id, region)
                list.add(
                    MetadataResult(
                        source = "TMDB",
                        sourceId = id,
                        title = title,
                        posterUrl = poster,
                        description = desc,
                        tags = emptyList(),
                        seasonEpisodeCounts = pickedSeasons.map { it.second },
                        streamCandidates = providers,
                        year = year,
                        type = MediaType.Series,
                        seasonNumbers = pickedSeasons.map { it.first },
                        releaseAt = releaseAt,
                        upcoming = releaseAt != null && releaseAt > System.currentTimeMillis()
                    )
                )
            }
        }
        return list
    }

    /** (season_number, episode_count) pairs, e.g. (4, 19) for Season 4. */
    private fun fetchTmdbSeasons(key: String, tvId: String): List<Pair<Int, Int>> {
        return try {
            val detail = getJson("https://api.themoviedb.org/3/tv/${URLEncoder.encode(tvId, "UTF-8")}?api_key=${URLEncoder.encode(key, "UTF-8")}")
            val seasonsArr = detail.optJSONArray("seasons") ?: return emptyList()
            val out = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until seasonsArr.length()) {
                val s = seasonsArr.optJSONObject(i) ?: continue
                val seasonNumber = s.optInt("season_number", 0)
                if (seasonNumber <= 0) continue
                val epCount = s.optInt("episode_count", 0)
                if (epCount > 0) out.add(seasonNumber to epCount)
            }
            out.sortedBy { it.first }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun fetchTmdbProviders(key: String, type: String, id: String, region: String): List<StreamCandidate> {
        return try {
            val json = getJson(
                "https://api.themoviedb.org/3/$type/${URLEncoder.encode(id, "UTF-8")}/watch/providers?api_key=${URLEncoder.encode(key, "UTF-8")}&region=${URLEncoder.encode(region, "UTF-8")}"
            )
            val results = json.optJSONObject("results") ?: return emptyList()
            val regionObj = results.optJSONObject(region.uppercase())
                ?: results.optJSONObject(region) ?: return emptyList()
            val flat = regionObj.optJSONArray("flatrate") ?: JSONArray()
            val out = mutableListOf<StreamCandidate>()
            val fallbackUrl = "https://www.themoviedb.org/$type/$id"
            for (i in 0 until flat.length()) {
                val p = flat.optJSONObject(i) ?: continue
                val name = p.optString("provider_name")
                // TMDB flatrate has no direct watch URL; point to TMDB title page where providers are listed.
                val url = p.optString("link").ifBlank { fallbackUrl }
                if (name.isNotBlank() && url.isNotBlank()) out.add(StreamCandidate(name, url))
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getJson(urlStr: String): JSONObject {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        conn.disconnect()
        if (code !in 200..299) throw IllegalStateException("HTTP $code: $text")
        return JSONObject(text)
    }

    private fun getJsonArray(urlStr: String): JSONArray {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        conn.disconnect()
        if (code !in 200..299) throw IllegalStateException("HTTP $code: $text")
        return JSONArray(text)
    }

    private fun postJson(urlStr: String, body: JSONObject): JSONObject {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        conn.disconnect()
        if (code !in 200..299) throw IllegalStateException("HTTP $code: $text")
        return JSONObject(text)
    }

    private fun stripHtml(html: String): String {
        if (html.isBlank()) return ""
        return html
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .trim()
    }
}
