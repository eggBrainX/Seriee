package com.seriee.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seriee.app.BuildConfig
import com.seriee.app.R

@Composable
fun ChangelogDialog(onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val bullets = stringArrayResource(R.array.changelog_current)
    Dialog(onDismissRequest = onDismiss) {
        NeoBrutalistBox(
            containerColor = p.surface,
            borderColor = p.border,
            shadowOffset = 6.dp,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.changelog_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = p.text)
                        Text(stringResource(R.string.changelog_version, BuildConfig.VERSION_NAME), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.accent)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.changelog_close), tint = p.muted)
                    }
                }

                bullets.forEach { entry ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("•", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = p.accent)
                        Text(entry, fontSize = 13.sp, color = p.text, lineHeight = 19.sp, modifier = Modifier.weight(1f))
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.changelog_got_it), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
