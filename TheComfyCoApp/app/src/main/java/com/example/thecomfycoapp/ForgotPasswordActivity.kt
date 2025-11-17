package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailField: TextInputEditText
    private lateinit var sendBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailField = findViewById(R.id.forgotEmailField)
        sendBtn = findViewById(R.id.sendResetBtn)

        sendBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                toast("Enter your email")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.forgotPassword(
                        mapOf("email" to email)
                    )

                    if (response.isSuccessful) {
                        val otp = response.body()?.code ?: ""
                        toast("Reset code: $otp") // show OTP in-app

                        // Go to ResetPasswordActivity
                        startActivity(
                            Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                                .putExtra("email", email)
                                .putExtra("otp", otp)
                        )

                    } else {
                        toast("Error: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    toast("Network error: ${e.message}")
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
