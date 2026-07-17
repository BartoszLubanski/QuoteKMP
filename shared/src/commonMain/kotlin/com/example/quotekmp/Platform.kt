package com.example.quotekmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform