package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.MapType
import com.ssquad.gps.camera.geotag.presentation.settingtab.adapter.MapTypeAdapter
import com.ssquad.gps.camera.geotag.utils.GridSpaceItemDecoration
import com.ssquad.gps.camera.geotag.utils.SharePrefManager
import com.google.android.gms.maps.GoogleMap
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ActivityMapTypeBinding

class MapTypeActivity : BaseActivity<ActivityMapTypeBinding>(ActivityMapTypeBinding::inflate) {
    private lateinit var mapTypeAdapter: MapTypeAdapter
    private var mapType: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {

    }

    override fun initView() {
        setupRecycleView()
        setupMapTypeList()
    }

    override fun initActionView() {
        binding.btnCheck.setOnClickListener {
            SharePrefManager.saveMapType(mapType)
            setResult(RESULT_OK)
            finish()
        }
        binding.btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

    }
    fun setupRecycleView(){
        mapTypeAdapter = MapTypeAdapter{
            mapType = it.type
            setResult(RESULT_OK)
        }
        mapTypeAdapter.setSelectedMapType(SharePrefManager.getMapType())
        binding.rvMapTypes.apply {
            adapter = mapTypeAdapter
            layoutManager = GridLayoutManager(this@MapTypeActivity, 2)
            setHasFixedSize(true)
            addItemDecoration(GridSpaceItemDecoration(16))
        }
    }
    private fun setupMapTypeList(){
        val mapTypes = listOf(
            MapType(
                id = 1,
                name = getString(R.string.normal),
                type = GoogleMap.MAP_TYPE_NORMAL,
                thumbnailRes = R.drawable.img_map_normal
            ),
            MapType(
                id = 2,
                name = getString(R.string.hybrid),
                type = GoogleMap.MAP_TYPE_HYBRID,
                thumbnailRes = R.drawable.img_map_hybrid
            ),
            MapType(
                id = 3,
                name = getString(R.string.satellite),
                type = GoogleMap.MAP_TYPE_SATELLITE,
                thumbnailRes = R.drawable.img_map_satellite
            ),
            MapType(
                id = 4,
                name = getString(R.string.terrain),
                type = GoogleMap.MAP_TYPE_TERRAIN,
                thumbnailRes = R.drawable.img_map_terrain
            )
        )

        mapTypeAdapter.submitList(mapTypes)
    }
}