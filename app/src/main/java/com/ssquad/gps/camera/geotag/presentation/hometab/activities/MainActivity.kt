package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.ssquad.gps.camera.geotag.fragments.HomeFragment
import com.ssquad.gps.camera.geotag.fragments.SettingsFragment
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.CameraActivity
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.PermissionActivity
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.updateCornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityMainBinding
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.RequestPermissionActivity
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.PermissionManager

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )
    private val activityForRes =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allPermissionsGranted = SharePrefManager.getBoolean("all_permissions_granted", false)
        val permissionsSkipped = SharePrefManager.getBoolean("permissions_skipped", false)

        if (!allPermissionsGranted && !permissionsSkipped) {
            startActivity(Intent(this, PermissionActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    override fun initData() {}

    override fun initView() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu[1].isEnabled = false
        (binding.bottomAppBar.background as MaterialShapeDrawable).updateCornerSize(this)
    }

    override fun initActionView() {
        setupBottomNavigation()
        setupFab()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_setting -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            if (PermissionManager.checkPermissionsGranted(
                    this,
                    listOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            ) {
                Intent(this, CameraActivity::class.java).let {
                    it.putExtra(Constants.CAMERA, true)
                    activityForRes.launch(it)
                }
            } else {
                Intent(this, RequestPermissionActivity::class.java).let {
                    it.putExtra(
                        Constants.INTENT_REQUEST_SINGLE_PERMISSION,
                        RequestPermissionActivity.TYPE_CAMERA
                    )
                    activityForRes.launch(it)
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_LOCATION_PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allPermissionsGranted) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                Toast.makeText(this, getString(R.string.warning_camera_location_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_LOCATION_PERMISSION_REQUEST_CODE = 102
    }
}
