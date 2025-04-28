package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.SortOption
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.PhotoAdapter
import com.ssquad.gps.camera.geotag.presentation.hometab.dialog.SortBottomSheet
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.PreviewSavedActivity
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityImagesSavedBinding
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.CameraActivity
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.RequestPermissionActivity
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PhotosViewModel
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.Constants
import com.ssquad.gps.camera.geotag.utils.GridSpacingItemDecoration
import com.ssquad.gps.camera.geotag.utils.PermissionManager
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.Resource
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MediaSavedActivity :
    BaseActivity<ActivityImagesSavedBinding>(ActivityImagesSavedBinding::inflate) {
    companion object {
        const val REQUEST_CODE_PREVIEW = 1001
        const val IS_VIDEO = "IS_VIDEO"
        fun getIntent(context: Context, isVideo: Boolean): Intent {
            return Intent(context, MediaSavedActivity::class.java).apply {
                putExtra(IS_VIDEO, isVideo)
            }
        }
    }

    private var photoAdapter: PhotoAdapter? = null
    private val photosViewModel: PhotosViewModel by viewModel()
    private lateinit var bottomSheet: SortBottomSheet
    private var isVideo: Boolean = false
    private var currentSortOption = SortOption.DATE_ADDED


    override fun initData() {

        isVideo = intent.getBooleanExtra(IS_VIDEO, false)
        loadMediaData()
    }

    override fun initView() {
        setupRecycleView()
        observeViewModel()
    }

    override fun initActionView() {
        with(binding) {
            btnArrange.setOnClickListener {
                bottomSheet = SortBottomSheet(currentSortOption) { selectedOption ->
                    currentSortOption = selectedOption
                    photosViewModel.sortPhotos(selectedOption)
                }
                bottomSheet.show(supportFragmentManager, "SortBottomSheet")
            }
            btnBack.setOnClickListener {
                finish()
            }
            btnTakePhoto.setOnClickListener {
                if (PermissionManager.checkPermissionsGranted(
                        this@MediaSavedActivity,
                        listOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                ) {
                    Intent(this@MediaSavedActivity, CameraActivity::class.java).let {
                        it.putExtra(Constants.CAMERA, true)
                        startActivity(it)
                    }
                } else {
                    Intent(this@MediaSavedActivity, RequestPermissionActivity::class.java).let {
                        it.putExtra(
                            Constants.INTENT_REQUEST_SINGLE_PERMISSION,
                            RequestPermissionActivity.TYPE_CAMERA
                        )
                        startActivityForResult(it, RequestPermissionActivity.REQUEST_CODE)
                    }
                }
            }
        }
    }

    private fun setupRecycleView() {
        photoAdapter = PhotoAdapter(isVideo) { photo ->
            startActivityForResult(
                PreviewSavedActivity.getIntent(this, photo),
                REQUEST_CODE_PREVIEW
            )
        }
        binding.rcvImage.apply {
            val spanCount = if (isVideo) 2 else 3
            setHasFixedSize(true)
            adapter = this@MediaSavedActivity.photoAdapter

            val gridLayoutManager = GridLayoutManager(this@MediaSavedActivity, spanCount)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter?.getItemViewType(position)) {
                        PhotoAdapter.VIEW_TYPE_DATE_HEADER -> spanCount
                        else -> 1
                    }
                }
            }
            layoutManager = gridLayoutManager
            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_medium)
            addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, false))
        }
    }

    override fun onResume() {
        super.onResume()
        initNativeAd()
        loadMediaData()
    }

    private fun loadMediaData() {
        if (isVideo) {
            photosViewModel.loadVideosFromAppAlbum()
            binding.tvTitle.text = getString(R.string.saved_video)
        } else {
            photosViewModel.loadPhotosFromAppAlbum()
            binding.tvTitle.text = getString(R.string.saved_image)
        }
    }

    private fun observeViewModel() {
        photosViewModel.photos.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    resource.data?.let { photos ->
                        val photoByDates = photos.groupBy { photo ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            dateFormat.format(Date(photo.dateAdded * 1000))
                        }
                        Log.d("MediaSavedActivity", "observeViewModel: ${photoByDates.size}")
                        photoAdapter?.submitList(photoByDates)
                        if (photoByDates.isEmpty()) {
                            Log.d("MediaSavedActivity", "observeViewModel: empty")
                            binding.llTakePhoto.visible()
                            binding.rcvImage.gone()
                        } else {
                            binding.llTakePhoto.gone()
                            binding.rcvImage.visible()
                        }
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(
                        this,
                        getString(R.string.unable_to_load_photos_from_album), Toast.LENGTH_LONG
                    ).show()
                    binding.llTakePhoto.visible()
                    binding.rcvImage.gone()
                }
            }

        }
    }

    fun initNativeAd() {
        val savedKey = RemoteConfig.remoteNativePhotoSelector
        Log.d("MediaSavedActivity", "initNativeAd: $savedKey")
        if (savedKey > 0) {
            binding.frNative.visible()
            AdmobLib.loadAndShowNative(
                this,
                AdsManager.admobNativeSaved,
                binding.frNative,
                layout = R.layout.custom_ads_native_small,
            )
        } else {
            binding.frNative.gone()
        }
    }


}
