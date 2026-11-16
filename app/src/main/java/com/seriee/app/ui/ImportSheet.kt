package com.seriee.app.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import com.seriee.app.data.ImportMapper
import com.seriee.app.data.ImportMode
import com.seriee.app.data.ImportSource
import com.seriee.app.data.ImportSummary
import com.seriee.app.data.ImportedEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    viewModel: SerieeViewModel,
    onDismiss: () -> Unit,
    onViewLibrary: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var source by remember { mutableStateOf(ImportSource.ANILIST) }
    var mode by remember { mutableStateOf(ImportMode.MERGE) }
    var username by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var entries by remember { mutableStateOf<List<ImportedEntry>?>(null) }
    var summary by remember { mutableStateOf<ImportSummary?>(null) }
    var showReplaceConfirm by remember { mutableStateOf(false) }

    val malPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        selectedFileName = displayName(context, uri)
        loading = true
        entries = null
        summary = null
        viewModel.parseMalUri(uri) { res ->
            loading = false
            res.onSuccess { list ->
                if (list.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.import_no_entries), Toast.LENGTH_SHORT).show()
                } else {
                    entries = list
                }
            }.onFailure { err ->
                Toast.makeText(context, err.message ?: context.getString(R.string.import_error_file_invalid), Toast.LENGTH_LONG).show()
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
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.import_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                    Text(stringResource(R.string.import_subtitle), fontSize = 12.sp, color = p.muted)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                }
            }

            if (summary != null) {
                val sum = summary!!
                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.import_result_title), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = p.success)
                        Text(stringResource(R.string.import_result_added, sum.added), fontSize = 14.sp, color = p.text, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.import_result_updated, sum.updated), fontSize = 14.sp, color = p.text, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.import_result_skipped, sum.skipped), fontSize = 14.sp, color = p.muted)
                    }
                }
                Button(
                    onClick = {
                        onDismiss()
                        onViewLibrary()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.import_view_library), fontWeight = FontWeight.ExtraBold)
                }
                return@Column
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipButton(
                    label = stringResource(R.string.import_source_anilist),
                    selected = source == ImportSource.ANILIST,
                    accent = p.accent,
                    onClick = { source = ImportSource.ANILIST; entries = null },
                )
                FilterChipButton(
                    label = stringResource(R.string.import_source_mal),
                    selected = source == ImportSource.MAL,
                    accent = p.accent,
                    onClick = { source = ImportSource.MAL; entries = null },
                )
            }

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { mode = ImportMode.MERGE }) {
                        RadioButton(selected = mode == ImportMode.MERGE, onClick = { mode = ImportMode.MERGE })
                        Column {
                            Text(stringResource(R.string.import_mode_merge), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                            Text(stringResource(R.string.import_mode_merge_desc), fontSize = 11.sp, color = p.muted)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { mode = ImportMode.REPLACE }) {
                        RadioButton(selected = mode == ImportMode.REPLACE, onClick = { mode = ImportMode.REPLACE })
                        Column {
                            Text(stringResource(R.string.import_mode_replace), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                            Text(stringResource(R.string.import_mode_replace_desc), fontSize = 11.sp, color = p.muted)
                        }
                    }
                }
            }

            if (source == ImportSource.ANILIST) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.import_anilist_username)) },
                    placeholder = { Text(stringResource(R.string.import_anilist_hint), color = p.muted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.import_anilist_privacy), fontSize = 11.sp, color = p.muted)
                Button(
                    onClick = {
                        val u = username.trim()
                        if (u.isEmpty()) {
                            Toast.makeText(context, context.getString(R.string.import_username_required), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        loading = true
                        entries = null
                        viewModel.fetchAniListEntries(u) { res ->
                            loading = false
                            res.onSuccess { list ->
                                if (list.isEmpty()) {
                                    Toast.makeText(context, context.getString(R.string.import_no_entries), Toast.LENGTH_SHORT).show()
                                } else {
                                    entries = list
                                }
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: context.getString(R.string.import_error_unknown), Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !loading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (loading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = p.onAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.import_loading), fontWeight = FontWeight.Bold)
                    } else {
                        Text(stringResource(R.string.import_fetch), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(stringResource(R.string.import_mal_help), fontSize = 12.sp, color = p.muted)
                if (selectedFileName != null) {
                    Text(stringResource(R.string.import_mal_selected, selectedFileName!!), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.accent)
                }
                OutlinedButton(
                    onClick = { malPicker.launch(arrayOf("text/xml", "application/xml", "application/gzip", "application/x-gzip", "*/*")) },
                    enabled = !loading,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = p.text)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.import_mal_pick), color = p.text, fontWeight = FontWeight.Bold)
                }
            }

            entries?.let { list ->
                val preview = remember(list, viewModel.items) {
                    ImportMapper.preview(viewModel.items, list)
                }
                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.import_preview_title), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                        Text(
                            stringResource(R.string.import_preview_counts, preview.total, preview.seriesCount, preview.movieCount),
                            fontSize = 12.sp,
                            color = p.text,
                        )
                        if (mode == ImportMode.MERGE) {
                            Text(
                                stringResource(R.string.import_preview_breakdown, preview.newCount, preview.updatedCount, preview.unchangedCount),
                                fontSize = 12.sp,
                                color = p.muted,
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if (mode == ImportMode.REPLACE && viewModel.items.isNotEmpty()) {
                            showReplaceConfirm = true
                        } else {
                            summary = viewModel.applyImport(list, mode)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.success, contentColor = p.onAccent),
                    border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.import_import_button, list.size), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    if (showReplaceConfirm) {
        NeoBrutalistConfirmDialog(
            title = stringResource(R.string.import_replace_confirm_title),
            message = stringResource(R.string.import_replace_confirm_message, viewModel.items.size),
            confirmText = stringResource(R.string.import_replace_confirm),
            onDismiss = { showReplaceConfirm = false },
            onConfirm = {
                entries?.let { summary = viewModel.applyImport(it, ImportMode.REPLACE) }
            },
        )
    }
}
