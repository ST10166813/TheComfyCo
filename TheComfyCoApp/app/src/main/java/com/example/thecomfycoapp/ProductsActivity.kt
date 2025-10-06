package com.example.thecomfycoapp

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val gson by lazy { Gson() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        // ===== Toolbar with Back Button =====
        val toolbar = findViewById<MaterialToolbar>(R.id.topBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ===== RecyclerView Setup =====
        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(SpacesItemDecoration(16))

        // ===== Load Products =====
        lifecycleScope.launch {
            try {
                val products = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getProducts()
                }

                recyclerView.adapter = ProductAdapter(products) { product ->
                    val intent = Intent(this@ProductsActivity, ProductDetailActivity::class.java)
                    intent.putExtra("product", gson.toJson(product))
                    startActivity(intent)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@ProductsActivity,
                    "Oops, couldn’t load products: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

/** Adds even spacing around grid items (in dp). */
class SpacesItemDecoration(private val spaceDp: Int) : RecyclerView.ItemDecoration() {
    private fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val s = spaceDp.dpToPx(parent.context)
        outRect.set(s, s, s, s)
    }
}
