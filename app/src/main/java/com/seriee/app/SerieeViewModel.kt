package com.seriee.app

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriee.app.data.AniListListClient
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.AppThemeMode
import com.seriee.app.data.AppThemeStyle
import com.seriee.app.data.CustomThemeConfig
import com.seriee.app.data.ImportFetchErrorCode
import com.seriee.app.data.ImportFetchException
import com.seriee.app.data.ImportMapper
import com.seriee.app.data.ImportMode
import com.seriee.app.data.ImportSummary
import com.seriee.app.data.ImportedEntry
import com.seriee.app.data.MalXmlParser
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.PosterStore
import com.seriee.app.data.SerieeRepository
import com.seriee.app.data.StreakState
import com.seriee.app.data.ThemePreset
import com.seriee.app.data.WatchEvent
import com.seriee.app.data.WatchHistoryRules
import com.seriee.app.data.WatchStatus
import com.seriee.app.debug.AppLogger
import com.seriee.app.reminder.ReminderScheduler
import com.seriee.app.widget.SerieeWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SerieeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SerieeRepository(app)

    var items by mutableStateOf(repo.loadItems())
        private set

    var history by mutableStateOf(repo.loadHistory())
        private set

    var streak by mutableStateOf(repo.loadStreak())
        private set

    var themeMode by mutableStateOf(repo.loadThemeMode())
        private set

    var themeStyle by mutableStateOf(repo.loadThemeStyle())
        private set

    var customTheme by mutableStateOf(repo.loadCustomTheme())
        private set

    var reduceTransparency by mutableStateOf(repo.loadReduceTransparency())
        private set

    var batteryAmoled by mutableStateOf(repo.loadBatteryAmoled())
        private set

    var gridView by mutableStateOf(repo.loadGridView())
        private set

    var language by mutableStateOf(repo.loadLanguage())
        private set

    var tmdbKey by mutableStateOf(repo.loadTmdbKey())
        private set

    var omdbKey by mutableStateOf(repo.loadOmdbKey())
        private set

    var showChangelogAuto by mutableStateOf(repo.loadLastSeenVersionCode() < BuildConfig.VERSION_CODE)
        private set

    var showBackupReminder by mutableStateOf(repo.loadPendingBackupPrompt())
        private set

    init {
        ReminderScheduler.rescheduleAll(app, items)
    }

    fun dismissAutoChangelog() {
        repo.saveLastSeenVersionCode(BuildConfig.VERSION_CODE)
        showChangelogAuto = false
    }

    fun dismissBackupReminder() {
        repo.savePendingBackupPrompt(false)
        showBackupReminder = false
    }

    fun refreshFromStorage() {
        val storedItems = repo.loadItems()
        val storedHistory = repo.loadHistory()
        val storedStreak = repo.loadStreak()
        if (storedItems != items) items = storedItems
        if (storedHistory != history) history = storedHistory
        if (storedStreak != streak) streak = storedStreak
    }

    private fun maybeTriggerBackupReminder(itemCount: Int, importedDelta: Int = 0) {
        val sevenDaysMs = 7L * 86_400_000L
        val lastBackup = repo.loadLastBackupAt()
        val backupIsStale = lastBackup == 0L || (System.currentTimeMillis() - lastBackup) > sevenDaysMs
        if (backupIsStale && (importedDelta >= 3 || (itemCount >= 10 && itemCount % 10 == 0))) {
            repo.savePendingBackupPrompt(true)
            showBackupReminder = true
        }
    }

    private fun persistItems(next: List<MediaItem>) {
        items = next
        repo.saveItems(next)
        val ctx = getApplication<Application>()
        val activePosters = next.mapNotNull { it.poster.takeIf { p -> p.startsWith("/") } }.toSet()
        PosterStore.pruneUnused(ctx, activePosters)
        SerieeWidgetProvider.notifyChanged(ctx)
    }

    private fun logWatch(item: MediaItem, season: Int, ep: Int, delta: Int) {
        if (delta <= 0) return
        val now = System.currentTimeMillis()
        streak = repo.recordWatchToday()
        val next = WatchHistoryRules.append(
            history,
            WatchEvent(item.id, item.title, item.type, season, ep, delta, now),
            now
        )
        history = next
        repo.saveHistory(next)
    }

    fun upsertItem(item: MediaItem) {
        val idx = items.indexOfFirst { it.id == item.id }
        val next = if (idx >= 0) items.map { if (it.id == item.id) item else it } else listOf(item) + items
        persistItems(next)
        ReminderScheduler.schedule(getApplication(), item)
        if (idx < 0) {
            maybeTriggerBackupReminder(next.size)
        }
    }

    fun deleteItem(id: String) {
        ReminderScheduler.cancel(getApplication(), id)
        persistItems(items.filterNot { it.id == id })
    }

    fun togglePin(id: String) {
        persistItems(items.map { if (it.id == id) it.copy(pinned = !it.pinned, updatedAt = System.currentTimeMillis()) else it })
    }

    fun incrementEpisode(id: String) {
        val target = items.firstOrNull { it.id == id } ?: return
        val now = System.currentTimeMillis()
        if (target.type == MediaType.Movie) {
            if (target.currentEp >= 1) return
            val updated = target.copy(currentEp = 1, totalEp = 1, status = WatchStatus.Completed, updatedAt = now)
            persistItems(items.map { if (it.id == id) updated else it })
            logWatch(updated, 1, 1, 1)
            return
        }
        val seasons = target.effectiveSeasons.toMutableList()
        var incSeason = seasons.firstOrNull()?.number ?: 1
        var incEp = 1
        var bumped = false
        for (i in seasons.indices) {
            val sec = seasons[i]
            if (sec.currentEp < sec.totalEp) {
                val nextEp = sec.currentEp + 1
                seasons[i] = sec.copy(currentEp = nextEp)
                incSeason = sec.number
                incEp = nextEp
                bumped = true
                break
            }
        }
        if (!bumped) return
        val allDone = seasons.all { it.currentEp >= it.totalEp }
        val updated = target.copy(
            seasons = seasons,
            season = incSeason,
            currentEp = seasons.sumOf { it.currentEp },
            totalEp = seasons.sumOf { it.totalEp.coerceAtLeast(1) }.coerceAtLeast(1),
            status = if (allDone) WatchStatus.Completed else WatchStatus.Watching,
            updatedAt = now,
        )
        persistItems(items.map { if (it.id == id) updated else it })
        logWatch(updated, incSeason, incEp, 1)
    }

    fun setSeasonEpisode(id: String, seasonNum: Int, ep: Int) {
        val target = items.firstOrNull { it.id == id } ?: return
        val now = System.currentTimeMillis()
        if (target.type == MediaType.Movie) {
            val next = if (ep > 0) 1 else 0
            val delta = next - (if (target.currentEp > 0) 1 else 0)
            val updated = target.copy(
                currentEp = next,
                totalEp = 1,
                status = when {
                    next >= 1 -> WatchStatus.Completed
                    target.status == WatchStatus.Completed -> WatchStatus.Watching
                    else -> target.status
                },
                updatedAt = now,
            )
            persistItems(items.map { if (it.id == id) updated else it })
            if (delta > 0) logWatch(updated, 1, 1, delta)
            return
        }
        val prevSec = target.effectiveSeasons.firstOrNull { it.number == seasonNum }
        val seasons = target.effectiveSeasons.map { sec ->
            if (sec.number == seasonNum) sec.copy(currentEp = ep.coerceIn(0, sec.totalEp.coerceAtLeast(1)))
            else sec
        }
        val nextSec = seasons.firstOrNull { it.number == seasonNum }
        val delta = (nextSec?.currentEp ?: 0) - (prevSec?.currentEp ?: 0)
        val allDone = seasons.all { it.currentEp >= it.totalEp }
        val curSum = seasons.sumOf { it.currentEp }
        val updated = target.copy(
            seasons = seasons,
            season = seasonNum,
            currentEp = curSum,
            totalEp = seasons.sumOf { it.totalEp.coerceAtLeast(1) }.coerceAtLeast(1),
            status = when {
                allDone -> WatchStatus.Completed
                curSum > 0 && target.status == WatchStatus.PlanToWatch -> WatchStatus.Watching
                else -> target.status
            },
            updatedAt = now,
        )
        persistItems(items.map { if (it.id == id) updated else it })
        if (delta > 0 && nextSec != null) logWatch(updated, seasonNum, nextSec.currentEp, delta)
    }

    fun markSeasonComplete(id: String, seasonNum: Int) {
        val target = items.firstOrNull { it.id == id } ?: return
        val sec = target.effectiveSeasons.firstOrNull { it.number == seasonNum } ?: return
        setSeasonEpisode(id, seasonNum, sec.totalEp)
    }

    fun setStatus(id: String, status: WatchStatus) {
        persistItems(items.map { if (it.id == id) it.copy(status = status, updatedAt = System.currentTimeMillis()) else it })
    }

    fun setRating(id: String, rating: Int) {
        persistItems(items.map { if (it.id == id) it.copy(rating = rating.coerceIn(0, 5), updatedAt = System.currentTimeMillis()) else it })
    }

    fun toggleReminder(id: String) {
        val next = items.map {
            if (it.id == id) it.copy(reminderEnabled = !it.reminderEnabled, updatedAt = System.currentTimeMillis()) else it
        }
        persistItems(next)
        next.firstOrNull { it.id == id }?.let { ReminderScheduler.schedule(getApplication(), it) }
    }

    fun setReleaseDate(id: String, ms: Long?) {
        val next = items.map {
            if (it.id == id) it.copy(releaseDate = ms, reminderEnabled = ms != null, updatedAt = System.currentTimeMillis()) else it
        }
        persistItems(next)
        next.firstOrNull { it.id == id }?.let { ReminderScheduler.schedule(getApplication(), it) }
    }

    fun testReminderNow(item: MediaItem) {
        val ctx = getApplication<Application>()
        ReminderScheduler.triggerNow(ctx, item)
        Toast.makeText(
            ctx,
            ctx.getString(R.string.reminder_test_message, ctx.getString(item.type.labelRes), item.title),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun clearHistory() {
        repo.clearHistory()
        history = emptyList()
    }

    fun resetStreak() {
        streak = repo.resetStreak()
        val ctx = getApplication<Application>()
        Toast.makeText(ctx, ctx.getString(R.string.streak_reset_done), Toast.LENGTH_SHORT).show()
    }

    fun updateThemeMode(mode: AppThemeMode) {
        themeMode = mode
        repo.saveThemeMode(mode)
    }

    fun updateThemeStyle(style: AppThemeStyle) {
        themeStyle = style
        repo.saveThemeStyle(style)
    }

    fun updateCustomAccent(argb: Long) {
        customTheme = customTheme.copy(accentArgb = argb)
        repo.saveCustomAccent(argb)
    }

    fun updateCustomBackground(startArgb: Long, endArgb: Long, grad: Boolean) {
        customTheme = customTheme.copy(bgStartArgb = startArgb, bgEndArgb = endArgb, useGradient = grad)
        repo.saveCustomBackground(startArgb, endArgb, grad)
    }

    fun applyThemePreset(preset: ThemePreset) {
        updateThemeMode(preset.mode)
        updateThemeStyle(preset.style)
        updateCustomAccent(preset.accentArgb)
        updateCustomBackground(preset.bgStartArgb, preset.bgEndArgb, preset.useGradient)
    }

    fun resetThemeToDefault() {
        updateThemeMode(AppThemeMode.Light)
        updateThemeStyle(AppThemeStyle.NeoBrutalism)
        updateReduceTransparency(false)
        val ctx = getApplication<Application>()
        Toast.makeText(ctx, ctx.getString(R.string.crash_theme_reset_done), Toast.LENGTH_SHORT).show()
    }

    fun updateReduceTransparency(v: Boolean) {
        reduceTransparency = v
        repo.saveReduceTransparency(v)
    }

    fun updateBatteryAmoled(v: Boolean) {
        batteryAmoled = v
        repo.saveBatteryAmoled(v)
    }

    fun updateGridView(v: Boolean) {
        gridView = v
        repo.saveGridView(v)
    }

    fun updateLanguage(lang: AppLanguage) {
        language = lang
        repo.saveLanguage(lang)
        val locales = if (lang.tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(lang.tag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun saveTmdbKey(k: String) {
        tmdbKey = k.trim()
        repo.saveTmdbKey(tmdbKey)
    }

    fun saveOmdbKey(k: String) {
        omdbKey = k.trim()
        repo.saveOmdbKey(omdbKey)
    }

    fun exportBackupToUri(uri: Uri, onDone: (Result<Int>) -> Unit) {
        viewModelScope.launch(AppLogger.coroutineExceptionHandler("Backup")) {
            val ctx = getApplication<Application>()
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val json = repo.exportBackupJson(items, history)
                    ctx.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error(ctx.getString(R.string.error_open_output))
                    items.size
                }
            }
            result.onSuccess {
                repo.saveLastBackupAt(System.currentTimeMillis())
                repo.savePendingBackupPrompt(false)
                showBackupReminder = false
            }.onFailure { err ->
                AppLogger.e("Backup", "Failed to export backup", err)
            }
            onDone(result)
        }
    }

    fun restoreBackupFromUri(uri: Uri, merge: Boolean, onDone: (Result<Int>) -> Unit) {
        viewModelScope.launch(AppLogger.coroutineExceptionHandler("Restore")) {
            val ctx = getApplication<Application>()
            val parsedResult = withContext(Dispatchers.IO) {
                runCatching {
                    val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error(ctx.getString(R.string.error_read_backup))
                    repo.parseBackupJson(text) ?: error(ctx.getString(R.string.error_empty_backup))
                }
            }
            parsedResult.onSuccess { parsed ->
                if (merge) {
                    val existingIds = items.map { it.id }.toSet()
                    val mergedItems = items + parsed.items.filterNot { it.id in existingIds }
                    val mergedHistory = WatchHistoryRules.merge(history, parsed.history)
                    persistItems(mergedItems)
                    history = mergedHistory
                    repo.saveHistory(mergedHistory)
                    if (parsed.streak.streakDays > streak.streakDays || parsed.streak.bestStreak > streak.bestStreak) {
                        val mergedStreak = StreakState(
                            streakDays = maxOf(streak.streakDays, parsed.streak.streakDays),
                            bestStreak = maxOf(streak.bestStreak, parsed.streak.bestStreak),
                            lastWatchDay = parsed.streak.lastWatchDay ?: streak.lastWatchDay,
                            watchDays = (streak.watchDays + parsed.streak.watchDays).toList().takeLast(7).toSet()
                        )
                        streak = mergedStreak
                        repo.restoreStreak(mergedStreak)
                    }
                } else {
                    persistItems(parsed.items)
                    val trimmed = WatchHistoryRules.trim(parsed.history)
                    history = trimmed
                    repo.saveHistory(trimmed)
                    streak = parsed.streak
                    repo.restoreStreak(parsed.streak)
                }
                ReminderScheduler.rescheduleAll(ctx, items)
                onDone(Result.success(parsed.items.size))
            }.onFailure { err ->
                AppLogger.e("Restore", "Failed to restore backup", err)
                onDone(Result.failure(err))
            }
        }
    }

    fun fetchAniListEntries(username: String, onResult: (Result<List<ImportedEntry>>) -> Unit) {
        viewModelScope.launch(AppLogger.coroutineExceptionHandler("AniListImport")) {
            val ctx = getApplication<Application>()
            val res = runCatching {
                val rows = AniListListClient.fetch(username)
                ImportMapper.fromAniList(rows)
            }.recoverCatching { err ->
                AppLogger.e("AniListImport", "AniList fetch failed for $username", err)
                val msg = when ((err as? ImportFetchException)?.code) {
                    ImportFetchErrorCode.USER_NOT_FOUND -> ctx.getString(R.string.import_error_user_not_found)
                    ImportFetchErrorCode.PRIVATE_OR_EMPTY -> ctx.getString(R.string.import_error_private_or_empty)
                    ImportFetchErrorCode.RATE_LIMITED -> ctx.getString(R.string.import_error_rate_limited)
                    ImportFetchErrorCode.NETWORK -> ctx.getString(R.string.import_error_network)
                    ImportFetchErrorCode.PARSE -> ctx.getString(R.string.import_error_parse)
                    else -> ctx.getString(R.string.import_error_unknown)
                }
                error(msg)
            }
            onResult(res)
        }
    }

    fun parseMalUri(uri: Uri, onResult: (Result<List<ImportedEntry>>) -> Unit) {
        viewModelScope.launch(AppLogger.coroutineExceptionHandler("MalImport")) {
            val ctx = getApplication<Application>()
            val res = withContext(Dispatchers.IO) {
                runCatching {
                    val entries = ctx.contentResolver.openInputStream(uri)?.use { MalXmlParser.parseStream(it) }
                        ?: error(ctx.getString(R.string.import_error_file_invalid))
                    ImportMapper.fromMal(entries)
                }
            }
            res.onFailure { err ->
                AppLogger.e("MalImport", "MAL XML parse failed", err)
            }
            onResult(res)
        }
    }

    fun applyImport(entries: List<ImportedEntry>, mode: ImportMode): ImportSummary {
        val (nextItems, summary) = when (mode) {
            ImportMode.MERGE -> ImportMapper.merge(items, entries)
            ImportMode.REPLACE -> ImportMapper.replace(entries)
        }
        persistItems(nextItems)
        ReminderScheduler.rescheduleAll(getApplication(), nextItems)
        maybeTriggerBackupReminder(nextItems.size, importedDelta = summary.added + summary.updated)
        return summary
    }
}
