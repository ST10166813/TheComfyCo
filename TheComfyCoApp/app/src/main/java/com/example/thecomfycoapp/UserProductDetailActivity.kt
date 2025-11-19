package com.example.thecomfycoapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.models.CartItemRequest
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import kotlinx.coroutines.launch

class UserProductDetailActivity : AppCompatActivity() {

    private lateinit var imgProduct: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDescription: TextView
    private lateinit var chipGroupSizes: ChipGroup

    private lateinit var tvQtyLabel: TextView
    private lateinit var tvQty: TextView
    private lateinit var tvBottomPrice: TextView

    private lateinit var btnMinus: TextView
    private lateinit var btnPlus: TextView
    private lateinit var btnAddToCart: MaterialButton

    private var qty = 1
    private lateinit var product: Product
    private var unitPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_product_detail)

        val topBar = findViewById<MaterialToolbar>(R.id.topBar)
        setSupportActionBar(topBar)
        topBar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        topBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        imgProduct = findViewById(R.id.imgUserProduct)
        tvName = findViewById(R.id.tvUserProductName)
        tvPrice = findViewById(R.id.tvUserProductPrice)
        tvDescription = findViewById(R.id.tvUserProductDescription)
        chipGroupSizes = findViewById(R.id.chipGroupSizes)

        tvQtyLabel = findViewById(R.id.tvQtyLabel)
        tvQty = findViewById(R.id.tvQty)
        tvBottomPrice = findViewById(R.id.tvBottomPrice)

        btnMinus = findViewById(R.id.btnQtyMinus)
        btnPlus = findViewById(R.id.btnQtyPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        val productJson = intent.getStringExtra("product") ?: "{}"
        product = Gson().fromJson(productJson, Product::class.java)

        bindProduct(product)
        setupQuantity()
        setupAddToCart()
    }

    private fun bindProduct(product: Product) {
        tvName.text = product.name
        unitPrice = product.price
        tvPrice.text = "R ${String.format("%.2f", unitPrice)}"
        tvDescription.text = product.description
        tvBottomPrice.text = "R ${String.format("%.2f", unitPrice * qty)}"

        val raw = product.images?.firstOrNull()
        val imageUrl = if (!raw.isNullOrBlank()) {
            if (raw.startsWith("http", ignoreCase = true)) raw
            else {
                val base = RetrofitClient.BASE_URL.trimEnd('/')
                val path = raw.trimStart('/')
                "$base/$path"
            }
        } else null

        Glide.with(this)
            .load(imageUrl)
            .error(R.drawable.logo)
            .into(imgProduct)

        chipGroupSizes.removeAllViews()
        val variants = product.variants ?: emptyList()
        if (variants.isNotEmpty()) {
            variants.forEachIndexed { index, v ->
                val chip = Chip(this).apply {
                    text = v.size ?: "Size"
                    isCheckable = true
                    isClickable = true
                }
                chipGroupSizes.addView(chip)
                if (index == 0) chip.isChecked = true
            }
        } else {
            val chip = Chip(this).apply {
                text = "One Size"
                isCheckable = true
                isClickable = true
                isChecked = true
            }
            chipGroupSizes.addView(chip)
        }
    }

    private fun setupQuantity() {
        tvQty.text = qty.toString()

        btnMinus.setOnClickListener {
            if (qty > 1) {
                qty--
                tvQty.text = qty.toString()
                tvBottomPrice.text = "R ${String.format("%.2f", unitPrice * qty)}"
            }
        }

        btnPlus.setOnClickListener {
            qty++
            tvQty.text = qty.toString()
            tvBottomPrice.text = "R ${String.format("%.2f", unitPrice * qty)}"
        }
    }

    private fun selectedSize(): String {
        val checkedId = chipGroupSizes.checkedChipId
        val chip = chipGroupSizes.findViewById<Chip>(checkedId)
        return chip?.text?.toString() ?: ""
    }

    private fun setupAddToCart() {
        btnAddToCart.setOnClickListener {
            addItemToCart()
        }
    }

    private fun getSavedToken(): String? {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    private fun addItemToCart() {
        val size = selectedSize()

        lifecycleScope.launch {
            try {
                val request = CartItemRequest(
                    productId = product._id ?: "",
                    quantity = qty
                )
                val token = getSavedToken()
                if (token == null) {
                    Toast.makeText(this@UserProductDetailActivity, "You are not logged in!", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val response = RetrofitClient.api.addToCart("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(this@UserProductDetailActivity, "Added to cart", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(
                        this@UserProductDetailActivity,
                        "Failed: ${response.code()} $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@UserProductDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
