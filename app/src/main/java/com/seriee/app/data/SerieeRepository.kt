package com.seriee.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SerieeRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("seriee", Context.MODE_PRIVATE)

    fun loadItems(): List<MediaItem> {
        val raw = prefs.getString(KEY_DATA, null)
        if (raw == null) {
            return emptyList()
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveItems(items: List<MediaItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(KEY_DATA, arr.toString()).apply()
    }

    fun loadTheme(): AppTheme {
        val name = prefs.getString(KEY_THEME, AppTheme.Dark.name) ?: AppTheme.Dark.name
        return runCatching { AppTheme.valueOf(name) }.getOrDefault(AppTheme.Dark)
    }

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun loadGridView(): Boolean = prefs.getBoolean(KEY_GRID, true)

    fun saveGridView(grid: Boolean) {
        prefs.edit().putBoolean(KEY_GRID, grid).apply()
    }

    fun loadLanguage(): AppLanguage =
        AppLanguage.fromTag(prefs.getString(KEY_LANG, AppLanguage.English.tag))

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANG, language.tag).apply()
    }

    fun loadTmdbKey(): String = prefs.getString(KEY_TMDB, "") ?: ""
    fun saveTmdbKey(key: String) {
        prefs.edit().putString(KEY_TMDB, key.trim()).apply()
    }

    fun loadLastSeenVersionCode(): Int = prefs.getInt(KEY_LAST_SEEN_VERSION, 0)
    fun saveLastSeenVersionCode(code: Int) {
        prefs.edit().putInt(KEY_LAST_SEEN_VERSION, code).apply()
    }

    private fun toJson(m: MediaItem): JSONObject = JSONObject().apply {
        put("id", m.id)
        put("title", m.title)
        put("type", m.type.name)
        put("status", m.status.name)
        put("poster", m.poster)
        put("season", m.season)
        put("currentEp", m.currentEp)
        put("totalEp", m.totalEp)
        put("pinned", m.pinned)
        put("streamUrl", m.streamUrl)
        put("videoPath", m.videoPath)
        put("videoUri", m.videoUri)
        put("releaseDate", m.releaseDate ?: -1L)
        put("reminderEnabled", m.reminderEnabled)
        val tagsArr = JSONArray()
        m.tags.forEach { tagsArr.put(it) }
        put("tags", tagsArr)
        put("rating", m.rating)
        put("updatedAt", m.updatedAt)
        put("description", m.description)
        put("source", m.source)
        put("sourceId", m.sourceId)
        val seasonsArr = JSONArray()
        m.seasons.forEach { s ->
            val sj = JSONObject()
            sj.put("number", s.number)
            sj.put("currentEp", s.currentEp)
            sj.put("totalEp", s.totalEp)
            val eps = JSONArray()
            s.episodes.forEach { ep ->
                val ej = JSONObject()
                ej.put("number", ep.number)
                ej.put("uri", ep.uri)
                ej.put("displayName", ep.displayName)
                eps.put(ej)
            }
            sj.put("episodes", eps)
            seasonsArr.put(sj)
        }
        put("seasons", seasonsArr)
    }

    private fun fromJson(o: JSONObject): MediaItem {
        val rel = o.optLong("releaseDate", -1L)
        val tagsArr = o.optJSONArray("tags")
        val parsedTags = if (tagsArr != null) {
            (0 until tagsArr.length()).mapNotNull { i ->
                tagsArr.optString(i).trim().takeIf { it.isNotEmpty() }
            }
        } else {
            val tagsStr = o.optString("tags")
            if (tagsStr.isNotBlank()) tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            else emptyList()
        }
        val parsedRating = o.optInt("rating", 0).coerceIn(0, 5)
        val parsedSeasons: List<Season> = runCatching {
            val arr = o.optJSONArray("seasons") ?: return@runCatching emptyList()
            (0 until arr.length()).map { i ->
                val sj = arr.getJSONObject(i)
                val epsArr = sj.optJSONArray("episodes")
                val eps = if (epsArr != null) {
                    (0 until epsArr.length()).map { j ->
                        val ej = epsArr.getJSONObject(j)
                        EpisodeFile(
                            number = ej.optInt("number", j + 1),
                            uri = ej.optString("uri"),
                            displayName = ej.optString("displayName")
                        )
                    }
                } else emptyList()
                Season(
                    number = sj.optInt("number", 1),
                    currentEp = sj.optInt("currentEp", 0),
                    totalEp = sj.optInt("totalEp", 1),
                    episodes = eps
                )
            }
        }.getOrDefault(emptyList())

        return MediaItem(
            id = o.optString("id"),
            title = o.optString("title"),
            type = runCatching { MediaType.valueOf(o.optString("type")) }.getOrDefault(MediaType.Series),
            status = runCatching { WatchStatus.valueOf(o.optString("status")) }.getOrDefault(WatchStatus.Watching),
            poster = o.optString("poster"),
            season = o.optInt("season", 1),
            currentEp = o.optInt("currentEp", 1),
            totalEp = o.optInt("totalEp", 1),
            pinned = o.optBoolean("pinned", false),
            streamUrl = o.optString("streamUrl"),
            videoPath = o.optString("videoPath"),
            videoUri = o.optString("videoUri"),
            releaseDate = if (rel > 0) rel else null,
            reminderEnabled = o.optBoolean("reminderEnabled", false),
            tags = parsedTags,
            rating = parsedRating,
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
            seasons = parsedSeasons,
            description = o.optString("description"),
            source = o.optString("source"),
            sourceId = o.optString("sourceId")
        )
    }

    fun exportBackupJson(items: List<MediaItem>): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("appName", "Seriee")
        root.put("timestamp", System.currentTimeMillis())
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        root.put("items", arr)
        return root.toString(2)
    }

    fun importBackupJson(rawJson: String): List<MediaItem> {
        val json = rawJson.trim()
        val itemsArray = if (json.startsWith("{")) {
            val root = JSONObject(json)
            root.optJSONArray("items") ?: JSONArray()
        } else {
            JSONArray(json)
        }
        return (0 until itemsArray.length()).map { fromJson(itemsArray.getJSONObject(it)) }
    }

    private companion object {
        const val KEY_DATA = "seriee_data_v2"
        const val KEY_THEME = "seriee_theme"
        const val KEY_GRID = "seriee_grid"
        const val KEY_LANG = "seriee_lang"
        const val KEY_TMDB = "tmdb_api_key"
        const val KEY_LAST_SEEN_VERSION = "last_seen_version_code"
    }
}
