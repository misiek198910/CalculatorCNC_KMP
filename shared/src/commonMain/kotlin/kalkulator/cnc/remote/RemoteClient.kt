package kalkulator.cnc.remote

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout // <--- NOWY IMPORT
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object RemoteClient {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
    }

    val apiService: ApiService by lazy {
        ApiService(httpClient)
    }
}