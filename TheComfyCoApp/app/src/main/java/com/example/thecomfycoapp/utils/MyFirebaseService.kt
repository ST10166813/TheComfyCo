package com.example.thecomfycoapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.thecomfycoapp.AdminDashboard
import com.example.thecomfycoapp.HomeActivity
import com.example.thecomfycoapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {

        val title = message.notification?.title ?: message.data["title"] ?: "Notification"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new alert"

        // Custom routing (admin vs normal user)
        val target = if (message.data["role"] == "admin") {
            AdminDashboard::class.java
        } else {
            HomeActivity::class.java
        }

        sendNotification(title, body, target)
    }

    private fun sendNotification(title: String, body: String, target: Class<*>) {

        val channelId = "comfyco_notifications"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ComfyCo Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, target)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.notification)   // 🔥 FIXED ICON
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notif)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Optionally send token here too
    }
}
