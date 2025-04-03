package com.example.baseproject.utils

import com.example.baseproject.R
import com.example.baseproject.data.models.LanguageModel
import com.example.baseproject.utils.Constants.HAWK_LANGUAGE_POSITION
import com.example.baseproject.utils.Constants.HAWK_RATE_STAR
import com.orhanobut.hawk.Hawk

object Common {

    fun setSelectedLanguage(language: LanguageModel) {
        Hawk.put(HAWK_LANGUAGE_POSITION, language)
    }

    fun getSelectedLanguage(): LanguageModel {
        return Hawk.get(HAWK_LANGUAGE_POSITION, LanguageModel(R.drawable.ic_english, R.string.english, "en"))
    }

    fun setRateStar() {
        Hawk.put(HAWK_RATE_STAR, getRateStar() + 1)
    }

    fun getRateStar(): Int {
        return Hawk.get(HAWK_RATE_STAR, 0)
    }

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