package com.seriee.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.seriee.app.R
import com.seriee.app.data.MediaItem
import com.seriee.app.debug.AppLogger

fun openUrl(context: Context, url: String) {
    val clean = url.trim()
    if (clean.isEmpty()) return
    val normalized = if (clean.startsWith("http://") || clean.startsWith("https://")) clean else "https://$clean"
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    }.onFailure { err ->
        AppLogger.e("Actions", "Failed to open URL: $normalized", err)
        Toast.makeText(context, context.getString(R.string.cannot_open_link), Toast.LENGTH_SHORT).show()
    }
}

fun openVideo(context: Context, item: MediaItem, pkg: String? = null) {
    val uriStr = item.videoUri.ifBlank {
        item.seasons.firstOrNull { it.episodes.isNotEmpty() }?.episodes?.firstOrNull()?.uri ?: ""
    }
    if (uriStr.isBlank()) {
        Toast.makeText(context, context.getString(R.string.pick_file_first), Toast.LENGTH_LONG).show()
        return
    }
    openVideoUri(context, uriStr, pkg)
}

fun openVideoUri(context: Context, uriStr: String, pkg: String? = null) {
    if (uriStr.isBlank()) {
        Toast.makeText(context, context.getString(R.string.pick_file_first), Toast.LENGTH_LONG).show()
        return
    }
    val uri = Uri.parse(uriStr)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (!pkg.isNullOrBlank()) setPackage(pkg)
    }
    runCatching {
        if (pkg.isNullOrBlank()) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_with)))
        } else {
            context.startActivity(intent)
        }
    }.onFailure { err ->
        AppLogger.e("Actions", "Failed to open video URI: $uriStr (pkg=$pkg)", err)
        val msg = if (!pkg.isNullOrBlank()) context.getString(R.string.player_not_installed)
        else context.getString(R.string.failed_open_video)
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}

fun displayName(context: Context, uri: Uri): String {
    var name: String? = null
    runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx)
            }
        }
    }
    return name ?: uri.lastPathSegment?.substringAfterLast('/') ?: "video"
}

object EpisodeFileParser {
    private val patterns = listOf(
        Regex("""[Ss]\d{1,2}[ ._-]?[Ee](\d{1,3})"""),
        Regex("""\b\d{1,2}x(\d{1,3})\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:EP?|Episode)[ ._-]?(\d{1,3})\b""", RegexOption.IGNORE_CASE),
        Regex("""[-–_ ]+(\d{1,3})(?:\s*\.\w{2,5}$|\s*[\[(])"""),
        Regex("""\b(\d{1,3})\b"""),
    )

    fun extractEpisodeNumber(fileName: String): Int? {
        val base = fileName.substringBeforeLast('.')
        for (i in 0 until patterns.size - 1) {
            val m = patterns[i].find(fileName) ?: continue
            val n = m.groupValues.getOrNull(1)?.toIntOrNull()
            if (n != null && n in 1..999) return n
        }
        val clean = base.replace(Regex("""\b(480|720|1080|2160)p?\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""x?26[45]""", RegexOption.IGNORE_CASE), "")
        val matches = patterns.last().findAll(clean).toList()
        val last = matches.lastOrNull()?.groupValues?.getOrNull(1)?.toIntOrNull()
        return if (last != null && last in 1..999) last else null
    }
}

fun relativeTime(context: Context, ms: Long): String {
    val diff = (System.currentTimeMillis() - ms).coerceAtLeast(0L)
    val min = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L
    return when {
        min < 1 -> context.getString(R.string.time_just_now)
        min < 60 -> context.getString(R.string.time_minutes_ago, min.toInt())
        hours < 24 -> context.getString(R.string.time_hours_ago, hours.toInt())
        days == 1L -> context.getString(R.string.time_yesterday)
        days < 7 -> context.getString(R.string.time_days_ago, days.toInt())
        days < 30 -> context.getString(R.string.time_weeks_ago, (days / 7).toInt().coerceAtLeast(1))
        else -> context.getString(R.string.time_months_ago, (days / 30).toInt().coerceAtLeast(1))
    }
}
