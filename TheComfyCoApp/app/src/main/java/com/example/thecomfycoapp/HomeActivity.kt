//package com.example.thecomfycoapp
//
//import android.content.Context
//import android.content.Intent
//import android.content.res.Configuration
//import android.os.Bundle
//import android.view.View
//import android.widget.TextView
//import androidx.activity.addCallback
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
//import androidx.core.view.GravityCompat
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.navigation.NavController
//import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.navigateUp
//import androidx.navigation.ui.setupActionBarWithNavController
//// IMPORTANT: do NOT import setupWithNavController here
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.material.appbar.MaterialToolbar
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.color.MaterialColors
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//
//class HomeActivity : AppCompatActivity() {
//
//    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var navController: NavController
//    private lateinit var toolbar: MaterialToolbar
//    private lateinit var appBarConfig: AppBarConfiguration
//
//    private lateinit var arrow: DrawerArrowDrawable
//
//    private lateinit var profileItem: TextView
//    private lateinit var settingsItem: TextView
//    private lateinit var signOutBtn: MaterialButton
//
//    private lateinit var bottomNav: BottomNavigationView
//    private lateinit var fabBtn: FloatingActionButton
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        // Apply saved theme BEFORE inflating
//        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
//        AppCompatDelegate.setDefaultNightMode(
//            prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
//        )
//
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//
//        // ----- Views
//        drawerLayout = findViewById(R.id.drawerLayout)
//        toolbar = findViewById(R.id.topAppBar)
//        bottomNav = findViewById(R.id.bottomNavigationView)
//        fabBtn = findViewById(R.id.fab)
//
//        setSupportActionBar(toolbar)
//        supportActionBar?.title = ""
//        toolbar.bringToFront()
//
//        // ----- NavController
//        val navHost =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        navController = navHost.navController
//
//        // Top-level destinations show hamburger (Home id)
//        val homeId = R.id.id_home_fragment
//        appBarConfig = AppBarConfiguration(setOf(homeId), drawerLayout)
//        setupActionBarWithNavController(navController, appBarConfig)
//
//        // ----- Drawer / Toolbar icon we fully control
//        arrow = DrawerArrowDrawable(this).apply {
//            color = MaterialColors.getColor(
//                toolbar, com.google.android.material.R.attr.colorOnSurface
//            )
//        }
//        toolbar.navigationIcon = arrow
//
//        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
//            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//                if (isAtTopLevel()) arrow.progress = slideOffset.coerceIn(0f, 1f)
//            }
//            override fun onDrawerClosed(drawerView: View) {
//                if (isAtTopLevel()) arrow.progress = 0f
//            }
//            override fun onDrawerOpened(drawerView: View) {
//                if (isAtTopLevel()) arrow.progress = 1f
//            }
//        })
//
//        bottomNav.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.menu_home -> {
//                    safeNavigate(R.id.id_home_fragment)
//                    true
//                }
//                R.id.menu_cart -> {
//                    safeNavigate(R.id.id_cart_fragment)
//                    true
//                }
//                R.id.id_settings_fragments -> {     // this only appears in your cart-specific menu
//                    safeNavigate(R.id.id_settings_fragments)
//                    true
//                }
//                else -> false
//            }
//        }
//
//
//        bottomNav.setOnItemReselectedListener { /* no-op */ }
//        // Make Home tab checked on launch
//        bottomNav.menu.findItem(R.id.menu_home)?.isChecked = true
//
//        // FAB action (hook up when you have a wishlist destination)
//        fabBtn.setOnClickListener {
//            // Example: safeNavigate(R.id.id_wishlist_fragment)
//        }
//
//        // ----- Drawer menu items
//        profileItem = findViewById(R.id.drawerProfile)
//        settingsItem = findViewById(R.id.drawerSettings)
//        signOutBtn = findViewById(R.id.drawerSignOut)
//
//        profileItem.setOnClickListener {
//            safeNavigate(R.id.id_profile_fragment)
//            closeDrawer()
//        }
//        settingsItem.setOnClickListener {
//            // Keep exactly what your graph uses
//            safeNavigate(R.id.id_settings_fragments)
//            closeDrawer()
//        }
//        signOutBtn.setOnClickListener { performSignOut() }
//
//        // ----- Back press: close drawer first
//        onBackPressedDispatcher.addCallback(this) {
//            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                closeDrawer()
//            } else {
//                isEnabled = false
//                onBackPressedDispatcher.onBackPressed()
//            }
//        }
//
//        // ----- Toggle hamburger/back and show/hide bottom bar by destination
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            supportActionBar?.title = ""
//
//            if (isAtTopLevel(destination.id)) {
//                drawerLayout.setDrawerLockMode(
//                    DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START
//                )
//                arrow.progress = 0f
//                toolbar.setNavigationOnClickListener {
//                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                        drawerLayout.openDrawer(GravityCompat.START)
//                    }
//                }
//            } else {
//                drawerLayout.setDrawerLockMode(
//                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START
//                )
//                arrow.progress = 1f
//                toolbar.setNavigationOnClickListener {
//                    navController.navigateUp(appBarConfig)
//                }
//            }
//
//            // Show bottom bar + FAB only on Home
//            val showBottom = destination.id == homeId
//            bottomNav.visibility = if (showBottom) View.VISIBLE else View.GONE
//            fabBtn.visibility = if (showBottom) View.VISIBLE else View.GONE
//        }
//    }
//
//    private fun isAtTopLevel(destId: Int = navController.currentDestination?.id ?: -1): Boolean {
//        return appBarConfig.topLevelDestinations.contains(destId)
//    }
//
//    private fun closeDrawer() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        }
//    }
//
//    private fun safeNavigate(destId: Int) {
//        val current = navController.currentDestination?.id
//        if (current != destId) {
//            try {
//                navController.navigate(destId)
//            } catch (_: Exception) {
//                // Avoid crash if an action is missing; add explicit actions later if needed.
//            }
//        }
//    }
//
//    private fun performSignOut() {
//        // Clear your own auth token
//        getSharedPreferences("auth", MODE_PRIVATE).edit().remove("token").apply()
//
//        // Google sign out (if used)
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .build()
//        GoogleSignIn.getClient(this, gso).signOut()
//
//        // Go to your auth screen (keep your class name as-is)
//        startActivity(Intent(this, AuthenicationActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        })
//        closeDrawer()
//        finish()
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        // No extra handling needed.
//    }
//}

