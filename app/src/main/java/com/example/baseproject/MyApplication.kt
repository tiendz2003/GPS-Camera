package com.example.baseproject

import com.example.baseproject.di.AppModule
import com.snake.squad.adslib.AdsApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication: AdsApplication("", isProduction = true) {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(AppModule.appModule)
        }
    }

}