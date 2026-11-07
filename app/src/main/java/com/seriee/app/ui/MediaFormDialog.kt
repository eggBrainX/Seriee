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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.R
import com.seriee.app.data.EpisodeFile
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaType
import com.seriee.app.data.MetadataRepository
import com.seriee.app.data.MetadataResult
import com.seriee.app.data.PosterStore
import com.seriee.app.data.Season
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler
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

@Composable
fun MediaFormDialog(
    initial: MediaItem?,
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

    val tmdbKey = remember {
        context.getSharedPreferences("seriee", android.content.Context.MODE_PRIVATE)
            .getString("tmdb_api_key", "") ?: ""
    }

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
                                val docId = cursor.getString(0)
                                val mime = cursor.getString(2)
                                if (mime != null && mime.startsWith("video/")) {
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
            val res = metadataRepo.search(q, tmdbKey)
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
        if (r.posterUrl.isNotBlank()) {
            poster = r.posterUrl
            posterIsStorage = false
        }
        description = r.description
        source = r.source
        sourceId = r.sourceId
        if (r.tags.isNotEmpty()) tags = r.tags
        if (r.seasonEpisodeCounts.isNotEmpty()) {
            val drafts = r.seasonEpisodeCounts.mapIndexed { idx, total ->
                val existing = seasonsDraft.getOrNull(idx)
                val num = existing?.number ?: (idx + 1)
                SeasonDraft(num, total.toString(), existing?.currentEpStr ?: "0", existing?.episodes ?: emptyMap())
            }.toMutableList()
            seasonsDraft = drafts
        }
        if (r.streamCandidates.isNotEmpty()) {
            if (streamUrl.isBlank()) streamUrl = r.streamCandidates.first().url
        }
        fetchResults = emptyList()
        fetchError = null
        Toast.makeText(context, context.getString(R.string.fetch_applied), Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = p.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, p.border)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (initial == null) stringResource(R.string.add_to_seriee) else stringResource(R.string.edit_media),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.text,
                        modifier = Modifier.weight(1f)
                    )
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

                // Fetch metadata
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FieldLabel(stringResource(R.string.fetch_metadata_title))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SField(fetchQuery, { fetchQuery = it }, placeholder = stringResource(R.string.fetch_search_hint), modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SerieColors.Blue600)
                                .clickable(enabled = !fetchLoading) { doFetch() }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            if (fetchLoading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            else Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.fetch_search_button), tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (fetchLoading) {
                        Text(stringResource(R.string.fetch_loading), fontSize = 11.sp, color = p.muted)
                    }
                    fetchError?.let { Text(it, color = SerieColors.Red, fontSize = 12.sp) }
                    if (hasSearched && !fetchLoading && fetchError == null && fetchResults.isEmpty()) {
                        Text(stringResource(R.string.fetch_no_results), fontSize = 12.sp, color = p.muted)
                    }
                    fetchResults.forEach { r ->
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
                                Text("${r.source} ${r.year}".trim(), fontSize = 11.sp, color = p.muted)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SerieColors.Blue600.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) { Text(r.source, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SerieColors.Blue400) }
                        }
                    }
                    // stream candidates from last fetch are applied directly; user can edit streamUrl below
                }

                Column {
                    FieldLabel(stringResource(R.string.title_label))
                    SField(title, { title = it }, placeholder = stringResource(R.string.title_placeholder))
                }

                // Description
                Column {
                    FieldLabel(stringResource(R.string.description_label))
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
                            cursorColor = SerieColors.Blue500,
                            focusedBorderColor = SerieColors.Blue500,
                            unfocusedBorderColor = p.border
                        )
                    )
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
                                if (releaseDate == null || releaseDate!! <= System.currentTimeMillis()) {
                                    releaseDate = System.currentTimeMillis() + 3 * 86400000L
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FieldLabel(stringResource(R.string.rating_label))
                    StarRatingBar(rating = rating, onRatingChange = { rating = it }, starSize = 26.dp)
                }

                TagInputField(tags = tags, onTagsChanged = { tags = it })

                val showReminderSection = status == WatchStatus.ComingSoon || status == WatchStatus.PlanToWatch || releaseDate != null
                if (showReminderSection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (status == WatchStatus.ComingSoon) SerieColors.Orange.copy(alpha = 0.08f) else p.card)
                            .border(1.dp, if (status == WatchStatus.ComingSoon) SerieColors.Orange.copy(alpha = 0.35f) else p.border, RoundedCornerShape(16.dp))
                            .padding(14.dp),
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
                                    tint = if (reminderEnabled) SerieColors.Orange else p.muted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(stringResource(R.string.release_reminder_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
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
                                    if (it && releaseDate == null) releaseDate = System.currentTimeMillis() + 3 * 86400000L
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SerieColors.Orange)
                            )
                        }
                        if (reminderEnabled || status == WatchStatus.ComingSoon) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.schedule_release_date), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.muted)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SerieColors.Orange.copy(alpha = 0.15f))
                                        .border(1.dp, SerieColors.Orange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            ReminderScheduler.showDateTimePicker(context, releaseDate) { selected ->
                                                releaseDate = selected
                                                reminderEnabled = true
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = SerieColors.Orange, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.pick_release_date), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SerieColors.Orange)
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(p.chip)
                                            .border(1.dp, p.border, RoundedCornerShape(10.dp))
                                            .clickable {
                                                ReminderScheduler.showDateTimePicker(context, releaseDate) { selected -> releaseDate = selected }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.EditCalendar, contentDescription = stringResource(R.string.change_date), tint = SerieColors.Orange, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(ReminderScheduler.formatReleaseDate(context, releaseDate), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                                            Text(stringResource(R.string.tap_to_change_release), fontSize = 10.sp, color = p.muted)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SerieColors.Orange.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(ReminderScheduler.timeUntilRelease(context, releaseDate), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SerieColors.Orange)
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

                if (type == MediaType.Series) {
                    // Multi-season editor
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.season),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.muted,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(p.chip)
                                    .border(1.dp, p.border, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextNum = (seasonsDraft.maxOfOrNull { it.number } ?: 0) + 1
                                        seasonsDraft = (seasonsDraft + SeasonDraft(nextNum, "12", "0")).sortedBy { it.number }.toMutableList()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) { Text(stringResource(R.string.add_season), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.text) }
                        }

                        seasonsDraft.forEachIndexed { sIdx, draft ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(p.card)
                                    .border(1.dp, p.border, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        stringResource(R.string.season_number_label, draft.number),
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (seasonsDraft.size > 1) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SerieColors.Red.copy(alpha = 0.15f))
                                                .clickable {
                                                    seasonsDraft = seasonsDraft.filter { it.number != draft.number }.toMutableList()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) { Text(stringResource(R.string.remove_season, draft.number), fontSize = 10.sp, color = SerieColors.Red) }
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
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.text
                                    )
                                    if (draft.episodes.isNotEmpty() && missingCount in 1..(total - 1)) {
                                        val chipSelected = showMissingOnly
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(if (chipSelected) SerieColors.Blue600 else p.chip)
                                                .border(1.dp, if (chipSelected) SerieColors.Blue600 else p.border, RoundedCornerShape(100.dp))
                                                .clickable {
                                                    showMissingOnlyBySeason[draft.number] = !chipSelected
                                                    episodePageBySeason[draft.number] = 0
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.episode_missing_only),
                                                fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                                color = if (chipSelected) Color.White else p.muted
                                            )
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
                                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.text
                                            )
                                            Text(
                                                stringResource(R.string.episode_page_label, safePage + 1, pageCount),
                                                fontSize = 9.sp, color = p.muted
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
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SerieColors.Blue600)
                                                .clickable(enabled = gotoText.isNotBlank(), onClick = goToPage)
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(stringResource(R.string.episode_go), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                if (pageEps.isEmpty() && showMissingOnly) {
                                    Text(stringResource(R.string.episode_all_picked), fontSize = 11.sp, color = SerieColors.Emerald)
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
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(p.chip)
                                    .border(1.dp, p.border, RoundedCornerShape(12.dp))
                                    .clickable { manualTarget = null; picker.launch(arrayOf("video/*")) }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.FolderOpen, null, tint = SerieColors.Blue400, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.pick_file), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                            }
                        }
                    }
                }

                error?.let { Text(it, color = SerieColors.Red, fontSize = 12.sp) }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.chip)
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 18.dp, vertical = 11.dp)
                    ) { Text(stringResource(R.string.cancel), color = p.muted, fontSize = 14.sp) }

                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600)
                            .clickable {
                                if (title.isBlank()) {
                                    error = context.getString(R.string.title_required)
                                } else {
                                    val base = initial ?: MediaItem(id = System.currentTimeMillis().toString(), title = "", type = type, status = status, poster = "")
                                    val builtSeasons: List<Season> = if (type == MediaType.Series) {
                                        seasonsDraft.sortedBy { it.number }.map { d ->
                                            val tot = (d.totalEpStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                                            val cur = (d.currentEpStr.toIntOrNull() ?: 0).coerceIn(0, tot)
                                            Season(number = d.number, currentEp = cur, totalEp = tot, episodes = d.episodes.values.sortedBy { it.number })
                                        }
                                    } else emptyList()
                                    val legacyTotal = if (type == MediaType.Movie) 1 else builtSeasons.sumOf { it.totalEp }.coerceAtLeast(1)
                                    val legacyCurrent = if (type == MediaType.Movie) (initial?.currentEp ?: 1) else builtSeasons.sumOf { it.currentEp }.coerceAtLeast(0)
                                    val firstFileUri = builtSeasons.flatMap { it.episodes }.firstOrNull()?.uri ?: videoUri
                                    val firstFilePath = builtSeasons.flatMap { it.episodes }.firstOrNull()?.displayName ?: videoPath
                                    onSave(
                                        base.copy(
                                            title = title.trim(),
                                            type = type,
                                            status = status,
                                            poster = poster.trim(),
                                            description = description.trim(),
                                            source = source,
                                            sourceId = sourceId,
                                            season = builtSeasons.firstOrNull()?.number ?: base.season,
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
                            .padding(horizontal = 22.dp, vertical = 11.dp)
                    ) { Text(stringResource(R.string.save), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(p.chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) { Text(label, fontSize = 11.sp, color = p.text) }
}

@Composable
private fun EpisodePagerButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
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

@Composable
private fun EpisodeAssignRow(ep: Int, file: EpisodeFile?, onPick: () -> Unit) {
    val p = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(p.chip)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("E$ep", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.text, modifier = Modifier.width(48.dp))
        Text(
            file?.displayName ?: stringResource(R.string.episode_missing),
            fontSize = 11.sp, color = if (file != null) SerieColors.Emerald else p.muted,
            maxLines = 1, modifier = Modifier.weight(1f)
        )
        if (file != null) {
            Text(
                stringResource(R.string.episode_picked_ok),
                fontSize = 9.sp, color = SerieColors.Emerald,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (file != null) SerieColors.Emerald.copy(alpha = 0.15f) else SerieColors.Blue600.copy(alpha = 0.15f))
                .clickable(onClick = onPick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) { Text(if (file != null) "✓" else "+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (file != null) SerieColors.Emerald else SerieColors.Blue400) }
    }
}

@Composable
private fun PosterSourceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) SerieColors.Blue600 else p.chip)
            .border(1.dp, if (selected) SerieColors.Blue600 else p.border, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else p.muted) }
}
