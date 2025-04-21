package com.example.baseproject.data.models

import com.example.baseproject.MyApplication
import com.example.baseproject.R

data class MapType(
    val id: Int,
    val name: String,
    val type: Int,
    val thumbnailRes: Int
)
data class FormatItem(
    val id: String,
    var isSelected: Boolean = false
){
    companion object{
        const val DATE_FORMAT_KEY = "date_format"
        const val TIME_FORMAT_KEY = "time_format"

        val DATE_FORMATS = listOf(
            FormatItem("dd/MM/yyyy",),
            FormatItem("MM/dd/yyyy",),
            FormatItem("yyyy/MM/dd")
        )

        val TIME_FORMATS = listOf(
            FormatItem(MyApplication.appContext.getString(R.string._12_hours)),
            FormatItem(MyApplication.appContext.getString(R.string._24_hours))
        )
    }
}