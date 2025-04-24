package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.PhotoAdapter
import com.ssquad.gps.camera.geotag.presentation.mainscreen.activity.PreviewImageActivity
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivitySelectedImageBinding
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PhotosViewModel
import com.ssquad.gps.camera.geotag.utils.GridSpacingItemDecoration
import com.ssquad.gps.camera.geotag.utils.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectImageActivity : BaseActivity<ActivitySelectedImageBinding>(ActivitySelectedImageBinding::inflate) {
    companion object {
        const val ALBUM_ID = "ALBUM_ID"
        const val ALBUM_NAME = "ALBUM_NAME"
        fun getIntent(context: Context, albumId: String, albumName: String): Intent {
            return Intent(context, SelectImageActivity::class.java).apply {
                putExtra(ALBUM_ID, albumId)
                putExtra(ALBUM_NAME, albumName)
            }
        }
    }
    private var albumId: String? = null
    private var albumName: String? = null
    private var adapter: PhotoAdapter? = null
    private val photoViewModel: PhotosViewModel by viewModel()


    override fun initData() {
        albumId = intent.getStringExtra(ALBUM_ID)
        albumName = intent.getStringExtra(ALBUM_NAME)
        albumId?.let { albumId ->
            photoViewModel.loadPhotosFromAlbum(albumId)
        }
        photoViewModel.getCacheDataTemplate()
    }

    override fun initView() {
        setupRecycleView()
        observeViewModel()
    }

    override fun initActionView() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
    private fun setupRecycleView(){
        adapter = PhotoAdapter {photo->
            startActivity(
                Intent(this, PreviewImageActivity::class.java).apply {
                    putExtra("TEMPLATE_DATA", photoViewModel.cacheData.value)
                    putExtra("IMAGE_PATH", (photo.path).toString())
                    putExtra("FROM_ALBUM", true)
                }
            )
        }
        binding.rcvImage.apply {
            val spanCount = 3
            setHasFixedSize(true)
            adapter = this@SelectImageActivity.adapter

            val gridLayoutManager = GridLayoutManager(this@SelectImageActivity, spanCount)
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
        binding.tvTitle.text = albumName
    }
    private fun observeViewModel(){
        photoViewModel.photos.observe(this){ resource->
            when(resource){
                is Resource.Loading -> {

                }
                is Resource.Success->{
                    resource.data?.let {photos->
                        val photosByDate = photos.groupBy {photo->
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(photo.dateAdded * 1000))
                        }
                        adapter?.submitList(photosByDate)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
    }
}