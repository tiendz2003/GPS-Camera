package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MainActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityPermissionBinding
import com.ssquad.gps.camera.geotag.utils.SharePrefManager

class PermissionActivity : BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_MEDIA_IMAGES
    )
    private val PERMISSION_REQUEST_CODE = 100
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun initData() {

    }

    override fun initView() {
    }

    override fun initActionView() {
        setupPermissionSwitches()

        setupSkipButton()
    }
    private fun setupPermissionSwitches() {
        binding.switchCamera.isChecked = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        binding.switchMicrophone.isChecked = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        binding.switchLocation.isChecked = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        binding.switchPhotoLibrary.isChecked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        binding.cardCamera.setOnClickListener { requestCameraPermission() }
        binding.cardMicrophone.setOnClickListener { requestMicrophonePermission() }
        binding.cardLocation.setOnClickListener { requestLocationPermission() }
        binding.cardPhotoLibrary.setOnClickListener { requestPhotoLibraryPermission() }

        binding.switchCamera.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
            }
        }

        binding.switchMicrophone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestMicrophonePermission()
            }
        }
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission()
            }
        }

        binding.switchPhotoLibrary.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestPhotoLibraryPermission()
            }
        }
    }
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }
    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun requestPhotoLibraryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun setupSkipButton() {
        binding.btnSkip.setOnClickListener {
            SharePrefManager.putBoolean("permissions_skipped", true)
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            updateSwitchStates()

            if (areAllPermissionsGranted()) {
                SharePrefManager.putBoolean("all_permissions_granted", true)
                Handler(Looper.getMainLooper()).postDelayed({
                    startMainActivity()
                }, 1000)
            }
        }
    }

    private fun updateSwitchStates() {
        binding.switchCamera.isChecked = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        binding.switchMicrophone.isChecked = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        binding.switchLocation.isChecked = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        binding.switchPhotoLibrary.isChecked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun areAllPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        // Only set the flag here when all permissions are truly granted
        SharePrefManager.putBoolean("all_permissions_granted", true)
        return true
    }
}