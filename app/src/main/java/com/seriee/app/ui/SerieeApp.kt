package com.seriee.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.AppTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SerieeApp(vm: SerieeViewModel) {
    val p = LocalPalette.current
    val haptic = LocalHapticFeedback.current
    val nextTabLabel = stringResource(R.string.nav_next_tab)
    val previousTabLabel = stringResource(R.string.nav_previous_tab)

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var showBackupRestore by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showImport by rememberSaveable { mutableStateOf(false) }
    var fabVisible by remember { mutableStateOf(true) }

    // Quick-action triggers handed down to the active screen.
    var searchTick by remember { mutableIntStateOf(0) }
    var comingSoonTick by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(initialPage = tab) { 2 }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        tab = pagerState.currentPage
    }
    LaunchedEffect(pagerState.settledPage) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val goToTab: (Int) -> Unit = { index ->
        tab = index
        scope.launch { pagerState.animateScrollToPage(index) }
    }

    // Hide the FAB while the user scrolls down, reveal it on the way back up.
    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag) {
                    if (available.y < -6f) fabVisible = false
                    else if (available.y > 6f) fabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        containerColor = p.bg,
        topBar = {
            SerieeTopBar(
                theme = vm.theme,
                customAccent = vm.customAccent,
                customGradient = vm.customGradient,
                language = vm.language,
                onTheme = vm::setTheme,
                onAccent = vm::setCustomAccent,
                onGradient = vm::setCustomGradient,
                onLanguage = vm::setLanguage,
                onOpenBackupRestore = { showBackupRestore = true },
                onOpenImport = {
                    vm.beginImport()
                    showImport = true
                },
                onOpenChangelog = { vm.openChangelog() },
                onOpenAbout = { showAbout = true },
                onSearch = { searchTick++ },
                onSchedule = {
                    goToTab(0)
                    comingSoonTick++
                }
            )
        },
        bottomBar = {
            SerieeNavBar(
                selectedIndex = tab,
                pagerPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction,
                onSelect = goToTab
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.9f else 1f,
                    animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f),
                    label = "fabScale"
                )
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(p.primary, p.accent)))
                        .clickable(
                            interactionSource = interaction,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            editingId = null
                            showForm = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add), tint = p.onAccent)
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(p.bg)
                .nestedScroll(scrollConnection)
        ) {
            p.gradient?.let { colors ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(colors))
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        customActions = listOf(
                            CustomAccessibilityAction(nextTabLabel) {
                                goToTab(1)
                                true
                            },
                            CustomAccessibilityAction(previousTabLabel) {
                                goToTab(0)
                                true
                            }
                        )
                    }
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = -pageOffset * 0.12f * size.width
                        }
                ) {
                    if (page == 0) {
                    ProgressScreen(
                        list = vm.items,
                        padding = padding,
                        onOpen = { detailId = it.id },
                        onIncrement = vm::incrementEpisode,
                        streakDays = vm.streakDays,
                        bestStreak = vm.bestStreak,
                        watchDays = vm.watchDays,
                        onAdd = {
                            editingId = null
                            showForm = true
                        },
                        searchFocusTick = searchTick,
                        comingSoonTick = comingSoonTick
                    )
                } else {
                    LibraryScreen(
                        list = vm.items,
                        gridView = vm.gridView,
                        padding = padding,
                        onSetGrid = vm::setGridView,
                        onOpen = { detailId = it.id },
                        onPin = vm::togglePin,
                        onAdd = {
                            editingId = null
                            showForm = true
                        },
                        searchFocusTick = searchTick
                    )
                }
                }
            }
        }
    }

    if (showForm) {
        MediaFormDialog(
            initial = editingId?.let { id -> vm.items.find { it.id == id } },
            existing = vm.items,
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

    if (showImport) {
        ImportSheet(
            vm = vm,
            onDismiss = { showImport = false },
            onViewLibrary = {
                showImport = false
                goToTab(1)
            }
        )
    }

    if (vm.showChangelog) {
        ChangelogDialog(
            versionName = vm.currentVersionName,
            onDismiss = { vm.dismissChangelog() }
        )
    }

    if (showAbout) {
        AboutDialog(
            versionName = vm.currentVersionName,
            onDismiss = { showAbout = false }
        )
    }
}

@Composable
private fun SerieeTopBar(
    theme: AppTheme,
    customAccent: Color,
    customGradient: Boolean,
    language: AppLanguage,
    onTheme: (AppTheme) -> Unit,
    onAccent: (Color) -> Unit,
    onGradient: (Boolean) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenAbout: () -> Unit,
    onSearch: () -> Unit,
    onSchedule: () -> Unit
) {
    val p = LocalPalette.current
    var optionsMenu by remember { mutableStateOf(false) }

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
                    .background(Brush.linearGradient(listOf(p.primary, p.accent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Seriee",
                style = TextStyle(
                    brush = Brush.horizontalGradient(listOf(p.text, p.accent)),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconPillButton(
                icon = Icons.Filled.Search,
                contentDescription = stringResource(R.string.search),
                onClick = onSearch
            )
            IconPillButton(
                icon = Icons.Filled.CalendarMonth,
                contentDescription = stringResource(R.string.schedule),
                onClick = onSchedule
            )
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(SerieeShape.chip)
                        .background(p.chip)
                        .border(1.dp, p.border, SerieeShape.chip)
                        .clickable { optionsMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        tint = p.muted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                SerieeOverflowMenu(
                    expanded = optionsMenu,
                    onDismiss = { optionsMenu = false },
                    theme = theme,
                    customAccent = customAccent,
                    customGradient = customGradient,
                    onTheme = onTheme,
                    onAccent = onAccent,
                    onGradient = onGradient,
                    language = language,
                    onLanguage = onLanguage,
                    onBackupRestore = onOpenBackupRestore,
                    onImport = onOpenImport,
                    onChangelog = onOpenChangelog,
                    onAbout = onOpenAbout
                )
            }
        }
    }
}

@Composable
private fun SerieeNavBar(
    selectedIndex: Int,
    pagerPosition: Float,
    onSelect: (Int) -> Unit
) {
    val p = LocalPalette.current
    val haptic = LocalHapticFeedback.current
    val items = listOf(
        Icons.Filled.Timeline to stringResource(R.string.nav_progress),
        Icons.Filled.VideoLibrary to stringResource(R.string.nav_library)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (p.isGlass) p.card.copy(alpha = 0.72f) else p.surface)
            .border(
                1.dp,
                if (p.isGlass) Color.White.copy(alpha = 0.10f) else p.border,
                RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 6.dp)
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            val itemWidth = maxWidth / 2
            val indicatorWidth = itemWidth * 0.64f
            val position = pagerPosition.coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = itemWidth * position + (itemWidth - indicatorWidth) / 2,
                        y = 7.dp
                    )
                    .width(indicatorWidth)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(p.primary, p.accent)))
            )
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { index, (icon, label) ->
                    val selected = abs(position - index) < 0.5f
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(index)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (selected) p.onAccent else p.muted,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) p.text else p.muted
                        )
                    }
                }
            }
        }
    }
}
