package com.example.baseproject.presentation.hometab.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityTemplatesBinding
import com.example.baseproject.data.models.TemplateType
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter
import com.example.baseproject.utils.setupHorizontal

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


    val listTheme = ThemeTemplateModel.getTemplate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        Log.d("TemplatesActivity", "initData: $listTheme")
        val listDaily = listTheme.filter { it.type == TemplateType.DAILY }
        val listTravel = listTheme.filter { it.type == TemplateType.TRAVEL }
        val listGps = listTheme.filter { it.type == TemplateType.GPS }
        dailyAdapter.submitList(listDaily)
        travelAdapter.submitList(listTravel)
        gpsAdapter.submitList(listGps)
    }

    override fun initView() {
        setupRecyclerView()
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
    fun navToPreview(selectedTemplate: ThemeTemplateModel) {
        val themeType = selectedTemplate.type
        val filterList = ThemeTemplateModel.getTemplate().filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
        val intent = PreviewTemplateActivity.getIntent(this, selectedTemplate, filterList,themeType)
        startActivity(intent)
    }
}