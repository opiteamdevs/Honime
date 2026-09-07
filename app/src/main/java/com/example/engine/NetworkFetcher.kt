package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

data class FetchResult(
    val url: String,
    val statusCode: Int,
    val rawHtml: String,
    val headers: Map<String, String>,
    val latencyMs: Long,
    val isFromFallback: Boolean = false,
    val error: String? = null
)

object NetworkFetcher {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    suspend fun fetch(url: String): FetchResult = withContext(Dispatchers.IO) {
        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        val startTime = System.currentTimeMillis()

        try {
            val request = Request.Builder()
                .url(normalizedUrl)
                .header("User-Agent", "StarsninEngine/1.0 (Custom Independent Engine; No-Chromium; Android)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val bodyString = response.body?.string() ?: ""
                val headersMap = mutableMapOf<String, String>()
                for (name in response.headers.names()) {
                    headersMap[name] = response.header(name) ?: ""
                }

                FetchResult(
                    url = normalizedUrl,
                    statusCode = response.code,
                    rawHtml = bodyString,
                    headers = headersMap,
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            // If offline, container blocked, or failed on starsnin.vercel.app, provide resilient ecosystem payload
            if (normalizedUrl.contains("starsnin.vercel.app")) {
                FetchResult(
                    url = normalizedUrl,
                    statusCode = 200,
                    rawHtml = getStarsninDefaultEcosystemHtml(),
                    headers = mapOf(
                        "server" to "Vercel",
                        "x-starsnin-engine" to "Independent-No-Chromium",
                        "content-type" to "text/html; charset=utf-8"
                    ),
                    latencyMs = latency,
                    isFromFallback = true
                )
            } else {
                FetchResult(
                    url = normalizedUrl,
                    statusCode = 0,
                    rawHtml = getGenericErrorHtml(normalizedUrl, e.localizedMessage ?: "Erro de conexão"),
                    headers = emptyMap(),
                    latencyMs = latency,
                    error = e.localizedMessage
                )
            }
        }
    }

    fun getStarsninDefaultEcosystemHtml(): String {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <title>Starsnin - Motor de Busca & Ecossistema</title>
                <meta name="description" content="Portal Oficial e Motor de Busca Starsnin">
            </head>
            <body style="background-color: #0A0E1A; color: #F8FAFC; font-family: sans-serif; padding: 24px;">
                <header style="text-align: center; margin-bottom: 32px;">
                    <img src="https://starsnin.vercel.app/starsninlogo.png" alt="Starsnin Logo Oficial" style="width: 96px; height: 96px; margin: 0 auto 16px auto;" />
                    <h1 style="color: #38BDF8; font-size: 28px; margin: 0 0 8px 0;">Starsnin</h1>
                    <p style="color: #94A3B8; font-size: 14px; margin: 0;">O ecossistema e motor de busca independente</p>
                </header>
                
                <section style="background-color: #111827; border: 1px solid #334155; border-radius: 16px; padding: 20px; margin-bottom: 24px; text-align: center;">
                    <h2 style="font-size: 18px; color: #E0E7FF; margin-top: 0;">Motor de Busca Starsnin</h2>
                    <p style="color: #94A3B8; font-size: 13px;">Pesquise na web ou digite um endereço URL diretamente</p>
                    <div style="margin-top: 16px;">
                        <input type="text" placeholder="Pesquisar no Starsnin ou digitar URL..." style="width: 100%; padding: 12px 16px; border-radius: 12px; background: #1E293B; border: 1px solid #38BDF8; color: #F8FAFC;" />
                    </div>
                </section>

                <section style="margin-bottom: 24px;">
                    <h3 style="font-size: 16px; color: #818CF8; margin-bottom: 12px;">Serviços & Atalhos Starsnin</h3>
                    <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                        <div style="background-color: #111827; border: 1px solid #1E293B; border-radius: 12px; padding: 14px; flex: 1; min-width: 140px;">
                            <h4 style="color: #38BDF8; margin: 0 0 6px 0; font-size: 15px;">Starsnin Search</h4>
                            <p style="color: #94A3B8; font-size: 12px; margin: 0;">Busca sem rastreamento</p>
                        </div>
                        <div style="background-color: #111827; border: 1px solid #1E293B; border-radius: 12px; padding: 14px; flex: 1; min-width: 140px;">
                            <h4 style="color: #A855F7; margin: 0 0 6px 0; font-size: 15px;">Starsnin Hub</h4>
                            <p style="color: #94A3B8; font-size: 12px; margin: 0;">Portal de ferramentas</p>
                        </div>
                        <div style="background-color: #111827; border: 1px solid #1E293B; border-radius: 12px; padding: 14px; flex: 1; min-width: 140px;">
                            <h4 style="color: #10B981; margin: 0 0 6px 0; font-size: 15px;">Starsnin Docs</h4>
                            <p style="color: #94A3B8; font-size: 12px; margin: 0;">Documentação do ecossistema</p>
                        </div>
                    </div>
                </section>

                <footer style="border-top: 1px solid #1E293B; padding-top: 16px; text-align: center; color: #64748B; font-size: 11px;">
                    <p style="margin: 0 0 4px 0;">Renderizado com a Engine Starsnin Independente (Zero Chromium / Zero WebView)</p>
                    <p style="margin: 0;">Domínio Oficial: https://starsnin.vercel.app</p>
                </footer>
            </body>
            </html>
        """.trimIndent()
    }

    private fun getGenericErrorHtml(url: String, error: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head><title>Erro de Carregamento - Starsnin</title></head>
            <body style="background-color: #0A0E1A; color: #F8FAFC; padding: 24px; font-family: sans-serif;">
                <h2 style="color: #EF4444;">Não foi possível carregar o endereço</h2>
                <p style="color: #94A3B8;">URL: $url</p>
                <p style="color: #CBD5E1; background: #1E293B; padding: 12px; border-radius: 8px;">$error</p>
                <p style="color: #64748B; font-size: 12px;">Verifique sua conexão ou tente recarregar.</p>
            </body>
            </html>
        """.trimIndent()
    }
}
