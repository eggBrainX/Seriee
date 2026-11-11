package com.seriee.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.AppLanguage
import com.seriee.app.data.AppTheme

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

/**
 * Root overflow menu with the Theme → Customize page stack.
 *
 * Back (system or in-menu arrow) walks up the stack before closing the popup.
 */
@Composable
fun SerieeOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    theme: AppTheme,
    customAccent: Color,
    customGradient: Boolean,
    onTheme: (AppTheme) -> Unit,
    onAccent: (Color) -> Unit,
    onGradient: (Boolean) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    onBackupRestore: () -> Unit,
    onImport: () -> Unit,
    onChangelog: () -> Unit,
    onAbout: () -> Unit
) {
    val p = LocalPalette.current
    val systemDark = isSystemInDarkTheme()
    var page by remember { mutableStateOf(MenuPage.Root) }
    val maxHeight = minOf(
        560.dp,
        LocalConfiguration.current.screenHeightDp.dp * 0.7f
    )

    LaunchedEffect(expanded) {
        if (expanded) page = MenuPage.Root
    }

    BackHandler(enabled = expanded) {
        if (page == MenuPage.Root) onDismiss() else page = MenuPage.Root
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(304.dp)
    ) {
        AnimatedContent(
            targetState = page,
            label = "menuPage",
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 3 } + fadeOut())
                }
            }
        ) { current ->
            when (current) {
                MenuPage.Root -> Column {
                    SettingRow(
                        icon = Icons.Filled.Palette,
                        title = stringResource(R.string.theme),
                        value = stringResource(theme.labelRes),
                        tint = p.accent
                    ) { page = MenuPage.Theme }
                    SettingRow(
                        icon = Icons.Filled.Translate,
                        title = stringResource(R.string.language),
                        value = stringResource(language.labelRes)
                    ) { page = MenuPage.Language }
                    HorizontalDivider(color = p.border)
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
                    HorizontalDivider(color = p.border)
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
                        Icon(Icons.Filled.Campaign, null, tint = p.muted, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.changelog_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = p.text,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(p.accent)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                stringResource(R.string.new_badge),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.onAccent
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
                    selected = theme,
                    systemDark = systemDark,
                    customAccent = customAccent,
                    customGradient = customGradient,
                    maxHeight = maxHeight,
                    onBack = { page = MenuPage.Root },
                    onTheme = onTheme,
                    onCustomize = { page = MenuPage.Customize }
                )

                MenuPage.Customize -> CustomizePage(
                    accent = customAccent,
                    gradient = customGradient,
                    maxHeight = maxHeight,
                    onBack = { page = MenuPage.Theme },
                    onAccent = onAccent,
                    onGradient = onGradient
                )

                MenuPage.Language -> LanguagePage(
                    selected = language,
                    onBack = { page = MenuPage.Root },
                    onLanguage = onLanguage
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
        Spacer(Modifier.width(6.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.text)
    }
    HorizontalDivider(color = p.border)
}

@Composable
private fun ThemePage(
    selected: AppTheme,
    systemDark: Boolean,
    customAccent: Color,
    customGradient: Boolean,
    maxHeight: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onTheme: (AppTheme) -> Unit,
    onCustomize: () -> Unit
) {
    val p = LocalPalette.current
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState())
    ) {
        MenuHeader(stringResource(R.string.theme), onBack)
        AppTheme.entries.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .background(if (isSelected) p.accentSoft else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton
                    ) { onTheme(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeSwatch(option, systemDark, customAccent, customGradient)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(option.labelRes),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) p.accent else p.text
                    )
                    Text(
                        stringResource(option.descriptionRes),
                        fontSize = 10.sp,
                        color = p.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = p.accent, modifier = Modifier.size(18.dp))
                }
            }
        }
        HorizontalDivider(color = p.border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onCustomize)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Palette, contentDescription = null, tint = p.accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.theme_customize),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = p.accent
            )
        }
    }
}

@Composable
private fun ThemeSwatch(
    theme: AppTheme,
    systemDark: Boolean,
    customAccent: Color,
    customGradient: Boolean
) {
    val palette = if (theme == AppTheme.Custom) {
        resolvePalette(AppTheme.Custom, customAccent = customAccent, customGradient = customGradient)
    } else {
        previewPalette(theme, systemDark)
    }
    val brush = if (theme == AppTheme.FollowSystem) {
        Brush.linearGradient(listOf(Color(0xFFF7F9FC), Color(0xFF0A0F18)))
    } else {
        Brush.linearGradient(listOf(palette.bg, palette.card))
    }
    val outline = if (theme == AppTheme.Amoled || theme == AppTheme.Light) {
        palette.text.copy(alpha = 0.35f)
    } else {
        palette.border
    }
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
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomizePage(
    accent: Color,
    gradient: Boolean,
    maxHeight: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onAccent: (Color) -> Unit,
    onGradient: (Boolean) -> Unit
) {
    val p = LocalPalette.current
    val live = resolvePalette(AppTheme.Custom, customAccent = accent, customGradient = gradient)

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
                    val selected = preset.value == accent.value
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(preset)
                            .border(
                                2.dp,
                                if (selected) p.text else Color.Transparent,
                                CircleShape
                            )
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton
                            ) { onAccent(preset) },
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
        }

        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.background),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = p.muted
            )
            SegmentedTabs(
                labels = listOf(
                    stringResource(R.string.background_solid),
                    stringResource(R.string.background_gradient)
                ),
                selected = if (gradient) 1 else 0,
                onSelect = { onGradient(it == 1) }
            )
        }

        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.theme_preview),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = p.muted
            )
            ThemePreview(live)
        }
    }
}

@Composable
private fun ThemePreview(palette: SeriePalette) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                palette.gradient?.let { Brush.linearGradient(it) }
                    ?: Brush.linearGradient(listOf(palette.bg, palette.bg))
            )
            .border(1.dp, palette.border, shape)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Brush.linearGradient(listOf(palette.primary, palette.accent)))
                )
                Spacer(Modifier.width(8.dp))
                Text("Seriee", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = palette.text)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.accent.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        stringResource(R.string.theme_preview_chip),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (palette.isGlass) palette.card.copy(alpha = 0.75f) else palette.card)
                    .border(1.dp, palette.border, RoundedCornerShape(12.dp))
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
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(palette.chip)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.62f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(palette.primary, palette.accent)))
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(if (isSelected) p.accentSoft else Color.Transparent)
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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) p.accent else p.text,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = p.accent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
