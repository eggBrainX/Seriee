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
            val seeded = seed()
            saveItems(seeded)
            return seeded
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
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    fun exportBackupJson(items: List<MediaItem>): String {
        val root = JSONObject()
        root.put("version", 1)
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

    private fun seed(): List<MediaItem> {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 24 * hour
        return listOf(
            MediaItem(
                id = "1", title = "Genshin Impact: Fontaine", type = MediaType.Series,
                status = WatchStatus.Watching,
                poster = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                season = 1, currentEp = 8, totalEp = 12, pinned = true,
                streamUrl = "https://www.youtube.com",
                tags = listOf("Anime", "Fantasy", "Action"),
                rating = 4,
                updatedAt = now - 2 * hour
            ),
            MediaItem(
                id = "2", title = "Arcane Season 2", type = MediaType.Series,
                status = WatchStatus.Watching,
                poster = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=500&q=80",
                season = 2, currentEp = 4, totalEp = 9, pinned = true,
                streamUrl = "https://www.netflix.com",
                tags = listOf("Animation", "Action", "Sci-Fi"),
                rating = 5,
                updatedAt = now - day
            ),
            MediaItem(
                id = "3", title = "Stranger Things S5", type = MediaType.Series,
                status = WatchStatus.PlanToWatch,
                poster = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
                season = 5, currentEp = 0, totalEp = 8,
                streamUrl = "https://www.netflix.com",
                tags = listOf("Sci-Fi", "Drama", "Mystery"),
                rating = 4,
                updatedAt = now - 3 * day
            ),
            MediaItem(
                id = "4", title = "Oppenheimer", type = MediaType.Movie,
                status = WatchStatus.Completed,
                poster = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
                season = 1, currentEp = 1, totalEp = 1,
                videoPath = "Oppenheimer.2023.1080p.mkv",
                tags = listOf("Drama", "Biography", "History"),
                rating = 5,
                updatedAt = now - 7 * day
            ),
            MediaItem(
                id = "5", title = "One Piece Live Action", type = MediaType.Series,
                status = WatchStatus.Watching,
                poster = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80",
                season = 1, currentEp = 5, totalEp = 8,
                streamUrl = "https://www.netflix.com",
                tags = listOf("Action", "Adventure", "Fantasy"),
                rating = 4,
                updatedAt = now - hour
            ),
            MediaItem(
                id = "6", title = "Interstellar", type = MediaType.Movie,
                status = WatchStatus.Watching,
                poster = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&q=80",
                season = 1, currentEp = 1, totalEp = 1,
                tags = listOf("Sci-Fi", "Adventure", "Drama"),
                rating = 5,
                updatedAt = now
            ),
            MediaItem(
                id = "7", title = "Blade Runner 2099", type = MediaType.Series,
                status = WatchStatus.ComingSoon,
                poster = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
                season = 1, currentEp = 0, totalEp = 10,
                releaseDate = now + 5 * day,
                reminderEnabled = true,
                streamUrl = "https://www.primevideo.com",
                tags = listOf("Sci-Fi", "Action", "Cyberpunk"),
                rating = 0,
                updatedAt = now
            )
        )
    }

    private companion object {
        const val KEY_DATA = "seriee_data_v2"
        const val KEY_THEME = "seriee_theme"
        const val KEY_GRID = "seriee_grid"
        const val KEY_LANG = "seriee_lang"
    }
}
