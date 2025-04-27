package com.ssquad.gps.camera.geotag.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object VideoUtils {
    fun createTempDirectory(context: Context): File {
        val tempDir = File(context.cacheDir, "video_processing_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        return tempDir
    }

    fun createTempFile(directory: File, filename: String): File {
        return File(directory, filename)
    }

    suspend fun copyUriToFile(context: Context, uri: Uri, file: File) = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun cleanupTempFiles(directory: File) {
        directory.deleteRecursively()
    }
}

object FileUtils {
    suspend fun copy(source: File, destination: File) = withContext(Dispatchers.IO) {
        source.inputStream().use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}