package com.example.baseproject.presentation.hometab.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.R
import com.example.baseproject.adapters.LanguageAdapter
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityLanguageBinding
import com.example.baseproject.utils.Common
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.visible
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.utils.GoogleENative
import java.util.Locale

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {

    private var adapter: LanguageAdapter? = null
    private var isFromHome = true

    override fun onStop() {
        super.onStop()
       // binding.vShowInterAds.gone()
    }

    override fun initData() {
        isFromHome = intent.getBooleanExtra(Constants.LANGUAGE_EXTRA, true)
        Log.d("LanguageActivity", "isFromHome $isFromHome") //Log
    }

    override fun initView() {
        initLanguage()
       // loadIntroAds()
    }

    @SuppressLint("SetTextI18n")
    override fun initActionView() {
        Log.d("LanguageActivity", "isFromHome $isFromHome")
        if (!isFromHome) {
            binding.tvTitle.text = getString(R.string.languages)
            binding.ivBack.gone()
        } else {
            binding.ivBack.visible()
            binding.ivBack.setOnClickListener {
                finish()
            }
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })
        }
        binding.ivDone.setOnClickListener {
            applyLanguage()
        }

    }

    override fun onResume() {
        super.onResume()
        //showNativeAd(AdsManager.admobNativeLanguage)
    }

   /* private fun showNativeAd(admobNativeLanguage: AdmobNativeModel) {
        if (RemoteConfig.remoteNativeLanguage <= 0) {
            binding.frLgNative.gone()
            return
        }
        if (isFromHome) {
            AdmobLib.loadAndShowNative(
                this,
                admobNativeLanguage,
                binding.frLgNative,
                size = GoogleENative.UNIFIED_MEDIUM,
                R.layout.custom_ads_native_medium_2
            )
        } else {
            AdmobLib.showNative(
                this,
                admobNativeLanguage,
                binding.frLgNative,
                size = GoogleENative.UNIFIED_MEDIUM,
                layout = R.layout.custom_ads_native_medium_2
            )
        }
    }
*/
  /*  private fun loadIntroAds() {
        if (isFromHome) return
        if (RemoteConfig.remoteNativeIntro > 0) {
            AdmobLib.loadNative(this, AdsManager.admobNativeIntro)
        }
        if (RemoteConfig.remoteInterIntro > 0) {
            AdmobLib.loadInterstitial(this, AdsManager.admobInterIntro)
        }
    }*/

   /* private fun showInterAd(navigateTo: () -> Unit) {
        if(isFromHome){
            //show ad từ màn home
            if (RemoteConfig.remoteInterLanguage > 0 ) {
                loadAndShowInterWithNativeAfter(
                    interModel = AdsManager.admobInterLanguage,
                    vShowInterAds = binding.vShowInterAds,
                ) { navigateTo() }
            } else {
                navigateTo()
            }
        }else{
            //show ad tải trc
            AdmobLib.showInterstitial(
                this,
                AdsManager.admobInterLanguage,
                onAdsCloseOrFailed = {
                    navigateTo()
                }, isPreload = false
            )
        }

    }*/

    private fun applyLanguage() {

        adapter?.getSelectedLanguage()?.let { selectedLanguage ->
            Common.languageSelected = selectedLanguage
            val locale = Locale(selectedLanguage.key)
            Log.d("LanguageActivity", "$locale")
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            createConfigurationContext(config)
            navigateToHome()
            /*showInterAd {
                navigateToHome()
            }*/

        } ?: run {
            //Thông báo
            Toast.makeText(this, R.string.please_select_language, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initLanguage() {
        val languageList = Common.getLanguageList()
        val currentLanguage = Common.languageSelected

        adapter = LanguageAdapter(
            this@LanguageActivity,
            languageList
        ) { selectedLanguage ->
          /*  //show ad
            showNativeAd(AdsManager.admobNativeLanguage2)
            Log.d("LanguageActivity", "Đã chọn: ${selectedLanguage.key}")*/
        }

        binding.rcvLanguage.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
            adapter = this@LanguageActivity.adapter
        }

        if (isFromHome) {
            adapter?.setSelectedLanguage(currentLanguage)
        }

    }

    private fun navigateToHome() {
        val intent = if (!isFromHome) {
            Intent(this@LanguageActivity, IntroActivity::class.java)
        } else {
            Intent(this@LanguageActivity, MainActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}