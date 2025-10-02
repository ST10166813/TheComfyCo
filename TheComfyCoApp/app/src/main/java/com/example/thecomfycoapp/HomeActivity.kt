package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private lateinit var profileItem: TextView
    private lateinit var settingsItem: TextView
    private lateinit var signOutBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawerLayout)

        // Find drawer views
        profileItem = findViewById(R.id.drawerProfile)
        settingsItem = findViewById(R.id.drawerSettings)
        signOutBtn = findViewById(R.id.drawerSignOut)

        // NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Clicks: navigate + close drawer
        profileItem.setOnClickListener {
            safeNavigate(R.id.id_profile_fragment)
            closeDrawer()
        }
        settingsItem.setOnClickListener {
            safeNavigate(R.id.id_settings_fragments)
            closeDrawer()
        }

        // Sign out
        signOutBtn.setOnClickListener {
            performSignOut()
        }
    }

    /** Called by fragments (e.g., HomeFragment) when hamburger is tapped */
    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun safeNavigate(destId: Int) {
        // Avoid duplicate navigate calls if already on destination
        if (navController.currentDestination?.id != destId) {
            navController.navigate(destId)
        }
    }

    private fun performSignOut() {
        // 1) Clear your app token
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .remove("token")
            .apply()

        // 2) Google sign-out (if used)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, gso)
        googleClient.signOut()

        // 3) Go back to login screen
        val intent = Intent(this, AuthenicationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // 4) Close drawer + finish
        closeDrawer()
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
