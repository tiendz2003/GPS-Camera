package com.ssquad.gps.camera.geotag.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.ssquad.gps.camera.geotag.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.contains
import kotlin.text.get

object RemoteConfig {

    private var isInit = false
    private var isTimedOut = false

    var remoteSplashAds: Long
        get() {
            return SharePrefManager.get(Keys.SPLASH_ADS, 2L)
        }
        set(value) {
            SharePrefManager.put(Keys.SPLASH_ADS, value)
        }

    var remoteBannerSplash: Long
        get() {
            return SharePrefManager.get(Keys.BANNER_SPLASH, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.BANNER_SPLASH, value)
        }
    var remoteNativeLanguage: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_LANGUAGE, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_LANGUAGE, value)
        }
    var remoteInterLanguage: Long
        get() {
            return SharePrefManager.get(Keys.INTER_LANGUAGE, 0L)
        }
        set(value) {
            SharePrefManager.put(Keys.INTER_LANGUAGE, value)
        }
    var remoteNativeIntro: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_INTRO, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_INTRO, value)
        }
    var remoteInterIntro: Long
        get() {
            return SharePrefManager.get(Keys.INTER_INTRO, 0L)
        }
        set(value) {
            SharePrefManager.put(Keys.INTER_INTRO, value)
        }
    var remoteBannerHome: Long
        get() {
            return SharePrefManager.get(Keys.BANNER_HOME, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.BANNER_HOME, value)
        }
    var remoteNativeHome: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_HOME, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_HOME, value)
        }
    var remoteInterHome: Long
        get() {
            return SharePrefManager.get(Keys.INTER_HOME, 0L)
        }
        set(value) {
            SharePrefManager.put(Keys.INTER_HOME, value)
        }
    var remoteInterBackToHome: Long
        get() {
            return SharePrefManager.get(Keys.INTER_BACK_TO_HOME, 2L)
        }
        set(value) {
            SharePrefManager.put(Keys.INTER_BACK_TO_HOME, value)
        }
    var remoteNativePhotoSelector: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_PHOTO_SELECTOR, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_PHOTO_SELECTOR, value)
        }

    var remoteNativeThemeSelector: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_THEME_SELECTOR, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_THEME_SELECTOR, value)
        }

    var remoteNativeSaved: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_SAVED, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_SAVED, value)
        }

    var remoteInterSave: Long
        get() {
            return SharePrefManager.get(Keys.INTER_SAVED, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.INTER_SAVED, value)
        }


    var remoteBannerPhotoEditor: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_BANNER_EDITOR, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_BANNER_EDITOR, value)
        }



    var remoteNativeSetting: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_SETTING, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_SETTING, value)
        }

    var remoteNativeAfterInter: Long
        get() {
            return SharePrefManager.get(Keys.NATIVE_AFTER_INTER, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.NATIVE_AFTER_INTER, value)
        }

    var remoteOnResume: Long
        get() {
            return SharePrefManager.get(Keys.ON_RESUME, 1L)
        }
        set(value) {
            SharePrefManager.put(Keys.ON_RESUME, value)
        }

    var remoteTimeShowInter: Long
        get() {
            return SharePrefManager.get(Keys.TIME_SHOW_INTER, 20000L)
        }
        set(value) {
            SharePrefManager.put(Keys.TIME_SHOW_INTER, value)
        }

    fun getRemoteValue(keyType: String): Long {
        return SharePrefManager.get(keyType, 0L)
    }

    fun initRemoteConfig(
        activity: AppCompatActivity,
        timeOut: Long = 8000,
        initListener: InitListener
    ) {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        mFirebaseRemoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                mFirebaseRemoteConfig.activate().addOnCompleteListener {
                    isInit = true
                    if (!isTimedOut) initListener.onComplete()
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                isInit = false
                if (!isTimedOut) initListener.onFailure()
            }
        })
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                isInit = true
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isTimedOut) initListener.onComplete()
                }, 2000)
            }
        }
        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeOut)
            if (!isInit) {
                isTimedOut = true
                initListener.onFailure()
            }
        }
    }

    fun getRemoteLongValue(key: String): Long {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        return mFirebaseRemoteConfig.getLong(key)
    }

    fun getAllRemoteValueToLocal(context: Context) {
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        val remoteValues = mutableMapOf<String, Long>()
        val remoteKeys = listOf(
            Keys.SPLASH_ADS, Keys.BANNER_SPLASH, Keys.NATIVE_LANGUAGE,
            Keys.INTER_LANGUAGE, Keys.NATIVE_INTRO, Keys.INTER_INTRO,
            Keys.INTER_HOME, Keys.INTER_BACK_TO_HOME, Keys.NATIVE_HOME,
            Keys.BANNER_HOME, Keys.NATIVE_PHOTO_SELECTOR, Keys.NATIVE_BANNER_EDITOR,
            Keys.INTER_SAVED, Keys.NATIVE_THEME_SELECTOR, Keys.NATIVE_SAVED
            , Keys.NATIVE_SETTING, Keys.NATIVE_AFTER_INTER,
            Keys.ON_RESUME, Keys.TIME_SHOW_INTER
        )

        remoteKeys.forEach { key ->
            val remoteValue = mFirebaseRemoteConfig.getLong(key)
            remoteValues[key] = remoteValue
        }

        SharePrefManager.putMultipleLongValues(remoteValues)
    }

    fun getDefaultRemoteValue() {
        val defaultValues = mapOf(
            "remote_splash_ads" to 2L,
            "remote_banner_splash" to 1L,
            "remote_native_language" to 1L,
            "remote_inter_language" to 0L,
            "remote_native_intro" to 1L,
            "remote_inter_intro" to 0L,
            "remote_inter_home" to 2L,
            "remote_inter_back_to_home" to 2L,
            "remote_native_home" to 1L,
            "remote_banner_home" to 1L,
            "remote_native_photo_selector" to 1L,
            "remote_banner_photo_editor" to 1L,
            "remote_inter_save" to 1L,
            "remote_native_theme_selector" to 1L,
            "remote_native_saved" to 1L,
            "remote_native_setting" to 1L,
            "remote_native_after_inter" to 1L,
            "remote_on_resume" to 1L,
            "remote_time_show_inter" to 20000L
        )

        for ((key, value) in defaultValues) {
            if (!SharePrefManager.contains(key)) {
                SharePrefManager.put(key, value)
            }
        }
    }

    interface InitListener {
        fun onComplete()
        fun onFailure()
    }
}