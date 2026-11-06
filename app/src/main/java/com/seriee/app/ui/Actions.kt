package com.seriee.app.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.seriee.app.data.MediaItem

const val VLC_PACKAGE = "org.videolan.vlc"
const val MX_PACKAGE = "com.mxtech.videoplayer.ad"

fun openUrl(context: Context, url: String) {
    val fixed = if (url.contains("://")) url else "https://$url"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixed)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak bisa membuka link", Toast.LENGTH_SHORT).show()
    }
}

/** Opens the picked offline video in an external player. [pkg] = null shows the system chooser. */
fun openVideo(context: Context, item: MediaItem, pkg: String?) {
    if (item.videoUri.isBlank()) {
        Toast.makeText(
            context,
            "Pilih file lewat tombol \"Pilih File\" (Edit) agar bisa dibuka di player",
            Toast.LENGTH_LONG
        ).show()
        return
    }
    val uri = Uri.parse(item.videoUri)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*")
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (pkg != null) setPackage(pkg)
    }
    try {
        if (pkg == null) {
            val chooser = Intent.createChooser(intent, "Buka dengan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
            context.startActivity(intent)
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Player tidak terpasang", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka video", Toast.LENGTH_SHORT).show()
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

fun relativeTime(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Baru saja"
        minutes < 60 -> "$minutes menit lalu"
        hours < 24 -> "$hours jam lalu"
        days < 2 -> "Kemarin"
        days < 7 -> "$days hari lalu"
        days < 30 -> "${days / 7} minggu lalu"
        else -> "${days / 30} bulan lalu"
    }
}
