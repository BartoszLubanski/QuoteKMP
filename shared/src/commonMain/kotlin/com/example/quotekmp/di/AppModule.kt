package com.example.quotekmp.di

import com.example.quotekmp.db.DatabaseDriverFactory
import com.example.quotekmp.db.QuoteDatabase
import com.example.quotekmp.network.QuoteApi
import com.example.quotekmp.repository.QuoteRepository
import org.koin.core.context.startKoin
import org.koin.core.KoinApplication
import org.koin.dsl.module
import com.example.quotekmp.viewmodel.QuoteViewModel

val commonModule = module {
    single { QuoteApi() }
    single { QuoteDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { QuoteRepository(get(), get()) }
    single { QuoteViewModel(get()) }
}

fun initKoin(config: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        config?.invoke(this)
        modules(commonModule)
    }
}