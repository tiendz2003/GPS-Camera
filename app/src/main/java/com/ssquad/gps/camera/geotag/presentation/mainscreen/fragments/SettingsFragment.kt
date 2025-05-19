package com.ssquad.gps.camera.geotag.presentation.mainscreen.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.ssquad.gps.camera.geotag.BuildConfig
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseFragment
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.LanguageActivity
import com.ssquad.gps.camera.geotag.databinding.FragmentSettingsBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.CustomDateTimeActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.DateTimeFormatActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapTypeActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.PolicyActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.SetupTempActivity
import com.ssquad.gps.camera.geotag.utils.Constants
import androidx.core.net.toUri
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MediaSavedActivity
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.RequestPermissionActivity
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.visible

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    private val reqNavigate = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }
    override fun initData() {

    }

    override fun initView() {

    }

    override fun initActionView() {
        with(binding){
            llMapSetting.setOnClickListener {
                if (PermissionManager.checkLocationPermissions(requireContext())) {
                    reqNavigate.launch(Intent(requireContext(), MapSettingActivity::class.java))
                } else {
                    Intent(requireContext(), RequestPermissionActivity::class.java).apply {
                        putExtra(Constants.INTENT_REQUEST_SINGLE_PERMISSION, RequestPermissionActivity.TYPE_LOCATION)
                        reqNavigate.launch(this)
                    }
                }
            }
            llMapType.setOnClickListener {
                startActivity(Intent(requireContext(), MapTypeActivity::class.java))
            }
            llDatetimeType.setOnClickListener {
                startActivity(Intent(requireContext(), CustomDateTimeActivity::class.java))
            }
            llDatetimeFormat.setOnClickListener {
                startActivity(Intent(requireContext(), DateTimeFormatActivity::class.java))
            }
            llTempUnit.setOnClickListener {
                startActivity(Intent(requireContext(), SetupTempActivity::class.java))
            }
            llLgSetting.setOnClickListener {
                val intent = Intent(requireContext(), LanguageActivity::class.java).apply {
                    putExtra(Constants.LANGUAGE_EXTRA, true)
                }
                startActivity(intent)
            }
            llPrivacy.setOnClickListener {
                reqNavigate.launch(Intent(requireContext(), PolicyActivity::class.java))
            }
            llShare.setOnClickListener {
                shareApp()
            }
            llRating.setOnClickListener {
                openAppRating(requireContext())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initNativeAd()
    }
    fun initNativeAd() {
        val settingKey = RemoteConfig.remoteNativeSetting
        if (settingKey == 0L) {
            binding.frNative.gone()
            return
        }
        binding.frNative.visible()
        Log.d("SetingFragment", "initNativeAd: $settingKey")
        if (AdsManager.admobNativeSetting.nativeAd.value != null) {
            AdmobLib.showNative(
                requireActivity(),
                AdsManager.admobNativeSetting,
                binding.frNative,
                GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                R.layout.custom_ads_native_small
            )
        } else {
            AdmobLib.loadAndShowNative(
                requireActivity(),
                AdsManager.admobNativeSetting,
                binding.frNative,
                GoogleENative.UNIFIED_SMALL_LIKE_BANNER,
                R.layout.custom_ads_native_small,
                isShowOnTestDevice = true
            )
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        val shareMessage =
            "${getString(R.string.app_name)} \n https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        reqNavigate.launch(Intent.createChooser(shareIntent, getString(R.string.share_to)))
    }
    private fun openAppRating(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("", e.toString())
            val intent = Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri())
            context.startActivity(intent)
        }
    }
}