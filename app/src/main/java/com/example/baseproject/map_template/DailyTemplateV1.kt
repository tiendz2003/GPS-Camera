package com.example.baseproject.map_template

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState
import com.example.baseproject.databinding.TemplateDaily1Binding

class DailyTemplateV1(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateDaily1Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateDaily1Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    override fun setData(data: TemplateDataModel,templateState: TemplateState?) {
        with(binding){
            templateState?.let {
              updateVisibility(it)
            }
            tvLocation.text = data.location?.uppercase()
            tvDate.text = data.currentDate
            tvTime.text = data.currentTime
            tvTemperature.text = data.temperature
        }
    }

    override fun updateVisibility(state: TemplateState) {
       with(binding){
           tvLocation.visibility = if (state.showLocation) VISIBLE else GONE
           tvDate.visibility = if (state.showDate) VISIBLE else GONE
           tvTime.visibility = if (state.showTime) VISIBLE else GONE
       }
    }

}
