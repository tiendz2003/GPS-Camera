package com.ssquad.gps.camera.geotag.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.data.local.db.AppDatabase
import com.ssquad.gps.camera.geotag.data.models.PhotoSource
import com.ssquad.gps.camera.geotag.presentation.hometab.activities.MainActivity
import com.ssquad.gps.camera.geotag.service.CameraWidgetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

class CameraWidgetProvider : AppWidgetProvider() {

    companion object{
        private const val ACTION_UPDATE_WIDGET = "com.ssquad.gps.camera.UPDATE_WIDGET"
        fun updateWidget(context:Context){
            val intent = Intent(context,CameraWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach {id->
            updateAppWidget(context,appWidgetManager,id)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if(intent?.action == ACTION_UPDATE_WIDGET){
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context!!,CameraWidgetProvider::class.java)
            )
            onUpdate(context,appWidgetManager,appWidgetIds)
        }
    }
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    val appDatabase = GlobalContext.get().get<AppDatabase>()
    val intent = Intent(context,CameraWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
    }
    val views = RemoteViews(context.packageName,R.layout.camera_widget)
    views.setRemoteAdapter(R.id.viewFlipper,intent)

    val pendingIntent = Intent(context,MainActivity::class.java).let {click->
        PendingIntent.getActivity(
            context,0,click,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    views.setPendingIntentTemplate(R.id.viewFlipper,pendingIntent)
    val photos = runBlocking {
        withContext(Dispatchers.IO){
            appDatabase.photoDao().getWidgetPhotoSyncBySource(PhotoSource.AUTO)
        }
    }
    if(photos.isEmpty()){
        views.setViewVisibility(R.id.empty_view,View.VISIBLE)
        views.setViewVisibility(R.id.viewFlipper,View.GONE)
    }else{
        views.setViewVisibility(R.id.empty_view,View.GONE)
        views.setViewVisibility(R.id.viewFlipper,View.VISIBLE)
    }
    appWidgetManager.updateAppWidget(appWidgetId,views)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.viewFlipper)
}