package com.example.thecomfycoapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log // ⬅️ Make sure you have this import!

object InternetCheck {

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: run {
            Log.d("InternetCheck", "isOnline result: false (No active network)")
            return false
        }
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: run {
            Log.d("InternetCheck", "isOnline result: false (No network capabilities)")
            return false
        }

        // Store the result in a variable
        val isConnected = when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        // Log the result before returning
        Log.d("InternetCheck", "isOnline result: $isConnected")

        // Return the stored result
        return isConnected
    }
}