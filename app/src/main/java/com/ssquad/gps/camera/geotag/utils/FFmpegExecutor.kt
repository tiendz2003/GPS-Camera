package com.ssquad.gps.camera.geotag.utils

import android.media.MediaMetadataRetriever
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class FFmpegExecutor {

    suspend fun processVideoWithOverlay(
        inputPath: String,
        overlayPath: String,
        outputPath: String,
        progressCallback: ((Float) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            // Lấy duration của video input
            val duration = getMediaDuration(inputPath)

            val cmd = arrayOf(
                "-y",
                "-i", inputPath,
                "-i", overlayPath,
                "-filter_complex", "[0:v][1:v]overlay=(main_w-overlay_w)/2:main_h-overlay_h",
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "28",
                "-tune", "fastdecode",
                "-profile:v", "baseline",
                "-level", "3.0",
                "-r", "30",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                outputPath
            ).joinToString(" ")

            val returnCode = suspendCoroutine<ReturnCode?> { continuation ->
                FFmpegKit.executeAsync(
                    cmd,
                    { session ->
                        continuation.resume(session.returnCode)
                    },
                    { log ->
                        Log.d("FFmpegKitLog", log.message)
                    },
                    { statistics ->
                        if (duration > 0) {
                            // Tính toán tiến độ chính xác hơn dựa trên thời lượng video
                            val progress = statistics.time.toFloat() / (duration * 1000)
                            progressCallback?.invoke(min(progress, 1.0f))
                        } else {
                            progressCallback?.invoke(statistics.time.toFloat() / 1000)
                        }
                    }
                )
            }

            return@withContext ReturnCode.isSuccess(returnCode)
        } catch (e: Exception) {
            Log.e("FFmpegExecutor", "Error executing FFmpeg command", e)
            return@withContext false
        }
    }
    private suspend fun getMediaDuration(filePath: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                time?.toLong()?.div(1000) ?: 0
            } catch (e: Exception) {
                Log.e("FFmpegExecutor", "Error getting media duration", e)
                0
            }
        }
    }
}
