package com.example.baseproject.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState
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
    override fun setData(data: TemplateDataModel, state: TemplateState?) {
        val part = data.location?.split(",")
        val city = part?.get(0)
        val country = part?.get(1)
        with(binding){
            tvCity.underline()
            tvCountry.underline()
            tvCity.text = city
            tvCountry.text = country
            tvTime.text = "${data.currentDate} ${data.currentTime}"
            state?.let {
                updateVisibility(it)
            }
        }
    }
    override fun updateVisibility(state: TemplateState) {
        with(binding){
            tvCity.visibility = if (state.showLocation) VISIBLE else GONE
            tvCountry.visibility = if (state.showLocation) VISIBLE else GONE
            tvTime.visibility = if (state.showDate) VISIBLE else GONE
        }
    }
}