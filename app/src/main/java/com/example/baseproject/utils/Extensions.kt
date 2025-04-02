package com.example.baseproject.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baseproject.R
import com.google.android.material.shape.MaterialShapeDrawable
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
fun ImageView.loadImageIcon(url: Any) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_image_default)
        .error(R.drawable.ic_image_default)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}