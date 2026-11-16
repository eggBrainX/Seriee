package com.seriee.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Parsed contents of an export file. [history] is null when the file has no history field. */
data class BackupPayload(
    val items: List<MediaItem>,
    val history: List<WatchEvent>?
)

class SerieeRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("seriee", Context.MODE_PRIVATE)

    // API keys are kept in a separate preferences file that is excluded from
    // Android Auto Backup and device-to-device transfer (see res/xml/backup_rules.xml
    // and res/xml/data_extraction_rules.xml).
    private val secretPrefs = context.applicationContext
        .getSharedPreferences("seriee_secrets", Context.MODE_PRIVATE)

    init {
        migrateLegacyKeys()
    }

    /** Moves keys saved by versions <= 0.2.7 out of the backed-up prefs file. */
    private fun migrateLegacyKeys() {
        val legacyTmdb = prefs.getString(KEY_TMDB, null)
        val legacyOmdb = prefs.getString(KEY_OMDB, null)
        if (legacyTmdb == null && legacyOmdb == null) return
        val editor = secretPrefs.edit()
        if (legacyTmdb != null) editor.putString(KEY_TMDB, legacyTmdb)
        if (legacyOmdb != null) editor.putString(KEY_OMDB, legacyOmdb)
        editor.apply()
        prefs.edit().remove(KEY_TMDB).remove(KEY_OMDB).apply()
    }

    fun loadItems(): List<MediaItem> {
        val raw = prefs.getString(KEY_DATA, null)
        if (raw == null) {
            return emptyList()
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length())
                .map { fromJson(arr.getJSONObject(it)) }
                .distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveItems(items: List<MediaItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(KEY_DATA, arr.toString()).apply()
    }

    /** Watch events, newest first. Malformed entries are skipped. */
    fun loadHistory(): List<WatchEvent> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                runCatching { historyFromJson(arr.getJSONObject(i)) }.getOrNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveHistory(events: List<WatchEvent>) {
        val arr = JSONArray()
        events.forEach { arr.put(historyToJson(it)) }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    /**
     * Appearance (light/dark/system). Falls back to the legacy flat theme list so
     * users upgrading from 0.4.1 keep the look they picked.
     */
    fun loadThemeMode(): AppThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null)
        if (stored != null) return AppThemeMode.fromName(stored)
        val legacy = prefs.getString(KEY_THEME, null) ?: return AppThemeMode.Light
        return LegacyThemeMap.migrate(legacy).first
    }

    /** Visual style, migrated from the legacy flat theme list when needed. */
    fun loadThemeStyle(): AppThemeStyle {
        val stored = prefs.getString(KEY_THEME_STYLE, null)
        if (stored != null) return AppThemeStyle.fromName(stored)
        val legacy = prefs.getString(KEY_THEME, null) ?: return AppThemeStyle.NeoBrutalism
        return LegacyThemeMap.migrate(legacy).second
    }

    fun saveTheme(mode: AppThemeMode, style: AppThemeStyle) {
        prefs.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .putString(KEY_THEME_STYLE, style.name)
            .apply()
    }

    /** Custom theme accent, stored as ARGB. Defaults to the blue primary. */
    fun loadCustomAccent(): Int = prefs.getInt(KEY_CUSTOM_ACCENT, 0xFF3B82F6.toInt())

    fun saveCustomAccent(argb: Int) {
        prefs.edit().putInt(KEY_CUSTOM_ACCENT, argb).apply()
    }

    /** Custom gradient stops, stored as ARGB. */
    fun loadCustomBackgroundStart(): Int = prefs.getInt(KEY_CUSTOM_BG_START, 0xFF1D4ED8.toInt())
    fun loadCustomBackgroundEnd(): Int = prefs.getInt(KEY_CUSTOM_BG_END, 0xFF7C2A64.toInt())

    fun saveCustomBackground(start: Int, end: Int) {
        prefs.edit()
            .putInt(KEY_CUSTOM_BG_START, start)
            .putInt(KEY_CUSTOM_BG_END, end)
            .apply()
    }

    /** Custom theme background: true = gradient, false = solid. */
    fun loadCustomGradient(): Boolean = prefs.getBoolean(KEY_CUSTOM_GRADIENT, false)

    fun saveCustomGradient(gradient: Boolean) {
        prefs.edit().putBoolean(KEY_CUSTOM_GRADIENT, gradient).apply()
    }

    /** Accessibility: collapse translucent glass surfaces to opaque ones. */
    fun loadReduceTransparency(): Boolean = prefs.getBoolean(KEY_REDUCE_TRANSPARENCY, false)

    fun saveReduceTransparency(value: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_TRANSPARENCY, value).apply()
    }

    /** Smart theme: switch the dark Classic style to AMOLED while battery saver runs. */
    fun loadBatterySaverAmoled(): Boolean = prefs.getBoolean(KEY_BATTERY_AMOLED, false)

    fun saveBatterySaverAmoled(value: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY_AMOLED, value).apply()
    }

    fun loadGridView(): Boolean = prefs.getBoolean(KEY_GRID, true)

    fun saveGridView(grid: Boolean) {
        prefs.edit().putBoolean(KEY_GRID, grid).apply()
    }

    fun loadLanguage(): AppLanguage =
        AppLanguage.fromTag(prefs.getString(KEY_LANG, ""))

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANG, language.tag).apply()
    }

    fun loadTmdbKey(): String = secretPrefs.getString(KEY_TMDB, "") ?: ""
    fun saveTmdbKey(key: String) {
        secretPrefs.edit().putString(KEY_TMDB, key.trim()).apply()
    }

    fun loadOmdbKey(): String = secretPrefs.getString(KEY_OMDB, "") ?: ""
    fun saveOmdbKey(key: String) {
        secretPrefs.edit().putString(KEY_OMDB, key.trim()).apply()
    }

    fun loadLastSeenVersionCode(): Int = prefs.getInt(KEY_LAST_SEEN_VERSION, 0)
    fun saveLastSeenVersionCode(code: Int) {
        prefs.edit().putInt(KEY_LAST_SEEN_VERSION, code).apply()
    }

    // Daily streak. lastWatchDay is ISO yyyy-MM-dd; watchDays holds the last 7 activity days.
    fun loadStreakDays(): Int = prefs.getInt(KEY_STREAK_DAYS, 0)
    fun loadBestStreak(): Int = prefs.getInt(KEY_BEST_STREAK, 0)
    fun loadLastWatchDay(): String = prefs.getString(KEY_LAST_WATCH_DAY, "") ?: ""
    fun loadWatchDays(): Set<String> =
        (prefs.getString(KEY_WATCH_DAYS, "") ?: "")
            .split(",").map { it.trim() }.filter { it.length == 10 }.toSet()

    fun saveStreak(streakDays: Int, bestStreak: Int, lastWatchDay: String, watchDays: Set<String>) {
        prefs.edit()
            .putInt(KEY_STREAK_DAYS, streakDays)
            .putInt(KEY_BEST_STREAK, bestStreak)
            .putString(KEY_LAST_WATCH_DAY, lastWatchDay)
            .putString(KEY_WATCH_DAYS, watchDays.sorted().joinToString(","))
            .apply()
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

        val rawId = o.optString("id").trim()
        val safeId = rawId.ifEmpty { UUID.randomUUID().toString() }
        val safeTotalEp = o.optInt("totalEp", 1).coerceAtLeast(1)
        val safeCurrentEp = o.optInt("currentEp", 1).coerceAtLeast(0)

        return MediaItem(
            id = safeId,
            title = o.optString("title"),
            type = runCatching { MediaType.valueOf(o.optString("type")) }.getOrDefault(MediaType.Series),
            status = runCatching { WatchStatus.valueOf(o.optString("status")) }.getOrDefault(WatchStatus.Watching),
            poster = o.optString("poster"),
            season = o.optInt("season", 1).coerceAtLeast(1),
            currentEp = safeCurrentEp,
            totalEp = safeTotalEp,
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

    private fun historyToJson(event: WatchEvent): JSONObject = JSONObject().apply {
        put("itemId", event.itemId)
        put("title", event.title)
        put("type", event.type.name)
        put("season", event.season)
        put("episode", event.episode)
        put("count", event.count)
        put("watchedAt", event.watchedAt)
    }

    private fun historyFromJson(o: JSONObject): WatchEvent? {
        val itemId = o.optString("itemId").trim()
        val watchedAt = o.optLong("watchedAt", 0L)
        if (itemId.isEmpty() || watchedAt <= 0L) return null
        return WatchEvent(
            itemId = itemId,
            title = o.optString("title"),
            type = runCatching { MediaType.valueOf(o.optString("type")) }
                .getOrDefault(MediaType.Series),
            season = o.optInt("season", 1).coerceAtLeast(0),
            episode = o.optInt("episode", 1).coerceAtLeast(0),
            count = o.optInt("count", 1).coerceAtLeast(1),
            watchedAt = watchedAt
        )
    }

    fun exportBackupJson(
        items: List<MediaItem>,
        history: List<WatchEvent> = loadHistory()
    ): String {
        val root = JSONObject()
        root.put("version", 4)
        root.put("appName", "Seriee")
        root.put("timestamp", System.currentTimeMillis())
        root.put("streakDays", loadStreakDays())
        root.put("bestStreak", loadBestStreak())
        root.put("lastWatchDay", loadLastWatchDay())
        val daysArr = JSONArray()
        loadWatchDays().forEach { daysArr.put(it) }
        root.put("watchDays", daysArr)
        val historyArr = JSONArray()
        history.forEach { historyArr.put(historyToJson(it)) }
        root.put("history", historyArr)
        val arr = JSONArray()
        items.forEach { arr.put(toJson(it)) }
        root.put("items", arr)
        return root.toString(2)
    }

    /**
     * Reads a backup without touching items or history storage. The streak side
     * effect stays here so callers keep the existing restore behaviour.
     */
    fun importBackupJson(rawJson: String): BackupPayload {
        val json = rawJson.trim()
        if (!json.startsWith("{")) {
            val arr = JSONArray(json)
            return BackupPayload(
                items = (0 until arr.length())
                    .map { fromJson(arr.getJSONObject(it)) }
                    .distinctBy { it.id },
                history = null
            )
        }
        val root = JSONObject(json)
        // Restore streak when the backup carries it (v3+); older backups keep current streak.
        if (root.has("streakDays")) {
            val daysArr = root.optJSONArray("watchDays")
            val days = mutableSetOf<String>()
            if (daysArr != null) for (i in 0 until daysArr.length()) days.add(daysArr.optString(i))
            saveStreak(
                root.optInt("streakDays", 0),
                root.optInt("bestStreak", 0),
                root.optString("lastWatchDay"),
                days
            )
        }
        val history = if (root.has("history")) {
            val historyArr = root.optJSONArray("history")
            if (historyArr == null) {
                emptyList()
            } else {
                (0 until historyArr.length()).mapNotNull { i ->
                    runCatching { historyFromJson(historyArr.getJSONObject(i)) }.getOrNull()
                }
            }
        } else {
            null
        }
        val itemsArray = root.optJSONArray("items") ?: JSONArray()
        val items = (0 until itemsArray.length())
            .map { fromJson(itemsArray.getJSONObject(it)) }
            .distinctBy { it.id }
        return BackupPayload(items = items, history = history)
    }

    private companion object {
        const val KEY_DATA = "seriee_data_v2"
        const val KEY_THEME = "seriee_theme"
        const val KEY_THEME_MODE = "seriee_theme_mode"
        const val KEY_THEME_STYLE = "seriee_theme_style"
        const val KEY_CUSTOM_ACCENT = "custom_accent"
        const val KEY_CUSTOM_BG_START = "custom_background_start"
        const val KEY_CUSTOM_BG_END = "custom_background_end"
        const val KEY_CUSTOM_GRADIENT = "custom_background_gradient"
        const val KEY_REDUCE_TRANSPARENCY = "seriee_reduce_transparency"
        const val KEY_BATTERY_AMOLED = "seriee_battery_amoled"
        const val KEY_GRID = "seriee_grid"
        const val KEY_LANG = "seriee_lang"
        const val KEY_TMDB = "tmdb_api_key"
        const val KEY_OMDB = "omdb_api_key"
        const val KEY_LAST_SEEN_VERSION = "last_seen_version_code"
        const val KEY_STREAK_DAYS = "streak_days"
        const val KEY_BEST_STREAK = "best_streak"
        const val KEY_LAST_WATCH_DAY = "last_watch_day"
        const val KEY_WATCH_DAYS = "watch_days"
        const val KEY_HISTORY = "watch_history_v1"
    }
}
