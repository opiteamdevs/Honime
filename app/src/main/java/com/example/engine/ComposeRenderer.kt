package com.example.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@Composable
fun RenderDomTree(
    root: DomElement,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Render all child nodes recursively
        for (child in root.children) {
            RenderNode(child, onNavigate = onNavigate)
        }
    }
}

@Composable
fun RenderNode(
    node: DomNode,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (node) {
        is DomText -> {
            val text = node.text.trim()
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    color = StarsninTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = modifier
                )
            }
        }
        is DomElement -> {
            RenderElement(element = node, onNavigate = onNavigate, modifier = modifier)
        }
    }
}

@Composable
fun RenderElement(
    element: DomElement,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tag = element.tag.lowercase()

    // Skip metadata elements
    if (tag == "head" || tag == "title" || tag == "meta" || tag == "link" || tag == "style" || tag == "script") {
        return
    }

    val styleBgColor = parseColor(element.styles["background-color"] ?: element.styles["background"])
    val styleTextColor = parseColor(element.styles["color"])
    val stylePadding = parseSpacing(element.styles["padding"])
    val styleMargin = parseSpacing(element.styles["margin"])
    val isFlex = element.styles["display"]?.contains("flex", ignoreCase = true) == true

    var elementModifier = modifier
    if (styleMargin > 0) {
        elementModifier = elementModifier.padding(styleMargin.dp)
    }
    if (styleBgColor != null) {
        elementModifier = elementModifier
            .clip(RoundedCornerShape(8.dp))
            .background(styleBgColor)
    }
    if (stylePadding > 0) {
        elementModifier = elementModifier.padding(stylePadding.dp)
    }

    when (tag) {
        "h1" -> {
            Text(
                text = element.getAllText(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = styleTextColor ?: StarsninCyan,
                lineHeight = 32.sp,
                modifier = elementModifier.padding(vertical = 4.dp)
            )
        }
        "h2" -> {
            Text(
                text = element.getAllText(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = styleTextColor ?: StarsninIndigo,
                lineHeight = 26.sp,
                modifier = elementModifier.padding(vertical = 4.dp)
            )
        }
        "h3" -> {
            Text(
                text = element.getAllText(),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = styleTextColor ?: StarsninTextPrimary,
                modifier = elementModifier.padding(vertical = 2.dp)
            )
        }
        "h4", "h5", "h6" -> {
            Text(
                text = element.getAllText(),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = styleTextColor ?: StarsninTextPrimary,
                modifier = elementModifier.padding(vertical = 2.dp)
            )
        }
        "p" -> {
            Column(
                modifier = elementModifier.padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (child in element.children) {
                    RenderNode(child, onNavigate = onNavigate)
                }
            }
        }
        "a" -> {
            val href = element.href ?: ""
            val linkText = element.getAllText().ifEmpty { href }
            Text(
                text = linkText,
                color = StarsninCyanBright,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = elementModifier
                    .clickable(enabled = href.isNotEmpty()) {
                        onNavigate(href)
                    }
                    .padding(vertical = 2.dp)
            )
        }
        "img" -> {
            val src = element.src ?: ""
            val alt = element.alt ?: "Imagem"
            if (src.isNotEmpty()) {
                Box(
                    modifier = elementModifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(src)
                            .crossfade(true)
                            .build(),
                        contentDescription = alt,
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = StarsninCyan,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StarsninSurfaceVariant),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = StarsninTextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = alt.ifEmpty { "Imagem web" },
                                        fontSize = 12.sp,
                                        color = StarsninTextMuted
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
        "button" -> {
            Button(
                onClick = {
                    element.attributes["onclick"]?.let { /* custom action */ }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = styleBgColor ?: StarsninCyan,
                    contentColor = styleTextColor ?: Color(0xFF003544)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = elementModifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = element.getAllText().ifEmpty { "Botão" },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        "input" -> {
            val type = element.attributes["type"] ?: "text"
            val placeholder = element.placeholder ?: ""
            var inputVal by remember(element.value) { mutableStateOf(element.value ?: "") }

            if (type == "submit" || type == "button") {
                Button(
                    onClick = { /* form submit */ },
                    colors = ButtonDefaults.buttonColors(containerColor = StarsninCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = elementModifier
                ) {
                    Text(text = element.value ?: "Enviar", color = Color(0xFF003544))
                }
            } else {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    placeholder = { Text(placeholder, color = StarsninTextMuted) },
                    modifier = elementModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StarsninCyan,
                        unfocusedBorderColor = StarsninBorder,
                        focusedTextColor = StarsninTextPrimary,
                        unfocusedTextColor = StarsninTextPrimary,
                        focusedContainerColor = StarsninSurfaceVariant,
                        unfocusedContainerColor = StarsninSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (inputVal.isNotBlank()) {
                                onNavigate(inputVal)
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputVal.isNotBlank()) {
                                    onNavigate(inputVal)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Buscar",
                                tint = StarsninCyan
                            )
                        }
                    }
                )
            }
        }
        "hr" -> {
            HorizontalDivider(
                color = StarsninBorder,
                thickness = 1.dp,
                modifier = elementModifier.padding(vertical = 8.dp)
            )
        }
        "br" -> {
            Spacer(modifier = Modifier.height(4.dp))
        }
        "code", "pre" -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF050810)),
                shape = RoundedCornerShape(6.dp),
                modifier = elementModifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, StarsninBorder, RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = element.getAllText(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = StarsninCyanBright,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                )
            }
        }
        "blockquote" -> {
            Box(
                modifier = elementModifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.dp,
                        color = StarsninBorder,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(StarsninSurfaceVariant)
                    .padding(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .background(StarsninIndigo, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = element.getAllText(),
                        fontStyle = FontStyle.Italic,
                        color = StarsninTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
        "ul", "ol" -> {
            Column(
                modifier = elementModifier.padding(vertical = 4.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                element.children.filterIsInstance<DomElement>().forEachIndexed { idx, li ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (tag == "ol") "${idx + 1}." else "•",
                            color = StarsninCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Column {
                            for (child in li.children) {
                                RenderNode(child, onNavigate = onNavigate)
                            }
                        }
                    }
                }
            }
        }
        else -> {
            // General container: div, section, article, header, nav, main, footer, table, etc.
            if (isFlex) {
                Row(
                    modifier = elementModifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (child in element.children) {
                        RenderNode(child, onNavigate = onNavigate)
                    }
                }
            } else {
                Column(
                    modifier = elementModifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (child in element.children) {
                        RenderNode(child, onNavigate = onNavigate)
                    }
                }
            }
        }
    }
}

private fun parseSpacing(cssValue: String?): Int {
    if (cssValue.isNullOrBlank()) return 0
    val digits = cssValue.takeWhile { it.isDigit() }
    return digits.toIntOrNull()?.coerceIn(0, 32) ?: 0
}

private fun parseColor(cssColor: String?): Color? {
    if (cssColor.isNullOrBlank()) return null
    val c = cssColor.trim().lowercase()
    if (c.startsWith("#")) {
        val hex = c.substring(1)
        return try {
            when (hex.length) {
                3 -> {
                    val r = hex[0].toString().repeat(2).toInt(16)
                    val g = hex[1].toString().repeat(2).toInt(16)
                    val b = hex[2].toString().repeat(2).toInt(16)
                    Color(r, g, b)
                }
                6 -> Color(android.graphics.Color.parseColor(c))
                8 -> Color(android.graphics.Color.parseColor(c))
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    return when (c) {
        "black" -> Color(0xFF000000)
        "white" -> Color(0xFFFFFFFF)
        "transparent" -> Color.Transparent
        "red" -> Color(0xFFEF4444)
        "blue" -> Color(0xFF3B82F6)
        "green" -> Color(0xFF10B981)
        "yellow" -> Color(0xFFF59E0B)
        "cyan" -> Color(0xFF06B6D4)
        else -> null
    }
}
