package com.example.quotekmp.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.quotekmp.db.QuoteDatabase
import com.example.quotekmp.model.Quote
import com.example.quotekmp.network.QuoteApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuoteRepository(
    private val api: QuoteApi,
    private val database: QuoteDatabase
) {
    fun observeQuotes(): Flow<List<Quote>> {
        return database.quoteQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { entity ->
                    Quote(id = entity.id.toInt(), quote = entity.quote, author = entity.author)
                }
            }
    }

    suspend fun refreshQuote(): Quote? {
        return try {
            val quote = api.getRandomQuote()
            database.quoteQueries.insertQuote(
                id = quote.id.toLong(),
                quote = quote.quote,
                author = quote.author
            )
            quote
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}