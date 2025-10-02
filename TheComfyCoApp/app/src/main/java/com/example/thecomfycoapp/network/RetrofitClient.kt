package com.example.thecomfycoapp.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 1. Change to lateinit var. Initialize it later.
    lateinit var api: ApiService

    // Moved BASE_URL here for clarity
    private const val BASE_URL = "https://thecomfycoapi-1.onrender.com"
    private var token: String? = null

    // 2. setToken is fine, but should likely rebuild API if called later.

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

        // Load the token immediately
        token = prefs.getString("token", null)

        // 3. Build/rebuild the client and API only AFTER token is set
        buildApi()
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        // The interceptor will now use the most recently set 'token'
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }

    private fun buildApi() {
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Optional: If you update the token while the app is running, rebuild the API.
    fun setToken(newToken: String?) {
        token = newToken
        buildApi()
    }
}