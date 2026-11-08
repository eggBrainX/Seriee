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

data class EpisodeFile(
    val number: Int,
    val uri: String,
    val displayName: String
)

data class Season(
    val number: Int,
    val currentEp: Int = 0,
    val totalEp: Int = 1,
    val episodes: List<EpisodeFile> = emptyList()
)

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
    val updatedAt: Long = System.currentTimeMillis(),
    val seasons: List<Season> = emptyList(),
    val description: String = "",
    val source: String = "",
    val sourceId: String = ""
) {
    val effectiveSeasons: List<Season>
        get() = if (seasons.isNotEmpty()) seasons else {
            val legacyEpisodes = if (videoUri.isNotBlank()) {
                listOf(EpisodeFile(number = currentEp.coerceAtLeast(1), uri = videoUri, displayName = videoPath.ifBlank { "video" }))
            } else emptyList()
            listOf(
                Season(
                    number = season.coerceAtLeast(1),
                    currentEp = currentEp,
                    totalEp = totalEp.coerceAtLeast(1),
                    episodes = legacyEpisodes
                )
            )
        }

    val totalEpisodes: Int
        get() = if (type == MediaType.Movie) 1 else effectiveSeasons.sumOf { it.totalEp.coerceAtLeast(1) }

    val watchedEpisodes: Int
        get() = if (type == MediaType.Movie) (if (currentEp > 0) 1 else 0) else effectiveSeasons.sumOf { it.currentEp.coerceIn(0, it.totalEp) }

    val percent: Int
        get() = if (totalEpisodes <= 0) 0 else Math.round(watchedEpisodes * 100f / totalEpisodes)

    val fraction: Float
        get() = if (totalEpisodes <= 0) 0f else (watchedEpisodes.toFloat() / totalEpisodes).coerceIn(0f, 1f)
}
