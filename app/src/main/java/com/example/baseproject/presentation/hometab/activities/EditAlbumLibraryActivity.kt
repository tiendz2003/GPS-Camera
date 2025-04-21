package com.example.baseproject.presentation.hometab.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityAlbumLibraryBinding
import com.example.baseproject.presentation.hometab.adapter.EditAlbumAdapter
import com.example.baseproject.presentation.viewmodel.AlbumViewModel
import com.example.baseproject.utils.PermissionManager
import com.example.baseproject.utils.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditAlbumLibraryActivity : BaseActivity<ActivityAlbumLibraryBinding>(
    ActivityAlbumLibraryBinding::inflate
) {
    private val albumsViewModel: AlbumViewModel by viewModel()
    private lateinit var adapter: EditAlbumAdapter
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(PermissionManager.checkAndRequestPermissions(this)){
            albumsViewModel.loadAlbums()
        }
    }

    override fun initData() {
    }

    override fun initView() {
        observeViewModel()
        setupRecycleView()
    }

    override fun initActionView() {

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
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            { albumsViewModel.loadAlbums() },
            { /*finish() */}
        )
    }
}