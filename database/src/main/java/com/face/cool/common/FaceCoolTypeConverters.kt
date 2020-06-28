package com.face.cool.common

import androidx.room.TypeConverter
import com.face.cool.databasa.users.ErrorStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class FaceCoolTypeConverters {

    @TypeConverter
    fun fromCountryLangList(countryLang: List<Long>?): String? {
        if (countryLang == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Long?>?>() {}.type
        return gson.toJson(countryLang, type)
    }

    @TypeConverter
    fun toCountryLangList(countryLangString: String?): List<Long>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Long?>?>() {}.type
        return gson.fromJson(countryLangString, type)
    }

    @TypeConverter
    fun fromErroToJson(countryLang: List<ErrorStatus>?): String? {
        if (countryLang == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<ErrorStatus?>?>() {}.type
        return gson.toJson(countryLang, type)
    }

    @TypeConverter
    fun toErrorLangList(countryLangString: String?): List<ErrorStatus?>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<ErrorStatus?>?>() {}.type
        return gson.fromJson(countryLangString, type)
    }

}

