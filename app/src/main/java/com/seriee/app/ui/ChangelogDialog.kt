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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
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

    // Only the current release is shown. When preparing a new version, add its
    // changelog_* strings and list them in the changelog_current array; older
    // entries stay in CHANGELOG.md. The release-notes workflow reads the same
    // array, so the dialog and the GitHub release can never drift apart.
    val items = stringArrayResource(R.array.changelog_current).toList()

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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.changelog_title),
                                fontSize = 20.sp,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.Bold,
                                color = p.text
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 100.dp))
                                    .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.primary)
                                    .then(
                                        if (p.isNeoBrutalist) {
                                            Modifier.border(2.dp, p.border, RoundedCornerShape(6.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    stringResource(R.string.changelog_version, versionName),
                                    fontSize = 11.sp,
                                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.onAccent
                                )
                            }
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
                                    contentDescription = stringResource(R.string.changelog_close),
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
                                    contentDescription = stringResource(R.string.changelog_close),
                                    tint = p.muted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    items.forEach { text ->
                        Row(
                            modifier = if (p.isNeoBrutalist) {
                                Modifier
                                    .fillMaxWidth()
                                    .neoBrutalist(
                                        backgroundColor = p.card,
                                        borderColor = p.border,
                                        shadowColor = NeoBrutalistColors.Black,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp,
                                        cornerRadius = 8.dp
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .liquidGlass(
                                        shape = RoundedCornerShape(16.dp),
                                        tint = p.card,
                                        blurRadius = 12.dp,
                                        refractionHeight = 8.dp,
                                        refractionAmount = 14.dp,
                                        chromaticAberration = false
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            },
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(if (p.isNeoBrutalist) 6.dp else 50.dp))
                                    .background(if (p.isNeoBrutalist) NeoBrutalistColors.Yellow else p.star.copy(alpha = 0.15f))
                                    .then(
                                        if (p.isNeoBrutalist) {
                                            Modifier.border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    null,
                                    tint = if (p.isNeoBrutalist) NeoBrutalistColors.Black else p.star,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text,
                                fontSize = 12.sp,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Medium else FontWeight.Normal,
                                color = p.text,
                                lineHeight = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    PrimaryButton(
                        text = stringResource(R.string.changelog_got_it),
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
