package com.example.thecomfycoapp.offline

import android.content.Context
import com.example.thecomfycoapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

object OfflineSyncManager {

    suspend fun syncProducts(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.offlineProductDao()

        val pending = dao.getAll()
        if (pending.isEmpty()) return@withContext

        for (p in pending) {
            try {

                val nameRB = RequestBody.create("text/plain".toMediaTypeOrNull(), p.name)
                val descRB = RequestBody.create("text/plain".toMediaTypeOrNull(), p.description)
                val priceRB = RequestBody.create("text/plain".toMediaTypeOrNull(), p.price.toString())
                val stockRB = RequestBody.create("text/plain".toMediaTypeOrNull(), p.stock.toString())
                val variantsRB = RequestBody.create("text/plain".toMediaTypeOrNull(), p.variants)

                val imagePart = p.imagePath?.let { path ->
                    val file = java.io.File(path)
                    val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("image", file.name, reqFile)
                }

                RetrofitClient.api.createProduct(
                    nameRB, descRB, priceRB, stockRB, variantsRB, imagePart
                )

            } catch (e: Exception) {
                // if fails, stop syncing
                continue
            }
        }

        dao.clear()
    }
}
