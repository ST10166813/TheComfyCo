package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.utils.LanguageManager
import com.example.thecomfycoapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.Executor

class AuthenicationActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loginBtn: MaterialButton
    private lateinit var googleSignInBtn: MaterialButton
    private lateinit var biometricBtn: MaterialButton
    private lateinit var emailEt: TextInputEditText
    private lateinit var passwordEt: TextInputEditText

    private lateinit var forgotBtn: Button

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenication)

        emailEt = findViewById(R.id.emailField)
        passwordEt = findViewById(R.id.passwordField)
        loginBtn = findViewById(R.id.loginBtn)
        googleSignInBtn = findViewById(R.id.googleSignInBtn)
        biometricBtn = findViewById(R.id.biometricLoginBtn)
        forgotBtn = findViewById(R.id.forgotBtn)

        biometricBtn.isEnabled = false

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Forgot password
        forgotBtn.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Email/password login
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
                    val role = response?.userDetails?.role
                    val userName = response?.userDetails?.name

                    if (token != null) {
                        saveToken(token)
                        RetrofitClient.setToken(token)
                    }
                    saveRole(role)

                    enableBiometricIfPossible()

                    val intent = if ((role ?: "").lowercase() == "admin") {
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

        // Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInBtn.setOnClickListener { signInWithGoogle() }

        // Biometric
        setupBiometricAuth()
        enableBiometricIfPossible()

        biometricBtn.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    // Biometric setup
    private fun setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    toast("Biometric Verified!")
                    startActivity(Intent(this@AuthenicationActivity, HomeActivity::class.java))
                    finish()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your fingerprint or device credentials")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    private fun enableBiometricIfPossible() {
        val token = getSavedToken()
        val bm = BiometricManager.from(this)
        val canAuth = bm.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        biometricBtn.isEnabled = (token != null && canAuth == BiometricManager.BIOMETRIC_SUCCESS)
    }

    // Google Sign-In handler
    private fun handleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // TODO: Call your backend /api/auth/login/google with account.idToken
                // For now, just save placeholder until backend integration
                val fakeToken = "google_jwt_token"
                saveToken(fakeToken)
                RetrofitClient.setToken(fakeToken)

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Helpers
    private fun saveToken(token: String?) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("token", token).apply()
    }

    private fun getSavedToken(): String? =
        getSharedPreferences("auth", MODE_PRIVATE).getString("token", null)

    private fun saveRole(role: String?) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("role", role).apply()
    }

    private fun signInWithGoogle() {
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
