package com.example.thecomfycoapp.offline

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_products")
data class OfflineProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val variants: String,
    val imagePath: String?,
    val timestamp: Long = System.currentTimeMillis()
)
