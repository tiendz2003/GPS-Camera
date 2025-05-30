package com.ssquad.gps.camera.geotag.data.models

import android.content.Context
import android.os.Parcelable
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.utils.CustomTemplateConfig
import kotlinx.parcelize.Parcelize

data class CustomTemplateModel(
    val id: String,
    val name: String,
    val icon: Int,
    var isSelected: Boolean = false,
    var isActive: Boolean = true
) {
    companion object {
        fun getCustomTemplates(context: Context) =
            arrayListOf(
                CustomTemplateModel(
                    CustomTemplateConfig.LOCATION,
                    context.getString(R.string.location),
                    R.drawable.ic_location
                ),
                CustomTemplateModel(
                    CustomTemplateConfig.LAT_LONG,
                    context.getString(R.string.latlng),
                    R.drawable.ic_latlong
                ),
                CustomTemplateModel(
                    CustomTemplateConfig.TIME,
                    context.getString(R.string.times),
                    R.drawable.ic_custom_time
                ),
                CustomTemplateModel(
                    CustomTemplateConfig.DATE,
                    context.getString(R.string.date),
                    R.drawable.ic_date
                ),

                )
    }
}

@Parcelize
data class TemplateDataModel(
    val location: String? = null?:"Loading...",
    val lat: String? = null?:"Loading...",
    val long: String? = null?:"Loading...",
    val temperatureC: Float? = null?:0F,
    val temperatureF: Float? = null?:0F,
    val currentTime: String? = null?:"Loading...",
    val currentDate: String? = null?:"Loading..."
) : Parcelable

data class TemplateState(
    val selectedTemplateId: Int? = null,
    val showLocation: Boolean = true,
    val showTemperature: Boolean = true,
    val showLatLong: Boolean = true,
    val showTime: Boolean = true,
    val showDate: Boolean = true
)