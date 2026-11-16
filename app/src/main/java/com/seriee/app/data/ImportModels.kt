package com.seriee.app.data

enum class ImportSource { ANILIST, MAL }
enum class ImportMode { MERGE, REPLACE }

data class ImportedEntry(
    val title: String,
    val type: MediaType,
    val status: WatchStatus,
    val currentEp: Int,
    val totalEp: Int,
    val rating: Int,
    val poster: String,
    val tags: List<String>,
    val description: String,
    val source: ImportSource,
    val sourceId: String,
)

data class ImportPreview(
    val total: Int,
    val newCount: Int,
    val updatedCount: Int,
    val unchangedCount: Int,
    val seriesCount: Int,
    val movieCount: Int,
)

data class ImportSummary(
    val added: Int,
    val updated: Int,
    val skipped: Int,
)
