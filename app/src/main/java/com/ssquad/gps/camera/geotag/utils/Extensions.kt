package com.ssquad.gps.camera.geotag.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ssquad.gps.camera.geotag.MyApplication
import com.ssquad.gps.camera.geotag.data.models.FormatItem
import com.ssquad.gps.camera.geotag.data.models.TemplateDataModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.map_template.DailyTemplateV1
import com.ssquad.gps.camera.geotag.map_template.DailyTemplateV2
import com.ssquad.gps.camera.geotag.map_template.DailyTemplateV3
import com.ssquad.gps.camera.geotag.map_template.DailyTemplateV4
import com.ssquad.gps.camera.geotag.map_template.DailyTemplateV5
import com.ssquad.gps.camera.geotag.map_template.GPSTemplateV1
import com.ssquad.gps.camera.geotag.map_template.GPSTemplateV2
import com.ssquad.gps.camera.geotag.map_template.GPSTemplateV3
import com.example.baseproject.map_template.GPSTemplateV4
import com.example.baseproject.map_template.GPSTemplateV5
import com.faltenreich.skeletonlayout.Skeleton
import com.google.android.gms.ads.MediaAspectRatio
import com.ssquad.gps.camera.geotag.map_template.TravelTemplateV1
import com.ssquad.gps.camera.geotag.map_template.TravelTemplateV2
import com.ssquad.gps.camera.geotag.map_template.TravelTemplateV3
import com.ssquad.gps.camera.geotag.map_template.TravelTemplateV4
import com.ssquad.gps.camera.geotag.map_template.TravelTemplateV5
import com.ssquad.gps.camera.geotag.presentation.custom.HorizontalSpaceItemDecoration
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.ThemeTemplateAdapter
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snake.squad.adslib.AdmobLib
import com.snake.squad.adslib.models.AdmobInterModel
import com.snake.squad.adslib.utils.GoogleENative
import com.ssquad.gps.camera.geotag.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.text.format

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

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


inline fun View.setOnDebounceClickListener(
    delay: Long = 800L,
    crossinline action: () -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delay) {
            lastClickTime = currentTime
            action()
        }
    }
}

fun navToActivity(context: Context, activity: Class<*>, bundle: Bundle? = null) {
    val intent = Intent(context, activity)
    intent.putExtras(bundle ?: Bundle())
    context.startActivity(intent)
}

fun String.formatCoordinate(decimalPlaces: Int = 4): String {
    val normalized = this.replace(",", ".")

    return normalized.toDoubleOrNull()?.let {
        String.format("%.${decimalPlaces}f", it)
    } ?: this
}

