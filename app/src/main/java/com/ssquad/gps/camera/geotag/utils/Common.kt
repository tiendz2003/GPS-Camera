package com.ssquad.gps.camera.geotag.utils

import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.data.models.LanguageModel
import com.ssquad.gps.camera.geotag.utils.Constants.LANGUAGE_POSITION
import com.ssquad.gps.camera.geotag.utils.Constants.RATE_STAR

object Common {

    /*fun setSelectedLanguage(language: LanguageModel) {
        Hawk.put(, language)
    }

    fun getSelectedLanguage(): LanguageModel {
        return Hawk.get(HAWK_LANGUAGE_POSITION, LanguageModel(R.drawable.ic_english, R.string.english, "en"))
    }*/
    var languageSelected: LanguageModel
        get() {
            return SharePrefManager.get(
                LANGUAGE_POSITION,
                LanguageModel(R.drawable.ic_english, R.string.english, "en")
            )
        }
        set(value) {
            SharePrefManager.put(LANGUAGE_POSITION, value)
        }
    var rateStar: Int
        get() {
            return SharePrefManager.get(RATE_STAR, 0)
        }
        set(value) {
            SharePrefManager.put(RATE_STAR, value + 1)
        }
    /*fun setRateStar() {
        Hawk.put(HAWK_RATE_STAR, getRateStar() + 1)
    }

    fun getRateStar(): Int {
        return Hawk.get(HAWK_RATE_STAR, 0)
    }*/

    fun getLanguageList(): MutableList<LanguageModel> {
        val languageList = mutableListOf<LanguageModel>()
        languageList.add(LanguageModel(R.drawable.ic_english, R.string.english, "en"))
        languageList.add(LanguageModel(R.drawable.ic_hindi, R.string.hindi, "hi"))
        languageList.add(LanguageModel(R.drawable.ic_spanish, R.string.spanish, "es"))
        languageList.add(LanguageModel(R.drawable.ic_french, R.string.french, "fr"))
        languageList.add(LanguageModel(R.drawable.ic_arabic, R.string.arabic, "ar"))
        languageList.add(LanguageModel(R.drawable.ic_bengali, R.string.bengali, "bn"))
        languageList.add(LanguageModel(R.drawable.ic_russian, R.string.russian, "ru"))
        languageList.add(LanguageModel(R.drawable.ic_portuguese, R.string.portuguese, "pt"))
        languageList.add(LanguageModel(R.drawable.ic_indonesian, R.string.indonesian, "in"))
        languageList.add(LanguageModel(R.drawable.ic_german, R.string.german, "de"))
        languageList.add(LanguageModel(R.drawable.ic_italian, R.string.italian, "it"))
        languageList.add(LanguageModel(R.drawable.ic_korean, R.string.korean, "ko"))
        return languageList
    }

}