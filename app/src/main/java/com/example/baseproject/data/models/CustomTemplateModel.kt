package com.example.baseproject.data.models

import android.content.Context
import com.example.baseproject.R
import com.example.baseproject.utils.CustomTemplateConfig

data class CustomTemplateModel(val id: String, val name: String, val icon: Int, var isSelected: Boolean = false, var isActive: Boolean = true) {
    companion object {
        fun getCustomTemplates(context: Context) =
            arrayListOf<CustomTemplateModel>(
              /*  CustomTemplateModel(
                    CustomTemplateConfig.MAP,
                    context.getString(R.string.map),
                    R.drawable.ic_location,
                    true
                ),*/
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
                    context.getString(R.string.time),
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