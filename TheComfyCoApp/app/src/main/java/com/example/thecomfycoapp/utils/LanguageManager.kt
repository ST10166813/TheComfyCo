package com.example.thecomfycoapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageManager {

    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_LANGUAGE = "app_lang"

    /** Read saved language tag (e.g. "en", "zu", "af") */
    fun getSavedLanguageTag(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    /** Apply whatever is currently saved to the whole app */
    fun applySavedLanguage(context: Context) {
        val langTag = getSavedLanguageTag(context)
        val locales = LocaleListCompat.forLanguageTags(langTag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** Change and persist language, then apply it */
    fun changeLanguage(context: Context, langTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langTag).apply()

        val locales = LocaleListCompat.forLanguageTags(langTag)
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
