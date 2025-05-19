package com.ssquad.gps.camera.geotag.presentation.settingtab.activity


import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseActivity
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivitySetupTempBinding
import com.ssquad.gps.camera.geotag.utils.SharePrefManager

class SetupTempActivity : BaseActivity<ActivitySetupTempBinding>(ActivitySetupTempBinding::inflate) {
    var type = SharePrefManager.getTemperature()
    override fun initData() {

    }

    override fun initView() {
        setChecked(type)
    }

    override fun initActionView() {
        binding.btnBack.setOnClickListener { finish() }
        binding.ctCelsius.setOnClickListener {
            type = false
            setChecked(false)
        }

        binding.ctFahrenheit.setOnClickListener {
            type = true
            setChecked(true)
        }
        binding.btnCheck.setOnClickListener {
            SharePrefManager.setTemperature(type)
            finish()
        }
    }

    private fun setChecked(type: Boolean) {
        if (type) {
            binding.cbCelsius.setImageResource(R.drawable.ic_unchecked_language)
            binding.cbFahrenheit.setImageResource(R.drawable.ic_selected)
        } else {
            binding.cbCelsius.setImageResource(R.drawable.ic_selected)
            binding.cbFahrenheit.setImageResource(R.drawable.ic_unchecked_language)
        }
    }
}