package com.example.thecomfycoapp

import android.app.Application
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.utils.LanguageManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 1. Apply saved language for the whole app
        LanguageManager.applySavedLanguage(this)

        // 2. Init Retrofit with correct context/locale
        RetrofitClient.init(this)

    }
}
