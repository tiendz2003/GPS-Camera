package com.ssquad.gps.camera.geotag

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ssquad.gps.camera.geotag.di.AppModule
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.worker.LoadDataTemplateWorker
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