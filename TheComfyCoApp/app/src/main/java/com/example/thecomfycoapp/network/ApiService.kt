package com.example.thecomfycoapp.network

import com.example.thecomfycoapp.models.LoginRequest
import com.example.thecomfycoapp.models.LoginResponse
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.models.RegisterRequest
import com.example.thecomfycoapp.models.RegisterResponse
import okhttp3.MultipartBody
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
    suspend fun logout(): Map<String, String> // { "message": "Logged out" }

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
        @Part("name") name: String,
        @Part("description") description: String,
        @Part("price") price: Double,
        @Part("stock") stock: Int,
        @Part("variants") variants: List<Map<String, Any>>,
        @Part image: MultipartBody.Part? = null
    ): Product

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Product

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Map<String, String>

}
