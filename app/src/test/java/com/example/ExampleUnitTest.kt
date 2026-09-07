package com.example

import com.example.engine.HtmlParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun customHtmlParser_parsesTagsCorrectly() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Starsnin Test</title></head>
            <body>
                <h1>Bem-vindo ao Starsnin</h1>
                <p>Navegador com engine independente</p>
                <a href="https://starsnin.vercel.app">Link Oficial</a>
                <img src="https://starsnin.vercel.app/starsninlogo.png" alt="Logo" />
            </body>
            </html>
        """.trimIndent()

        val root = HtmlParser.parse(sampleHtml, baseUrl = "https://starsnin.vercel.app")
        assertNotNull(root)

        val h1 = root.findFirstByTag("h1")
        assertNotNull(h1)
        assertEquals("Bem-vindo ao Starsnin", h1?.getAllText()?.trim())

        val link = root.findFirstByTag("a")
        assertNotNull(link)
        assertEquals("https://starsnin.vercel.app", link?.href)

        val img = root.findFirstByTag("img")
        assertNotNull(img)
        assertEquals("https://starsnin.vercel.app/starsninlogo.png", img?.src)
    }
}
