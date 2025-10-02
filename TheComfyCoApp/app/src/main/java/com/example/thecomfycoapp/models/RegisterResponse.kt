package com.example.thecomfycoapp.models

// Response after register
data class RegisterResponse(
    val userId: String,
    val token: String,
    val role : String

)