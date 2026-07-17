package com.example.quotekmp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotekmp.model.Quote
import com.example.quotekmp.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuoteViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<Quote?>(null)
    val uiState: StateFlow<Quote?> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val fetched = repository.refreshQuote()
            _uiState.value = fetched ?: repository.observeQuotes().first().firstOrNull()
        }
    }
}