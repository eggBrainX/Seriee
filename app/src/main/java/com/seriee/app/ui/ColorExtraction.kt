package com.seriee.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun accentFromPoster(context: Context, poster: String): Long? {
    if (poster.isBlank()) return null
    val data: Any = if (poster.startsWith("/")) File(poster) else poster
    return withContext(Dispatchers.IO) {
        runCatching {
            val req = ImageRequest.Builder(context)
                .data(data)
                .size(120)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(req) as? SuccessResult ?: return@runCatching null
            val bmp = result.drawable.toBitmap()
            dominantAccent(bmp)
        }.getOrNull()
    }
}

fun dominantAccent(bitmap: Bitmap): Long? {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= 0 || h <= 0) return null
    val stepX = maxOf(1, w / 24)
    val stepY = maxOf(1, h / 24)
    val buckets = HashMap<Int, Pair<Int, Float>>()
    val hsv = FloatArray(3)
    var y = 0
    while (y < h) {
        var x = 0
        while (x < w) {
            val px = bitmap.getPixel(x, y)
            if (AndroidColor.alpha(px) >= 180) {
                AndroidColor.colorToHSV(px, hsv)
                val sat = hsv[1]
                val v = hsv[2]
                if (sat >= 0.28f && v in 0.25f..0.95f) {
                    val key = ((AndroidColor.red(px) shr 4) shl 8) or
                        ((AndroidColor.green(px) shr 4) shl 4) or
                        (AndroidColor.blue(px) shr 4)
                    val score = sat * (1f - kotlin.math.abs(v - 0.65f))
                    val prev = buckets[key]
                    buckets[key] = if (prev == null) px to score else prev.first to (prev.second + score)
                }
            }
            x += stepX
        }
        y += stepY
    }
    val best = buckets.values.maxByOrNull { it.second }?.first ?: return null
    AndroidColor.colorToHSV(best, hsv)
    hsv[1] = hsv[1].coerceAtLeast(0.55f)
    hsv[2] = hsv[2].coerceIn(0.62f, 0.92f)
    val vivid = AndroidColor.HSVToColor(0xFF, hsv)
    return vivid.toLong() and 0xFFFFFFFFL
}
