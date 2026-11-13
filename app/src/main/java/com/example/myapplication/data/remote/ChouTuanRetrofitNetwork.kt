package com.example.myapplication.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class ProductDto(
    val productId: String,
    val title: String,
    val price: Double,
    val originalPrice: Double,
    val specOptions: List<String> = emptyList()
)

data class OrderCreateRequest(
    val merchantId: String,
    val items: List<String>,
    val totalAmount: Double
)

data class OrderResponse(
    val orderId: String,
    val tenantId: String,
    val status: String,
    val expireTimestampMillis: Long
)

interface ChouTuanApiService {
    @GET("gateway-service/product-service/api/v1/products")
    suspend fun getProducts(
        @Header("X-Tenant-Id") tenantId: String,
        @Query("category") category: String? = null
    ): List<ProductDto>

    @POST("gateway-service/order-service/api/v1/orders")
    suspend fun createOrder(
        @Header("X-Tenant-Id") tenantId: String,
        @Body request: OrderCreateRequest
    ): OrderResponse

    @POST("gateway-service/order-service/api/v1/orders/{orderId}/redeem")
    suspend fun redeemVoucher(
        @Header("X-Tenant-Id") tenantId: String,
        @Path("orderId") orderId: String,
        @Query("code") voucherCode: String
    ): OrderResponse
}
