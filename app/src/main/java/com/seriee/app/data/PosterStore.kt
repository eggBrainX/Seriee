package com.seriee.app.data

import android.content.Context
import android.net.Uri
import java.io.File

object PosterStore {
    fun copyPosterToInternal(context: Context, uri: Uri, mediaId: String): String? {
        return try {
            val resolver = context.contentResolver
            val ext = resolver.getType(uri)?.substringAfterLast("/")?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment?.substringAfterLast(".", "")?.takeIf { it.isNotBlank() } ?: "jpg"
            val dir = File(context.filesDir, "posters")
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, "${mediaId}_${System.currentTimeMillis()}.$ext")
            resolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            "posters/${dest.name}"
        } catch (_: Exception) {
            null
        }
    }

    fun fileFor(context: Context, relativePath: String): File? {
        if (relativePath.startsWith("posters/")) {
            val f = File(context.filesDir, relativePath)
            return if (f.exists()) f else null
        }
        return null
    }

    fun deleteIfLocal(context: Context, poster: String) {
        if (poster.startsWith("posters/")) {
            try {
                val f = File(context.filesDir, poster)
                if (f.exists()) f.delete()
            } catch (_: Exception) { }
        }
    }

    fun isLocalPath(poster: String): Boolean = poster.startsWith("posters/")
    fun isRemoteUrl(poster: String): Boolean = poster.startsWith("http://") || poster.startsWith("https://")
}
