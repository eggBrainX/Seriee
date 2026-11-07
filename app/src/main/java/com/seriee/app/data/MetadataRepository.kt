package com.seriee.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class StreamCandidate(val name: String, val url: String)

data class MetadataResult(
    val source: String,
    val sourceId: String,
    val title: String,
    val posterUrl: String,
    val description: String,
    val tags: List<String>,
    val seasonEpisodeCounts: List<Int>,
    val streamCandidates: List<StreamCandidate>,
    val year: String = ""
)

class MetadataRepository {

    suspend fun search(query: String, tmdbKey: String? = null, region: String = "ID"): Result<List<MetadataResult>> =
        withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return@withContext Result.failure(IllegalArgumentException("blank query"))
            val results = mutableListOf<MetadataResult>()
            val anilist = runCatching { searchAniList(trimmed) }
            anilist.getOrNull()?.let { results.addAll(it) }
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
                    coverImage { large } description genres episodes
                    startDate { year }
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
        val seasons = if (total > 0) listOf(total) else emptyList()
        val streams = mutableListOf<StreamCandidate>()
        val links = media.optJSONArray("externalLinks")
        if (links != null) for (i in 0 until links.length()) {
            val site = links.optJSONObject(i)?.optString("site") ?: ""
            val url = links.optJSONObject(i)?.optString("url") ?: ""
            if (url.isNotBlank()) streams.add(StreamCandidate(site.ifBlank { "Official" }, url))
        }
        val year = media.optJSONObject("startDate")?.optInt("year", 0)?.takeIf { it > 0 }?.toString() ?: ""
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
                year = year
            )
        )
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
                        year = year
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
                val year = (item.optString("first_air_date").take(4))
                // episode count requires detail call
                val seasons = fetchTmdbSeasonCounts(key, id)
                val providers = fetchTmdbProviders(key, "tv", id, region)
                list.add(
                    MetadataResult(
                        source = "TMDB",
                        sourceId = id,
                        title = title,
                        posterUrl = poster,
                        description = desc,
                        tags = emptyList(),
                        seasonEpisodeCounts = seasons,
                        streamCandidates = providers,
                        year = year
                    )
                )
            }
        }
        return list
    }

    private fun fetchTmdbSeasonCounts(key: String, tvId: String): List<Int> {
        return try {
            val detail = getJson("https://api.themoviedb.org/3/tv/${URLEncoder.encode(tvId, "UTF-8")}?api_key=${URLEncoder.encode(key, "UTF-8")}")
            val seasonsArr = detail.optJSONArray("seasons") ?: return emptyList()
            val counts = mutableListOf<Int>()
            for (i in 0 until seasonsArr.length()) {
                val s = seasonsArr.optJSONObject(i) ?: continue
                val seasonNumber = s.optInt("season_number", 0)
                if (seasonNumber <= 0) continue
                val epCount = s.optInt("episode_count", 0)
                if (epCount > 0) counts.add(epCount)
            }
            counts
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
