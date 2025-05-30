package com.ssquad.gps.camera.geotag.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseCustomView
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.databinding.TemplateTravel3Binding
import com.ssquad.gps.camera.geotag.utils.getFormattedTemperature

class TravelTemplateV3(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateTravel3Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateTravel3Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel,templateState: TemplateState?) {
        with(binding){
            tvLocation.text = data.location
            tvTemperature.text = data.getFormattedTemperature()
            tvDate.text = data.currentDate
            templateState?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding){
            llLocation.visibility = if (state.showLocation) VISIBLE else GONE
            tvTemperature.visibility = if (state.showTemperature) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
        }
    }
}