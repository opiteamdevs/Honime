package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.RenderDomTree
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
    val isInspectorOpen by viewModel.isInspectorOpen.collectAsStateWithLifecycle()
    val isBookmarksOpen by viewModel.isBookmarksOpen.collectAsStateWithLifecycle()

    val activeTab = viewModel.activeTab
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var urlInputText by remember(activeTab?.url) {
        mutableStateOf(activeTab?.url ?: STARSNIN_HOME_URL)
    }

    // ModalNavigationDrawer provides the lateral vertical tabs drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = StarsninDarkBg,
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 320.dp)
                    .fillMaxHeight()
            ) {
                VerticalTabsSidebar(
                    tabs = tabs,
                    activeTabId = activeTabId,
                    onSelectTab = { selectedId ->
                        viewModel.selectTab(selectedId)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onCloseTab = { closedId ->
                        viewModel.closeTab(closedId)
                    },
                    onNewTab = {
                        viewModel.newTab()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenInspector = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openInspector()
                    },
                    onOpenBookmarks = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openBookmarks()
                    },
                    onNavigateHome = {
                        viewModel.navigateToHome()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onCloseSidebar = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        },
        modifier = modifier.testTag("browser_screen_root")
    ) {
        // Main Screen: Full-width Chrome-style experience
        Scaffold(
            topBar = {
                BrowserToolbar(
                    activeTab = activeTab,
                    urlText = urlInputText,
                    tabCount = tabs.size,
                    onUrlTextChange = { urlInputText = it },
                    onSubmitUrl = { url ->
                        if (activeTab != null) {
                            viewModel.loadUrl(activeTab.id, url)
                        }
                    },
                    onGoBack = { viewModel.goBack() },
                    onGoForward = { viewModel.goForward() },
                    onRefresh = { viewModel.refresh() },
                    onNavigateHome = { viewModel.navigateToHome() },
                    onOpenTabs = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onOpenInspector = { viewModel.openInspector() },
                    onOpenBookmarks = { viewModel.openBookmarks() },
                    onNewTab = { viewModel.newTab() }
                )
            },
            containerColor = StarsninDarkBg
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    activeTab == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma aba ativa", color = StarsninTextMuted)
                        }
                    }

                    // 1. New Tab / Search Results View for Starsnin Portal
                    activeTab.url.contains("starsnin.vercel.app") || activeTab.url.isBlank() -> {
                        val queryParam = extractQueryParam(activeTab.url)
                        StarsninHomeView(
                            currentQuery = queryParam,
                            onSearch = { query ->
                                val target = "https://starsnin.vercel.app?q=${query.trim().replace(" ", "+")}"
                                viewModel.loadUrl(activeTab.id, target)
                            },
                            onOpenUrl = { targetUrl ->
                                viewModel.loadUrl(activeTab.id, targetUrl)
                            }
                        )
                    }

                    // 2. Loading state with independent engine indicator
                    activeTab.isLoading && activeTab.domTree == null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = StarsninCyan,
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Carregando com Engine Própria...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = StarsninTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sem Chromium • Sem WebViews",
                                fontSize = 11.sp,
                                color = StarsninTextMuted
                            )
                        }
                    }

                    // 3. Error state with retry
                    activeTab.errorMessage != null && activeTab.domTree == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Não foi possível carregar a página",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarsninTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeTab.errorMessage ?: "Erro desconhecido",
                                fontSize = 12.sp,
                                color = StarsninTextMuted
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(containerColor = StarsninCyan)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF003544)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tentar novamente", color = Color(0xFF003544), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 4. Custom DOM Engine Renderer (Full-width reader)
                    activeTab.domTree != null -> {
                        RenderDomTree(
                            root = activeTab.domTree,
                            onNavigate = { href ->
                                viewModel.loadUrl(activeTab.id, href)
                            }
                        )
                    }

                    // Fallback to Home View
                    else -> {
                        StarsninHomeView(
                            currentQuery = "",
                            onSearch = { query ->
                                val target = "https://starsnin.vercel.app?q=${query.trim().replace(" ", "+")}"
                                viewModel.loadUrl(activeTab.id, target)
                            },
                            onOpenUrl = { targetUrl ->
                                viewModel.loadUrl(activeTab.id, targetUrl)
                            }
                        )
                    }
                }
            }
        }

        // DOM Inspector Modal Sheet
        if (isInspectorOpen) {
            DomInspectorSheet(
                tab = activeTab,
                onDismiss = { viewModel.closeInspector() }
            )
        }

        // Bookmarks Modal Dialog
        if (isBookmarksOpen) {
            BookmarksDialog(
                onSelectBookmark = { url ->
                    if (activeTab != null) {
                        viewModel.loadUrl(activeTab.id, url)
                    }
                    viewModel.closeBookmarks()
                },
                onDismiss = { viewModel.closeBookmarks() }
            )
        }
    }
}

private fun extractQueryParam(url: String): String {
    if (!url.contains("?q=")) return ""
    val queryPart = url.substringAfter("?q=").substringBefore("&")
    return java.net.URLDecoder.decode(queryPart, "UTF-8").replace("+", " ")
}
