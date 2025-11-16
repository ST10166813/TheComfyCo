package com.example.thecomfycoapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun saveLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun applyLocale(context: Context): Context {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val lang = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        val locale = Locale(lang)

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
