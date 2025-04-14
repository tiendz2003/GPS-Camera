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
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
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

class CameraRepositoryImpl: CameraRepository {
    override suspend fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO){
        try {
            val fileName ="GPS_CAMERA_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )}.jpg"
            var fos:OutputStream? = null
            var imageUri:Uri? = null

            //Luu anh
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/GPS_CAMERA")
                put(MediaStore.MediaColumns.IS_PENDING, 1)

            }
            context.contentResolver.also { resolver ->
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                imageUri?.let {uri->
                    context.contentResolver.update(uri,contentValues,null,null)
                }

            return@withContext imageUri
        }catch (e:Exception){
            e.printStackTrace()
            return@withContext null
        }
    }

    override suspend fun saveVideoToGallery(context: Context, sourceUri: Uri): Uri? =withContext(Dispatchers.IO){
        try {
            val fileName = "GPS_CAMERA_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )}.mp4"
            var outputStream : OutputStream? = null
            var videoUri:Uri? = null
            var inputStream:InputStream? = null
            inputStream = context.contentResolver.openInputStream(sourceUri)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/GPS_CAMERA")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            context.contentResolver.also { resolver ->
                videoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = videoUri?.let { resolver.openOutputStream(it) }
            }
            outputStream?.use {output->
                inputStream?.use { input->
                    input.copyTo(output)
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            videoUri?.let {uri->
                context.contentResolver.update(uri,contentValues,null,null)
            }
            return@withContext videoUri
        }catch (e:Exception){
            e.printStackTrace()
            return@withContext null
        }finally {
            if(sourceUri.scheme =="file"){
                File(sourceUri.path!!).delete()
            }
        }
    }

    override suspend fun processVideoWithTemplate(
        context: Context,
        inputVideoUri: Uri,
        templateView: View
    ): Uri? = withContext(Dispatchers.Default) {
        try {
            // Tạo các tệp tạm
            val tempDir = File(context.cacheDir, "video_processing")
            tempDir.mkdirs()

            val tempInputFile = File(tempDir, "input_video.mp4")
            val tempTemplateFile = File(tempDir, "template_overlay.png")
            val tempOutputFile = File(tempDir, "output_video.mp4")

            context.contentResolver.openInputStream(inputVideoUri)?.use { input ->
                FileOutputStream(tempInputFile).use { output ->
                    input.copyTo(output)
                }
            }

            exportTemplateToImage(templateView, tempTemplateFile.absolutePath)

            // Xử lý video với FFmpeg
            val cmd = arrayOf(
                "-y", // Ghi đè nếu output file tồn tại
                "-i", tempInputFile.absolutePath,
                "-i", tempTemplateFile.absolutePath,
                "-filter_complex", "[0:v][1:v]overlay=(main_w-overlay_w)/2:main_h-overlay_h",
               // "-c:v", "mpeg4", // Dùng encoder mpeg4 - nhanh hơn libx264
                "-preset", "ultrafast", // Tăng tốc nếu dùng libx264 (dự phòng)
                "-pix_fmt", "yuv420p", // Đảm bảo file có thể phát trong mọi trình phát
                "-c:a", "copy", // Giữ lại âm thanh gốc, không encode lại
                "-movflags", "+faststart", // Tối ưu phát video ngay khi tải
                tempOutputFile.absolutePath
            )



            val rc = FFmpeg.execute(cmd)

            if (rc == RETURN_CODE_SUCCESS) {
                // Lưu video vào thư viện
                return@withContext saveVideoToGallery(context, Uri.fromFile(tempOutputFile))
            } else {
                Log.e("FFmpeg", "Command failed with rc=${rc} and output: ${FFmpeg.listExecutions()}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("VideoProcessor", "Error processing video", e)
            return@withContext null
        }
    }
    private suspend fun exportTemplateToImage(templateView: View, outputPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (templateView.width == 0 || templateView.height == 0) {
                return@withContext false
            }

            val bitmap = createBitmap(templateView.width, templateView.height)
            val canvas = Canvas(bitmap)
            templateView.draw(canvas)

            val outputFile = File(outputPath)
            val outputStream = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}