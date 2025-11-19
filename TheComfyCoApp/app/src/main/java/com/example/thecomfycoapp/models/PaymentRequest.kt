package com.example.thecomfycoapp.models

data class PaymentRequest(
    val amount: Double,
    val customerName: String,
    val maskedCard: String? = null
)
