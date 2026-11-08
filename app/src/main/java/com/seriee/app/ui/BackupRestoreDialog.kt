package com.seriee.app.ui

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
    vm: SerieeViewModel,
    onDismiss: () -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var replaceMode by remember { mutableStateOf(false) }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            vm.exportBackup(context, uri).onSuccess { count ->
                Toast.makeText(context, context.getString(R.string.backup_success, count), Toast.LENGTH_LONG).show()
                onDismiss()
            }.onFailure { err ->
                Toast.makeText(context, context.getString(R.string.backup_failed, err.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.importBackup(context, uri, replace = replaceMode).onSuccess { count ->
                Toast.makeText(context, context.getString(R.string.restore_success, count), Toast.LENGTH_LONG).show()
                onDismiss()
            }.onFailure { err ->
                Toast.makeText(context, context.getString(R.string.restore_failed, err.localizedMessage ?: ""), Toast.LENGTH_LONG).show()
            }
        }
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
            border = BorderStroke(1.dp, p.border)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = null,
                        tint = SerieColors.Blue400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.backup_restore),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.text,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                    }
                }

                // Info banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.chip)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.current_data, vm.items.size),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.text
                    )
                }

                // Section 1: Backup / Export
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = SerieColors.Blue400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.backup_to_json_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )
                    }

                    Text(
                        stringResource(R.string.backup_to_json_desc),
                        fontSize = 12.sp,
                        color = p.muted,
                        lineHeight = 17.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600)
                            .clickable {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportLauncher.launch("seriee_backup_$timeStamp.json")
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.save_backup_file),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Section 2: Restore / Import
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FileUpload,
                            contentDescription = null,
                            tint = SerieColors.Emerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.restore_from_json),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )
                    }

                    Text(
                        stringResource(R.string.restore_description),
                        fontSize = 12.sp,
                        color = p.muted,
                        lineHeight = 17.sp
                    )

                    // Mode Selection: Merge vs Replace
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(p.chip)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.restore_method),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { replaceMode = false },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (!replaceMode) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (!replaceMode) SerieColors.Blue400 else p.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    stringResource(R.string.merge_data),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.text
                                )
                                Text(
                                    stringResource(R.string.merge_data_desc),
                                    fontSize = 10.sp,
                                    color = p.muted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { replaceMode = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (replaceMode) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (replaceMode) SerieColors.Orange else p.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    stringResource(R.string.replace_data),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.text
                                )
                                Text(
                                    stringResource(R.string.replace_data_desc),
                                    fontSize = 10.sp,
                                    color = p.muted
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Emerald)
                            .clickable {
                                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.FileUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.pick_backup_file),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // TMDB API key — hidden after save
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.tmdb_api_key_title),
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.text
                    )
                    Text(
                        stringResource(R.string.tmdb_api_key_hint),
                        fontSize = 12.sp, color = p.muted
                    )
                    var keyInput by remember { mutableStateOf(vm.tmdbKey) }
                    // Visible while entering a new key, hidden after save — eye icon toggles reveal
                    var showKey by remember { mutableStateOf(vm.tmdbKey.isBlank()) }
                    val isSaved = vm.tmdbKey.isNotBlank()
                    SField(
                        value = keyInput,
                        onChange = {
                            keyInput = it
                            // Reveal while typing so user can verify, hide again on Save
                            if (!showKey && isSaved && it != vm.tmdbKey) {
                                showKey = true
                            }
                        },
                        placeholder = if (isSaved && !showKey) "••••••••••••••••••••" else "API key",
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showKey = !showKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showKey) "Hide key" else "Show key",
                                    tint = p.muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    // Saved indicator
                    if (isSaved) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storage,
                                contentDescription = null,
                                tint = SerieColors.Emerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (showKey) "Visible" else "Key saved — tap eye to reveal",
                                fontSize = 11.sp,
                                color = p.muted
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SerieColors.Blue600)
                                .clickable {
                                    vm.setTmdbKey(keyInput)
                                    showKey = false
                                    android.widget.Toast.makeText(context, context.getString(R.string.tmdb_api_key_save), android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) { Text(stringResource(R.string.tmdb_api_key_save), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
                        if (vm.tmdbKey.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(p.chip)
                                    .clickable {
                                        vm.setTmdbKey("")
                                        keyInput = ""
                                        // Empty input is shown in plain text (see showKey init)
                                        showKey = true
                                        android.widget.Toast.makeText(context, context.getString(R.string.tmdb_api_key_cleared), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) { Text(stringResource(R.string.delete), fontSize = 12.sp, color = p.muted) }
                        }
                    }
                }
            }
        }
    }
}
