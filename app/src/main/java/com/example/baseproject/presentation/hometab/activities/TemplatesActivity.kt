package com.example.baseproject.presentation.hometab.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityTemplatesBinding
import com.example.baseproject.models.TemplateType
import com.example.baseproject.models.ThemeTemplateModel
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter
import com.example.baseproject.utils.dpToPx

class TemplatesActivity : BaseActivity<ActivityTemplatesBinding>(ActivityTemplatesBinding::inflate) {
    private val dailyAdapter by lazy { ThemeTemplateAdapter {
        Log.d("TemplatesActivity", "initData: ${it.id}")

    }
    }
    private val travelAdapter by lazy { ThemeTemplateAdapter(){
        Log.d("TemplatesActivity", "initData: $it")
    }
    }
    private val gpsAdapter by lazy { ThemeTemplateAdapter(){
        Log.d("TemplatesActivity", "initData: $it")
    }
    }


    val listTheme = ThemeTemplateModel.getTemplate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        Log.d("TemplatesActivity", "initData: $listTheme")
        val listDaily = listTheme.filter { it.type == TemplateType.DAILY }
        val listTravel = listTheme.filter { it.type == TemplateType.TRAVEL }
        val listGps = listTheme.filter { it.type == TemplateType.GPS }
        dailyAdapter.submitList(listDaily)
        travelAdapter.submitList(listTravel)
        gpsAdapter.submitList(listGps)
    }

    override fun initView() {
        setupRecyclerView()
    }

    override fun initActionView() {
        binding.tvViewAllDaily.setOnClickListener {
            startActivity(DetailTemplateActivity.getIntent(this, TemplateType.DAILY))
        }
        binding.tvViewAllTravel.setOnClickListener {
            startActivity(DetailTemplateActivity.getIntent(this, TemplateType.TRAVEL))
        }
        binding.tvViewAllGPS.setOnClickListener {
            startActivity(DetailTemplateActivity.getIntent(this, TemplateType.GPS))
        }
    }
    private fun setupRecyclerView() {
        binding.rvDaily.apply {
            Log.d("TemplatesActivity", "List size: ${listTheme.size}")
            binding.rvDaily.addItemDecoration(
                HorizontalSpaceItemDecoration(16.dpToPx(this@TemplatesActivity),4.dpToPx(this@TemplatesActivity))
            )
            binding.rvDaily.setHasFixedSize(true)
            adapter = dailyAdapter
            layoutManager = LinearLayoutManager(this@TemplatesActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.rvTravel.apply {
            binding.rvTravel.addItemDecoration(
                HorizontalSpaceItemDecoration(16.dpToPx(this@TemplatesActivity),4.dpToPx(this@TemplatesActivity))
            )
            binding.rvTravel.setHasFixedSize(true)
            adapter = travelAdapter
            layoutManager = LinearLayoutManager(this@TemplatesActivity, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.rvGPS.apply {
            binding.rvGPS.addItemDecoration(
                HorizontalSpaceItemDecoration(16.dpToPx(this@TemplatesActivity),4.dpToPx(this@TemplatesActivity))
            )
            binding.rvGPS.setHasFixedSize(true)
            adapter = gpsAdapter
            layoutManager = LinearLayoutManager(this@TemplatesActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }
}