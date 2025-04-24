package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.utils.BitmapHolder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.presentation.mainscreen.fragment.CustomOptionsFragment
import com.ssquad.gps.camera.geotag.presentation.mainscreen.fragment.PreviewOptionsFragment
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PreviewShareViewModel
import com.ssquad.gps.camera.geotag.utils.addTemplate
import com.ssquad.gps.camera.geotag.utils.parcelable
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.ssquad.gps.camera.geotag.utils.loadImageIcon
import androidx.core.net.toUri
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityPreviewBinding
import com.ssquad.gps.camera.geotag.service.MapManager
import com.ssquad.gps.camera.geotag.utils.Config
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.flipHorizontally
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class PreviewImageActivity : BaseActivity<ActivityPreviewBinding>(ActivityPreviewBinding::inflate) {

    private val previewViewModel: PreviewShareViewModel by viewModel()
    private var templateData: TemplateDataModel? = null
    private var templateId: String? = null
    private lateinit var mapManager: MapManager
    private var mapSnapshotJob: Job? = null
    private var mapBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        mapManager = MapManager(this, lifecycle, binding.mapView)
        mapManager.setOnMapReadyCallback { map ->
            templateData?.let { data ->
                if (isGPSTemplate()) {
                    loadMapImage(data)
                }
            }
        }
    }

    override fun initData() {
        templateData = intent.parcelable<TemplateDataModel>("TEMPLATE_DATA")
        templateId = SharePrefManager.getDefaultTemplate()
        val imgPath = intent.getStringExtra("IMAGE_PATH")
        val isFromAlbum = intent.getBooleanExtra("FROM_ALBUM", false)
        if (templateData != null && templateId != null) {
            previewViewModel.setSelectedTemplate(templateId!!)
        }
        if (isFromAlbum) {
            val uri = imgPath?.toUri()
            Log.d("PreviewImageActivity", "uri: $uri")
            uri?.let {
                displayEditImageWithTemplate(it, templateData, templateId)
            }
        } else {
            val bitmap = BitmapHolder.imageBitmap
            if (bitmap != null) {
                displayImageWithTemplate(bitmap, templateData, templateId)
            } else {
                Toast.makeText(this, "Error receiving photo", Toast.LENGTH_SHORT).show()
                finish()
            }
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

    private fun isGPSTemplate(): Boolean {
        return templateId?.let { Config.isGPSTemplate(templateId) } == true
    }

    private fun loadMapImage(template: TemplateDataModel) {
        if (mapBitmap != null) {
            updateTemplateWithMap(template, mapBitmap)
            return
        }
        try {
            val lat = template.lat?.replace(",", ".")?.toDouble()
            val lon = template.long?.replace(",", ".")?.toDouble()

            if (lat == null || lon == null) {
                Log.e("PreviewImageActivity", "Toạ độ sai")
                return
            }
            mapSnapshotJob?.cancel()
            mapSnapshotJob = lifecycleScope.launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    mapManager.captureMapImage(lat, lon) { bitmap ->
                        Log.d("PreviewImageActivity", "Đã chụp: ${bitmap != null}")
                        mapBitmap = bitmap
                        updateTemplateWithMap(template, mapBitmap)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "Lỗi ${e.message}")
            updateTemplateWithMap(template, null)
        }
    }
    private fun updateTemplateWithMap(template: TemplateDataModel, bitmap: Bitmap?) {
        binding.templateContainer.removeAllViews()
        templateId?.let { id ->
            binding.templateContainer.addTemplate(
                this,
                id,
                template,
                previewViewModel.previewUiState.value.templateState,
                bitmap
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                previewViewModel.previewUiState.collect { previewUiState ->
                    if (previewUiState.selectedTemplateId != null) {
                        updateTemplate(previewUiState.selectedTemplateId)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                previewViewModel.toastEvents.collect { message ->
                    Toast.makeText(this@PreviewImageActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                previewViewModel.navigationEvents.collect {
                    finish()
                }
            }
        }
    }

    private fun displayImageWithTemplate(
        bitmap: Bitmap,
        templateData: TemplateDataModel?,
        templateId: String?
    ) {
        val isFrontCamera = intent.getBooleanExtra("IS_FRONT_CAMERA", false)
        Log.d("PreviewImageActivity", "isFrontCamera: $isFrontCamera")
        val displayBitmap = if (isFrontCamera) {
            bitmap.flipHorizontally()
        } else {
            bitmap
        }
        binding.imagePreview.setImageBitmap(displayBitmap)
        if (templateData != null && templateId != null) {
            this.templateData = templateData
            this.templateId = templateId

            binding.templateContainer.addTemplate(
                this,
                templateId,
                templateData,
            )

            if (Config.isGPSTemplate(templateId)) {
                loadMapImage(templateData)
            }
        }
    }

    private fun displayEditImageWithTemplate(
        uri: Uri,
        templateData: TemplateDataModel?,
        templateId: String?
    ) {
        binding.imagePreview.loadImageIcon(uri)
        if (templateData != null && templateId != null) {
            this.templateData = templateData
            this.templateId = templateId

            binding.templateContainer.addTemplate(
                this,
                templateId,
                templateData,
            )

            if (Config.isGPSTemplate(templateId)) {
                loadMapImage(templateData)
            }
        }
    }

    fun updateTemplate(templateId: String) {
        this.templateId = templateId
        binding.templateContainer.removeAllViews()
        templateData?.let { data ->
           if(Config.isGPSTemplate(templateId) && mapBitmap != null) {
               binding.templateContainer.addTemplate(
                   this,
                   templateId,
                   data,
                   previewViewModel.previewUiState.value.templateState,
                   mapBitmap
               )
           }else{
                binding.templateContainer.addTemplate(
                     this,
                     templateId,
                     data,
                     previewViewModel.previewUiState.value.templateState
                )
           }

            if (Config.isGPSTemplate(templateId) && mapBitmap == null) {
                loadMapImage(data)
            }
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

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val resizeBitmap =
            bitmap.scale(binding.previewContainer.width, binding.previewContainer.height)
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
                0 -> getString(R.string.template)
                else -> getString(R.string.custom)
            }
        }.attach()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapSnapshotJob?.cancel()
        mapBitmap = null
        BitmapHolder.imageBitmap = null// xoa' reset sau moi lan destroy
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