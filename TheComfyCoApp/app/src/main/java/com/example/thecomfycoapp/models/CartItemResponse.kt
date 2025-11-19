package com.example.thecomfycoapp.models

data class CartItemResponse(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val image: String?
)
