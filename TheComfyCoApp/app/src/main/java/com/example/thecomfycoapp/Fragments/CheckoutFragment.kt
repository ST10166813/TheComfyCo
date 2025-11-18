package com.example.thecomfycoapp.Fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.models.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var tvOrderSummary: TextView
    private lateinit var etCardName: TextInputEditText
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etExpiry: TextInputEditText
    private lateinit var etCvv: TextInputEditText
    private lateinit var btnPay: MaterialButton

    private val PREFS_NAME = "cart_prefs"
    private val KEY_CART_ITEMS = "cart_items"

    private var cartList: MutableList<CartItemModel> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOrderSummary = view.findViewById(R.id.tvOrderSummary)
        etCardName = view.findViewById(R.id.etCardName)
        etCardNumber = view.findViewById(R.id.etCardNumber)
        etExpiry = view.findViewById(R.id.etExpiry)
        etCvv = view.findViewById(R.id.etCvv)
        btnPay = view.findViewById(R.id.btnPay)

        loadCart()
        updateSummary()

        btnPay.setOnClickListener { submitOrder() }
    }

    // Load Cart
    private fun loadCart() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CART_ITEMS, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<CartItemModel>>() {}.type
        cartList = Gson().fromJson(json, type)
    }

    private fun updateSummary() {
        val totalItems = cartList.sumOf { it.qty }
        val totalPrice = cartList.sumOf { it.product.price * it.qty }

        tvOrderSummary.text = "$totalItems items • Total: R ${String.format("%.2f", totalPrice)}"
    }

    private fun clearCart() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CART_ITEMS).apply()
    }

    // Submit Order
    private fun submitOrder() {

        val name = etCardName.text.toString().trim()
        val cardNumber = etCardNumber.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (cardNumber.length < 4) {
            Toast.makeText(requireContext(), "Invalid card number", Toast.LENGTH_SHORT).show()
            return
        }

        val mask = "**** **** **** " + cardNumber.takeLast(4)

        val items = cartList.map {
            OrderItemRequest(
                productId = it.product._id ?: "",
                productName = it.product.name ?: "Unknown",
                size = it.size,
                quantity = it.qty,
                unitPrice = it.product.price,
                lineTotal = it.product.price * it.qty
            )
        }

        val order = OrderRequest(
            items = items,
            totalItems = items.sumOf { it.quantity },
            grandTotal = items.sumOf { it.lineTotal },
            customerName = name,
            maskedCard = mask
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.placeOrder(order)

                if (response.isSuccessful) {
                    clearCart()
                    Toast.makeText(requireContext(), "Payment Successful!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.id_order_confirmation_fragment)
                } else {
                    Toast.makeText(requireContext(), "Order failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
