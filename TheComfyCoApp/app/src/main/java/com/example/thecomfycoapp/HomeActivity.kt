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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: androidx.navigation.NavController
    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarConfig: AppBarConfiguration

    private lateinit var arrow: DrawerArrowDrawable

    private lateinit var profileItem: TextView
    private lateinit var settingsItem: TextView
    private lateinit var signOutBtn: MaterialButton

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabBtn: FloatingActionButton

    private val homeDestId get() = R.id.id_home_fragment
    private val cartDestId get() = R.id.id_cart_fragment
    private val settingsDestId get() = R.id.id_settings_fragments

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ✅ Ensure Retrofit uses whatever token the user logged in with
        RetrofitClient.init(applicationContext)

        // ----- Views -----
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigationView)
        fabBtn = findViewById(R.id.fab)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar.bringToFront()

        // ----- NavController -----
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
        appBarConfig = AppBarConfiguration(setOf(homeDestId), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)

        // ----- Drawer icon -----
        arrow = DrawerArrowDrawable(this).apply {
            color = MaterialColors.getColor(
                toolbar,
                com.google.android.material.R.attr.colorOnSurface
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

        // ----- Bottom nav -----
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    safeNavigate(homeDestId); true
                }

                R.id.menu_cart -> {
                    safeNavigate(cartDestId); true
                }

                else -> false
            }
        }
        bottomNav.setOnItemReselectedListener { /* no-op */ }
        bottomNav.menu.findItem(R.id.menu_home)?.isChecked = true

        fabBtn.setOnClickListener {
            // Optional: wishlist / quick action
        }

        // ----- Drawer menu items -----
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

        // ----- Back press handling -----
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                closeDrawer()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        // ----- Destination change listener -----
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = ""

            if (isAtTopLevel(destination.id)) {
                drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_UNLOCKED,
                    GravityCompat.START
                )
                arrow.progress = 0f
                toolbar.setNavigationOnClickListener {
                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }
                }
            } else {
                drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    GravityCompat.START
                )
                arrow.progress = 1f
                toolbar.setNavigationOnClickListener {
                    navController.navigateUp(appBarConfig)
                }
            }

            // Show bottom nav only on Home (your choice – can add Cart too if you want)
            val showBottom = destination.id == homeDestId
            bottomNav.visibility = if (showBottom) View.VISIBLE else View.GONE
            fabBtn.visibility = if (showBottom) View.VISIBLE else View.GONE

            syncBottomNavSelection(destination.id)
        }

        // ----- FCM token (fixed to use new ApiService signature) -----
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCMToken", "Failed to get token", task.exception)
                return@addOnCompleteListener
            }
            val fcmToken = task.result ?: ""
            sendTokenToBackend(fcmToken)
        }
    }

    // ---------- FCM TOKEN SEND (NO authHeader PARAM) ----------
    private fun sendTokenToBackend(fcmToken: String) {
        if (fcmToken.isBlank()) return

        val prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val jwtToken = prefs.getString("token", null) ?: return

        // ✅ Ensure Retrofit is using this user's token for subsequent calls
        RetrofitClient.setToken(jwtToken)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.saveDeviceToken(
                    mapOf("token" to fcmToken)
                )
                Log.d(
                    "FCMToken",
                    if (response.isSuccessful)
                        "Token sent successfully"
                    else
                        "Failed: ${response.code()} ${response.message()}"
                )
            } catch (e: Exception) {
                Log.e("FCMToken", "Error sending token to backend", e)
            }
        }
    }

    // ---------- Helpers ----------

    private fun isAtTopLevel(destId: Int = navController.currentDestination?.id ?: -1) =
        appBarConfig.topLevelDestinations.contains(destId)

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun safeNavigate(destId: Int) {
        val current = navController.currentDestination?.id
        if (current != destId) {
            try {
                navController.navigate(destId)
                syncBottomNavSelection(destId)
            } catch (_: Exception) {
            }
        }
    }

    private fun syncBottomNavSelection(destId: Int) {
        when (destId) {
            homeDestId -> bottomNav.menu.findItem(R.id.menu_home)?.isChecked = true
            cartDestId -> bottomNav.menu.findItem(R.id.menu_cart)?.isChecked = true
        }
    }

    private fun performSignOut() {
        // Clear saved token
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .remove("token")
            .apply()

        // Also clear from Retrofit so headers stop sending it
        RetrofitClient.setToken(null)

        // Google sign-out
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()

        startActivity(
            Intent(this, AuthenicationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        closeDrawer()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
