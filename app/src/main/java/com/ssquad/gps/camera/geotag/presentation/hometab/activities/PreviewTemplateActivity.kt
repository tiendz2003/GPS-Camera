package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.TemplateType
import com.ssquad.gps.camera.geotag.data.models.ThemeTemplateModel
import com.ssquad.gps.camera.geotag.presentation.custom.HorizontalSpaceItemDecoration
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.PreviewOptionsAdapter
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityPreviewTemplateBinding
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.dpToPx
import com.ssquad.gps.camera.geotag.utils.loadImageIcon
import com.ssquad.gps.camera.geotag.utils.scrollToCenter

class PreviewTemplateActivity : BaseActivity<ActivityPreviewTemplateBinding>(
    ActivityPreviewTemplateBinding::inflate
) {
    private val detailTemplateAdapter by lazy {
        PreviewOptionsAdapter{ position, theme ->
            updateSelectedPosition(position)
            updatePreview(theme)
            binding.rvThemeOptions.scrollToCenter(position)
        }
    }
    companion object {
        private const val SELECTED_TEMPLATE = "SELECTED_TEMPLATE"
        private const val THEME_LIST = "THEME_LIST"
        private const val THEME_TYPE = "THEME_TYPE"

        fun getIntent(
            context: Context,
            selectedTemplate: ThemeTemplateModel,
            templateList: ArrayList<ThemeTemplateModel>,
            templateType: TemplateType
        ): Intent {
            return Intent(context, PreviewTemplateActivity::class.java).apply {
                putExtra(SELECTED_TEMPLATE, selectedTemplate)
                putParcelableArrayListExtra(THEME_LIST, templateList)
                putExtra(THEME_TYPE, templateType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }
    private fun updateSelectedPosition(position: Int){
        detailTemplateAdapter.updateSelection(position)
    }
    override fun initData() {
        var selectedPosition = 0
        val selectedTemplate =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(SELECTED_TEMPLATE, ThemeTemplateModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(SELECTED_TEMPLATE)
        }
        val themeType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(THEME_TYPE, TemplateType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(THEME_TYPE) as? TemplateType
        }

        val themeList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(THEME_LIST, ThemeTemplateModel::class.java) ?: arrayListOf()
        } else {
            intent.getParcelableArrayListExtra(THEME_LIST) ?: arrayListOf()
        }
        themeType?.let {
            binding.tvTitle.text = when (it) {
                TemplateType.DAILY -> {
                    getString(R.string.daily)
                }
                TemplateType.TRAVEL -> {
                    getString(R.string.travel)
                }
                TemplateType.GPS -> {
                    getString(R.string.gps)
                }
            }
        }
        detailTemplateAdapter.submitList(themeList){
            selectedTemplate?.let {
                selectedPosition = themeList.indexOf(it)
                detailTemplateAdapter.updateSelection(selectedPosition)
                binding.rvThemeOptions.post {
                    binding.rvThemeOptions.scrollToCenter(selectedPosition)
                    updatePreview(it)
                }
            }
        }

    }
    override fun initView() {
        setupView()
    }

    override fun initActionView() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivDone.setOnClickListener {
            val selectedTemplate = detailTemplateAdapter.currentList.find { it.isSelected }
            selectedTemplate?.let {
                SharePrefManager.setDefaultTemplate(it.id)
                val resultIntent = Intent().apply {
                    putExtra("SELECTED_TEMPLATE_ID", it.id)
                }
                setResult(RESULT_OK, resultIntent)
                // This will return to previous activity with the selected template ID
                finish()
            }
        }
    }
    private fun setupView(){
        binding.rvThemeOptions.apply {
            setHasFixedSize(true)
            addItemDecoration(
                HorizontalSpaceItemDecoration(16.dpToPx(this@PreviewTemplateActivity),4.dpToPx(this@PreviewTemplateActivity))
            )
            layoutManager = LinearLayoutManager(this@PreviewTemplateActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = detailTemplateAdapter
        }

    }
    private fun updatePreview(theme: ThemeTemplateModel) {
        binding.ivThemeBackground.loadImageIcon(theme.image)
    }

}