package com.ssquad.gps.camera.geotag.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.Dispatchers
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.domain.CameraRepository
import com.ssquad.gps.camera.geotag.utils.FFmpegExecutor
import com.ssquad.gps.camera.geotag.utils.VideoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class VideoProcessingService : Service() {

    private val cameraRepository: CameraRepository by inject()
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "video_processing_channel"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService() = this@VideoProcessingService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PROCESS_VIDEO -> {
                val inputUri = intent.getParcelableExtra<Uri>(EXTRA_INPUT_URI)
                val address = intent.getStringExtra(EXTRA_ADDRESS)
                val hasTemplate = intent.getBooleanExtra(EXTRA_HAS_TEMPLATE, false)
                val templatePath = intent.getStringExtra(EXTRA_TEMPLATE_PATH)
                startForeground(NOTIFICATION_ID, createProgressNotification(0f))
                if (inputUri != null) {
                    processVideo(inputUri, templatePath, address)
                    updateNotification("Đang xử lý video...", 0.0f)
                } else {
                    stopSelf()
                }
            }
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun processVideo(inputUri: Uri, templatePath: String?, address: String?) {
        var tempDir: File? = null
        var tempInputFile: File? = null
        var tempOutputFile: File? = null

        serviceScope.launch {
            try {
                updateNotification("Đang xử lý video...", 0.1f)

                val result = if (templatePath != null) {
                    // Sử dụng templatePath để xử lý video với FFmpeg
                    tempDir = VideoUtils.createTempDirectory(this@VideoProcessingService)
                    tempInputFile = VideoUtils.createTempFile(tempDir!!, "input_video.mp4")
                    tempOutputFile = VideoUtils.createTempFile(tempDir!!, "output_video.mp4")

                    VideoUtils.copyUriToFile(this@VideoProcessingService, inputUri, tempInputFile!!)

                    val ffmpegExecutor : FFmpegExecutor by inject()
                    val success = ffmpegExecutor.processVideoWithOverlay(
                        tempInputFile!!.absolutePath,
                        templatePath,
                        tempOutputFile!!.absolutePath
                    ) { progress ->
                        updateNotification("Đang xử lý video...", 0.1f + progress * 0.6f)
                    }

                    val savedUri = if (success) {
                        updateNotification("Đang lưu video...", 0.8f)
                        cameraRepository.saveVideoToGallery(Uri.fromFile(tempOutputFile!!), address)
                    } else null

                    savedUri
                } else {
                    // Chỉ lưu video nếu không có template
                    updateNotification("Đang lưu video...", 0.5f)
                    cameraRepository.saveVideoToGallery(inputUri, address)
                }

                // Dọn dẹp file ngay sau khi sử dụng
                tempDir?.let { VideoUtils.cleanupTempFiles(it) }

                // Thông báo kết quả
                if (result != null) {
                    updateNotification("Đã lưu video thành công", 1.0f)
                    broadcastSuccess(result)
                } else {
                    updateNotification("Lưu video thất bại", 1.0f)
                    broadcastFailure("Không thể lưu video")
                }

                delay(1500)
                stopForegroundAndService()

            } catch (e: Exception) {
                Log.e(TAG, "Error in video processing", e)

                // Đảm bảo dọn dẹp file tạm ngay cả khi có lỗi
                tempDir?.let { VideoUtils.cleanupTempFiles(it) }

                updateNotification("Lỗi: ${e.message}", 1.0f)
                broadcastFailure(e.message ?: "Lỗi không xác định")
                delay(1500)
                stopForegroundAndService()
            } finally {
                // Double-check dọn dẹp
                tempDir?.let { VideoUtils.cleanupTempFiles(it) }
            }
        }
    }


    private fun broadcastSuccess(uri: Uri) {
        // Hiển thị notification kết quả với sound và vibration
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Xử lý video hoàn tất")
            .setContentText("Video đã được lưu thành công")
            .setSmallIcon(R.drawable.ig_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, builder.build())

        // Tiếp tục broadcast như cũ
        val intent = Intent(ACTION_VIDEO_SAVED).apply {
            putExtra(EXTRA_SAVED_URI, uri)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastFailure(error: String) {
        val intent = Intent(ACTION_VIDEO_SAVE_FAILED).apply {
            putExtra(EXTRA_ERROR_MESSAGE, error)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun stopForegroundAndService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Video Processing",
            NotificationManager.IMPORTANCE_HIGH  // Thay đổi từ LOW thành HIGH
        )
        channel.description = "Thông báo khi đang xử lý video"
        channel.enableVibration(true)
        channel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }


    private fun createProgressNotification(progress: Float): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang xử lý video")
            .setContentText("Vui lòng đợi...")
            .setSmallIcon(R.drawable.ig_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS) // Thêm category
            .setVibrate(longArrayOf(0, 100, 100, 100))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setOngoing(true)
        if (progress > 0) {
            builder.setProgress(100, (progress * 100).toInt(), false)
        }

        return builder.build()
    }

    private fun updateNotification(message: String, progress: Float) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setSmallIcon(R.drawable.ig_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (progress < 1.0f) {
            builder.setProgress(100, (progress * 100).toInt(), false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onDestroy() {
        serviceScope.cancel()
        VideoUtils.cleanupAllTempDirectories()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "VideoProcessingService"

        const val ACTION_PROCESS_VIDEO = "ACTION_PROCESS_VIDEO"
        const val ACTION_VIDEO_SAVED = "ACTION_VIDEO_SAVED"
        const val ACTION_VIDEO_SAVE_FAILED = "ACTION_VIDEO_SAVE_FAILED"

        const val EXTRA_INPUT_URI = "input_uri"
        const val EXTRA_ADDRESS = "address"
        const val EXTRA_HAS_TEMPLATE = "has_template"
        const val EXTRA_TEMPLATE_PATH = "template_path"
        const val EXTRA_SAVED_URI = "saved_uri"
        const val EXTRA_ERROR_MESSAGE = "error_message"

        fun startProcessing(
            context: Context,
            inputUri: Uri,
            address: String? = null,
            templatePath: String? = null
        ) {
            val intent = Intent(context, VideoProcessingService::class.java).apply {
                action = ACTION_PROCESS_VIDEO
                putExtra(EXTRA_INPUT_URI, inputUri)
                putExtra(EXTRA_ADDRESS, address)
                if (templatePath != null) {
                    putExtra(EXTRA_HAS_TEMPLATE, true)
                    putExtra(EXTRA_TEMPLATE_PATH, templatePath)
                }
            }

            context.startForegroundService(intent)
        }
    }
}