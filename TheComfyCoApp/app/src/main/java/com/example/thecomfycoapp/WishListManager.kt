package com.example.thecomfycoapp

import android.content.Context
import com.example.thecomfycoapp.models.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object WishlistManager {

    private const val PREFS_NAME = "wishlist_prefs"
    private const val KEY_WISHLIST = "wishlist_items"

    private val gson = Gson()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getWishlist(context: Context): MutableList<Product> {
        val json = prefs(context).getString(KEY_WISHLIST, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Product>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveWishlist(context: Context, list: MutableList<Product>) {
        prefs(context).edit()
            .putString(KEY_WISHLIST, gson.toJson(list))
            .apply()
    }

    fun addToWishlist(context: Context, product: Product) {
        val list = getWishlist(context)
        val id = product._id
        if (!id.isNullOrBlank() && list.any { it._id == id }) return // avoid duplicates
        list.add(product)
        saveWishlist(context, list)
    }

    fun removeFromWishlist(context: Context, productId: String) {
        val list = getWishlist(context)
        val newList = list.filterNot { it._id == productId }.toMutableList()
        saveWishlist(context, newList)
    }
}
