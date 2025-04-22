package com.ssquad.gps.camera.geotag.utils


import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ssquad.gps.camera.geotag.R

object CustomSnackbar {

    fun showProcessingSnackbar(view: View, message: String): Snackbar {
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        val snackbarView = snackbar.view
        val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        snackbarView.layoutParams = params

        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.snackbar_processing, null)
        // Set background color
        snackbarView.setBackgroundColor(Color.TRANSPARENT)
        // Set message
        val textView = customView.findViewById<TextView>(R.id.text_processing)
        textView.text = message

        // Ẩn text mặc định
        val snackbarTextView = snackbarView.findViewById<View>(com.google.android.material.R.id.snackbar_text)
        snackbarTextView.invisible()
        snackbarView.setPadding(0, 160, 0, 0)
        // Thêm customView vào snackbar
        (snackbarView as ViewGroup).addView(customView,0)

        return snackbar
    }

    fun showSuccessSnackbar(
        view: View,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,

    ): Snackbar {
        val snackbar = Snackbar.make(view, "", duration)
        val snackbarView = snackbar.view
        val params = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        snackbarView.layoutParams = params
        snackbarView.setBackgroundColor(Color.TRANSPARENT)
        // Inflate custom view
        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.snackbar_success, null)

        // Set message
        val textView = customView.findViewById<TextView>(R.id.text_success)
        textView.text = message

        // Ẩn text mặc định
        val snackbarTextView = snackbarView.findViewById<View>(com.google.android.material.R.id.snackbar_text)
        snackbarTextView.invisible()
        snackbarView.setPadding(0, 160, 0, 0)
        // Thêm custom view
        (snackbarView as ViewGroup).addView(customView,0)

        return snackbar
    }
}
