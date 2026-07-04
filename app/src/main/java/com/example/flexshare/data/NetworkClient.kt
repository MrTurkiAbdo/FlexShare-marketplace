package com.example.flexshare.data

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.observer.* // 🚀 Core observer plugin
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object NetworkClient {
    const val BASE_URL = "http://192.168.100.128:8080/"

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        // 🪵 FIXED: Correct client-side syntax for ResponseObserver
        ResponseObserver { response ->
            println("🚀 KtorClient Response: ${response.status.value} for request")
        }
    }
}