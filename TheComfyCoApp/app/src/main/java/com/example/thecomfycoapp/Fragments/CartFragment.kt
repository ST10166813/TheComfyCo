package com.example.thecomfycoapp.Fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.thecomfycoapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class CartFragment : Fragment(R.layout.fragment_cart) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView) ?: return

        // Show the cart-specific menu where the RIGHT slot is the Settings icon
        bottomNav.menu.clear()
        bottomNav.inflateMenu(R.menu.bottom_menu_cart)

        // Don't preselect; it can turn taps into "reselect" events
        // bottomNav.selectedItemId = R.id.cartFragment

        // When going to Settings, remove Cart from the back stack
        val toSettingsPoppingCart = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.id_cart_fragment, /*inclusive=*/true)
            .build()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    findNavController().navigate(R.id.id_home_fragment)
                    true
                }
                // RIGHT slot in this menu is cartFragment but visually shows Settings
                R.id.cartFragment -> {
                    findNavController().navigate(R.id.id_settings_fragments, null, toSettingsPoppingCart)
                    true
                }
                else -> false
            }
        }

        // Also handle re-taps on the already-selected item
        bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> findNavController().navigate(R.id.id_home_fragment)
                R.id.cartFragment -> findNavController().navigate(R.id.id_settings_fragments, null, toSettingsPoppingCart)
            }
        }
    }
}
