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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
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
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (PermissionManager.checkPermissionGranted(this, permissionForGallery)) {
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
                finish()
            } else {
                PermissionManager.showOpenSettingsDialog(this) {
                    isOpenSettingApp = true
                }
            }
        }


    override fun initData() {

    }

    override fun initView() {
        //this.window.setBackgroundDrawableResource(R.drawable.bg_transparent)
        type = intent.getIntExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, 0)
        when (type) {
            TYPE_CAMERA -> {
                binding.imvBanner.loadImageIcon(R.drawable.ic_per_camera)
                binding.tvTitle.text = getString(R.string.til_per_camera)
                binding.tvContent.text = getString(R.string.hint_per_camera)
            }

            TYPE_GALLERY -> {
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

    override fun initActionView() {
        TODO("Not yet implemented")
    }
    override fun onResume() {
        super.onResume()
        if (isOpenSettingApp) {
            isOpenSettingApp = false
            when (type) {
                TYPE_CAMERA -> {
                    if (PermissionManager.checkPermissionsGranted(this, permissionsForCamera)) {
                      //  showToast(getString(R.string.per_granted))
                        finish()
                        navToActivity(this, CameraActivity::class.java)
                    }
                }

                TYPE_GALLERY -> {
                    if (PermissionManager.checkPermissionGranted(this, permissionForGallery)) {
                       // showToast(getString(R.string.per_granted))
                        finish()
                        navToActivity(
                            this@RequestPermissionActivity,
                            MediaSavedActivity::class.java,Bundle().apply {
                                putBoolean(Constants.IS_VIDEO, false)
                            }
                        )
                    }
                }

                TYPE_LOCATION -> {
                    if (PermissionManager.checkPermissionGranted(this, permissionForLocation)) {
                        //showToast(getString(R.string.per_granted))
                        finish()
                        startActivity(
                            Intent(
                                this@RequestPermissionActivity,
                                RequestPermissionActivity::class.java
                            ).apply {
                                putExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, TYPE_LOCATION)
                            })
                    }
                }

                TYPE_SAVE_IMG -> {
                    if (PermissionManager.checkPermissionGranted(this, permissionForGallery)) {
                      //  showToast(getString(R.string.per_granted))
                        finish()
                    }
                }

                else -> {
                    if (PermissionManager.checkPermissionsGranted(this, permissionsForCamera)) {
                       // showToast(getString(R.string.per_granted))
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
        if (!PermissionManager.checkPermissionGranted(this, permissionForGallery)) {
            requestPermissionForGallery.launch(permissionForGallery)
        } else {
            //showToast(getString(R.string.per_granted))
            finish()
        }
    }

    private fun requestPermissionLocation() {
        if (!PermissionManager.checkPermissionGranted(this, permissionForLocation)) {
            requestPermissionForLocation.launch(permissionForLocation)
        } else {
            //showToast(getString(R.string.per_granted))
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