package com.example.baseproject.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ThemeTemplateModel(
    val id: String,
    val image: String,
    var isSelected: Boolean = false,
    val type: TemplateType
) : Parcelable{
    companion object{
        fun getTemplate():List<ThemeTemplateModel>{
            val listTemplate = arrayListOf(
                ThemeTemplateModel(
                    id = "1",
                    image ="file:///android_asset/theme/daily_theme_1.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = "2",
                    image ="file:///android_asset/theme/daily_theme_2.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = "3",
                    image ="file:///android_asset/theme/daily_theme_3.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = "4",
                    image ="file:///android_asset/theme/daily_theme_4.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = "5",
                    image ="file:///android_asset/theme/daily_theme_5.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = "6",
                    image ="file:///android_asset/theme/travel_theme_1.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = "7",
                    image ="file:///android_asset/theme/travel_theme_2.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = "8",
                    image ="file:///android_asset/theme/travel_theme_3.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = "9",
                    image ="file:///android_asset/theme/travel_theme_4.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = "10",
                    image ="file:///android_asset/theme/travel_theme_5.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = "11",
                    image ="file:///android_asset/theme/gps_theme_1.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = "12",
                    image ="file:///android_asset/theme/gps_theme_2.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = "13",
                    image ="file:///android_asset/theme/gps_theme_3.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = "14",
                    image ="file:///android_asset/theme/gps_theme_4.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = "15",
                    image ="file:///android_asset/theme/gps_theme_5.webp",
                    type = TemplateType.GPS
                )

            )
            return listTemplate
        }
    }
}