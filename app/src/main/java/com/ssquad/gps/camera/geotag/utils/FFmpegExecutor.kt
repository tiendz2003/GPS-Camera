package com.ssquad.gps.camera.geotag.utils

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FFmpegExecutor {

    suspend fun processVideoWithOverlay(
        inputPath: String,
        overlayPath: String,
        outputPath: String,
        progressCallback: ((Float) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.Default) {
        try {
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
                        progressCallback?.invoke(statistics.time.toFloat() / 1000)
                    }
                )
            }

            return@withContext ReturnCode.isSuccess(returnCode)
        } catch (e: Exception) {
            Log.e("FFmpegExecutor", "Error executing FFmpeg command", e)
            return@withContext false
        }
    }
}
