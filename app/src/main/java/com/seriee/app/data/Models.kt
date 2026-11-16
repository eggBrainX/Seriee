package com.seriee.app.data

import com.seriee.app.R
import java.util.UUID

enum class MediaType(val labelRes: Int) {
    Series(R.string.type_series),
    Movie(R.string.type_movie),
}

enum class WatchStatus(val labelRes: Int, val filterRes: Int) {
    Watching(R.string.status_watching, R.string.filter_watching),
    Completed(R.string.status_completed, R.string.filter_completed),
    PlanToWatch(R.string.status_plan_to_watch, R.string.filter_plan_to_watch),
    ComingSoon(R.string.status_coming_soon, R.string.filter_coming_soon),
    OnHold(R.string.status_on_hold, R.string.filter_on_hold),
    Dropped(R.string.status_dropped, R.string.filter_dropped),
}

enum class AppThemeMode(val labelRes: Int) {
    System(R.string.theme_mode_system),
    Light(R.string.theme_mode_light),
    Dark(R.string.theme_mode_dark),
}

enum class AppThemeStyle(val labelRes: Int, val descRes: Int) {
    NeoBrutalism(R.string.theme_style_neo_brutalism, R.string.theme_style_neo_brutalism_desc),
    Classic(R.string.theme_style_classic, R.string.theme_style_classic_desc),
    Amoled(R.string.theme_style_amoled, R.string.theme_style_amoled_desc),
    Frost(R.string.theme_style_frost, R.string.theme_style_frost_desc),
    Liquid(R.string.theme_style_liquid, R.string.theme_style_liquid_desc),
    MaterialYou(R.string.theme_style_material_you, R.string.theme_style_material_you_desc),
    Custom(R.string.theme_style_custom, R.string.theme_style_custom_desc),
}

enum class AppLanguage(val tag: String, val labelRes: Int) {
    System("", R.string.language_system),
    English("en", R.string.language_english),
    Indonesian("in", R.string.language_indonesian),
}

data class EpisodeFile(
    val number: Int,
    val uri: String,
    val displayName: String,
)

data class Season(
    val number: Int,
    val currentEp: Int,
    val totalEp: Int,
    val episodes: List<EpisodeFile> = emptyList(),
)

data class MediaItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: MediaType,
    val status: WatchStatus,
    val poster: String = "",
    val season: Int = 1,
    val currentEp: Int = 0,
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
    val sourceId: String = "",
) {
    val effectiveSeasons: List<Season>
        get() = when {
            type == MediaType.Movie -> emptyList()
            seasons.isNotEmpty() -> seasons
            else -> listOf(
                Season(
                    number = season.coerceAtLeast(1),
                    currentEp = currentEp.coerceAtLeast(0),
                    totalEp = totalEp.coerceAtLeast(1),
                    episodes = if (videoUri.isNotBlank()) {
                        listOf(EpisodeFile(1, videoUri, videoPath.ifBlank { "video" }))
                    } else {
                        emptyList()
                    }
                )
            )
        }

    val totalEpisodes: Int
        get() = if (type == MediaType.Movie) 1 else effectiveSeasons.sumOf { it.totalEp.coerceAtLeast(1) }

    val watchedEpisodes: Int
        get() = if (type == MediaType.Movie) {
            if (currentEp > 0) 1 else 0
        } else {
            effectiveSeasons.sumOf { it.currentEp.coerceIn(0, it.totalEp.coerceAtLeast(1)) }
        }

    val percent: Int
        get() = if (totalEpisodes <= 0) 0 else ((watchedEpisodes * 100) / totalEpisodes).coerceIn(0, 100)

    val fraction: Float
        get() = if (totalEpisodes <= 0) 0f else (watchedEpisodes.toFloat() / totalEpisodes.toFloat()).coerceIn(0f, 1f)
}

val POPULAR_TAGS = listOf(
    "Action",
    "Drama",
    "Comedy",
    "Sci-Fi",
    "Anime",
    "Thriller",
    "Romance",
    "Fantasy",
    "Horror",
    "Documentary",
)
