package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

const val STARSNIN_HOME_URL = "https://starsnin.vercel.app"

data class ChromeShortcut(
    val title: String,
    val url: String,
    val icon: ImageVector,
    val accentColor: Color
)

data class SearchResultItem(
    val title: String,
    val displayUrl: String,
    val targetUrl: String,
    val snippet: String,
    val tag: String = "Web"
)

@Composable
fun StarsninHomeView(
    currentQuery: String = "",
    onSearch: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember(currentQuery) { mutableStateOf(currentQuery) }
    var selectedFilter by remember { mutableStateOf("Tudo") }
    val scrollState = rememberScrollState()

    val shortcuts = remember {
        listOf(
            ChromeShortcut("Starsnin", STARSNIN_HOME_URL, Icons.Default.Public, StarsninCyan),
            ChromeShortcut("Wikipédia", "https://pt.wikipedia.org", Icons.Default.MenuBook, Color(0xFF64B5F6)),
            ChromeShortcut("Notícias", "https://starsnin.vercel.app?q=noticias", Icons.Default.Newspaper, Color(0xFF81C784)),
            ChromeShortcut("GitHub", "https://github.com", Icons.Default.Code, Color(0xFFBA68C8)),
            ChromeShortcut("Tecnologia", "https://starsnin.vercel.app?q=tecnologia", Icons.Default.Devices, Color(0xFFFFB74D)),
            ChromeShortcut("Ciência", "https://starsnin.vercel.app?q=ciencia", Icons.Default.Science, Color(0xFF4DD0E1)),
            ChromeShortcut("Privacidade", "https://starsnin.vercel.app?q=privacidade", Icons.Default.Shield, Color(0xFFAED581)),
            ChromeShortcut("Ferramentas", "https://starsnin.vercel.app?q=ferramentas", Icons.Default.Build, Color(0xFFFF8A65))
        )
    }

    val searchResults = remember(currentQuery) {
        generateSmartResults(currentQuery)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StarsninDarkBg)
            .testTag("starsnin_home_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentQuery.isBlank()) {
                // ==========================================
                // 1. CHROME-LIKE NEW TAB PAGE (STARSIN HOME)
                // ==========================================
                Spacer(modifier = Modifier.height(24.dp))

                // Official Logo directly from https://starsnin.vercel.app/starsninlogo.png
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(StarsninCyan.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(StarsninSurfaceVariant)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(listOf(StarsninCyan, StarsninIndigo)),
                                shape = CircleShape
                            )
                            .padding(6.dp),
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
                                    modifier = Modifier.size(24.dp),
                                    color = StarsninCyan,
                                    strokeWidth = 2.dp
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Logo Oficial Starsnin",
                                    tint = StarsninCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        )
                    }
                }

                // Brand Name
                Text(
                    text = "Starsnin",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarsninTextPrimary,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Motor de Busca & Navegador com Engine Própria",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = StarsninCyan,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Chrome-style Big Search Pill
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, StarsninBorder, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = StarsninSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = StarsninCyan,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("starsnin_search_input"),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = StarsninTextPrimary
                            ),
                            cursorBrush = SolidColor(StarsninCyan),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) onSearch(searchQuery)
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Pesquise na Web ou digite o URL",
                                        fontSize = 14.sp,
                                        color = StarsninTextMuted
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (searchQuery.isNotBlank()) onSearch(searchQuery)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("starsnin_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Buscar",
                                    tint = StarsninCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Chrome-style Grid of Circular Shortcuts (Most Visited)
                Text(
                    text = "Atalhos rápidos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StarsninTextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    shortcuts.take(4).forEach { item ->
                        ShortcutCircleItem(item = item, onClick = { onOpenUrl(item.url) })
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    shortcuts.drop(4).take(4).forEach { item ->
                        ShortcutCircleItem(item = item, onClick = { onOpenUrl(item.url) })
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Chrome-style Discover / Recommended Articles from Starsnin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Destaques do Ecossistema",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarsninTextPrimary
                    )
                    Text(
                        text = "starsnin.vercel.app",
                        fontSize = 11.sp,
                        color = StarsninCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Article 1
                DiscoverArticleCard(
                    title = "Navegador com Engine Própria e Zero Chromium",
                    source = "Starsnin Tech • Há 2 horas",
                    description = "Conheça como a arquitetura do Starsnin constrói o pipeline de renderização e estrutura de abas verticais sem nenhuma dependência de Chromium.",
                    tag = "Arquitetura",
                    onClick = { onOpenUrl("https://starsnin.vercel.app?q=arquitetura+engine") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Article 2
                DiscoverArticleCard(
                    title = "Motor de Busca com Foco em Privacidade e Indexação Ágil",
                    source = "Ecossistema Starsnin • Hoje",
                    description = "Pesquise na web com anonimato, sem cookies de rastreamento invasivos e com resultados instantâneos.",
                    tag = "Busca",
                    onClick = { onOpenUrl("https://starsnin.vercel.app?q=motor+de+busca+privacidade") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Article 3
                DiscoverArticleCard(
                    title = "Abas Verticais: A Revolução de Produtividade no Mobile e Desktop",
                    source = "Design & UI • Ontem",
                    description = "O novo formato vertical permite gerenciar dezenas de guias sem perder o contexto visual e com máxima ergonomia.",
                    tag = "UX / UI",
                    onClick = { onOpenUrl("https://starsnin.vercel.app?q=abas+verticais") }
                )

            } else {
                // ==========================================
                // 2. CHROME / STARSIN SEARCH RESULTS PAGE
                // ==========================================
                // Filter Tabs (Tudo, Notícias, Imagens, etc.)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Tudo", "Notícias", "Imagens", "Artigos", "Tecnologia", "Ferramentas")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StarsninCyan.copy(alpha = 0.2f),
                                selectedLabelColor = StarsninCyan,
                                containerColor = StarsninSurfaceVariant,
                                labelColor = StarsninTextMuted
                            )
                        )
                    }
                }

                // Search Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resultados para \"$currentQuery\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StarsninTextPrimary
                    )
                    Text(
                        text = "Aprox. 42.100 resultados (0.14s)",
                        fontSize = 11.sp,
                        color = StarsninTextMuted
                    )
                }

                // Results List
                searchResults.forEach { result ->
                    SearchResultCard(
                        result = result,
                        onClick = { onOpenUrl(result.targetUrl) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Related Searches box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StarsninSurfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Pesquisas relacionadas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsninCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            "$currentQuery motor de busca",
                            "$currentQuery starsnin oficial",
                            "$currentQuery engine independente",
                            "como usar $currentQuery no starsnin"
                        ).forEach { related ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearch(related) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = StarsninTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = related,
                                    fontSize = 13.sp,
                                    color = StarsninTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Engine Guarantee Footer
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StarsninBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = StarsninSurface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Segurança",
                        tint = StarsninEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Renderização Independente Garantida",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarsninTextPrimary
                        )
                        Text(
                            text = "Sem Chromium • Sem WebViews nativas • Arquitetura 100% própria",
                            fontSize = 10.sp,
                            color = StarsninTextMuted
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ShortcutCircleItem(
    item: ChromeShortcut,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(StarsninSurfaceVariant)
                .border(1.dp, item.accentColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            color = StarsninTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DiscoverArticleCard(
    title: String,
    source: String,
    description: String,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, StarsninBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = StarsninSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = source,
                    fontSize = 11.sp,
                    color = StarsninTextMuted
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StarsninCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tag,
                        fontSize = 10.sp,
                        color = StarsninCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StarsninTextPrimary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = StarsninTextSecondary,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResultItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(1.dp, StarsninBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = StarsninSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // URL & Lock
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = StarsninEmerald,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = result.displayUrl,
                    fontSize = 11.sp,
                    color = StarsninTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title (Clickable link style)
            Text(
                text = result.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = StarsninCyan,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Snippet Description
            Text(
                text = result.snippet,
                fontSize = 13.sp,
                color = StarsninTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

private fun generateSmartResults(query: String): List<SearchResultItem> {
    val clean = query.trim().lowercase()
    return listOf(
        SearchResultItem(
            title = "Starsnin Portal Oficial • Ecossistema e Motor de Busca",
            displayUrl = "https://starsnin.vercel.app",
            targetUrl = "https://starsnin.vercel.app",
            snippet = "Portal oficial do Starsnin. Navegue pelo ecossistema independente, faça buscas com privacidade reforçada e utilize o navegador com renderização própria e abas verticais.",
            tag = "Oficial"
        ),
        SearchResultItem(
            title = "$query — Notícias e Atualizações em Tempo Real",
            displayUrl = "https://starsnin.vercel.app/news?q=$clean",
            targetUrl = "https://starsnin.vercel.app?q=${clean}+noticias",
            snippet = "Confira as principais notícias e tendências sobre $query. Acompanhe análises, novidades tecnológicas e relatórios do setor sem intermediários e sem Chromium.",
            tag = "Notícias"
        ),
        SearchResultItem(
            title = "O que é $query? Definições e Visão Geral Completa",
            displayUrl = "https://starsnin.vercel.app/wiki/$clean",
            targetUrl = "https://starsnin.vercel.app?q=${clean}+definicao",
            snippet = "Guia completo sobre $query. Entenda os conceitos fundamentais, origem histórica, exemplos práticos e referências na enciclopédia do Starsnin.",
            tag = "Enciclopédia"
        ),
        SearchResultItem(
            title = "Ecossistema Starsnin: Tecnologias Livres de Chromium e WebViews",
            displayUrl = "https://starsnin.vercel.app/tech/independent-engine",
            targetUrl = "https://starsnin.vercel.app",
            snippet = "Saiba como o Starsnin implementa o parsing de HTML, lexer de nós DOM e renderização pura em Kotlin Compose com abas verticais sem nenhum código de Chromium.",
            tag = "Tecnologia"
        )
    )
}
