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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle

private class FailingQuoteApi : QuoteApi {
    override suspend fun getRandomQuote(): Quote {
        throw RuntimeException("No network")
    }
}

private class SucceedingQuoteApi(private val quote: Quote) : QuoteApi {
    override suspend fun getRandomQuote(): Quote = quote
}

private class ControllableQuoteApi : QuoteApi {
    private val pendingResponses = mutableListOf<CompletableDeferred<Quote>>()

    fun resolveCall(callIndex: Int, quote: Quote) {
        pendingResponses[callIndex].complete(quote)
    }

    override suspend fun getRandomQuote(): Quote {
        val response = CompletableDeferred<Quote>()
        pendingResponses.add(response)
        return response.await()
    }
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

    @Test
    fun refresh_ignoresStaleResponse_whenCalledAgainBeforeFirstRequestCompletes() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        QuoteDatabase.Schema.create(driver)
        val database = QuoteDatabase(driver)

        val staleQuote = Quote(id = 1, quote = "Stale quote", author = "Stale Author")
        val freshQuote = Quote(id = 2, quote = "Fresh quote", author = "Fresh Author")

        val api = ControllableQuoteApi()
        val repository = QuoteRepository(api = api, database = database)
        val viewModel = QuoteViewModel(repository)

        viewModel.refresh()

        api.resolveCall(callIndex = 1, quote = freshQuote)
        api.resolveCall(callIndex = 0, quote = staleQuote)

        advanceUntilIdle()

        val state = viewModel.uiState.value as QuoteUiState.Success
        assertEquals("Fresh quote", state.quote.quote)
    }
}