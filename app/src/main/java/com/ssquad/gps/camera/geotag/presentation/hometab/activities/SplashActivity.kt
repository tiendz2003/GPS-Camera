package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.invisible
import com.ssquad.gps.camera.geotag.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.aoa.AppOnResumeAdsManager
import com.snake.squad.adslib.aoa.AppOpenAdsManager
import com.snake.squad.adslib.cmp.GoogleMobileAdsConsentManager
import com.snake.squad.adslib.utils.AdsHelper
import com.ssquad.gps.camera.geotag.databinding.ActivitySplashBinding
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.showInterSplash
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val isInitAds = AtomicBoolean(false)
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
                return
            }
        }
        if (AdsHelper.isNetworkConnected(this)) {
            binding.tvLoadingAds.visible()
            RemoteConfig.initRemoteConfig(
                this,
                initListener = object : RemoteConfig.InitListener {
                    override fun onComplete() {
                        RemoteConfig.getAllRemoteValueToLocal(this@SplashActivity)
                        if (isInitAds.get()) {
                            return
                        }
                        isInitAds.set(true)
                        setupCMP()

                    }

                    override fun onFailure() {
                        RemoteConfig.getDefaultRemoteValue()
                        if (isInitAds.get()) {
                            return
                        }
                        isInitAds.set(true)
                        setupCMP()
                    }

                })
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
        AdmobLib.initialize(this, isDebug = true, isShowAds = true, onInitializedAds = {

            if (it) {
                if (RemoteConfig.remoteOnResume == 1L) {
                    AppOnResumeAdsManager.initialize(application, AdsManager.ON_RESUME)
                    AppOnResumeAdsManager.getInstance()
                        .disableForActivity(SplashActivity::class.java)
                }
                AdsManager.reset()
                loadAds()
                if (RemoteConfig.remoteBannerSplash == 1L) {
                    binding.viewLine.visible()
                    AdmobLib.loadAndShowBanner(
                        this@SplashActivity,
                        AdsManager.BANNER_SPLASH,
                        binding.frBanner,
                        binding.viewLine
                    )
                } else {
                    binding.frBanner.gone()
                    binding.viewLine.gone()
                }
                val key = RemoteConfig.remoteSplashAds
                Log.d("SplashActivity", "key: $key")
                when (key) {
                    0L -> {
                        binding.tvLoadingAds.invisible()
                        Handler(Looper.getMainLooper()).postDelayed({
                            replaceActivity()
                        }, 3000)
                    }

                    1L -> {
                        AppOpenAdsManager(
                            this,
                            adsID = AdsManager.AOA_SPLASH,
                            timeOut = 20000,
                            onAdsCloseOrFailed = {
                                replaceActivity()
                            }).loadAndShowAoA()
                    }

                    2L -> {
                        showInterSplash {
                            replaceActivity()
                        }
                    }

                    else -> {
                        binding.tvLoadingAds.invisible()
                        Handler(Looper.getMainLooper()).postDelayed({
                            replaceActivity()
                        }, 3000)
                    }
                }
            } else {
                binding.tvLoadingAds.invisible()
                Handler(Looper.getMainLooper()).postDelayed({
                    replaceActivity()
                }, 3000)
            }
        })
    }
    private fun loadAds() {
        //chú ý đoạn này
        if (RemoteConfig.remoteNativeSetting == 1L) {
            AdmobLib.loadNative(this, AdsManager.admobNativeSetting, isCheckTestAds = true)
        }
        if (RemoteConfig.remoteNativeLanguage == 1L) {
            AdmobLib.loadNative(this, AdsManager.admobNativeLanguage)
            AdmobLib.loadNative(this, AdsManager.admobNativeLanguage2)
        }
        if (RemoteConfig.remoteInterLanguage == 1L) {
            AdmobLib.loadInterstitial(
                activity = this,
                admobInterModel = AdsManager.admobInterLanguage,
            )
        }
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
            replaceActivity()
        }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitProcess(0)
        }
    }

}