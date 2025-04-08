package com.example.baseproject.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baseproject.R
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
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