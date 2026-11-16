package com.seriee.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seriee.app.R

@Composable
fun Poster(url: String, modifier: Modifier, shape: Shape = RoundedCornerShape(12.dp)) {
    val p = LocalPalette.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val effectiveShape = if (p.isNeoBrutalist) RoundedCornerShape(8.dp) else shape
    val borderMod = if (p.isNeoBrutalist) Modifier.border(2.dp, p.border, effectiveShape) else Modifier
    if (url.isNotBlank()) {
        val model: Any? = remember(url) {
            when {
                com.seriee.app.data.PosterStore.isRemoteUrl(url) -> url
                com.seriee.app.data.PosterStore.isLocalPath(url) -> com.seriee.app.data.PosterStore.fileFor(context, url) ?: url
                else -> url
            }
        }
        val request = remember(model) {
            coil.request.ImageRequest.Builder(context)
                .data(model)
                .crossfade(false)
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .build()
        }
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(effectiveShape)
                .background(p.chip)
                .then(borderMod)
        )
    } else {
        Box(
            modifier = modifier
                .clip(effectiveShape)
                .background(p.chip)
                .then(borderMod),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = if (p.isNeoBrutalist) p.text else p.muted.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        NeoBrutalistBox(
            onClick = onClick,
            role = Role.Tab,
            backgroundColor = if (selected) NeoBrutalistColors.Yellow else p.surface,
            borderColor = p.border,
            shadowColor = NeoBrutalistColors.Black,
            borderWidth = 2.dp,
            shadowOffset = 3.dp,
            cornerRadius = 6.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) NeoBrutalistColors.Black else p.text
            )
        }
        return
    }
    val shape = CircleShape
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = SerieeMotion.SnappySpring,
        label = "chipScale"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            selected -> p.primary.copy(alpha = if (p.isGlass) 0.86f else 1f)
            p.isGlass && p.isLight -> Color.White.copy(alpha = 0.56f)
            p.isGlass -> Color.White.copy(alpha = 0.12f)
            else -> p.chip
        },
        animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) p.onAccent else p.text.copy(alpha = 0.84f),
        animationSpec = tween(SerieeMotion.quick, easing = SerieeMotion.StandardEasing),
        label = "chipText"
    )
    val borderColor = when {
        selected && p.isGlass -> Color.White.copy(alpha = 0.55f)
        p.isGlass && p.isLight -> Color.White.copy(alpha = 0.72f)
        p.isGlass -> Color.White.copy(alpha = 0.22f)
        selected -> Color.Transparent
        else -> p.border
    }
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(if (p.isGlass) 0.75.dp else 1.dp, borderColor, shape)
                } else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun FieldLabel(text: String) {
    val p = LocalPalette.current
    Text(
        text = if (p.isNeoBrutalist) text.uppercase() else text,
        fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
        fontSize = 12.sp,
        fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.SemiBold,
        color = if (p.isNeoBrutalist) p.text else p.muted,
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
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    filled: Boolean = false
) {
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        val neoShape = RoundedCornerShape(8.dp)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .neoBrutalist(
                    backgroundColor = p.surface,
                    borderColor = p.border,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp,
                    cornerRadius = 8.dp
                )
        ) {
            TextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        placeholder,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = p.muted
                    )
                },
                leadingIcon = leading,
                trailingIcon = trailing,
                visualTransformation = visualTransformation,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.text
                ),
                shape = neoShape,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = p.text,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = p.muted,
                    unfocusedPlaceholderColor = p.muted,
                    focusedLeadingIconColor = p.text,
                    unfocusedLeadingIconColor = p.text,
                    focusedTrailingIconColor = p.text,
                    unfocusedTrailingIconColor = p.text
                )
            )
        }
        return
    }
    if (filled) {
        val pillShape = CircleShape
        Box(
            modifier = modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = pillShape,
                    tint = if (p.isGlass) {
                        if (p.isLight) Color.White.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.10f)
                    } else p.chip,
                    blurRadius = 12.dp,
                    refractionHeight = 10.dp,
                    refractionAmount = 16.dp,
                    chromaticAberration = false
                )
        ) {
            TextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(placeholder, fontSize = 14.sp) },
                leadingIcon = leading,
                trailingIcon = trailing,
                visualTransformation = visualTransformation,
                textStyle = TextStyle(fontSize = 14.sp),
                shape = pillShape,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = p.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = p.muted,
                    unfocusedPlaceholderColor = p.muted,
                    focusedLeadingIconColor = p.muted,
                    unfocusedLeadingIconColor = p.muted,
                    focusedTrailingIconColor = p.muted,
                    unfocusedTrailingIconColor = p.muted
                )
            )
        }
        return
    }
    val fieldShape = RoundedCornerShape(16.dp)
    val glassModifier = if (p.isGlass) {
        Modifier.liquidGlass(
            shape = fieldShape,
            tint = if (p.isLight) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
            blurRadius = 10.dp,
            refractionHeight = 8.dp,
            refractionAmount = 12.dp,
            chromaticAberration = false,
            withBorder = false
        )
    } else Modifier
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth().then(glassModifier),
        singleLine = true,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = leading,
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        textStyle = TextStyle(fontSize = 14.sp),
        shape = fieldShape,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = p.text,
            unfocusedTextColor = p.text,
            focusedContainerColor = if (p.isGlass) Color.Transparent else p.bg,
            unfocusedContainerColor = if (p.isGlass) Color.Transparent else p.bg,
            cursorColor = p.primary,
            focusedBorderColor = p.primary,
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
    val fieldShape = if (p.isNeoBrutalist) RoundedCornerShape(8.dp) else RoundedCornerShape(16.dp)
    Column(modifier) {
        if (label.isNotEmpty()) FieldLabel(label)
        Box {
            if (p.isNeoBrutalist) {
                NeoBrutalistBox(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { expanded = true },
                    role = Role.DropdownList,
                    backgroundColor = p.surface,
                    borderColor = p.border,
                    shadowColor = NeoBrutalistColors.Black,
                    borderWidth = 2.5.dp,
                    shadowOffset = 4.dp,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = valueLabel,
                            color = p.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = p.text)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = fieldShape,
                            tint = if (p.isGlass) {
                                if (p.isLight) Color.White.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.10f)
                            } else p.bg,
                            blurRadius = 10.dp,
                            refractionHeight = 8.dp,
                            refractionAmount = 12.dp,
                            chromaticAberration = false
                        )
                        .clickable { expanded = true }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
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
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = if (p.isNeoBrutalist) RoundedCornerShape(8.dp) else RoundedCornerShape(20.dp),
                containerColor = if (p.isNeoBrutalist) p.surface else if (p.isGlass) p.elevated else p.surface,
                tonalElevation = 0.dp,
                shadowElevation = if (p.isNeoBrutalist) 0.dp else 8.dp,
                border = if (p.isNeoBrutalist) BorderStroke(2.5.dp, p.border) else null
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.second,
                                color = p.text,
                                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal
                            )
                        },
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
    showLabel: Boolean = true,
    labelBelow: Boolean = false
) {
    val p = LocalPalette.current
    val starColor = p.star
    val labelText = when (rating) {
        5 -> stringResource(R.string.rating_masterpiece)
        4 -> stringResource(R.string.rating_great)
        3 -> stringResource(R.string.rating_good)
        2 -> stringResource(R.string.rating_okay)
        1 -> stringResource(R.string.rating_poor)
        else -> stringResource(R.string.rating_none)
    }
    val labelColor = if (p.isNeoBrutalist) p.text else if (rating > 0) starColor else p.muted

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..maxStars) {
                val isFilled = i <= rating
                val icon = if (isFilled) Icons.Filled.Star else Icons.Filled.StarBorder
                val tint = if (isFilled) {
                    if (p.isNeoBrutalist) NeoBrutalistColors.Black else starColor
                } else {
                    p.muted.copy(alpha = 0.45f)
                }

                Box(
                    modifier = Modifier
                        .size(starSize + 8.dp)
                        .clip(if (p.isNeoBrutalist) RoundedCornerShape(6.dp) else CircleShape)
                        .then(
                            if (p.isNeoBrutalist && isFilled) {
                                Modifier
                                    .background(NeoBrutalistColors.Yellow)
                                    .border(1.5.dp, p.border, RoundedCornerShape(6.dp))
                            } else Modifier
                        )
                        .clickable(enabled = onRatingChange != null) {
                            if (onRatingChange != null) {
                                val newRating = if (rating == i) 0 else i
                                onRatingChange(newRating)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(R.string.rating_content_desc, i, maxStars),
                        tint = tint,
                        modifier = Modifier.size(starSize)
                    )
                }
            }
            if (showLabel && !labelBelow) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = labelText,
                    fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 12.sp,
                    fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = labelColor,
                    maxLines = 1
                )
            }
        }
        if (showLabel && labelBelow) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = labelText,
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 12.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = labelColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StarRatingBadge(
    rating: Int,
    modifier: Modifier = Modifier
) {
    if (rating <= 0) return
    val p = LocalPalette.current
    if (p.isNeoBrutalist) {
        val shape = RoundedCornerShape(6.dp)
        Box(
            modifier = modifier
                .clip(shape)
                .background(NeoBrutalistColors.Yellow)
                .border(1.5.dp, NeoBrutalistColors.Black, shape)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = NeoBrutalistColors.Black,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "$rating.0",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeoBrutalistColors.Black
                )
            }
        }
        return
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = if (p.isGlass) 0.56f else 0.65f))
            .then(
                if (p.isGlass) Modifier.border(0.75.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                else Modifier
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = p.star,
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
    if (p.isNeoBrutalist) {
        val shape = RoundedCornerShape(6.dp)
        val fill = color ?: NeoBrutalistColors.SkyBlue
        Box(
            modifier = modifier
                .clip(shape)
                .background(fill)
                .border(1.5.dp, NeoBrutalistColors.Black, shape)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = tag,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeoBrutalistColors.Black
            )
        }
        return
    }
    val accent = color ?: p.primary
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = if (p.isGlass) 0.18f else 0.14f))
            .then(
                if (p.isGlass) Modifier.border(0.5.dp, accent.copy(alpha = 0.32f), CircleShape)
                else Modifier
            )
            .padding(horizontal = 7.dp, vertical = 2.dp)
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
        FieldLabel(stringResource(R.string.genre_tag_label))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val fieldShape = if (p.isNeoBrutalist) RoundedCornerShape(8.dp) else RoundedCornerShape(12.dp)
            val containerModifier = if (p.isNeoBrutalist) {
                Modifier
                    .weight(1f)
                    .neoBrutalist(
                        backgroundColor = p.surface,
                        borderColor = p.border,
                        shadowColor = NeoBrutalistColors.Black,
                        borderWidth = 2.5.dp,
                        shadowOffset = 4.dp,
                        cornerRadius = 8.dp
                    )
            } else Modifier.weight(1f)

            if (p.isNeoBrutalist) {
                Box(modifier = containerModifier) {
                    TextField(
                        value = input,
                        onValueChange = { newVal ->
                            if (newVal.endsWith(",") || newVal.endsWith("\n")) addTag(newVal)
                            else input = newVal
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.genre_placeholder), fontSize = 13.sp, color = p.muted) },
                        leadingIcon = {
                            Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = p.text, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = if (input.isNotBlank()) {
                            {
                                NeoBrutalistBox(
                                    onClick = { addTag(input) },
                                    backgroundColor = NeoBrutalistColors.Mint,
                                    borderColor = p.border,
                                    shadowColor = NeoBrutalistColors.Black,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp,
                                    cornerRadius = 6.dp,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.add),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeoBrutalistColors.Black
                                    )
                                }
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { addTag(input) }),
                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.text),
                        shape = fieldShape,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = p.text,
                            unfocusedTextColor = p.text,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = p.text,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            } else {
                OutlinedTextField(
                    value = input,
                    onValueChange = { newVal ->
                        if (newVal.endsWith(",") || newVal.endsWith("\n")) {
                            addTag(newVal)
                        } else {
                            input = newVal
                        }
                    },
                    modifier = containerModifier,
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.genre_placeholder), fontSize = 13.sp) },
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
                                    .background(p.primary)
                                    .clickable { addTag(input) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(stringResource(R.string.add), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.onAccent)
                            }
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTag(input) }),
                    textStyle = TextStyle(fontSize = 13.sp),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = p.text,
                        unfocusedTextColor = p.text,
                        focusedContainerColor = p.bg,
                        unfocusedContainerColor = p.bg,
                        cursorColor = p.primary,
                        focusedBorderColor = p.primary,
                        unfocusedBorderColor = p.border,
                        focusedPlaceholderColor = p.muted,
                        unfocusedPlaceholderColor = p.muted
                    )
                )
            }
        }

        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tags.forEach { tag ->
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = { onTagsChanged(tags - tag) },
                            backgroundColor = NeoBrutalistColors.Pink,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 2.dp,
                            cornerRadius = 6.dp,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    tag,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeoBrutalistColors.Black
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.remove_tag, tag),
                                    tint = NeoBrutalistColors.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(p.primary.copy(alpha = 0.2f))
                                .border(1.dp, p.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { onTagsChanged(tags - tag) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.remove_tag, tag),
                                    tint = p.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.popular_genres),
                fontFamily = if (p.isNeoBrutalist) FontFamily.Monospace else FontFamily.Default,
                fontSize = 10.sp,
                fontWeight = if (p.isNeoBrutalist) FontWeight.Bold else FontWeight.Normal,
                color = p.muted
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                popularGenres.forEach { genre ->
                    val isSelected = tags.any { it.equals(genre, ignoreCase = true) }
                    if (p.isNeoBrutalist) {
                        NeoBrutalistBox(
                            onClick = {
                                if (isSelected) {
                                    onTagsChanged(tags.filterNot { it.equals(genre, ignoreCase = true) })
                                } else {
                                    onTagsChanged(tags + genre)
                                }
                            },
                            backgroundColor = if (isSelected) NeoBrutalistColors.Yellow else p.surface,
                            borderColor = p.border,
                            shadowColor = NeoBrutalistColors.Black,
                            borderWidth = 2.dp,
                            shadowOffset = 2.dp,
                            cornerRadius = 6.dp,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isSelected) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = p.text,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(Modifier.width(2.dp))
                                }
                                Text(
                                    genre,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = if (isSelected) NeoBrutalistColors.Black else p.text,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) p.primary else p.chip)
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
                                    color = if (isSelected) p.onAccent else p.muted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
