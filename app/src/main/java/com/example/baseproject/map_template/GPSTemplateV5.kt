package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState
import com.example.baseproject.databinding.TemplateGps5Binding
import com.example.baseproject.utils.loadImageIcon

class GPSTemplateV5(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateGps5Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateGps5Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel,templateState: TemplateState?) {
        with(binding) {
            tvLocationName.text = data.location
            tvLatLabel.text = "Lat: ${data.lat}"
            tvLongLabel.text = "Long: ${data.long}"
            tvTemperature.text= data.temperature
            tvDateTime.text = data.currentDate
            templateState?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvLocationName.visibility = if (state.showLocation) VISIBLE else GONE
            ivMapThumbnail.visibility = if (state.showLocation) VISIBLE else GONE
            tvLatLabel.visibility = if (state.showLatLong) VISIBLE else GONE
            tvLongLabel.visibility = if (state.showLatLong) VISIBLE else GONE
            tvTemperature.visibility = if (state.showTemperature) VISIBLE else GONE
            tvDateTime.visibility = if (state.showDate) VISIBLE else GONE
        }
    }
    override fun setMapImage(imageUrl: Bitmap?) {
        Log.d("GPSTemplateV1", "setMapImage: $imageUrl")
        imageUrl?.let {
            binding.ivMapThumbnail.loadImageIcon(it)
        }
    }
}