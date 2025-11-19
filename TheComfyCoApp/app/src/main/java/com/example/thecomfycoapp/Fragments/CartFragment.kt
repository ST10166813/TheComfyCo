package com.example.thecomfycoapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.CartAdapter
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.UserProductListActivity
import com.example.thecomfycoapp.models.CartItemModel
import com.example.thecomfycoapp.models.CartItemRequest
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvGrandTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabWishlist: FloatingActionButton

    private var cartList = mutableListOf<CartItemModel>()
    private var adapter: CartAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 These IDs are EXACTLY from your XML
        rvCartItems     = view.findViewById(R.id.rvCartItems)
        tvEmpty         = view.findViewById(R.id.tvMessgae)
        tvGrandTotal    = view.findViewById(R.id.tvCartGrandTotal)
        btnCheckout     = view.findViewById(R.id.btnCheckout)
        bottomNav       = view.findViewById(R.id.bottomNavigationView)
        fabWishlist     = view.findViewById(R.id.fabWishlist)

        setupBottomNav()
        setupRecycler()
        loadCartFromApi()
        updateUiState()
        setupCheckout()
        setupWishlist()
    }

    // -------- Helper: get saved token --------
    private fun getSavedToken(): String? {
        val prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    // -------- Bottom nav --------
    private fun setupBottomNav() {
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_menu_cart)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    findNavController().navigate(R.id.id_home_fragment)
                    true
                }
                R.id.cartFragment -> {
                    val intent = Intent(requireContext(), UserProductListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    findNavController().navigate(R.id.id_home_fragment)
                }
                R.id.cartFragment -> {
                    val intent = Intent(requireContext(), UserProductListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    // -------- Cart load from API --------
    private fun loadCartFromApi() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = getSavedToken()
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "You are not logged in!", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Ensure Retrofit uses this token
                RetrofitClient.setToken(token)

                val response = RetrofitClient.api.getCart()
                if (response.isSuccessful) {
                    val cartResponse = response.body()

                    cartList = cartResponse?.items?.map {
                        CartItemModel(
                            product = Product(
                                _id = it.productId,
                                name = it.name ?: "",
                                description = "",
                                price = it.price,
                                stock = 1,
                                variants = null,
                                images = listOfNotNull(it.image ?: "")
                            ),
                            qty = it.quantity,
                            size = null
                        )
                    }?.toMutableList() ?: mutableListOf()

                    adapter?.updateItems(cartList)
                    updateUiState()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(
                        requireContext(),
                        "Failed to load cart: ${response.code()} $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load cart: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // -------- RecyclerView --------
    private fun setupRecycler() {
        rvCartItems.layoutManager = LinearLayoutManager(requireContext())

        adapter = CartAdapter(
            items = cartList,
            onQtyChanged = { position, newQty ->
                if (position in cartList.indices) {
                    val item = cartList[position]
                    if (newQty <= 0) {
                        removeItemFromApi(item.product._id ?: "")
                    } else {
                        updateItemQtyInApi(item.product._id ?: "", newQty)
                    }
                }
            },
            onItemDeleted = { position ->
                if (position in cartList.indices) {
                    val item = cartList[position]
                    removeItemFromApi(item.product._id)
                }
            }
        )

        rvCartItems.adapter = adapter
    }

    // -------- API helpers --------
    private fun removeItemFromApi(productId: String?) {
        productId?.let { id ->
            viewLifecycleOwner.lifecycleScope.launch {
                val token = getSavedToken()
                if (token.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "You are not logged in!", Toast.LENGTH_LONG).show()
                    return@launch
                }

                RetrofitClient.setToken(token)

                val response = RetrofitClient.api.removeFromCart(id)
                if (response.isSuccessful) {
                    loadCartFromApi()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(
                        requireContext(),
                        "Failed to remove item: ${response.code()} $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateItemQtyInApi(productId: String, newQty: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val token = getSavedToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(requireContext(), "You are not logged in!", Toast.LENGTH_LONG).show()
                return@launch
            }

            RetrofitClient.setToken(token)

            try {
                // Option 1: just call addToCart with the new quantity (backend increments)
                val addReq = CartItemRequest(
                    productId = productId,
                    quantity = newQty
                )

                val addResponse = RetrofitClient.api.addToCart(addReq)
                if (addResponse.isSuccessful) {
                    loadCartFromApi()
                } else {
                    val err = addResponse.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(
                        requireContext(),
                        "Failed to update item: ${addResponse.code()} $err",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to update item: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // -------- UI state --------
    private fun updateUiState() {
        if (cartList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvCartItems.visibility = View.GONE
            btnCheckout.isEnabled = false
            tvGrandTotal.text = "Total: R 0.00"
        } else {
            tvEmpty.visibility = View.GONE
            rvCartItems.visibility = View.VISIBLE
            btnCheckout.isEnabled = true

            val grandTotal = cartList.sumOf { it.product.price * it.qty }
            tvGrandTotal.text = "Total: R ${String.format("%.2f", grandTotal)}"
        }
    }

    // -------- Checkout --------
    private fun setupCheckout() {
        btnCheckout.setOnClickListener {
            if (cartList.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalItems = cartList.sumOf { it.qty }
            val grandTotal = cartList.sumOf { it.product.price * it.qty }

            val bundle = Bundle().apply {
                putInt("total_items", totalItems)
                putDouble("grand_total", grandTotal)
            }

            findNavController().navigate(R.id.id_checkout_fragment, bundle)
        }
    }

    // -------- Wishlist --------
    private fun setupWishlist() {
        fabWishlist.setOnClickListener {
            Toast.makeText(requireContext(), "Wishlist coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
