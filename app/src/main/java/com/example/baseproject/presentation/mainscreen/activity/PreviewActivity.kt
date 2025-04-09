package com.example.baseproject.presentation.mainscreen.activity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityPreviewBinding
import com.example.baseproject.utils.BitmapHolder
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.presentation.hometab.adapter.PreviewOptionsAdapter
import com.example.baseproject.presentation.mainscreen.fragment.CustomOptionsFragment
import com.example.baseproject.presentation.mainscreen.fragment.PreviewOptionsFragment
import com.example.baseproject.presentation.viewmodel.PreviewViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreviewActivity : BaseActivity<ActivityPreviewBinding>(ActivityPreviewBinding::inflate) {
    private var isImage = true
    private var videoUri: Uri? = null
    private var previewOptionsAdapter: PreviewOptionsAdapter? = null
    private val previewViewModel: PreviewViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        isImage = intent.getBooleanExtra("IS_IMAGE", true)
        if(isImage){
            val bitmap = BitmapHolder.bitmap
            if(bitmap != null){
                displayImage(bitmap)
            }else{
                Toast.makeText(this, "Không nhận được ảnh", Toast.LENGTH_SHORT).show()
                finish()
            }
        }else{
            val videoUriString = intent.getStringExtra("VIDEO_URI")
            if(videoUri != null){
                videoUri = videoUriString?.toUri()
                //previewvideo
            }else{

            }
        }

    }

    override fun initView() {
        setupViewPager()
        observeViewModel()
    }

    override fun initActionView() {
        with(binding){
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
                previewViewModel.previewUiState.collect {previewUiState->
                    previewUiState.savedImageUri?.let {
                        Toast.makeText(this@PreviewActivity, "Lưu ảnh thành công", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    if(previewUiState.isSaving) {
                        Toast.makeText(this@PreviewActivity, "Đang lưu ảnh", Toast.LENGTH_SHORT).show()
                    }
                    previewUiState.error?.let {
                        Toast.makeText(this@PreviewActivity, "Lưu ảnh thất bại :${it} ", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
    private fun displayImage(bitmap: Bitmap) {
        binding.imagePreview.setImageBitmap(bitmap)

    }
    private fun saveImage() {
        val bitmap = BitmapHolder.bitmap
        if (bitmap != null) {
            previewViewModel.saveImageToGallery(this, bitmap)
        }
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
    inner class PreviewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> PreviewOptionsFragment()
                else -> CustomOptionsFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }

    }
}