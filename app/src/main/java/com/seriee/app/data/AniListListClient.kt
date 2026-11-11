package com.seriee.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads a public AniList anime list without authentication.
 *
 * `Page.mediaList` is used instead of `MediaListCollection` so large lists can
 * be streamed in pages of 50. No username or token is persisted.
 */
class AniListListClient(
    private val endpoint: String = "https://graphql.anilist.co"
) {

    suspend fun fetch(userName: String): List<AniListEntry> = withContext(Dispatchers.IO) {
        val user = userName.trim()
        if (user.isBlank()) throw ImportException(ImportError.UserNotFound)

        val entries = ArrayList<AniListEntry>()
        var page = 1
        var hasNext = true
        var retried = false

        while (hasNext && page <= 40) {
            val payload = requestBody(user, page)
            val json = try {
                post(payload)
            } catch (e: ImportException) {
                if (e.error == ImportError.RateLimited && !retried) {
                    retried = true
                    try {
                        Thread.sleep(1200)
                    } catch (_: InterruptedException) {
                        throw ImportException(ImportError.Cancelled)
                    }
                    post(payload)
                } else {
                    throw e
                }
            }

            val pageObj = json.optJSONObject("data")?.optJSONObject("Page")
                ?: throw ImportException(ImportError.Parse)
            val rows = pageObj.optJSONArray("mediaList") ?: JSONArray()
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: continue
                val media = row.optJSONObject("media") ?: continue
                val titleObj = media.optJSONObject("title")
                val title = str(titleObj, "english")
                    .ifBlank { str(titleObj, "romaji") }
                    .ifBlank { str(titleObj, "native") }
                if (title.isBlank()) continue
                entries.add(
                    AniListEntry(
                        id = media.optLong("id", 0L),
                        title = title,
                        format = str(media, "format"),
                        totalEpisodes = media.optInt("episodes", 0),
                        progress = row.optInt("progress", 0),
                        score10 = row.optDouble("score", 0.0),
                        status = str(row, "status"),
                        poster = str(media.optJSONObject("coverImage"), "large"),
                        genres = stringList(media.optJSONArray("genres")),
                        description = str(media, "description"),
                        notes = str(row, "notes")
                    )
                )
            }

            hasNext = pageObj.optJSONObject("pageInfo")?.optBoolean("hasNextPage") ?: false
            page++
        }

        if (entries.isEmpty()) throw ImportException(ImportError.ListPrivateOrEmpty)
        entries
    }

    private fun requestBody(userName: String, page: Int): JSONObject {
        val query = """
            query (${'$'}userName: String, ${'$'}page: Int) {
              Page(page: ${'$'}page, perPage: 50) {
                pageInfo { hasNextPage }
                mediaList(userName: ${'$'}userName, type: ANIME, sort: [UPDATED_TIME_DESC]) {
                  status progress repeat notes
                  score(format: POINT_10)
                  media {
                    id episodes format genres description
                    title { english romaji native }
                    coverImage { large }
                  }
                }
              }
            }
        """.trimIndent()
        return JSONObject().apply {
            put("query", query)
            put("variables", JSONObject().put("userName", userName).put("page", page))
        }
    }

    private fun post(body: JSONObject): JSONObject {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        val text = try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            when {
                code == 429 -> throw ImportException(ImportError.RateLimited)
                code == 404 -> throw ImportException(ImportError.UserNotFound)
                code !in 200..299 -> throw ImportException(
                    if (response.contains("private", ignoreCase = true)) {
                        ImportError.ListPrivateOrEmpty
                    } else {
                        ImportError.Network
                    }
                )
            }
            response
        } catch (e: ImportException) {
            throw e
        } catch (e: IOException) {
            throw ImportException(ImportError.Network, e)
        } finally {
            connection.disconnect()
        }

        val json = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw ImportException(ImportError.Parse, e)
        }

        val errors = json.optJSONArray("errors")
        if (errors != null && errors.length() > 0) {
            val message = errors.optJSONObject(0)?.optString("message").orEmpty()
            throw ImportException(
                when {
                    message.contains("not found", ignoreCase = true) -> ImportError.UserNotFound
                    message.contains("private", ignoreCase = true) -> ImportError.ListPrivateOrEmpty
                    else -> ImportError.Parse
                }
            )
        }
        return json
    }

    private fun str(obj: JSONObject?, key: String): String {
        if (obj == null || obj.isNull(key)) return ""
        return obj.optString(key, "").trim()
    }

    private fun stringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            array.optString(i).trim().takeIf { it.isNotEmpty() }
        }
    }
}
