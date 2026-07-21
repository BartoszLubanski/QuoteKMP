package com.example.quotekmp.viewmodel

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.quotekmp.db.QuoteDatabase
import com.example.quotekmp.model.Quote
import com.example.quotekmp.network.QuoteApi
import com.example.quotekmp.repository.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FailingQuoteApi : QuoteApi {
    override suspend fun getRandomQuote(): Quote {
        throw RuntimeException("No network")
    }
}

private class SucceedingQuoteApi(private val quote: Quote) : QuoteApi {
    override suspend fun getRandomQuote(): Quote = quote
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refresh_fallsBackToCachedQuote_whenNetworkFails() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        QuoteDatabase.Schema.create(driver)
        val database = QuoteDatabase(driver)
        database.quoteQueries.insertQuote(id = 1L, quote = "Cached quote", author = "Cached Author")

        val repository = QuoteRepository(api = FailingQuoteApi(), database = database)
        val viewModel = QuoteViewModel(repository)

        val state = viewModel.uiState.first { it is QuoteUiState.Success } as QuoteUiState.Success

        assertEquals("Cached quote", state.quote.quote)
        assertEquals(true, state.isFromCache)
    }

    @Test
    fun refresh_emitsEmpty_whenNetworkFailsAndNoCache() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        QuoteDatabase.Schema.create(driver)
        val database = QuoteDatabase(driver)

        val repository = QuoteRepository(api = FailingQuoteApi(), database = database)
        val viewModel = QuoteViewModel(repository)

        val state = viewModel.uiState.first { it !is QuoteUiState.Loading }

        assertEquals(QuoteUiState.Empty, state)
    }

    @Test
    fun refresh_emitsFreshQuote_whenNetworkSucceeds() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        QuoteDatabase.Schema.create(driver)
        val database = QuoteDatabase(driver)

        val fetchedQuote = Quote(id = 1, quote = "Fresh quote", author = "Fresh Author")
        val repository = QuoteRepository(api = SucceedingQuoteApi(fetchedQuote), database = database)
        val viewModel = QuoteViewModel(repository)

        val state = viewModel.uiState.first { it is QuoteUiState.Success } as QuoteUiState.Success

        assertEquals("Fresh quote", state.quote.quote)
        assertEquals(false, state.isFromCache)
    }
}