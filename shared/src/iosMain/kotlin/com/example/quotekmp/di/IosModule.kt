package com.example.quotekmp.di

import com.example.quotekmp.db.DatabaseDriverFactory
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
}