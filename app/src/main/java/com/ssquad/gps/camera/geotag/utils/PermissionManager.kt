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
import com.ssquad.gps.camera.geotag.R

object PermissionManager {
    private const val PREF_NAME = "permission_prefs"
    private const val PREF_FIRST_TIME = "first_time"
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

    fun checkCamPermissions(context: Context): Boolean {
        return checkPermissionsGranted(
            context,
            listOf(
                Manifest.permission.CAMERA
            )
        )
    }
    fun checkMicroPermissions(context: Context): Boolean {
        return checkPermissionsGranted(
            context,
            listOf(
                Manifest.permission.RECORD_AUDIO
            )
        )
    }
    fun checkLocationPermissions(context: Context): Boolean {
        return checkPermissionsGranted(
            context,
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    fun checkLocationGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Kiểm tra xem có nên hiển thị màn hình xin quyền hay không
     * @return true nếu cần hiển thị màn hình xin quyền, false nếu không
     */
    fun shouldShowPermissionScreen(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allPermissionsGranted = sharedPreferences.getBoolean(PREF_FIRST_TIME, false)

        // Nếu đã cấp tất cả quyền thì không hiển thị màn hình xin quyền nữa
        if (allPermissionsGranted) {
            return false
        }

        // Kiểm tra xem có quyền nào chưa được cấp không
        val cameraGranted = PermissionManager.checkCamPermissions(context)
        val microphoneGranted = PermissionManager.checkMicroPermissions(context)
        val locationGranted = PermissionManager.checkLocationPermissions(context)
        val storageGranted = PermissionManager.checkLibraryGranted(context)

        // Nếu còn quyền chưa được cấp, hiển thị màn hình xin quyền
        return !(cameraGranted && microphoneGranted && locationGranted && storageGranted)
    }
     fun showOpenSettingsDialog(context: Context,gotoSettings: () -> Unit) {
         val builder = AlertDialog.Builder(context)
         builder.setTitle(R.string.go_to_setting)
         builder.setMessage(R.string.warning_camera_location_permission)
         builder.setCancelable(false)
         builder.setPositiveButton(
             R.string.go_to_setting
         ) { _, _ ->
             gotoSettings.invoke()
             val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
             val uri = Uri.fromParts("package", context.packageName, null)
             intent.data = uri
             context.startActivity(intent)
         }
         val alert: AlertDialog = builder.create()
         alert.show()
    }
}
