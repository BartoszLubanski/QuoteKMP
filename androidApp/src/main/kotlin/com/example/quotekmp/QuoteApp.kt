package com.example.quotekmp

import android.app.Application
import com.example.quotekmp.di.androidModule
import com.example.quotekmp.di.initKoin
import org.koin.android.ext.koin.androidContext

class QuoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@QuoteApp)
            modules(androidModule)
        }
    }
}