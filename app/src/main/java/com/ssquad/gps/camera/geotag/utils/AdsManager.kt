package com.ssquad.gps.camera.geotag.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobBannerCollapsibleModel
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.models.AdmobNativeModel
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MainActivity

object AdsManager {
    const val AOA_SPLASH = "ca-app-pub-8475252859305547/1876534897"
    const val INTER_SPLASH = "ca-app-pub-8475252859305547/2076904938"
    const val BANNER_SPLASH = "ca-app-pub-8475252859305547/5281668680"
    const val NATIVE_LANGUAGE = "ca-app-pub-8475252859305547/9563453222"
    const val NATIVE_LANGUAGE_2 = "ca-app-pub-8475252859305547/5707968699"
    const val INTER_LANGUAGE = "ca-app-pub-8475252859305547/1997029798"
    const val NATIVE_INTRO = "ca-app-pub-8475252859305547/9763823260"
    const val INTER_INTRO = "ca-app-pub-8475252859305547/5457762658"
    const val INTER_HOME = "ca-app-pub-8475252859305547/8250371554"
    const val INTER_BACK_TO_HOME = "ca-app-pub-8475252859305547/2831599313"
    const val NATIVE_HOME = "ca-app-pub-8475252859305547/9823533018"
    const val BANNER_HOME = "ca-app-pub-8475252859305547/8450741597"
    const val NATIVE_PHOTO_SELECTOR = "ca-app-pub-8475252859305547/1518517645"
    const val BANNER_PHOTO_EDITOR ="ca-app-pub-8475252859305547/1366458490"
    const val INTER_SAVE = "ca-app-pub-8475252859305547/7197369670"
    const val NATIVE_THEME_SELECTOR = "ca-app-pub-8475252859305547/8917645450"
    const val NATIVE_SAVED = "ca-app-pub-8475252859305547/4571206333"
    const val NATIVE_SETTING = "ca-app-pub-8475252859305547/5624208212"
    const val NATIVE_FULL_SCREEN_AFTER_INTER = "ca-app-pub-8475252859305547/1885333245"
    const val ON_RESUME = "ca-app-pub-8475252859305547/3081805357"

    //interstitial
    val adSplashModel = AdmobInterModel(INTER_SPLASH)
    val admobInterLanguage = AdmobInterModel(INTER_LANGUAGE)
    val admobInterIntro = AdmobInterModel(INTER_INTRO)
    val admobInterBackHome = AdmobInterModel(INTER_BACK_TO_HOME)
    val admobInterHome = AdmobInterModel(INTER_HOME)
    val admobInterSave= AdmobInterModel(INTER_SAVE)

    //banner
    val admobBannerHome = AdmobBannerCollapsibleModel(BANNER_HOME)
    val admobBannerPhotoEditor = AdmobBannerCollapsibleModel(BANNER_PHOTO_EDITOR)

    //native
    val admobNativeLanguage = AdmobNativeModel(NATIVE_LANGUAGE)
    val admobNativeLanguage2 = AdmobNativeModel(NATIVE_LANGUAGE_2)
    val admobNativeIntro = AdmobNativeModel(NATIVE_INTRO)
    val admobNativeHome = AdmobNativeModel(NATIVE_HOME)
    val admobNativePhotoSelector = AdmobNativeModel(NATIVE_PHOTO_SELECTOR)
    val admobNativeThemeSelector = AdmobNativeModel(NATIVE_THEME_SELECTOR)
    val admobNativeSaved = AdmobNativeModel(NATIVE_SAVED)
    val admobNativeSetting = AdmobNativeModel(NATIVE_SETTING)
    val admobNativeFullScreenAfterInter = AdmobNativeModel(NATIVE_FULL_SCREEN_AFTER_INTER)


    var lastInterShown = 0L
    private val isShowInter: Boolean
        get() = System.currentTimeMillis() - lastInterShown >= RemoteConfig.remoteTimeShowInter

    private var countInterHome = 0
    fun isShowInterHome(): Boolean {
        Log.d("countInterHome", "countInterHome: ${RemoteConfig.remoteInterHome}")
        if (RemoteConfig.remoteInterHome < 1) return false
        countInterHome++
        return isShowInter && countInterHome % RemoteConfig.remoteInterHome == 0L
    }

    private var countInterBackToHome = 0
    fun isShowInterBackToHome(): Boolean {
        Log.d("countBackToHome", "countBackToHome: ${RemoteConfig.remoteInterBackToHome}")
        if (RemoteConfig.remoteInterBackToHome < 1) return false

        countInterBackToHome++
        Log.d("countInterBackToHome", "countInterBackToHome: $countInterBackToHome")
        return isShowInter && countInterBackToHome % RemoteConfig.remoteInterBackToHome == 0L
    }


    private var countInterSave = 0
    fun isShowInterSaveImage(): Boolean {
        if (RemoteConfig.remoteInterSave < 1) return false

        countInterSave++
        return isShowInter && countInterSave % RemoteConfig.remoteInterSave == 0L
    }

    private var isRatingShown = false
    fun isShowRating(): Boolean {
        if (!isRatingShown) {
            isRatingShown = true
            return true
        }
        return false
    }

    fun isShowNativeFullScreen(): Boolean {
        return RemoteConfig.remoteNativeAfterInter == 1L && !AdmobLib.getCheckTestDevice()
    }

    fun reset() {
        isRatingShown = false
        countInterHome = 0
        countInterBackToHome = 0
    }

   /* fun handleBackToHome(activity: AppCompatActivity, view: View?, navigateToMain: () -> Unit) {
        if (isShowInterBackToHome()) {
            view?.visible()
            activity.loadAndShowInterWithNativeAfter(
                interModel = admobInterBackHome,
                vShowInterAds = view,
            ) {
                view?.gone()
                lastInterShown = System.currentTimeMillis()
                navigateToMain()
            }
        } else {
            view?.gone()
            navigateToMain()
        }
    }*/

    fun navigateToMainActivity(activity: Activity, shouldFinish: Boolean = true) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
        if (shouldFinish) {
            activity.finish()
        }
    }

   /* fun handleBackPress(activity: AppCompatActivity, view: View?, shouldFinish: Boolean = true) {
        handleBackToHome(activity, view) {
            navigateToMainActivity(activity, shouldFinish)
        }
    }*/


}