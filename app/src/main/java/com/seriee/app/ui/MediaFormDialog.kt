package com.seriee.app.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.EpisodeFile
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.MetadataRepository
import com.seriee.app.data.MetadataResult
import com.seriee.app.data.PosterStore
import com.seriee.app.data.Season
import com.seriee.app.data.SerieeRepository
import com.seriee.app.data.WatchStatus
import com.seriee.app.data.parseSeasonNumberFromText
import com.seriee.app.reminder.ReminderScheduler
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val EPISODES_PAGE_SIZE = 50
private const val EPISODES_UNPAGED_LIMIT = 100

private data class SeasonDraft(
    val number: Int,
    val totalEpStr: String,
    val currentEpStr: String,
    val episodes: Map<Int, EpisodeFile> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFormDialog(
    initial: MediaItem?,
    existing: List<MediaItem> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (MediaItem) -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: MediaType.Series) }
    var status by remember { mutableStateOf(initial?.status ?: WatchStatus.Watching) }
    var poster by remember { mutableStateOf(initial?.poster ?: "") }
    var posterIsStorage by remember { mutableStateOf(PosterStore.isLocalPath(initial?.poster ?: "")) }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var source by remember { mutableStateOf(initial?.source ?: "") }
    var sourceId by remember { mutableStateOf(initial?.sourceId ?: "") }
    var streamUrl by remember { mutableStateOf(initial?.streamUrl ?: "") }
    var videoPath by remember { mutableStateOf(initial?.videoPath ?: "") }
    var videoUri by remember { mutableStateOf(initial?.videoUri ?: "") }
    var releaseDate by remember { mutableStateOf(initial?.releaseDate) }
    var reminderEnabled by remember {
        mutableStateOf(initial?.reminderEnabled ?: (initial?.status == WatchStatus.ComingSoon))
    }
    var tags by remember { mutableStateOf(initial?.tags ?: emptyList()) }
    var rating by remember { mutableStateOf(initial?.rating ?: 0) }
    var error by remember { mutableStateOf<String?>(null) }

    // seasons drafts
    var seasonsDraft by remember {
        mutableStateOf(
            run {
                val eff = initial?.effectiveSeasons
                if (!eff.isNullOrEmpty() && initial.type == MediaType.Series) {
                    eff.map { s ->
                        val map = mutableMapOf<Int, EpisodeFile>()
                        s.episodes.forEach { map[it.number] = it }
                        SeasonDraft(s.number, s.totalEp.toString(), s.currentEp.toString(), map)
                    }.toMutableList()
                } else mutableListOf(SeasonDraft(1, (initial?.totalEp ?: 12).toString(), (initial?.currentEp ?: 1).toString(), mutableMapOf()))
            }
        )
    }

    // Per-season episode paging (season number -> state). Keeps 1000+ ep lists cheap.
    val episodePageBySeason = remember { mutableStateMapOf<Int, Int>() }
    val showMissingOnlyBySeason = remember { mutableStateMapOf<Int, Boolean>() }
    val gotoEpBySeason = remember { mutableStateMapOf<Int, String>() }

    // fetch state
    var fetchQuery by remember { mutableStateOf(initial?.title ?: "") }
    var fetchResults by remember { mutableStateOf<List<MetadataResult>>(emptyList()) }
    var fetchLoading by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    val metadataRepo = remember { MetadataRepository() }

    // Keys live in the isolated, non-backed-up preferences file.
    val keysRepo = remember { SerieeRepository(context) }
    val tmdbKey = remember { keysRepo.loadTmdbKey() }
    val omdbKey = remember { keysRepo.loadOmdbKey() }

    fun upsertDraft(idx: Int, updated: SeasonDraft) {
        if (idx in seasonsDraft.indices) {
            val copy = seasonsDraft.toMutableList()
            copy[idx] = updated
            seasonsDraft = copy
        }
    }

    // [persistTreeUri] = tree URI returned by OpenDocumentTree() that owns these descendants.
    // When non-null, the caller must have persisted that tree URI already (see folderPicker)
    // and per-child persist attempts must be skipped: descendants built with
    // buildDocumentUriUsingTree() cannot take individual persistable grants
    // (that call throws and is swallowed, leaving no grant after reboot).
    // When null, [files] came from OpenMultipleDocuments()/OpenDocument() and each URI
    // can and must take its own persistable grant.
    fun mergePicked(files: List<Uri>, targetSeasonIdx: Int = 0, persistTreeUri: Uri? = null) {
        if (files.isEmpty()) return
        // 100s/1000s of files: resolver queries + permission grants are IPC — keep off main thread.
        scope.launch {
            val parsed = withContext(Dispatchers.IO) {
                files.map { uri ->
                    if (persistTreeUri == null) {
                        try {
                            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } catch (_: Exception) {}
                    }
                    val name = displayName(context, uri) ?: uri.lastPathSegment ?: "video"
                    Triple(uri.toString(), name, EpisodeFileParser.parseSeasonEpisode(name) to EpisodeFileParser.parseEpisode(name))
                }
            }
            // Apply on main using latest drafts (user may have edited while parsing).
            val newDrafts = seasonsDraft.toMutableList()
            fun ensureIndex(num: Int): Int {
                var idx = newDrafts.indexOfFirst { it.number == num }
                if (idx < 0) {
                    newDrafts.add(SeasonDraft(num, "12", "0"))
                    idx = newDrafts.size - 1
                }
                return idx
            }
            for ((uriStr, name, pair) in parsed) {
                val (sx, epParsed) = pair
                if (sx != null) {
                    val (sNum, eNum) = sx
                    val idx = ensureIndex(sNum)
                    val d = newDrafts[idx]
                    newDrafts[idx] = d.copy(episodes = d.episodes + (eNum to EpisodeFile(eNum, uriStr, name)))
                } else {
                    val idx = newDrafts.getOrNull(targetSeasonIdx)?.let { targetSeasonIdx } ?: ensureIndex(1)
                    val d = newDrafts[idx]
                    val eNum = epParsed ?: (d.episodes.keys.maxOrNull()?.plus(1) ?: 1)
                    newDrafts[idx] = d.copy(episodes = d.episodes + (eNum to EpisodeFile(eNum, uriStr, name)))
                }
            }
            seasonsDraft = newDrafts.sortedBy { it.number }.toMutableList()
        }
    }

    // single file picker (movie / per-episode manual replace target)
    var manualTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) } // (seasonIdx, epNum)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            val name = displayName(context, uri) ?: uri.lastPathSegment ?: "video"
            val mt = manualTarget
            if (mt != null && type == MediaType.Series) {
                val (sIdx, epNum) = mt
                if (sIdx in seasonsDraft.indices) {
                    val d = seasonsDraft[sIdx]
                    upsertDraft(sIdx, d.copy(episodes = d.episodes + (epNum to EpisodeFile(epNum, uri.toString(), name))))
                }
            } else {
                videoUri = uri.toString()
                videoPath = name
            }
            manualTarget = null
        }
    }

    val multiPickerTarget = remember { mutableIntStateOf(0) }
    val multiPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) mergePicked(uris, multiPickerTarget.intValue)
    }

    val folderPickerTarget = remember { mutableIntStateOf(0) }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            // Persist the picker-returned tree URI BEFORE enumerating/saving descendants.
            // Descendants built with DocumentsContract.buildDocumentUriUsingTree() cannot
            // take their own persistable grants — persistence comes from the tree grant.
            // Without this, saved episode URIs stop opening after process death / reboot.
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            // Enumerate video files off the main thread (folders can hold 1000+ files).
            val target = folderPickerTarget.intValue
            scope.launch {
                val files = withContext(Dispatchers.IO) {
                    val out = mutableListOf<Uri>()
                    try {
                        val children = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(uri, android.provider.DocumentsContract.getTreeDocumentId(uri))
                        context.contentResolver.query(children, arrayOf(android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID, android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME, android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE), null, null, null)?.use { cursor ->
                            while (cursor.moveToNext()) {
                                val docId = if (!cursor.isNull(0)) cursor.getString(0) else null
                                val mime = if (!cursor.isNull(2)) cursor.getString(2) else null
                                if (!docId.isNullOrBlank() && mime != null && mime.startsWith("video/")) {
                                    out.add(android.provider.DocumentsContract.buildDocumentUriUsingTree(uri, docId))
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    out
                }
                // Pass the persisted tree URI so mergePicked skips per-child persist attempts.
                if (files.isNotEmpty()) mergePicked(files, target, persistTreeUri = uri)
            }
        }
    }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val id = initial?.id ?: title.trim().hashCode().toString().take(8).ifBlank { "new" }
                val rel = PosterStore.copyPosterToInternal(context, uri, id)
                withContext(Dispatchers.Main) {
                    if (rel != null) {
                        poster = rel
                        posterIsStorage = true
                    } else {
                        Toast.makeText(context, context.getString(R.string.fetch_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun doFetch() {
        val q = fetchQuery.trim()
        if (q.isBlank()) return
        fetchLoading = true
        fetchError = null
        hasSearched = true
        scope.launch {
            val res = metadataRepo.search(q, tmdbKey, omdbKey)
            withContext(Dispatchers.Main) {
                fetchLoading = false
                res.onSuccess { fetchResults = it }
                    .onFailure {
                        fetchResults = emptyList()
                        fetchError = context.getString(R.string.fetch_failed)
                    }
            }
        }
    }

    fun applyFetch(r: MetadataResult) {
        title = r.title
        type = r.type
        if (r.posterUrl.isNotBlank()) {
            poster = r.posterUrl
            posterIsStorage = false
        }
        description = r.description
        source = r.source
        sourceId = r.sourceId
        if (r.tags.isNotEmpty()) tags = r.tags
        // Use the source's real air time instead of a guessed date, so Coming Soon
        // countdowns match AniList/TMDB. AniList's `airingAt` is exact (episode 1).
        val upcomingAir = r.releaseAt?.takeIf { it > System.currentTimeMillis() }
        if (r.upcoming) status = WatchStatus.ComingSoon
        if (upcomingAir != null) {
            releaseDate = upcomingAir
            reminderEnabled = true
        }
        // Season comes from the source result (AniList entry / TMDB season numbers),
        // falling back to parsing the fetched title. This keeps "…Season 4" from
        // saving as a Season 1 card — so Pick Folder targets Season 4.
        val detectedSeason = r.seasonNumbers.firstOrNull()?.takeIf { it > 0 }
            ?: parseSeasonNumberFromText(r.title)
        // Season-scoped result: the source named one explicit season (TMDB
        // filter / single AniList entry), so counts hold just that season.
        val isSeasonScoped = r.seasonEpisodeCounts.size == 1 && detectedSeason != null
        if (r.seasonEpisodeCounts.isNotEmpty()) {
            val scopedDrafts = r.seasonEpisodeCounts.mapIndexed { idx, total ->
                val num = r.seasonNumbers.getOrNull(idx)?.takeIf { it > 0 }
                    ?: detectedSeason
                    ?: seasonsDraft.getOrNull(idx)?.number
                    ?: (idx + 1)
                // Match by season number only — never borrow progress/episode
                // files from another season via index fallback.
                val existing = seasonsDraft.firstOrNull { it.number == num }
                SeasonDraft(num, total.toString(), existing?.currentEpStr ?: "0", existing?.episodes ?: emptyMap())
            }
            seasonsDraft = if (isSeasonScoped && seasonsDraft.size > 1) {
                // Editing a multi-season series with a single-season result:
                // update only that season and preserve the other drafts
                // (progress + episode files) instead of replacing the list.
                val byNumber = seasonsDraft.associateBy { it.number }.toMutableMap()
                scopedDrafts.forEach { byNumber[it.number] = it }
                byNumber.values.sortedBy { it.number }.toMutableList()
            } else {
                scopedDrafts.sortedBy { it.number }.toMutableList()
            }
        } else if (detectedSeason != null) {
            val existing = seasonsDraft.firstOrNull { it.number == detectedSeason }
                ?: if (seasonsDraft.size == 1) seasonsDraft.firstOrNull { it.episodes.isEmpty() } else null
            val single = existing?.copy(number = detectedSeason)
                ?: SeasonDraft(detectedSeason, "12", "0", emptyMap())
            seasonsDraft = if (seasonsDraft.size > 1) {
                // Same preservation for the no-counts path on multi-season drafts.
                (seasonsDraft.filter { it.number != detectedSeason } + single)
                    .sortedBy { it.number }.toMutableList()
            } else {
                mutableListOf(single)
            }
        }
        if (r.streamCandidates.isNotEmpty()) {
            if (streamUrl.isBlank()) streamUrl = r.streamCandidates.first().url
        }
        fetchResults = emptyList()
        fetchError = null
        Toast.makeText(context, context.getString(R.string.fetch_applied), Toast.LENGTH_SHORT).show()
    }

    fun normalizedTitle(value: String) = value.trim().replace(Regex("\\s+"), " ").lowercase()

    fun targetSeason(): Int =
        if (type == MediaType.Series) (seasonsDraft.minOfOrNull { it.number } ?: 1) else 1

    // Same title + type + season is one library entry. When both entries carry a
    // metadata source id, different ids mean different titles (remakes, etc.).
    fun isDuplicateOf(other: MediaItem): Boolean {
        if (other.type != type) return false
        if (normalizedTitle(other.title) != normalizedTitle(title)) return false
        if (other.season != targetSeason()) return false
        return if (sourceId.isNotBlank() && other.sourceId.isNotBlank()) {
            other.sourceId == sourceId && other.source.equals(source, ignoreCase = true)
        } else true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(
            topStart = if (p.isNeoBrutalist) 8.dp else 28.dp,
            topEnd = if (p.isNeoBrutalist) 8.dp else 28.dp
        ),
        containerColor = if (p.isGlass) p.surface.copy(alpha = if (p.isLight) 0.90f else 0.84f) else p.surface,
        tonalElevation = if (p.isNeoBrutalist) 0.dp else BottomSheetDefaults.Elevation,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = { SheetDragHandle() }
    ) {
        LiquidSheetHost {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (initial == null) stringResource(R.string.add_to_seriee) else stringResource(R.string.edit_media),
                        fontSize = 20.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                        color = p.text,
                        modifier = Modifier.weight(1f)
                    )
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = onDismiss,
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.OffWhite,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 2.dp,
                            cornerRadius = 6.dp,
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = NeoBrutalistColors.Black)
                        }
                    } else {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                        }
                    }
                }

                // Fetch metadata
                val fetchCardModifier = if (p.isNeoBrutalist) {
                    Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = p.card,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 8.dp
                        )
                        .padding(14.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(20.dp),
                            tint = p.card,
                            blurRadius = 12.dp,
                            refractionHeight = 8.dp,
                            refractionAmount = 14.dp,
                            chromaticAberration = false
                        )
                        .padding(14.dp)
                }
                Column(
                    modifier = fetchCardModifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FieldLabel(stringResource(R.string.fetch_metadata_title))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SField(fetchQuery, { fetchQuery = it }, placeholder = stringResource(R.string.fetch_search_hint), modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = { doFetch() },
                                enabled = !fetchLoading,
                                role = Role.Button,
                                backgroundColor = NeoBrutalistColors.Yellow,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.5.dp,
                                shadowOffset = 3.dp,
                                cornerRadius = 8.dp,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (fetchLoading) {
                                    CircularProgressIndicator(color = NeoBrutalistColors.Black, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.fetch_search_button), tint = NeoBrutalistColors.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(p.primary)
                                    .clickable(enabled = !fetchLoading) { doFetch() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                if (fetchLoading) CircularProgressIndicator(color = p.onAccent, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                else Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.fetch_search_button), tint = p.onAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    if (fetchLoading) {
                        Text(
                            stringResource(R.string.fetch_loading),
                            fontSize = 11.sp,
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            color = p.muted
                        )
                    }
                    fetchError?.let {
                        Text(it, color = p.danger, fontSize = 12.sp, fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal)
                    }
                    if (hasSearched && !fetchLoading && fetchError == null && fetchResults.isEmpty()) {
                        Text(stringResource(R.string.fetch_no_results), fontSize = 12.sp, color = p.muted)
                    }
                    fetchResults.forEach { r ->
                        val resultSeason = r.seasonNumbers.firstOrNull()?.takeIf { it > 0 }
                            ?: parseSeasonNumberFromText(r.title)
                        val seasonLabel = if (r.type == MediaType.Series && resultSeason != null && resultSeason > 1)
                            " · " + stringResource(R.string.season_format, resultSeason) else ""
                        val airLabel = r.releaseAt?.takeIf { it > System.currentTimeMillis() }
                            ?.let { " · " + ReminderScheduler.formatShortDate(context, it) } ?: ""

                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = { applyFetch(r) },
                                role = Role.Button,
                                backgroundColor = p.chip,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                cornerRadius = 8.dp,
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Poster(r.posterUrl, Modifier.size(width = 44.dp, height = 58.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(r.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text, maxLines = 1)
                                        Text(
                                            ("${r.source} ${r.year}".trim() + seasonLabel + airLabel),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = p.muted
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeoBrutalistColors.SkyBlue)
                                            .border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            r.source,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeoBrutalistColors.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(p.chip)
                                    .border(1.dp, p.border, RoundedCornerShape(12.dp))
                                    .clickable { applyFetch(r) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Poster(r.posterUrl, Modifier.size(width = 44.dp, height = 58.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(r.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1)
                                    Text(("${r.source} ${r.year}".trim() + seasonLabel + airLabel), fontSize = 11.sp, color = p.muted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(p.primary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) { Text(r.source, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.primary) }
                            }
                        }
                    }
                    // stream candidates from last fetch are applied directly; user can edit streamUrl below
                }

                Column {
                    FieldLabel(stringResource(R.string.title_label))
                    SField(title, {
                        title = it
                        val detected = parseSeasonNumberFromText(it)
                        if (detected != null && type == MediaType.Series && seasonsDraft.size == 1) {
                            val only = seasonsDraft.first()
                            if (only.episodes.isEmpty() && only.number != detected) {
                                seasonsDraft = listOf(only.copy(number = detected)).toMutableList()
                            }
                        }
                    }, placeholder = stringResource(R.string.title_placeholder))
                }

                // Description
                Column {
                    FieldLabel(stringResource(R.string.description_label))
                    if (p.isNeoBrutalist) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalist(
                                    backgroundColor = p.surface,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.5.dp,
                                    shadowOffset = 4.dp,
                                    cornerRadius = 8.dp
                                )
                        ) {
                            TextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                placeholder = { Text("", fontSize = 14.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = p.text,
                                    unfocusedTextColor = p.text,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = p.text,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            placeholder = { Text("", fontSize = 14.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = p.text,
                                unfocusedTextColor = p.text,
                                focusedContainerColor = p.bg,
                                unfocusedContainerColor = p.bg,
                                cursorColor = p.primary,
                                focusedBorderColor = p.primary,
                                unfocusedBorderColor = p.border
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownField(
                        label = stringResource(R.string.type_label),
                        valueLabel = stringResource(type.labelRes),
                        options = MediaType.entries.map { it to stringResource(it.labelRes) },
                        onSelect = { type = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = stringResource(R.string.status_label),
                        valueLabel = stringResource(status.labelRes),
                        options = WatchStatus.entries.map { it to stringResource(it.labelRes) },
                        onSelect = {
                            status = it
                            if (it == WatchStatus.ComingSoon) {
                                reminderEnabled = true
                                val currentRelease = releaseDate
                                if (currentRelease == null || currentRelease <= System.currentTimeMillis()) {
                                    ReminderScheduler.showDateTimePicker(context, currentRelease) { selected ->
                                        releaseDate = selected
                                        reminderEnabled = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                val ratingCardModifier = if (p.isNeoBrutalist) {
                    Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = p.card,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 8.dp
                        )
                        .padding(12.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(18.dp),
                            tint = p.card,
                            blurRadius = 12.dp,
                            refractionHeight = 8.dp,
                            refractionAmount = 14.dp,
                            chromaticAberration = false
                        )
                        .padding(12.dp)
                }
                Column(
                    modifier = ratingCardModifier,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FieldLabel(stringResource(R.string.rating_label))
                    StarRatingBar(rating = rating, onRatingChange = { rating = it }, starSize = 26.dp)
                }

                TagInputField(tags = tags, onTagsChanged = { tags = it })

                val showReminderSection = status == WatchStatus.ComingSoon || status == WatchStatus.PlanToWatch || releaseDate != null
                if (showReminderSection) {
                    val reminderCardModifier = if (p.isNeoBrutalist) {
                        Modifier
                            .fillMaxWidth()
                            .neoBrutalist(
                                backgroundColor = if (status == WatchStatus.ComingSoon && p.isLight) NeoBrutalistColors.Cream else p.card,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.5.dp,
                                shadowOffset = 4.dp,
                                cornerRadius = 8.dp
                            )
                            .padding(14.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (status == WatchStatus.ComingSoon) p.reminder.copy(
                                    alpha = 0.08f
                                ) else p.card
                            )
                            .border(
                                1.dp,
                                if (status == WatchStatus.ComingSoon) p.reminder.copy(alpha = 0.35f) else p.border,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    }
                    Column(
                        modifier = reminderCardModifier,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.Alarm,
                                    contentDescription = null,
                                    tint = when {
                                        p.isNeoBrutalist -> p.text
                                        reminderEnabled -> p.reminder
                                        else -> p.muted
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        stringResource(R.string.release_reminder_title),
                                        fontSize = 13.sp,
                                        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = p.text
                                    )
                                    Text(
                                        if (reminderEnabled) stringResource(R.string.reminder_on_desc) else stringResource(R.string.reminder_off_desc),
                                        fontSize = 11.sp, color = p.muted
                                    )
                                }
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = {
                                    reminderEnabled = it
                                    val currentRelease = releaseDate
                                    if (it && (currentRelease == null || currentRelease <= System.currentTimeMillis())) {
                                        ReminderScheduler.showDateTimePicker(context, currentRelease) { selected ->
                                            releaseDate = selected
                                            reminderEnabled = true
                                        }
                                    }
                                },
                                colors = if (p.isNeoBrutalist) {
                                    SwitchDefaults.colors(
                                        checkedThumbColor = NeoBrutalistColors.Black,
                                        checkedTrackColor = NeoBrutalistColors.Mint,
                                        checkedBorderColor = p.border,
                                        uncheckedThumbColor = p.text,
                                        uncheckedTrackColor = p.chip,
                                        uncheckedBorderColor = p.border
                                    )
                                } else {
                                    SwitchDefaults.colors(checkedThumbColor = contrastOn(p.reminder), checkedTrackColor = p.reminder)
                                }
                            )
                        }
                        if (reminderEnabled || status == WatchStatus.ComingSoon) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    stringResource(R.string.schedule_release_date),
                                    fontSize = 11.sp,
                                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.SemiBold,
                                    color = p.muted
                                )
                                if (p.isNeoBrutalist) {
                                    NeoBrutalistBox(
                                        onClick = {
                                            ReminderScheduler.showDateTimePicker(
                                                context,
                                                releaseDate
                                            ) { selected ->
                                                releaseDate = selected
                                                reminderEnabled = true
                                            }
                                        },
                                        role = Role.Button,
                                        backgroundColor = NeoBrutalistColors.Yellow,
                                        borderColor = p.border,
                                        shadowColor = NeoBrutalistColors.Black,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp,
                                        cornerRadius = 8.dp,
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                stringResource(R.string.pick_release_date),
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeoBrutalistColors.Black
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(p.reminder.copy(alpha = 0.15f))
                                            .border(
                                                1.dp,
                                                p.reminder.copy(alpha = 0.4f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                ReminderScheduler.showDateTimePicker(
                                                    context,
                                                    releaseDate
                                                ) { selected ->
                                                    releaseDate = selected
                                                    reminderEnabled = true
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = p.reminder, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.pick_release_date), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.reminder)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.preset), fontSize = 11.sp, color = p.muted)
                                    val now = System.currentTimeMillis()
                                    PresetChip(stringResource(R.string.preset_tomorrow)) { releaseDate = now + 86400000L }
                                    PresetChip(stringResource(R.string.preset_3_days)) { releaseDate = now + 3 * 86400000L }
                                    PresetChip(stringResource(R.string.preset_1_week)) { releaseDate = now + 7 * 86400000L }
                                    PresetChip(stringResource(R.string.preset_1_month)) { releaseDate = now + 30 * 86400000L }
                                }
                                if (releaseDate != null) {
                                    if (p.isNeoBrutalist) {
                                        NeoBrutalistBox(
                                            onClick = {
                                                ReminderScheduler.showDateTimePicker(
                                                    context,
                                                    releaseDate
                                                ) { selected -> releaseDate = selected }
                                            },
                                            role = Role.Button,
                                            backgroundColor = p.chip,
                                            borderColor = p.border,
                                            shadowColor = NeoBrutalistColors.Black,
                                            borderWidth = 2.dp,
                                            shadowOffset = 3.dp,
                                            cornerRadius = 8.dp,
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.EditCalendar, contentDescription = stringResource(R.string.change_date), tint = p.text, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        ReminderScheduler.formatReleaseDate(context, releaseDate),
                                                        fontSize = 12.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = p.text
                                                    )
                                                    Text(stringResource(R.string.tap_to_change_release), fontSize = 10.sp, color = p.muted)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(NeoBrutalistColors.Yellow)
                                                        .border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        ReminderScheduler.timeUntilRelease(context, releaseDate),
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = NeoBrutalistColors.Black
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(p.chip)
                                                .border(1.dp, p.border, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    ReminderScheduler.showDateTimePicker(
                                                        context,
                                                        releaseDate
                                                    ) { selected -> releaseDate = selected }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.EditCalendar, contentDescription = stringResource(R.string.change_date), tint = p.reminder, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(ReminderScheduler.formatReleaseDate(context, releaseDate), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                                                Text(stringResource(R.string.tap_to_change_release), fontSize = 10.sp, color = p.muted)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(p.reminder.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(ReminderScheduler.timeUntilRelease(context, releaseDate), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.reminder)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Poster: toggle storage/url
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel(stringResource(R.string.poster_url_optional))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PosterSourceChip(stringResource(R.string.poster_source_url), selected = !posterIsStorage) { posterIsStorage = false }
                        PosterSourceChip(stringResource(R.string.poster_source_storage), selected = posterIsStorage) { posterIsStorage = true }
                    }
                    if (!posterIsStorage) {
                        SField(poster, { poster = it }, placeholder = "https://...", keyboardType = KeyboardType.Uri)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (poster.isNotBlank()) {
                                Poster(poster, Modifier.size(width = 56.dp, height = 72.dp))
                                Spacer(Modifier.width(10.dp))
                            }
                            if (p.isNeoBrutalist) {
                                NeoBrutalistBox(
                                    onClick = { posterPicker.launch(arrayOf("image/*")) },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.SkyBlue,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    cornerRadius = 8.dp,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.pick_files),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeoBrutalistColors.Black
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(p.chip)
                                        .border(1.dp, p.border, RoundedCornerShape(12.dp))
                                        .clickable { posterPicker.launch(arrayOf("image/*")) }
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Text(stringResource(R.string.pick_files), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                                }
                            }
                        }
                    }
                }

                if (type == MediaType.Series) {
                    // Multi-season editor
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.seasons),
                                fontSize = 12.sp,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.SemiBold,
                                color = p.muted,
                                modifier = Modifier.weight(1f)
                            )
                            if (p.isNeoBrutalist) {
                                NeoBrutalistBox(
                                    onClick = {
                                        val nextNum =
                                            (seasonsDraft.maxOfOrNull { it.number } ?: 0) + 1
                                        seasonsDraft = (seasonsDraft + SeasonDraft(
                                            nextNum,
                                            "12",
                                            "0"
                                        )).sortedBy { it.number }.toMutableList()
                                    },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.Mint,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                    cornerRadius = 6.dp,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.add_season),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeoBrutalistColors.Black
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(p.chip)
                                        .border(1.dp, p.border, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val nextNum =
                                                (seasonsDraft.maxOfOrNull { it.number } ?: 0) + 1
                                            seasonsDraft = (seasonsDraft + SeasonDraft(
                                                nextNum,
                                                "12",
                                                "0"
                                            )).sortedBy { it.number }.toMutableList()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) { Text(stringResource(R.string.add_season), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.text) }
                            }
                        }

                        seasonsDraft.forEachIndexed { sIdx, draft ->
                            val seasonCardModifier = if (p.isNeoBrutalist) {
                                Modifier
                                    .fillMaxWidth()
                                    .neoBrutalist(
                                        backgroundColor = p.card,
                                        borderColor = p.border,
                                        shadowColor = NeoBrutalistColors.Black,
                                        borderWidth = 2.5.dp,
                                        shadowOffset = 4.dp,
                                        cornerRadius = 8.dp
                                    )
                                    .padding(12.dp)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(18.dp),
                                        tint = p.card,
                                        blurRadius = 12.dp,
                                        refractionHeight = 8.dp,
                                        refractionAmount = 14.dp,
                                        chromaticAberration = false
                                    )
                                    .padding(12.dp)
                            }
                            Column(
                                modifier = seasonCardModifier,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        stringResource(R.string.season_number_label, draft.number),
                                        fontSize = 13.sp,
                                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = p.text,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (seasonsDraft.size > 1) {
                                        if (p.isNeoBrutalist) {
                                            NeoBrutalistBox(
                                                onClick = {
                                                    seasonsDraft =
                                                        seasonsDraft.filter { it.number != draft.number }
                                                            .toMutableList()
                                                },
                                                role = Role.Button,
                                                backgroundColor = NeoBrutalistColors.CoralRed,
                                                borderColor = p.border,
                                                shadowColor = NeoBrutalistColors.Black,
                                                borderWidth = 1.5.dp,
                                                shadowOffset = 2.dp,
                                                cornerRadius = 6.dp,
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    stringResource(R.string.remove_season, draft.number),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NeoBrutalistColors.Black
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(p.danger.copy(alpha = 0.15f))
                                                    .clickable {
                                                        seasonsDraft =
                                                            seasonsDraft.filter { it.number != draft.number }
                                                                .toMutableList()
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) { Text(stringResource(R.string.remove_season, draft.number), fontSize = 10.sp, color = p.danger) }
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        FieldLabel(stringResource(R.string.current_episode))
                                        SField(draft.currentEpStr, { v ->
                                            upsertDraft(sIdx, draft.copy(currentEpStr = v.filter(Char::isDigit).take(4)))
                                        }, keyboardType = KeyboardType.Number)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        FieldLabel(stringResource(R.string.total_episodes))
                                        SField(draft.totalEpStr, { v ->
                                            upsertDraft(sIdx, draft.copy(totalEpStr = v.filter(Char::isDigit).take(4)))
                                        }, keyboardType = KeyboardType.Number)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (p.isNeoBrutalist) {
                                        NeoBrutalistBox(
                                            onClick = {
                                                folderPickerTarget.intValue = sIdx
                                                folderPicker.launch(null)
                                            },
                                            role = Role.Button,
                                            backgroundColor = NeoBrutalistColors.SkyBlue,
                                            borderColor = p.border,
                                            shadowColor = NeoBrutalistColors.Black,
                                            borderWidth = 2.dp,
                                            shadowOffset = 3.dp,
                                            cornerRadius = 8.dp,
                                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                stringResource(R.string.pick_folder),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeoBrutalistColors.Black
                                            )
                                        }
                                        NeoBrutalistBox(
                                            onClick = {
                                                multiPickerTarget.intValue = sIdx
                                                multiPicker.launch(arrayOf("video/*"))
                                            },
                                            role = Role.Button,
                                            backgroundColor = NeoBrutalistColors.Mint,
                                            borderColor = p.border,
                                            shadowColor = NeoBrutalistColors.Black,
                                            borderWidth = 2.dp,
                                            shadowOffset = 3.dp,
                                            cornerRadius = 8.dp,
                                            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                stringResource(R.string.pick_files),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeoBrutalistColors.Black
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(p.chip)
                                                .border(1.dp, p.border, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    folderPickerTarget.intValue = sIdx
                                                    folderPicker.launch(null)
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) { Text(stringResource(R.string.pick_folder), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.text) }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(p.chip)
                                                .border(1.dp, p.border, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    multiPickerTarget.intValue = sIdx
                                                    multiPicker.launch(arrayOf("video/*"))
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) { Text(stringResource(R.string.pick_files), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.text) }
                                    }
                                }
                                // Episode list — paged for long shows (e.g. 1000+ eps), direct for small ones.
                                val total = (draft.totalEpStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                                val pickedCount = remember(draft.episodes, total) {
                                    draft.episodes.count { it.key in 1..total }
                                }
                                val missingCount = total - pickedCount
                                val showMissingOnly = showMissingOnlyBySeason[draft.number] == true
                                val visibleTotal = if (showMissingOnly) missingCount else total
                                val isPaged = visibleTotal > EPISODES_UNPAGED_LIMIT

                                // Summary + missing-only filter (lets user fix gaps without scrolling 1000 rows)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        stringResource(R.string.episode_picked_summary, pickedCount, total),
                                        fontSize = 11.sp,
                                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                        fontWeight = FontWeight.Bold,
                                        color = p.text
                                    )
                                    if (draft.episodes.isNotEmpty() && missingCount in 1..(total - 1)) {
                                        val chipSelected = showMissingOnly
                                        if (p.isNeoBrutalist) {
                                            NeoBrutalistBox(
                                                onClick = {
                                                    showMissingOnlyBySeason[draft.number] =
                                                        !chipSelected
                                                    episodePageBySeason[draft.number] = 0
                                                },
                                                role = Role.Button,
                                                backgroundColor = if (chipSelected) NeoBrutalistColors.Yellow else p.chip,
                                                borderColor = p.border,
                                                shadowColor = NeoBrutalistColors.Black,
                                                borderWidth = 1.5.dp,
                                                shadowOffset = 2.dp,
                                                cornerRadius = 6.dp,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    stringResource(R.string.episode_missing_only),
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (chipSelected) NeoBrutalistColors.Black else p.text
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(100.dp))
                                                    .background(if (chipSelected) p.primary else p.chip)
                                                    .border(
                                                        1.dp,
                                                        if (chipSelected) p.primary else p.border,
                                                        RoundedCornerShape(100.dp)
                                                    )
                                                    .clickable {
                                                        showMissingOnlyBySeason[draft.number] =
                                                            !chipSelected
                                                        episodePageBySeason[draft.number] = 0
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    stringResource(R.string.episode_missing_only),
                                                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                                    color = if (chipSelected) p.onAccent else p.muted
                                                )
                                            }
                                        }
                                    }
                                }
                                if (missingCount > 0 && draft.episodes.isNotEmpty()) {
                                    Text(
                                        stringResource(R.string.episode_not_matched),
                                        fontSize = 10.sp, color = p.muted
                                    )
                                }

                                val missingEps = if (showMissingOnly) {
                                    remember(draft.episodes, total) {
                                        (1..total).filter { it !in draft.episodes }
                                    }
                                } else emptyList()

                                val pageCount = if (visibleTotal <= 0) 1 else (visibleTotal + EPISODES_PAGE_SIZE - 1) / EPISODES_PAGE_SIZE
                                val rawPage = episodePageBySeason[draft.number] ?: 0
                                val safePage = rawPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))

                                val pageEps: List<Int> = if (isPaged) {
                                    if (showMissingOnly) {
                                        val from = (safePage * EPISODES_PAGE_SIZE).coerceIn(0, missingEps.size)
                                        val to = (from + EPISODES_PAGE_SIZE).coerceAtMost(missingEps.size)
                                        if (from >= to) emptyList() else missingEps.subList(from, to)
                                    } else {
                                        val from = safePage * EPISODES_PAGE_SIZE + 1
                                        val to = (from + EPISODES_PAGE_SIZE - 1).coerceAtMost(total)
                                        (from..to).toList()
                                    }
                                } else {
                                    if (showMissingOnly) missingEps else (1..total).toList()
                                }

                                if (isPaged) {
                                    val gotoText = gotoEpBySeason[draft.number] ?: ""
                                    val goToPage: () -> Unit = {
                                        val target = gotoText.toIntOrNull()
                                        if (target != null) {
                                            val clamped = target.coerceIn(1, total)
                                            val destPage = if (showMissingOnly) {
                                                val idx = missingEps.indexOf(clamped)
                                                if (idx >= 0) idx / EPISODES_PAGE_SIZE else safePage
                                            } else {
                                                (clamped - 1) / EPISODES_PAGE_SIZE
                                            }
                                            episodePageBySeason[draft.number] = destPage
                                            gotoEpBySeason[draft.number] = ""
                                        }
                                    }
                                    // Pager controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        EpisodePagerButton(
                                            label = stringResource(R.string.episode_prev),
                                            enabled = safePage > 0
                                        ) { episodePageBySeason[draft.number] = (safePage - 1).coerceAtLeast(0) }
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                if (pageEps.isEmpty()) "0 of $visibleTotal"
                                                else stringResource(
                                                    R.string.episode_page_range,
                                                    pageEps.first(), pageEps.last(), visibleTotal
                                                ),
                                                fontSize = 11.sp,
                                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                                fontWeight = FontWeight.Bold,
                                                color = p.text
                                            )
                                            Text(
                                                stringResource(R.string.episode_page_label, safePage + 1, pageCount),
                                                fontSize = 9.sp,
                                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                                color = p.muted
                                            )
                                        }
                                        EpisodePagerButton(
                                            label = stringResource(R.string.episode_next),
                                            enabled = safePage < pageCount - 1
                                        ) { episodePageBySeason[draft.number] = (safePage + 1).coerceAtMost((pageCount - 1).coerceAtLeast(0)) }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SField(
                                            value = gotoText,
                                            onChange = { gotoEpBySeason[draft.number] = it.filter(Char::isDigit).take(4) },
                                            placeholder = stringResource(R.string.episode_go_to_hint),
                                            keyboardType = KeyboardType.Number,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (p.isNeoBrutalist) {
                                            NeoBrutalistBox(
                                                onClick = goToPage,
                                                enabled = gotoText.isNotBlank(),
                                                role = Role.Button,
                                                backgroundColor = if (gotoText.isNotBlank()) NeoBrutalistColors.Yellow else p.chip,
                                                borderColor = p.border,
                                                shadowColor = NeoBrutalistColors.Black,
                                                borderWidth = 2.dp,
                                                shadowOffset = 3.dp,
                                                cornerRadius = 8.dp,
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    stringResource(R.string.episode_go),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NeoBrutalistColors.Black
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(p.primary)
                                                    .clickable(
                                                        enabled = gotoText.isNotBlank(),
                                                        onClick = goToPage
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(stringResource(R.string.episode_go), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.onAccent)
                                            }
                                        }
                                    }
                                }

                                if (pageEps.isEmpty() && showMissingOnly) {
                                    Text(stringResource(R.string.episode_all_picked), fontSize = 11.sp, color = p.success)
                                } else {
                                    // Only the visible page is composed — 50 rows max even for 1000+ ep shows.
                                    pageEps.forEach { ep ->
                                        EpisodeAssignRow(
                                            ep = ep,
                                            file = draft.episodes[ep],
                                            onPick = {
                                                manualTarget = sIdx to ep
                                                picker.launch(arrayOf("video/*"))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    FieldLabel(stringResource(R.string.streaming_link_optional))
                    SField(streamUrl, { streamUrl = it }, placeholder = "https://www.netflix.com/title/...", keyboardType = KeyboardType.Uri)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.preset), fontSize = 11.sp, color = p.muted)
                        PresetChip("Netflix") { streamUrl = "https://www.netflix.com" }
                        PresetChip("Bilibili") { streamUrl = "https://www.bilibili.tv" }
                        PresetChip("YouTube") { streamUrl = "https://www.youtube.com" }
                    }
                }

                if (type == MediaType.Movie) {
                    Column {
                        FieldLabel(stringResource(R.string.offline_file_optional))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SField(videoPath, { videoPath = it }, placeholder = stringResource(R.string.video_name_placeholder), modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            if (p.isNeoBrutalist) {
                                NeoBrutalistBox(
                                    onClick = {
                                        manualTarget = null; picker.launch(arrayOf("video/*"))
                                    },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.SkyBlue,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    cornerRadius = 8.dp,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.FolderOpen, null, tint = NeoBrutalistColors.Black, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            stringResource(R.string.pick_file),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeoBrutalistColors.Black
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(p.chip)
                                        .border(1.dp, p.border, RoundedCornerShape(12.dp))
                                        .clickable {
                                            manualTarget = null; picker.launch(arrayOf("video/*"))
                                        }
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.FolderOpen, null, tint = p.primary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.pick_file), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                                }
                            }
                        }
                    }
                }

                error?.let { Text(it, color = p.danger, fontSize = 12.sp, fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal) }

                val performSave = {
                    error = null
                    if (title.isBlank()) {
                        error = context.getString(R.string.title_required)
                    } else if (existing.any { other ->
                            other.id != initial?.id && isDuplicateOf(other)
                        }) {
                        error = context.getString(R.string.duplicate_media)
                    } else {
                        val base = initial ?: MediaItem(
                            id = UUID.randomUUID().toString(),
                            title = "",
                            type = type,
                            status = status,
                            poster = ""
                        )
                        val builtSeasons: List<Season> = if (type == MediaType.Series) {
                            seasonsDraft.sortedBy { it.number }.map { d ->
                                val tot =
                                    (d.totalEpStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                                val cur =
                                    (d.currentEpStr.toIntOrNull() ?: 0).coerceIn(0, tot)
                                Season(
                                    number = d.number,
                                    currentEp = cur,
                                    totalEp = tot,
                                    episodes = d.episodes.values.sortedBy { it.number })
                            }
                        } else emptyList()
                        val legacyTotal =
                            if (type == MediaType.Movie) 1 else builtSeasons.sumOf { it.totalEp }
                                .coerceAtLeast(1)
                        val legacyCurrent =
                            if (type == MediaType.Movie) (initial?.currentEp
                                ?: 1) else builtSeasons.sumOf { it.currentEp }
                                .coerceAtLeast(0)
                        val firstFileUri =
                            builtSeasons.flatMap { it.episodes }.firstOrNull()?.uri
                                ?: videoUri
                        val firstFilePath = builtSeasons.flatMap { it.episodes }
                            .firstOrNull()?.displayName ?: videoPath
                        onSave(
                            base.copy(
                                title = title.trim(),
                                type = type,
                                status = status,
                                poster = poster.trim(),
                                description = description.trim(),
                                source = source,
                                sourceId = sourceId,
                                season = builtSeasons.firstOrNull()?.number
                                    ?: base.season,
                                currentEp = legacyCurrent,
                                totalEp = legacyTotal,
                                seasons = builtSeasons,
                                streamUrl = streamUrl.trim(),
                                videoPath = firstFilePath.trim(),
                                videoUri = firstFileUri,
                                releaseDate = if (status == WatchStatus.ComingSoon || reminderEnabled) releaseDate else null,
                                reminderEnabled = reminderEnabled && (releaseDate != null),
                                tags = tags,
                                rating = rating,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = onDismiss,
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.OffWhite,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                color = NeoBrutalistColors.Black,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        NeoBrutalistBox(
                            onClick = performSave,
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.Yellow,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                stringResource(R.string.save),
                                color = NeoBrutalistColors.Black,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Box(
                            Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    tint = p.chip,
                                    blurRadius = 12.dp,
                                    refractionHeight = 8.dp,
                                    refractionAmount = 14.dp,
                                    chromaticAberration = false
                                )
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) { Text(stringResource(R.string.cancel), color = p.text.copy(alpha = 0.8f), fontSize = 14.sp) }

                        Box(
                            Modifier
                                .liquidGlass(
                                    shape = CircleShape,
                                    tint = p.primary.copy(alpha = if (p.isGlass) 0.82f else 1f),
                                    blurRadius = 12.dp,
                                    refractionHeight = 8.dp,
                                    refractionAmount = 16.dp,
                                    chromaticAberration = false
                                )
                                .clickable(onClick = performSave)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) { Text(stringResource(R.string.save), color = p.onAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            onClick = onClick,
            role = Role.Button,
            backgroundColor = p.chip,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 1.5.dp,
            shadowOffset = 2.dp,
            cornerRadius = 6.dp,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                label,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.text
            )
        }
    } else {
        Box(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(p.chip)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) { Text(label, fontSize = 11.sp, color = p.text) }
    }
}

@Composable
private fun EpisodePagerButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            onClick = onClick,
            enabled = enabled,
            role = Role.Button,
            backgroundColor = if (enabled) NeoBrutalistColors.OffWhite else p.chip,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.dp,
            shadowOffset = if (enabled) 2.dp else 0.dp,
            cornerRadius = 6.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (enabled) NeoBrutalistColors.Black else p.muted
            )
        }
    } else {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled) p.chip else p.chip.copy(alpha = 0.5f))
                .border(1.dp, p.border, RoundedCornerShape(8.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) p.text else p.muted
            )
        }
    }
}

@Composable
private fun EpisodeAssignRow(ep: Int, file: EpisodeFile?, onPick: () -> Unit) {
    val p = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 8.dp))
            .background(p.chip)
            .then(
                if (p.isNeoBrutalist) {
                    Modifier.border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "E$ep",
            fontSize = 11.sp,
            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.ExtraBold,
            color = p.text,
            modifier = Modifier.width(48.dp)
        )
        Text(
            file?.displayName ?: stringResource(R.string.episode_missing),
            fontSize = 11.sp,
            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
            color = if (file != null) p.success else p.muted,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        if (file != null) {
            Text(
                stringResource(R.string.episode_picked_ok),
                fontSize = 9.sp,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.success,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        if (p.isNeoBrutalist) {
            NeoBrutalistBox(
                onClick = onPick,
                role = Role.Button,
                backgroundColor = if (file != null) NeoBrutalistColors.Mint else NeoBrutalistColors.Yellow,
                borderColor = p.border,
                shadowColor = NeoBrutalistColors.Black,
                borderWidth = 1.5.dp,
                shadowOffset = 2.dp,
                cornerRadius = 6.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (file != null) "✓" else "+",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeoBrutalistColors.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (file != null) p.success.copy(alpha = 0.15f) else p.primary.copy(
                            alpha = 0.15f
                        )
                    )
                    .clickable(onClick = onPick)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) { Text(if (file != null) "✓" else "+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (file != null) p.success else p.primary) }
        }
    }
}

@Composable
private fun PosterSourceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            onClick = onClick,
            role = Role.Tab,
            backgroundColor = if (selected) NeoBrutalistColors.Yellow else p.chip,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.dp,
            shadowOffset = if (selected) 2.dp else 1.dp,
            cornerRadius = 6.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                label,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) NeoBrutalistColors.Black else p.text
            )
        }
    } else {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) p.primary else p.chip)
                .border(
                    1.dp,
                    if (selected) p.primary else p.border,
                    RoundedCornerShape(100.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) p.onAccent else p.muted) }
    }
}
