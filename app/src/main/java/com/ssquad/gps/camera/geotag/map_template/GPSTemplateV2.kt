package com.ssquad.gps.camera.geotag.map_template

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
import com.ssquad.gps.camera.geotag.databinding.TemplateGps2Binding
import com.ssquad.gps.camera.geotag.utils.formatCoordinate
import com.ssquad.gps.camera.geotag.utils.loadImageIcon

class GPSTemplateV2(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateGps2Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateGps2Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel,state: TemplateState?) {
        with(binding){
            tvDate.text = data.currentDate
            tvTime.text = data.currentTime
            tvAddress.text = data.location
            tvLatValue.text = "${data.lat?.formatCoordinate()}°N"
            tvLongValue.text = "${data.long?.formatCoordinate()}°E"
            state?.let {
                updateVisibility(it)
            }
        }
    }

    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvAddress.visibility = if (state.showLocation) VISIBLE else GONE
            imvMap.visibility = if (state.showLocation) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
            tvTime.visibility = if (state.showTime) VISIBLE else GONE
            llLat.visibility = if (state.showLatLong) VISIBLE else GONE
            llLong.visibility = if (state.showLatLong) VISIBLE else GONE
        }
    }
    override fun setMapImage(imageUrl: Bitmap?) {
        Log.d("GPSTemplateV1", "setMapImage: $imageUrl")
        imageUrl?.let {
            binding.imvMap.loadImageIcon(it)
        }
    }
}