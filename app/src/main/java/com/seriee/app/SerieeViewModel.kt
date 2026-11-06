package com.seriee.app

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.seriee.app.data.AppTheme
import com.seriee.app.data.MediaItem
import com.seriee.app.data.SerieeRepository
import com.seriee.app.data.WatchStatus

class SerieeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SerieeRepository(app)

    var items by mutableStateOf(repo.loadItems())
        private set
    private val _theme = mutableStateOf(repo.loadTheme())
    val theme: AppTheme get() = _theme.value
    private val _gridView = mutableStateOf(repo.loadGridView())
    val gridView: Boolean get() = _gridView.value

    private fun commit(newItems: List<MediaItem>) {
        items = newItems
        repo.saveItems(newItems)
    }

    init {
        com.seriee.app.reminder.ReminderScheduler.createNotificationChannel(app)
        // Refresh alarms for any pending items
        items.forEach { item ->
            if (item.reminderEnabled && item.releaseDate != null && item.releaseDate > System.currentTimeMillis()) {
                com.seriee.app.reminder.ReminderScheduler.scheduleReminder(app, item)
            }
        }
    }

    fun incrementEpisode(id: String) {
        commit(items.map { m ->
            if (m.id == id && m.currentEp < m.totalEp) {
                val next = m.currentEp + 1
                m.copy(
                    currentEp = next,
                    status = if (next >= m.totalEp) WatchStatus.Completed else m.status,
                    updatedAt = System.currentTimeMillis()
                )
            } else m
        })
    }

    fun togglePin(id: String) {
        commit(items.map { if (it.id == id) it.copy(pinned = !it.pinned) else it })
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
        com.seriee.app.reminder.ReminderScheduler.sendImmediateNotification(
            getApplication(),
            item.title,
            "Pengingat aktif untuk ${item.type.label} \"${item.title}\"!",
            item.id
        )
    }

    fun upsert(item: MediaItem) {
        val exists = items.any { it.id == item.id }
        commit(if (exists) items.map { if (it.id == item.id) item else it } else listOf(item) + items)

        if (item.reminderEnabled && item.releaseDate != null && item.releaseDate > System.currentTimeMillis()) {
            com.seriee.app.reminder.ReminderScheduler.scheduleReminder(getApplication(), item)
        } else {
            com.seriee.app.reminder.ReminderScheduler.cancelReminder(getApplication(), item.id)
        }
    }

    fun delete(id: String) {
        com.seriee.app.reminder.ReminderScheduler.cancelReminder(getApplication(), id)
        commit(items.filterNot { it.id == id })
    }

    fun setTheme(value: AppTheme) {
        _theme.value = value
        repo.saveTheme(value)
    }

    fun setGridView(value: Boolean) {
        _gridView.value = value
        repo.saveGridView(value)
    }

    fun exportBackup(context: Context, uri: Uri): Result<Int> {
        return runCatching {
            val jsonString = repo.exportBackupJson(items)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: error("Tidak bisa membuka output stream")
            items.size
        }
    }

    fun importBackup(context: Context, uri: Uri, replace: Boolean): Result<Int> {
        return runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Tidak bisa membaca file backup")
            val imported = repo.importBackupJson(raw)
            if (imported.isEmpty()) error("File cadangan kosong atau format tidak sesuai")

            val finalItems = if (replace) {
                imported
            } else {
                val importedIds = imported.map { it.id }.toSet()
                imported + items.filterNot { it.id in importedIds }
            }
            commit(finalItems)

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
