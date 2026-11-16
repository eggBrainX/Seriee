package com.seriee.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.SerieeViewModel
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.AppThemeMode
import com.seriee.app.data.AppThemeStyle
import kotlinx.coroutines.launch

private enum class MenuPage { Root, Theme, Customize, Language }

/** Accent presets offered by the custom theme. */
private val accentPresets = listOf(
    Color(0xFF3B82F6),
    Color(0xFFF0559B),
    Color(0xFF8B5CF6),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
    Color(0xFF06B6D4),
    Color(0xFFF8FAFC)
)

/** Two-stop background gradients for the custom theme. */
private val gradientPresets = listOf(
    Color(0xFF1D4ED8) to Color(0xFF7C2A64),
    Color(0xFF0EA5E9) to Color(0xFF6366F1),
    Color(0xFFF0559B) to Color(0xFF8B5CF6),
    Color(0xFF10B981) to Color(0xFF06B6D4),
    Color(0xFFF59E0B) to Color(0xFFEF4444),
    Color(0xFF64748B) to Color(0xFF0F172A)
)

/**
 * Root overflow menu with the Theme → Customize page stack.
 *
 * Back (system or in-menu arrow) walks up the stack before closing the popup.
 */
@Composable
fun SerieeOverflowMenu(
    vm: SerieeViewModel,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onBackupRestore: () -> Unit,
    onImport: () -> Unit,
    onChangelog: () -> Unit,
    onAbout: () -> Unit,
    artworkPoster: String?
) {
    val p = LocalPalette.current
    val systemDark = isSystemInDarkTheme()
    var page by remember { mutableStateOf(MenuPage.Root) }
    val maxHeight = minOf(
        560.dp,
        LocalConfiguration.current.screenHeightDp.dp * 0.72f
    ).coerceAtLeast(120.dp)

    LaunchedEffect(expanded) {
        if (expanded) page = MenuPage.Root
    }

    BackHandler(enabled = expanded) {
        if (page == MenuPage.Root) onDismiss() else page = MenuPage.Root
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = if (p.isNeoBrutalist) RoundedCornerShape(8.dp) else RoundedCornerShape(24.dp),
        containerColor = if (p.isNeoBrutalist) p.surface else if (p.isGlass) p.elevated else p.surface,
        tonalElevation = 0.dp,
        shadowElevation = if (p.isNeoBrutalist) 0.dp else 8.dp,
        border = if (p.isNeoBrutalist) BorderStroke(2.5.dp, p.border) else null,
        modifier = Modifier.width(312.dp)
    ) {
        AnimatedContent(
            targetState = page,
            label = "menuPage",
            transitionSpec = {
                val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(
                    durationMillis = 220,
                    easing = SerieeMotion.EaseOutExpo
                )
                val fadeInSpec = tween<Float>(durationMillis = 190, easing = SerieeMotion.StandardEasing)
                val fadeOutSpec = tween<Float>(durationMillis = 130, easing = SerieeMotion.StandardEasing)
                val transition = if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally(slideSpec) { it / 4 } + fadeIn(fadeInSpec)) togetherWith
                        (slideOutHorizontally(slideSpec) { -it / 4 } + fadeOut(fadeOutSpec))
                } else {
                    (slideInHorizontally(slideSpec) { -it / 4 } + fadeIn(fadeInSpec)) togetherWith
                        (slideOutHorizontally(slideSpec) { it / 4 } + fadeOut(fadeOutSpec))
                }
                transition using SizeTransform(
                    clip = true,
                    sizeAnimationSpec = { _, _ ->
                        tween(durationMillis = 220, easing = SerieeMotion.EaseOutExpo)
                    }
                )
            }
        ) { current ->
            when (current) {
                MenuPage.Root -> Column {
                    SettingRow(
                        icon = Icons.Filled.Palette,
                        title = stringResource(R.string.theme),
                        value = "${stringResource(vm.themeMode.labelRes)} · ${stringResource(vm.themeStyle.labelRes)}",
                        tint = if (p.isNeoBrutalist) p.text else p.accent
                    ) { page = MenuPage.Theme }
                    SettingRow(
                        icon = Icons.Filled.Translate,
                        title = stringResource(R.string.language),
                        value = stringResource(vm.language.labelRes)
                    ) { page = MenuPage.Language }
                    HorizontalDivider(color = p.border, thickness = if (p.isNeoBrutalist) 2.dp else 1.dp)
                    SettingRow(
                        icon = Icons.Filled.CloudDownload,
                        title = stringResource(R.string.import_collection)
                    ) {
                        onDismiss()
                        onImport()
                    }
                    SettingRow(
                        icon = Icons.Filled.Storage,
                        title = stringResource(R.string.backup_restore)
                    ) {
                        onDismiss()
                        onBackupRestore()
                    }
                    HorizontalDivider(color = p.border, thickness = if (p.isNeoBrutalist) 2.dp else 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable {
                                onDismiss()
                                onChangelog()
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Campaign, null, tint = if (p.isNeoBrutalist) p.text else p.muted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.changelog_title),
                            fontSize = 14.sp,
                            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Medium,
                            color = p.text,
                            modifier = Modifier.weight(1f)
                        )
                        val badgeShape = RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 5.dp)
                        Box(
                            Modifier
                                .clip(badgeShape)
                                .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.accent)
                                .then(
                                    if (p.isNeoBrutalist) Modifier.border(1.5.dp, NeoBrutalistColors.Black, badgeShape)
                                    else Modifier
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                stringResource(R.string.new_badge),
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.onAccent
                            )
                        }
                    }
                    SettingRow(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.about_title)
                    ) {
                        onDismiss()
                        onAbout()
                    }
                }

                MenuPage.Theme -> ThemePage(
                    vm = vm,
                    systemDark = systemDark,
                    maxHeight = maxHeight,
                    onBack = { page = MenuPage.Root },
                    onCustomize = { page = MenuPage.Customize }
                )

                MenuPage.Customize -> CustomizePage(
                    vm = vm,
                    systemDark = systemDark,
                    maxHeight = maxHeight,
                    artworkPoster = artworkPoster,
                    onBack = { page = MenuPage.Theme }
                )

                MenuPage.Language -> LanguagePage(
                    selected = vm.language,
                    onBack = { page = MenuPage.Root },
                    onLanguage = vm::setLanguage
                )
            }
        }
    }
}

