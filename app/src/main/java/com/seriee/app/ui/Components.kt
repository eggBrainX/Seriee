package com.seriee.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriee.app.R
import com.seriee.app.data.POPULAR_TAGS

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    starSize: Dp = 26.dp,
    showLabel: Boolean = true,
) {
    val p = LocalPalette.current
    val ratingText = when (rating) {
        5 -> stringResource(R.string.rating_masterpiece)
        4 -> stringResource(R.string.rating_great)
        3 -> stringResource(R.string.rating_good)
        2 -> stringResource(R.string.rating_okay)
        1 -> stringResource(R.string.rating_poor)
        else -> stringResource(R.string.rating_none)
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..5).forEach { star ->
                val filled = star <= rating
                Icon(
                    imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = stringResource(R.string.rating_content_desc, star, 5),
                    tint = if (filled) Color(0xFFFFB703) else p.muted,
                    modifier = Modifier
                        .size(starSize)
                        .clip(RoundedCornerShape(6.dp))
                        .then(
                            if (onRatingChanged != null) {
                                Modifier.clickable {
                                    onRatingChanged(if (rating == star) 0 else star)
                                }
                            } else Modifier
                        )
                )
            }
            if (showLabel) {
                Spacer(Modifier.width(8.dp))
                Text(ratingText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.muted)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
) {
    val p = LocalPalette.current
    var draft by remember { mutableStateOf("") }

    fun addTag(raw: String) {
        val clean = raw.trim()
        if (clean.isEmpty()) return
        if (tags.any { it.equals(clean, ignoreCase = true) }) {
            draft = ""
            return
        }
        onTagsChanged(tags + clean)
        draft = ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.genre_tag_label),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = p.text
        )
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(stringResource(R.string.genre_placeholder), color = p.muted, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = p.accent,
                    unfocusedBorderColor = p.border,
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                ),
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = p.accent,
                shape = RoundedCornerShape(10.dp),
                border = if (p.isNeoBrutalist) BorderStroke(2.dp, p.border) else null,
                modifier = Modifier.clickable { addTag(draft) }
            ) {
                IconButton(onClick = { addTag(draft) }) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add), tint = p.onAccent)
                }
            }
        }
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tags.forEach { tag ->
                    val shape = RoundedCornerShape(8.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(shape)
                            .background(p.primaryContainer)
                            .border(1.dp, p.border, shape)
                            .clickable { onTagsChanged(tags.filterNot { it == tag }) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.text)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.remove_tag, tag),
                            tint = p.text,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.popular_genres), fontSize = 12.sp, color = p.muted, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            POPULAR_TAGS.filter { sug -> tags.none { it.equals(sug, ignoreCase = true) } }.forEach { sug ->
                val shape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(p.chip)
                        .border(1.dp, p.border.copy(alpha = 0.6f), shape)
                        .clickable { addTag(sug) }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text("+ $sug", fontSize = 12.sp, color = p.text)
                }
            }
        }
    }
}
