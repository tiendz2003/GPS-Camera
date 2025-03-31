package com.example.baseproject.presentation.hometab.activities

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.adapters.LanguageAdapter
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityLanguageBinding
import com.example.baseproject.utils.Common
import com.example.baseproject.utils.Constants
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.visible

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {

    private var adapter: LanguageAdapter? = null
    private var isFromHome = true

    override fun initData() {
        isFromHome = intent.getBooleanExtra(Constants.LANGUAGE_EXTRA, true)
    }

    override fun initView() {
        initLanguage()
    }

    override fun initActionView() {
        if (!isFromHome) {
            binding.ivBack.gone()
        } else {
            binding.ivBack.visible()
            binding.ivBack.setOnClickListener {
                finish()
            }
        }

        binding.ivDone.setOnClickListener {
            applyLanguage()
        }
    }

    private fun applyLanguage() {
        adapter?.let { Common.setSelectedLanguage(it.getSelectedPositionLanguage()) }
        if (!isFromHome) {
            val intent = Intent(this@LanguageActivity, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            val intent = Intent(this@LanguageActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun initLanguage() {
        val languageList = Common.getLanguageList()
        adapter = LanguageAdapter(
            this@LanguageActivity,
            languageList
        )
        binding.rcvLanguage.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
        }
        binding.rcvLanguage.adapter = adapter
        adapter?.setSelectedPositionLanguage(Common.getSelectedLanguage())

    }

}