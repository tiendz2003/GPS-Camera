package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.TemplateType
import com.ssquad.gps.camera.geotag.data.models.ThemeTemplateModel
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.ThemeTemplateAdapter
import com.ssquad.gps.camera.geotag.databinding.ActivityTemplatesBinding
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.setupHorizontal
import com.ssquad.gps.camera.geotag.utils.updateSelection

class TemplatesActivity :
    BaseActivity<ActivityTemplatesBinding>(ActivityTemplatesBinding::inflate) {
    private val previewActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedTemplateId = result.data?.getStringExtra("SELECTED_TEMPLATE_ID")
            if (selectedTemplateId != null) {
                val resultIntent = Intent().apply {
                    putExtra("SELECTED_TEMPLATE_ID", selectedTemplateId)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
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
            previewActivityLauncher.launch(DetailTemplateActivity.Companion.getIntent(this, TemplateType.DAILY))
        }
        binding.tvViewAllTravel.setOnClickListener {
            previewActivityLauncher.launch(DetailTemplateActivity.Companion.getIntent(this, TemplateType.TRAVEL))
        }
        binding.tvViewAllGPS.setOnClickListener {
            previewActivityLauncher.launch(DetailTemplateActivity.Companion.getIntent(this, TemplateType.GPS))
        }
        binding.ivBack.setOnClickListener {
            finish()
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
        listTheme.forEach { it.isSelected = it.id == selectedTemplate.id }
        val themeType = selectedTemplate.type
        val filterList = listTheme.filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
        val intent = PreviewTemplateActivity.Companion.getIntent(this, selectedTemplate, filterList, themeType)
        // Use registerForActivityResult to handle the result
        previewActivityLauncher.launch(intent)
    }
}