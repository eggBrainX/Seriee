package com.seriee.app.data

import android.content.Context
import android.net.Uri
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
        }.getOrNull()
    }
}
