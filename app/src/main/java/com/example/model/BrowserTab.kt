package com.example.model

import com.example.engine.DomElement

data class BrowserTab(
    val id: String,
    val title: String,
    val url: String,
    val isLoading: Boolean = false,
    val domTree: DomElement? = null,
    val rawHtml: String = "",
    val statusCode: Int = 200,
    val latencyMs: Long = 0L,
    val headers: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val history: List<String> = listOf(url),
    val historyIndex: Int = 0
) {
    val canGoBack: Boolean get() = historyIndex > 0
    val canGoForward: Boolean get() = historyIndex < history.size - 1
}
