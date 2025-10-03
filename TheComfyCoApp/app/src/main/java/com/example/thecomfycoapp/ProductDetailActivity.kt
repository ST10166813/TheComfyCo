package com.example.thecomfycoapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.thecomfycoapp.network.RetrofitClient // Assuming your Retrofit client is here
import com.example.thecomfycoapp.models.Product
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var product: Product
    private lateinit var etName: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var tvTotalStock: TextView
    private lateinit var tvVariantsList: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productJson = intent.getStringExtra("product") ?: run {
            Toast.makeText(this, "Product data missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI elements
        val imgProduct = findViewById<ImageView>(R.id.imgProductDetail)
        etName = findViewById(R.id.etProductDetailName)
        etPrice = findViewById(R.id.etProductDetailPrice)
        etDescription = findViewById(R.id.etProductDetailDescription)
        tvTotalStock = findViewById(R.id.tvTotalStock)
        tvVariantsList = findViewById(R.id.tvVariantsList)
        btnUpdate = findViewById(R.id.btnUpdateProduct)
        btnDelete = findViewById(R.id.btnDeleteProduct)

        // Parse and populate data
        product = Gson().fromJson(productJson, Product::class.java)
        populateData(imgProduct)

        // Set listeners
        btnUpdate.setOnClickListener { updateProduct() }
        btnDelete.setOnClickListener { deleteProduct() }
    }

    private fun populateData(imgProduct: ImageView) {
        // Populate editable fields
        etName.setText(product.name)
        etPrice.setText(String.format("%.2f", product.price))
        etDescription.setText(product.description)

        // Fallback to 0 if product.stock is null
        val totalStock = product.stock ?: 0

        tvTotalStock.text = "Total Stock: $totalStock units"


        // Display variants (simple string formatting)
        val variantText = product.variants?.joinToString("\n") { variant ->
            "${variant.size}, ${variant.color} (Stock: ${variant.stock})"
        } ?: "No specific variants listed."
        tvVariantsList.text = variantText

        val images = product.images
        // Load image
        if (!images.isNullOrEmpty()) { // Now the check is safe on the local 'images'
            val fullImageUrl = images[0]
            Glide.with(this)
                .load(fullImageUrl)
                .into(imgProduct)
        }
    }

    private fun updateProduct() {
        val updatedName = etName.text.toString()
        val updatedPrice = etPrice.text.toString().toFloatOrNull()
        val updatedDescription = etDescription.text.toString()

        if (updatedName.isBlank() || updatedPrice == null) {
            Toast.makeText(this, "Name and Price are required.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Create a map or a model representing the updated fields
        // Note: Variants/Stock are not updated here; they would require a complex UI/model update.
        val updateMap = mapOf(
            "name" to updatedName,
            "price" to updatedPrice.toString(),
            "description" to updatedDescription
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 2. You need to implement an updateProduct function in your ApiService
                // This function should send a PUT request to /api/products/{id} with the token
                val response = RetrofitClient.api.updateProduct(
                    product._id!!, // Assuming your product model has an ID field
                    updateMap
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "✅ Product updated!", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity and refresh the previous screen
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Update failed: HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Error updating product: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteProduct() {
        // Confirmation dialog is recommended before deleting!

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. You need to implement a deleteProduct function in your ApiService
                // This function should send a DELETE request to /api/products/{id} with the token
                val response = RetrofitClient.api.deleteProduct( product._id!!,)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "🗑️ Product deleted!", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity and refresh the previous screen
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Deletion failed: HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Error deleting product: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}