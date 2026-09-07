package com.example.engine

import java.util.Stack

object HtmlParser {

    private val SELF_CLOSING_TAGS = setOf(
        "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "param", "source", "track", "wbr"
    )

    fun parse(rawHtml: String, baseUrl: String = ""): DomElement {
        val cleanHtml = cleanRawHtml(rawHtml)
        val root = DomElement(tag = "document")
        val stack = Stack<MutableElement>()
        val rootMutable = MutableElement(tag = "document")
        stack.push(rootMutable)

        var i = 0
        val len = cleanHtml.length

        while (i < len) {
            if (cleanHtml[i] == '<') {
                // Comment check
                if (cleanHtml.startsWith("<!--", i)) {
                    val endComment = cleanHtml.indexOf("-->", i + 4)
                    i = if (endComment != -1) endComment + 3 else len
                    continue
                }

                // DocType check
                if (cleanHtml.regionMatches(i, "<!doctype", 0, 9, ignoreCase = true)) {
                    val endDocType = cleanHtml.indexOf('>', i)
                    i = if (endDocType != -1) endDocType + 1 else len
                    continue
                }

                val endTag = cleanHtml.indexOf('>', i)
                if (endTag == -1) break

                val tagContent = cleanHtml.substring(i + 1, endTag).trim()
                i = endTag + 1

                if (tagContent.startsWith("/")) {
                    // Closing tag
                    val tagName = tagContent.substring(1).trim().lowercase()
                    if (stack.size > 1) {
                        // Pop matching tag or closest
                        var matchIndex = -1
                        for (k in stack.indices.reversed()) {
                            if (stack[k].tag == tagName) {
                                matchIndex = k
                                break
                            }
                        }
                        if (matchIndex > 0) {
                            while (stack.size > matchIndex) {
                                val popped = stack.pop()
                                val parent = stack.peek()
                                parent.children.add(popped.toImmutable(baseUrl))
                            }
                        }
                    }
                } else {
                    // Opening or self-closing tag
                    val isExplicitSelfClosing = tagContent.endsWith("/")
                    val innerTagContent = if (isExplicitSelfClosing) tagContent.dropLast(1).trim() else tagContent
                    val parts = splitTagAndAttributes(innerTagContent)
                    val tagName = parts.first.lowercase()
                    val attributes = parts.second

                    if (tagName.isEmpty()) continue

                    // Script and Style special handling: extract text or skip
                    if (tagName == "script" || tagName == "style") {
                        val closeTag = "</$tagName>"
                        val closeIndex = cleanHtml.indexOf(closeTag, i, ignoreCase = true)
                        if (closeIndex != -1) {
                            i = closeIndex + closeTag.length
                        }
                        continue
                    }

                    val isSelfClosing = isExplicitSelfClosing || SELF_CLOSING_TAGS.contains(tagName)
                    val element = MutableElement(tag = tagName, attributes = attributes)

                    if (isSelfClosing) {
                        if (stack.isNotEmpty()) {
                            stack.peek().children.add(element.toImmutable(baseUrl))
                        }
                    } else {
                        stack.push(element)
                    }
                }
            } else {
                // Text node
                val nextTag = cleanHtml.indexOf('<', i)
                val textSegment = if (nextTag == -1) {
                    cleanHtml.substring(i)
                } else {
                    cleanHtml.substring(i, nextTag)
                }
                val decoded = decodeHtmlEntities(textSegment.trim())
                if (decoded.isNotEmpty() && stack.isNotEmpty()) {
                    stack.peek().children.add(DomText(decoded))
                }
                i = if (nextTag == -1) len else nextTag
            }
        }

        // Unwind any remaining open elements
        while (stack.size > 1) {
            val popped = stack.pop()
            stack.peek().children.add(popped.toImmutable(baseUrl))
        }

        return rootMutable.toImmutable(baseUrl)
    }

    private fun cleanRawHtml(html: String): String {
        return html
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    private fun splitTagAndAttributes(content: String): Pair<String, Map<String, String>> {
        val trimmed = content.trim()
        val firstSpace = trimmed.indexOfFirst { it.isWhitespace() }
        if (firstSpace == -1) {
            return Pair(trimmed, emptyMap())
        }
        val tag = trimmed.substring(0, firstSpace)
        val attrString = trimmed.substring(firstSpace).trim()
        val attributes = parseAttributes(attrString)
        return Pair(tag, attributes)
    }

    private fun parseAttributes(attrString: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex("""([a-zA-Z0-9_\-]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+)))?""")
        for (match in regex.findAll(attrString)) {
            val key = match.groupValues[1].lowercase()
            val value = when {
                match.groupValues[2].isNotEmpty() -> match.groupValues[2]
                match.groupValues[3].isNotEmpty() -> match.groupValues[3]
                match.groupValues[4].isNotEmpty() -> match.groupValues[4]
                else -> key
            }
            result[key] = value
        }
        return result
    }

    private fun decodeHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("&[a-zA-Z0-9#]+;"), "")
    }

    private class MutableElement(
        val tag: String,
        val attributes: Map<String, String> = emptyMap(),
        val children: MutableList<DomNode> = mutableListOf()
    ) {
        fun toImmutable(baseUrl: String): DomElement {
            val styles = parseInlineStyles(attributes["style"])
            val fixedAttributes = attributes.toMutableMap()
            // Normalize relative URLs to absolute URLs
            if (baseUrl.isNotEmpty()) {
                fixedAttributes["href"]?.let { fixedAttributes["href"] = resolveUrl(baseUrl, it) }
                fixedAttributes["src"]?.let { fixedAttributes["src"] = resolveUrl(baseUrl, it) }
            }
            return DomElement(
                tag = tag,
                attributes = fixedAttributes,
                children = children,
                styles = styles
            )
        }

        private fun parseInlineStyles(styleAttr: String?): Map<String, String> {
            if (styleAttr.isNullOrBlank()) return emptyMap()
            val map = mutableMapOf<String, String>()
            val pairs = styleAttr.split(';')
            for (p in pairs) {
                val colon = p.indexOf(':')
                if (colon != -1) {
                    val key = p.substring(0, colon).trim().lowercase()
                    val value = p.substring(colon + 1).trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        map[key] = value
                    }
                }
            }
            return map
        }

        private fun resolveUrl(base: String, relative: String): String {
            if (relative.startsWith("http://") || relative.startsWith("https://") || relative.startsWith("//")) {
                return if (relative.startsWith("//")) "https:$relative" else relative
            }
            if (relative.startsWith("#") || relative.startsWith("mailto:") || relative.startsWith("javascript:")) {
                return relative
            }
            val cleanBase = base.substringBefore('#').substringBefore('?')
            val lastSlash = cleanBase.lastIndexOf('/')
            val root = if (cleanBase.contains("://")) {
                val protocolEnd = cleanBase.indexOf("://") + 3
                val hostEnd = cleanBase.indexOf('/', protocolEnd)
                if (hostEnd != -1) cleanBase.substring(0, hostEnd) else cleanBase
            } else cleanBase

            return if (relative.startsWith("/")) {
                "$root$relative"
            } else if (lastSlash != -1 && lastSlash > 7) {
                cleanBase.substring(0, lastSlash + 1) + relative
            } else {
                "$cleanBase/$relative"
            }
        }
    }
}
