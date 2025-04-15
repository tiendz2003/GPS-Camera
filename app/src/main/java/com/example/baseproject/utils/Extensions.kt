package com.example.baseproject.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baseproject.R
import com.example.baseproject.bases.BaseCustomView
import com.example.baseproject.data.models.TemplateDataModel
import com.example.baseproject.data.models.TemplateState
import com.example.baseproject.map_template.DailyTemplateV1
import com.example.baseproject.map_template.DailyTemplateV2
import com.example.baseproject.map_template.DailyTemplateV3
import com.example.baseproject.map_template.DailyTemplateV4
import com.example.baseproject.map_template.DailyTemplateV5
import com.example.baseproject.map_template.GPSTemplateV1
import com.example.baseproject.map_template.GPSTemplateV2
import com.example.baseproject.map_template.GPSTemplateV3
import com.example.baseproject.map_template.GPSTemplateV4
import com.example.baseproject.map_template.GPSTemplateV5
import com.example.baseproject.map_template.TravelTemplateV1
import com.example.baseproject.map_template.TravelTemplateV2
import com.example.baseproject.map_template.TravelTemplateV3
import com.example.baseproject.map_template.TravelTemplateV4
import com.example.baseproject.map_template.TravelTemplateV5
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayout
import kotlin.math.roundToInt

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

fun Int.dpToPx(context: Context): Int {
    val displayMetrics: DisplayMetrics = context.resources!!.displayMetrics
    return (this * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun MaterialShapeDrawable.updateCornerSize(context: Context) {
    val shapeAppearanceModel = this.shapeAppearanceModel.toBuilder()
        .setTopLeftCornerSize(16f.dpToPx(context))
        .setTopRightCornerSize(16f.dpToPx(context))
        .build()

    // Thiết lập model mới và giữ nguyên các thuộc tính đường cong cho FAB
    this.shapeAppearanceModel = shapeAppearanceModel

    // Thiết lập đổ bóng
    this.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
    this.initializeElevationOverlay(context)
    this.elevation = 4f.dpToPx(context)
}

fun RecyclerView.setupHorizontal(adapter: RecyclerView.Adapter<*>) {
    addItemDecoration(HorizontalSpaceItemDecoration(16.dpToPx(context), 4.dpToPx(context)))
    setHasFixedSize(true)
    this.adapter = adapter
    layoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.HORIZONTAL, false
    )
}
 fun RecyclerView.scrollToCenter(position: Int) {
    val layoutManager = this.layoutManager as? LinearLayoutManager ?: return
    val itemCount = adapter?.itemCount?:0
    if(position == 0 || position == itemCount - 1) {
        this.smoothScrollToPosition(position)
        return
    }
    val smoothScroller = object : LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {

            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return 150f / displayMetrics.densityDpi
        }
    }
    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}
fun ImageView.loadImageIcon(url: Any) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_image_default)
        .error(R.drawable.ic_image_default)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}
@SuppressLint("DefaultLocale")
fun Int.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun FrameLayout.addTemplate(
    context: Context,
    type: String,
    data: TemplateDataModel,
    templateState: TemplateState? = null,
    imageMap: Any? = null
){
    this.removeAllViews()
    val layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    if (this.width == 0 || this.height == 0) {
        this.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY)
        )
        this.layout(0, 0, 1080, 300)
    }
    layoutParams.gravity = Gravity.BOTTOM
    val templateView = when(type){
        Config.TEMPLATE_1 -> DailyTemplateV1(context, null)
        Config.TEMPLATE_2 -> DailyTemplateV2(context, null)
        Config.TEMPLATE_3 -> DailyTemplateV3(context, null)
        Config.TEMPLATE_4 -> DailyTemplateV4(context, null)
        Config.TEMPLATE_5 -> DailyTemplateV5(context, null)
        Config.TEMPLATE_6 -> TravelTemplateV1(context, null)
        Config.TEMPLATE_7 -> TravelTemplateV2(context, null)
        Config.TEMPLATE_8 -> TravelTemplateV3(context, null)
        Config.TEMPLATE_9 -> TravelTemplateV4(context, null)
        Config.TEMPLATE_10 -> TravelTemplateV5(context, null)
        Config.TEMPLATE_11 -> GPSTemplateV1(context, null)
        Config.TEMPLATE_12 -> GPSTemplateV2(context, null)
        Config.TEMPLATE_13 -> GPSTemplateV3(context, null)
        Config.TEMPLATE_14 -> GPSTemplateV4(context, null)
        Config.TEMPLATE_15 -> GPSTemplateV5(context, null)
        else -> {
            Config.TEMPLATE_1
        }
    }
    if(templateView is BaseCustomView){
        templateView.layoutParams = layoutParams
        templateView.setData(data,templateState)
        this.addView(templateView)
    }
}
inline fun <reified T:Parcelable> Intent.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(key)
    }
}
 fun Activity.startCountdownAnimation(view: View) {
     view.visible()
     view.animate()
        .scaleX(1.5f)
        .scaleY(1.5f)
        .setDuration(500)
        .withEndAction {
            view.gone()
            view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(500)
                .start()
        }
        .start()
}