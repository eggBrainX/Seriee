package com.seriee.app.debug

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.seriee.app.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    CRASH,
}

data class LogEntry(
    val timestampMs: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String = "",
)

data class CrashReport(
    val timestampMs: Long,
    val threadName: String,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
    val consecutiveCount: Int = 1,
)

object AppLogger {
    private const val PREFS_NAME = "seriee_debug_logs"
    private const val KEY_LOGS = "logs_json"
    private const val KEY_PENDING_CRASH = "pending_crash_json"
    private const val KEY_CRASH_STREAK = "consecutive_crashes"
    private const val MAX_ENTRIES = 150

    private val lock = Any()
    private var appContext: Context? = null

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _pendingCrash = MutableStateFlow<CrashReport?>(null)
    val pendingCrash: StateFlow<CrashReport?> = _pendingCrash.asStateFlow()

    fun init(context: Context) {
        val ctx = context.applicationContext
        synchronized(lock) {
            appContext = ctx
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedLogs = prefs.getString(KEY_LOGS, null)
            if (!savedLogs.isNullOrBlank()) {
                _logs.value = parseLogs(savedLogs)
            }
            val savedCrash = prefs.getString(KEY_PENDING_CRASH, null)
            if (!savedCrash.isNullOrBlank()) {
                _pendingCrash.value = parseCrash(savedCrash)
            }
        }
    }

    fun coroutineExceptionHandler(tag: String): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            recordCrash("$tag/coroutine", throwable, commitSync = false)
        }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        append(LogLevel.INFO, tag, message, null)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        append(LogLevel.WARN, tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        append(LogLevel.ERROR, tag, message, throwable)
    }

    fun recordCrash(threadName: String, throwable: Throwable, commitSync: Boolean = false): CrashReport {
        val stack = stackTraceString(throwable)
        val type = throwable::class.java.simpleName.ifBlank { "Exception" }
        val msg = throwable.message?.takeIf { it.isNotBlank() } ?: type
        Log.e("UncaughtException", "Crash on thread=$threadName: $msg", throwable)
        synchronized(lock) {
            val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val count = (prefs?.getInt(KEY_CRASH_STREAK, 0) ?: 0) + 1
            val report = CrashReport(
                timestampMs = System.currentTimeMillis(),
                threadName = threadName,
                exceptionType = type,
                message = msg,
                stackTrace = stack,
                consecutiveCount = count,
            )
            val entry = LogEntry(
                timestampMs = report.timestampMs,
                level = LogLevel.CRASH,
                tag = "Crash[$threadName]",
                message = "$type: $msg",
                stackTrace = stack,
            )
            val next = (listOf(entry) + _logs.value).take(MAX_ENTRIES)
            _logs.value = next
            _pendingCrash.value = report
            persistLocked(next, report, count, commitSync)
            return report
        }
    }

    fun dismissPendingCrash() {
        synchronized(lock) {
            _pendingCrash.value = null
            appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ?.edit()
                ?.remove(KEY_PENDING_CRASH)
                ?.putInt(KEY_CRASH_STREAK, 0)
                ?.apply()
        }
    }

    fun clearLogs() {
        synchronized(lock) {
            _logs.value = emptyList()
            appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ?.edit()
                ?.remove(KEY_LOGS)
                ?.apply()
        }
    }

    fun formatAllLogs(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        return _logs.value.joinToString("\n\n") { entry ->
            buildString {
                append("[${fmt.format(Date(entry.timestampMs))}] ${entry.level.name}/${entry.tag}: ${entry.message}")
                if (entry.stackTrace.isNotBlank()) {
                    append('\n')
                    append(entry.stackTrace.trimEnd())
                }
            }
        }
    }

    fun writeShareableLogFile(context: Context): File? = runCatching {
        val dir = File(context.filesDir, "logs").apply { mkdirs() }
        val file = File(dir, "seriee-debug-logs.txt")
        val content = formatAllLogs().ifBlank { "No log entries recorded." }
        file.writeText(content, Charsets.UTF_8)
        file
    }.getOrNull()

    fun shareLogs(context: Context) {
        val file = writeShareableLogFile(context)
        if (file == null) {
            Toast.makeText(context, context.getString(R.string.debug_share_failed), Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.debug_share_title))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.debug_share_title)))
        }.onFailure { err ->
            e("AppLogger", "Failed to share log file", err)
            Toast.makeText(context, context.getString(R.string.debug_share_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun append(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val stack = if (throwable != null) stackTraceString(throwable) else ""
        synchronized(lock) {
            val entry = LogEntry(
                timestampMs = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message,
                stackTrace = stack,
            )
            val next = (listOf(entry) + _logs.value).take(MAX_ENTRIES)
            _logs.value = next
            val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val streak = prefs?.getInt(KEY_CRASH_STREAK, 0) ?: 0
            persistLocked(next, _pendingCrash.value, streak, commitSync = false)
        }
    }

    private fun persistLocked(logs: List<LogEntry>, crash: CrashReport?, crashStreak: Int, commitSync: Boolean) {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val editor = prefs.edit()
            .putString(KEY_LOGS, serializeLogs(logs))
            .putInt(KEY_CRASH_STREAK, crashStreak)
        if (crash != null) {
            editor.putString(KEY_PENDING_CRASH, serializeCrash(crash))
        }
        if (commitSync) editor.commit() else editor.apply()
    }

    fun stackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    private fun serializeLogs(entries: List<LogEntry>): String {
        val arr = JSONArray()
        for (e in entries) {
            arr.put(
                JSONObject().apply {
                    put("ts", e.timestampMs)
                    put("lvl", e.level.name)
                    put("tag", e.tag)
                    put("msg", e.message)
                    if (e.stackTrace.isNotBlank()) put("st", e.stackTrace)
                }
            )
        }
        return arr.toString()
    }

    private fun parseLogs(raw: String): List<LogEntry> = runCatching {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val lvl = runCatching { LogLevel.valueOf(o.optString("lvl", "INFO")) }.getOrDefault(LogLevel.INFO)
            LogEntry(
                timestampMs = o.optLong("ts", 0L),
                level = lvl,
                tag = o.optString("tag", ""),
                message = o.optString("msg", ""),
                stackTrace = o.optString("st", ""),
            )
        }
    }.getOrDefault(emptyList())

    private fun serializeCrash(c: CrashReport): String = JSONObject().apply {
        put("ts", c.timestampMs)
        put("thread", c.threadName)
        put("type", c.exceptionType)
        put("msg", c.message)
        put("st", c.stackTrace)
        put("streak", c.consecutiveCount)
    }.toString()

    private fun parseCrash(raw: String): CrashReport? = runCatching {
        val o = JSONObject(raw)
        CrashReport(
            timestampMs = o.optLong("ts", System.currentTimeMillis()),
            threadName = o.optString("thread", "main"),
            exceptionType = o.optString("type", "Exception"),
            message = o.optString("msg", ""),
            stackTrace = o.optString("st", ""),
            consecutiveCount = o.optInt("streak", 1),
        )
    }.getOrNull()
}
