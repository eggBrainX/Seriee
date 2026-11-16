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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreDialog(
    viewModel: SerieeViewModel,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mergeMode by remember { mutableStateOf(true) }
    var tmdbDraft by remember { mutableStateOf(viewModel.tmdbKey) }
    var omdbDraft by remember { mutableStateOf(viewModel.omdbKey) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToUri(uri) { res ->
                res.onSuccess { count ->
                    Toast.makeText(context, context.getString(R.string.backup_success, count), Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(context, context.getString(R.string.backup_failed, err.message ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackupFromUri(uri, mergeMode) { res ->
                res.onSuccess { count ->
                    Toast.makeText(context, context.getString(R.string.restore_success, count), Toast.LENGTH_LONG).show()
                    onDismiss()
                }.onFailure { err ->
                    Toast.makeText(context, context.getString(R.string.restore_failed, err.message ?: ""), Toast.LENGTH_LONG).show()
                }
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
                Text(stringResource(R.string.backup_restore), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                }
            }

            Text(
                text = stringResource(R.string.current_data, viewModel.items.size),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = p.accent,
            )

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.backup_to_json), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                    Text(stringResource(R.string.backup_description), fontSize = 12.sp, color = p.muted, lineHeight = 18.sp)
                    Button(
                        onClick = { exportLauncher.launch("seriee-backup.json") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                        border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.save_backup_file), fontWeight = FontWeight.Bold)
                    }
                }
            }

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.restore_from_json), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                    Text(stringResource(R.string.restore_description), fontSize = 12.sp, color = p.muted, lineHeight = 18.sp)
                    Text(stringResource(R.string.restore_method), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.text)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mergeMode = true }
                    ) {
                        RadioButton(selected = mergeMode, onClick = { mergeMode = true })
                        Column {
                            Text(stringResource(R.string.merge_data), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                            Text(stringResource(R.string.merge_data_desc), fontSize = 11.sp, color = p.muted)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mergeMode = false }
                    ) {
                        RadioButton(selected = !mergeMode, onClick = { mergeMode = false })
                        Column {
                            Text(stringResource(R.string.replace_data), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                            Text(stringResource(R.string.replace_data_desc), fontSize = 11.sp, color = p.muted)
                        }
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(if (p.isNeoBrutalist) 2.dp else 1.dp, p.border),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.Upload, contentDescription = null, tint = p.text)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.pick_backup_file), color = p.text, fontWeight = FontWeight.Bold)
                    }
                }
            }

            SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.tmdb_api_key_title), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                    OutlinedTextField(
                        value = tmdbDraft,
                        onValueChange = { tmdbDraft = it },
                        placeholder = { Text(stringResource(R.string.tmdb_api_key_hint), color = p.muted, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            viewModel.saveTmdbKey(tmdbDraft)
                            Toast.makeText(
                                context,
                                if (tmdbDraft.isBlank()) context.getString(R.string.tmdb_api_key_cleared) else context.getString(R.string.save),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    ) {
                        Text(stringResource(R.string.tmdb_api_key_save), fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.omdb_api_key_title), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                    OutlinedTextField(
                        value = omdbDraft,
                        onValueChange = { omdbDraft = it },
                        placeholder = { Text(stringResource(R.string.omdb_api_key_hint), color = p.muted, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            viewModel.saveOmdbKey(omdbDraft)
                            Toast.makeText(
                                context,
                                if (omdbDraft.isBlank()) context.getString(R.string.omdb_api_key_cleared) else context.getString(R.string.save),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    ) {
                        Text(stringResource(R.string.omdb_api_key_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
