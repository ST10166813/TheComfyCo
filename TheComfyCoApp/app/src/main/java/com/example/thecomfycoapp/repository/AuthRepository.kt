package com.example.thecomfycoapp.repository

import com.example.thecomfycoapp.models.LoginRequest
import com.example.thecomfycoapp.models.LoginResponse
import com.example.thecomfycoapp.models.RegisterRequest
import com.example.thecomfycoapp.models.RegisterResponse
import com.example.thecomfycoapp.network.RetrofitClient


class AuthRepository {
    private val api = RetrofitClient.api

    suspend fun register(name: String, email: String, password: String, confirmPassword: String): RegisterResponse {
        return api.register(RegisterRequest(name, email, password, confirmPassword))
    }


    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email, password))
    }

    suspend fun logout(): Map<String, String> {
        return api.logout()
    }
}
