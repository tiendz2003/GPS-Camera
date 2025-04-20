package com.example.baseproject.presentation.hometab.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityMainBinding
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.baseproject.R
import com.example.baseproject.fragments.HomeFragment
import com.example.baseproject.fragments.SettingsFragment
import com.example.baseproject.presentation.mainscreen.activity.CameraActivity
import com.example.baseproject.presentation.mainscreen.activity.PermissionActivity
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.dpToPx
import com.example.baseproject.utils.updateCornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allPermissionsGranted = SharePrefManager.getBoolean("all_permissions_granted", false)

        // Chỉ cho vào main khi đã cấp đủ quyền
        if (!allPermissionsGranted) {
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
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun areAllPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this, CameraActivity::class.java))
        } else {
            Toast.makeText(this, "Quyền camera là cần thiết để chụp ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }
}
