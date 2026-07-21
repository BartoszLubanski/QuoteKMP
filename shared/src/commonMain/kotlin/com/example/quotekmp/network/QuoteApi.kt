package com.example.quotekmp.network

import com.example.quotekmp.model.Quote
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface QuoteApi {
    suspend fun getRandomQuote(): Quote
}

class KtorQuoteApi : QuoteApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
        }
    }

    override suspend fun getRandomQuote(): Quote {
        return client.get("https://dummyjson.com/quotes/random").body()
    }
}