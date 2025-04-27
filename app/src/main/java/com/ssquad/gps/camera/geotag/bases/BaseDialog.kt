package com.ssquad.gps.camera.geotag.bases

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<B : ViewBinding>(
    val bindingFactory: (LayoutInflater) -> B,
    context: Context,
    private val cancelable: Boolean = false
) : Dialog(context) {
    protected val binding: B by lazy { bindingFactory(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(cancelable)
        initView()
        initData()
        initActionView()
        showSmooth()
        layoutContainer.setOnClickListener {
            if (cancelable) hideDialog()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (cancelable) hideDialog()
    }

    protected fun showSmooth() {
        layoutContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    protected fun hideDialog() {
        layoutContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .start()
        Handler().postDelayed({
            dismiss()
        }, 300)
    }

    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initActionView()
    protected abstract val layoutContainer: View
}