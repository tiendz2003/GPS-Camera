package com.example.baseproject.utils

object Config {
    const val TEMPLATE_1 = "TEMPLATE_1"
    const val TEMPLATE_2 = "TEMPLATE_2"
    const val TEMPLATE_3 = "TEMPLATE_3"
    const val TEMPLATE_4 = "TEMPLATE_4"
    const val TEMPLATE_5 = "TEMPLATE_5"
    const val TEMPLATE_6 = "TEMPLATE_6"
    const val TEMPLATE_7 = "TEMPLATE_7"
    const val TEMPLATE_8 = "TEMPLATE_8"
    const val TEMPLATE_9 = "TEMPLATE_9"
    const val TEMPLATE_10 = "TEMPLATE_10"
    const val TEMPLATE_11 = "TEMPLATE_11"
    const val TEMPLATE_12 = "TEMPLATE_12"
    const val TEMPLATE_13 = "TEMPLATE_13"
    const val TEMPLATE_14 = "TEMPLATE_14"
    const val TEMPLATE_15 = "TEMPLATE_15"

    fun isGPSTemplate(templateId: String?): Boolean {
        return templateId in listOf(
            TEMPLATE_11,
            TEMPLATE_12,
            TEMPLATE_13,
            TEMPLATE_14,
            TEMPLATE_15
        )
    }
}