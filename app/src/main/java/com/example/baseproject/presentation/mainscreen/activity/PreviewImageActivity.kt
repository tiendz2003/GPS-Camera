package com.example.baseproject.presentation.mainscreen.activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityPreviewBinding
import com.example.baseproject.utils.BitmapHolder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.presentation.mainscreen.fragment.CustomOptionsFragment
import com.example.baseproject.presentation.mainscreen.fragment.PreviewOptionsFragment
import com.example.baseproject.presentation.viewmodel.PreviewViewModel
import com.example.baseproject.utils.addTemplate
import com.example.baseproject.utils.parcelable
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class PreviewImageActivity : BaseActivity<ActivityPreviewBinding>(ActivityPreviewBinding::inflate) {

    private val previewViewModel: PreviewViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        val templateData = intent.parcelable<TemplateDataModel>("TEMPLATE_DATA")
        val templateId = intent.getStringExtra("TEMPLATE_ID")
            val bitmap = BitmapHolder.bitmap
            if (bitmap != null) {
                displayImageWithTemplate(bitmap, templateData, templateId)
            } else {
                Toast.makeText(this, "Không nhận được ảnh", Toast.LENGTH_SHORT).show()
                finish()
            }

    }

    override fun initView() {
        setupViewPager()
        observeViewModel()
    }

    override fun initActionView() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }
            btnSave.setOnClickListener {
                saveImage()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                previewViewModel.previewUiState.collect { previewUiState ->
                    previewUiState.savedImageUri?.let {
                        Toast.makeText(
                            this@PreviewImageActivity,
                            "Lưu ảnh thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    if (previewUiState.isSaving) {
                        Toast.makeText(this@PreviewImageActivity, "Đang lưu ảnh", Toast.LENGTH_SHORT)
                            .show()
                    }
                    previewUiState.error?.let {
                        Toast.makeText(
                            this@PreviewImageActivity,
                            "Lưu ảnh thất bại :${it} ",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun displayImageWithTemplate(
        bitmap: Bitmap,
        templateData: TemplateDataModel?,
        templateId: String?
    ) {
        binding.imagePreview.setImageBitmap(bitmap)
        if (templateData != null && templateId != null) {
            binding.templateContainer.addTemplate(
                this,
                templateId,
                templateData,
            )
        }
    }

    private fun saveImage() {

       // val bitmap = BitmapHolder.bitmap
        lifecycleScope.launch {
            try {
                val combineBitmap = withContext(Dispatchers.Default) {
                    val originBitmap = createBitmap(
                        binding.previewContainer.width,
                        binding.previewContainer.height
                    )
                    withContext(Dispatchers.Main) {
                        val canvas = Canvas(originBitmap)
                        binding.previewContainer.draw(canvas)
                    }
                    originBitmap
                }
                previewViewModel.saveImageToGallery(this@PreviewImageActivity, combineBitmap)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PreviewImageActivity,
                        "Lưu ảnh thất bại: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun resizeBitmap(bitmap:Bitmap): Bitmap {
       val resizeBitmap = bitmap.scale(binding.previewContainer.width, binding.previewContainer.height)
        return resizeBitmap
    }
    private fun setupViewPager() {
        val pagerAdapter = PreviewPagerAdapter(this)
        binding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Preview"
                else -> "Custom"
            }
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        BitmapHolder.bitmap = null // xoa' reset sau moi lan destroy
    }

    inner class PreviewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PreviewOptionsFragment()
                else -> CustomOptionsFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }

    }
}