fun Bitmap.rotate(degrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees.toFloat())
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun MaterialShapeDrawable.updateCornerSize(context: Context) {
    val shapeAppearanceModel = this.shapeAppearanceModel.toBuilder()
        .setTopLeftCornerSize(18f.dpToPx(context))
        .setTopRightCornerSize(18f.dpToPx(context))
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

fun ThemeTemplateAdapter.updateSelection(selectedId: String): Boolean {
    val currentList = this.currentList.toMutableList()
    var changed = false

    currentList.forEach { item ->
        if (item.isSelected) {
            item.isSelected = false
            changed = true
        }
    }

    currentList.find { it.id == selectedId }?.let { item ->
        if (!item.isSelected) {
            item.isSelected = true
            changed = true
        }
    }
    if (changed) {
        this.submitList(null)
        this.submitList(currentList)
    }

    return changed
}

@SuppressLint("DefaultLocale")
fun Long.kbToMb(): String {
    val kb = this.toLong()
    val mb = kb / 1024
    return if (mb < 1) {
        "$kb KB"
    } else {
        String.format("%.2f MB", mb.toFloat() / 1024)
    }
}
fun TemplateDataModel?.getFormattedTemperature(): String {
    if (this == null) return "--"

    return if (SharePrefManager.getTemperature()) {
        "${this.temperatureF?.toInt() ?: "--"} °F"
    } else {
        "${this.temperatureC?.toInt() ?: "--"} °C"
    }
}
fun Long.formatCapturedTime(): String {
    val sdf = SimpleDateFormat("h:mm a dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this*1000))
}

@SuppressLint("DefaultLocale")
fun Int.formatCaptureDuration(): String {
    val seconds = (this / 1000)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds)
}

fun RecyclerView.scrollToCenter(position: Int) {
    val layoutManager = this.layoutManager as? LinearLayoutManager ?: return
    val itemCount = adapter?.itemCount ?: 0
    if (position < 0 || position >= itemCount) {
        return
    }
    if (position == 0 || position == itemCount - 1) {
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

@SuppressLint("DefaultLocale")
fun Int.formatDuration(): String {
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@SuppressLint("DefaultLocale")
fun Long.formatDuration(): String {
    val minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply {
        preScale(-1.0f, 1.0f)
    }
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}

fun FrameLayout.addTemplate(
    context: Context,
    type: String,
    data: TemplateDataModel,
    templateState: TemplateState? = null,
    imageMap: Bitmap? = null
) {
    this.removeAllViews()
    val layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    if (this.width == 0 || this.height == 0) {
        this.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY)
        )
        this.layout(0, 0, 1080, 300)
    }
    layoutParams.gravity = Gravity.BOTTOM
    val templateView = when (type) {
        Config.TEMPLATE_1 -> DailyTemplateV1(context, null)
        Config.TEMPLATE_2 -> DailyTemplateV2(context, null)
        Config.TEMPLATE_3 -> DailyTemplateV3(context, null)
        Config.TEMPLATE_4 -> DailyTemplateV4(context, null)
        Config.TEMPLATE_5 -> DailyTemplateV5(context, null)
        Config.TEMPLATE_6 -> TravelTemplateV1(context, null)
        Config.TEMPLATE_7 -> TravelTemplateV2(context, null)
        Config.TEMPLATE_8 -> TravelTemplateV3(context, null)
        Config.TEMPLATE_9 -> TravelTemplateV4(context, null)
        Config.TEMPLATE_10 -> TravelTemplateV5(context, null)
        Config.TEMPLATE_11 -> GPSTemplateV1(context, null)
        Config.TEMPLATE_12 -> GPSTemplateV2(context, null)
        Config.TEMPLATE_13 -> GPSTemplateV3(context, null)
        Config.TEMPLATE_14 -> GPSTemplateV4(context, null)
        Config.TEMPLATE_15 -> GPSTemplateV5(context, null)
        else -> {
            DailyTemplateV1(context, null)
        }
    }
    templateView.layoutParams = layoutParams
    templateView.setData(data, templateState)
    this.addView(templateView)
    if (Config.isGPSTemplate(type) && imageMap != null) {
        templateView.setMapImage(imageMap)
    }
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(key)
    }
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

fun Date.formatToDate(): String {
    val savedFormat = SharePrefManager.getString(
        FormatItem.DATE_FORMAT_KEY,
        FormatItem.DATE_FORMATS[0].id
    ) ?: FormatItem.DATE_FORMATS[0].id

    val dateFormat = SimpleDateFormat(savedFormat, Locale.getDefault())
    return dateFormat.format(this)
}

fun Date.formatToTime(): String {
    val timeFormatOption = SharePrefManager.getString(
        FormatItem.TIME_FORMAT_KEY,
        FormatItem.TIME_FORMATS[0].id
    ) ?: FormatItem.TIME_FORMATS[0].id

    val pattern =
        if (timeFormatOption == MyApplication.appContext.getString(R.string._12_hours)) "hh:mm a" else "HH:mm"
    val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return timeFormat.format(this)
}

fun Date.formatToDateTime(): String {
    return "${formatToDate()} ${formatToTime()}"
}

inline fun <reified T> Gson.fromJsonWithTypeToken(value: String): T {
    return this.fromJson(value, object : TypeToken<T>() {}.type)
}

inline fun <reified T> Gson.toJsonWithTypeToken(obj: T): String {
    return this.toJson(obj, object : TypeToken<T>() {}.type)
}
fun AppCompatActivity.showInterSplash(navAction: () -> Unit) {
    loadNativeFullScreen()
    var nativeDialog: NativeFullScreenDialog? = null
    var isNativeFail = false

    AdmobLib.loadAndShowInterstitialSplash(
        this,
        AdsManager.adSplashModel,
        timeout = 10000,
        onAdsShowed = {
            lifecycleScope.launch {
                delay(1000)
                nativeDialog = createNativeFullScreen(false, navAction) {
                    isNativeFail = true
                }
                nativeDialog?.show()
            }
        },
        onAdsFail = {
            navAction()
        },
        onAdsClose = {
            if (!AdsManager.isShowNativeFullScreen() || isNativeFail) {
                navAction()
            } else {
                nativeDialog?.isClosedOrFail = true
            }
        },
        onAdsLoaded = {
            Log.d("TAG===", "initAds: on ads loaded")
        }
    )
}
fun AppCompatActivity.createNativeFullScreen(
    isStartNow: Boolean = false,
    navAction: () -> Unit,
    onFailure: () -> Unit = navAction
): NativeFullScreenDialog? {
    if (!AdsManager.isShowNativeFullScreen()) return null

    return NativeFullScreenDialog(
        this@createNativeFullScreen,
        AdsManager.admobNativeFullScreenAfterInter,
        isStartNow,
        onClose = navAction,
        onFailure = onFailure
    )
}

fun AppCompatActivity.loadNativeFullScreen() {
    if (!AdsManager.isShowNativeFullScreen()) return

    AdmobLib.loadNative(
        this,
        AdsManager.admobNativeFullScreenAfterInter,
        size = GoogleENative.UNIFIED_FULL_SCREEN,
        mediaViewRatio = MediaAspectRatio.ANY
    )
}

fun AppCompatActivity.loadAndShowInterWithNativeAfter(
    interModel: AdmobInterModel,
    vShowInterAds: View?,
    isUpdateTime: Boolean = true,
    navAction: () -> Unit
) {
    vShowInterAds?.visible()
    loadNativeFullScreen()

    var nativeDialog: NativeFullScreenDialog? = null
    var isNativeFail = false

    AdmobLib.loadAndShowInterstitial(
        this,
        interModel,
        onAdsShowed = {
            lifecycleScope.launch {
                delay(1000)
                nativeDialog = createNativeFullScreen(
                    navAction = navAction,
                    onFailure = { isNativeFail = true }
                )
                nativeDialog?.show()
            }
        },
        onAdsFail = {
            navAction()
        },
        onAdsClose = {
            lifecycleScope.launch {
                if (!AdsManager.isShowNativeFullScreen() || isNativeFail) {
                    navAction()
                } else {
                    nativeDialog?.isClosedOrFail = true
                }
            }
        },
        onAdsCloseOrFailed = {
            if (isUpdateTime) AdsManager.lastInterShown = System.currentTimeMillis()
        }
    )
}