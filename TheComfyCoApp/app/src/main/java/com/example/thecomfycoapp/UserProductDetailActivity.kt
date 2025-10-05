package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson

class UserProductDetailActivity : AppCompatActivity() {

    private lateinit var imgProduct: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDescription: TextView
    private lateinit var chipGroupSizes: ChipGroup
    private lateinit var tvQty: TextView
    private lateinit var tvBottomPrice: TextView
    private lateinit var btnMinus: MaterialButton
    private lateinit var btnPlus: MaterialButton
    private lateinit var btnAddToCart: MaterialButton

    private var qty = 1
    private lateinit var product: Product
    private var unitPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_product_detail)

        // Toolbar with back
        val topBar = findViewById<MaterialToolbar>(R.id.topBar)
        setSupportActionBar(topBar)
        topBar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        topBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        imgProduct = findViewById(R.id.imgUserProduct)
        tvName = findViewById(R.id.tvUserProductName)
        tvPrice = findViewById(R.id.tvUserProductPrice)
        tvDescription = findViewById(R.id.tvUserProductDescription)
        chipGroupSizes = findViewById(R.id.chipGroupSizes)
        tvQty = findViewById(R.id.tvQty)
        tvBottomPrice = findViewById(R.id.tvBottomPrice)
        btnMinus = findViewById(R.id.btnQtyMinus)
        btnPlus = findViewById(R.id.btnQtyPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        // Receive product from Intent
        val productJson = intent.getStringExtra("product") ?: "{}"
        product = Gson().fromJson(productJson, Product::class.java)

        bindProduct(product)
        setupQuantity()
        setupAddToCart()
    }

    private fun bindProduct(product: Product) {
        tvName.text = product.name
        unitPrice = (product.price ?: 0.0)
        tvPrice.text = "R ${String.format("%.2f", unitPrice)}"
        tvDescription.text = product.description ?: ""
        tvBottomPrice.text = "R ${String.format("%.2f", unitPrice * qty)}"

        // Image
        val imageUrl = if (!product.images.isNullOrEmpty()) {
            val base = RetrofitClient.BASE_URL.trimEnd('/')
            val path = product.images.firstOrNull()?.trimStart('/')
            "$base/$path"
        } else null

        Glide.with(this)
            .load(imageUrl)
            .error(R.drawable.logo)
            .into(imgProduct)

        // Sizes as single-select chips
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
            // If no variants, create a single "One Size" chip
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

    private fun selectedSizeText(): String {
        val checkedId = chipGroupSizes.checkedChipId
        val chip = chipGroupSizes.findViewById<Chip>(checkedId)
        return chip?.text?.toString() ?: ""
    }

    private fun setupAddToCart() {
        btnAddToCart.setOnClickListener {
            // TODO: add item to your cart storage if you have one (Room/Singleton/etc.)
            // For now, jump to the Cart page in your HomeActivity (which hosts CartFragment)
            val i = Intent(this, HomeActivity::class.java).apply {
                putExtra("open_fragment", "cart")   // handle this extra in HomeActivity
                putExtra("from_detail", true)
            }
            startActivity(i)
            finish()
        }
    }
}
