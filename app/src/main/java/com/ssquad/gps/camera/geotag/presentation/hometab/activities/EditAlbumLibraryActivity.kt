package com.ssquad.gps.camera.geotag.presentation.hometab.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.snake.squad.adslib.AdmobLib
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.EditAlbumAdapter
import com.ssquad.gps.camera.geotag.databinding.ActivityAlbumLibraryBinding
import com.ssquad.gps.camera.geotag.presentation.viewmodel.AlbumViewModel
import com.ssquad.gps.camera.geotag.utils.AdsManager
import com.ssquad.gps.camera.geotag.utils.RemoteConfig
import com.ssquad.gps.camera.geotag.utils.Resource
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditAlbumLibraryActivity : BaseActivity<ActivityAlbumLibraryBinding>(
    ActivityAlbumLibraryBinding::inflate
) {
    private val albumsViewModel: AlbumViewModel by viewModel()
    private lateinit var adapter: EditAlbumAdapter
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun initData() {
    }

    override fun initView() {
        observeViewModel()
        setupRecycleView()
    }

    override fun initActionView() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    private fun setupRecycleView(){
        adapter = EditAlbumAdapter {album->
            startActivity(SelectImageActivity.getIntent(this,album.id,album.name))
        }
        binding.rcvAlbum.adapter = adapter
    }
    private fun observeViewModel(){
        albumsViewModel.albums.observe(this){resource->
            when(resource){
                is Resource.Loading -> {

                }
                is Resource.Success->{
                    resource.data?.let {albums->
                        Log.d("EditAlbumLibraryActivity", "observeViewModel: $albums")
                        adapter.submitList(albums)
                        if (albums.isEmpty()) {
                            binding.llTakePhoto.visible()
                        }else{
                            binding.llTakePhoto.gone()
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }
    fun initNativeAd(){
        val photoSelectorKey = RemoteConfig.remoteNativePhotoSelector
        Log.d("EditAlbumLibraryActivity", "initNativeAd: $photoSelectorKey")
        if (photoSelectorKey > 0) {
            binding.frNative.visible()
            AdmobLib.loadAndShowNative(
                this,
                AdsManager.admobNativePhotoSelector,
                binding.frNative,
                layout = R.layout.custom_ads_native_small,
            )
        } else {
            binding.frNative.gone()
        }
    }
    override fun onResume() {
        super.onResume()
        initNativeAd()
        albumsViewModel.loadAlbums()
    }
}