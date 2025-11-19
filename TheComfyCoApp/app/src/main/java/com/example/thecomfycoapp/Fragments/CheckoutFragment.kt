package com.example.thecomfycoapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.models.PaymentRequest
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private lateinit var tvOrderSummary: TextView
    private lateinit var etCardName: TextInputEditText
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etExpiry: TextInputEditText
    private lateinit var etCvv: TextInputEditText
    private lateinit var btnPay: MaterialButton

    private var totalItems: Int = 0
    private var grandTotal: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOrderSummary = view.findViewById(R.id.tvOrderSummary)
        etCardName = view.findViewById(R.id.etCardName)
        etCardNumber = view.findViewById(R.id.etCardNumber)
        etExpiry = view.findViewById(R.id.etExpiry)
        etCvv = view.findViewById(R.id.etCvv)
        btnPay = view.findViewById(R.id.btnPay)

        totalItems = arguments?.getInt("total_items") ?: 0
        grandTotal = arguments?.getDouble("grand_total") ?: 0.0
        tvOrderSummary.text = "$totalItems items • Total: R ${String.format("%.2f", grandTotal)}"

        setupFormatting()

        btnPay.setOnClickListener { submitOrder() }
    }

    private fun getSavedToken(): String? {
        val prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
        return prefs.getString("token", null)
    }

    private fun setupFormatting() {
        // Card number auto-format: 1234 5678 9012 3456
        etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digitsOnly = s.toString().replace(" ", "")
                val formatted = StringBuilder()
                for (i in digitsOnly.indices) {
                    formatted.append(digitsOnly[i])
                    if ((i + 1) % 4 == 0 && i + 1 < digitsOnly.length) {
                        formatted.append(" ")
                    }
                }
                etCardNumber.setText(formatted.toString())
                etCardNumber.setSelection(formatted.length)

                isFormatting = false
            }
        })

        // Expiry auto-format: MM/YY
        etExpiry.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                var input = s.toString().replace("/", "")
                if (input.length > 2) {
                    input = input.substring(0, 2) + "/" + input.substring(2)
                }
                etExpiry.setText(input)
                etExpiry.setSelection(input.length)

                isFormatting = false
            }
        })
    }

    private fun submitOrder() {
        val name = etCardName.text.toString().trim()
        val cardNumber = etCardNumber.text.toString().replace(" ", "").trim()
        val expiry = etExpiry.text.toString().trim()
        val cvv = etCvv.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Enter card holder name", Toast.LENGTH_SHORT).show()
            return
        }
        if (!cardNumber.matches(Regex("\\d{16}"))) {
            Toast.makeText(requireContext(), "Card number must be 16 digits", Toast.LENGTH_SHORT).show()
            return
        }
        if (!expiry.matches(Regex("(0[1-9]|1[0-2])/\\d{2}"))) {
            Toast.makeText(requireContext(), "Expiry must be MM/YY", Toast.LENGTH_SHORT).show()
            return
        }
        if (!cvv.matches(Regex("\\d{3}"))) {
            Toast.makeText(requireContext(), "CVV must be 3 digits", Toast.LENGTH_SHORT).show()
            return
        }

        val token = getSavedToken() ?: run {
            Toast.makeText(requireContext(), "You are not logged in!", Toast.LENGTH_LONG).show()
            return
        }

        // Make sure Retrofit has token
        RetrofitClient.setToken(token)

        val paymentData = PaymentRequest(
            amount = grandTotal,
            customerName = name,
            maskedCard = "****${cardNumber.takeLast(4)}"
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.pay(paymentData)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.message?.contains("successful", ignoreCase = true) == true) {
                        Toast.makeText(requireContext(), "Payment Successful!", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.id_order_confirmation_fragment)
                    } else {
                        Toast.makeText(requireContext(), "Order failed: ${body?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Order failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
