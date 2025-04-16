package com.example.baseproject.presentation.hometab.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.data.models.SortOption
import com.example.baseproject.databinding.ActivityImagesSavedBinding
import com.example.baseproject.presentation.hometab.adapter.PhotoAdapter
import com.example.baseproject.presentation.hometab.dialog.SortBottomSheet
import com.example.baseproject.presentation.mainscreen.activity.PreviewSavedActivity
import com.example.baseproject.presentation.viewmodel.PhotosViewModel
import com.example.baseproject.utils.GridSpacingItemDecoration
import com.example.baseproject.utils.Resource
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

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
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun setupRecycleView() {
        photoAdapter = PhotoAdapter(isVideo){ photo ->
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
        loadMediaData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PREVIEW && resultCode == RESULT_OK) {
            loadMediaData()
        }
    }
    fun loadMediaData(){
        if (isVideo) {
            photosViewModel.loadVideosFromAppAlbum()
            binding.tvTitle.text = getString(R.string.saved_video)
        }else{
            photosViewModel.loadPhotosFromAppAlbum()
            binding.tvTitle.text = getString(R.string.saved_image)
        }
    }
    fun observeViewModel() {
        photosViewModel.photos.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    resource.data?.let { photos ->
                        val photoByDates = photos.groupBy { photo ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            dateFormat.format(Date(photo.dateAdded*1000))

                        }
                        photoAdapter?.submitList(photoByDates)
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}
