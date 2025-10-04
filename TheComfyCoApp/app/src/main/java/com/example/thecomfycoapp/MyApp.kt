package com.example.thecomfycoapp

import android.app.Application
import com.example.thecomfycoapp.network.RetrofitClient

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load saved token (if any) and build the Retrofit client with the auth interceptor.
        RetrofitClient.init(this)
    }
}
