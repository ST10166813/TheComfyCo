//package com.example.thecomfycoapp.network
//
//import com.example.thecomfycoapp.models.*
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.Response
//import retrofit2.http.*
//
//interface ApiService {
//
//    @POST("api/auth/register")
//    suspend fun register(@Body request: RegisterRequest): RegisterResponse
//
//    @POST("api/auth/login")
//    suspend fun login(@Body request: LoginRequest): LoginResponse
//
//    @POST("api/auth/logout")
//    suspend fun logout(): Map<String, String>
//
//    @POST("api/auth/login/google")
//    suspend fun loginWithGoogle(@Body request: Map<String, String>): LoginResponse
//
//    @GET("api/products")
//    suspend fun getProducts(): List<Product>
//
//    @GET("api/products/{id}")
//    suspend fun getProduct(@Path("id") id: String): Product
//
//    @Multipart
//    @POST("api/products") // change to "api/admin/products" if that's your protected path
//    suspend fun createProduct(
//        @Part("name") name: RequestBody,
//        @Part("description") description: RequestBody,
//        @Part("price") price: RequestBody,
//        @Part("stock") stock: RequestBody,
//        @Part("variants") variants: RequestBody,
//        @Part image: MultipartBody.Part?
//    ): Response<Product>
//
//    @PUT("api/products/{id}")
//    suspend fun updateProduct(
//        @Path("id") id: String,
//        @Body product: Product
//    ): Response<Product>
//
//    @DELETE("api/products/{id}")
//    suspend fun deleteProduct(@Path("id") id: String): Response<Map<String, String>>
//
//    @POST("api/auth/forgot-password")
//    suspend fun forgotPassword(@Body request: Map<String, String>): Response<ApiResponse>
//
//    @POST("api/auth/reset-password")
//    suspend fun resetPassword(@Body request: Map<String, String>): Response<ApiResponse>
//
//
//    @POST("/register-token")
//    suspend fun saveDeviceToken(
//        @Body body: Map<String, String>,
//        @Header("Authorization") authHeader: String
//    ): Response<Any> // now isSuccessful, code(), message() work
//
//    // User: create order at checkout
//    @POST("api/orders")
//    suspend fun createOrder(
//        @Body order: OrderRequest
//    ): Response<OrderResponse>
//
//    // Admin: fetch all orders for "Manage Orders"
//    @GET("api/admin/orders")
//    suspend fun getOrders(): Response<List<OrderResponse>>
//}

package com.example.thecomfycoapp.network

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

    // ------------- DEVICE TOKEN -------------
    // Interceptor already adds Authorization, so NO manual @Header here
    @POST("register-token")
    suspend fun saveDeviceToken(
        @Body body: Map<String, String>
    ): Response<Any>

    // ------------- ORDERS -------------

    // User: create order at checkout
    @POST("api/orders")
    suspend fun createOrder(
        @Body order: OrderRequest
    ): Response<OrderResponse>

    // Admin: fetch all orders for "Manage Orders"
    // Again: interceptor adds Authorization for you
    @GET("api/admin/orders")
    suspend fun getOrders(): Response<List<OrderResponse>>
}
