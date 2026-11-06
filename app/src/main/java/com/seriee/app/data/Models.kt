package com.seriee.app.data

enum class MediaType(val label: String) {
    Series("Series"),
    Movie("Movie")
}

enum class WatchStatus(val label: String) {
    Watching("Watching"),
    Completed("Completed"),
    PlanToWatch("Plan to Watch"),
    ComingSoon("Coming Soon")
}

enum class AppTheme(val label: String) {
    Dark("Dark"),
    Amoled("AMOLED"),
    Glass("Frost Glass"),
    Light("Light")
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
