package com.ssquad.gps.camera.geotag.data.models

import android.os.Parcelable
import com.ssquad.gps.camera.geotag.utils.Config
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
                    id = Config.TEMPLATE_1,
                    image ="file:///android_asset/theme/daily_theme_1.webp",
                    type = TemplateType.DAILY,
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_2,
                    image ="file:///android_asset/theme/daily_theme_2.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_3,
                    image ="file:///android_asset/theme/daily_theme_3.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_4,
                    image ="file:///android_asset/theme/daily_theme_4.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_5,
                    image ="file:///android_asset/theme/daily_theme_5.webp",
                    type = TemplateType.DAILY
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_6,
                    image ="file:///android_asset/theme/travel_theme_1.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_7,
                    image ="file:///android_asset/theme/travel_theme_2.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_8,
                    image ="file:///android_asset/theme/travel_theme_3.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_9,
                    image ="file:///android_asset/theme/travel_theme_4.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_10,
                    image ="file:///android_asset/theme/travel_theme_5.webp",
                    type = TemplateType.TRAVEL
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_11,
                    image ="file:///android_asset/theme/gps_theme_1.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_12,
                    image ="file:///android_asset/theme/gps_theme_2.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_13,
                    image ="file:///android_asset/theme/gps_theme_3.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_14,
                    image ="file:///android_asset/theme/gps_theme_4.webp",
                    type = TemplateType.GPS
                ),
                ThemeTemplateModel(
                    id = Config.TEMPLATE_15,
                    image ="file:///android_asset/theme/gps_theme_5.webp",
                    type = TemplateType.GPS
                )

            )
            return listTemplate
        }
        fun getThemeById(id: String): ThemeTemplateModel? {
            return getTemplate().find { it.id == id }
        }
    }
}