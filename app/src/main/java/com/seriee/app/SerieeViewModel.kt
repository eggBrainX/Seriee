package com.seriee.app

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seriee.app.R
import com.seriee.app.data.AniListListClient
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.AppTheme
import com.seriee.app.data.ImportError
import com.seriee.app.data.ImportException
import com.seriee.app.data.ImportMapper
import com.seriee.app.data.ImportMode
import com.seriee.app.data.ImportPhase
import com.seriee.app.data.ImportSource
import com.seriee.app.data.ImportUiState
import com.seriee.app.data.MalXmlParser
import com.seriee.app.data.MediaItem
import com.seriee.app.data.SerieeRepository
import com.seriee.app.data.WatchStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SerieeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SerieeRepository(app)

    var items by mutableStateOf(repo.loadItems())
        private set
    private val _theme = mutableStateOf(repo.loadTheme())
    val theme: AppTheme get() = _theme.value
    private val _customAccent = mutableStateOf(Color(repo.loadCustomAccent()))
    val customAccent: Color get() = _customAccent.value
    private val _customGradient = mutableStateOf(repo.loadCustomGradient())
    val customGradient: Boolean get() = _customGradient.value
    private val _gridView = mutableStateOf(repo.loadGridView())
    val gridView: Boolean get() = _gridView.value
    private val _language = mutableStateOf(repo.loadLanguage())
    val language: AppLanguage get() = _language.value
    private val _tmdbKey = mutableStateOf(repo.loadTmdbKey())
    val tmdbKey: String get() = _tmdbKey.value
    private val _omdbKey = mutableStateOf(repo.loadOmdbKey())
    val omdbKey: String get() = _omdbKey.value

    private val _streakDays = mutableStateOf(repo.loadStreakDays())
    val streakDays: Int get() = _streakDays.value
    private val _bestStreak = mutableStateOf(repo.loadBestStreak())
    val bestStreak: Int get() = _bestStreak.value
    private val _watchDays = mutableStateOf(repo.loadWatchDays())
    val watchDays: Set<String> get() = _watchDays.value

    private val _showChangelog = mutableStateOf(false)
    val showChangelog: Boolean get() = _showChangelog.value

    private val aniListClient = AniListListClient()
    private var importJob: Job? = null
    private val _importState = mutableStateOf(ImportUiState())
    val importState: ImportUiState get() = _importState.value

    // Single source of truth: versionCode/versionName in app/build.gradle.kts,
    // generated into BuildConfig. No hardcoded fallbacks here.
    val currentVersionCode: Int = BuildConfig.VERSION_CODE
    val currentVersionName: String = BuildConfig.VERSION_NAME

    private fun commit(newItems: List<MediaItem>) {
        items = newItems
        repo.saveItems(newItems)
    }

    init {
        com.seriee.app.reminder.ReminderScheduler.createNotificationChannel(app)
        // Show changelog once per version upgrade
        val lastSeen = repo.loadLastSeenVersionCode()
        if (lastSeen < currentVersionCode) {
            _showChangelog.value = true
        }
        // Refresh alarms for any pending items
        items.forEach { item ->
            if (item.reminderEnabled && item.releaseDate != null && item.releaseDate > System.currentTimeMillis()) {
                com.seriee.app.reminder.ReminderScheduler.scheduleReminder(app, item)
            }
        }
    }

    fun incrementEpisode(id: String) {
        val before = items.sumOf { it.watchedEpisodes }
        commit(items.map { m ->
            if (m.id != id) return@map m
            // Multi-season path
            if (m.seasons.isNotEmpty()) {
                val idx = m.seasons.indexOfFirst { it.currentEp < it.totalEp }
                val newSeasons = if (idx < 0) m.seasons else m.seasons.mapIndexed { i, s ->
                    if (i == idx) s.copy(currentEp = (s.currentEp + 1).coerceAtMost(s.totalEp)) else s
                }
                val watched = newSeasons.sumOf { it.currentEp }
                val total = newSeasons.sumOf { it.totalEp }
                val activeSeason = newSeasons.firstOrNull { it.currentEp < it.totalEp } ?: newSeasons.lastOrNull()
                m.copy(
                    seasons = newSeasons,
                    season = activeSeason?.number ?: m.season,
                    currentEp = watched,
                    totalEp = total.coerceAtLeast(1),
                    status = if (watched >= total) WatchStatus.Completed else m.status,
                    updatedAt = System.currentTimeMillis()
                )
            } else if (m.currentEp < m.totalEp) {
                val next = m.currentEp + 1
                m.copy(
                    currentEp = next,
                    status = if (next >= m.totalEp) WatchStatus.Completed else m.status,
                    updatedAt = System.currentTimeMillis()
                )
            } else m
        })
        if (items.sumOf { it.watchedEpisodes } > before) recordWatch()
    }

    fun setCurrentEpisode(id: String, seasonNo: Int, epNo: Int) {
        val before = items.sumOf { it.watchedEpisodes }
        commit(items.map { m ->
            if (m.id != id) return@map m
            if (m.seasons.isNotEmpty()) {
                val newSeasons = m.seasons.map { s ->
                    if (s.number == seasonNo) s.copy(currentEp = epNo.coerceIn(0, s.totalEp)) else s
                }
                val watched = newSeasons.sumOf { it.currentEp }
                val total = newSeasons.sumOf { it.totalEp }
                m.copy(
                    seasons = newSeasons,
                    season = seasonNo,
                    currentEp = watched,
                    totalEp = total.coerceAtLeast(1),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                m.copy(
                    currentEp = epNo.coerceIn(0, m.totalEp),
                    updatedAt = System.currentTimeMillis()
                )
            }
        })
        if (items.sumOf { it.watchedEpisodes } > before) recordWatch()
    }

    fun togglePin(id: String) {
        commit(items.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
    }

    /**
     * Daily streak. Watch activity is bucketed by local calendar day:
     * same day = no change, yesterday = +1, gap = reset to 1.
     */
    private fun recordWatch() {
        val today = java.time.LocalDate.now()
        val todayStr = today.toString()
        val last = runCatching { java.time.LocalDate.parse(repo.loadLastWatchDay()) }.getOrNull()
        val newStreak = when (last) {
            null -> 1
            today -> _streakDays.value
            today.minusDays(1) -> _streakDays.value + 1
            else -> 1
        }
        val newBest = maxOf(_bestStreak.value, newStreak)
        val cutoff = today.minusDays(6)
        val newDays = (_watchDays.value + todayStr)
            .filter { runCatching { java.time.LocalDate.parse(it) >= cutoff }.getOrDefault(false) }
            .toSet()
        _streakDays.value = newStreak
        _bestStreak.value = newBest
        _watchDays.value = newDays
        repo.saveStreak(newStreak, newBest, todayStr, newDays)
    }

    fun resetStreak() {
        _streakDays.value = 0
        _bestStreak.value = 0
        _watchDays.value = emptySet()
        repo.saveStreak(0, 0, "", emptySet())
    }

    fun toggleReminder(id: String) {
        commit(items.map { m ->
            if (m.id == id) {
                val newStatus = !m.reminderEnabled
                val updated = m.copy(reminderEnabled = newStatus)
                if (newStatus && updated.releaseDate != null && updated.releaseDate > System.currentTimeMillis()) {
                    com.seriee.app.reminder.ReminderScheduler.scheduleReminder(getApplication(), updated)
                } else {
                    com.seriee.app.reminder.ReminderScheduler.cancelReminder(getApplication(), id)
                }
                updated
            } else m
        })
    }

    fun testReminder(item: MediaItem) {
        val app = getApplication<Application>()
        com.seriee.app.reminder.ReminderScheduler.sendImmediateNotification(
            app,
            item.title,
            app.getString(
                R.string.reminder_test_message,
                app.getString(item.type.labelRes),
                item.title
            ),
            item.id
        )
    }

    fun upsert(item: MediaItem) {
        val synced = if (item.seasons.isNotEmpty()) {
            val watched = item.seasons.sumOf { it.currentEp.coerceIn(0, it.totalEp) }
            val total = item.seasons.sumOf { it.totalEp.coerceAtLeast(1) }
            val active = item.seasons.firstOrNull { it.currentEp < it.totalEp } ?: item.seasons.lastOrNull()
            item.copy(season = active?.number ?: item.season, currentEp = watched, totalEp = total.coerceAtLeast(1))
        } else item
        val exists = items.any { it.id == synced.id }
        commit(if (exists) items.map { if (it.id == synced.id) synced else it } else listOf(synced) + items)

        if (synced.reminderEnabled && synced.releaseDate != null && synced.releaseDate > System.currentTimeMillis()) {
            com.seriee.app.reminder.ReminderScheduler.scheduleReminder(getApplication(), synced)
        } else {
            com.seriee.app.reminder.ReminderScheduler.cancelReminder(getApplication(), synced.id)
        }
    }

    fun delete(id: String) {
        com.seriee.app.reminder.ReminderScheduler.cancelReminder(getApplication(), id)
        items.find { it.id == id }?.poster?.let {
            com.seriee.app.data.PosterStore.deleteIfLocal(getApplication(), it)
        }
        commit(items.filterNot { it.id == id })
    }

    fun setTmdbKey(value: String) {
        _tmdbKey.value = value.trim()
        repo.saveTmdbKey(value.trim())
    }

    fun setOmdbKey(value: String) {
        _omdbKey.value = value.trim()
        repo.saveOmdbKey(value.trim())
    }

    fun openChangelog() {
        _showChangelog.value = true
    }

    fun dismissChangelog() {
        _showChangelog.value = false
        repo.saveLastSeenVersionCode(currentVersionCode)
    }

    fun setTheme(value: AppTheme) {
        _theme.value = value
        repo.saveTheme(value)
    }

    fun setCustomAccent(value: Color) {
        _customAccent.value = value
        repo.saveCustomAccent(value.toArgb())
        if (_theme.value != AppTheme.Custom) setTheme(AppTheme.Custom)
    }

    fun setCustomGradient(value: Boolean) {
        _customGradient.value = value
        repo.saveCustomGradient(value)
        if (_theme.value != AppTheme.Custom) setTheme(AppTheme.Custom)
    }

    fun setGridView(value: Boolean) {
        _gridView.value = value
        repo.saveGridView(value)
    }

    fun setLanguage(value: AppLanguage) {
        if (_language.value == value) return
        _language.value = value
        repo.saveLanguage(value)
        val locales = if (value.tag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(value.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    // ------------------------------------------------------------- import UX

    fun beginImport() {
        importJob?.cancel()
        val current = _importState.value
        _importState.value = ImportUiState(source = current.source, mode = current.mode)
    }

    fun setImportSource(source: ImportSource) {
        importJob?.cancel()
        _importState.value = _importState.value.copy(
            source = source,
            phase = ImportPhase.Idle,
            fileName = "",
            preview = null,
            result = null,
            error = null
        )
    }

    fun setImportMode(mode: ImportMode) {
        _importState.value = _importState.value.copy(mode = mode)
    }

    fun fetchAniList(username: String) {
        importJob?.cancel()
        _importState.value = _importState.value.copy(
            phase = ImportPhase.Loading,
            preview = null,
            result = null,
            error = null
        )
        importJob = viewModelScope.launch {
            try {
                val rows = aniListClient.fetch(username)
                val mapped = ImportMapper.fromAniList(rows)
                if (mapped.isEmpty()) throw ImportException(ImportError.ListPrivateOrEmpty)
                _importState.value = _importState.value.copy(
                    phase = ImportPhase.Preview,
                    preview = ImportMapper.preview(mapped, items)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: ImportException) {
                _importState.value = _importState.value.copy(phase = ImportPhase.Error, error = e.error)
            } catch (e: Exception) {
                _importState.value = _importState.value.copy(phase = ImportPhase.Error, error = ImportError.Unknown)
            }
        }
    }

    fun loadMalFile(context: Context, uri: Uri, displayName: String) {
        importJob?.cancel()
        _importState.value = _importState.value.copy(
            phase = ImportPhase.Loading,
            fileName = displayName,
            preview = null,
            result = null,
            error = null
        )
        importJob = viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw ImportException(ImportError.FileInvalid)
                    ImportMapper.fromMal(MalXmlParser.parseBytes(bytes))
                }
                if (result.isEmpty()) throw ImportException(ImportError.NoEntries)
                _importState.value = _importState.value.copy(
                    phase = ImportPhase.Preview,
                    preview = ImportMapper.preview(result, items)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: ImportException) {
                _importState.value = _importState.value.copy(phase = ImportPhase.Error, error = e.error)
            } catch (e: Exception) {
                _importState.value = _importState.value.copy(phase = ImportPhase.Error, error = ImportError.FileInvalid)
            }
        }
    }

    fun applyImport() {
        val preview = _importState.value.preview ?: return
        val result = if (_importState.value.mode == ImportMode.Replace) {
            // Replace drops the old library, so clear its pending alarms first.
            items.forEach { old ->
                if (old.reminderEnabled) {
                    com.seriee.app.reminder.ReminderScheduler.cancelReminder(getApplication(), old.id)
                }
            }
            val (finalItems, result) = ImportMapper.replace(preview.entries)
            commit(finalItems)
            result
        } else {
            val (finalItems, result) = ImportMapper.merge(preview.entries, items)
            commit(finalItems)
            result
        }
        _importState.value = _importState.value.copy(
            phase = ImportPhase.Done,
            preview = null,
            result = result
        )
    }

    fun dismissImport() {
        importJob?.cancel()
        _importState.value = ImportUiState(source = _importState.value.source, mode = _importState.value.mode)
    }

    fun exportBackup(context: Context, uri: Uri): Result<Int> {
        return runCatching {
            val jsonString = repo.exportBackupJson(items)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: error(getApplication<Application>().getString(R.string.error_open_output))
            items.size
        }
    }

    fun importBackup(context: Context, uri: Uri, replace: Boolean): Result<Int> {
        return runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: error(getApplication<Application>().getString(R.string.error_read_backup))
            val imported = repo.importBackupJson(raw)
            if (imported.isEmpty()) error(getApplication<Application>().getString(R.string.error_empty_backup))

            val finalItems = if (replace) {
                imported
            } else {
                val importedIds = imported.map { it.id }.toSet()
                imported + items.filterNot { it.id in importedIds }
            }
            commit(finalItems)
            // Import may have restored streak history (v3 backups).
            _streakDays.value = repo.loadStreakDays()
            _bestStreak.value = repo.loadBestStreak()
            _watchDays.value = repo.loadWatchDays()

            // Reschedule active reminders
            finalItems.forEach { item ->
                if (item.reminderEnabled && item.releaseDate != null && item.releaseDate > System.currentTimeMillis()) {
                    com.seriee.app.reminder.ReminderScheduler.scheduleReminder(getApplication(), item)
                }
            }
            imported.size
        }
    }
}