package com.example.thecomfycoapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarConfig: AppBarConfiguration

    private lateinit var arrow: DrawerArrowDrawable

    private lateinit var profileItem: TextView
    private lateinit var settingsItem: TextView
    private lateinit var signOutBtn: MaterialButton

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabBtn: FloatingActionButton

    // ---- Destination IDs used across the Activity
    private val homeDestId get() = R.id.id_home_fragment
    private val cartDestId get() = R.id.id_cart_fragment
    private val settingsDestId get() = R.id.id_settings_fragments // keep your actual id

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme BEFORE inflating
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ----- Views
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigationView)
        fabBtn = findViewById(R.id.fab)
//
//        val btnViewProduct: MaterialButton = findViewById(R.id.btnViewProduct)
//        btnViewProduct.setOnClickListener {
//            val intent = Intent(this, UserProductListActivity::class.java) // Replace with your product page activity
//            startActivity(intent)
//        }

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar.bringToFront()

        // ----- NavController
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // Top-level destinations (Home shows hamburger)
        appBarConfig = AppBarConfiguration(setOf(homeDestId), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)

        // ----- Drawer / Toolbar icon
        arrow = DrawerArrowDrawable(this).apply {
            color = MaterialColors.getColor(
                toolbar, com.google.android.material.R.attr.colorOnSurface
            )
        }
        toolbar.navigationIcon = arrow

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (isAtTopLevel()) arrow.progress = slideOffset.coerceIn(0f, 1f)
            }
            override fun onDrawerClosed(drawerView: View) {
                if (isAtTopLevel()) arrow.progress = 0f
            }
            override fun onDrawerOpened(drawerView: View) {
                if (isAtTopLevel()) arrow.progress = 1f
            }
        })

        // ----- Bottom nav in Activity (visible on Home only)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    safeNavigate(homeDestId)
                    true
                }
                R.id.menu_cart -> {
                    safeNavigate(cartDestId)
                    true
                }
                // This id doesn't exist in the Activity's menu, but leaving here is harmless.
                R.id.id_settings_fragments -> {
                    safeNavigate(settingsDestId)
                    true
                }
                else -> false
            }
        }
        bottomNav.setOnItemReselectedListener { /* no-op */ }

        // Mark Home checked on launch
        bottomNav.menu.findItem(R.id.menu_home)?.isChecked = true

        // FAB action (wire up when ready)
        fabBtn.setOnClickListener {
            // Example: safeNavigate(R.id.id_wishlist_fragment)
        }

        // ----- Drawer menu items
        profileItem = findViewById(R.id.drawerProfile)
        settingsItem = findViewById(R.id.drawerSettings)
        signOutBtn = findViewById(R.id.drawerSignOut)

        profileItem.setOnClickListener {
            safeNavigate(R.id.id_profile_fragment)
            closeDrawer()
        }
        settingsItem.setOnClickListener {
            safeNavigate(settingsDestId)
            closeDrawer()
        }
        signOutBtn.setOnClickListener { performSignOut() }

        // ----- Back press: close drawer first
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                closeDrawer()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        // ----- Destination changes: toggle drawer/back button, show/hide bottom bar,
        // and keep bottom nav selection in sync with where we are.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = ""

            if (isAtTopLevel(destination.id)) {
                drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START
                )
                arrow.progress = 0f
                toolbar.setNavigationOnClickListener {
                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                }
            } else {
                drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START
                )
                arrow.progress = 1f
                toolbar.setNavigationOnClickListener {
                    navController.navigateUp(appBarConfig)
                }
            }

            // Show Activity bottom bar + FAB only on Home
            val showBottom = destination.id == homeDestId
            bottomNav.visibility = if (showBottom) View.VISIBLE else View.GONE
            fabBtn.visibility = if (showBottom) View.VISIBLE else View.GONE

            // Keep the Activity bottom nav's checked state in sync
            syncBottomNavSelection(destination.id)
        }


        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCMToken", token) // Step 2a: Check the log to confirm token is received
                sendTokenToBackend(token)
            }
        }

    }

    private fun sendTokenToBackend(token: String) {
        val authToken = getSharedPreferences("auth", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                RetrofitClient.api.saveDeviceToken(
                    mapOf("token" to token),
                    "Bearer $authToken" // include your JWT for auth
                )
                Log.d("FCMToken", "Token sent to backend successfully")
            } catch (e: Exception) {
                Log.e("FCMToken", "Error sending token to backend", e)
            }
        }
    }


    private fun isAtTopLevel(destId: Int = navController.currentDestination?.id ?: -1): Boolean {
        return appBarConfig.topLevelDestinations.contains(destId)
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // Navigate only if we're not already there, and optimistically sync selection
    private fun safeNavigate(destId: Int) {
        val current = navController.currentDestination?.id
        if (current != destId) {
            try {
                navController.navigate(destId)
                // Optimistic selection (destination callback will confirm)
                syncBottomNavSelection(destId)
            } catch (_: Exception) {
                // Avoid crash if an action is missing; add explicit actions later if needed.
            }
        }
    }

    // Ensure the Activity bottom nav reflects the current destination
    private fun syncBottomNavSelection(destId: Int) {
        when (destId) {
            homeDestId -> bottomNav.menu.findItem(R.id.menu_home)?.isChecked = true
            cartDestId -> bottomNav.menu.findItem(R.id.menu_cart)?.isChecked = true
            else -> {
                // For other screens (settings/profile/etc.), leave last state as-is.
                // If you prefer to clear selection, uncomment below:
                // bottomNav.menu.setGroupCheckable(0, true, true)
                // bottomNav.menu.findItem(R.id.menu_home)?.isChecked = false
                // bottomNav.menu.findItem(R.id.menu_cart)?.isChecked = false
            }
        }
    }

    private fun performSignOut() {
        // Clear your own auth token
        getSharedPreferences("auth", MODE_PRIVATE).edit().remove("token").apply()

        // Google sign out (if used)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).signOut()

        // Go to your auth screen (keep your class name as-is)
        startActivity(Intent(this, AuthenicationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        closeDrawer()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // No extra handling needed.
    }
}