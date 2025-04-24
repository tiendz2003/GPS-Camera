package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MainActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityPermissionBinding
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.SharePrefManager

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {
    private val requestCam =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                binding.switchCamera.isChecked = false
            } else {
                if (PermissionManager.checkCamPermissions(this)) {
                    binding.switchCamera.isChecked = true
                }
            }
        }
    private val requestRecord =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                binding.switchMicrophone.isChecked = false
            } else {
                if (PermissionManager.checkMicroPermissions(this)) {
                    binding.switchMicrophone.isChecked = true
                }
            }
        }
    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                binding.switchLocation.isChecked = false
            } else {
                if (PermissionManager.checkLocationPermissions(this)) {
                    binding.switchLocation.isChecked = true
                }
            }
        }
    private val requestAlbum =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                binding.switchPhotoLibrary.isChecked = false
            } else {
                if (PermissionManager.checkLibraryGranted(this)) {
                    binding.switchPhotoLibrary.isChecked = true
                }
            }
        }

    override fun initData() {

    }

    override fun initView() {
        SharePrefManager.isFirstOpenApp(false)
    }

    override fun initActionView() {
        setupPermissionSwitches()
    }

    private fun setupPermissionSwitches() {
        binding.switchCamera.isChecked =
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        binding.switchMicrophone.isChecked =
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        binding.switchLocation.isChecked =
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        binding.switchPhotoLibrary.isChecked =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        binding.btnContinue.setOnClickListener {
            startMainActivity()
        }
        binding.tvGrantLater.setOnClickListener {
            startMainActivity()
        }
    }

    private fun requestCameraPermission() {
        if (!PermissionManager.checkPermissionGranted(this, Manifest.permission.CAMERA)) {
            requestCam.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestMicrophonePermission() {
        if (!PermissionManager.checkPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            requestRecord.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun requestLocationPermission() {
        if (!PermissionManager.checkPermissionGranted(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            requestLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestPhotoLibraryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionManager.checkPermissionGranted(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            ) {
                requestAlbum.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (!PermissionManager.checkPermissionGranted(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                requestAlbum.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun bindPermissions() {
        binding.switchCamera.isChecked =
            PermissionManager.checkPermissionGranted(this, Manifest.permission.CAMERA)
        binding.switchMicrophone.isChecked =
            PermissionManager.checkPermissionGranted(this, Manifest.permission.RECORD_AUDIO)
    }

    override fun onResume() {
        super.onResume()
        bindPermissions()
    }
}