package com.example.thecomfycoapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.models.OrderResponse
import com.example.thecomfycoapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ManageOrdersActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_orders)

        // Initialise Retrofit with auth token from SharedPreferences
        RetrofitClient.init(applicationContext)

        rvOrders = findViewById(R.id.rvOrders)
        tvEmpty = findViewById(R.id.tvEmptyOrders)
        progressBar = findViewById(R.id.progressOrders)

        rvOrders.layoutManager = LinearLayoutManager(this)
        adapter = OrdersAdapter()
        rvOrders.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        rvOrders.visibility = View.GONE

        // Optional: show nice message if no token
        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token.isNullOrEmpty()) {
            progressBar.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Please log in as an admin to view orders."
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getOrders()

                val orders: List<OrderResponse> = if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    // Optional: examine status code
                    val msg = when (response.code()) {
                        401 -> "Not authorised – please log in again."
                        403 -> "Access forbidden – this account is not an admin."
                        else -> "Failed to load orders. (Code ${response.code()})"
                    }

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = msg
                        Toast.makeText(
                            this@ManageOrdersActivity,
                            msg,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return@launch
                }

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (orders.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "No orders yet."
                        rvOrders.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvOrders.visibility = View.VISIBLE
                        adapter.submitList(orders)
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Server error while loading orders."
                    Toast.makeText(
                        this@ManageOrdersActivity,
                        "HTTP error: ${e.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Failed to load orders."
                    Toast.makeText(
                        this@ManageOrdersActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
