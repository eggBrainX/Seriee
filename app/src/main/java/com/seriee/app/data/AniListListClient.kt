package com.seriee.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class ImportFetchErrorCode {
    USER_NOT_FOUND,
    PRIVATE_OR_EMPTY,
    RATE_LIMITED,
    NETWORK,
    PARSE,
    UNKNOWN,
}

class ImportFetchException(val code: ImportFetchErrorCode, message: String? = null) :
    Exception(message ?: code.name)

object AniListListClient {
    private const val QUERY = """
        query (${'$'}userName: String, ${'$'}page: Int) {
          Page(page: ${'$'}page, perPage: 50) {
            pageInfo { hasNextPage }
            mediaList(userName: ${'$'}userName, type: ANIME, sort: UPDATED_TIME_DESC) {
              status
              progress
              score(format: POINT_100)
              media {
                id format episodes genres description(asHtml: false)
                title { romaji english native }
                coverImage { extraLarge large }
              }
            }
          }
        }
    """

    suspend fun fetch(username: String): List<AniListListRow> = withContext(Dispatchers.IO) {
        val clean = username.trim()
        if (clean.isEmpty()) throw ImportFetchException(ImportFetchErrorCode.USER_NOT_FOUND)
        val rows = ArrayList<AniListListRow>()
        var page = 1
        var hasNext = true

        while (hasNext && page <= 40) {
            var code: Int
            var body: String
            fun exec(): Pair<Int, String> {
                val payload = JSONObject().apply {
                    put("query", QUERY)
                    put("variables", JSONObject().apply {
                        put("userName", clean)
                        put("page", page)
                    })
                }.toString()
                val conn = (URL("https://graphql.anilist.co").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                }
                conn.outputStream.use { it.write(payload.toByteArray()) }
                val c = conn.responseCode
                val text = (if (c in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                return c to text
            }

            try {
                val res = exec()
                code = res.first
                body = res.second
                if (code == 429) {
                    delay(1500)
                    val retry = exec()
                    code = retry.first
                    body = retry.second
                }
            } catch (e: ImportFetchException) {
                throw e
            } catch (_: Exception) {
                throw ImportFetchException(ImportFetchErrorCode.NETWORK)
            }

            if (code == 429) throw ImportFetchException(ImportFetchErrorCode.RATE_LIMITED)
            if (code == 404) throw ImportFetchException(ImportFetchErrorCode.USER_NOT_FOUND)
            if (code !in 200..299) throw ImportFetchException(ImportFetchErrorCode.UNKNOWN)

            val json = runCatching { JSONObject(body) }.getOrElse {
                throw ImportFetchException(ImportFetchErrorCode.PARSE)
            }
            val errs = json.optJSONArray("errors")
            if (errs != null && errs.length() > 0) {
                val msg = errs.optJSONObject(0)?.optString("message", "")?.lowercase() ?: ""
                val st = errs.optJSONObject(0)?.optInt("status", 0) ?: 0
                if (st == 404 || "not found" in msg) throw ImportFetchException(ImportFetchErrorCode.USER_NOT_FOUND)
                if ("private" in msg) throw ImportFetchException(ImportFetchErrorCode.PRIVATE_OR_EMPTY)
                throw ImportFetchException(ImportFetchErrorCode.UNKNOWN)
            }

            val pageObj = json.optJSONObject("data")?.optJSONObject("Page")
                ?: throw ImportFetchException(ImportFetchErrorCode.PARSE)
            hasNext = pageObj.optJSONObject("pageInfo")?.optBoolean("hasNextPage", false) ?: false
            val listArr = pageObj.optJSONArray("mediaList")
            if (listArr != null) {
                for (i in 0 until listArr.length()) {
                    val item = listArr.optJSONObject(i) ?: continue
                    val media = item.optJSONObject("media") ?: continue
                    val tObj = media.optJSONObject("title")
                    val title = listOf("english", "romaji", "native")
                        .firstNotNullOfOrNull { k -> tObj?.optString(k)?.takeIf { it.isNotBlank() && it != "null" } }
                        ?: "Untitled"
                    val cover = media.optJSONObject("coverImage")
                    val poster = cover?.optString("extraLarge")?.takeIf { it.isNotBlank() && it != "null" }
                        ?: cover?.optString("large")?.takeIf { it.isNotBlank() && it != "null" }
                        ?: ""
                    val gArr = media.optJSONArray("genres")
                    val genres = if (gArr != null) {
                        (0 until gArr.length()).mapNotNull { gArr.optString(it).takeIf { s -> s.isNotBlank() } }
                    } else emptyList()
                    rows.add(
                        AniListListRow(
                            mediaId = media.optInt("id", 0),
                            title = title,
                            format = media.optString("format", "TV"),
                            episodes = media.optInt("episodes", 0),
                            status = item.optString("status", "PLANNING"),
                            progress = item.optInt("progress", 0),
                            score100 = item.optInt("score", 0),
                            poster = poster,
                            genres = genres,
                            description = media.optString("description", "")
                                .replace(Regex("<[^>]*>"), " ")
                                .replace(Regex("\\s+"), " ")
                                .trim(),
                        )
                    )
                }
            }
            page++
        }

        if (rows.isEmpty()) throw ImportFetchException(ImportFetchErrorCode.PRIVATE_OR_EMPTY)
        rows
    }
}
