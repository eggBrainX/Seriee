package com.seriee.app.data

import androidx.annotation.StringRes
import com.seriee.app.R

enum class ImportSource { AniList, MyAnimeList }

enum class ImportMode { Merge, Replace }

enum class ImportPhase { Idle, Loading, Preview, Done, Error }

enum class ImportError(@StringRes val messageRes: Int) {
    UserNotFound(R.string.import_error_user_not_found),
    ListPrivateOrEmpty(R.string.import_error_private_or_empty),
    RateLimited(R.string.import_error_rate_limited),
    Network(R.string.import_error_network),
    Parse(R.string.import_error_parse),
    FileInvalid(R.string.import_error_file_invalid),
    Cancelled(R.string.import_error_cancelled),
    NoEntries(R.string.import_no_entries),
    Unknown(R.string.import_error_unknown)
}

class ImportException(val error: ImportError, cause: Throwable? = null) :
    Exception(error.name, cause)

/** One row from AniList's public `mediaList` collection. */
data class AniListEntry(
    val id: Long,
    val title: String,
    val format: String,
    val totalEpisodes: Int,
    val progress: Int,
    val score10: Double,
    val status: String,
    val poster: String,
    val genres: List<String>,
    val description: String,
    val notes: String
)

/** One `<anime>` row from a MyAnimeList XML export. */
data class MalEntry(
    val id: String,
    val title: String,
    val type: String,
    val totalEpisodes: Int,
    val watchedEpisodes: Int,
    val score: Int,
    val status: String,
    val image: String,
    val tags: List<String>,
    val comments: String
)

data class ImportPreview(
    val entries: List<MediaItem>,
    val total: Int,
    val newCount: Int,
    val updatedCount: Int,
    val unchangedCount: Int,
    val seriesCount: Int,
    val movieCount: Int
)

data class ImportResult(
    val added: Int,
    val updated: Int,
    val skipped: Int
) {
    val total: Int get() = added + updated + skipped
}

data class ImportUiState(
    val phase: ImportPhase = ImportPhase.Idle,
    val source: ImportSource = ImportSource.AniList,
    val mode: ImportMode = ImportMode.Merge,
    val fileName: String = "",
    val preview: ImportPreview? = null,
    val result: ImportResult? = null,
    val error: ImportError? = null
)
