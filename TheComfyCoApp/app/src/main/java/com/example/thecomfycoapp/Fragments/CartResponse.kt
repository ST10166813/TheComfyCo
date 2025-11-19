package com.example.thecomfycoapp.Fragments

import com.example.thecomfycoapp.models.CartItemResponse

data class CartResponse(
    val userId: String,
    val items: List<CartItemResponse>
)
