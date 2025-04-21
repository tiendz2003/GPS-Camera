package com.example.baseproject.presentation.hometab.activities

import android.util.Log
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityTemplatesBinding
import com.example.baseproject.data.models.TemplateType
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.setupHorizontal
import com.example.baseproject.utils.updateSelection

class TemplatesActivity :
    BaseActivity<ActivityTemplatesBinding>(ActivityTemplatesBinding::inflate) {
    private val dailyAdapter by lazy {
        ThemeTemplateAdapter {selectedTemplate ->
            Log.d("TemplatesActivity", "initData: ${selectedTemplate.id}")
            navToPreview(selectedTemplate)
        }
    }
    private val travelAdapter by lazy {
        ThemeTemplateAdapter {selectedTemplate->
            navToPreview(selectedTemplate)
        }
    }
    private val gpsAdapter by lazy {
        ThemeTemplateAdapter {selectedTemplate->
            navToPreview(selectedTemplate)
        }
    }


    private val listTheme = ThemeTemplateModel.getTemplate()


    override fun initData() {
        Log.d("TemplatesActivity", "initData: $listTheme")
        val listDaily = listTheme.filter { it.type == TemplateType.DAILY }
        val listTravel = listTheme.filter { it.type == TemplateType.TRAVEL }
        val listGps = listTheme.filter { it.type == TemplateType.GPS }
        dailyAdapter.submitList(listDaily)
        travelAdapter.submitList(listTravel)
        gpsAdapter.submitList(listGps)
        updateTemplateSelection()
    }
    private fun updateTemplateSelection(){
        gpsAdapter.updateSelection(SharePrefManager.getDefaultTemplate())
        dailyAdapter.updateSelection(SharePrefManager.getDefaultTemplate())
        travelAdapter.updateSelection(SharePrefManager.getDefaultTemplate())
    }
    override fun initView() {
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        updateTemplateSelection()
    }
    override fun initActionView() {
        binding.tvViewAllDaily.setOnClickListener {
            startActivity(DetailTemplateActivity.getIntent(this, TemplateType.DAILY))
        }
        binding.tvViewAllTravel.setOnClickListener {
            startActivity(DetailTemplateActivity.getIntent(this, TemplateType.TRAVEL))
        }
        binding.tvViewAllGPS.setOnClickListener {
            startActivity(DetailTemplateActivity.getIntent(this, TemplateType.GPS))
        }
    }

    private fun setupRecyclerView() {
        binding.rvDaily.apply {
            Log.d("TemplatesActivity", "List size: ${listTheme.size}")
            setupHorizontal(dailyAdapter)
        }
        binding.rvTravel.apply {
            setupHorizontal(travelAdapter)
        }
        binding.rvGPS.apply {
            setupHorizontal(gpsAdapter)
        }
    }
    private fun navToPreview(selectedTemplate: ThemeTemplateModel) {
        val themeType = selectedTemplate.type
        val filterList = ThemeTemplateModel.getTemplate().filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
        val intent = PreviewTemplateActivity.getIntent(this, selectedTemplate, filterList,themeType)
        startActivity(intent)
    }
}