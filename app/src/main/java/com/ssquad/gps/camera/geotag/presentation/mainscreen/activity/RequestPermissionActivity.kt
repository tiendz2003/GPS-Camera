package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityRequestPermissionBinding
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.EditAlbumLibraryActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MediaSavedActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingActivity
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.loadImageIcon
import com.ssquad.gps.camera.geotag.utils.navToActivity

class RequestPermissionActivity :
    BaseActivity<ActivityRequestPermissionBinding>(ActivityRequestPermissionBinding::inflate) {

    var type = TYPE_CAMERA
    private var isOpenSettingApp = false

    private val permissionsForCamera by lazy {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val permissionForGallery by lazy {
        listOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            },
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val permissionForLocation by lazy { Manifest.permission.ACCESS_FINE_LOCATION }

    private val requestPermissionForCamera =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (PermissionManager.checkPermissionsGranted(this, permissionsForCamera)) {
                navToActivity(this, CameraActivity::class.java)
                finish()
            } else {
                PermissionManager.showOpenSettingsDialog(this) {
                    isOpenSettingApp = true
                }
            }
        }

    private val requestPermissionForGallery =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (PermissionManager.checkPermissionsGranted(this, permissionForGallery)) {
                if (intent.getBooleanExtra(Constants.INTENT_LIBRARY_PERMISSION, false)) {
                    navToActivity(this@RequestPermissionActivity, EditAlbumLibraryActivity::class.java)
                } else {
                    navToActivity(this@RequestPermissionActivity, MediaSavedActivity::class.java)
                }
                finish()
            } else {
                PermissionManager.showOpenSettingsDialog(this) {
                    isOpenSettingApp = true
                }
            }
        }

    private val requestPermissionForLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (PermissionManager.checkPermissionGranted(this, permissionForLocation)) {
                navToActivity(this@RequestPermissionActivity, MapSettingActivity::class.java)
                finish()
            } else {
                PermissionManager.showOpenSettingsDialog(this) {
                    isOpenSettingApp = true
                }
            }
        }

    override fun initData() {}

    override fun initView() {
        type = intent.getIntExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, 0)
        when (type) {
            TYPE_CAMERA -> {
                binding.imvBanner.loadImageIcon(R.drawable.ic_per_camera)
                binding.tvTitle.text = getString(R.string.til_per_camera)
                binding.tvContent.text = getString(R.string.hint_per_camera)
            }

            TYPE_GALLERY, TYPE_SAVE_IMG -> {
                binding.imvBanner.loadImageIcon(R.drawable.ic_per_image)
                binding.tvTitle.text = getString(R.string.til_per_gallery)
                binding.tvContent.text = getString(R.string.hint_per_gallery)
            }

            TYPE_LOCATION -> {
                binding.imvBanner.loadImageIcon(R.drawable.ic_per_location)
                binding.tvTitle.text = getString(R.string.til_per_location)
                binding.tvContent.text = getString(R.string.hint_per_location)
            }

            else -> {
                binding.imvBanner.loadImageIcon(R.drawable.ic_per_camera)
                binding.tvTitle.text = getString(R.string.til_per_camera)
                binding.tvContent.text = getString(R.string.hint_per_camera)
            }
        }

        binding.imvBack.setOnClickListener { finish() }

        binding.btnGrant.setOnClickListener {
            when (type) {
                TYPE_CAMERA -> requestCameraRecord()
                TYPE_GALLERY, TYPE_SAVE_IMG -> requestPermissionGallery()
                TYPE_LOCATION -> requestPermissionLocation()
                else -> requestCameraRecord()
            }
        }
    }

    override fun initActionView() {}

    override fun onResume() {
        super.onResume()
        if (isOpenSettingApp) {
            isOpenSettingApp = false
            when (type) {
                TYPE_CAMERA -> {
                    if (PermissionManager.checkPermissionsGranted(this, permissionsForCamera)) {
                        finish()
                        navToActivity(this, CameraActivity::class.java)
                    }
                }

                TYPE_GALLERY, TYPE_SAVE_IMG -> {
                    if (PermissionManager.checkPermissionsGranted(this, permissionForGallery)) {
                        finish()
                        val intentTarget = if (intent.getBooleanExtra(Constants.INTENT_LIBRARY_PERMISSION, false)) {
                            EditAlbumLibraryActivity::class.java
                        } else {
                            MediaSavedActivity::class.java
                        }
                        navToActivity(this@RequestPermissionActivity, intentTarget)
                    }
                }

                TYPE_LOCATION -> {
                    if (PermissionManager.checkPermissionGranted(this, permissionForLocation)) {
                        finish()
                        navToActivity(this@RequestPermissionActivity, MapSettingActivity::class.java)
                    }
                }

                else -> {
                    if (PermissionManager.checkPermissionsGranted(this, permissionsForCamera)) {
                        finish()
                    }
                }
            }
        }
    }

    private fun requestCameraRecord() {
        if (!PermissionManager.checkPermissionsGranted(this, permissionsForCamera)) {
            requestPermissionForCamera.launch(permissionsForCamera.toTypedArray())
        }
    }

    private fun requestPermissionGallery() {
        if (!PermissionManager.checkPermissionsGranted(this, permissionForGallery)) {
            requestPermissionForGallery.launch(permissionForGallery.toTypedArray())
        } else {
            finish()
        }
    }

    private fun requestPermissionLocation() {
        if (!PermissionManager.checkPermissionGranted(this, permissionForLocation)) {
            requestPermissionForLocation.launch(permissionForLocation)
        } else {
            finish()
        }
    }

    companion object {
        const val TYPE_CAMERA = 0
        const val TYPE_GALLERY = 1
        const val TYPE_LOCATION = 2
        const val TYPE_ALL = 3
        const val TYPE_SAVE_IMG = 4
        const val REQUEST_CODE = 1001
    }
}

