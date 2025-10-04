//package com.example.thecomfycoapp
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.thecomfycoapp.models.Product
//import com.example.thecomfycoapp.network.RetrofitClient
//import com.example.thecomfycoapp.R // Make sure R is imported
//
//class ProductAdapter(
//    private val products: List<Product>,
//    private val onItemClick: (Product) -> Unit
//) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
//
//    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
//        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
//        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
//        val tvStock: TextView = itemView.findViewById(R.id.tvProductStock) // ⬅️ ADDED
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_product, parent, false)
//        return ProductViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
//        val product = products[position]
//
//        // Bind Text Data
//        holder.tvName.text = product.name
//        holder.tvPrice.text = "R ${String.format("%.2f", product.price)}"
//        holder.tvStock.text = "Stock: ${product.stock ?: 0} units" // ⬅️ ADDED STOCK BINDING
//
//
//        // Load first image using Glide with BASE_URL
//        if (!product.images.isNullOrEmpty()) {
//            val imagePath = product.images.firstOrNull()
//            if (!imagePath.isNullOrEmpty()) {
//                // Ensure exactly one slash between BASE_URL and path
//                val imageUrl = if (imagePath.startsWith("/")) {
//                    RetrofitClient.BASE_URL + imagePath
//                } else {
//                    RetrofitClient.BASE_URL + "/" + imagePath
//                }
//
//                Glide.with(holder.itemView.context)
//                    .load(imageUrl)
//                    .error(R.drawable.logo) // fallback if image fails
//                    .into(holder.imgProduct)
//            } else {
//                holder.imgProduct.setImageResource(R.drawable.logo) // fallback
//            }
//        } else {
//            holder.imgProduct.setImageResource(R.drawable.logo) // fallback
//        }
//
//
//        // Handle click
//        holder.itemView.setOnClickListener { onItemClick(product) }
//    }
//
//
//    override fun getItemCount(): Int = products.size
//}

package com.example.thecomfycoapp

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.R

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvStock: TextView = itemView.findViewById(R.id.tvProductStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Bind Text Data
        holder.tvName.text = product.name
        holder.tvPrice.text = "R ${String.format("%.2f", product.price)}"
        holder.tvStock.text = "Stock: ${product.stock ?: 0} units"

        // Load image safely
        if (!product.images.isNullOrEmpty()) {
            val rawPath = product.images.firstOrNull()
            if (!rawPath.isNullOrBlank()) {
                //  Build safe image URL
                val base = RetrofitClient.BASE_URL.trimEnd('/')
                val path = rawPath.trimStart('/')
                val imageUrl = "$base/$path"

                Log.d("ProductAdapter", "Loading image: $imageUrl")

                // Load image with Glide (no cache for debugging)
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)  // temporary for debug
                    .skipMemoryCache(true)                      // temporary for debug
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
                        ): Boolean {
                            Log.e("GlideFail", "Image failed: $imageUrl, error=${e?.rootCauses?.firstOrNull()?.message}")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
                        ): Boolean {
                            Log.d("GlideSuccess", "Loaded: $imageUrl")
                            return false
                        }
                    })
                    .error(R.drawable.logo) // fallback if load fails
                    .into(holder.imgProduct)
            } else {
                holder.imgProduct.setImageResource(R.drawable.logo)
            }
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo)
        }

        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount(): Int = products.size
}
