package kalkulator.cnc.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApiService(private val client: HttpClient) {

    // 1. Adres dla bazy danych CNC
    private val cncBaseUrl = "https://kalkulatorcnc.mivs.dev"

    // 2. Adres dla wspólnych wiadomości (newsów)
    private val parishBaseUrl = "https://api-parafia.mivs.dev"

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
            // Używamy adresu parafialnego
            client.get("$parishBaseUrl/news").body()
        } catch (e: Exception) {
            println("[CNC_LOG] Błąd pobierania newsów: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}