package com.example.baseproject.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.example.baseproject.domain.CameraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraRepositoryImpl : CameraRepository {
    override suspend fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "GPS_CAMERA_${
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                        Date()
                    )
                }.jpg"
                var fos: OutputStream? = null
                var imageUri: Uri? = null

                //Luu anh
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis() / 1000)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/GPS_CAMERA")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                context.contentResolver.also { resolver ->
                    imageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
                fos?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                imageUri?.let { uri ->
                    context.contentResolver.update(uri, contentValues, null, null)
                }

                return@withContext imageUri
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

    override suspend fun saveVideoToGallery(context: Context, sourceUri: Uri): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "GPS_CAMERA_${
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                        Date()
                    )
                }.mp4"
                var outputStream: OutputStream? = null
                var videoUri: Uri? = null
                var inputStream: InputStream? = null
                inputStream = context.contentResolver.openInputStream(sourceUri)
                val contentValues = ContentValues()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    contentValues.apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/GPS_CAMERA")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
                context.contentResolver.also { resolver ->
                    videoUri =
                        resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    outputStream = videoUri?.let { resolver.openOutputStream(it) }
                }
                outputStream?.use { output ->
                    inputStream?.use { input ->
                        input.copyTo(output)
                    }
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                videoUri?.let { uri ->
                    context.contentResolver.update(uri, contentValues, null, null)
                }
                Log.d("VideoProcessor", "Video saved at: $videoUri")
                return@withContext videoUri
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VideoProcessor", "Error: ${e.message}")
                return@withContext null
            } finally {
                if (sourceUri.scheme == "file") {
                    File(sourceUri.path!!).delete()
                }
            }
        }

    override suspend fun processVideoWithTemplate(
        context: Context,
        videoUri: Uri,
        templateView: View
    ): Uri? = withContext(Dispatchers.Default) {
        try {
            // Tạo các tệp tạm
            val tempDir = File(context.cacheDir, "video_processing")
            tempDir.mkdirs()

            val tempInputFile = File(tempDir, "input_video.mp4")
            val tempTemplateFile = File(tempDir, "template_overlay.png")
            val tempOutputFile = File(tempDir, "output_video.mp4")
            //luu vào file tạm
            context.contentResolver.openInputStream(videoUri)?.use { input ->
                FileOutputStream(tempInputFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!exportTemplateToImage(templateView, tempTemplateFile.absolutePath)) {
                Log.e("VideoProcessor", "Không export được template vào ảnh")
                return@withContext null
            }
            Log.d(
                "TemplateExport",
                "Template file exists: ${File(tempTemplateFile.absolutePath).exists()}, size: ${
                    File(tempTemplateFile.absolutePath).length()
                }"
            )

            // Xử lý video với FFmpeg
            val cmd = arrayOf(
                "-y",//ghi đè file nếu đã tồn tại
                "-i",
                tempInputFile.absolutePath,//video gốc
                "-i",
                tempTemplateFile.absolutePath,//template
                "-filter_complex",
                "[0:v][1:v]overlay=(main_w-overlay_w)/2:main_h-overlay_h",//vj tri template
                "-c:v",
                "libx264",
                "-preset",
                "ultrafast",//tăng tốc độ xử lý
                "-pix_fmt",
                "yuv420p",
                "-c:a",
                "aac",//mã hoá âm thanh
                "-b:a",
                "128k",
                "-movflags",
                "+faststart",//tối ưu phát
                tempOutputFile.absolutePath
            ).joinToString(" ")
            Log.d("FFmpegCommand", "CMD: $cmd")
            // đợi xử lý xong và lấy return code
            //GHi nhứo: executeAsync là hàm không đồng bộ, không chặn luồng chính
            val returnCode = suspendCoroutine<ReturnCode?> { continuation ->
                FFmpegKit.executeAsync(
                    cmd,
                    { session ->
                        continuation.resume(session.returnCode)
                        Log.i(
                            "VideoProcessor",
                            "xu ly thanh cong FFmpeg code: ${session.returnCode}"
                        )
                    },
                    { log ->
                        Log.d("FFmpegKitLog", log.message)
                    },
                    { statistics ->
                        Log.d("FFmpegKit", "Thoi gian: ${statistics.time}")
                    }
                )
            }
            //xử lý xong rồi ,lấy return code
            if (ReturnCode.isSuccess(returnCode)) {
                Log.i("VideoProcessor", "Xử lý video thành công")
                // Lưu video vào thư viện xoa file tạm
                val result = saveVideoToGallery(context, Uri.fromFile(tempOutputFile))
                tempInputFile.delete()
                tempTemplateFile.delete()
                tempOutputFile.delete()
                tempDir.deleteRecursively()
                return@withContext result
            } else {
                Log.e("FFmpeg", "THẤT BẠI  rc=${returnCode})}")
                tempDir.deleteRecursively()
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("VideoProcessor", "Lỗi khi xử lý video", e)
            return@withContext null
        }
    }

    private suspend fun exportTemplateToImage(templateView: View, outputPath: String): Boolean =
        withContext(Dispatchers.Main) {
            try {
                Log.d(
                    "TemplateExport",
                    "Kích thước template: ${templateView.width} x ${templateView.height}"
                )

                if (templateView.width == 0 || templateView.height == 0) {
                    return@withContext false
                }

                // Tạo bitmap từ View trên Main thread
                val bitmap = createBitmap(templateView.width, templateView.height)
                val canvas = Canvas(bitmap)
                templateView.draw(canvas)

                // luu file ->IO
                withContext(Dispatchers.IO) {
                    val outputFile = File(outputPath)
                    FileOutputStream(outputFile).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.flush()
                    }
                    Log.d("TemplateExport", "Export thành công vào: $outputPath")
                }

                return@withContext true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TemplateExport", "Lỗi khi export template: ${e.message}", e)
                return@withContext false
            }
        }
}