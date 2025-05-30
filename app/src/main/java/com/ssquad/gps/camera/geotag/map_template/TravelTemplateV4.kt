package com.ssquad.gps.camera.geotag.map_template

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseCustomView
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.databinding.TemplateTravel4Binding

class TravelTemplateV4(context: Context?, attrs: AttributeSet?) : BaseCustomView(context, attrs) {

    private lateinit var binding: TemplateTravel4Binding

    override val templateId: Int = 2

    override fun getStyleableRes(): IntArray? = null

    override fun initView() {
        binding = TemplateTravel4Binding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initListener() {}

    override fun initData() {}

    override fun initStyleable(mTypedArray: TypedArray?) {}

    @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel, templateState: TemplateState?) {
        with(binding) {
            tvLocation.text = data.location
            tvTime.text = data.currentTime
            templateState?.let {
                updateVisibility(it)
            }
        }
    }

    override fun updateVisibility(state: TemplateState) {
        with(binding) {
            llLocation.visibility = if (state.showLocation) VISIBLE else GONE
            tvTime.visibility = if (state.showTime) VISIBLE else GONE
        }
    }
}