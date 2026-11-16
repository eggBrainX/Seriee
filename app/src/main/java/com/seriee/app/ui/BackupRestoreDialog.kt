@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection")

package com.seriee.app.ui

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    fun sectionCardModifier(): Modifier = if (p.isNeoBrutalist) {
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
            .padding(16.dp)
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
            .padding(16.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (p.isNeoBrutalist) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeoBrutalistColors.Yellow)
                                .border(2.dp, p.border, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Storage,
                                contentDescription = null,
                                tint = NeoBrutalistColors.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            tint = p.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.backup_restore),
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
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = p.muted)
                        }
                    }
                }

                // Info banner
                val bannerModifier = if (p.isNeoBrutalist) {
                    Modifier
                        .fillMaxWidth()
                        .neoBrutalist(
                            backgroundColor = NeoBrutalistColors.SkyBlue,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp,
                            cornerRadius = 8.dp
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.chip)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                }
                Row(
                    modifier = bannerModifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.current_data, vm.items.size),
                        fontSize = 12.sp,
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.text,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (vm.streakDays > 0) stringResource(R.string.streak_days, vm.streakDays)
                        else stringResource(R.string.best_streak, vm.bestStreak),
                        fontSize = 11.sp,
                        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                        color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.muted
                    )
                    if (vm.streakDays > 0 || vm.bestStreak > 0) {
                        Spacer(Modifier.width(8.dp))
                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = {
                                    vm.resetStreak()
                                    android.widget.Toast.makeText(context, context.getString(R.string.streak_reset_done), android.widget.Toast.LENGTH_SHORT).show()
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
                                    stringResource(R.string.reset_streak),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoBrutalistColors.Black
                                )
                            }
                        } else {
                            Text(
                                stringResource(R.string.reset_streak),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = p.danger,
                                modifier = Modifier.clickable {
                                    vm.resetStreak()
                                    android.widget.Toast.makeText(context, context.getString(R.string.streak_reset_done), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Section 1: Backup / Export
                Column(
                    modifier = sectionCardModifier(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = if (p.isNeoBrutalist) p.text else p.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.backup_to_json_title),
                            fontSize = 15.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                            color = p.text
                        )
                    }

                    Text(
                        stringResource(R.string.backup_to_json_desc),
                        fontSize = 12.sp,
                        color = p.muted,
                        lineHeight = 17.sp
                    )

                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportLauncher.launch("seriee_backup_$timeStamp.json")
                            },
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.Yellow,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.FileDownload,
                                    contentDescription = null,
                                    tint = NeoBrutalistColors.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.save_backup_file),
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
                                .background(p.primary)
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
                                tint = p.onAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.save_backup_file),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = p.onAccent
                            )
                        }
                    }
                }

                // Section 2: Restore / Import
                Column(
                    modifier = sectionCardModifier(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FileUpload,
                            contentDescription = null,
                            tint = if (p.isNeoBrutalist) p.text else p.success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.restore_from_json),
                            fontSize = 15.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
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
                            .clip(RoundedCornerShape(if (p.isNeoBrutalist) 8.dp else 10.dp))
                            .background(p.chip)
                            .then(
                                if (p.isNeoBrutalist) {
                                    Modifier.border(2.dp, p.border, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.restore_method),
                            fontSize = 11.sp,
                            fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (p.isNeoBrutalist) {
                                        Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (!replaceMode) NeoBrutalistColors.Yellow else p.surface)
                                            .border(2.dp, p.border, RoundedCornerShape(6.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { replaceMode = false }
                                .padding(horizontal = if (p.isNeoBrutalist) 8.dp else 0.dp, vertical = if (p.isNeoBrutalist) 8.dp else 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (!replaceMode) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = when {
                                    p.isNeoBrutalist && !replaceMode -> NeoBrutalistColors.Black
                                    !replaceMode -> p.primary
                                    else -> p.muted
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    stringResource(R.string.merge_data),
                                    fontSize = 12.sp,
                                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (p.isNeoBrutalist && !replaceMode) NeoBrutalistColors.Black else p.text
                                )
                                Text(
                                    stringResource(R.string.merge_data_desc),
                                    fontSize = 10.sp,
                                    color = if (p.isNeoBrutalist && !replaceMode) NeoBrutalistColors.Black else p.muted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (p.isNeoBrutalist) {
                                        Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (replaceMode) NeoBrutalistColors.CoralRed else p.surface)
                                            .border(2.dp, p.border, RoundedCornerShape(6.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { replaceMode = true }
                                .padding(horizontal = if (p.isNeoBrutalist) 8.dp else 0.dp, vertical = if (p.isNeoBrutalist) 8.dp else 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (replaceMode) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = when {
                                    p.isNeoBrutalist && replaceMode -> NeoBrutalistColors.Black
                                    replaceMode -> p.warning
                                    else -> p.muted
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    stringResource(R.string.replace_data),
                                    fontSize = 12.sp,
                                    fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (p.isNeoBrutalist && replaceMode) NeoBrutalistColors.Black else p.text
                                )
                                Text(
                                    stringResource(R.string.replace_data_desc),
                                    fontSize = 10.sp,
                                    color = if (p.isNeoBrutalist && replaceMode) NeoBrutalistColors.Black else p.muted
                                )
                            }
                        }
                    }

                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            role = Role.Button,
                            backgroundColor = NeoBrutalistColors.Mint,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.5.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 8.dp,
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.FileUpload,
                                    contentDescription = null,
                                    tint = NeoBrutalistColors.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.pick_backup_file),
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
                                .background(p.success)
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
                                tint = p.onAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.pick_backup_file),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = p.onAccent
                            )
                        }
                    }
                }

                // TMDB API key — hidden after save
                Column(
                    modifier = sectionCardModifier(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.tmdb_api_key_title),
                        fontSize = 15.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                        color = p.text
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
                            if (p.isNeoBrutalist) {
                                NeoBrutalistBox(
                                    onClick = { showKey = !showKey },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.OffWhite,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 1.5.dp,
                                    shadowOffset = 2.dp,
                                    cornerRadius = 6.dp,
                                    modifier = Modifier.size(34.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showKey) "Hide key" else "Show key",
                                        tint = NeoBrutalistColors.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
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
                                tint = p.success,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (showKey) "Visible" else "Key saved — tap eye to reveal",
                                fontSize = 11.sp,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                color = p.muted
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = {
                                    vm.setTmdbKey(keyInput)
                                    showKey = false
                                    android.widget.Toast.makeText(context, context.getString(R.string.tmdb_api_key_save), android.widget.Toast.LENGTH_SHORT).show()
                                },
                                role = Role.Button,
                                backgroundColor = NeoBrutalistColors.Yellow,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                cornerRadius = 6.dp,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    stringResource(R.string.tmdb_api_key_save),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoBrutalistColors.Black
                                )
                            }
                            if (vm.tmdbKey.isNotBlank()) {
                                NeoBrutalistBox(
                                    onClick = {
                                        vm.setTmdbKey("")
                                        keyInput = ""
                                        showKey = true
                                        android.widget.Toast.makeText(context, context.getString(R.string.tmdb_api_key_cleared), android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.CoralRed,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    cornerRadius = 6.dp,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.delete),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeoBrutalistColors.Black
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(p.primary)
                                    .clickable {
                                        vm.setTmdbKey(keyInput)
                                        showKey = false
                                        android.widget.Toast.makeText(context, context.getString(R.string.tmdb_api_key_save), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) { Text(stringResource(R.string.tmdb_api_key_save), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.onAccent) }
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

                // OMDb API key — hidden after save (same pattern as TMDB)
                Column(
                    modifier = sectionCardModifier(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.omdb_api_key_title),
                        fontSize = 15.sp,
                        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                        color = p.text
                    )
                    Text(
                        stringResource(R.string.omdb_api_key_hint),
                        fontSize = 12.sp, color = p.muted
                    )
                    var omdbInput by remember { mutableStateOf(vm.omdbKey) }
                    var omdbShow by remember { mutableStateOf(vm.omdbKey.isBlank()) }
                    val omdbSaved = vm.omdbKey.isNotBlank()
                    SField(
                        value = omdbInput,
                        onChange = {
                            omdbInput = it
                            if (!omdbShow && omdbSaved && it != vm.omdbKey) omdbShow = true
                        },
                        placeholder = if (omdbSaved && !omdbShow) "••••••••••••••••••••" else "API key",
                        visualTransformation = if (omdbShow) VisualTransformation.None else PasswordVisualTransformation(),
                        trailing = {
                            if (p.isNeoBrutalist) {
                                NeoBrutalistBox(
                                    onClick = { omdbShow = !omdbShow },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.OffWhite,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 1.5.dp,
                                    shadowOffset = 2.dp,
                                    cornerRadius = 6.dp,
                                    modifier = Modifier.size(34.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (omdbShow) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (omdbShow) "Hide key" else "Show key",
                                        tint = NeoBrutalistColors.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { omdbShow = !omdbShow },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (omdbShow) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (omdbShow) "Hide key" else "Show key",
                                        tint = p.muted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                    if (omdbSaved) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storage,
                                contentDescription = null,
                                tint = p.success,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (omdbShow) "Visible" else "Key saved — tap eye to reveal",
                                fontSize = 11.sp,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                color = p.muted
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (p.isNeoBrutalist) {
                            NeoBrutalistBox(
                                onClick = {
                                    vm.setOmdbKey(omdbInput)
                                    omdbShow = false
                                    android.widget.Toast.makeText(context, context.getString(R.string.omdb_api_key_save), android.widget.Toast.LENGTH_SHORT).show()
                                },
                                role = Role.Button,
                                backgroundColor = NeoBrutalistColors.Yellow,
                                borderColor = p.border,
                                shadowColor = NeoBrutalistColors.Black,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp,
                                cornerRadius = 6.dp,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    stringResource(R.string.omdb_api_key_save),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoBrutalistColors.Black
                                )
                            }
                            if (vm.omdbKey.isNotBlank()) {
                                NeoBrutalistBox(
                                    onClick = {
                                        vm.setOmdbKey("")
                                        omdbInput = ""
                                        omdbShow = true
                                        android.widget.Toast.makeText(context, context.getString(R.string.omdb_api_key_cleared), android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    role = Role.Button,
                                    backgroundColor = NeoBrutalistColors.CoralRed,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp,
                                    cornerRadius = 6.dp,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.delete),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeoBrutalistColors.Black
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(p.primary)
                                    .clickable {
                                        vm.setOmdbKey(omdbInput)
                                        omdbShow = false
                                        android.widget.Toast.makeText(context, context.getString(R.string.omdb_api_key_save), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) { Text(stringResource(R.string.omdb_api_key_save), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.onAccent) }
                            if (vm.omdbKey.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(p.chip)
                                        .clickable {
                                            vm.setOmdbKey("")
                                            omdbInput = ""
                                            omdbShow = true
                                            android.widget.Toast.makeText(context, context.getString(R.string.omdb_api_key_cleared), android.widget.Toast.LENGTH_SHORT).show()
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
}
