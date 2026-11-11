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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.R

@Composable
fun ChangelogDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    val p = LocalPalette.current

    val items = listOf(
        stringResource(R.string.changelog_15),
        stringResource(R.string.changelog_16),
        stringResource(R.string.changelog_17),
        stringResource(R.string.changelog_18),
        stringResource(R.string.changelog_19),
        stringResource(R.string.changelog_20),
        stringResource(R.string.changelog_21),
        stringResource(R.string.changelog_1),
        stringResource(R.string.changelog_2),
        stringResource(R.string.changelog_3),
        stringResource(R.string.changelog_4),
        stringResource(R.string.changelog_5),
        stringResource(R.string.changelog_6),
        stringResource(R.string.changelog_7),
        stringResource(R.string.changelog_8),
        stringResource(R.string.changelog_9),
        stringResource(R.string.changelog_10),
        stringResource(R.string.changelog_11),
        stringResource(R.string.changelog_12),
        stringResource(R.string.changelog_13),
        stringResource(R.string.changelog_14)
    )

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.changelog_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Brush.linearGradient(listOf(SerieColors.Blue600, SerieColors.Indigo500)))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                stringResource(R.string.changelog_version, versionName),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
                            contentDescription = stringResource(R.string.changelog_close),
                            tint = p.muted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                items.forEach { text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(p.card)
                            .border(1.dp, p.border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(50))
                                .background(SerieColors.Amber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Star, null, tint = SerieColors.Amber, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text,
                            fontSize = 12.sp,
                            color = p.text,
                            lineHeight = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

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
                        stringResource(R.string.changelog_got_it),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
