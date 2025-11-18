package com.example.thecomfycoapp.models

data class OrderItemRequest(
    val productId: String,
    val productName: String,
    val size: String?,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

data class OrderRequest(
    val items: List<OrderItemRequest>,
    val totalItems: Int,
    val grandTotal: Double,
    val customerName: String,
    val maskedCard: String?
)

// What the backend returns for admin / confirmation list
data class OrderResponse(
    val id: String,
    val customerName: String?,
    val status: String?,
    val totalItems: Int,
    val grandTotal: Double,
    val createdAt: String?,
    val items: List<OrderItemResponse>?
)

data class OrderItemResponse(
    val productId: String?,
    val productName: String?,
    val size: String?,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)
