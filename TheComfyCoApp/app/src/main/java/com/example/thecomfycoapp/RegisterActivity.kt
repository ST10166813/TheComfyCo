package com.example.thecomfycoapp

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.thecomfycoapp.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val nameField = findViewById<EditText>(R.id.fullNameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmPasswordField)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Create Account
        btnCreateAccount.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(name, email, password, confirmPassword) { response, error ->
                runOnUiThread {
                    if (error != null) {
                        Toast.makeText(this, "Register failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                        saveToken(response?.token, response?.role)
                        finish()
                    }
                }
            }
        }
    }

    private fun saveToken(token: String?, role: String?) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val editor = prefs.edit()
        token?.let { editor.putString("token", it) }
        role?.let { editor.putString("role", it) }
        editor.apply()
    }
}
