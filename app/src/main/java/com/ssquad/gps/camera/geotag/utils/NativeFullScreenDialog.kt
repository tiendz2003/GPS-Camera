package com.ssquad.gps.camera.geotag.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobNativeModel
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseDialog
import com.ssquad.gps.camera.geotag.databinding.DialogNativeFullscreenBinding
import java.util.Timer
import java.util.TimerTask

class NativeFullScreenDialog(
    private val mActivity: Activity,
    private val nativeModel: AdmobNativeModel,
    private val isStartNow: Boolean = false,
    private val onClose: () -> Unit,
    private val onFailure: () -> Unit
) : BaseDialog<DialogNativeFullscreenBinding>(
    DialogNativeFullscreenBinding::inflate,
    mActivity,
    false
) {
    companion object {
        private const val DEFAULT_COUNTER = 3
    }

    private var counter = DEFAULT_COUNTER
    var isClosedOrFail: Boolean = false
        set(value) {
            field = value
            startCounter()
        }
    private var isShowed: Boolean = false

    override fun initData() {

    }

    override fun initView() {
        binding.btnClose.isEnabled = false

        AdmobLib.showNative(
            mActivity,
            nativeModel,
            binding.frNative,
            size = GoogleENative.UNIFIED_FULL_SCREEN,
            layout = R.layout.custom_admob_native_full_screen,
            onAdsShowed = {
                Log.d("TAG", "initView: ${this.javaClass} showed")
                mActivity.runOnUiThread {
                    isShowed = true
                    startCounter()
                }
            },
            onAdsShowFail = {
                Log.e("TAG", "initView: ${this.javaClass} failure")
                Handler(Looper.getMainLooper()).post {
                    dismiss()
                    onFailure()
                }
                return@showNative Unit
            }
        )
    }

    override fun initActionView() {
        binding.btnClose.setOnClickListener {
            dismiss()
            onClose()
        }
    }

    override val layoutContainer: View
        get() = binding.root

    private fun startCounter() {
        if (isStartNow || isClosedOrFail && isShowed && isShowing) {
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mActivity.runOnUiThread {
                        if (counter == 0) {
                            binding.tvCounter.invisible()
                            binding.ivClose.visible()
                            binding.btnClose.isEnabled = true
                            timer.cancel()
                        } else {
                            binding.tvCounter.text = counter.toString()
                            counter--
                        }
                    }
                }
            }, 500, 1000)
        }
    }
}