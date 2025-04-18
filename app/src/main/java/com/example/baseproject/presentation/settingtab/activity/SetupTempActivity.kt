package com.example.baseproject.presentation.settingtab.activity


import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivitySetupTempBinding
import com.example.baseproject.utils.SharePrefManager

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