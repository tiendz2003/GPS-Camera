package com.example.baseproject.data.models

import android.content.Context
import android.os.Parcelable
import com.example.baseproject.R
import com.example.baseproject.utils.CustomTemplateConfig
import kotlinx.parcelize.Parcelize

data class CustomTemplateModel(val id: String, val name: String, val icon: Int, var isSelected: Boolean = false, var isActive: Boolean = true) {
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
    val location: String? = null,
    val lat: String? = null,
    val long: String? = null,
    val temperature: String? = null,
    val currentTime: String? = null,
    val currentDate: String? = null
):Parcelable{
    companion object{
        fun getDefaultTemplateData(): TemplateDataModel {
            return TemplateDataModel(
                location = "Hanoi",
                lat = "21.0285",
                long = "105.8542",
                temperature = "25°C",
                currentTime = "12:00 PM",
                currentDate = "01/01/2025"
            )
        }
    }
}
data class TemplateState(
    val selectedTemplateId: Int? = null,
    val showLocation: Boolean = true,
    val showTemperature: Boolean = true,
    val showLatLong: Boolean = true,
    val showTime: Boolean = true,
    val showDate: Boolean = true
)