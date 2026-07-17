package com.example.quotekmp.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class QuoteTest {

    @Test
    fun decodesQuoteFromJson() {
        val json = """{"id":1,"quote":"Test quote","author":"Test Author"}"""

        val quote = Json.decodeFromString<Quote>(json)

        assertEquals(1, quote.id)
        assertEquals("Test quote", quote.quote)
        assertEquals("Test Author", quote.author)
    }
}