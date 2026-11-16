package com.seriee.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class StreakState(
    val streakDays: Int = 0,
    val bestStreak: Int = 0,
    val lastWatchDay: String? = null,
    val watchDays: Set<String> = emptySet(),
)

data class CustomThemeConfig(
    val accentArgb: Long = 0xFF3B82F6L,
    val bgStartArgb: Long = 0xFF0F172AL,
    val bgEndArgb: Long = 0xFF1E293BL,
    val useGradient: Boolean = false,
)

class SerieeRepository(context: Context) {
    private val prefs = context.getSharedPreferences("seriee", Context.MODE_PRIVATE)
    private val secretPrefs = context.getSharedPreferences("seriee_secrets", Context.MODE_PRIVATE)

    fun loadItems(): List<MediaItem> {
        val raw = prefs.getString("items", null) ?: return emptyList()
        return runCatching { parseItemsArray(JSONArray(raw)) }.getOrDefault(emptyList())
    }

    fun saveItems(items: List<MediaItem>) {
        prefs.edit().putString("items", itemsToJsonArray(items).toString()).apply()
    }

    fun loadHistory(): List<WatchEvent> {
        val raw = prefs.getString("watch_history", null) ?: return emptyList()
        return runCatching {
            WatchHistoryRules.trim(parseHistoryArray(JSONArray(raw)))
        }.getOrDefault(emptyList())
    }

