package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseActivity
import com.ssquad.gps.camera.geotag.data.models.FormatItem.Companion.DATE_FORMATS
import com.ssquad.gps.camera.geotag.data.models.FormatItem.Companion.DATE_FORMAT_KEY
import com.ssquad.gps.camera.geotag.data.models.FormatItem.Companion.TIME_FORMATS
import com.ssquad.gps.camera.geotag.data.models.FormatItem.Companion.TIME_FORMAT_KEY
import com.ssquad.gps.camera.geotag.databinding.ActivityDateTimeFormatBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.adapter.FormatAdapter
import com.ssquad.gps.camera.geotag.utils.SharePrefManager

class DateTimeFormatActivity : BaseActivity<ActivityDateTimeFormatBinding>(
    ActivityDateTimeFormatBinding::inflate
) {
    private lateinit var timeAdapter: FormatAdapter
    private lateinit var dateAdapter: FormatAdapter

    override fun initData() {
    }
    override fun initView() {
        setupDateRecycleView()
        setupTimeRecycleView()
    }
    override fun initActionView() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }
        }
        setupSaveButton()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupDateRecycleView() {
        val listDateExample = DATE_FORMATS
        listDateExample.forEach { it.isSelected = false }
        val currentDateFormat =
            SharePrefManager.getString(DATE_FORMAT_KEY, listDateExample[0].id)
        dateAdapter = FormatAdapter(listDateExample) { selectedFormat ->
            listDateExample.forEach { it.isSelected = false }
            val selectedItem = listDateExample.find { it.id == selectedFormat.id }
            selectedItem?.isSelected = true
            dateAdapter.notifyDataSetChanged()
        }
        val initSelect = listDateExample.find { it.id == currentDateFormat }
        initSelect?.isSelected = true
        binding.rcvDateFormat.adapter = dateAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupTimeRecycleView() {
        val listTimeExample = TIME_FORMATS
        listTimeExample.forEach { it.isSelected = false }
        val currentTimeFormat =
            SharePrefManager.getString(TIME_FORMAT_KEY, listTimeExample[0].id)
        timeAdapter = FormatAdapter(listTimeExample) { selectedFormat ->
            listTimeExample.forEach { it.isSelected = false }
            val selectedItem = listTimeExample.find { it.id == selectedFormat.id }
            selectedItem?.isSelected = true
            timeAdapter.notifyDataSetChanged()
        }
        val initSelect = listTimeExample.find { it.id == currentTimeFormat }
        initSelect?.isSelected = true
        binding.rcvTimeFormat.adapter = timeAdapter
    }
    private fun setupSaveButton() {
        binding.btnCheck.setOnClickListener {

            val selectedDateFormat = DATE_FORMATS.find { it.isSelected }?.id ?: DATE_FORMATS[0].id
            SharePrefManager.putString(DATE_FORMAT_KEY, selectedDateFormat)

            val selectedTimeFormat = TIME_FORMATS.find { it.isSelected }?.id ?: TIME_FORMATS[0].id
            SharePrefManager.putString(TIME_FORMAT_KEY, selectedTimeFormat)

            finish()
        }
    }

}