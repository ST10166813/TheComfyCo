package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.offline.OfflineSyncManager
import com.example.thecomfycoapp.utils.InternetCheck
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboard : AppCompatActivity() {

    private lateinit var tvStatProducts: TextView
    private lateinit var tvStatStock: TextView
    private lateinit var tvStatLowStock: TextView
    private lateinit var tvRecent: TextView

    private lateinit var addProductBtn: Button
    private lateinit var viewProductBtn: Button
    private lateinit var manageOrdersBtn: Button
    private lateinit var refreshBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)

        // Views
        tvStatProducts = findViewById(R.id.tvStatProducts)
        tvStatStock = findViewById(R.id.tvStatStock)
        tvStatLowStock = findViewById(R.id.tvStatLowStock)
        tvRecent = findViewById(R.id.tvRecent)

        addProductBtn = findViewById(R.id.addProductBtn)
        viewProductBtn = findViewById(R.id.viewproductbtn)
        manageOrdersBtn = findViewById(R.id.manageOrdersBtn)
        refreshBtn = findViewById(R.id.refreshBtn)

        // Navigate to screens
        addProductBtn.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }
        viewProductBtn.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        manageOrdersBtn.setOnClickListener {
            Toast.makeText(this, "Orders module coming soon ✨", Toast.LENGTH_SHORT).show()
        }
        refreshBtn.setOnClickListener { loadDashboard() }

        // Initial load
        loadDashboard()

        val logoutBtn: MaterialButton = findViewById(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            startActivity(
                Intent(this, AuthenicationActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }

        // FCM token registration
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCMToken", fcmToken ?: "No token received")
                val jwtToken = getSharedPreferences("auth", MODE_PRIVATE).getString("token", null)
                if (!jwtToken.isNullOrEmpty() && fcmToken != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val response = RetrofitClient.api.saveDeviceToken(
                                authHeader = "Bearer $jwtToken",
                                body = mapOf("token" to fcmToken)
                            )
                            Log.d(
                                "FCMToken",
                                if (response.isSuccessful) "Token registered successfully"
                                else "Failed: ${response.code()} ${response.message()}"
                            )
                        } catch (e: Exception) {
                            Log.e("FCMToken", "Exception sending token", e)
                        }
                    }
                }
            } else {
                Log.e("FCMToken", "Failed to get FCM token", task.exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (InternetCheck.isOnline(this@AdminDashboard)) {
                OfflineSyncManager.syncProducts(this@AdminDashboard)
            }
        }
    }

    private fun loadDashboard(lowStockThreshold: Int = 5) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val products = RetrofitClient.api.getProducts()
                val totalProducts = products.size
                val totalStock = products.sumOf { it.stock ?: 0 }
                val lowStock = products.count { (it.stock ?: 0) <= lowStockThreshold }
                val recentList = products.takeLast(5)
                    .reversed()
                    .joinToString("\n") { "• ${it.name}" }
                    .ifBlank { "• No recent actions yet." }

                withContext(Dispatchers.Main) {
                    tvStatProducts.text = totalProducts.toString()
                    tvStatStock.text = totalStock.toString()
                    tvStatLowStock.text = lowStock.toString()
                    tvRecent.text = recentList
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboard, "Failed to load: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
