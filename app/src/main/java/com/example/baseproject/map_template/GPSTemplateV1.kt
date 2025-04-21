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
import com.example.baseproject.databinding.TemplateGps1Binding
import com.example.baseproject.utils.loadImageIcon

class GPSTemplateV1(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateGps1Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateGps1Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}

    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel, templateState: TemplateState?) {
        val part = data.location?.split(",")
        val city = part?.get(0)
        with(binding) {
            tvDate.text = data.currentDate
            tvTemp.text = data.temperature
            tvCity.text = city
            tvAddress.text = data.location
            templateState?.let {
                updateVisibility(it)
            }
        }
    }

    override fun updateVisibility(state: TemplateState) {
        with(binding) {
            tvCity.visibility = if (state.showLocation) VISIBLE else GONE
            tvAddress.visibility = if (state.showLocation) VISIBLE else GONE
            imvMap.visibility = if (state.showLocation) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
            tvTemp.visibility = if (state.showTemperature) VISIBLE else GONE
        }
    }

    override fun setMapImage(imageUrl: Bitmap?) {
        Log.d("GPSTemplateV1", "setMapImage: $imageUrl")
        imageUrl?.let {
            binding.imvMap.loadImageIcon(it)
        }
    }
}