    fun saveHistory(events: List<WatchEvent>) {
        val trimmed = WatchHistoryRules.trim(events)
        prefs.edit().putString("watch_history", historyToJsonArray(trimmed).toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().putString("watch_history", "[]").apply()
    }

    fun loadStreak(): StreakState {
        return StreakState(
            streakDays = prefs.getInt("streak_days", 0),
            bestStreak = prefs.getInt("best_streak", 0),
            lastWatchDay = prefs.getString("last_watch_day", null),
            watchDays = prefs.getStringSet("watch_days", emptySet()) ?: emptySet(),
        )
    }

    fun recordWatchToday(today: LocalDate = LocalDate.now()): StreakState {
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()
        val cur = loadStreak()
        val nextStreak = when {
            cur.lastWatchDay == todayStr -> cur.streakDays.coerceAtLeast(1)
            cur.lastWatchDay == yesterdayStr -> cur.streakDays + 1
            else -> 1
        }
        val nextBest = maxOf(cur.bestStreak, nextStreak)
        val cutoff = today.minusDays(6).toString()
        val nextDays = (cur.watchDays + todayStr).filter { it >= cutoff }.toSet()
        prefs.edit()
            .putInt("streak_days", nextStreak)
            .putInt("best_streak", nextBest)
            .putString("last_watch_day", todayStr)
            .putStringSet("watch_days", nextDays)
            .apply()
        return StreakState(nextStreak, nextBest, todayStr, nextDays)
    }

    fun resetStreak(): StreakState {
        prefs.edit()
            .putInt("streak_days", 0)
            .putInt("best_streak", 0)
            .remove("last_watch_day")
            .putStringSet("watch_days", emptySet())
            .apply()
        return StreakState()
    }

    fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString("theme_mode", AppThemeMode.Light.name)
        return AppThemeMode.entries.firstOrNull { it.name == name } ?: AppThemeMode.Light
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun loadThemeStyle(): AppThemeStyle {
        val name = prefs.getString("theme_style", AppThemeStyle.NeoBrutalism.name)
        return AppThemeStyle.entries.firstOrNull { it.name == name } ?: AppThemeStyle.NeoBrutalism
    }

    fun saveThemeStyle(style: AppThemeStyle) {
        prefs.edit().putString("theme_style", style.name).apply()
    }

    fun loadCustomTheme(): CustomThemeConfig {
        return CustomThemeConfig(
            accentArgb = prefs.getLong("custom_accent", 0xFF3B82F6L),
            bgStartArgb = prefs.getLong("custom_bg_start", 0xFF0F172AL),
            bgEndArgb = prefs.getLong("custom_bg_end", 0xFF1E293BL),
            useGradient = prefs.getBoolean("custom_gradient", false),
        )
    }

    fun saveCustomAccent(argb: Long) {
        prefs.edit().putLong("custom_accent", argb).apply()
    }

    fun saveCustomBackground(startArgb: Long, endArgb: Long, useGradient: Boolean) {
        prefs.edit()
            .putLong("custom_bg_start", startArgb)
            .putLong("custom_bg_end", endArgb)
            .putBoolean("custom_gradient", useGradient)
            .apply()
    }

    fun loadReduceTransparency(): Boolean = prefs.getBoolean("reduce_transparency", false)
    fun saveReduceTransparency(v: Boolean) = prefs.edit().putBoolean("reduce_transparency", v).apply()

    fun loadBatteryAmoled(): Boolean = prefs.getBoolean("battery_amoled", true)
    fun saveBatteryAmoled(v: Boolean) = prefs.edit().putBoolean("battery_amoled", v).apply()

    fun loadGridView(): Boolean = prefs.getBoolean("grid_view", true)
    fun saveGridView(v: Boolean) = prefs.edit().putBoolean("grid_view", v).apply()

    fun loadLanguage(): AppLanguage {
        val tag = prefs.getString("language", "") ?: ""
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: AppLanguage.System
    }

    fun saveLanguage(lang: AppLanguage) = prefs.edit().putString("language", lang.tag).apply()

    fun loadTmdbKey(): String = secretPrefs.getString("tmdb_api_key", "") ?: ""
    fun saveTmdbKey(k: String) = secretPrefs.edit().putString("tmdb_api_key", k.trim()).apply()

    fun loadOmdbKey(): String = secretPrefs.getString("omdb_api_key", "") ?: ""
    fun saveOmdbKey(k: String) = secretPrefs.edit().putString("omdb_api_key", k.trim()).apply()

    fun loadLastSeenVersionCode(): Int = prefs.getInt("last_seen_version_code", 0)
    fun saveLastSeenVersionCode(code: Int) = prefs.edit().putInt("last_seen_version_code", code).apply()

    fun exportBackupJson(items: List<MediaItem>, history: List<WatchEvent>): String {
        val st = loadStreak()
        return JSONObject().apply {
            put("version", 4)
            put("appName", "Seriee")
            put("timestamp", System.currentTimeMillis())
            put("streakDays", st.streakDays)
            put("bestStreak", st.bestStreak)
            put("lastWatchDay", st.lastWatchDay ?: JSONObject.NULL)
            put("watchDays", JSONArray(st.watchDays.toList()))
            put("history", historyToJsonArray(WatchHistoryRules.trim(history)))
            put("items", itemsToJsonArray(items))
        }.toString(2)
    }

    data class ParsedBackup(
        val items: List<MediaItem>,
        val history: List<WatchEvent>,
        val streak: StreakState,
    )

    fun parseBackupJson(jsonText: String): ParsedBackup? {
        val trimmed = jsonText.trim()
        if (trimmed.isEmpty()) return null
        return runCatching {
            if (trimmed.startsWith("[")) {
                ParsedBackup(parseItemsArray(JSONArray(trimmed)), emptyList(), StreakState())
            } else {
                val obj = JSONObject(trimmed)
                val itemsArr = obj.optJSONArray("items") ?: return null
                val items = parseItemsArray(itemsArr)
                val historyArr = obj.optJSONArray("history")
                val history = if (historyArr != null) WatchHistoryRules.trim(parseHistoryArray(historyArr)) else emptyList()
                val streakDays = obj.optInt("streakDays", 0).coerceAtLeast(0)
                val bestStreak = obj.optInt("bestStreak", streakDays).coerceAtLeast(streakDays)
                val lastDay = obj.optString("lastWatchDay", "").ifBlank { null }
                val daysArr = obj.optJSONArray("watchDays")
                val watchDays = HashSet<String>()
                if (daysArr != null) {
                    for (i in 0 until daysArr.length()) {
                        val s = daysArr.optString(i, "")
                        if (s.isNotBlank()) watchDays.add(s)
                    }
                }
                ParsedBackup(items, history, StreakState(streakDays, bestStreak, lastDay, watchDays))
            }
        }.getOrNull()
    }

    fun restoreStreak(streak: StreakState) {
        prefs.edit()
            .putInt("streak_days", streak.streakDays)
            .putInt("best_streak", streak.bestStreak)
            .apply {
                if (streak.lastWatchDay != null) putString("last_watch_day", streak.lastWatchDay)
                else remove("last_watch_day")
            }
            .putStringSet("watch_days", streak.watchDays)
            .apply()
    }

    private fun itemsToJsonArray(items: List<MediaItem>): JSONArray {
        val arr = JSONArray()
        for (item in items) {
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("type", item.type.name)
                put("status", item.status.name)
                put("poster", item.poster)
                put("season", item.season)
                put("currentEp", item.currentEp)
                put("totalEp", item.totalEp)
                put("pinned", item.pinned)
                put("streamUrl", item.streamUrl)
                put("videoPath", item.videoPath)
                put("videoUri", item.videoUri)
                if (item.releaseDate != null) put("releaseDate", item.releaseDate)
                put("reminderEnabled", item.reminderEnabled)
                put("tags", JSONArray(item.tags))
                put("rating", item.rating)
                put("updatedAt", item.updatedAt)
                put("description", item.description)
                put("source", item.source)
                put("sourceId", item.sourceId)
                put("seasons", JSONArray().apply {
                    for (sec in item.seasons) {
                        put(JSONObject().apply {
                            put("number", sec.number)
                            put("currentEp", sec.currentEp)
                            put("totalEp", sec.totalEp)
                            put("episodes", JSONArray().apply {
                                for (ep in sec.episodes) {
                                    put(JSONObject().apply {
                                        put("number", ep.number)
                                        put("uri", ep.uri)
                                        put("displayName", ep.displayName)
                                    })
                                }
                            })
                        })
                    }
                })
            })
        }
        return arr
    }

    private fun parseItemsArray(arr: JSONArray): List<MediaItem> {
        val out = ArrayList<MediaItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val type = if (o.optString("type") == MediaType.Movie.name) MediaType.Movie else MediaType.Series
            val statusStr = o.optString("status", WatchStatus.Watching.name)
            val status = WatchStatus.entries.firstOrNull {
                it.name.equals(statusStr, ignoreCase = true) ||
                    it.name.equals(statusStr.replace(" ", ""), ignoreCase = true)
            } ?: WatchStatus.Watching
            val tags = ArrayList<String>()
            o.optJSONArray("tags")?.let { tArr ->
                for (j in 0 until tArr.length()) {
                    val t = tArr.optString(j, "").trim()
                    if (t.isNotEmpty()) tags.add(t)
                }
            }
            val seasons = ArrayList<Season>()
            o.optJSONArray("seasons")?.let { sArr ->
                for (j in 0 until sArr.length()) {
                    val sObj = sArr.optJSONObject(j) ?: continue
                    val eps = ArrayList<EpisodeFile>()
                    sObj.optJSONArray("episodes")?.let { eArr ->
                        for (k in 0 until eArr.length()) {
                            val eObj = eArr.optJSONObject(k) ?: continue
                            eps.add(
                                EpisodeFile(
                                    number = eObj.optInt("number", 1),
                                    uri = eObj.optString("uri", ""),
                                    displayName = eObj.optString("displayName", "video")
                                )
                            )
                        }
                    }
                    val tot = sObj.optInt("totalEp", 1).coerceAtLeast(1)
                    val cur = sObj.optInt("currentEp", 0).coerceIn(0, tot)
                    seasons.add(Season(sObj.optInt("number", j + 1), cur, tot, eps))
                }
            }
            out.add(
                MediaItem(
                    id = o.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                    title = o.optString("title", ""),
                    type = type,
                    status = status,
                    poster = o.optString("poster", ""),
                    season = o.optInt("season", 1).coerceAtLeast(1),
                    currentEp = o.optInt("currentEp", 0).coerceAtLeast(0),
                    totalEp = o.optInt("totalEp", 1).coerceAtLeast(1),
                    pinned = o.optBoolean("pinned", false),
                    streamUrl = o.optString("streamUrl", ""),
                    videoPath = o.optString("videoPath", ""),
                    videoUri = o.optString("videoUri", ""),
                    releaseDate = if (o.has("releaseDate") && !o.isNull("releaseDate")) o.optLong("releaseDate") else null,
                    reminderEnabled = o.optBoolean("reminderEnabled", false),
                    tags = tags,
                    rating = o.optInt("rating", 0).coerceIn(0, 5),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                    seasons = seasons,
                    description = o.optString("description", ""),
                    source = o.optString("source", ""),
                    sourceId = o.optString("sourceId", ""),
                )
            )
        }
        return out
    }

    private fun historyToJsonArray(events: List<WatchEvent>): JSONArray {
        val arr = JSONArray()
        for (ev in events) {
            arr.put(JSONObject().apply {
                put("itemId", ev.itemId)
                put("title", ev.title)
                put("type", ev.type.name)
                put("season", ev.season)
                put("episode", ev.episode)
                put("count", ev.count)
                put("watchedAt", ev.watchedAt)
            })
        }
        return arr
    }

    private fun parseHistoryArray(arr: JSONArray): List<WatchEvent> {
        val out = ArrayList<WatchEvent>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val count = o.optInt("count", 0)
            val watchedAt = o.optLong("watchedAt", 0L)
            if (count <= 0 || watchedAt <= 0L) continue
            val type = if (o.optString("type") == MediaType.Movie.name) MediaType.Movie else MediaType.Series
            out.add(
                WatchEvent(
                    itemId = o.optString("itemId", ""),
                    title = o.optString("title", ""),
                    type = type,
                    season = o.optInt("season", 1),
                    episode = o.optInt("episode", 1),
                    count = count,
                    watchedAt = watchedAt,
                )
            )
        }
        return out
    }
}
