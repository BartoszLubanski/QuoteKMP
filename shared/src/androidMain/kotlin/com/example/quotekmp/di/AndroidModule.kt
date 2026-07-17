package com.example.quotekmp.di

import com.example.quotekmp.db.DatabaseDriverFactory
import org.koin.dsl.module

val androidModule = module {
    single{ DatabaseDriverFactory(get()) }
}