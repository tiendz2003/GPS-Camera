package com.ssquad.gps.camera.geotag.fragments

import com.bumptech.glide.Glide
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseFragment
import com.ssquad.gps.camera.geotag.databinding.FragmentIntroBinding

class IntroFragment : BaseFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate) {

    private val ARG_OBJECT = "position"

    override fun initData() {}

    override fun initView() {
        if (arguments != null) {
            fragmentPosition(requireArguments().getInt(ARG_OBJECT))
        }
    }

    override fun initActionView() {}

    private fun fragmentPosition(position: Int) {
        when (position) {
            0 -> {
                Glide.with(this).load(R.drawable.img_intro_1).into(binding.ivIntro)
                binding.tvTitle.text = getString(R.string.title_intro_1)
                binding.tvMessage.text = getString(R.string.message_intro_1)
            }
            1 -> {
                Glide.with(this).load(R.drawable.img_intro_2).into(binding.ivIntro)
                binding.tvTitle.text = getString(R.string.title_intro_2)
                binding.tvMessage.text = getString(R.string.message_intro_2)
            }
            else -> {
                Glide.with(this).load(R.drawable.img_intro_3).into(binding.ivIntro)
                binding.tvTitle.text = getString(R.string.title_intro_3)
                binding.tvMessage.text = getString(R.string.message_intro_3)
            }
        }
    }

}