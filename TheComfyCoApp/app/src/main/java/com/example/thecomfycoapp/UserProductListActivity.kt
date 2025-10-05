package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProductListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_product_list)

        recyclerView = findViewById(R.id.recyclerViewUserProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchProductsForUsers()
    }

    private fun fetchProductsForUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ✅ Fetch list directly (not Response)
                val products: List<Product> = RetrofitClient.api.getProducts()

                withContext(Dispatchers.Main) {
                    if (products.isNotEmpty()) {
                        recyclerView.adapter = ProductAdapter(products) { product ->
                            // Go to detail page
                            val intent = Intent(this@UserProductListActivity, UserProductDetailActivity::class.java)
                            intent.putExtra("product", Gson().toJson(product))
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@UserProductListActivity, "No products available", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserProductListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}