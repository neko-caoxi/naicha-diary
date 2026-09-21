package com.naicha.diary.data

import android.content.Context
import android.content.SharedPreferences

object AppSettings {

    private const val FILE = "drink_diary_prefs"
    private const val KEY_API = "deepseek_api_key"
    private const val KEY_AUTO = "auto_recognize"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    var apiKey: String
        get() = if (::prefs.isInitialized) prefs.getString(KEY_API, "").orEmpty() else ""
        set(value) {
            if (::prefs.isInitialized) prefs.edit().putString(KEY_API, value.trim()).apply()
        }

    var autoRecognize: Boolean
        get() = if (::prefs.isInitialized) prefs.getBoolean(KEY_AUTO, false) else false
        set(value) {
            if (::prefs.isInitialized) prefs.edit().putBoolean(KEY_AUTO, value).apply()
        }

    val hasApiKey: Boolean get() = apiKey.isNotBlank()
}
