package com.seriee.app.data

import kotlin.math.roundToInt

data class AniListListRow(
    val mediaId: Int,
    val title: String,
    val format: String,
    val episodes: Int,
    val status: String,
    val progress: Int,
    val score100: Int,
    val poster: String,
    val genres: List<String>,
    val description: String,
)

data class MalRawEntry(
    val id: String,
    val title: String,
    val type: String,
    val episodes: Int,
    val watched: Int,
    val status: String,
    val score: Int,
)

object ImportMapper {
    private val statusRank = mapOf(
        WatchStatus.PlanToWatch to 0,
        WatchStatus.ComingSoon to 0,
        WatchStatus.OnHold to 1,
        WatchStatus.Dropped to 2,
        WatchStatus.Watching to 3,
        WatchStatus.Completed to 4,
    )

    private fun normTitle(t: String): String =
        t.trim().lowercase().replace(Regex("\\s+"), " ")

    fun fromAniList(rows: List<AniListListRow>): List<ImportedEntry> = rows.map { r ->
        val isMovie = r.format == "MOVIE"
        val type = if (isMovie) MediaType.Movie else MediaType.Series
        val status = when (r.status) {
            "CURRENT", "REPEATING" -> WatchStatus.Watching
            "COMPLETED" -> WatchStatus.Completed
            "PAUSED" -> WatchStatus.OnHold
            "DROPPED" -> WatchStatus.Dropped
            else -> WatchStatus.PlanToWatch
        }
        val total = if (isMovie) 1 else maxOf(1, r.episodes, r.progress)
        val current = if (isMovie) {
            if (status == WatchStatus.Completed || r.progress > 0) 1 else 0
        } else {
            r.progress.coerceIn(0, total)
        }
        val rating = if (r.score100 > 0) (r.score100 / 20f).roundToInt().coerceIn(1, 5) else 0
        ImportedEntry(
            title = r.title,
            type = type,
            status = status,
            currentEp = current,
            totalEp = total,
            rating = rating,
            poster = r.poster,
            tags = r.genres.take(6),
            description = r.description,
            source = ImportSource.ANILIST,
            sourceId = r.mediaId.toString(),
        )
    }

    fun fromMal(entries: List<MalRawEntry>): List<ImportedEntry> = entries.map { e ->
        val isMovie = e.type.trim().equals("movie", ignoreCase = true)
        val type = if (isMovie) MediaType.Movie else MediaType.Series
        val status = when (e.status.trim().lowercase()) {
            "watching" -> WatchStatus.Watching
            "completed" -> WatchStatus.Completed
            "on-hold" -> WatchStatus.OnHold
            "dropped" -> WatchStatus.Dropped
            else -> WatchStatus.PlanToWatch
        }
        val total = if (isMovie) 1 else maxOf(1, e.episodes, e.watched)
        val current = if (isMovie) {
            if (status == WatchStatus.Completed || e.watched > 0) 1 else 0
        } else {
            e.watched.coerceIn(0, total)
        }
        val rating = if (e.score > 0) (e.score / 2f).roundToInt().coerceIn(1, 5) else 0
        ImportedEntry(
            title = e.title,
            type = type,
            status = status,
            currentEp = current,
            totalEp = total,
            rating = rating,
            poster = "",
            tags = emptyList(),
            description = "",
            source = ImportSource.MAL,
            sourceId = e.id,
        )
    }

    private fun findMatchIndex(existing: List<MediaItem>, entry: ImportedEntry): Int {
        val bySource = existing.indexOfFirst {
            it.source.equals(entry.source.name, ignoreCase = true) &&
                entry.sourceId.isNotBlank() &&
                it.sourceId == entry.sourceId
        }
        if (bySource >= 0) return bySource
        val targetTitle = normTitle(entry.title)
        return existing.indexOfFirst { normTitle(it.title) == targetTitle && it.type == entry.type }
    }

    private fun toMediaItem(entry: ImportedEntry, now: Long): MediaItem {
        val idPrefix = if (entry.source == ImportSource.ANILIST) "anilist" else "mal"
        val id = if (entry.sourceId.isNotBlank()) "$idPrefix-${entry.sourceId}" else java.util.UUID.randomUUID().toString()
        val seasons = if (entry.type == MediaType.Series) {
            listOf(Season(1, entry.currentEp, entry.totalEp.coerceAtLeast(1)))
        } else emptyList()
        return MediaItem(
            id = id,
            title = entry.title,
            type = entry.type,
            status = entry.status,
            poster = entry.poster,
            season = 1,
            currentEp = entry.currentEp,
            totalEp = entry.totalEp.coerceAtLeast(1),
            tags = entry.tags,
            rating = entry.rating,
            updatedAt = now,
            seasons = seasons,
            description = entry.description,
            source = entry.source.name,
            sourceId = entry.sourceId,
        )
    }

