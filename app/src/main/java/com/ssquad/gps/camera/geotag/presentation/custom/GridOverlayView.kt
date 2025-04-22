package com.ssquad.gps.camera.geotag.presentation.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1f
        alpha = 100
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        // vẽ line chiều dọc
        canvas.drawLine(width / 3, 0f, width / 3, height, paint)//1/3
        canvas.drawLine(width * 2 / 3, 0f, width * 2 / 3, height, paint)//2/3
        // vẽ line chiều ngang
        canvas.drawLine(0f, height / 3, width, height / 3, paint)
        canvas.drawLine(0f, height * 2 / 3, width, height * 2 / 3, paint)
    }
}