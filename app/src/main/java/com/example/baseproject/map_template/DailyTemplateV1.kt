package com.example.baseproject.map_template

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
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
    override fun setData(data: TemplateDataModel) {
        binding.tvLocation.text = data.location?.uppercase()
        binding.tvDate.text = data.currentDate
        binding.tvTime.text = data.currentTime
        binding.tvTemperature.text = data.temperature
    }

  /*  @SuppressLint("SetTextI18n")
    override fun setData(data: TemplateDataModel) {
        val pref = SharePrefUtils.getInstance(context)

        val isShowTime = pref.showTime()
        val isShowDate = pref.showDate()
        val isShowLocation = pref.showLocation()
        val isShowWatermark = pref.showWatermark()

        // Vị trí
        binding.tvCity.text = data.city.uppercase()
        binding.tvAddress.text = data.address
        binding.tvCity.setVisibleGone(isShowLocation)
        binding.tvAddress.setVisibleGone(isShowLocation)

        // Watermark
        binding.tvAppName.setVisibleGone(isShowWatermark)
        binding.imvLogo.setVisibleGone(isShowWatermark)

        // Thời gian & ngày
        when {
            isShowTime && isShowDate -> {
                binding.tvTime.visible()
                binding.tvTime.text = "${data.time} ${data.date}"
            }
            isShowTime -> {
                binding.tvTime.visible()
                binding.tvTime.text = data.time
            }
            isShowDate -> {
                binding.tvTime.visible()
                binding.tvTime.text = data.date
            }
            else -> {
                binding.tvTime.gone()
            }
        }
    }*/
}
