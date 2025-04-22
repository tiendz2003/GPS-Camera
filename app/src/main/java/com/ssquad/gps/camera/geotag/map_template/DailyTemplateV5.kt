package com.ssquad.gps.camera.geotag.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.ssquad.gps.camera.geotag.bases.BaseCustomView
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.databinding.TemplateDaily5Binding

class DailyTemplateV5(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateDaily5Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateDaily5Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel, state: TemplateState?) {
        val part = data.location?.split(",")
        val city = part?.get(0)
        with(binding){
            tvTime.text = data.currentTime
            tvCity.text = city
            tvAddress.text = data.location
            tvTemp.text = data.temperature
            tvDate.text = data.currentDate
            state?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvCity.visibility = if (state.showLocation) VISIBLE else GONE
            tvAddress.visibility = if (state.showLocation) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
            tvTime.visibility = if (state.showTime) VISIBLE else GONE
            tvTemp.visibility = if (state.showTemperature) VISIBLE else GONE
        }
    }
}