@Composable
private fun MenuHeader(title: String, onBack: () -> Unit) {
    val p = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (p.isNeoBrutalist) {
            NeoBrutalistBox(
                modifier = Modifier.size(38.dp),
                onClick = onBack,
                role = Role.Button,
                backgroundColor = NeoBrutalistColors.OffWhite,
                borderColor = p.border,
                shadowColor = NeoBrutalistColors.Black,
                borderWidth = 2.dp,
                shadowOffset = 2.dp,
                cornerRadius = 6.dp,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = NeoBrutalistColors.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = p.text,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
            color = p.text
        )
    }
    HorizontalDivider(color = p.border, thickness = if (p.isNeoBrutalist) 2.dp else 1.dp)
}

@Composable
private fun SectionLabel(text: String) {
    val p = LocalPalette.current
    Text(
        text = if (p.isNeoBrutalist) text.uppercase() else text,
        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (p.isNeoBrutalist) p.text else p.muted,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 2.dp)
    )
}

@Composable
private fun ThemePage(
    vm: SerieeViewModel,
    systemDark: Boolean,
    maxHeight: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onCustomize: () -> Unit
) {
    val p = LocalPalette.current
    val custom = CustomThemeColors(
        accent = vm.customAccent,
        backgroundStart = vm.customBackgroundStart,
        backgroundEnd = vm.customBackgroundEnd,
        gradient = vm.customGradient
    )
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState())
    ) {
        MenuHeader(stringResource(R.string.theme), onBack)

        SectionLabel(stringResource(R.string.theme_appearance))
        Box(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            SegmentedTabs(
                labels = AppThemeMode.entries.map { stringResource(it.labelRes) },
                selected = vm.themeMode.ordinal,
                onSelect = { index -> vm.setThemeMode(AppThemeMode.entries[index]) }
            )
        }

        SectionLabel(stringResource(R.string.theme_style))
        AppThemeStyle.entries.forEach { style ->
            val isSelected = style == vm.themeStyle
            val selectedBg by animateColorAsState(
                targetValue = when {
                    !isSelected -> Color.Transparent
                    p.isNeoBrutalist -> NeoBrutalistColors.Yellow
                    else -> p.accentSoft
                },
                animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
                label = "themeRowBg_${style.name}"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(selectedBg)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton
                    ) { vm.setThemeStyle(style) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeSwatch(
                    mode = vm.themeMode,
                    style = style,
                    systemDark = systemDark,
                    custom = custom
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(style.labelRes),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected || p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Medium,
                        color = when {
                            p.isNeoBrutalist && isSelected -> NeoBrutalistColors.Black
                            p.isNeoBrutalist -> p.text
                            isSelected -> p.accent
                            else -> p.text
                        }
                    )
                    Text(
                        stringResource(style.descriptionRes),
                        fontSize = 10.sp,
                        color = if (p.isNeoBrutalist && isSelected) NeoBrutalistColors.Black.copy(alpha = 0.78f) else p.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = p.border,
            thickness = if (p.isNeoBrutalist) 2.dp else 1.dp,
            modifier = Modifier.padding(top = 6.dp)
        )
        SwitchRow(
            title = stringResource(R.string.theme_reduce_transparency),
            subtitle = stringResource(R.string.theme_reduce_transparency_desc),
            checked = vm.reduceTransparency,
            onCheckedChange = vm::setReduceTransparency
        )
        SwitchRow(
            title = stringResource(R.string.theme_battery_amoled),
            subtitle = stringResource(R.string.theme_battery_amoled_desc),
            checked = vm.batterySaverAmoled,
            onCheckedChange = vm::setBatterySaverAmoled
        )
        HorizontalDivider(color = p.border, thickness = if (p.isNeoBrutalist) 2.dp else 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onCustomize)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Palette,
                contentDescription = null,
                tint = if (p.isNeoBrutalist) p.text else p.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.theme_customize),
                fontSize = 13.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (p.isNeoBrutalist) p.text else p.accent
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val p = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Medium,
                color = p.text
            )
            Text(subtitle, fontSize = 10.sp, color = p.muted, lineHeight = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = if (p.isNeoBrutalist) {
                SwitchDefaults.colors(
                    checkedThumbColor = NeoBrutalistColors.Black,
                    checkedTrackColor = NeoBrutalistColors.Yellow,
                    checkedBorderColor = NeoBrutalistColors.Black,
                    uncheckedThumbColor = NeoBrutalistColors.Black,
                    uncheckedTrackColor = p.chip,
                    uncheckedBorderColor = p.border
                )
            } else {
                SwitchDefaults.colors(
                    checkedThumbColor = p.onAccent,
                    checkedTrackColor = p.accent,
                    uncheckedThumbColor = p.muted,
                    uncheckedTrackColor = p.chip,
                    uncheckedBorderColor = p.border
                )
            }
        )
    }
}

