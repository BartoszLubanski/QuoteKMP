package com.example.quotekmp.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.quotekmp.db.QuoteDatabase
import com.example.quotekmp.network.QuoteApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import com.example.quotekmp.network.KtorQuoteApi

class QuoteRepositoryTest {

    @Test
    fun observeQuotes_mapsCachedEntityToDomainModel() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        QuoteDatabase.Schema.create(driver)
        val database = QuoteDatabase(driver)
        database.quoteQueries.insertQuote(id = 1L, quote = "Test quote", author = "Test Author")

        val repository = QuoteRepository(api = KtorQuoteApi(), database = database)
        val quotes = repository.observeQuotes().first()

        assertEquals(1, quotes.size)
        assertEquals(1, quotes.first().id)
        assertEquals("Test quote", quotes.first().quote)
        assertEquals("Test Author", quotes.first().author)
    }
}