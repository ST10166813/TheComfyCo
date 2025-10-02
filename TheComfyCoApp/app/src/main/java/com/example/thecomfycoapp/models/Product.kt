package com.example.thecomfycoapp.models

data class Product(
    val _id: String? = null,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val variants: List<Variant>?,
    val images: List<String>? // 🔹 added for product images
)