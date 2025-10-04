package com.example.thecomfycoapp

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.models.Variant
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var etNewStockQuantity: TextInputEditText
    private lateinit var tvTotalStock: TextView
    private lateinit var tvVariantsList: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnUpdateStock: Button
    private lateinit var btnManageVariants: Button
    private lateinit var imgProduct: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Views
        imgProduct = findViewById(R.id.imgProductDetail)
        etName = findViewById(R.id.etProductDetailName)
        etPrice = findViewById(R.id.etProductDetailPrice)
        etDescription = findViewById(R.id.etProductDetailDescription)
        tvTotalStock = findViewById(R.id.tvTotalStock)
        tvVariantsList = findViewById(R.id.tvVariantsList)
        btnUpdate = findViewById(R.id.btnUpdateProduct)
        btnDelete = findViewById(R.id.btnDeleteProduct)
        btnUpdateStock = findViewById(R.id.btnUpdateStock)
        etNewStockQuantity = findViewById(R.id.etNewStockQuantity)
        btnManageVariants = findViewById(R.id.btnManageVariants)

        // Product from intent
        val productJson = intent.getStringExtra("product")
        if (productJson.isNullOrBlank()) {
            Toast.makeText(this, "Product data missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        product = Gson().fromJson(productJson, Product::class.java)

        populateData()

        btnUpdate.setOnClickListener { updateProductDetails() }
        btnDelete.setOnClickListener { deleteProduct() }
        btnUpdateStock.setOnClickListener { updateProductStock() }
        btnManageVariants.setOnClickListener { showEditVariantsDialog() }
    }

    private fun buildImageUrlOrNull(): String? {
        val raw = product.images?.firstOrNull() ?: return null
        if (raw.isBlank()) return null
        val base = RetrofitClient.BASE_URL.trimEnd('/')
        val path = raw.trimStart('/')
        return "$base/$path"
    }

    private fun populateData() {
        etName.setText(product.name)
        etPrice.setText(String.format("%.2f", product.price))
        etDescription.setText(product.description)

        val totalStock = product.stock ?: 0
        tvTotalStock.text = "Current Stock: $totalStock units"
        etNewStockQuantity.setText(totalStock.toString())

        val variantText = product.variants?.joinToString("\n") { v ->
            val colorText = v.color?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""
            "${v.size}$colorText (Stock: ${v.stock ?: 0})"
        } ?: "No variants listed."
        tvVariantsList.text = variantText

        val url = buildImageUrlOrNull()
        if (url != null) {
            Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // temp, avoids stale 404
                .skipMemoryCache(true)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
                    ): Boolean {
                        Log.e("ProductDetail", "Image load failed: $url, err=${e?.rootCauses?.firstOrNull()?.message}")
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
                    ): Boolean {
                        Log.d("ProductDetail", "Loaded: $url")
                        return false
                    }
                })
                .error(R.drawable.logo)
                .into(imgProduct)
        } else {
            imgProduct.setImageResource(R.drawable.logo)
        }
    }

    private fun showEditVariantsDialog() {
        val variants = product.variants ?: run {
            Toast.makeText(this, "No variants to manage.", Toast.LENGTH_SHORT).show()
            return
        }

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }
        scrollView.addView(layout)

        val stockEditTexts = mutableMapOf<Variant, TextInputEditText>()

        variants.forEach { variant ->
            val tvName = TextView(this).apply {
                val colorText = variant.color?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""
                text = "${variant.size}$colorText (Current: ${variant.stock ?: 0})"
                setPadding(0, 16, 0, 4)
            }
            layout.addView(tvName)

            val etStock = TextInputEditText(this).apply {
                setText((variant.stock ?: 0).toString())
                inputType = InputType.TYPE_CLASS_NUMBER
            }
            val tilStock = TextInputLayout(this).apply {
                hint = "New Stock"
                addView(etStock)
            }
            layout.addView(tilStock)
            stockEditTexts[variant] = etStock
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Variant Stock")
            .setView(scrollView)
            .setPositiveButton("Save") { _, _ ->
                saveAllVariantsAndRefresh(stockEditTexts)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAllVariantsAndRefresh(stockEditTexts: Map<Variant, TextInputEditText>) {
        val updatedVariants = product.variants!!.toMutableList()
        var totalNewStock = 0

        stockEditTexts.forEach { (variant, etStock) ->
            val newStock = etStock.text.toString().toIntOrNull()
            if (newStock == null || newStock < 0) {
                Toast.makeText(this, "Invalid stock value.", Toast.LENGTH_LONG).show()
                return
            }
            val index = updatedVariants.indexOfFirst { it.size == variant.size && it.color == variant.color }
            if (index != -1) {
                updatedVariants[index] = updatedVariants[index].copy(stock = newStock)
                totalNewStock += newStock
            }
        }

        // Update local product and UI, then push to server
        product = product.copy(variants = updatedVariants, stock = totalNewStock)
        populateData()
        updateProductDetails()
    }

    private fun updateProductDetails() {
        val updatedName = etName.text.toString()
        val updatedPrice = etPrice.text.toString().toDoubleOrNull()
        val updatedDescription = etDescription.text.toString()

        if (updatedName.isBlank() || updatedPrice == null) {
            Toast.makeText(this, "Name and Price are required.", Toast.LENGTH_SHORT).show()
            return
        }

        // Preserve existing fields (variants, images, stock, etc.)
        val updatedProduct = product.copy(
            name = updatedName,
            price = updatedPrice,
            description = updatedDescription
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.updateProduct(product._id!!, updatedProduct)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        product = response.body() ?: updatedProduct
                        populateData()
                        Toast.makeText(this@ProductDetailActivity, "✅ Product updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        val body = response.errorBody()?.string()
                        Toast.makeText(this@ProductDetailActivity, "Update failed: ${response.code()} - $body", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateProductStock() {
        val newStock = etNewStockQuantity.text.toString().toIntOrNull()
        if (newStock == null || newStock < 0) {
            Toast.makeText(this, "Invalid stock value.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedProduct = product.copy(stock = newStock)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.updateProduct(product._id!!, updatedProduct)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        product = response.body() ?: updatedProduct
                        populateData()
                        Toast.makeText(this@ProductDetailActivity, "✅ Stock updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        val body = response.errorBody()?.string()
                        Toast.makeText(this@ProductDetailActivity, "Stock update failed: ${response.code()} - $body", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteProduct() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.deleteProduct(product._id!!)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "🗑️ Product deleted!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Log.e("DELETE", "Code: ${response.code()}, Error: $errorMsg")
                        Toast.makeText(this@ProductDetailActivity, "Delete failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
