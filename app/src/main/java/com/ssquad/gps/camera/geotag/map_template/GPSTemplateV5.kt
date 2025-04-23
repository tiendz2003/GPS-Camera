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
import com.ssquad.gps.camera.geotag.databinding.TemplateGps5Binding
import com.ssquad.gps.camera.geotag.utils.formatCoordinate
import com.ssquad.gps.camera.geotag.utils.loadImageIcon

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
            tvLatLabel.text = "Lat: ${data.lat?.formatCoordinate()}"
            tvLongLabel.text = "Long: ${data.long?.formatCoordinate()}"
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
            cardMapThumbnail.visibility = if (state.showLocation) VISIBLE else GONE
            llLatLong.visibility = if (state.showLatLong) VISIBLE else GONE
            tvTemperature.visibility = if (state.showTemperature) VISIBLE else GONE
            tvDateTime.visibility = if (state.showDate) VISIBLE else GONE
        }
    }
    override fun setMapImage(imageUrl: Bitmap?) {
        Log.d("GPSTemplateV1", "setMapImage: $imageUrl")
        imageUrl?.let {
            binding.cardMapThumbnail.loadImageIcon(it)
        }
    }
}