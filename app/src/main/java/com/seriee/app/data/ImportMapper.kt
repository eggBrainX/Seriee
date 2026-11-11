package com.seriee.app.data

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pure mapping and merge logic for collection imports.
 *
 * Kept free of Android APIs so the whole matrix can be covered by JVM unit
 * tests: `ImportMapperTest` and `ImportMergeTest`.
 */
object ImportMapper {

    const val SOURCE_ANILIST = "AniList"
    const val SOURCE_MAL = "MyAnimeList"

    // ---------------------------------------------------------------- mapping

    fun fromAniList(entries: List<AniListEntry>): List<MediaItem> =
        entries.mapNotNull { row ->
            val title = row.title.trim().ifBlank { return@mapNotNull null }
            val movie = row.format.equals("MOVIE", ignoreCase = true)
            val progress = if (movie) {
                if (row.progress > 0) 1 else 0
            } else {
                row.progress.coerceAtLeast(0)
            }
            val total = if (movie) 1 else row.totalEpisodes.takeIf { it > 0 } ?: progress.coerceAtLeast(1)
            val tags = row.genres.cleanTags()
            val description = stripHtml(row.description).ifBlank { row.notes.trim() }
            MediaItem(
                id = stableId(SOURCE_ANILIST, row.id.toString(), title),
                title = title,
                type = if (movie) MediaType.Movie else MediaType.Series,
                status = anilistStatus(row.status),
                poster = row.poster.trim(),
                season = if (movie) 1 else (parseSeasonNumberFromText(title) ?: 1),
                currentEp = if (movie) progress else progress.coerceAtMost(total),
                totalEp = total,
                tags = tags,
                rating = (row.score10 / 2.0).roundToInt().coerceIn(0, 5),
                updatedAt = System.currentTimeMillis(),
                description = description,
                source = SOURCE_ANILIST,
                sourceId = row.id.toString()
            )
        }

    fun fromMal(entries: List<MalEntry>): List<MediaItem> =
        entries.mapNotNull { row ->
            val title = row.title.trim().ifBlank { return@mapNotNull null }
            val movie = row.type.equals("movie", ignoreCase = true) ||
                row.type.equals("Movie", ignoreCase = true)
            val progress = if (movie) {
                if (row.watchedEpisodes > 0) 1 else 0
            } else {
                row.watchedEpisodes.coerceAtLeast(0)
            }
            val total = if (movie) 1 else row.totalEpisodes.takeIf { it > 0 } ?: progress.coerceAtLeast(1)
            MediaItem(
                id = stableId(SOURCE_MAL, row.id, title),
                title = title,
                type = if (movie) MediaType.Movie else MediaType.Series,
                status = malStatus(row.status),
                poster = row.image.trim(),
                season = if (movie) 1 else (parseSeasonNumberFromText(title) ?: 1),
                currentEp = if (movie) progress else progress.coerceAtMost(total),
                totalEp = total,
                tags = row.tags.cleanTags(),
                rating = (row.score / 2.0).roundToInt().coerceIn(0, 5),
                updatedAt = System.currentTimeMillis(),
                description = stripHtml(row.comments),
                source = SOURCE_MAL,
                sourceId = row.id
            )
        }

    fun anilistStatus(raw: String?): WatchStatus = when (raw?.trim()?.uppercase()) {
        "CURRENT", "REPEATING" -> WatchStatus.Watching
        "COMPLETED" -> WatchStatus.Completed
        "PLANNING" -> WatchStatus.PlanToWatch
        "PAUSED" -> WatchStatus.OnHold
        "DROPPED" -> WatchStatus.Dropped
        else -> WatchStatus.PlanToWatch
    }

    fun malStatus(raw: String?): WatchStatus {
        val key = raw.orEmpty().lowercase().filter { it.isLetterOrDigit() }
        return when {
            key == "watching" || key == "current" || key == "repeating" -> WatchStatus.Watching
            key == "completed" -> WatchStatus.Completed
            key == "plantowatch" || key == "planning" -> WatchStatus.PlanToWatch
            key == "onhold" || key == "paused" -> WatchStatus.OnHold
            key == "dropped" -> WatchStatus.Dropped
            else -> WatchStatus.PlanToWatch
        }
    }

    // ------------------------------------------------------------------ merge

