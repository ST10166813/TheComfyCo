package com.example.thecomfycoapp.network

import com.example.thecomfycoapp.Fragments.CartResponse
import com.example.thecomfycoapp.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/logout")
    suspend fun logout(): Map<String, String>

    @POST("api/auth/login/google")
    suspend fun loginWithGoogle(@Body request: Map<String, String>): LoginResponse

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
        @Part image: MultipartBody.Part?
    ): Response<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body product: Product
    ): Response<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Map<String, String>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<ApiResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<ApiResponse>

    @POST("register-token")
    suspend fun saveDeviceToken(@Body body: Map<String, String>): Response<Any>

    @GET("api/admin/orders")
    suspend fun getOrders(): Response<List<OrderResponse>>

    // -------- CART (no @Header) --------
    @POST("api/cart/add")
    suspend fun addToCart(
        @Body item: CartItemRequest
    ): Response<CartResponse>

    @GET("api/cart")
    suspend fun getCart(): Response<CartResponse>

    @DELETE("api/cart/remove/{productId}")
    suspend fun removeFromCart(
        @Path("productId") productId: String
    ): Response<CartResponse>

    @DELETE("api/cart/clear")
    suspend fun clearCart(): Response<Map<String, String>>

    // -------- PAYMENT (no @Header) --------
    @POST("api/payment/pay")
    suspend fun pay(
        @Body payment: PaymentRequest
    ): Response<PaymentResponse>
}
