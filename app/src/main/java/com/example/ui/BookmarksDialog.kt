package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class BookmarkItem(
    val title: String,
    val url: String,
    val description: String
)

val DEFAULT_BOOKMARKS = listOf(
    BookmarkItem(
        title = "Starsnin Oficial",
        url = "https://starsnin.vercel.app",
        description = "Página Inicial & Motor de Busca Starsnin"
    ),
    BookmarkItem(
        title = "Starsnin Logo Asset",
        url = "https://starsnin.vercel.app/starsninlogo.png",
        description = "Logotipo Oficial do Sistema Starsnin"
    ),
    BookmarkItem(
        title = "Starsnin Ecossistema",
        url = "https://starsnin.vercel.app/#ecossistema",
        description = "Ferramentas e Serviços Integrados"
    )
)

@Composable
fun BookmarksDialog(
    onSelectBookmark: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, StarsninBorder, RoundedCornerShape(16.dp))
                .testTag("bookmarks_dialog"),
            colors = CardDefaults.cardColors(containerColor = StarsninSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = StarsninCyan
                        )
                        Text(
                            text = "Favoritos Starsnin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = StarsninTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = StarsninTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DEFAULT_BOOKMARKS) { bookmark ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectBookmark(bookmark.url)
                                    onDismiss()
                                }
                                .border(1.dp, StarsninBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = StarsninSurfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(StarsninCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = StarsninCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bookmark.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = StarsninTextPrimary
                                    )
                                    Text(
                                        text = bookmark.url,
                                        fontSize = 11.sp,
                                        color = StarsninCyan
                                    )
                                    Text(
                                        text = bookmark.description,
                                        fontSize = 10.sp,
                                        color = StarsninTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
