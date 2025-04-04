package com.example.baseproject.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraHelper (private val context: Context) {
    @SuppressLint("SimpleDateFormat")
    fun takePhoto(
        imageCapture: ImageCapture,
        executor: Executor,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
       val nameFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nameFile)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/GPS-Camera")
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri?.toString() ?: return
                    onSuccess(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception.message ?: "Lỗi khi chụp ảnh")
                }
            }
        )
    }
}