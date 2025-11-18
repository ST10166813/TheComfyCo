package com.example.thecomfycoapp.Fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thecomfycoapp.R
import com.google.android.material.button.MaterialButton

class OrderConfirmationFragment : Fragment(R.layout.fragment_order_confirmation) {

    private lateinit var btnContinueShopping: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnContinueShopping = view.findViewById(R.id.btnContinueShopping)

        btnContinueShopping.setOnClickListener {
            findNavController().navigate(R.id.id_home_fragment)
        }
    }
}
