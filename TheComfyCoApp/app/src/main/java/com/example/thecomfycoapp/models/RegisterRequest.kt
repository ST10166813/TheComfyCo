package com.example.thecomfycoapp.models

// Request for register/login
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)
