package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.model.BrowserTab
import com.example.ui.theme.*

const val STARSNIN_OFFICIAL_LOGO_URL = "https://starsnin.vercel.app/starsninlogo.png"

@Composable
fun VerticalTabsSidebar(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onOpenInspector: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onNavigateHome: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(min = 280.dp, max = 320.dp)
            .fillMaxHeight()
            .testTag("vertical_tabs_sidebar"),
        color = StarsninDarkBg,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = StarsninBorder,
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(vertical = 12.dp, horizontal = 12.dp)
        ) {
            // Header: Official Logo, Title and Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateHome() }
                ) {
                    // Official Logo directly from https://starsnin.vercel.app/starsninlogo.png
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StarsninSurfaceVariant)
                            .border(1.dp, StarsninCyan.copy(alpha = 0.5f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(STARSNIN_OFFICIAL_LOGO_URL)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Logo Oficial Starsnin",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit,
                            loading = {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = StarsninCyan,
                                    strokeWidth = 2.dp
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Logo Oficial Starsnin Fallback",
                                    tint = StarsninCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Starsnin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = StarsninCyan
                        )
                        Text(
                            text = "Abas Verticais • ${tabs.size} abas",
                            fontSize = 11.sp,
                            color = StarsninTextMuted
                        )
                    }
                }

                // Close Drawer Button
                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar painel de abas",
                        tint = StarsninTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add New Tab Button (+)
            Button(
                onClick = onNewTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("new_tab_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StarsninSurfaceVariant,
                    contentColor = StarsninCyan
                ),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(StarsninCyan, StarsninIndigo))
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova Aba",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nova Guia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = StarsninBorder.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Vertical Tab List (Positioned vertically on the lateral corner)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTabId
                    VerticalTabItem(
                        tab = tab,
                        isActive = isActive,
                        onSelect = { onSelectTab(tab.id) },
                        onClose = { onCloseTab(tab.id) },
                        canClose = tabs.size > 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = StarsninBorder.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Lateral Utilities (Bookmarks & Engine Console)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpenBookmarks,
                    modifier = Modifier.testTag("bookmarks_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = StarsninTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Favoritos", fontSize = 12.sp, color = StarsninTextSecondary)
                }

                TextButton(
                    onClick = onOpenInspector,
                    modifier = Modifier.testTag("dom_inspector_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = StarsninCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DevTools", fontSize = 12.sp, color = StarsninCyan)
                }
            }
        }
    }
}

@Composable
fun VerticalTabItem(
    tab: BrowserTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    canClose: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) StarsninSurfaceVariant else Color.Transparent
    val borderColor = if (isActive) StarsninCyan else Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .testTag("vertical_tab_${tab.id}"),
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status / Favicon Indicator
                if (tab.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = StarsninCyan
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) StarsninCyan.copy(alpha = 0.25f) else StarsninBorder
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (tab.url.contains("starsnin.vercel.app")) Icons.Default.Public else Icons.Default.Web,
                            contentDescription = null,
                            tint = if (isActive) StarsninCyan else StarsninTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tab.title.ifBlank { "Nova Guia" },
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) StarsninTextPrimary else StarsninTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tab.url.replace("https://", "").replace("http://", ""),
                        fontSize = 10.sp,
                        color = StarsninTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (canClose) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar guia",
                        tint = if (isActive) StarsninCyan else StarsninTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
