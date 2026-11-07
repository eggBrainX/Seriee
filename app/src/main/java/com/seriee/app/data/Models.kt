package com.seriee.app.data

import androidx.annotation.StringRes
import com.seriee.app.R

enum class MediaType(@StringRes val labelRes: Int) {
    Series(R.string.type_series),
    Movie(R.string.type_movie)
}

enum class WatchStatus(@StringRes val labelRes: Int) {
    Watching(R.string.status_watching),
    Completed(R.string.status_completed),
    PlanToWatch(R.string.status_plan_to_watch),
    ComingSoon(R.string.status_coming_soon)
}

enum class AppTheme(val label: String) {
    Dark("Dark"),
    Amoled("AMOLED"),
    Glass("Frost Glass"),
    Light("Light")
}

enum class AppLanguage(val tag: String, val label: String) {
    English("en", "English"),
    Indonesian("in", "Bahasa Indonesia");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: English
    }
}

data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val status: WatchStatus,
    val poster: String,
    val season: Int = 1,
    val currentEp: Int = 1,
    val totalEp: Int = 1,
    val pinned: Boolean = false,
    val streamUrl: String = "",
    val videoPath: String = "",
    val videoUri: String = "",
    val releaseDate: Long? = null,
    val reminderEnabled: Boolean = false,
    val tags: List<String> = emptyList(),
    val rating: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val percent: Int
        get() = if (totalEp <= 0) 0 else Math.round(currentEp * 100f / totalEp)

    val fraction: Float
        get() = if (totalEp <= 0) 0f else (currentEp.toFloat() / totalEp).coerceIn(0f, 1f)
}
