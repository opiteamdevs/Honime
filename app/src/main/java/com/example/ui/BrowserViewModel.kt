package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.HtmlParser
import com.example.engine.NetworkFetcher
import com.example.model.BrowserTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class BrowserViewModel : ViewModel() {

    private val initialTab = BrowserTab(
        id = UUID.randomUUID().toString(),
        title = "Starsnin - Ecossistema",
        url = STARSNIN_HOME_URL
    )

    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(initialTab))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _isSidebarExpanded = MutableStateFlow<Boolean>(true)
    val isSidebarExpanded: StateFlow<Boolean> = _isSidebarExpanded.asStateFlow()

    private val _isInspectorOpen = MutableStateFlow<Boolean>(false)
    val isInspectorOpen: StateFlow<Boolean> = _isInspectorOpen.asStateFlow()

    private val _isBookmarksOpen = MutableStateFlow<Boolean>(false)
    val isBookmarksOpen: StateFlow<Boolean> = _isBookmarksOpen.asStateFlow()

    val activeTab: BrowserTab?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    init {
        // Automatically fetch initial home page using our custom engine
        loadUrl(initialTab.id, STARSNIN_HOME_URL)
    }

    fun toggleSidebar() {
        _isSidebarExpanded.update { !it }
    }

    fun openInspector() {
        _isInspectorOpen.value = true
    }

    fun closeInspector() {
        _isInspectorOpen.value = false
    }

    fun openBookmarks() {
        _isBookmarksOpen.value = true
    }

    fun closeBookmarks() {
        _isBookmarksOpen.value = false
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun newTab(url: String = STARSNIN_HOME_URL) {
        val newTab = BrowserTab(
            id = UUID.randomUUID().toString(),
            title = if (url == STARSNIN_HOME_URL) "Starsnin" else url,
            url = url
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        loadUrl(newTab.id, url)
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) return

        val newTabs = currentTabs.filter { it.id != tabId }
        _tabs.value = newTabs

        if (_activeTabId.value == tabId) {
            _activeTabId.value = newTabs.last().id
        }
    }

    fun loadUrl(tabId: String, rawUrl: String) {
        val targetUrl = when {
            rawUrl.isBlank() -> STARSNIN_HOME_URL
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            rawUrl.contains(".") && !rawUrl.contains(" ") -> "https://$rawUrl"
            else -> "https://starsnin.vercel.app?q=${rawUrl.replace(" ", "+")}"
        }

        // Set loading state
        _tabs.update { list ->
            list.map { tab ->
                if (tab.id == tabId) {
                    val updatedHistory = if (tab.url != targetUrl) {
                        tab.history.take(tab.historyIndex + 1) + targetUrl
                    } else tab.history
                    tab.copy(
                        url = targetUrl,
                        isLoading = true,
                        errorMessage = null,
                        history = updatedHistory,
                        historyIndex = updatedHistory.size - 1
                    )
                } else tab
            }
        }

        viewModelScope.launch {
            val result = NetworkFetcher.fetch(targetUrl)
            val domTree = HtmlParser.parse(result.rawHtml, baseUrl = targetUrl)

            // Extract page title from parsed DOM
            val titleElement = domTree.findFirstByTag("title")
            val extractedTitle = titleElement?.getAllText()?.ifBlank { null }
                ?: if (targetUrl.contains("starsnin.vercel.app")) "Starsnin - Ecossistema" else targetUrl

            _tabs.update { list ->
                list.map { tab ->
                    if (tab.id == tabId) {
                        tab.copy(
                            title = extractedTitle,
                            isLoading = false,
                            domTree = domTree,
                            rawHtml = result.rawHtml,
                            statusCode = result.statusCode,
                            latencyMs = result.latencyMs,
                            headers = result.headers,
                            errorMessage = result.error
                        )
                    } else tab
                }
            }
        }
    }

    fun refresh() {
        val currentTab = activeTab ?: return
        loadUrl(currentTab.id, currentTab.url)
    }

    fun navigateToHome() {
        val currentTab = activeTab ?: return
        loadUrl(currentTab.id, STARSNIN_HOME_URL)
    }

    fun goBack() {
        val currentTab = activeTab ?: return
        if (currentTab.canGoBack) {
            val prevIndex = currentTab.historyIndex - 1
            val prevUrl = currentTab.history[prevIndex]
            _tabs.update { list ->
                list.map { tab ->
                    if (tab.id == currentTab.id) {
                        tab.copy(historyIndex = prevIndex, url = prevUrl)
                    } else tab
                }
            }
            loadUrl(currentTab.id, prevUrl)
        }
    }

    fun goForward() {
        val currentTab = activeTab ?: return
        if (currentTab.canGoForward) {
            val nextIndex = currentTab.historyIndex + 1
            val nextUrl = currentTab.history[nextIndex]
            _tabs.update { list ->
                list.map { tab ->
                    if (tab.id == currentTab.id) {
                        tab.copy(historyIndex = nextIndex, url = nextUrl)
                    } else tab
                }
            }
            loadUrl(currentTab.id, nextUrl)
        }
    }
}
