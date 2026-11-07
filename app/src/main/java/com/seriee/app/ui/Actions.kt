package com.seriee.app.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.seriee.app.R
import com.seriee.app.data.MediaItem

const val VLC_PACKAGE = "org.videolan.vlc"
const val MX_PACKAGE = "com.mxtech.videoplayer.ad"

fun openUrl(context: Context, url: String) {
    val fixed = if (url.contains("://")) url else "https://$url"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixed)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.cannot_open_link), Toast.LENGTH_SHORT).show()
    }
}

/** Opens the picked offline video in an external player. [pkg] = null shows the system chooser. */
fun openVideo(context: Context, item: MediaItem, pkg: String?) {
    // Prefer per-episode file if available, else legacy single file
    val uriStr = item.effectiveSeasons
        .flatMap { it.episodes }
        .firstOrNull { it.number == item.currentEp }?.uri
        ?: item.videoUri
    if (uriStr.isBlank()) {
        Toast.makeText(
            context,
            context.getString(R.string.pick_file_first),
            Toast.LENGTH_LONG
        ).show()
        return
    }
    openVideoUri(context, uriStr, pkg)
}

fun openVideoUri(context: Context, uriString: String, pkg: String?) {
    if (uriString.isBlank()) {
        Toast.makeText(context, context.getString(R.string.pick_file_first), Toast.LENGTH_LONG).show()
        return
    }
    val uri = Uri.parse(uriString)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*")
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (pkg != null) setPackage(pkg)
    }
    try {
        if (pkg == null) {
            val chooser = Intent.createChooser(intent, context.getString(R.string.open_with)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
            context.startActivity(intent)
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.player_not_installed), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.failed_open_video), Toast.LENGTH_SHORT).show()
    }
}

/** Auto-detect (season, episode) from a filename. Null if not detectable. */
object EpisodeFileParser {
    private val sxe = Regex("""S(\d+)[^E0-9]*E(\d+)""", RegexOption.IGNORE_CASE)
    private val epRegex = Regex("""E(?:P|PISODE)?\s*0*(\d+)""", RegexOption.IGNORE_CASE)
    private val trailing = Regex("""[-_\s]0*(\d{1,3})\.[A-Za-z0-9]+${'$'}""")

    fun parseSeasonEpisode(filename: String): Pair<Int, Int>? {
        sxe.find(filename)?.let { m ->
            val s = m.groupValues[1].toIntOrNull() ?: 1
            val e = m.groupValues[2].toIntOrNull() ?: return@let null
            return s to e
        }
        return null
    }

    fun parseEpisode(filename: String): Int? {
        epRegex.find(filename)?.let { return it.groupValues[1].toIntOrNull() }
        trailing.find(filename)?.let { return it.groupValues[1].toIntOrNull() }
        return null
    }
}

fun displayName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }
}

fun relativeTime(context: Context, ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> context.getString(R.string.time_just_now)
        minutes < 60 -> context.getString(R.string.time_minutes_ago, minutes)
        hours < 24 -> context.getString(R.string.time_hours_ago, hours)
        days < 2 -> context.getString(R.string.time_yesterday)
        days < 7 -> context.getString(R.string.time_days_ago, days)
        days < 30 -> context.getString(R.string.time_weeks_ago, days / 7)
        else -> context.getString(R.string.time_months_ago, days / 30)
    }
}
