//package com.example.thecomfycoapp.Fragments
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.thecomfycoapp.CartAdapter
//import com.example.thecomfycoapp.R
//import com.example.thecomfycoapp.UserProductListActivity
//import com.example.thecomfycoapp.models.CartItemModel
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//
//class CartFragment : Fragment(R.layout.fragment_cart) {
//
//    private lateinit var rvCartItems: RecyclerView
//    private lateinit var tvEmpty: TextView
//    private lateinit var tvGrandTotal: TextView
//    private lateinit var btnCheckout: MaterialButton
//    private lateinit var bottomNav: BottomNavigationView
//    private lateinit var fabWishlist: FloatingActionButton
//
//    private val PREFS_NAME = "cart_prefs"
//    private val KEY_CART_ITEMS = "cart_items"
//
//    private var cartList = mutableListOf<CartItemModel>()
//    private var adapter: CartAdapter? = null
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // ----- Bind views -----
//        rvCartItems = view.findViewById(R.id.rvCartItems)
//        tvEmpty = view.findViewById(R.id.tvMessgae)
//        tvGrandTotal = view.findViewById(R.id.tvCartGrandTotal)
//        btnCheckout = view.findViewById(R.id.btnCheckout)
//        bottomNav = view.findViewById(R.id.bottomNavigationView)
//        fabWishlist = view.findViewById(R.id.fabWishlist)
//
//        setupBottomNav()
//        loadCart()
//        setupRecycler()
//        updateUiState()
//        setupCheckout()
//        setupWishlist()
//    }
//
//    // ---------------- Bottom nav: Home + View Products ----------------
//
//    private fun setupBottomNav() {
//        // Use the cart-specific menu
//        bottomNav.menu.clear()
//        bottomNav.inflateMenu(R.menu.bottom_menu_cart)
//
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                // Left icon → HomeFragment
//                R.id.homeFragment -> {
//                    findNavController().navigate(R.id.id_home_fragment)
//                    true
//                }
//                // Right icon (was "settings") → open UserProductListActivity
//                R.id.cartFragment -> {
//                    val intent = Intent(requireContext(), UserProductListActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
//                else -> false
//            }
//        }
//
//        bottomNav.setOnItemReselectedListener { item ->
//            when (item.itemId) {
//                R.id.homeFragment -> {
//                    findNavController().navigate(R.id.id_home_fragment)
//                }
//                R.id.cartFragment -> {
//                    val intent = Intent(requireContext(), UserProductListActivity::class.java)
//                    startActivity(intent)
//                }
//            }
//        }
//    }
//
//    // ---------------- Cart data ----------------
//
//    private fun loadCart() {
//        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val json = prefs.getString(KEY_CART_ITEMS, "[]") ?: "[]"
//        val type = object : TypeToken<MutableList<CartItemModel>>() {}.type
//        cartList = Gson().fromJson(json, type)
//    }
//
//    private fun saveCart() {
//        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        prefs.edit()
//            .putString(KEY_CART_ITEMS, Gson().toJson(cartList))
//            .apply()
//    }
//
//    // ---------------- RecyclerView ----------------
//
//    private fun setupRecycler() {
//        rvCartItems.layoutManager = LinearLayoutManager(requireContext())
//
//        adapter = CartAdapter(
//            items = cartList,
//            onQtyChanged = { position, newQty ->
//                if (position in cartList.indices) {
//                    val item = cartList[position]
//                    if (newQty <= 0) {
//                        cartList.removeAt(position)
//                    } else {
//                        item.qty = newQty
//                    }
//                    saveCart()
//                    adapter?.notifyDataSetChanged()
//                    updateUiState()
//                }
//            },
//            onItemDeleted = { position ->
//                if (position in cartList.indices) {
//                    cartList.removeAt(position)
//                    saveCart()
//                    adapter?.notifyDataSetChanged()
//                    updateUiState()
//                }
//            }
//        )
//
//        rvCartItems.adapter = adapter
//    }
//
//    // ---------------- UI state (empty vs items + total) ----------------
//
//    private fun updateUiState() {
//        if (cartList.isEmpty()) {
//            tvEmpty.visibility = View.VISIBLE
//            rvCartItems.visibility = View.GONE
//            btnCheckout.isEnabled = false
//            tvGrandTotal.text = "Total: R 0.00"
//        } else {
//            tvEmpty.visibility = View.GONE
//            rvCartItems.visibility = View.VISIBLE
//            btnCheckout.isEnabled = true
//
//            val grandTotal = cartList.sumOf { (it.product.price ?: 0.0) * it.qty }
//            tvGrandTotal.text = "Total: R ${String.format("%.2f", grandTotal)}"
//        }
//    }
//
//    // ---------------- Buttons ----------------
//
//    private fun setupCheckout() {
//        btnCheckout.setOnClickListener {
//            // Navigate to the CheckoutFragment
//            findNavController().navigate(R.id.id_checkout_fragment)
//        }
//    }
//
//    private fun setupWishlist() {
//        fabWishlist.setOnClickListener {
//            Toast.makeText(requireContext(), "Wishlist coming soon", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
//
//package com.example.thecomfycoapp.Fragments
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.thecomfycoapp.CartAdapter
//import com.example.thecomfycoapp.R
//import com.example.thecomfycoapp.UserProductListActivity
//import com.example.thecomfycoapp.models.CartItemModel
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//
//class CartFragment : Fragment(R.layout.fragment_cart) {
//
//    private lateinit var rvCartItems: RecyclerView
//    private lateinit var tvEmpty: TextView
//    private lateinit var tvGrandTotal: TextView
//    private lateinit var btnCheckout: MaterialButton
//    private lateinit var bottomNav: BottomNavigationView
//    private lateinit var fabWishlist: FloatingActionButton
//
//    private val PREFS_NAME = "cart_prefs"
//    private val KEY_CART_ITEMS = "cart_items"
//
//    private var cartList = mutableListOf<CartItemModel>()
//    private var adapter: CartAdapter? = null
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        rvCartItems = view.findViewById(R.id.rvCartItems)
//        tvEmpty = view.findViewById(R.id.tvMessgae)
//        tvGrandTotal = view.findViewById(R.id.tvCartGrandTotal)
//        btnCheckout = view.findViewById(R.id.btnCheckout)
//        bottomNav = view.findViewById(R.id.bottomNavigationView)
//        fabWishlist = view.findViewById(R.id.fabWishlist)
//
//        setupBottomNav()
//        loadCart()
//        setupRecycler()
//        updateUiState()
//        setupCheckout()
//        setupWishlist()
//    }
//
//    private fun setupBottomNav() {
//        bottomNav.menu.clear()
//        bottomNav.inflateMenu(R.menu.bottom_menu_cart)
//
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.homeFragment -> {
//                    findNavController().navigate(R.id.id_home_fragment)
//                    true
//                }
//                R.id.cartFragment -> {
//                    val intent = Intent(requireContext(), UserProductListActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
//                else -> false
//            }
//        }
//
//        bottomNav.setOnItemReselectedListener { item ->
//            when (item.itemId) {
//                R.id.homeFragment -> {
//                    findNavController().navigate(R.id.id_home_fragment)
//                }
//                R.id.cartFragment -> {
//                    val intent = Intent(requireContext(), UserProductListActivity::class.java)
//                    startActivity(intent)
//                }
//            }
//        }
//    }
//
//    private fun loadCart() {
//        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val json = prefs.getString(KEY_CART_ITEMS, "[]") ?: "[]"
//        val type = object : TypeToken<MutableList<CartItemModel>>() {}.type
//        cartList = Gson().fromJson(json, type)
//    }
//
//    private fun saveCart() {
//        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        prefs.edit()
//            .putString(KEY_CART_ITEMS, Gson().toJson(cartList))
//            .apply()
//    }
//
//    private fun setupRecycler() {
//        rvCartItems.layoutManager = LinearLayoutManager(requireContext())
//
//        adapter = CartAdapter(
//            items = cartList,
//            onQtyChanged = { position, newQty ->
//                if (position in cartList.indices) {
//                    val item = cartList[position]
//                    if (newQty <= 0) {
//                        cartList.removeAt(position)
//                    } else {
//                        item.qty = newQty
//                    }
//                    saveCart()
//                    adapter?.notifyDataSetChanged()
//                    updateUiState()
//                }
//            },
//            onItemDeleted = { position ->
//                if (position in cartList.indices) {
//                    cartList.removeAt(position)
//                    saveCart()
//                    adapter?.notifyDataSetChanged()
//                    updateUiState()
//                }
//            }
//        )
//
//        rvCartItems.adapter = adapter
//    }
//
//    private fun updateUiState() {
//        if (cartList.isEmpty()) {
//            tvEmpty.visibility = View.VISIBLE
//            rvCartItems.visibility = View.GONE
//            btnCheckout.isEnabled = false
//            tvGrandTotal.text = "Total: R 0.00"
//        } else {
//            tvEmpty.visibility = View.GONE
//            rvCartItems.visibility = View.VISIBLE
//            btnCheckout.isEnabled = true
//
//            val grandTotal = cartList.sumOf { it.product.price * it.qty }
//            tvGrandTotal.text = "Total: R ${String.format("%.2f", grandTotal)}"
//        }
//    }
//
//    private fun setupCheckout() {
//        btnCheckout.setOnClickListener {
//            findNavController().navigate(R.id.id_checkout_fragment)
//        }
//    }
//
//    private fun setupWishlist() {
//        fabWishlist.setOnClickListener {
//            Toast.makeText(requireContext(), "Wishlist coming soon", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
package com.example.thecomfycoapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.CartAdapter
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.UserProductListActivity
import com.example.thecomfycoapp.models.CartItemModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvGrandTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabWishlist: FloatingActionButton

    private val PREFS_NAME = "cart_prefs"
    private val KEY_CART_ITEMS = "cart_items"

    private var cartList = mutableListOf<CartItemModel>()
    private var adapter: CartAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCartItems = view.findViewById(R.id.rvCartItems)
        tvEmpty = view.findViewById(R.id.tvMessgae)
        tvGrandTotal = view.findViewById(R.id.tvCartGrandTotal)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        bottomNav = view.findViewById(R.id.bottomNavigationView)
        fabWishlist = view.findViewById(R.id.fabWishlist)

        setupBottomNav()
        loadCart()
        setupRecycler()
        updateUiState()
        setupCheckout()
        setupWishlist()
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

    // -------- Cart load/save --------
    private fun loadCart() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CART_ITEMS, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<CartItemModel>>() {}.type
        cartList = Gson().fromJson(json, type)
    }

    private fun saveCart() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CART_ITEMS, Gson().toJson(cartList))
            .apply()
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
                        cartList.removeAt(position)
                    } else {
                        item.qty = newQty
                    }
                    saveCart()
                    adapter?.notifyDataSetChanged()
                    updateUiState()
                }
            },
            onItemDeleted = { position ->
                if (position in cartList.indices) {
                    cartList.removeAt(position)
                    saveCart()
                    adapter?.notifyDataSetChanged()
                    updateUiState()
                }
            }
        )

        rvCartItems.adapter = adapter
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

            // 👉 Pass only what the checkout UI needs
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
