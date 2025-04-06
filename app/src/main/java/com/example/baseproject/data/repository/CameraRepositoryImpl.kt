package com.example.baseproject.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.baseproject.domain.CameraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
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
}