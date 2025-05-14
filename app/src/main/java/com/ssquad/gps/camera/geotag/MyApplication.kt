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
import com.ssquad.gps.camera.geotag.utils.VideoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File
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
        cleanupCacheOnStartup()
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
    private fun cleanupCacheOnStartup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Xóa tất cả file trong thư mục cache
                val cacheDir = cacheDir
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("video_processing_")) {
                        file.deleteRecursively()
                    }
                }

                // Reset danh sách theo dõi
                VideoUtils.cleanupAllTempDirectories()

                // Lọc và xóa các file tạm khác
                cleanupFFmpegTempFiles()
            } catch (e: Exception) {
                Log.e("MyApplication", "Error cleaning cache on startup", e)
            }
        }
    }

    private fun cleanupFFmpegTempFiles() {
        try {
            // FFmpeg có thể tạo file tạm trong /data/data/[package]/ffmpeg
            val ffmpegDir = File(filesDir.parent, "ffmpeg")
            if (ffmpegDir.exists() && ffmpegDir.isDirectory) {
                ffmpegDir.deleteRecursively()
            }

            // Xóa các file .tmp trong thư mục cache
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".tmp") || file.name.endsWith(".mp4")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Error cleaning FFmpeg temp files", e)
        }
    }
}