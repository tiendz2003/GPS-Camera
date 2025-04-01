package com.example.baseproject.utils

import android.content.Context
import android.view.View
import com.google.android.material.shape.MaterialShapeDrawable

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