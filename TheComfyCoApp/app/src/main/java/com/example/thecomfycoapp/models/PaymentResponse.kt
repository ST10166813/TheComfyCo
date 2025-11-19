package com.example.thecomfycoapp.models

data class PaymentResponse(
    val success: Boolean,
    val message: String?,
    val totalPaid: Double?
)

