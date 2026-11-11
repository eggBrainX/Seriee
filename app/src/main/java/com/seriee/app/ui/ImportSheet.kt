package com.seriee.app.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import com.seriee.app.data.ImportMode
import com.seriee.app.data.ImportPhase
import com.seriee.app.data.ImportSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    vm: SerieeViewModel,
    onDismiss: () -> Unit,
    onViewLibrary: () -> Unit
) {
    val p = LocalPalette.current
    val state = vm.importState
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var localErrorRes by remember { mutableStateOf<Int?>(null) }
    var confirmReplace by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun close() {
        vm.dismissImport()
        onDismiss()
    }

    LaunchedEffect(Unit) { vm.beginImport() }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "myanimelist.xml"
            localErrorRes = null
            vm.loadMalFile(context, uri, name)
        }
    }

    if (confirmReplace) {
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text(stringResource(R.string.import_replace_confirm_title)) },
            text = { Text(stringResource(R.string.import_replace_confirm_message, vm.items.size)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReplace = false
                    vm.applyImport()
                }) {
                    Text(stringResource(R.string.import_replace_confirm), color = p.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReplace = false }) {
                    Text(stringResource(R.string.cancel), color = p.muted)
                }
            },
            containerColor = p.elevated,
            titleContentColor = p.text,
            textContentColor = p.muted
        )
    }

    ModalBottomSheet(
        onDismissRequest = { close() },
        sheetState = sheetState,
        containerColor = p.surface,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = { SheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CloudDownload, null, tint = p.accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.import_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.text
                    )
                    Text(
                        stringResource(R.string.import_subtitle),
                        fontSize = 11.sp,
                        color = p.muted
                    )
                }
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { close() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, stringResource(R.string.close), tint = p.muted, modifier = Modifier.size(18.dp))
                }
            }

            SegmentedTabs(
                labels = listOf(
                    stringResource(R.string.import_source_anilist),
                    stringResource(R.string.import_source_mal)
                ),
                selected = if (state.source == ImportSource.AniList) 0 else 1,
                onSelect = {
                    localErrorRes = null
                    vm.setImportSource(
                        if (it == 0) ImportSource.AniList else ImportSource.MyAnimeList
                    )
                }
            )

            // Merge / replace
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioCard(
                    title = stringResource(R.string.import_mode_merge),
                    subtitle = stringResource(R.string.import_mode_merge_desc),
                    selected = state.mode == ImportMode.Merge,
                    onClick = { vm.setImportMode(ImportMode.Merge) }
                )
                RadioCard(
                    title = stringResource(R.string.import_mode_replace),
                    subtitle = stringResource(R.string.import_mode_replace_desc),
                    selected = state.mode == ImportMode.Replace,
                    onClick = { vm.setImportMode(ImportMode.Replace) }
                )
            }

            HorizontalDivider(color = p.border)

            when (state.phase) {
                ImportPhase.Done -> ResultBlock(vm, onViewLibrary, { close() })

                else -> {
                    if (state.source == ImportSource.AniList) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.import_anilist_username),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.muted
                            )
                            SField(
                                value = username,
                                onChange = {
                                    username = it
                                    localErrorRes = null
                                },
                                placeholder = stringResource(R.string.import_anilist_hint)
                            )
                            Text(
                                stringResource(R.string.import_anilist_privacy),
                                fontSize = 10.sp,
                                color = p.muted,
                                lineHeight = 14.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SecondaryButton(
                                text = stringResource(R.string.import_mal_pick),
                                onClick = { filePicker.launch(arrayOf("*/*")) }
                            )
                            if (state.fileName.isNotBlank()) {
                                Text(
                                    stringResource(R.string.import_mal_selected, state.fileName),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.success
                                )
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.Info, null, tint = p.muted, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    stringResource(R.string.import_mal_help),
                                    fontSize = 10.sp,
                                    color = p.muted,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    if (state.phase == ImportPhase.Loading) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = p.primary,
                                trackColor = p.chip
                            )
                            Text(
                                stringResource(R.string.import_loading),
                                fontSize = 11.sp,
                                color = p.muted
                            )
                        }
                    }

                    val localError = localErrorRes
                    if (localError != null) {
                        Text(stringResource(localError), fontSize = 11.sp, color = p.danger)
                    }
                    state.error?.let { error ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(p.danger)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(error.messageRes),
                                fontSize = 11.sp,
                                color = p.danger
                            )
                        }
                    }

                    val preview = state.preview
                    if (preview != null) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(SerieeShape.field)
                                .background(p.card)
                                .border(1.dp, p.border, SerieeShape.field)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                stringResource(R.string.import_preview_title),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.text
                            )
                            Text(
                                stringResource(
                                    R.string.import_preview_counts,
                                    preview.total,
                                    preview.seriesCount,
                                    preview.movieCount
                                ),
                                fontSize = 11.sp,
                                color = p.muted
                            )
                            Text(
                                stringResource(
                                    R.string.import_preview_breakdown,
                                    preview.newCount,
                                    preview.updatedCount,
                                    preview.unchangedCount
                                ),
                                fontSize = 11.sp,
                                color = p.success
                            )
                        }

                        if (state.mode == ImportMode.Replace) {
                            SecondaryButton(
                                text = stringResource(R.string.cancel),
                                onClick = { vm.setImportSource(state.source) }
                            )
                        }
                        PrimaryButton(
                            text = stringResource(R.string.import_import_button, preview.total),
                            gradient = true,
                            onClick = {
                                if (state.mode == ImportMode.Replace) confirmReplace = true
                                else vm.applyImport()
                            }
                        )
                    } else {
                        val action = {
                            when (state.source) {
                                ImportSource.AniList -> {
                                    if (username.isBlank()) {
                                        localErrorRes = R.string.import_username_required
                                    } else {
                                        localErrorRes = null
                                        vm.fetchAniList(username)
                                    }
                                }
                                ImportSource.MyAnimeList -> {
                                    localErrorRes = null
                                    filePicker.launch(arrayOf("*/*"))
                                }
                            }
                            Unit
                        }
                        PrimaryButton(
                            text = if (state.source == ImportSource.AniList) {
                                stringResource(R.string.import_fetch)
                            } else {
                                stringResource(R.string.import_mal_pick)
                            },
                            enabled = state.phase != ImportPhase.Loading,
                            onClick = action
                        )
                        if (state.phase == ImportPhase.Loading) {
                            SecondaryButton(
                                text = stringResource(R.string.cancel),
                                onClick = { close() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultBlock(
    vm: SerieeViewModel,
    onViewLibrary: () -> Unit,
    onDone: () -> Unit
) {
    val p = LocalPalette.current
    val result = vm.importState.result ?: return
    Column(
        Modifier
            .fillMaxWidth()
            .clip(SerieeShape.card)
            .background(p.card)
            .border(1.dp, p.border, SerieeShape.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(p.success.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = p.success, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.import_result_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = p.text
            )
        }
        Text(stringResource(R.string.import_result_added, result.added), fontSize = 12.sp, color = p.text)
        Text(stringResource(R.string.import_result_updated, result.updated), fontSize = 12.sp, color = p.text)
        Text(stringResource(R.string.import_result_skipped, result.skipped), fontSize = 12.sp, color = p.muted)
    }
    PrimaryButton(
        text = stringResource(R.string.import_view_library),
        gradient = true,
        onClick = {
            vm.dismissImport()
            onViewLibrary()
        }
    )
    SecondaryButton(
        text = stringResource(R.string.close),
        onClick = onDone
    )
}
