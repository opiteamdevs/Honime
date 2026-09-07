package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DomElement
import com.example.engine.DomNode
import com.example.engine.DomText
import com.example.model.BrowserTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomInspectorSheet(
    tab: BrowserTab?,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: DOM Tree, 1: Source, 2: Headers & Telemetry

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarsninDarkBg,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("dom_inspector_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = StarsninCyan
                    )
                    Text(
                        text = "Inspetor da Engine Starsnin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsninTextPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = StarsninTextSecondary
                    )
                }
            }

            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = StarsninDarkBg,
                contentColor = StarsninCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Árvore DOM (${tab?.domTree?.totalNodes() ?: 0} nós)") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("HTML Bruto") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Headers & Telemetria") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // DOM Tree hierarchy
                    if (tab?.domTree != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp)
                        ) {
                            item {
                                DomElementNodeView(element = tab.domTree, depth = 0)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma árvore DOM disponível no momento",
                                color = StarsninTextMuted
                            )
                        }
                    }
                }
                1 -> {
                    // Raw HTML Source
                    val html = tab?.rawHtml?.ifEmpty { "<!-- Nenhum código carregado -->" } ?: ""
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF04060C))
                            .border(1.dp, StarsninBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = html,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = StarsninCyanBright,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
                2 -> {
                    // Headers and Engine Telemetry
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StarsninSurface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Estatísticas da Requisição", fontWeight = FontWeight.Bold, color = StarsninCyan)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("URL: ${tab?.url}", fontSize = 12.sp, color = StarsninTextPrimary)
                                    Text("Status: ${tab?.statusCode} OK", fontSize = 12.sp, color = StarsninEmerald)
                                    Text("Latência de Rede: ${tab?.latencyMs} ms", fontSize = 12.sp, color = StarsninTextSecondary)
                                    Text("Engine: Starsnin Custom Tokenizer & DOM Builder (No Chromium)", fontSize = 12.sp, color = StarsninIndigo)
                                }
                            }
                        }

                        item {
                            Text("Headers HTTP Recebidos", fontWeight = FontWeight.SemiBold, color = StarsninTextPrimary)
                        }

                        if (tab?.headers?.isNotEmpty() == true) {
                            tab.headers.forEach { (k, v) ->
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(StarsninSurfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(k, color = StarsninCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        Text(v.take(40), color = StarsninTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        } else {
                            item {
                                Text("Nenhum header capturado", color = StarsninTextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DomElementNodeView(element: DomElement, depth: Int) {
    val indent = (depth * 14).dp
    Column(modifier = Modifier.padding(start = indent, top = 2.dp, bottom = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "<${element.tag}",
                color = StarsninCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            element.attributes.forEach { (k, v) ->
                Text(
                    text = " $k=\"${v.take(20)}\"",
                    color = StarsninIndigo,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
            Text(
                text = if (element.children.isEmpty()) " />" else ">",
                color = StarsninCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        if (depth < 5) {
            for (child in element.children.take(20)) {
                when (child) {
                    is DomText -> {
                        val trimmed = child.text.trim()
                        if (trimmed.isNotEmpty()) {
                            Text(
                                text = "\"${trimmed.take(40)}\"",
                                color = StarsninTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = (indent + 12.dp))
                            )
                        }
                    }
                    is DomElement -> {
                        DomElementNodeView(element = child, depth = depth + 1)
                    }
                }
            }
        }

        if (element.children.isNotEmpty()) {
            Text(
                text = "</${element.tag}>",
                color = StarsninCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}
