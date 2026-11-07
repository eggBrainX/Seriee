package com.seriee.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.AppTheme

@Composable
fun SerieeApp(vm: SerieeViewModel) {
    val p = LocalPalette.current

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var showBackupRestore by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = p.bg,
        topBar = {
            SerieeTopBar(
                current = vm.theme,
                onTheme = vm::setTheme,
                currentLanguage = vm.language,
                onLanguage = vm::setLanguage,
                onOpenBackupRestore = { showBackupRestore = true },
                onOpenChangelog = { vm.openChangelog() }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = p.surface) {
                val colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = SerieColors.Blue400,
                    indicatorColor = SerieColors.Blue600,
                    unselectedIconColor = p.muted,
                    unselectedTextColor = p.muted
                )
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Timeline, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_progress)) },
                    colors = colors
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_library)) },
                    colors = colors
                )
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(SerieColors.Blue600, SerieColors.Indigo500)))
                    .clickable {
                        editingId = null
                        showForm = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add), tint = Color.White)
            }
        }
    ) { padding ->
        if (tab == 0) {
            ProgressScreen(
                list = vm.items,
                padding = padding,
                onOpen = { detailId = it.id },
                onIncrement = vm::incrementEpisode
            )
        } else {
            LibraryScreen(
                list = vm.items,
                gridView = vm.gridView,
                padding = padding,
                onSetGrid = vm::setGridView,
                onOpen = { detailId = it.id },
                onPin = vm::togglePin
            )
        }
    }

    if (showForm) {
        MediaFormDialog(
            initial = editingId?.let { id -> vm.items.find { it.id == id } },
            onDismiss = { showForm = false },
            onSave = {
                vm.upsert(it)
                showForm = false
            }
        )
    }

    detailId?.let { id ->
        val item = vm.items.find { it.id == id }
        if (item != null) {
            DetailDialog(
                item = item,
                onDismiss = { detailId = null },
                onEdit = {
                    detailId = null
                    editingId = item.id
                    showForm = true
                },
                onDelete = {
                    detailId = null
                    vm.delete(item.id)
                },
                onToggleReminder = { vm.toggleReminder(item.id) },
                onTestReminder = { vm.testReminder(item) },
                onUpdateReleaseDate = { newDate ->
                    vm.upsert(item.copy(releaseDate = newDate, reminderEnabled = true))
                },
                onUpdateRating = { newRating ->
                    vm.upsert(item.copy(rating = newRating))
                },
                onSelectEpisode = { seasonNo, epNo ->
                    vm.setCurrentEpisode(item.id, seasonNo, epNo)
                }
            )
        }
    }

    if (showBackupRestore) {
        BackupRestoreDialog(
            vm = vm,
            onDismiss = { showBackupRestore = false }
        )
    }

    if (vm.showChangelog) {
        ChangelogDialog(
            versionName = vm.currentVersionName,
            onDismiss = { vm.dismissChangelog() }
        )
    }
}

@Composable
private fun SerieeTopBar(
    current: AppTheme,
    onTheme: (AppTheme) -> Unit,
    currentLanguage: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenChangelog: () -> Unit
) {
    val p = LocalPalette.current
    var themeMenu by remember { mutableStateOf(false) }
    var languageMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.bg)
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(SerieColors.Blue600, SerieColors.Indigo500))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Seriee",
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        listOf(p.text, if (p.isLight) SerieColors.Blue600 else SerieColors.Blue400)
                    ),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Changelog button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, p.border, RoundedCornerShape(12.dp))
                    .clickable { onOpenChangelog() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = stringResource(R.string.changelog_title),
                    tint = p.muted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Backup & Restore button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, p.border, RoundedCornerShape(12.dp))
                    .clickable { onOpenBackupRestore() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Storage,
                    contentDescription = stringResource(R.string.backup_restore),
                    tint = p.muted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Language switch button
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, p.border, RoundedCornerShape(12.dp))
                        .clickable { languageMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Language, contentDescription = stringResource(R.string.language), tint = p.muted, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = languageMenu, onDismissRequest = { languageMenu = false }) {
                    AppLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    lang.label,
                                    fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onLanguage(lang)
                                languageMenu = false
                            }
                        )
                    }
                }
            }

            // Theme switch button
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, p.border, RoundedCornerShape(12.dp))
                        .clickable { themeMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Palette, contentDescription = stringResource(R.string.theme), tint = p.muted, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = themeMenu, onDismissRequest = { themeMenu = false }) {
                    AppTheme.entries.forEach { theme ->
                        val dot = paletteFor(theme)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    theme.label,
                                    fontWeight = if (theme == current) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(dot.bg)
                                        .border(1.dp, if (dot.isLight) Color(0xFF94A3B8) else Color(0xFF64748B), CircleShape)
                                )
                            },
                            onClick = {
                                onTheme(theme)
                                themeMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
