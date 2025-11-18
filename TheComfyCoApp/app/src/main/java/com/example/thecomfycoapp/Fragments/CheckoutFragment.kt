package com.example.thecomfycoapp.Fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.models.CartItemModel
import com.example.thecomfycoapp.models.OrderItemRequest
import com.example.thecomfycoapp.models.OrderRequest
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var tvOrderSummary: TextView
    private lateinit var etCardName: TextInputEditText
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etExpiry: TextInputEditText
    private lateinit var etCvv: TextInputEditText
    private lateinit var btnPay: MaterialButton

    private val PREFS_NAME = "cart_prefs"
    private val KEY_CART_ITEMS = "cart_items"

    private var cartList = mutableListOf<CartItemModel>()
    private var isUpdatingCardNumber = false
    private var isUpdatingExpiry = false

    private val TAG = "CheckoutFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure Retrofit has the current token from SharedPreferences
        RetrofitClient.init(requireContext().applicationContext)

        tvOrderSummary = view.findViewById(R.id.tvOrderSummary)
        etCardName = view.findViewById(R.id.etCardName)
        etCardNumber = view.findViewById(R.id.etCardNumber)
        etExpiry = view.findViewById(R.id.etExpiry)
        etCvv = view.findViewById(R.id.etCvv)
        btnPay = view.findViewById(R.id.btnPay)

        loadCart()
        buildSummary()
        setupInputMasks()
        setupPayButton()
    }

    // ----------------- CART -----------------

    private fun loadCart() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CART_ITEMS, "[]") ?: "[]"
        val type = object : TypeToken<MutableList<CartItemModel>>() {}.type
        cartList = Gson().fromJson(json, type)
        Log.d(TAG, "Loaded cart items: ${cartList.size}")
    }

    private fun buildSummary() {
        if (cartList.isEmpty()) {
            tvOrderSummary.text = "Your cart is empty."
            btnPay.isEnabled = false
            return
        }

        val totalItems = cartList.sumOf { it.qty }
        val grandTotal = cartList.sumOf { it.product.price * it.qty }
        tvOrderSummary.text =
            "$totalItems item(s) • Total: R ${String.format("%.2f", grandTotal)}"

        Log.d(TAG, "Summary -> items: $totalItems, total: $grandTotal")
    }

    // ----------------- INPUT MASKS -----------------

    private fun setupInputMasks() {
        setupCardNumberMask()
        setupExpiryMask()
    }

    private fun setupCardNumberMask() {
        etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingCardNumber) return
                isUpdatingCardNumber = true

                val rawDigits = s.toString().replace(" ", "").take(16) // max 16 digits
                val groups = rawDigits.chunked(4)
                val formatted = groups.joinToString(" ")

                if (formatted != s.toString()) {
                    etCardNumber.setText(formatted)
                    etCardNumber.setSelection(formatted.length)
                }

                isUpdatingCardNumber = false
            }
        })
    }

    private fun setupExpiryMask() {
        etExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingExpiry) return
                isUpdatingExpiry = true

                val digits = s.toString()
                    .filter { it.isDigit() }
                    .take(4) // MMYY

                val formatted = when {
                    digits.length <= 2 -> digits
                    else -> digits.substring(0, 2) + " / " + digits.substring(2)
                }

                if (formatted != s.toString()) {
                    etExpiry.setText(formatted)
                    etExpiry.setSelection(formatted.length)
                }

                isUpdatingExpiry = false
            }
        })
    }

    // ----------------- PAY BUTTON → CREATE ORDER -----------------

    private fun setupPayButton() {
        btnPay.setOnClickListener {
            val name = etCardName.text?.toString()?.trim().orEmpty()
            val number = etCardNumber.text?.toString()?.trim().orEmpty()
            val expiry = etExpiry.text?.toString()?.trim().orEmpty()
            val cvv = etCvv.text?.toString()?.trim().orEmpty()

            // Basic validations
            if (name.isEmpty() || number.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please complete all card details.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (cartList.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPay.isEnabled = false

            val totalItems = cartList.sumOf { it.qty }
            val grandTotal = cartList.sumOf { it.product.price * it.qty }

            // Build order items
            val orderItems = cartList.map { item ->
                OrderItemRequest(
                    productId = item.product._id ?: "",
                    productName = item.product.name ?: "",
                    size = item.size,
                    quantity = item.qty,
                    unitPrice = item.product.price,
                    lineTotal = item.product.price * item.qty
                )
            }

            // Masked card - just last 4 digits
            val last4 = number.filter { it.isDigit() }.takeLast(4)

            val orderRequest = OrderRequest(
                items = orderItems,
                totalItems = totalItems,
                grandTotal = grandTotal,
                customerName = name,
                maskedCard = if (last4.isNotEmpty()) "**** **** **** $last4" else null
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val errorMessage = createOrderOnServer(orderRequest)

                if (errorMessage == null) {
                    // Success -> clear cart & navigate
                    val prefs = requireContext()
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().remove(KEY_CART_ITEMS).apply()

                    try {
                        findNavController().navigate(R.id.id_order_confirmation_fragment)
                    } catch (e: Exception) {
                        Log.e(TAG, "Navigation to confirmation failed", e)
                        Toast.makeText(
                            requireContext(),
                            "Order placed, but couldn't open confirmation screen.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    btnPay.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Calls backend to create an order.
     * Returns null on success, or an error message string on failure.
     */
    private suspend fun createOrderOnServer(order: OrderRequest): String? =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.createOrder(order)
                if (response.isSuccessful) {
                    Log.d(TAG, "Order created: ${response.body()}")
                    null
                } else {
                    val code = response.code()
                    val errorBody: ResponseBody? = response.errorBody()
                    val errorText = try {
                        errorBody?.string()
                    } catch (e: Exception) {
                        null
                    }

                    Log.e(TAG, "createOrder failed. HTTP $code, body=$errorText")
                    "Could not place order (code $code)."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while creating order", e)
                "Something went wrong while placing your order. Please try again."
            }
        }
}
