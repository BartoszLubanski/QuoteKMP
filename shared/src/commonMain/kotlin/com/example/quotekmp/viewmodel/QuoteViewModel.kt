package com.example.quotekmp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotekmp.model.Quote
import com.example.quotekmp.repository.QuoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class QuoteUiState(
    val quote: Quote,
    val isFromCache: Boolean
)

class QuoteViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<QuoteUiState?>(null)
    val uiState: StateFlow<QuoteUiState?> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val fetched = repository.refreshQuote()
            _uiState.value = if (fetched != null) {
                QuoteUiState(quote = fetched, isFromCache = false)
            } else {
                repository.observeQuotes().first().randomOrNull()?.let { cached ->
                    QuoteUiState(quote = cached, isFromCache = true)
                }
            }
        }
    }
}