@Composable
private fun ThemeSwatch(
    mode: AppThemeMode,
    style: AppThemeStyle,
    systemDark: Boolean,
    custom: CustomThemeColors
) {
    val dark = when (mode) {
        AppThemeMode.System -> systemDark
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val dynamicScheme = rememberMaterialYouScheme(style == AppThemeStyle.MaterialYou, dark)
    val palette = previewPalette(mode, style, systemDark, custom, dynamicScheme)
    if (style == AppThemeStyle.NeoBrutalism) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .neoBrutalist(
                    backgroundColor = palette.bg,
                    borderColor = NeoBrutalistColors.Black,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    cornerRadius = 6.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(NeoBrutalistColors.Yellow)
                    .border(1.5.dp, NeoBrutalistColors.Black, RoundedCornerShape(3.dp))
            )
        }
        return
    }
    val brush = when {
        mode == AppThemeMode.System -> Brush.linearGradient(
            listOf(
                previewPalette(AppThemeMode.Light, style, false, custom, dynamicScheme).bg,
                previewPalette(AppThemeMode.Dark, style, true, custom, dynamicScheme).bg
            )
        )
        palette.gradient != null -> Brush.linearGradient(palette.gradient)
        else -> Brush.linearGradient(listOf(palette.bg, palette.card))
    }
    val outline = if (palette.isLight) palette.text.copy(alpha = 0.28f) else palette.border
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(brush)
            .border(1.dp, outline, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(palette.accent)
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomizePage(
    vm: SerieeViewModel,
    systemDark: Boolean,
    maxHeight: androidx.compose.ui.unit.Dp,
    artworkPoster: String?,
    onBack: () -> Unit
) {
    val p = LocalPalette.current
    val custom = CustomThemeColors(
        accent = vm.customAccent,
        backgroundStart = vm.customBackgroundStart,
        backgroundEnd = vm.customBackgroundEnd,
        gradient = vm.customGradient
    )
    val live = resolvePalette(
        mode = vm.themeMode,
        style = AppThemeStyle.Custom,
        systemDark = systemDark,
        custom = custom,
        reduceTransparency = vm.reduceTransparency
    )
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var pickingArtwork by remember { mutableStateOf(false) }
    var codeMessage by remember { mutableStateOf<Int?>(null) }

    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState())
    ) {
        MenuHeader(stringResource(R.string.customize_theme_title), onBack)

        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.accent),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = p.muted
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                accentPresets.forEach { preset ->
                    val selected = preset.value == vm.customAccent.value
                    val presetShape = if (p.isNeoBrutalist) RoundedCornerShape(6.dp) else CircleShape
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(presetShape)
                            .background(preset)
                            .border(
                                if (p.isNeoBrutalist) 2.dp else 2.dp,
                                when {
                                    selected && p.isNeoBrutalist -> NeoBrutalistColors.Black
                                    selected -> p.text
                                    p.isNeoBrutalist -> p.border
                                    else -> Color.Transparent
                                },
                                presetShape
                            )
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton
                            ) { vm.setCustomAccent(preset) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = if (preset.luminance() > 0.6f) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            if (p.isNeoBrutalist) {
                NeoBrutalistBox(
                    onClick = if (artworkPoster != null && !pickingArtwork) {
                        {
                            pickingArtwork = true
                            scope.launch {
                                val color = artworkPoster?.let { accentFromPoster(context, it) }
                                if (color != null) vm.setCustomAccent(color)
                                pickingArtwork = false
                            }
                        }
                    } else null,
                    enabled = artworkPoster != null && !pickingArtwork,
                    backgroundColor = if (artworkPoster != null) NeoBrutalistColors.SkyBlue else p.chip,
                    borderColor = p.border,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    cornerRadius = 6.dp,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pickingArtwork) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = NeoBrutalistColors.Black
                            )
                        } else {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = if (artworkPoster != null) NeoBrutalistColors.Black else p.muted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.theme_accent_from_artwork),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (artworkPoster != null) NeoBrutalistColors.Black else p.muted
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (artworkPoster != null) p.chip else p.chip.copy(alpha = 0.4f))
                        .clickable(enabled = artworkPoster != null && !pickingArtwork) {
                            pickingArtwork = true
                            scope.launch {
                                val color = artworkPoster?.let { accentFromPoster(context, it) }
                                if (color != null) vm.setCustomAccent(color)
                                pickingArtwork = false
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pickingArtwork) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = p.accent
                        )
                    } else {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = if (artworkPoster != null) p.accent else p.muted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.theme_accent_from_artwork),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (artworkPoster != null) p.text else p.muted
                    )
                }
            }
            if (!live.hasReadableAccent()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = p.warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.theme_accent_low_contrast),
                        fontSize = 10.sp,
                        color = p.warning
                    )
                }
            }
        }

        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.background),
                fontSize = 11.sp,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontWeight = FontWeight.Bold,
                color = p.muted
            )
            SegmentedTabs(
                labels = listOf(
                    stringResource(R.string.background_solid),
                    stringResource(R.string.background_gradient)
                ),
                selected = if (vm.customGradient) 1 else 0,
                onSelect = { vm.setCustomGradient(it == 1) }
            )
            if (vm.customGradient) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gradientPresets.forEach { (start, end) ->
                        val selected = start.value == vm.customBackgroundStart.value &&
                            end.value == vm.customBackgroundEnd.value
                        val gradRadius = if (p.isNeoBrutalist) 6.dp else 9.dp
                        Box(
                            modifier = Modifier
                                .size(width = 52.dp, height = 30.dp)
                                .clip(RoundedCornerShape(gradRadius))
                                .background(Brush.linearGradient(listOf(start, end)))
                                .border(
                                    width = if (p.isNeoBrutalist) (if (selected) 2.5.dp else 1.5.dp) else (if (selected) 2.dp else 1.dp),
                                    color = if (selected) p.text else p.border,
                                    shape = RoundedCornerShape(gradRadius)
                                )
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton
                                ) { vm.setCustomBackground(start, end) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            thickness = if (p.isNeoBrutalist) 2.dp else 1.dp,
            color = p.border,
            modifier = Modifier.padding(top = 8.dp)
        )
        CodeRow(
            icon = Icons.Filled.ContentCopy,
            label = stringResource(R.string.theme_copy_code)
        ) {
            clipboard.setText(AnnotatedString(vm.themeCode()))
            codeMessage = R.string.theme_code_copied
        }
        CodeRow(
            icon = Icons.Filled.ContentPaste,
            label = stringResource(R.string.theme_paste_code)
        ) {
            val applied = vm.applyThemeCode(clipboard.getText()?.text)
            codeMessage = if (applied) R.string.theme_code_applied else R.string.theme_code_invalid
        }
        codeMessage?.let { message ->
            Text(
                text = stringResource(message),
                fontSize = 10.sp,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }
        HorizontalDivider(
            thickness = if (p.isNeoBrutalist) 2.dp else 1.dp,
            color = p.border
        )

        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.theme_preview),
                fontSize = 11.sp,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontWeight = FontWeight.Bold,
                color = p.muted
            )
            ThemePreview(live)
        }
    }
}

