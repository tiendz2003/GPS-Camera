package com.ssquad.gps.camera.geotag.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.ssquad.gps.camera.geotag.domain.CameraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap
import com.ssquad.gps.camera.geotag.utils.FFmpegExecutor
import com.ssquad.gps.camera.geotag.utils.FileUtils
import com.ssquad.gps.camera.geotag.utils.VideoUtils
import kotlinx.coroutines.delay
class CameraRepositoryImpl(
    private val context: Context,
    private val ffmpegExecutor: FFmpegExecutor
) : CameraRepository {
    override suspend fun saveImageToGallery(
        context: Context,
        bitmap: Bitmap,
        address: String?
    ): Uri? =
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
                    if (!address.isNullOrEmpty()) {
                        put(MediaStore.Images.ImageColumns.DESCRIPTION, address)
                    }
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
                if (!address.isNullOrEmpty() && imageUri != null) {
                    addExifLocationData(context, imageUri!!, address)
                }

                return@withContext imageUri
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }

    @SuppressLint("SuspiciousIndentation")
    override suspend fun saveVideoToGallery(
        sourceUri: Uri,
        address: String?
    ): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "GPS_CAMERA_${
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                }.mp4"
                Log.d("VideoProcessor", "Saving video)")
                var outputStream: OutputStream? = null
                var videoUri: Uri? = null

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.TITLE, fileName)
                    put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                    put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/GPS_CAMERA")
                    put(MediaStore.Video.Media.DURATION, 0) // Sẽ được cập nhật sau
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                    if (!address.isNullOrEmpty()) {
                        put(MediaStore.Video.VideoColumns.DESCRIPTION, address)
                    }
                }

                // Sử dụng Video.Media.EXTERNAL_CONTENT_URI cho video
                context.contentResolver.also { resolver ->
                    videoUri =
                        resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    outputStream = videoUri?.let { resolver.openOutputStream(it) }
                }

                outputStream?.use { output ->
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        input.copyTo(output)
                    }
                }

                // Cập nhật IS_PENDING để hoàn tất quá trình
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                videoUri?.let { uri ->
                    context.contentResolver.update(uri, contentValues, null, null)
                }

                // Debug log để kiểm tra thông tin về video đã lưu
                videoUri?.let { uri ->
                    val projection = arrayOf(
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.BUCKET_ID,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Video.Media.DATA
                    )
                    context.contentResolver.query(uri, projection, null, null, null)
                        ?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val bucketIdIndex =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
                                val bucketNameIndex =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                                val dataIndex =
                                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                                val bucketId = cursor.getString(bucketIdIndex)
                                val bucketName = cursor.getString(bucketNameIndex)
                                val data = cursor.getString(dataIndex)

                                Log.d(
                                    "VideoSave",
                                    "BUCKET_ID=$bucketId, BUCKET_NAME=$bucketName, PATH=$data"
                                )
                            }
                        }
                }

                // Quét lại MediaStore để cập nhật thông tin
                videoUri?.let { uri ->
                    try {
                        // Đợi một chút để đảm bảo file đã được ghi đầy đủ
                        delay(500)

                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(getFilePathFromUri(context, uri) ?: return@let),
                            arrayOf("video/mp4")
                        ) { path, scannedUri ->
                            Log.d("VideoScan", "Scanned: $path -> $scannedUri")
                        }
                    } catch (e: Exception) {
                        Log.e("VideoScan", "Scan failed: ${e.message}")
                    }
                }
                if (!address.isNullOrEmpty() && videoUri != null) {
                    addExifLocationData(context, videoUri!!, address)
                }

                return@withContext videoUri

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VideoProcessor", "Error saving video: ${e.message}")
                return@withContext null
            } finally {
                if (sourceUri.scheme == "file") {
                    try {
                        File(sourceUri.path!!).delete()
                    } catch (e: Exception) {
                        Log.e("VideoProcessor", "Error deleting temp file: ${e.message}")
                    }
                }
            }
        }

    private fun addExifLocationData(context: Context, imageUri: Uri, locationAddress: String) {
        try {
            val path = getFilePathFromUri(context, imageUri) ?: return

            val exifInterface = ExifInterface(path)

            exifInterface.setAttribute(ExifInterface.TAG_USER_COMMENT, locationAddress)
            exifInterface.saveAttributes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Hàm hỗ trợ để lấy đường dẫn file từ Uri
    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    override suspend fun processVideoWithTemplate(
        inputUri: Uri,
        templateView: View?,
        address: String?
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // Chuẩn bị các file tạm
            val tempDir = VideoUtils.createTempDirectory(context)
            val tempInputFile = VideoUtils.createTempFile(tempDir, "input_video.mp4")
            val tempOutputFile = VideoUtils.createTempFile(tempDir, "output_video.mp4")

            // Copy video gốc vào file tạm
            VideoUtils.copyUriToFile(context, inputUri, tempInputFile)

            // Xử lý với template nếu có
            if (templateView != null) {
                val templateFile = VideoUtils.createTempFile(tempDir, "template_overlay.png")
                val templatePath = exportTemplateToImage(templateView)

                if (templatePath != null) {
                    // Xử lý video với overlay
                    val success = ffmpegExecutor.processVideoWithOverlay(
                        tempInputFile.absolutePath,
                        templatePath,
                        tempOutputFile.absolutePath
                    )

                    if (!success) {
                        return@withContext null
                    }
                } else {
                    // Fallback nếu không export được template
                    FileUtils.copy(tempInputFile, tempOutputFile)
                }
            } else {
                // Không có template, chỉ copy
                FileUtils.copy(tempInputFile, tempOutputFile)
            }

            // Lưu vào gallery
            val savedUri = saveVideoToGallery(Uri.fromFile(tempOutputFile), address)

            // Dọn dẹp
            VideoUtils.cleanupTempFiles(tempDir)

            return@withContext savedUri
        } catch (e: Exception) {
            Log.e(TAG, "Error processing video", e)
            return@withContext null
        }
    }

    override suspend fun exportTemplateToImage(view: View): String? = withContext(Dispatchers.Main) {
        try {
            if (view.width <= 0 || view.height <= 0) return@withContext null

            val bitmap = createBitmap(view.width, view.height)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val tempFile = File(
                context.cacheDir,
                "template_${System.currentTimeMillis()}.png"
            )

            withContext(Dispatchers.IO) {
                FileOutputStream(tempFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
            }

            return@withContext tempFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting template", e)
            return@withContext null
        }
    }
    companion object {
        private const val TAG = "VideoProcessor"
    }
}