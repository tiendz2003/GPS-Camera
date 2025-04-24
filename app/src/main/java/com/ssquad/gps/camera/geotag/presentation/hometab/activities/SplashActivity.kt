package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.invisible
import com.ssquad.gps.camera.geotag.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.cmp.GoogleMobileAdsConsentManager
import com.snake.squad.adslib.utils.AdsHelper
import com.ssquad.gps.camera.geotag.databinding.ActivitySplashBinding
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    private var isMobileAdsInitializeCalled = AtomicBoolean(false)

    override fun initData() {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN
        ) {
            finish()
            return
        }
    }

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            }
        }
        if (AdsHelper.isNetworkConnected(this)) {
            binding.tvLoadingAds.visible()
            setupCMP()
        } else {
            binding.tvLoadingAds.invisible()
            Handler(Looper.getMainLooper()).postDelayed({
                replaceActivity()
            }, 3000)
        }

    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun setupCMP() {
        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(this)
        googleMobileAdsConsentManager.gatherConsent { error ->
            error?.let {
                initializeMobileAdsSdk()
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.get()) {
            //start action
            return
        }
        isMobileAdsInitializeCalled.set(true)
        initAds()
    }

    private fun initAds() {
        AdmobLib.setCheckTestDevice(false)
        AdmobLib.initialize(this, isDebug = true, isShowAds = false, onInitializedAds = {

            if (it) {
                // todo: fix here
                binding.tvLoadingAds.invisible()
                Handler(Looper.getMainLooper()).postDelayed({
                    replaceActivity()
                }, 3000)
            } else {
                binding.tvLoadingAds.invisible()
                Handler(Looper.getMainLooper()).postDelayed({
                    replaceActivity()
                }, 3000)
            }
        })
    }

    private fun replaceActivity() {
        val intent = Intent(this@SplashActivity, LanguageActivity::class.java)
        intent.putExtra(Constants.LANGUAGE_EXTRA, false)
        startActivity(intent)
        finish()
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
        }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitProcess(0)
        }
    }

}