@Composable
private fun CodeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (p.isNeoBrutalist) p.text else p.muted, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Medium,
            color = p.text
        )
    }
}

@Composable
private fun ThemePreview(palette: SeriePalette) {
    val p = LocalPalette.current
    val useNeo = p.isNeoBrutalist || palette.isNeoBrutalist
    val shape = RoundedCornerShape(if (useNeo) 8.dp else 16.dp)
    val previewModifier = if (useNeo) {
        Modifier
            .fillMaxWidth()
            .neoBrutalist(
                backgroundColor = palette.bg,
                borderColor = NeoBrutalistColors.Black,
                shadowColor = NeoBrutalistColors.Black,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                cornerRadius = 8.dp
            )
            .padding(12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                palette.gradient?.let { Brush.linearGradient(it) }
                    ?: Brush.linearGradient(listOf(palette.bg, palette.bg))
            )
            .border(1.dp, palette.border, shape)
            .padding(12.dp)
    }
    Box(modifier = previewModifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(if (useNeo) 6.dp else 7.dp))
                        .background(palette.primary)
                        .then(
                            if (useNeo) {
                                Modifier.border(2.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                            } else {
                                Modifier
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text("Seriee", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = palette.text)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (useNeo) NeoBrutalistColors.SkyBlue else palette.accent.copy(alpha = 0.2f))
                        .then(
                            if (useNeo) {
                                Modifier.border(1.5.dp, NeoBrutalistColors.Black, RoundedCornerShape(6.dp))
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        stringResource(R.string.theme_preview_chip),
                        fontSize = 8.sp,
                        fontFamily = if (useNeo) FontFamily.Monospace else FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = if (useNeo) NeoBrutalistColors.Black else palette.accent
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(if (useNeo) 8.dp else 12.dp))
                    .background(if (palette.isGlass) palette.card.copy(alpha = 0.75f) else palette.card)
                    .border(
                        if (useNeo) 2.dp else 1.dp,
                        if (useNeo) NeoBrutalistColors.Black else palette.border,
                        RoundedCornerShape(if (useNeo) 8.dp else 12.dp)
                    )
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.theme_preview_card_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.text
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(if (useNeo) 8.dp else 5.dp)
                            .clip(RoundedCornerShape(if (useNeo) 4.dp else 50.dp))
                            .background(palette.chip)
                            .then(
                                if (useNeo) {
                                    Modifier.border(1.5.dp, NeoBrutalistColors.Black, RoundedCornerShape(4.dp))
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.62f)
                                .height(if (useNeo) 8.dp else 5.dp)
                                .clip(RoundedCornerShape(if (useNeo) 4.dp else 50.dp))
                                .background(palette.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguagePage(
    selected: AppLanguage,
    onBack: () -> Unit,
    onLanguage: (AppLanguage) -> Unit
) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxWidth()) {
        MenuHeader(stringResource(R.string.language), onBack)
        AppLanguage.entries.forEach { option ->
            val isSelected = option == selected
            val rowBg by animateColorAsState(
                targetValue = when {
                    isSelected && p.isNeoBrutalist -> NeoBrutalistColors.Yellow
                    isSelected -> p.accentSoft
                    else -> Color.Transparent
                },
                animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
                label = "langBg_${option.name}"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(rowBg)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton
                    ) { onLanguage(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(option.labelRes),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected || p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected && p.isNeoBrutalist -> NeoBrutalistColors.Black
                        isSelected -> p.accent
                        else -> p.text
                    },
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
