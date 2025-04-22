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
        private const val REQUEST_STORAGE_PERMISSION = 100

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun checkAndRequestPermissions(activity: AppCompatActivity): Boolean {
            val permissionsToRequest = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

            return if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), REQUEST_STORAGE_PERMISSION)
                false
            } else {
                true
            }
        }

        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit
        ) {
            if (requestCode == REQUEST_STORAGE_PERMISSION) {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
        fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }

    fun requestPermissions(
        context: Context,
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: (shouldShowRationale: Boolean) -> Unit
    ) {
        // Kiểm tra nếu đã có tất cả quyền
        if (hasPermissions(context, permissions)) {
            onPermissionGranted()
            return
        }

        // Kiểm tra nếu nên hiển thị giải thích
        val activity = context as? AppCompatActivity
        if (activity != null) {
            val shouldShowRationale = permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
            }

            if (shouldShowRationale) {
                showPermissionExplanationDialog(context) {
                    launcher.launch(permissions)
                }
            } else {
                launcher.launch(permissions)
            }
        } else {
            launcher.launch(permissions)
        }
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
