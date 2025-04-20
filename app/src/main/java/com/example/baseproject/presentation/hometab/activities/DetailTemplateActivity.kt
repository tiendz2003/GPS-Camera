package com.example.baseproject.presentation.hometab.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityDetailTemplateBinding
import com.example.baseproject.data.models.TemplateType
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter
import com.example.baseproject.utils.SharePrefManager

class DetailTemplateActivity : BaseActivity<ActivityDetailTemplateBinding>(
    ActivityDetailTemplateBinding::inflate
) {
    private val previewTemplateContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedTemplateId = result.data?.getStringExtra("SELECTED_TEMPLATE_ID") ?: return@registerForActivityResult
            updateSelectedTemplate(selectedTemplateId)
        }
    }

    private val allItemsAdapter by lazy {
        ThemeTemplateAdapter(isFromDetail = true) { selectedTemplate ->
            val themeType = selectedTemplate.type
            val filterList = ThemeTemplateModel.getTemplate().filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
            val intent = PreviewTemplateActivity.getIntent(this, selectedTemplate, filterList, themeType)
            previewTemplateContract.launch(intent)
        }
    }

    companion object {
        const val TEMPLATE_NAME = "TEMPLATE_NAME"

        fun getIntent(context: Context, templateName: TemplateType): Intent {
            return Intent(context, DetailTemplateActivity::class.java).apply {
                putExtra(TEMPLATE_NAME, templateName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        val defaultTemplateId = SharePrefManager.getDefaultTemplate()
        loadTemplatesByType(defaultTemplateId)
    }

    private fun loadTemplatesByType(defaultTemplateId: String?) {
        val templateType: TemplateType? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(TEMPLATE_NAME, TemplateType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(TEMPLATE_NAME) as? TemplateType
        }

        templateType?.let { type ->
            when (type) {
                TemplateType.DAILY -> binding.tvTitle.text = getString(R.string.daily)
                TemplateType.TRAVEL -> binding.tvTitle.text = getString(R.string.travel)
                TemplateType.GPS -> binding.tvTitle.text = getString(R.string.gps)
            }

            val filteredList = ThemeTemplateModel.getTemplate()
                .filter { it.type == type }
                .map { it.copy(isSelected = it.id == defaultTemplateId) }

            Log.d("DetailTemplateActivity", "Danh sách đã lọc: $filteredList")
            allItemsAdapter.submitList(filteredList)
        } ?: Log.e("DetailTemplateActivity", "Không nhận được loại mẫu!")
    }

    override fun initView() {
        setupRecyclerView()
    }

    // Phương thức cập nhật UI dựa trên template đã chọn
    private fun updateSelectedTemplate(selectedTemplateId: String) {
        SharePrefManager.setDefaultTemplate(selectedTemplateId)

        val currentList = allItemsAdapter.currentList.toMutableList()

        currentList.forEach { it.isSelected = false }

        currentList.find { it.id == selectedTemplateId }?.let {
            it.isSelected = true
            Log.d("DetailTemplateActivity", "Selected template: ${it.id}")
        }

        allItemsAdapter.submitList(null) // Force refresh
        allItemsAdapter.submitList(currentList)
    }

    override fun initActionView() {

    }

    private fun setupRecyclerView() {
        binding.rvDetail.apply {
            setHasFixedSize(true)
            adapter = allItemsAdapter
            Log.d("DetailTemplateActivity", "kích thước: ${allItemsAdapter.currentList.size}")
            val gridLayoutManager = GridLayoutManager(this@DetailTemplateActivity, 2)
            layoutManager = gridLayoutManager
            val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val column = position % 2

                    outRect.left = spacing - column * spacing / 2
                    outRect.right = (column + 1) * spacing / 2
                    if (position >= 2) {
                        outRect.top = spacing
                    }
                }
            })
        }
    }
    override fun onResume() {
        super.onResume()
        val defaultTemplateId = SharePrefManager.getDefaultTemplate()
            if (allItemsAdapter.currentList.find { it.isSelected }?.id != defaultTemplateId) {
                loadTemplatesByType(defaultTemplateId)
            }

    }
}