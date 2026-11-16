package com.seriee.app.data

import android.content.Context
import android.net.Uri
import com.seriee.app.debug.AppLogger
import java.io.File

object PosterStore {
    fun savePosterFromUri(context: Context, uri: Uri, itemId: String): String? {
        return runCatching {
            val dir = File(context.filesDir, "posters").apply { mkdirs() }
            val ext = context.contentResolver.getType(uri)?.substringAfter('/') ?: "jpg"
            val dest = File(dir, "${itemId}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            dest.absolutePath
        }.onFailure { err ->
            AppLogger.w("PosterStore", "Failed to save poster from URI", err)
        }.getOrNull()
    }

    fun pruneUnused(context: Context, activePosters: Set<String>) {
        runCatching {
            val dir = File(context.filesDir, "posters")
            if (!dir.exists() || !dir.isDirectory) return
            val files = dir.listFiles() ?: return
            for (f in files) {
                if (f.isFile && f.absolutePath !in activePosters) {
                    f.delete()
                }
            }
        }.onFailure { err ->
            AppLogger.w("PosterStore", "Failed to prune unused posters", err)
        }
    }
}
