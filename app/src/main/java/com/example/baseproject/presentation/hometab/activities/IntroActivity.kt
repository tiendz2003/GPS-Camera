package com.example.baseproject.presentation.hometab.activities

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.example.baseproject.adapters.IntroViewPagerAdapter
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private var mAdapter: IntroViewPagerAdapter? = null

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

        val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            }
        binding.vpIntro.registerOnPageChangeCallback(onPageChangeCallback)
        binding.dotIndicator.attachTo(binding.vpIntro)

    }

    override fun initActionView() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun goToHome() {
        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
        finish()
    }
}