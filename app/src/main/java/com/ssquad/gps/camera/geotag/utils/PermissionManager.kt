package com.ssquad.gps.camera.geotag.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

object PermissionManager {
    fun checkPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionsGranted(context: Context, permissions: List<String>): Boolean {
        permissions.forEach { permission ->
            if (!checkPermissionGranted(context, permission)) return false
        }

        return true
    }

    fun checkLibraryGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissionGranted(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            checkPermissionsGranted(
                context,
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    fun checkCamAndMicroPermissions(context: Context): Boolean {
        return checkPermissionsGranted(
            context,
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }
     fun showPermissionExplanationDialog(context: Context, onRetry: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Yêu cầu quyền")
            .setMessage("Ứng dụng cần quyền để truy cập ảnh và video. Vui lòng cấp quyền để sử dụng tính năng này.")
            .setPositiveButton("Cho phép") { _, _ -> onRetry() }
            .setNegativeButton("Hủy", null)
            .show()
    }

     fun showOpenSettingsDialog(context: Context,gotoSettings: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Yêu cầu quyền")
            .setMessage("Bạn đã tắt quyền truy cập ảnh/video. Vui lòng mở cài đặt để cấp quyền.")
            .setPositiveButton("Mở cài đặt") { _, _ ->
                gotoSettings()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
