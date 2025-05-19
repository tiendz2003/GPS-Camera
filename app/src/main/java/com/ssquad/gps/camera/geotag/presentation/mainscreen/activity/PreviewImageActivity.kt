package com.ssquad.gps.camera.geotag.presentation.mainscreen.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseActivity
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
import com.mapbox.maps.Style
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.BannerCollapsibleType
import com.snake.squad.adslib.utils.BannerType
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityPreviewBinding
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PhotosViewModel
import com.ssquad.gps.camera.geotag.service.MapboxManager
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.Config
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.ssquad.gps.camera.geotag.utils.flipHorizontally
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.loadAndShowInterWithNativeAfter
import com.ssquad.gps.camera.geotag.utils.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

class PreviewImageActivity : BaseActivity<ActivityPreviewBinding>(ActivityPreviewBinding::inflate) {

    private val previewViewModel: PreviewShareViewModel by viewModel()

    private var templateData: TemplateDataModel? = null
    private var templateId: String? = null
    private var mapSnapshotJob: Job? = null
    private var mapBitmap: Bitmap? = null
    private lateinit var mapboxManager:MapboxManager
    private var isTemplateDataLoaded = false
    private var pendingImagePath: String? = null
    private var pendingIsFromAlbum: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

    }

    override fun initData() {
        initMapbox()
        templateId = SharePrefManager.getDefaultTemplate()
        pendingImagePath = intent.getStringExtra("IMAGE_PATH")
        pendingIsFromAlbum = intent.getBooleanExtra("FROM_ALBUM", false)
        val uri = pendingImagePath?.toUri()
        Log.d("PreviewImageActivity", "uri: $uri")
        uri?.let {
            binding.imagePreview.loadImageIcon(uri)
        }
        templateData = intent.parcelable<TemplateDataModel>("TEMPLATE_DATA")

        if (templateData != null && templateId != null) {
            previewViewModel.setSelectedTemplate(templateId!!)
        }
        if (templateData == null) {
            loadTemplateData()
        } else {
            if (templateId != null) {
                previewViewModel.setSelectedTemplate(templateId!!)
            }
            processImage()
        }
    }
    private fun initMapbox(){
        mapboxManager = MapboxManager(this)
    }
    private fun loadTemplateData() {
        lifecycleScope.launch {
            previewViewModel.getCacheDataTemplate()
            previewViewModel.previewUiState
                .map { it.cacheDataTemplate }
                .filterNotNull()
                .firstOrNull()
                ?.let { data ->
                    // Đã có dữ liệu template
                    templateData = data
                    isTemplateDataLoaded = true

                    if (templateId != null) {
                        previewViewModel.setSelectedTemplate(templateId!!)
                    }
                    processImage()
                }
        }
    }


    private fun processImage() {
        if (pendingIsFromAlbum) {
            val uri = pendingImagePath?.toUri()
            Log.d("PreviewImageActivity", "uri: $uri")
            uri?.let {
                displayEditImageWithTemplate(templateData, templateId)
            }
        } else {
            val bitmap = BitmapHolder.imageBitmap
            if (bitmap != null) {
                displayImageWithTemplate(bitmap, templateData, templateId)
            } else {
                Toast.makeText(this, getString(R.string.error_receiving_photo), Toast.LENGTH_SHORT)
                    .show()
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
                initInterAd {
                    saveImage()
                }
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
                Log.e("PreviewImageActivity", "Invalid coordinates")
                return
            }
            mapSnapshotJob?.cancel()
            mapboxManager.createSnapshot(
                latitude = lat,
                longitude = lon,
                zoom = 14.0,
                mapStyle = Style.SATELLITE_STREETS,
                onSnapshotReady = { bitmap ->
                    mapSnapshotJob = lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        mapBitmap = bitmap
                        updateTemplateWithMap(template, mapBitmap)
                    }
                },
                onError = { error ->
                    Log.e("CameraActivity", "Error creating snapshot: $error")
                    updateTemplateWithMap(template, null)
                }
            )
        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "Error: ${e.message}")
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
                    if (pendingIsFromAlbum) {
                        showTemplateLoading(previewUiState.cacheDataTemplate == null)
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
    private fun showTemplateLoading(show: Boolean) {
        binding.skeletonTemplateLoading.apply {
            if (show) {
                // Setup shimmer config nếu muốn (set 1 lần)
                maskColor = ContextCompat.getColor(context, R.color.neutralGrey) // màu xám loading
                shimmerColor = ContextCompat.getColor(context, R.color.neutralWhite) // màu shimmer trắng sáng
                shimmerDurationInMillis = 1000L // thời gian chạy 1 vòng shimmer (ms)
                showShimmer = true // bật shimmer
                showSkeleton() // bắt đầu loading
                visibility = View.VISIBLE
            } else {
                showOriginal() // ẩn shimmer
                visibility = View.GONE
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
        templateData: TemplateDataModel?,
        templateId: String?
    ) {
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

    private fun updateTemplate(templateId: String) {
        this.templateId = templateId
        binding.templateContainer.removeAllViews()
        templateData?.let { data ->
            if (Config.isGPSTemplate(templateId) && mapBitmap != null) {
                binding.templateContainer.addTemplate(
                    this,
                    templateId,
                    data,
                    previewViewModel.previewUiState.value.templateState,
                    mapBitmap
                )
            } else {
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
                previewViewModel.saveImageToGallery(this@PreviewImageActivity, combineBitmap, templateData?.location)
                val intent = Intent("com.ssquad.gps.camera.NEW_PHOTO_CAPTURED")
                sendBroadcast(intent)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PreviewImageActivity,
                        getString(R.string.image_saving_failed_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initBannerHomeAd() {
        Log.d("PreviewImageActivity", "initBannerHomeAd: ${RemoteConfig.remoteBannerPhotoEditor}")
        when (RemoteConfig.remoteBannerPhotoEditor) {
            0L -> {
                binding.viewLine.gone()
                binding.frBanner.gone()
            }
            1L -> {
                binding.frBanner.visible()
                binding.viewLine.visible()
                AdmobLib.loadAndShowBanner(
                    this,
                    AdsManager.BANNER_HOME,
                    binding.frBanner,
                    binding.viewLine,
                    isShowOnTestDevice = true
                )
            }
            2L -> {
                binding.frBanner.visible()
                binding.viewLine.visible()
                AdmobLib.loadAndShowBannerCollapsible(
                    this,
                    AdsManager.admobBannerPhotoEditor,
                    binding.frBanner,
                    binding.viewLine,
                    BannerCollapsibleType.BOTTOM,
                    isShowOnTestDevice = true
                )
            }
        }
    }
    private fun initInterAd(saveImage:()-> Unit){
        if(AdsManager.isShowInterSaveImage()){
            binding.vShowInterAds.visible()
            loadAndShowInterWithNativeAfter(
                interModel = AdsManager.admobInterSave,
                vShowInterAds = binding.vShowInterAds,
            ) { saveImage() }
        }else{
            saveImage()
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
                0 -> getString(R.string.template)
                else -> getString(R.string.custom)
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        initBannerHomeAd()
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
        mapboxManager.destroySnapshotter()
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