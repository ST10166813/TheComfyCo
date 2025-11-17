package com.example.thecomfycoapp.models

data class CartItemModel(
    val product: Product,
    var qty: Int,
    val size: String?
)
