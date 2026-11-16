package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.R

private const val REPO_URL = "https://github.com/eggBrainX/Seriee"

/**
 * About / Credits screen. TMDB's API terms require the notice below to be shown
 * prominently in an About or Credits section, and JustWatch requires attribution
 * for watch-provider data.
 */
@Composable
fun AboutDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    val p = LocalPalette.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val innerContent: @Composable () -> Unit = {
            LiquidSheetHost {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 12.dp))
                                .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.primary)
                                .then(
                                    if (p.isNeoBrutalist) {
                                        Modifier.border(2.dp, p.border, RoundedCornerShape(6.dp))
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "S",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.onAccent
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.about_title),
                                fontSize = 20.sp,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                                color = p.text
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                stringResource(R.string.about_version, versionName),
                                fontSize = 12.sp,
                                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                                color = p.muted
                            )
                        }
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
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.about_close),
                                    tint = NeoBrutalistColors.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(p.chip)
                                    .clickable(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.about_close),
                                    tint = p.muted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        stringResource(R.string.about_tagline),
                        fontSize = 13.sp,
                        color = p.text,
                        lineHeight = 19.sp
                    )

                    SectionTitle(stringResource(R.string.about_data_sources))
                    BodyText(stringResource(R.string.about_tmdb_notice))
                    BodyText(stringResource(R.string.about_justwatch_notice))
                    BodyText(stringResource(R.string.about_other_sources))

                    SectionTitle(stringResource(R.string.about_privacy))
                    BodyText(stringResource(R.string.about_privacy_body))
                    LinkRow(
                        label = stringResource(R.string.about_privacy_link),
                        url = "$REPO_URL/blob/main/PRIVACY.md"
                    )

                    SectionTitle(stringResource(R.string.about_license))
                    BodyText(stringResource(R.string.about_license_value))
                    LinkRow(
                        label = stringResource(R.string.about_license_link),
                        url = "$REPO_URL/blob/main/LICENSE"
                    )
                    LinkRow(
                        label = stringResource(R.string.about_third_party_link),
                        url = "$REPO_URL/blob/main/THIRD_PARTY_NOTICES.md"
                    )

                    SectionTitle(stringResource(R.string.about_source))
                    LinkRow(label = REPO_URL.removePrefix("https://"), url = REPO_URL)

                    BodyText(stringResource(R.string.about_disclaimer))

                    PrimaryButton(
                        text = stringResource(R.string.about_close),
                        onClick = onDismiss
                    )
                }
            }
        }

        if (p.isNeoBrutalist) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .neoBrutalist(
                        backgroundColor = p.surface,
                        borderColor = p.border,
                        shadowColor = NeoBrutalistColors.Black,
                        borderWidth = 2.5.dp,
                        shadowOffset = 6.dp,
                        cornerRadius = 8.dp
                    )
            ) {
                innerContent()
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = if (p.isGlass) p.surface.copy(alpha = if (p.isLight) 0.90f else 0.84f) else p.surface,
                border = BorderStroke(1.dp, p.border),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                innerContent()
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val p = LocalPalette.current
    Text(
        if (p.isNeoBrutalist) text.uppercase() else text,
        fontSize = 13.sp,
        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
        color = p.text,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun BodyText(text: String) {
    val p = LocalPalette.current
    Text(
        text,
        fontSize = 12.sp,
        color = p.muted,
        lineHeight = 18.sp
    )
}

@Composable
private fun LinkRow(label: String, url: String) {
    val p = LocalPalette.current
    val uriHandler = LocalUriHandler.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            onClick = { runCatching { uriHandler.openUri(url) } },
            role = Role.Button,
            backgroundColor = NeoBrutalistColors.SkyBlue,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.dp,
            shadowOffset = 3.dp,
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeoBrutalistColors.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.about_open_link),
                    tint = NeoBrutalistColors.Black,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(14.dp),
                    tint = p.card,
                    blurRadius = 12.dp,
                    refractionHeight = 8.dp,
                    refractionAmount = 14.dp,
                    chromaticAberration = false
                )
                .clickable { runCatching { uriHandler.openUri(url) } }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = p.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.about_open_link),
                tint = p.muted,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
