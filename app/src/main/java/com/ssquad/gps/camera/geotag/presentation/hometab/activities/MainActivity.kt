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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.BannerCollapsibleType
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityMainBinding
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.RequestPermissionActivity
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.loadAndShowInterWithNativeAfter
import com.ssquad.gps.camera.geotag.utils.visible

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val activityForRes =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            ViewCompat.onApplyWindowInsets(v, WindowInsetsCompat.CONSUMED)
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    override fun initData() {


    }

    override fun initView() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu[1].isEnabled = false
        (binding.bottomAppBar.background as MaterialShapeDrawable).updateCornerSize(this)
    }

    override fun initActionView() {
        setupBottomNavigation()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        initBannerHomeAd()
    }
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> initInterstitialAd {
                    replaceFragment(HomeFragment())
                }
                R.id.nav_setting -> initInterstitialAd {
                    replaceFragment(SettingsFragment())
                }
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
                initInterstitialAd {
                    Intent(this, CameraActivity::class.java).let {
                        it.putExtra(Constants.CAMERA, true)
                        activityForRes.launch(it)
                    }
                }
            } else {
                initInterstitialAd {
                    Intent(this, RequestPermissionActivity::class.java).putExtra(
                        Constants.INTENT_REQUEST_SINGLE_PERMISSION,
                        RequestPermissionActivity.TYPE_CAMERA
                    ).let {
                        activityForRes.launch(it)
                    }
                }
            }
        }
    }

    private fun initBannerHomeAd() {
        when (RemoteConfig.remoteBannerHome) {
            0L -> {
                binding.viewLine.gone()
                binding.frHomeBanner.gone()
            }

            1L -> {
                binding.frHomeBanner.visible()
                binding.viewLine.visible()
                AdmobLib.loadAndShowBanner(
                    this,
                    AdsManager.BANNER_HOME,
                    binding.frHomeBanner,
                    binding.viewLine,
                    isShowOnTestDevice = true
                )
            }

            2L -> {
                binding.frHomeBanner.visible()
                binding.viewLine.visible()
                AdmobLib.loadAndShowBannerCollapsible(
                    this,
                    AdsManager.admobBannerHome,
                    binding.frHomeBanner,
                    binding.viewLine,
                    BannerCollapsibleType.BOTTOM,
                    isShowOnTestDevice = true
                )
            }
        }
    }

    private fun initInterstitialAd(onAdComplete: () -> Unit) {
        if (AdsManager.isShowInterHome()) {
            loadAndShowInterWithNativeAfter(
                interModel = AdsManager.admobInterHome,
                vShowInterAds = binding.vShowInterAds,
            ) {
                AdsManager.lastInterShown = System.currentTimeMillis()
                onAdComplete()
            }
        } else {
            onAdComplete()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_LOCATION_PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allPermissionsGranted) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.warning_camera_location_permission),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val CAMERA_LOCATION_PERMISSION_REQUEST_CODE = 102
    }
}
