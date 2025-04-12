package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.databinding.TemplateTravel3Binding

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
    override fun setData(data: TemplateDataModel) {
        binding.tvLocation.text = data.location
        binding.tvTemperature.text = data.temperature
        binding.tvDate.text = data.currentDate
    }
}