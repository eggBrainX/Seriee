package com.seriee.app.ui

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(SerieColors.Blue600, SerieColors.Indigo500)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "S",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.about_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(R.string.about_version, versionName),
                            fontSize = 12.sp,
                            color = p.muted
                        )
                    }
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SerieColors.Blue600)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.about_close),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val p = LocalPalette.current
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(p.card)
            .border(1.dp, p.border, RoundedCornerShape(10.dp))
            .clickable { runCatching { uriHandler.openUri(url) } }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (p.isLight) SerieColors.Blue600 else SerieColors.Blue400,
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
