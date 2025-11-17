package com.example.thecomfycoapp.offline

import androidx.room.*

@Dao
interface OfflineProductDao {

    @Insert
    suspend fun insert(product: OfflineProduct)

    @Query("SELECT * FROM offline_products ORDER BY timestamp ASC")
    suspend fun getAll(): List<OfflineProduct>

    @Query("DELETE FROM offline_products")
    suspend fun clear()
}
