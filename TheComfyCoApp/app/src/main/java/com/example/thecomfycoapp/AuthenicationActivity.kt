//package com.example.thecomfycoapp
//
//import androidx.appcompat.app.AppCompatActivity
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.lifecycle.ViewModelProvider
//import com.example.thecomfycoapp.viewmodel.AuthViewModel
//import com.example.thecomfycoapp.network.RetrofitClient
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.SignInButton
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.tasks.Task
//
//class AuthenicationActivity : AppCompatActivity() {
//
//    private lateinit var viewModel: AuthViewModel
//    private lateinit var googleSignInClient: GoogleSignInClient
//    private lateinit var statusText: TextView
//    private lateinit var logoutBtn: Button
//
//    private val RC_SIGN_IN = 1001
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_authenication)
//
//        val emailField = findViewById<EditText>(R.id.emailField)
//        val passwordField = findViewById<EditText>(R.id.passwordField)
//        val loginBtn = findViewById<Button>(R.id.loginBtn)
//        val googleSignInBtn = findViewById<SignInButton>(R.id.googleSignInBtn)
//        logoutBtn = findViewById<Button>(R.id.logoutBtn)
//        // statusText = findViewById(R.id.statusText) // if you use it, uncomment
//
//        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
//
//        // 🔹 Email/Password Login
//        loginBtn.setOnClickListener {
//            val email = emailField.text.toString()
//            val password = passwordField.text.toString()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            viewModel.login(email, password) { response, error ->
//                runOnUiThread {
//                    if (error != null) {
//                        Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_SHORT).show()
//                    } else {
//                        // save token + role to SharedPreferences
//                        saveToken2(response?.token, response?.role)
//                        // >>> IMPORTANT: refresh Retrofit so interceptor starts sending the new token
//                        RetrofitClient.setToken(response?.token)
//
//                        val role = response?.userDetails?.role ?: "user"
//                        val userName = response?.userDetails?.name
//
//                        val intent = if (role == "admin") {
//                            Intent(this, AdminDashboard::class.java)
//                        } else {
//                            Intent(this, HomeActivity::class.java)
//                        }
//                        intent.putExtra("name", userName)
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        startActivity(intent)
//                    }
//                }
//            }
//        }
//
//        // 🔹 Google Sign-In (optional; currently storing a placeholder token)
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .build()
//        googleSignInClient = GoogleSignIn.getClient(this, gso)
//
//        googleSignInBtn.setOnClickListener { signInWithGoogle() }
//
//        // 🔹 Logout
//        logoutBtn.setOnClickListener {
//            viewModel.logout { response, error ->
//                runOnUiThread {
//                    if (error != null) {
//                        // statusText?.text = "Logout failed: ${error.message}"
//                    } else {
//                        // statusText?.text = response?.get("message") ?: "Logged out"
//                        clearToken()
//                    }
//                }
//            }
//            googleSignInClient.signOut()
//        }
//    }
//
//    private fun signInWithGoogle() {
//        val signInIntent: Intent = googleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
//
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == RC_SIGN_IN) {
//            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }
//
//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
//            if (account != null) {
//                val userName = account.displayName
//                // ⚠️ Placeholder: you likely want to exchange Google token on your backend to get a real JWT
//                saveToken("google_login")
//                RetrofitClient.setToken("google_login") // placeholder; replace with real JWT from backend
//                val intent = Intent(this, HomeActivity::class.java)
//                intent.putExtra("name", userName)
//                startActivity(intent)
//                finish()
//            }
//        } catch (e: ApiException) {
//            // statusText?.text = "Google Sign-In failed"
//            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun saveToken(token: String?) {
//        token?.let {
//            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
//            prefs.edit().putString("token", it).apply()
//        }
//    }
//
//    private fun saveToken2(token: String?, role: String?) {
//        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
//        val editor = prefs.edit()
//        token?.let { editor.putString("token", it) }
//        role?.let { editor.putString("role", it) }
//        editor.apply()
//    }
//
//    private fun clearToken() {
//        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
//        prefs.edit().remove("token").apply()
//        RetrofitClient.setToken(null)
//    }
//}

package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AuthenicationActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loginBtn: MaterialButton
    private lateinit var googleSignInBtn: MaterialButton   // <-- MaterialButton, matches XML
    private lateinit var emailEt: TextInputEditText
    private lateinit var passwordEt: TextInputEditText

    // Activity Result API launcher for Google Sign-In
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure this matches your layout file name exactly
        setContentView(R.layout.activity_authenication)

        // Views (IDs must match your XML)
        emailEt = findViewById(R.id.emailField)
        passwordEt = findViewById(R.id.passwordField)
        loginBtn = findViewById(R.id.loginBtn)
        googleSignInBtn = findViewById(R.id.googleSignInBtn)

        // ViewModel
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Email / Password login
        loginBtn.setOnClickListener {
            val email = emailEt.text?.toString()?.trim().orEmpty()
            val password = passwordEt.text?.toString()?.trim().orEmpty()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please enter email & password")
                return@setOnClickListener
            }

            loginBtn.isEnabled = false

            viewModel.login(email, password) { response, error ->
                runOnUiThread {
                    loginBtn.isEnabled = true

                    if (error != null) {
                        toast("Login failed: ${error.message}")
                        return@runOnUiThread
                    }

                    val token = response?.token
                    val roleFromUser = response?.userDetails?.role
                    val userName = response?.userDetails?.name

                    // Save auth and refresh Retrofit
                    saveToken2(token, roleFromUser)
                    RetrofitClient.setToken(token)

                    // Decide where to go
                    val role = (roleFromUser ?: response?.role ?: "user").lowercase()

                    try {
                        val intent = if (role == "admin") {
                            Intent(this, AdminDashboard::class.java)
                        } else {
                            Intent(this, HomeActivity::class.java)
                        }
                        intent.putExtra("name", userName)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        // finish() // enable if you don't want back to auth
                    } catch (t: Throwable) {
                        toast("Navigation failed")
                        t.printStackTrace()
                    }
                }
            }
        }

        // Google Sign-In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // add .requestIdToken(getString(R.string.default_web_client_id)) if you swap tokens with backend
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Pretty Material Google button
        googleSignInBtn.setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                val userName = account.displayName

                // TODO: If using secure backend auth, exchange Google ID token for your JWT:
                // val idToken = account.idToken
                // send idToken to backend → receive JWT → save & set in Retrofit

                saveToken("google_login") // placeholder
                RetrofitClient.setToken("google_login") // placeholder

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("name", userName)
                startActivity(intent)
                finish()
            } else {
                toast("Google account not found")
            }
        } catch (e: ApiException) {
            toast("Google Sign-In failed: ${e.statusCode}")
        } catch (t: Throwable) {
            toast("Google Sign-In error")
            t.printStackTrace()
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
        prefs.edit().apply {
            token?.let { putString("token", it) }
            role?.let { putString("role", it) }
        }.apply()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