    /** Computes counts without writing anything; used by the preview step. */
    fun preview(imported: List<MediaItem>, existing: List<MediaItem>): ImportPreview {
        val lookup = Lookup(existing)
        val unique = uniqueById(imported)
        val now = System.currentTimeMillis()
        var added = 0
        var updated = 0
        var unchanged = 0
        unique.forEach { row ->
            val local = lookup.find(row)
            if (local == null) {
                added++
            } else {
                val merged = mergeOne(local, row, now)
                if (merged.copy(updatedAt = local.updatedAt) == local) unchanged++ else updated++
            }
        }
        return ImportPreview(
            entries = unique,
            total = added + updated + unchanged,
            newCount = added,
            updatedCount = updated,
            unchangedCount = unchanged,
            seriesCount = unique.count { it.type == MediaType.Series },
            movieCount = unique.count { it.type == MediaType.Movie }
        )
    }

    /**
     * Non-destructive merge: progress and totals only grow, status only
     * upgrades, ratings/descriptions fill blanks, local-only fields survive.
     */
    fun merge(imported: List<MediaItem>, existing: List<MediaItem>): Pair<List<MediaItem>, ImportResult> {
        val lookup = Lookup(existing)
        val now = System.currentTimeMillis()
        val replaced = HashMap<String, MediaItem>()
        val additions = ArrayList<MediaItem>()
        var updated = 0
        var skipped = 0

        uniqueById(imported).forEach { row ->
            val local = lookup.find(row)
            if (local == null) {
                additions.add(row)
            } else if (replaced.containsKey(local.id)) {
                skipped++
            } else {
                val merged = mergeOne(local, row, now)
                replaced[local.id] = merged
                if (merged.copy(updatedAt = local.updatedAt) == local) skipped++ else updated++
            }
        }

        val items = existing.map { replaced[it.id] ?: it } + additions
        return items to ImportResult(added = additions.size, updated = updated, skipped = skipped)
    }

    fun replace(imported: List<MediaItem>): Pair<List<MediaItem>, ImportResult> {
        val items = uniqueById(imported)
        return items to ImportResult(added = items.size, updated = 0, skipped = 0)
    }

    private fun mergeOne(local: MediaItem, imported: MediaItem, now: Long): MediaItem =
        local.copy(
            currentEp = maxOf(local.currentEp, imported.currentEp),
            totalEp = maxOf(local.totalEp, imported.totalEp),
            status = upgradeStatus(local.status, imported.status),
            rating = if (local.rating > 0) local.rating else imported.rating,
            description = local.description.ifBlank { imported.description },
            tags = (local.tags + imported.tags).cleanTags(),
            poster = local.poster.ifBlank { imported.poster },
            source = local.source.ifBlank { imported.source },
            sourceId = local.sourceId.ifBlank { imported.sourceId },
            updatedAt = now
        )

    private fun upgradeStatus(local: WatchStatus, imported: WatchStatus): WatchStatus =
        if (statusRank(imported) > statusRank(local)) imported else local

    private fun statusRank(status: WatchStatus): Int = when (status) {
        WatchStatus.PlanToWatch -> 0
        WatchStatus.OnHold -> 1
        WatchStatus.Dropped -> 1
        WatchStatus.Watching -> 2
        WatchStatus.ComingSoon -> 2
        WatchStatus.Completed -> 3
    }

    private class Lookup(existing: List<MediaItem>) {
        private val bySource = existing
            .filter { it.sourceId.isNotBlank() }
            .associateBy { it.source.trim().lowercase() to it.sourceId.trim() }
        private val byTitle = existing.associateBy { normalize(it.title) to it.type }

        fun find(imported: MediaItem): MediaItem? {
            if (imported.sourceId.isNotBlank()) {
                bySource[imported.source.lowercase() to imported.sourceId]?.let { return it }
            }
            return byTitle[normalize(imported.title) to imported.type]
        }
    }

    // ---------------------------------------------------------------- helpers

    fun normalize(title: String): String =
        title.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun stableId(source: String, sourceId: String, title: String): String {
        val prefix = if (source == SOURCE_ANILIST) "anilist" else "mal"
        val key = sourceId.trim().ifBlank { abs(title.lowercase().hashCode()).toString() }
        return "$prefix-$key"
    }

    private fun uniqueById(items: List<MediaItem>): List<MediaItem> {
        val seen = HashSet<String>()
        return items.filter { seen.add(it.id) }
    }

    private fun splitTags(raw: String): List<String> =
        raw.split(',', ';', '|').map { it.trim() }.filter { it.isNotEmpty() }

    private fun List<String>.cleanTags(): List<String> {
        val seen = HashSet<String>()
        return map { it.trim() }
            .filter { it.isNotEmpty() && seen.add(it.lowercase()) }
            .take(8)
    }

    /** Minimal HTML stripping for AniList descriptions (`<br>`, entities). */
    fun stripHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
