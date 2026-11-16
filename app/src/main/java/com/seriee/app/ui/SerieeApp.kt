package com.seriee.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.MediaItem
import com.seriee.app.data.WatchStatus
import com.seriee.app.debug.AppLogger
import com.seriee.app.reminder.ReminderScheduler
import kotlinx.coroutines.launch

@Composable
fun SerieeApp(viewModel: SerieeViewModel) {
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val progressListState = rememberLazyListState()
    val libraryListState = rememberLazyListState()
    val libraryGridState = rememberLazyGridState()
    val insightsListState = rememberLazyListState()

    var showSearch by remember { mutableStateOf(false) }
    var overflowOpen by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChangelogManual by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDebugOverlay by remember { mutableStateOf(false) }
    val pendingCrash by AppLogger.pendingCrash.collectAsState()

    var detailTargetId by remember { mutableStateOf<String?>(null) }
    var formInitialItem by remember { mutableStateOf<MediaItem?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }

    val detailItem = remember(detailTargetId, viewModel.items) {
        detailTargetId?.let { id -> viewModel.items.firstOrNull { it.id == id } }
    }

    val fabVisible by remember(pagerState.currentPage, viewModel.gridView) {
        derivedStateOf {
            when (pagerState.currentPage) {
                0 -> !progressListState.isScrollInProgress
                1 -> if (viewModel.gridView) !libraryGridState.isScrollInProgress else !libraryListState.isScrollInProgress
                else -> false
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible && pagerState.currentPage < 2,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        formInitialItem = null
                        showFormDialog = true
                    },
                    containerColor = p.accent,
                    contentColor = p.onAccent,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(if (p.isNeoBrutalist) 0.dp else 4.dp),
                    modifier = Modifier
                        .padding(bottom = 72.dp)
                        .then(
                            if (p.isNeoBrutalist) Modifier.border(2.dp, p.border, RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add))
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(p.backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                TopHeaderBar(
                    showSearch = showSearch,
                    overflowOpen = overflowOpen,
                    onToggleSearch = { showSearch = !showSearch },
                    onOpenSchedule = { showScheduleDialog = true },
                    onToggleOverflow = { overflowOpen = it },
                    onOpenImport = { overflowOpen = false; showImportSheet = true },
                    onOpenBackup = { overflowOpen = false; showBackupDialog = true },
                    onOpenTheme = { overflowOpen = false; showThemeMenu = true },
                    onOpenLanguage = { overflowOpen = false; showLanguageDialog = true },
                    onOpenChangelog = { overflowOpen = false; showChangelogManual = true },
                    onOpenDebugOverlay = { overflowOpen = false; showDebugOverlay = !showDebugOverlay },
                    onOpenAbout = { overflowOpen = false; showAboutDialog = true },
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) { page ->
                    when (page) {
                        0 -> ProgressScreen(
                            items = viewModel.items,
                            streak = viewModel.streak,
                            showSearch = showSearch,
                            listState = progressListState,
                            onIncrement = { viewModel.incrementEpisode(it) },
                            onSelect = { detailTargetId = it.id },
                            onResetStreak = { viewModel.resetStreak() },
                            onAddClick = {
                                formInitialItem = null
                                showFormDialog = true
                            },
                        )

                        1 -> LibraryScreen(
                            items = viewModel.items,
                            gridView = viewModel.gridView,
                            showSearch = showSearch,
                            gridState = libraryGridState,
                            listState = libraryListState,
                            onToggleGrid = { viewModel.updateGridView(it) },
                            onTogglePin = { viewModel.togglePin(it) },
                            onSelect = { detailTargetId = it.id },
                            onAddClick = {
                                formInitialItem = null
                                showFormDialog = true
                            },
                            onImportClick = { showImportSheet = true },
                        )

                        2 -> InsightsScreen(
                            items = viewModel.items,
                            history = viewModel.history,
                            watchDays = viewModel.streak.watchDays,
                            listState = insightsListState,
                            onClearHistory = { viewModel.clearHistory() },
                        )
                    }
                }
            }

            FloatingBottomNav(
                currentPage = pagerState.currentPage,
                onSelectPage = { idx -> scope.launch { pagerState.animateScrollToPage(idx) } },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )

            AnimatedVisibility(
                visible = showDebugOverlay,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 58.dp),
            ) {
                DebugOverlay(onDismiss = { showDebugOverlay = false })
            }
        }
    }

    if (detailItem != null) {
        DetailDialog(
            item = detailItem,
            onDismiss = { detailTargetId = null },
            onEdit = {
                formInitialItem = detailItem
                detailTargetId = null
                showFormDialog = true
            },
            onDelete = { viewModel.deleteItem(detailItem.id) },
            onStatusChange = { viewModel.setStatus(detailItem.id, it) },
            onRatingChange = { viewModel.setRating(detailItem.id, it) },
            onSeasonEpisodeChange = { s, ep -> viewModel.setSeasonEpisode(detailItem.id, s, ep) },
            onToggleReminder = { viewModel.toggleReminder(detailItem.id) },
            onReleaseDateChange = { viewModel.setReleaseDate(detailItem.id, it) },
            onTestReminder = { viewModel.testReminderNow(detailItem) },
        )
    }

    if (showFormDialog) {
        MediaFormDialog(
            initial = formInitialItem,
            existingTitles = viewModel.items.map { it.title }.toSet(),
            tmdbKey = viewModel.tmdbKey,
            omdbKey = viewModel.omdbKey,
            onDismiss = { showFormDialog = false },
            onSave = { viewModel.upsertItem(it) },
        )
    }

    if (showThemeMenu) {
        ThemeMenu(
            mode = viewModel.themeMode,
            style = viewModel.themeStyle,
            custom = viewModel.customTheme,
            reduceTransparency = viewModel.reduceTransparency,
            batteryAmoled = viewModel.batteryAmoled,
            items = viewModel.items,
            onModeChange = { viewModel.updateThemeMode(it) },
            onStyleChange = { viewModel.updateThemeStyle(it) },
            onCustomAccentChange = { viewModel.updateCustomAccent(it) },
            onCustomBackgroundChange = { s, e, g -> viewModel.updateCustomBackground(s, e, g) },
            onApplyPreset = { viewModel.applyThemePreset(it) },
            onReduceTransparencyChange = { viewModel.updateReduceTransparency(it) },
            onBatteryAmoledChange = { viewModel.updateBatteryAmoled(it) },
            onDismiss = { showThemeMenu = false },
        )
    }

    if (showBackupDialog) {
        BackupRestoreDialog(viewModel = viewModel, onDismiss = { showBackupDialog = false })
    }

    if (showImportSheet) {
        ImportSheet(
            viewModel = viewModel,
            onDismiss = { showImportSheet = false },
            onViewLibrary = { scope.launch { pagerState.animateScrollToPage(1) } },
        )
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            current = viewModel.language,
            onSelect = { viewModel.updateLanguage(it); showLanguageDialog = false },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showScheduleDialog) {
        UpcomingScheduleDialog(
            items = viewModel.items,
            onSelectItem = { item ->
                showScheduleDialog = false
                detailTargetId = item.id
            },
            onDismiss = { showScheduleDialog = false },
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showChangelogManual || viewModel.showChangelogAuto) {
        ChangelogDialog(
            onDismiss = {
                showChangelogManual = false
                viewModel.dismissAutoChangelog()
            }
        )
    }

    pendingCrash?.let { report ->
        CrashErrorDialog(
            report = report,
            onDismiss = { AppLogger.dismissPendingCrash() },
            onOpenOverlay = {
                AppLogger.dismissPendingCrash()
                showDebugOverlay = true
            },
        )
    }
}

