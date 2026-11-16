package com.seriee.app.data

data class WatchEvent(
    val itemId: String,
    val title: String,
    val type: MediaType,
    val season: Int,
    val episode: Int,
    val count: Int,
    val watchedAt: Long,
)

object WatchHistoryRules {
    const val MAX_EVENTS = 500
    const val RETENTION_DAYS = 365L
    private const val DAY_MS = 86_400_000L

    fun trim(events: List<WatchEvent>, nowMs: Long = System.currentTimeMillis()): List<WatchEvent> {
        val cutoff = nowMs - RETENTION_DAYS * DAY_MS
        return events
            .filter { it.watchedAt >= cutoff && it.count > 0 }
            .sortedByDescending { it.watchedAt }
            .take(MAX_EVENTS)
    }

    fun append(
        existing: List<WatchEvent>,
        event: WatchEvent,
        nowMs: Long = System.currentTimeMillis(),
    ): List<WatchEvent> {
        if (event.count <= 0) return trim(existing, nowMs)
        return trim(listOf(event) + existing, nowMs)
    }

    fun merge(
        existing: List<WatchEvent>,
        incoming: List<WatchEvent>,
        nowMs: Long = System.currentTimeMillis(),
    ): List<WatchEvent> {
        val seen = HashSet<String>()
        val out = ArrayList<WatchEvent>(existing.size + incoming.size)
        for (ev in existing + incoming) {
            val key = "${ev.itemId}|${ev.season}|${ev.episode}|${ev.watchedAt}"
            if (seen.add(key)) {
                out.add(ev)
            }
        }
        return trim(out, nowMs)
    }
}
