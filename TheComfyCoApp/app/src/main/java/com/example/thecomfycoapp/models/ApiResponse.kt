package com.example.thecomfycoapp.models

data class ApiResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val error: String? = null,
    val code: String? = null
)
