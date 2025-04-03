package com.example.baseproject.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {
    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 100

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun checkAndRequestPermissions(activity: AppCompatActivity): Boolean {
            val permissionsToRequest = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 trở lên cần READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else
                // Android 10 - 12 cần READ_MEDIA_IMAGES
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
    }
}
