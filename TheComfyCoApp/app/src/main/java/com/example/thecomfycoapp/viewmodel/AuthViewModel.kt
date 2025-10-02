package com.example.thecomfycoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thecomfycoapp.models.LoginResponse
import com.example.thecomfycoapp.models.RegisterResponse
import com.example.thecomfycoapp.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    fun register(name: String, email: String, password: String, confirmPassword: String, onResult: (RegisterResponse?, Throwable?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.register(name, email, password, confirmPassword)
                onResult(response, null)
            } catch (e: Throwable) {
                onResult(null, e)
            }
        }
    }


    fun login(email: String, password: String, onResult: (LoginResponse?, Throwable?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                onResult(response, null)
            } catch (e: Throwable) {
                onResult(null, e)
            }
        }
    }

    fun logout(onResult: (Map<String, String>?, Throwable?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.logout()
                onResult(response, null)
            } catch (e: Throwable) {
                onResult(null, e)
            }
        }
    }
}
