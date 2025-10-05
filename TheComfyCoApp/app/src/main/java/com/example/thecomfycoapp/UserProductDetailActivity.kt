package com.example.thecomfycoapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.gson.Gson

class UserProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_product_detail)

        val imgProduct = findViewById<ImageView>(R.id.imgUserProduct)
        val tvName = findViewById<TextView>(R.id.tvUserProductName)
        val tvPrice = findViewById<TextView>(R.id.tvUserProductPrice)
        val tvDescription = findViewById<TextView>(R.id.tvUserProductDescription)
        val tvVariants = findViewById<TextView>(R.id.tvUserProductVariants)

        val productJson = intent.getStringExtra("product")
        val product = Gson().fromJson(productJson, Product::class.java)

        tvName.text = product.name
        tvPrice.text = "R ${String.format("%.2f", product.price)}"
        tvDescription.text = product.description

        val variantsText = product.variants?.joinToString("\n") {
            "• ${it.size}  "
        } ?: "No variants available."
        tvVariants.text = variantsText

        val imageUrl = if (!product.images.isNullOrEmpty()) {
            val base = RetrofitClient.BASE_URL.trimEnd('/')
            val path = product.images.firstOrNull()?.trimStart('/')
            "$base/$path"
        } else null

        Glide.with(this)
            .load(imageUrl)
            .error(R.drawable.logo)
            .into(imgProduct)
    }
}