package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.ssquad.gps.camera.geotag.bases.BaseCustomView
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.databinding.TemplateGps4Binding
import com.ssquad.gps.camera.geotag.utils.getFormattedTemperature
import com.ssquad.gps.camera.geotag.utils.loadImageIcon

class GPSTemplateV4(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateGps4Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateGps4Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel,templateState: TemplateState?) {
        with(binding) {
            val part = data.location?.split(",")
            val city = part?.get(0)
            tvLocationTitle.text = city
            tvLocationAddress.text = data.location
            tvDate.text = data.currentDate
            tvTemperature.text = data.getFormattedTemperature()
            templateState?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvLocationTitle.visibility = if (state.showLocation) VISIBLE else GONE
            ivMapThumbnail.visibility = if (state.showLocation) VISIBLE else GONE
            tvLocationAddress.visibility = if (state.showLocation) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
            tvTemperature.visibility = if (state.showTemperature) VISIBLE else GONE
        }
    }
    override fun setMapImage(imageUrl: Bitmap?) {
        Log.d("GPSTemplateV1", "setMapImage: $imageUrl")
        imageUrl?.let {
            binding.ivMapThumbnail.loadImageIcon(it)
        }
    }
}