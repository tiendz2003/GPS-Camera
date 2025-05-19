package com.ssquad.gps.camera.geotag.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ssquad.gps.camera.geotag.widget.CameraWidgetProvider

class PhotoBroadcastReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Cập nhật widget khi có ảnh mới
            CameraWidgetProvider.updateWidget(context!!)
        }
    }
}