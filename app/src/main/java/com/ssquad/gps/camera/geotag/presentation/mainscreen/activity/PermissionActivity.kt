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
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MainActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityPermissionBinding
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.showToast
import kotlin.text.compareTo

class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {
    private var isOpenSettingApp = false
    private val requestCam =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                binding.switchCamera.isChecked = false
            } else {
                if (PermissionManager.checkCamPermissions(this)) {
                    binding.switchCamera.isChecked = true
                    binding.switchCamera.isEnabled = false // Vô hiệu hóa switch sau khi cấp quyền
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
                    binding.switchMicrophone.isEnabled = false // Vô hiệu hóa switch sau khi cấp quyền
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
                    binding.switchLocation.isEnabled = false // Vô hiệu hóa switch sau khi cấp quyền
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
                    binding.switchPhotoLibrary.isEnabled = false // Vô hiệu hóa switch sau khi cấp quyền
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
        // CAMERA
        if (PermissionManager.checkPermissionGranted(this, Manifest.permission.CAMERA)) {
            binding.switchCamera.setOnCheckedChangeListener(null) // Xóa listener trước
            binding.switchCamera.isChecked = true
            binding.switchCamera.isEnabled = false // Vô hiệu hóa switch
        } else {
            binding.switchCamera.isChecked = false
            binding.switchCamera.isEnabled = true // Đảm bảo switch có thể tương tác
            binding.switchCamera.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) requestCameraPermission()
                else binding.switchCamera.isChecked = false // Nếu người dùng tắt mà chưa cấp quyền
            }
        }

        // MICROPHONE
        if (PermissionManager.checkPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            binding.switchMicrophone.setOnCheckedChangeListener(null) // Xóa listener trước
            binding.switchMicrophone.isChecked = true
            binding.switchMicrophone.isEnabled = false // Vô hiệu hóa switch
        } else {
            binding.switchMicrophone.isChecked = false
            binding.switchMicrophone.isEnabled = true // Đảm bảo switch có thể tương tác
            binding.switchMicrophone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) requestMicrophonePermission()
                else binding.switchMicrophone.isChecked = false // Nếu người dùng tắt mà chưa cấp quyền
            }
        }

        // LOCATION
        if (PermissionManager.checkPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            binding.switchLocation.setOnCheckedChangeListener(null) // Xóa listener trước
            binding.switchLocation.isChecked = true
            binding.switchLocation.isEnabled = false // Vô hiệu hóa switch
        } else {
            binding.switchLocation.isChecked = false
            binding.switchLocation.isEnabled = true // Đảm bảo switch có thể tương tác
            binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) requestLocationPermission()
                else binding.switchLocation.isChecked = false // Nếu người dùng tắt mà chưa cấp quyền
            }
        }

        // PHOTO LIBRARY
        val photoPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (PermissionManager.checkPermissionGranted(this, photoPermission)) {
            binding.switchPhotoLibrary.setOnCheckedChangeListener(null) // Xóa listener trước
            binding.switchPhotoLibrary.isChecked = true
            binding.switchPhotoLibrary.isEnabled = false // Vô hiệu hóa switch
        } else {
            binding.switchPhotoLibrary.isChecked = false
            binding.switchPhotoLibrary.isEnabled = true // Đảm bảo switch có thể tương tác
            binding.switchPhotoLibrary.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) requestPhotoLibraryPermission()
                else binding.switchPhotoLibrary.isChecked = false // Nếu người dùng tắt mà chưa cấp quyền
            }
        }

        // Button actions
        binding.cardCamera.setOnClickListener { requestCameraPermission() }
        binding.cardMicrophone.setOnClickListener { requestMicrophonePermission() }
        binding.cardLocation.setOnClickListener { requestLocationPermission() }
        binding.cardPhotoLibrary.setOnClickListener { requestPhotoLibraryPermission() }

        binding.btnContinue.setOnClickListener {
            if (PermissionManager.checkCamPermissions(this) &&
                PermissionManager.checkMicroPermissions(this) &&
                PermissionManager.checkLocationPermissions(this) &&
                PermissionManager.checkLibraryGranted(this)
            ) {
                startMainActivity()
            }else{
                showToast(getString(R.string.warning_camera_location_permission))
            }
        }
        binding.tvGrantLater.setOnClickListener { startMainActivity() }
    }


    private fun requestCameraPermission() {
        if (!PermissionManager.checkPermissionGranted(this, Manifest.permission.CAMERA)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                PermissionManager.showOpenSettingsDialog(this) {
                    binding.switchCamera.isChecked = false
                    binding.switchCamera.isEnabled = true
                }
            } else {
                requestCam.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun requestMicrophonePermission() {
        if (!PermissionManager.checkPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                PermissionManager.showOpenSettingsDialog(this) {
                    binding.switchMicrophone.isChecked = false
                    binding.switchMicrophone.isEnabled = true

                }
            } else {
                requestRecord.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun requestLocationPermission() {
        if (!PermissionManager.checkPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionManager.showOpenSettingsDialog(this) {
                    binding.switchLocation.isChecked = false
                    binding.switchLocation.isEnabled = true
                }
            } else {
                requestLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun requestPhotoLibraryPermission() {
        val photoPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (!PermissionManager.checkPermissionGranted(this, photoPermission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, photoPermission)) {
                PermissionManager.showOpenSettingsDialog(this) {
                    binding.switchPhotoLibrary.isChecked = false
                    binding.switchPhotoLibrary.isEnabled = true
                }
            } else {
                requestAlbum.launch(photoPermission)
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        updateAllSwitchStates()
    }

    private fun updateAllSwitchStates() {
        // CAMERA
        if (PermissionManager.checkPermissionGranted(this, Manifest.permission.CAMERA)) {
            binding.switchCamera.setOnCheckedChangeListener(null)
            binding.switchCamera.isChecked = true
            binding.switchCamera.isEnabled = false
        }

        // MICROPHONE
        if (PermissionManager.checkPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
            binding.switchMicrophone.setOnCheckedChangeListener(null)
            binding.switchMicrophone.isChecked = true
            binding.switchMicrophone.isEnabled = false
        }

        // LOCATION
        if (PermissionManager.checkPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            binding.switchLocation.setOnCheckedChangeListener(null)
            binding.switchLocation.isChecked = true
            binding.switchLocation.isEnabled = false
        }

        // PHOTO LIBRARY
        val photoPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (PermissionManager.checkPermissionGranted(this, photoPermission)) {
            binding.switchPhotoLibrary.setOnCheckedChangeListener(null)
            binding.switchPhotoLibrary.isChecked = true
            binding.switchPhotoLibrary.isEnabled = false
        }else {
            binding.switchPhotoLibrary.setOnCheckedChangeListener(null)
            binding.switchPhotoLibrary.isChecked = false
            binding.switchPhotoLibrary.isEnabled = true
            binding.switchPhotoLibrary.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) requestPhotoLibraryPermission()
            }
        }
    }

}