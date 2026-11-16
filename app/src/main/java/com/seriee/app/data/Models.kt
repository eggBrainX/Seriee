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
    ComingSoon(R.string.status_coming_soon),
    OnHold(R.string.status_on_hold),
    Dropped(R.string.status_dropped)
}

/**
 * How the app picks light or dark surfaces.
 *
 * Appearance and [AppThemeStyle] are independent axes: every style ships both
 * a light and a dark variant.
 */
enum class AppThemeMode(@StringRes val labelRes: Int) {
    System(R.string.theme_mode_system),
    Light(R.string.theme_mode_light),
    Dark(R.string.theme_mode_dark);

    companion object {
        fun fromName(name: String?): AppThemeMode =
            entries.firstOrNull { it.name == name } ?: System
    }
}

/** Visual treatment applied on top of the chosen appearance. */
enum class AppThemeStyle(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int
) {
    Classic(R.string.theme_style_classic, R.string.theme_style_classic_desc),
    Amoled(R.string.theme_style_amoled, R.string.theme_style_amoled_desc),
    Frost(R.string.theme_style_frost, R.string.theme_style_frost_desc),
    Liquid(R.string.theme_style_liquid, R.string.theme_style_liquid_desc),
    MaterialYou(R.string.theme_style_material_you, R.string.theme_style_material_you_desc),
    Custom(R.string.theme_style_custom, R.string.theme_style_custom_desc),
    NeoBrutalism(R.string.theme_style_neo_brutalism, R.string.theme_style_neo_brutalism_desc);

    companion object {
        fun fromName(name: String?): AppThemeStyle =
            entries.firstOrNull { it.name == name } ?: Classic
    }
}

/**
 * Maps the flat [AppTheme] list used up to 0.4.1 onto the appearance/style pair.
 * Kept pure so it can be unit-tested and reused for theme codes.
 */
object LegacyThemeMap {
    fun migrate(name: String?): Pair<AppThemeMode, AppThemeStyle> = when (name) {
        "FollowSystem" -> AppThemeMode.System to AppThemeStyle.Classic
        "Dark" -> AppThemeMode.Dark to AppThemeStyle.Classic
        "Amoled" -> AppThemeMode.Dark to AppThemeStyle.Amoled
        "Glass" -> AppThemeMode.Dark to AppThemeStyle.Frost
        "LiquidGlass" -> AppThemeMode.Dark to AppThemeStyle.Liquid
        "Light" -> AppThemeMode.Light to AppThemeStyle.Classic
        "Custom" -> AppThemeMode.Dark to AppThemeStyle.Custom
        else -> AppThemeMode.Dark to AppThemeStyle.Classic
    }
}

enum class AppLanguage(@StringRes val labelRes: Int, val tag: String) {
    System(R.string.language_system, ""),
    English(R.string.language_english, "en"),
    Indonesian(R.string.language_indonesian, "in");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == (tag ?: "") } ?: System
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
