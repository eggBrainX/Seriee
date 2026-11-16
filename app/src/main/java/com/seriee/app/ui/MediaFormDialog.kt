package com.seriee.app.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.EpisodeFile
import com.seriee.app.data.MediaItem
import com.seriee.app.data.MediaMetadata
import com.seriee.app.data.MediaType
import com.seriee.app.data.MetadataRepository
import com.seriee.app.data.PosterStore
import com.seriee.app.data.Season
import com.seriee.app.data.WatchStatus
import com.seriee.app.reminder.ReminderScheduler
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MediaFormDialog(
    initial: MediaItem?,
    existingTitles: Set<String>,
    tmdbKey: String,
    omdbKey: String,
    onDismiss: () -> Unit,
    onSave: (MediaItem) -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val itemId = remember(initial) { initial?.id ?: UUID.randomUUID().toString() }

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: MediaType.Series) }
    var status by remember { mutableStateOf(initial?.status ?: WatchStatus.Watching) }
    var rating by remember { mutableIntStateOf(initial?.rating ?: 0) }
    var poster by remember { mutableStateOf(initial?.poster ?: "") }
    var streamUrl by remember { mutableStateOf(initial?.streamUrl ?: "") }
    var videoPath by remember { mutableStateOf(initial?.videoPath ?: "") }
    var videoUri by remember { mutableStateOf(initial?.videoUri ?: "") }
    var tags by remember { mutableStateOf(initial?.tags ?: emptyList()) }
    var releaseDate by remember { mutableStateOf(initial?.releaseDate) }
    var reminderEnabled by remember { mutableStateOf(initial?.reminderEnabled ?: false) }
    var source by remember { mutableStateOf(initial?.source ?: "") }
    var sourceId by remember { mutableStateOf(initial?.sourceId ?: "") }

    var seasons by remember {
        mutableStateOf(
            if (initial == null) listOf(Season(1, 0, 12))
            else initial.effectiveSeasons.ifEmpty { listOf(Season(1, 0, 12)) }
        )
    }

    var searchLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<MediaMetadata>>(emptyList()) }
    var bulkSeasonTarget by remember { mutableIntStateOf(1) }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val saved = PosterStore.savePosterFromUri(context, uri, itemId)
            if (saved != null) poster = saved
        }
    }

    val singleVideoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            videoUri = uri.toString()
            videoPath = displayName(context, uri)
        }
    }

    val bulkFilesPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        seasons = seasons.map { sec ->
            if (sec.number != bulkSeasonTarget) sec
            else {
                val map = sec.episodes.associateBy { it.number }.toMutableMap()
                var unmatched = 0
                for (u in uris) {
                    runCatching { context.contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    val name = displayName(context, u)
                    val epNum = EpisodeFileParser.extractEpisodeNumber(name)
                    if (epNum != null) {
                        map[epNum] = EpisodeFile(epNum, u.toString(), name)
                    } else {
                        unmatched++
                    }
                }
                if (unmatched > 0) {
                    Toast.makeText(context, context.getString(R.string.episode_not_matched), Toast.LENGTH_SHORT).show()
                }
                val nextTotal = maxOf(sec.totalEp, map.keys.maxOrNull() ?: sec.totalEp)
                sec.copy(totalEp = nextTotal, episodes = map.values.sortedBy { it.number })
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.surface,
        contentColor = p.text,
        dragHandle = { SheetDragHandle() },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(if (initial == null) R.string.add_to_seriee else R.string.edit_media),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.text,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_label)) },
                    placeholder = { Text(stringResource(R.string.title_placeholder), color = p.muted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val q = title.trim()
                        if (q.isEmpty()) {
                            Toast.makeText(context, context.getString(R.string.fetch_search_hint), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        searchLoading = true
                        scope.launch {
                            val res = MetadataRepository.searchAll(q, tmdbKey, omdbKey)
                            searchLoading = false
                            searchResults = res
                            if (res.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.fetch_no_results), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    enabled = !searchLoading,
                ) {
                    if (searchLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = p.onAccent)
                    } else {
                        Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.fetch_search_button), modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (searchResults.isNotEmpty()) {
                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.fetch_metadata_title), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.muted)
                        searchResults.take(5).forEach { meta ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(p.card)
                                    .clickable {
                                        title = meta.title
                                        type = meta.type
                                        if (meta.posterUrl.isNotBlank()) poster = meta.posterUrl
                                        if (meta.description.isNotBlank()) description = meta.description
                                        if (meta.rating5 > 0) rating = meta.rating5
                                        if (meta.tags.isNotEmpty()) tags = meta.tags
                                        if (meta.streamUrl.isNotBlank()) streamUrl = meta.streamUrl
                                        if (meta.releaseDate != null) {
                                            releaseDate = meta.releaseDate
                                            reminderEnabled = true
                                        }
                                        source = meta.source
                                        sourceId = meta.sourceId
                                        if (meta.type == MediaType.Series) {
                                            seasons = listOf(Season(meta.seasonNumber ?: 1, 0, meta.totalEp.coerceAtLeast(1)))
                                        }
                                        searchResults = emptyList()
                                        Toast.makeText(context, context.getString(R.string.fetch_applied), Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp),
                            ) {
                                Poster(poster = meta.posterUrl, type = meta.type, title = meta.title, modifier = Modifier.size(36.dp, 50.dp), cornerRadius = 6.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(meta.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${meta.source.uppercase()} · ${stringResource(meta.type.labelRes)}", fontSize = 11.sp, color = p.muted)
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                maxLines = 3,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.type_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediaType.entries.forEach { t ->
                        FilterChipButton(
                            label = stringResource(t.labelRes),
                            selected = type == t,
                            accent = p.accent,
                            onClick = { type = t },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.status_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatchStatus.entries.forEach { st ->
                        FilterChipButton(
                            label = stringResource(st.labelRes),
                            selected = status == st,
                            accent = statusColor(st, p),
                            onClick = { status = st },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.rating_label), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                StarRatingBar(rating = rating, onRatingChanged = { rating = it })
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = poster,
                    onValueChange = { poster = it },
                    label = { Text(stringResource(R.string.poster_url_optional)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = { posterPicker.launch(arrayOf("image/*")) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, p.border),
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = stringResource(R.string.poster_source_storage), tint = p.text)
                }
            }

            if (type == MediaType.Series) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.seasons), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text, modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = {
                                val nextNum = (seasons.maxOfOrNull { it.number } ?: 0) + 1
                                seasons = seasons + Season(nextNum, 0, 12)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, p.border),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = p.text, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.add_season), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    seasons.forEachIndexed { idx, sec ->
                        SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.season_number_label, sec.number),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = p.text,
                                        modifier = Modifier.weight(1f),
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            bulkSeasonTarget = sec.number
                                            bulkFilesPicker.launch(arrayOf("video/*"))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, p.border),
                                    ) {
                                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = p.text, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${stringResource(R.string.pick_files)} (${sec.episodes.size})", fontSize = 11.sp, color = p.text)
                                    }
                                    if (seasons.size > 1) {
                                        IconButton(
                                            onClick = { seasons = seasons.filterIndexed { i, _ -> i != idx } },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.remove_season, sec.number), tint = p.danger, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = sec.currentEp.toString(),
                                        onValueChange = { v ->
                                            val n = v.filter { it.isDigit() }.toIntOrNull() ?: 0
                                            seasons = seasons.mapIndexed { i, s -> if (i == idx) s.copy(currentEp = n.coerceIn(0, s.totalEp.coerceAtLeast(1))) else s }
                                        },
                                        label = { Text(stringResource(R.string.current_episode)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                    OutlinedTextField(
                                        value = sec.totalEp.toString(),
                                        onValueChange = { v ->
                                            val n = (v.filter { it.isDigit() }.toIntOrNull() ?: 1).coerceAtLeast(1)
                                            seasons = seasons.mapIndexed { i, s -> if (i == idx) s.copy(totalEp = n, currentEp = s.currentEp.coerceAtMost(n)) else s }
                                        },
                                        label = { Text(stringResource(R.string.total_episodes)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.release_reminder_title), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                            Text(
                                text = if (reminderEnabled) stringResource(R.string.reminder_on_desc) else stringResource(R.string.reminder_off_desc),
                                fontSize = 11.sp,
                                color = p.muted,
                            )
                        }
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                ReminderScheduler.showDateTimePicker(context, releaseDate) { ms ->
                                    releaseDate = ms
                                    reminderEnabled = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, p.border),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = p.text, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(ReminderScheduler.formatReleaseDate(context, releaseDate), color = p.text, fontSize = 12.sp)
                        }
                        if (releaseDate != null) {
                            IconButton(onClick = { releaseDate = null; reminderEnabled = false }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear), tint = p.muted)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = streamUrl,
                onValueChange = { streamUrl = it },
                label = { Text(stringResource(R.string.streaming_link_optional)) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = videoPath,
                    onValueChange = { videoPath = it },
                    label = { Text(stringResource(R.string.offline_file_optional)) },
                    placeholder = { Text(stringResource(R.string.video_name_placeholder), color = p.muted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = { singleVideoPicker.launch(arrayOf("video/*")) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, p.border),
                ) {
                    Text(stringResource(R.string.pick_file), color = p.text, fontWeight = FontWeight.Bold)
                }
            }

            TagSelector(tags = tags, onTagsChanged = { tags = it })

            Button(
                onClick = {
                    val cleanTitle = title.trim()
                    if (cleanTitle.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.title_required), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (initial == null && existingTitles.any { it.equals(cleanTitle, ignoreCase = true) }) {
                        Toast.makeText(context, context.getString(R.string.duplicate_media), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val finalSeasons = if (type == MediaType.Movie) emptyList() else seasons
                    val firstSeason = finalSeasons.firstOrNull()
                    val item = MediaItem(
                        id = itemId,
                        title = cleanTitle,
                        type = type,
                        status = status,
                        poster = poster.trim(),
                        season = firstSeason?.number ?: 1,
                        currentEp = if (type == MediaType.Movie) (if (status == WatchStatus.Completed) 1 else (initial?.currentEp ?: 0)) else finalSeasons.sumOf { it.currentEp },
                        totalEp = if (type == MediaType.Movie) 1 else finalSeasons.sumOf { it.totalEp.coerceAtLeast(1) }.coerceAtLeast(1),
                        pinned = initial?.pinned ?: false,
                        streamUrl = streamUrl.trim(),
                        videoPath = videoPath.trim(),
                        videoUri = videoUri,
                        releaseDate = releaseDate,
                        reminderEnabled = reminderEnabled && releaseDate != null,
                        tags = tags,
                        rating = rating,
                        updatedAt = System.currentTimeMillis(),
                        seasons = finalSeasons,
                        description = description.trim(),
                        source = source,
                        sourceId = sourceId,
                    )
                    onSave(item)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(stringResource(R.string.save), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
