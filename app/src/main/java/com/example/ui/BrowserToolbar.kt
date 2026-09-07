package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.ui.theme.*

@Composable
fun BrowserToolbar(
    activeTab: BrowserTab?,
    urlText: String,
    tabCount: Int,
    onUrlTextChange: (String) -> Unit,
    onSubmitUrl: (String) -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenInspector: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("browser_toolbar"),
        color = StarsninSurface,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Top Bar: Home | Omnibox | Tabs Counter | Menu (Chrome Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Home Button (Chrome-style left icon)
                IconButton(
                    onClick = onNavigateHome,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("toolbar_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Página Inicial",
                        tint = StarsninTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Chrome-style Omnibox (Address & Search Bar)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(StarsninSurfaceVariant)
                        .border(1.dp, StarsninBorder, RoundedCornerShape(21.dp))
                        .testTag("address_bar_container"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SSL Lock icon
                        Icon(
                            imageVector = if (urlText.startsWith("https")) Icons.Default.Lock else Icons.Default.Search,
                            contentDescription = "Segurança",
                            tint = if (urlText.startsWith("https")) StarsninEmerald else StarsninTextMuted,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Text Field for URL or Search
                        BasicTextField(
                            value = urlText,
                            onValueChange = onUrlTextChange,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("address_bar_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = StarsninTextPrimary
                            ),
                            cursorBrush = SolidColor(StarsninCyan),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { onSubmitUrl(urlText) }),
                            decorationBox = { innerTextField ->
                                if (urlText.isEmpty()) {
                                    Text(
                                        text = "Pesquise ou digite o endereço da web",
                                        fontSize = 12.sp,
                                        color = StarsninTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Action button inside Omnibox: Clear or Refresh or Go
                        if (urlText.isNotBlank()) {
                            IconButton(
                                onClick = { onUrlTextChange("") },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar URL",
                                    tint = StarsninTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (activeTab?.isLoading == true) {
                                    // Stop / Refresh toggle
                                    onRefresh()
                                } else {
                                    onSubmitUrl(urlText)
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("toolbar_go_button")
                        ) {
                            Icon(
                                imageVector = if (activeTab?.isLoading == true) Icons.Default.Refresh else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = if (activeTab?.isLoading == true) "Recarregar" else "Navegar",
                                tint = StarsninCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Chrome-style Tabs Counter Button [ 1 ]
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StarsninSurfaceVariant)
                        .border(1.5.dp, StarsninTextSecondary, RoundedCornerShape(8.dp))
                        .clickable { onOpenTabs() }
                        .testTag("toolbar_tabs_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsninCyan,
                        textAlign = TextAlign.Center
                    )
                }

                // Chrome-style 3-dots Menu (More Options)
                Box {
                    IconButton(
                        onClick = { isMenuOpen = true },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toolbar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu de Opções",
                            tint = StarsninTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Chrome Dropdown Menu
                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false },
                        modifier = Modifier
                            .background(StarsninSurface)
                            .border(1.dp, StarsninBorder, RoundedCornerShape(8.dp))
                            .width(220.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nova guia", color = StarsninTextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, tint = StarsninCyan)
                            },
                            onClick = {
                                isMenuOpen = false
                                onNewTab()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Abas verticais", color = StarsninTextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.ViewSidebar, contentDescription = null, tint = StarsninCyan)
                            },
                            onClick = {
                                isMenuOpen = false
                                onOpenTabs()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Favoritos", color = StarsninTextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.StarBorder, contentDescription = null, tint = StarsninTextSecondary)
                            },
                            onClick = {
                                isMenuOpen = false
                                onOpenBookmarks()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Recarregar", color = StarsninTextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = StarsninTextSecondary)
                            },
                            onClick = {
                                isMenuOpen = false
                                onRefresh()
                            }
                        )

                        HorizontalDivider(color = StarsninBorder, thickness = 1.dp)

                        DropdownMenuItem(
                            text = { Text("Inspetor DOM (Engine)", color = StarsninTextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Code, contentDescription = null, tint = StarsninIndigo)
                            },
                            onClick = {
                                isMenuOpen = false
                                onOpenInspector()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Sem Chromium / Zero WebView", color = StarsninEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StarsninEmerald)
                            },
                            onClick = { isMenuOpen = false }
                        )
                    }
                }
            }

            // Quick Navigation Control Strip (Back / Forward / Status indicator)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarsninDarkBg.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = if (activeTab?.canGoBack == true) StarsninCyan else StarsninTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(enabled = activeTab?.canGoBack == true) { onGoBack() }
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Avançar",
                        tint = if (activeTab?.canGoForward == true) StarsninCyan else StarsninTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(enabled = activeTab?.canGoForward == true) { onGoForward() }
                    )

                    Text(
                        text = if (activeTab?.url?.contains("starsnin.vercel.app") == true) "Starsnin Oficial" else activeTab?.title ?: "",
                        fontSize = 10.sp,
                        color = StarsninTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StarsninEmerald)
                    )
                    Text(
                        text = "Engine Independente",
                        fontSize = 9.sp,
                        color = StarsninTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Loading Progress Bar (Chrome-style thin indicator below Omnibox)
            if (activeTab?.isLoading == true) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = StarsninCyan,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
