//package com.example.thecomfycoapp
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//
//
//class AdminDashboard : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_admin_dashboard)
//
//        val addProductBtn = findViewById<Button>(R.id.addProductBtn)
//        addProductBtn.setOnClickListener {
//            startActivity(Intent(this, AddProductActivity::class.java))
//            val addProductBtn = findViewById<Button>(R.id.addProductBtn)
//            addProductBtn.setOnClickListener {
//                startActivity(Intent(this, AddProductActivity::class.java))
//            }
//
//            val viewProductBtn = findViewById<Button>(R.id.viewproductbtn)
//            viewProductBtn.setOnClickListener {
//                startActivity(Intent(this, ProductsActivity::class.java))
//            }
//
//
//        }
//
//}}

package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
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

        // Actions
        addProductBtn.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }
        viewProductBtn.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        manageOrdersBtn.setOnClickListener {
            Toast.makeText(this, "Orders module coming soon ✨", Toast.LENGTH_SHORT).show()
        }
        refreshBtn.setOnClickListener {
            loadDashboard()
        }

        // Initial load
        loadDashboard()

        val logoutBtn: MaterialButton = findViewById(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            // Clear user session or Firebase auth, if any
            // Example: FirebaseAuth.getInstance().signOut()

            // Go back to LoginActivity
            val intent = Intent(this, AuthenicationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    private fun loadDashboard(lowStockThreshold: Int = 5) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = RetrofitClient.api.getProducts()

                val totalProducts = products.size
                val totalStock = products.sumOf { it.stock ?: 0 }
                val lowStock = products.count { (it.stock ?: 0) in 0..lowStockThreshold }

                // Build a simple recent list (names of last few products)
                // If your API doesn’t return dates, we just show last 5 in current order
                val recentList = products.takeLast(5).reversed().joinToString("\n") { "• ${it.name}" }
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
