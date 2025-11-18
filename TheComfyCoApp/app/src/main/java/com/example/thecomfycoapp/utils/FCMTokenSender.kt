package com.example.thecomfycoapp.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FCMTokenSender {

    fun sendToken(context: Context, lifecycle: androidx.lifecycle.LifecycleCoroutineScope) {

        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = prefs.getString("token", null) ?: return

        RetrofitClient.setToken(jwtToken)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            lifecycle.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.api.saveDeviceToken(mapOf("token" to token))
                    Log.d("FCM", "Token sync result: ${response.code()}")
                } catch (e: Exception) {
                    Log.e("FCM", "Error sending token", e)
                }
            }
        }
    }
}
