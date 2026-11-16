package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seriee.app.BuildConfig
import com.seriee.app.R

private const val REPO_BASE = "https://github.com/eggBrainX/Seriee"

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.border,
            shadowOffset = 6.dp,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.about_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                        Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.accent)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.about_close), tint = p.muted)
                    }
                }

                Text(stringResource(R.string.about_tagline), fontSize = 13.sp, color = p.text, lineHeight = 19.sp)

                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.about_data_sources), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                        Text(stringResource(R.string.about_tmdb_notice), fontSize = 11.sp, color = p.muted, lineHeight = 16.sp)
                        Text(stringResource(R.string.about_justwatch_notice), fontSize = 11.sp, color = p.muted, lineHeight = 16.sp)
                        Text(stringResource(R.string.about_other_sources), fontSize = 11.sp, color = p.muted, lineHeight = 16.sp)
                    }
                }

                SerieeCard(containerColor = p.cardAlt, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.about_privacy), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text)
                        Text(stringResource(R.string.about_privacy_body), fontSize = 11.sp, color = p.muted, lineHeight = 16.sp)
                    }
                }

                Text(
                    "${stringResource(R.string.about_license)}: ${stringResource(R.string.about_license_value)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.text,
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        R.string.about_source to REPO_BASE,
                        R.string.about_privacy_link to "$REPO_BASE/blob/main/PRIVACY.md",
                        R.string.about_license_link to "$REPO_BASE/blob/main/LICENSE",
                        R.string.about_third_party_link to "$REPO_BASE/blob/main/THIRD_PARTY_NOTICES.md",
                    ).forEach { (labelRes, url) ->
                        OutlinedButton(
                            onClick = { openUrl(context, url) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, p.border),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(labelRes), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = p.muted, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                Text(stringResource(R.string.about_disclaimer), fontSize = 11.sp, color = p.muted, lineHeight = 15.sp)

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.about_close), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
