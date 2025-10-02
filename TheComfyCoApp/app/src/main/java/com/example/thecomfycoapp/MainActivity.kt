package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginBtn = findViewById<Button>(R.id.loginbtn)   // LOGIN button
        val registerBtn = findViewById<Button>(R.id.registerbtn) // CREATE ACCOUNT button

// When LOGIN clicked → go to LoginActivity
        loginBtn.setOnClickListener {
            val intent = Intent(this, AuthenicationActivity::class.java)
            startActivity(intent)
        }

        // When REGISTER clicked → go to RegisterActivity
        registerBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}