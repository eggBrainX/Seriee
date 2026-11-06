package com.seriee.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun Poster(url: String, modifier: Modifier, shape: Shape = RoundedCornerShape(12.dp)) {
    val p = LocalPalette.current
    if (url.isNotBlank()) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(shape)
                .background(p.chip)
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(p.chip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = p.muted.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SerieColors.Blue600 else p.chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else p.muted
        )
    }
}

@Composable
fun FieldLabel(text: String) {
    val p = LocalPalette.current
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = p.muted,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun SField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val p = LocalPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = leading,
        trailingIcon = trailing,
        textStyle = TextStyle(fontSize = 14.sp),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = p.text,
            unfocusedTextColor = p.text,
            focusedContainerColor = p.bg,
            unfocusedContainerColor = p.bg,
            cursorColor = SerieColors.Blue500,
            focusedBorderColor = SerieColors.Blue500,
            unfocusedBorderColor = p.border,
            focusedPlaceholderColor = p.muted,
            unfocusedPlaceholderColor = p.muted,
            focusedLeadingIconColor = p.muted,
            unfocusedLeadingIconColor = p.muted
        )
    )
}

@Composable
fun <T> DropdownField(
    label: String,
    valueLabel: String,
    options: List<Pair<T, String>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        if (label.isNotEmpty()) FieldLabel(label)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(p.bg)
                    .border(1.dp, p.border, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valueLabel,
                    color = p.text,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = p.muted)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.second) },
                        onClick = {
                            onSelect(option.first)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 24.dp,
    showLabel: Boolean = true
) {
    val p = LocalPalette.current
    val starColor = SerieColors.Amber

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..maxStars) {
                val isFilled = i <= rating
                val icon = if (isFilled) Icons.Filled.Star else Icons.Filled.StarBorder
                val tint = if (isFilled) starColor else p.muted.copy(alpha = 0.45f)

                Box(
                    modifier = Modifier
                        .size(starSize + 8.dp)
                        .clip(CircleShape)
                        .clickable(enabled = onRatingChange != null) {
                            if (onRatingChange != null) {
                                // Toggle off if clicking the currently selected star
                                val newRating = if (rating == i) 0 else i
                                onRatingChange(newRating)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Rating $i dari $maxStars",
                        tint = tint,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
            if (showLabel) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = when (rating) {
                        5 -> "5.0 (Masterpiece)"
                        4 -> "4.0 (Sangat Bagus)"
                        3 -> "3.0 (Bagus)"
                        2 -> "2.0 (Biasa)"
                        1 -> "1.0 (Kurang)"
                        else -> "Belum Dinilai"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (rating > 0) starColor else p.muted
                )
            }
        }
    }
}

@Composable
fun StarRatingBadge(
    rating: Int,
    modifier: Modifier = Modifier
) {
    if (rating <= 0) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = SerieColors.Amber,
                modifier = Modifier.size(11.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "$rating.0",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun GenreTagBadge(
    tag: String,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val p = LocalPalette.current
    val accent = color ?: SerieColors.Blue400
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent
        )
    }
}

@Composable
fun TagInputField(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalPalette.current
    var input by remember { mutableStateOf("") }

    val popularGenres = listOf(
        "Action", "Sci-Fi", "Drama", "Comedy", "Fantasy",
        "Anime", "Horror", "Romance", "Thriller", "Adventure", "Mystery"
    )

    fun addTag(raw: String) {
        val clean = raw.trim().replace(",", "")
        if (clean.isNotBlank() && !tags.any { it.equals(clean, ignoreCase = true) }) {
            onTagsChanged(tags + clean)
        }
        input = ""
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel("Genre / Kategori Tag (Opsional)")

        // Tag Input Text Field with Add Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { newVal ->
                    if (newVal.endsWith(",") || newVal.endsWith("\n")) {
                        addTag(newVal)
                    } else {
                        input = newVal
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Ketik genre lalu tekan tambah...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.LocalOffer,
                        contentDescription = null,
                        tint = p.muted,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = if (input.isNotBlank()) {
                    {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SerieColors.Blue600)
                                .clickable { addTag(input) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addTag(input) }),
                textStyle = TextStyle(fontSize = 13.sp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                    focusedContainerColor = p.bg,
                    unfocusedContainerColor = p.bg,
                    cursorColor = SerieColors.Blue500,
                    focusedBorderColor = SerieColors.Blue500,
                    unfocusedBorderColor = p.border,
                    focusedPlaceholderColor = p.muted,
                    unfocusedPlaceholderColor = p.muted
                )
            )
        }

        // Active tags chips (removable)
        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SerieColors.Blue600.copy(alpha = 0.2f))
                            .border(1.dp, SerieColors.Blue600.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onTagsChanged(tags - tag) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SerieColors.Blue400
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Hapus tag $tag",
                                tint = SerieColors.Blue400,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Popular Genre Suggestions
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Saran Genre Populer:", fontSize = 10.sp, color = p.muted)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                popularGenres.forEach { genre ->
                    val isSelected = tags.any { it.equals(genre, ignoreCase = true) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) SerieColors.Blue600 else p.chip)
                            .clickable {
                                if (isSelected) {
                                    onTagsChanged(tags.filterNot { it.equals(genre, ignoreCase = true) })
                                } else {
                                    onTagsChanged(tags + genre)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isSelected) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = p.muted,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                            }
                            Text(
                                genre,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else p.muted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

