package com.example.baseproject

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.baseproject.di.AppModule
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.worker.LoadDataTemplateWorker
import com.google.android.libraries.places.api.Places
import com.snake.squad.adslib.AdsApplication
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class MyApplication : AdsApplication("", isProduction = true) {
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
        SharePrefManager.initialize(this)
        Places.initialize(applicationContext, appContext.getString(R.string.google_maps_key))
        val workerFactory: LoadDataTemplateWorker.Factory by inject()
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()
        WorkManager.initialize(this, config)
        scheduleDataTemplateWorker()
    }

    private fun scheduleDataTemplateWorker() {
        val constrain = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<LoadDataTemplateWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constrain)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LoadDataTemplateWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        val firstWork =
            OneTimeWorkRequestBuilder<LoadDataTemplateWorker>().setConstraints(constrain)
                .build()//chay 1 lan khi mo ap
        WorkManager.getInstance(this).enqueue(firstWork)
    }
}