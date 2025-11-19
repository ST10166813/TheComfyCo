package com.example.thecomfycoapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class WishlistActivity : AppCompatActivity() {

    private lateinit var rvWishlist: RecyclerView
    private lateinit var adapter: WishlistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        val topBar = findViewById<MaterialToolbar>(R.id.topBarWishlist)
        setSupportActionBar(topBar)
        topBar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        topBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        rvWishlist = findViewById(R.id.rvWishlist)
        rvWishlist.layoutManager = LinearLayoutManager(this)

        val initialList = WishlistManager.getWishlist(this)

        adapter = WishlistAdapter(
            items = initialList,
            onDeleteClick = { product ->
                removeFromWishlist(product)
            }
        )

        rvWishlist.adapter = adapter

        // show toast if we arrived from the heart icon
        if (intent.getBooleanExtra("show_added_toast", false)) {
            Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show()
            intent.removeExtra("show_added_toast")
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedList = WishlistManager.getWishlist(this)
        adapter.updateData(updatedList)
    }

    private fun removeFromWishlist(product: com.example.thecomfycoapp.models.Product) {
        val id = product._id
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Unable to remove item", Toast.LENGTH_SHORT).show()
            return
        }
        WishlistManager.removeFromWishlist(this, id)
        val updatedList = WishlistManager.getWishlist(this)
        adapter.updateData(updatedList)
        Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show()
    }
}
