package com.example.thecomfycoapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var codeField: TextInputEditText
    private lateinit var newPasswordField: TextInputEditText
    private lateinit var resetBtn: MaterialButton
    private var email: String = ""
    private var otp: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        codeField = findViewById(R.id.otpField)
        newPasswordField = findViewById(R.id.newPasswordField)
        resetBtn = findViewById(R.id.resetPasswordBtn)

        email = intent.getStringExtra("email") ?: ""
        otp = intent.getStringExtra("otp") ?: ""

        resetBtn.setOnClickListener {
            val code = codeField.text.toString().trim()
            val newPassword = newPasswordField.text.toString().trim()

            if (code.isEmpty() || newPassword.isEmpty()) {
                toast("Enter all fields")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.resetPassword(
                        mapOf(
                            "email" to email,
                            "code" to code,
                            "newPassword" to newPassword
                        )
                    )

                    if (response.isSuccessful) {
                        toast("Password updated successfully")
                        finish() // close activity
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
