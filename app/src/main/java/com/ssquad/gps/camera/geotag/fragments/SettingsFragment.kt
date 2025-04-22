package com.ssquad.gps.camera.geotag.fragments

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.ssquad.gps.camera.geotag.BuildConfig
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseFragment
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.LanguageActivity
import com.ssquad.gps.camera.geotag.databinding.FragmentSettingsBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.CustomDateTimeActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.DateTimeFormatActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapTypeActivity
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.SetupTempActivity
import com.ssquad.gps.camera.geotag.utils.Constants

private const val URL = "param1"
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
                startActivity(Intent(requireContext(), MapSettingActivity::class.java))
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
            llShare.setOnClickListener {
                shareApp()
            }
            llRating.setOnClickListener {

            }
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

}