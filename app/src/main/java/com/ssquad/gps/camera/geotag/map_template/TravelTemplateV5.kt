package com.ssquad.gps.camera.geotag.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.ssquad.gps.camera.geotag.bases.BaseCustomView
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.databinding.TemplateTravel5Binding

class TravelTemplateV5(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateTravel5Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateTravel5Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel,templateState: TemplateState?) {
        with(binding) {
            tvDate.text = data.currentDate
            tvLocation.text = data.location
            tvTime.text = data.currentTime
            tvTemperature.text = data.temperature
            templateState?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding) {
            tvLocation.visibility = if (state.showLocation) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
            tvTime.visibility = if (state.showTime) VISIBLE else GONE
            tvTemperature.visibility = if (state.showTemperature) VISIBLE else GONE
        }
    }
}