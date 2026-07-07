package kalkulator.cnc.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kalkulator.cnc.entity.NewsItem

class ApiService(private val client: HttpClient) {

    private val cncBaseUrl = "https://kalkulatorcnc.mivs.dev"
    private val parishBaseUrl = "https://api-parafia.mivs.dev"
    private val privacyBaseUrl = "https://privacypolicy.mivs.dev"

    suspend fun getCncDatabase(lang: String, since: String?, onProgress: (Int) -> Unit): List<CncRecord>? {
        return try {
            client.get("$cncBaseUrl/v1/database") {
                // 🔥 Wysyłamy parametr języka do serwera API
                parameter("lang", lang)
                if (since != null) parameter("since", since)

                onDownload { bytesDownloaded, totalBytes ->
                    if (totalBytes != null && totalBytes > 0) {
                        val percentage = ((bytesDownloaded * 100) / totalBytes).toInt()
                        onProgress(percentage)
                    }
                }
            }.body()
        } catch (e: Exception) {
            println("[CNC_LOG] Krytyczny Błąd API (Database): ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getNewsFeed(): List<NewsResponse>? {
        return try {
            client.get("$parishBaseUrl/news").body<List<NewsResponse>>()
        } catch (e: Exception) {
            println("[CNC_LOG] Błąd pobierania newsów: ${e.message}")
            null
        }
    }

    suspend fun getPrivacyPolicy(serviceName: String): String? {
        return try {
            val response: PrivacyPolicyResponse = client.get("$privacyBaseUrl/api/privacy/$serviceName").body()
            response.content
        } catch (e: Exception) {
            println("[CNC_LOG] Błąd pobierania polityki prywatności: ${e.message}")
            null
        }
    }
}