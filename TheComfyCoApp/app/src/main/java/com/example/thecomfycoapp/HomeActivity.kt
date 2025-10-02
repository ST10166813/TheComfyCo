package com.example.thecomfycoapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private lateinit var profileItem: TextView
    private lateinit var settingsItem: TextView
    private lateinit var signOutBtn: MaterialButton

    private lateinit var toolbar: MaterialToolbar
    private lateinit var burger: DrawerArrowDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme BEFORE inflating UI
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(
            prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.topAppBar)

        // --- Hamburger icon (theme-aware) ---
        val onSurface = MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorOnSurface)
        burger = DrawerArrowDrawable(this).apply {
            color = onSurface   // auto flips light/dark
            progress = 0f       // hamburger state
            // optionally: setBarLength(), setArrowHeadLength(), etc., if you want a bigger icon
        }
        toolbar.navigationIcon = burger
        toolbar.contentInsetStartWithNavigation = 0 // ensure big left hit area

        // Open drawer on FIRST tap, always
        toolbar.setNavigationOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Animate hamburger <-> arrow while dragging the drawer
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                burger.progress = slideOffset.coerceIn(0f, 1f)
            }
            override fun onDrawerOpened(drawerView: View) { burger.progress = 1f }
            override fun onDrawerClosed(drawerView: View) { burger.progress = 0f }
        })

        // --- Drawer items ---
        profileItem = findViewById(R.id.drawerProfile)
        settingsItem = findViewById(R.id.drawerSettings)
        signOutBtn   = findViewById(R.id.drawerSignOut)

        profileItem.setOnClickListener { safeNavigate(R.id.id_profile_fragment); closeDrawer() }
        settingsItem.setOnClickListener { safeNavigate(R.id.id_settings_fragments); closeDrawer() }
        signOutBtn.setOnClickListener { performSignOut() }

        // --- NavController ---
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun openDrawer() { drawerLayout.openDrawer(GravityCompat.START) }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun safeNavigate(destId: Int) {
        if (navController.currentDestination?.id != destId) {
            navController.navigate(destId)
        }
    }

    private fun performSignOut() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().remove("token").apply()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().build()
        GoogleSignIn.getClient(this, gso).signOut()

        startActivity(Intent(this, AuthenicationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        closeDrawer()
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) closeDrawer() else super.onBackPressed()
    }
}
