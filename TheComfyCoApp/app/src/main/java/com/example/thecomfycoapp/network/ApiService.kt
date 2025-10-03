package com.example.thecomfycoapp.network

import com.example.thecomfycoapp.models.LoginRequest
import com.example.thecomfycoapp.models.LoginResponse
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.models.RegisterRequest
import com.example.thecomfycoapp.models.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody // ⬅️ ADDED
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/logout")
    suspend fun logout(): Map<String, String>

    @POST("api/auth/login/google")
    suspend fun loginWithGoogle(@Body request: Map<String, String>): LoginResponse

    // 🔹 Product Endpoints
    @GET("api/products")
    suspend fun getProducts(): List<Product>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): Product

    @Multipart
    @POST("api/products")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("variants") variants: RequestBody,
        @Part image: MultipartBody.Part? // Nullable image part
    ): Response<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): Response<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String
    ): Response<ResponseBody>

}