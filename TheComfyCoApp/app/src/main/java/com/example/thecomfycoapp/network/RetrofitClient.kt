//package com.example.thecomfycoapp.network
//
//import android.content.Context
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object RetrofitClient {
//
//    lateinit var api: ApiService
//
//    // NOTE the trailing slash (recommended by Retrofit)
//    const val BASE_URL = "https://thecomfycoapi-1.onrender.com"
//
//    private var token: String? = null
//
//    fun init(context: Context) {
//        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
//        token = prefs.getString("token", null)
//        buildApi()
//    }
//
//    private val authInterceptor = Interceptor { chain ->
//        val requestBuilder = chain.request().newBuilder()
//        token?.let {
//            if (it.isNotBlank()) {
//                requestBuilder.addHeader("Authorization", "Bearer $it")
//            }
//        }
//        chain.proceed(requestBuilder.build())
//    }
//
//    private fun buildApi() {
//        val client = OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .build()
//
//        api = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
//
//    /** Call this immediately after a successful login/logout to refresh the header */
//    fun setToken(newToken: String?) {
//        token = newToken
//        buildApi()
//    }
//}

package com.example.thecomfycoapp.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    lateinit var api: ApiService

    // MUST end with a "/"
    const val BASE_URL = "https://thecomfycoapi-1.onrender.com/"

    private var token: String? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        token = prefs.getString("token", null)
        buildApi()
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            if (it.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        }
        chain.proceed(requestBuilder.build())
    }

    private fun buildApi() {
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        api = Retrofit.Builder()
            .baseUrl(BASE_URL)   // now with trailing slash
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /** Call this immediately after a successful login/logout to refresh the header */
    fun setToken(newToken: String?) {
        token = newToken
        buildApi()
    }
}
