package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.utils.LanguageManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language first so the whole activity loads in that locale
        LanguageManager.applySavedLanguage(this)

        // Optional splash screen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init Retrofit
        RetrofitClient.init(applicationContext)

        // Buttons
        val loginBtn = findViewById<Button>(R.id.loginbtn)
        val registerBtn = findViewById<Button>(R.id.registerbtn)

        loginBtn.setOnClickListener {
            startActivity(Intent(this, AuthenicationActivity::class.java))
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
