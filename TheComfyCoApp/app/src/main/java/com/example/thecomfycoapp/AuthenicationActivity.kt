package com.example.thecomfycoapp

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.thecomfycoapp.viewmodel.AuthViewModel
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class AuthenicationActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var statusText: TextView
    private lateinit var logoutBtn: Button

    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenication)

        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val googleSignInBtn = findViewById<SignInButton>(R.id.googleSignInBtn)
        logoutBtn = findViewById<Button>(R.id.logoutBtn)
        // statusText = findViewById(R.id.statusText) // if you use it, uncomment

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // 🔹 Email/Password Login
        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password) { response, error ->
                runOnUiThread {
                    if (error != null) {
                        Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        // save token + role to SharedPreferences
                        saveToken2(response?.token, response?.role)
                        // >>> IMPORTANT: refresh Retrofit so interceptor starts sending the new token
                        RetrofitClient.setToken(response?.token)

                        val role = response?.userDetails?.role ?: "user"
                        val userName = response?.userDetails?.name

                        val intent = if (role == "admin") {
                            Intent(this, AdminDashboard::class.java)
                        } else {
                            Intent(this, HomeActivity::class.java)
                        }
                        intent.putExtra("name", userName)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }
        }

        // 🔹 Google Sign-In (optional; currently storing a placeholder token)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInBtn.setOnClickListener { signInWithGoogle() }

        // 🔹 Logout
        logoutBtn.setOnClickListener {
            viewModel.logout { response, error ->
                runOnUiThread {
                    if (error != null) {
                        // statusText?.text = "Logout failed: ${error.message}"
                    } else {
                        // statusText?.text = response?.get("message") ?: "Logged out"
                        clearToken()
                    }
                }
            }
            googleSignInClient.signOut()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                val userName = account.displayName
                // ⚠️ Placeholder: you likely want to exchange Google token on your backend to get a real JWT
                saveToken("google_login")
                RetrofitClient.setToken("google_login") // placeholder; replace with real JWT from backend
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("name", userName)
                startActivity(intent)
                finish()
            }
        } catch (e: ApiException) {
            // statusText?.text = "Google Sign-In failed"
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToken(token: String?) {
        token?.let {
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            prefs.edit().putString("token", it).apply()
        }
    }

    private fun saveToken2(token: String?, role: String?) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val editor = prefs.edit()
        token?.let { editor.putString("token", it) }
        role?.let { editor.putString("role", it) }
        editor.apply()
    }

    private fun clearToken() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().remove("token").apply()
        RetrofitClient.setToken(null)
    }
}