    private fun mergeItem(existing: MediaItem, entry: ImportedEntry, now: Long): Pair<MediaItem, Boolean> {
        var changed = false
        val rankNew = statusRank[entry.status] ?: 0
        val rankOld = statusRank[existing.status] ?: 0
        val nextStatus = if (rankNew > rankOld) entry.status else existing.status
        if (nextStatus != existing.status) changed = true

        val nextRating = if (existing.rating > 0) existing.rating else entry.rating
        if (nextRating != existing.rating) changed = true

        val nextPoster = existing.poster.ifBlank { entry.poster }
        if (nextPoster != existing.poster) changed = true

        val nextDesc = existing.description.ifBlank { entry.description }
        if (nextDesc != existing.description) changed = true

        val nextSource = existing.source.ifBlank { entry.source.name }
        val nextSourceId = existing.sourceId.ifBlank { entry.sourceId }
        if (nextSource != existing.source || nextSourceId != existing.sourceId) changed = true

        val tagMap = LinkedHashMap<String, String>()
        for (t in existing.tags + entry.tags) {
            val trimmed = t.trim()
            if (trimmed.isNotEmpty()) tagMap.putIfAbsent(trimmed.lowercase(), trimmed)
        }
        val nextTags = tagMap.values.take(8)
        if (nextTags.size != existing.tags.size) changed = true

        var nextSeasons = existing.seasons
        var nextCur = existing.currentEp
        var nextTot = existing.totalEp

        if (existing.type == MediaType.Series) {
            val eff = existing.effectiveSeasons
            if (eff.size == 1) {
                val s1 = eff.first()
                val mTot = maxOf(s1.totalEp, entry.totalEp, 1)
                val mCur = maxOf(s1.currentEp, entry.currentEp).coerceIn(0, mTot)
                if (mTot != s1.totalEp || mCur != s1.currentEp) changed = true
                nextSeasons = listOf(s1.copy(currentEp = mCur, totalEp = mTot))
                nextCur = mCur
                nextTot = mTot
            } else {
                val mCur = maxOf(existing.currentEp, entry.currentEp)
                val mTot = maxOf(existing.totalEp, entry.totalEp, 1)
                if (mCur != existing.currentEp || mTot != existing.totalEp) changed = true
                nextCur = mCur
                nextTot = mTot
            }
        } else {
            val mCur = maxOf(existing.currentEp, entry.currentEp)
            if (mCur != existing.currentEp) changed = true
            nextCur = mCur
            nextTot = 1
        }

        if (!changed) return existing to false
        return existing.copy(
            status = nextStatus,
            rating = nextRating,
            poster = nextPoster,
            description = nextDesc,
            tags = nextTags,
            source = nextSource,
            sourceId = nextSourceId,
            seasons = nextSeasons,
            currentEp = nextCur,
            totalEp = nextTot,
            updatedAt = now,
        ) to true
    }

    fun preview(existing: List<MediaItem>, entries: List<ImportedEntry>): ImportPreview {
        var newCount = 0
        var updatedCount = 0
        var unchangedCount = 0
        var seriesCount = 0
        var movieCount = 0
        val now = System.currentTimeMillis()
        for (e in entries) {
            if (e.type == MediaType.Movie) movieCount++ else seriesCount++
            val idx = findMatchIndex(existing, e)
            if (idx < 0) newCount++
            else if (mergeItem(existing[idx], e, now).second) updatedCount++
            else unchangedCount++
        }
        return ImportPreview(entries.size, newCount, updatedCount, unchangedCount, seriesCount, movieCount)
    }

    fun merge(
        existing: List<MediaItem>,
        entries: List<ImportedEntry>,
        now: Long = System.currentTimeMillis(),
    ): Pair<List<MediaItem>, ImportSummary> {
        val list = existing.toMutableList()
        var added = 0
        var updated = 0
        var skipped = 0
        for (e in entries) {
            val idx = findMatchIndex(list, e)
            if (idx < 0) {
                list.add(toMediaItem(e, now))
                added++
            } else {
                val (merged, changed) = mergeItem(list[idx], e, now)
                if (changed) {
                    list[idx] = merged
                    updated++
                } else skipped++
            }
        }
        return list to ImportSummary(added, updated, skipped)
    }

    fun replace(
        entries: List<ImportedEntry>,
        now: Long = System.currentTimeMillis(),
    ): Pair<List<MediaItem>, ImportSummary> {
        val items = entries.map { toMediaItem(it, now) }
        return items to ImportSummary(items.size, 0, 0)
    }
}
