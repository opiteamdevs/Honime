package com.example.engine

sealed class DomNode

data class DomText(
    val text: String
) : DomNode()

data class DomElement(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: List<DomNode> = emptyList(),
    val styles: Map<String, String> = emptyMap()
) : DomNode() {
    val id: String? get() = attributes["id"]
    val className: String? get() = attributes["class"]
    val href: String? get() = attributes["href"]
    val src: String? get() = attributes["src"]
    val alt: String? get() = attributes["alt"]
    val title: String? get() = attributes["title"]
    val placeholder: String? get() = attributes["placeholder"]
    val value: String? get() = attributes["value"]

    fun totalNodes(): Int {
        var count = 1
        for (child in children) {
            when (child) {
                is DomElement -> count += child.totalNodes()
                is DomText -> count += 1
            }
        }
        return count
    }

    fun findFirstByTag(targetTag: String): DomElement? {
        if (tag.equals(targetTag, ignoreCase = true)) return this
        for (child in children) {
            if (child is DomElement) {
                val found = child.findFirstByTag(targetTag)
                if (found != null) return found
            }
        }
        return null
    }

    fun getAllText(): String {
        val sb = StringBuilder()
        for (child in children) {
            when (child) {
                is DomText -> sb.append(child.text).append(" ")
                is DomElement -> sb.append(child.getAllText()).append(" ")
            }
        }
        return sb.toString().trim()
    }
}
