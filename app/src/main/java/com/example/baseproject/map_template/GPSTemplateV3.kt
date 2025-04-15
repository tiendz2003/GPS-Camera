package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState
import com.example.baseproject.databinding.TemplateGps3Binding

class GPSTemplateV3(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateGps3Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateGps3Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel, state: TemplateState?) {
        with(binding) {
            val part = data.location?.split(",")
            val city = part?.get(0)
            tvCity.text = city
            tvLocation.text = data.location
            tvLatitude.text = "Latitude: ${data.lat}"
            tvLongitude.text = "Longitude: ${data.long}"
            tvTemp.text= data.temperature
            state?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvCity.visibility = if (state.showLocation) VISIBLE else GONE
            tvLocation.visibility = if (state.showLocation) VISIBLE else GONE
            tvLatitude.visibility = if (state.showLatLong) VISIBLE else GONE
            tvLongitude.visibility = if (state.showLatLong) VISIBLE else GONE
            tvTemp.visibility = if (state.showTemperature) VISIBLE else GONE
        }
    }
}