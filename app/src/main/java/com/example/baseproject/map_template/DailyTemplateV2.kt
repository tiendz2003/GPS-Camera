package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.databinding.TemplateDaily2Binding
import com.example.baseproject.utils.underline

class DailyTemplateV2(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateDaily2Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateDaily2Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}
    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel) {
        val part = data.location?.split(",")
        val city = part?.get(0)
        val country = part?.get(1)

        binding.tvCity.underline()
        binding.tvCountry.underline()

        binding.tvCity.text = city
        binding.tvCountry.text = country
        binding.tvTime.text = "${data.currentDate} ${data.currentTime}"
    }
}