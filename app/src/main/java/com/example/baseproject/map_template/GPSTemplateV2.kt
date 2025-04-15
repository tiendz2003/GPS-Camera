package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState
import com.example.baseproject.databinding.TemplateGps2Binding

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
            tvLatValue.text = data.lat
            tvLongValue.text = data.long
            state?.let {
                updateVisibility(it)
            }
        }
    }

    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvAddress.visibility = if (state.showLocation) VISIBLE else GONE
            tvDate.visibility = if (state.showDate) VISIBLE else GONE
            tvTime.visibility = if (state.showTime) VISIBLE else GONE
            tvLatValue.visibility = if (state.showLatLong) VISIBLE else GONE
            tvLongValue.visibility = if (state.showLatLong) VISIBLE else GONE
        }
    }
}