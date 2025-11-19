package com.example.thecomfycoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient

class WishlistAdapter(
    private var items: MutableList<Product>,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgWishlistItem)
        val tvName: TextView = itemView.findViewById(R.id.tvWishlistItemName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvWishlistItemPrice)
        val tvDescription: TextView = itemView.findViewById(R.id.tvWishlistItemDescription)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnWishlistDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist_product, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val product = items[position]

        holder.tvName.text = product.name ?: "Product"
        holder.tvPrice.text = "R ${String.format("%.2f", product.price)}"
        holder.tvDescription.text = product.description ?: ""

        // Load image
        val raw = product.images?.firstOrNull()
        val imageUrl = if (!raw.isNullOrBlank()) {
            if (raw.startsWith("http", ignoreCase = true)) raw
            else {
                val base = RetrofitClient.BASE_URL.trimEnd('/')
                val path = raw.trimStart('/')
                "$base/$path"
            }
        } else null

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .error(R.drawable.logo)
            .into(holder.img)

        holder.btnDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: MutableList<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
