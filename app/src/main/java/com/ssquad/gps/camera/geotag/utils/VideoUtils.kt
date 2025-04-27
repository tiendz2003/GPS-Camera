package com.ssquad.gps.camera.geotag.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

object VideoUtils {
    // Thêm một list để tracking tất cả các thư mục tạm đã tạo
    private val tempDirectories = Collections.synchronizedList(mutableListOf<File>())

    fun createTempDirectory(context: Context): File {
        val tempDir = File(context.cacheDir, "video_processing_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        tempDirectories.add(tempDir)
        return tempDir
    }

    fun createTempFile(directory: File, filename: String): File {
        val file = File(directory, filename)
        return file
    }

    suspend fun copyUriToFile(context: Context, uri: Uri, file: File) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("VideoUtils", "Error copying URI to file", e)
            if (file.exists()) {
                file.delete()
            }
            throw e
        }
    }

    fun cleanupTempFiles(directory: File?) {
        if (directory == null) return

        try {
            // Xóa tất cả file trong thư mục trước
            directory.listFiles()?.forEach { file ->
                try {
                    if (file.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        if (!file.delete()) {
                            // Force delete if normal delete fails
                            Runtime.getRuntime().exec("rm -f ${file.absolutePath}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("VideoUtils", "Error deleting file: ${file.absolutePath}", e)
                }
            }

            // Sau đó xóa thư mục cha
            if (!directory.delete()) {
                Runtime.getRuntime().exec("rm -rf ${directory.absolutePath}")
            }

            // Xóa khỏi danh sách theo dõi
            tempDirectories.remove(directory)
        } catch (e: Exception) {
            Log.e("VideoUtils", "Error cleaning up temp files", e)
        }
    }

    // Thêm phương thức để dọn dẹp tất cả thư mục tạm đã tạo
    fun cleanupAllTempDirectories() {
        val dirs = ArrayList(tempDirectories) // Copy để tránh ConcurrentModificationException
        dirs.forEach { directory ->
            cleanupTempFiles(directory)
        }
        tempDirectories.clear()
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