@Composable
private fun TopHeaderBar(
    showSearch: Boolean,
    overflowOpen: Boolean,
    onToggleSearch: () -> Unit,
    onOpenSchedule: () -> Unit,
    onToggleOverflow: (Boolean) -> Unit,
    onOpenImport: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenDebugOverlay: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val p = LocalPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(p.accent)
                    .then(if (p.isNeoBrutalist) Modifier.border(2.dp, p.border, RoundedCornerShape(10.dp)) else Modifier)
            ) {
                Text("S", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = p.onAccent)
            }
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.app_name), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
        }

        IconButton(onClick = onToggleSearch) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search),
                tint = if (showSearch) p.accent else p.text,
            )
        }
        IconButton(onClick = onOpenSchedule) {
            Icon(Icons.Rounded.Schedule, contentDescription = stringResource(R.string.schedule), tint = p.text)
        }
        Box {
            IconButton(onClick = { onToggleOverflow(true) }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.more_options), tint = p.text)
            }
            DropdownMenu(
                expanded = overflowOpen,
                onDismissRequest = { onToggleOverflow(false) },
                modifier = Modifier.background(p.surface),
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_collection), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = p.text) },
                    onClick = onOpenImport,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.backup_restore), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.Backup, contentDescription = null, tint = p.text) },
                    onClick = onOpenBackup,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.theme), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.Palette, contentDescription = null, tint = p.text) },
                    onClick = onOpenTheme,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.language), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null, tint = p.text) },
                    onClick = onOpenLanguage,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.changelog_title), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.NewReleases, contentDescription = null, tint = p.text) },
                    onClick = onOpenChangelog,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.debug_overlay), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.BugReport, contentDescription = null, tint = p.text) },
                    onClick = onOpenDebugOverlay,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.about_title), color = p.text) },
                    leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = p.text) },
                    onClick = onOpenAbout,
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomNav(
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    val navItems = listOf(
        Triple(0, Icons.Rounded.PlayCircle, R.string.nav_progress),
        Triple(1, Icons.AutoMirrored.Rounded.LibraryBooks, R.string.nav_library),
        Triple(2, Icons.Rounded.Insights, R.string.nav_insights),
    )

    SerieeCard(
        containerColor = p.surface,
        cornerRadius = 18.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            navItems.forEach { (idx, icon, labelRes) ->
                val selected = currentPage == idx
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) p.accent else Color.Transparent)
                        .clickable { onSelectPage(idx) }
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                ) {
                    Icon(
                        icon,
                        contentDescription = stringResource(labelRes),
                        tint = if (selected) p.onAccent else p.muted,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = stringResource(labelRes),
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (selected) p.onAccent else p.muted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    Dialog(onDismissRequest = onDismiss) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.border,
            shadowOffset = 6.dp,
            cornerRadius = 14.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.language), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                AppLanguage.entries.forEach { lang ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(lang) }
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = current == lang, onClick = { onSelect(lang) })
                        Text(stringResource(lang.labelRes), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingScheduleDialog(
    items: List<MediaItem>,
    onSelectItem: (MediaItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val scheduled = remember(items) {
        items.filter { it.releaseDate != null || it.status == WatchStatus.ComingSoon }
            .sortedBy { it.releaseDate ?: Long.MAX_VALUE }
    }
    Dialog(onDismissRequest = onDismiss) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.border,
            shadowOffset = 6.dp,
            cornerRadius = 14.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.schedule), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                if (scheduled.isEmpty()) {
                    Text(stringResource(R.string.no_schedule), fontSize = 13.sp, color = p.muted)
                } else {
                    scheduled.take(8).forEach { item ->
                        SerieeCard(
                            containerColor = p.cardAlt,
                            onClick = { onSelectItem(item) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.text)
                                    Text(
                                        ReminderScheduler.timeUntilRelease(context, item.releaseDate),
                                        fontSize = 12.sp,
                                        color = p.warning,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                if (item.reminderEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(p.reminder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
