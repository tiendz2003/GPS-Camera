package com.example.baseproject

import android.content.Context
import com.example.baseproject.di.AppModule
import com.snake.squad.adslib.AdsApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication: AdsApplication("", isProduction = true) {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        startKoin {
            androidContext(this@MyApplication)
            modules(AppModule.appModule)
        }
    }

}