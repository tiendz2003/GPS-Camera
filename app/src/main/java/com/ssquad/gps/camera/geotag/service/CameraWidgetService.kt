package com.ssquad.gps.camera.geotag.service

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.data.local.db.AppDatabase
import com.ssquad.gps.camera.geotag.data.models.PhotoDto
import com.ssquad.gps.camera.geotag.data.models.PhotoSource
import com.ssquad.gps.camera.geotag.widget.CameraWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class CameraWidgetService:RemoteViewsService() {
    private val appDatabase:AppDatabase by inject()
    override fun onGetViewFactory(p0: Intent?): RemoteViewsFactory {
        return CameraWidgetFactory(applicationContext,appDatabase)
    }

}
class CameraWidgetFactory(
    private val context:Context,
    appDatabase:AppDatabase
):RemoteViewsService.RemoteViewsFactory{
    private var photos:List<PhotoDto> = emptyList()
    private val photoDao = appDatabase.photoDao()
    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        photos = runBlocking {
            withContext(Dispatchers.IO){
                Log.d("CameraWidgetFactory","${photoDao.getWidgetPhotoSyncBySource(PhotoSource.AUTO)}")
                photoDao.getWidgetPhotoSyncBySource(PhotoSource.AUTO)
            }
        }
    }

    override fun onDestroy() {
       photos = emptyList()
    }

    override fun getCount(): Int {
        return photos.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val photo = photos[position]

        return RemoteViews(
            context.packageName,
            R.layout.widget_item
        ).apply {
            try {
                val bitmap = getBitmapFromPath(context,photo.path)
                Log.d("CameraWidgetFactory","$bitmap")
                setImageViewBitmap(R.id.widget_image,bitmap)
            }catch (e:Exception){
                Log.e("CameraWidgetFactory","${e.message}")
                setImageViewResource(R.id.widget_image,R.drawable.ic_open_template)
            }
            val fillIntent = Intent().apply {
                putExtra("photo_path",photo.path)
                putExtra("photo_id",photo.id)
            }
            setOnClickFillInIntent(R.id.widget_image,fillIntent)
        }

    }

    override fun getLoadingView(): RemoteViews? {
       return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(p0: Int): Long {
       return p0.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
    private fun getBitmapFromPath(context: Context, path: String): Bitmap {
        val uri = Uri.parse(path)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetWidth = getWidgetWidth(context,appWidgetManager)
        val widgetHeight = getWidgetHeight(context,appWidgetManager)
        return try {
            Glide.with(context).asBitmap().load(uri).override(widgetWidth*2,widgetHeight*2).centerCrop().diskCacheStrategy(
                DiskCacheStrategy.ALL).submit().get()
        }catch (e:Exception){
            Log.e("Widget", "Error loading bitmap", e)
            // Fallback nếu Glide thất bại
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_per_image, options)
        }
    }
    // Hàm lấy kích thước thực tế của widget
    private fun getWidgetWidth(context: Context, appWidgetManager: AppWidgetManager): Int {
        // Lấy kích thước từ AppWidgetOptions nếu có
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, CameraWidgetProvider::class.java)
        )

        if (appWidgetIds.isNotEmpty()) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetIds[0])
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            return if (width > 0) {
                // Chuyển đổi dp sang px
                (width * context.resources.displayMetrics.density).toInt()
            } else {
                // Giá trị mặc định nếu không lấy được
                (180 * context.resources.displayMetrics.density).toInt()
            }
        }

        return (180 * context.resources.displayMetrics.density).toInt()
    }

    private fun getWidgetHeight(context: Context, appWidgetManager: AppWidgetManager): Int {
        // Tương tự như getWidgetWidth
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, CameraWidgetProvider::class.java)
        )

        if (appWidgetIds.isNotEmpty()) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetIds[0])
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            return if (height > 0) {
                (height * context.resources.displayMetrics.density).toInt()
            } else {
                (110 * context.resources.displayMetrics.density).toInt()
            }
        }

        return (110 * context.resources.displayMetrics.density).toInt()
    }
}