package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.content.Intent
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.adapters.IntroViewPagerAdapter
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityIntroBinding
import com.ssquad.gps.camera.geotag.utils.Common
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.invisible
import com.ssquad.gps.camera.geotag.utils.visible

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private var mAdapter: IntroViewPagerAdapter? = null


    override fun onStop() {
        super.onStop()
        binding.vShowInterAds.gone()
    }
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.vpIntro.currentItem in 1..2) {
                binding.vpIntro.currentItem -= 1
            } else {
                finish()
            }
        }
    }

    override fun initData() {
        mAdapter = IntroViewPagerAdapter(this)
        Log.d("IntroActivity", "${Common.languageSelected}")
    }

    override fun initView() {
        binding.vpIntro.adapter = mAdapter

        binding.btnNext.setOnClickListener {
            when (binding.vpIntro.currentItem) {
                0 -> {
                    binding.vpIntro.currentItem = 1
                }

                1 -> {
                    binding.vpIntro.currentItem = 2
                }

                2 -> {
                    goToHome()
                }
            }
        }

        binding.btnNext2.setOnClickListener {
            when (binding.vpIntro.currentItem) {
                0 -> {
                    binding.vpIntro.currentItem = 1
                }

                1 -> {
                    binding.vpIntro.currentItem = 2
                }

                2 -> {
                    goToHome()
                }
            }
        }

        val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> {
                            binding.btnNext2.text = getString(R.string.next)
                            binding.btnNext.text = getString(R.string.next)
                            updateView()
                        }

                        1 -> {
                            binding.btnNext2.text = getString(R.string.next)
                            binding.btnNext.text = getString(R.string.next)
                           // showNativeAds()
                        }

                        2 -> {
                            updateView()
                            binding.btnNext2.text = getString(R.string.start)
                            binding.btnNext.text = getString(R.string.start)
                        }
                    }
                }
            }
        binding.vpIntro.registerOnPageChangeCallback(onPageChangeCallback)
        binding.dotIndicator.attachTo(binding.vpIntro)
        binding.vpIntro.isUserInputEnabled = false

    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun goToHome() {
        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
        finish()
    }

    private fun updateView(isShowAds: Boolean = false) {
        binding.frNative.isVisible = isShowAds
        if (isShowAds) {
            binding.btnNext.visible()
            binding.btnNext2.gone()
            binding.dotIndicator.updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToEnd = ConstraintLayout.LayoutParams.UNSET
            }
        } else {
            binding.btnNext.invisible()
            binding.btnNext2.visible()
            binding.dotIndicator.updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
    }

 /*   private fun showNativeAds() {
        Log.d("Intro", "${RemoteConfig.remoteNativeIntro}")
        if (RemoteConfig.remoteNativeIntro == 0L) return
        updateView(true)
        AdmobLib.showNative(
            this,
            AdsManager.admobNativeIntro,
            binding.frNative,
            size = GoogleENative.UNIFIED_MEDIUM,
            layout = R.layout.custom_ads_native_medium_2,
            onAdsShowFail = {
                Log.e("Intro", "Quảng cáo native không hiển thị được")
                updateView()
            })

    }

    private fun showInter(navAction: () -> Unit) {
        if (RemoteConfig.remoteInterIntro != 1L) {
            navAction()
        } else {
            binding.vShowInterAds.visible()
            AdmobLib.showInterstitial(
                this,
                AdsManager.admobInterIntro,
                onAdsCloseOrFailed = {
                    navAction()
                },
                isPreload = false
            )
        }
    }*/
}
