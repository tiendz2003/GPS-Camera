package com.example.baseproject.bases

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState

abstract class BaseCustomView : RelativeLayout {
    private var attrs: AttributeSet? = null
    private val styleableRes = getStyleableRes()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.attrs = attrs
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.attrs = attrs
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.attrs = attrs
        init()
    }

    private fun init() {
        initView()
        initData()
        initListener()
        styleableRes?.let {
            val mTypedArray = context.theme.obtainStyledAttributes(attrs, it, 0, 0)
            initStyleable(mTypedArray)
        }
    }

    abstract fun getStyleableRes(): IntArray?
    abstract fun initView()
    abstract fun initListener()
    abstract fun initData()
    abstract fun initStyleable(mTypedArray: TypedArray?)

    abstract fun setData(data: TemplateDataModel,state: TemplateState?=null)
    abstract fun updateVisibility(state: TemplateState)

    open val templateId: Int = -1
}
