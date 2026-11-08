package com.nearai.admin.feature.orders.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service interface for payment-related endpoints used by the Admin Portal.
 *
 * Backend contracts:
 * - GET  /api/v1/payments/for-order/{order_id} — get payment by order ID (admin only)
 * - POST /api/v1/payments/{payment_id}/collect-cash — collect COD cash (admin only)
 */
interface PaymentApiService {

    @retrofit2.http.GET("api/v1/payments/for-order/{order_id}")
    suspend fun getPaymentByOrderId(@Path("order_id") orderId: String): PaymentInfoDto

    @POST("api/v1/payments/{payment_id}/collect-cash")
    suspend fun collectCash(@Path("payment_id") paymentId: String): CollectCashResponseDto
}

// =============================================================================
// DTOs
// =============================================================================

/**
 * Payment information DTO returned by the backend.
 * Matches backend PaymentResponse schema.
 */
@Serializable
data class PaymentInfoDto(
    @SerialName("id") val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("provider") val provider: String,
    @SerialName("provider_payment_id") val providerPaymentId: String? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("status") val status: String,
    @SerialName("checkout_url") val checkoutUrl: String? = null,
    @SerialName("collected_at") val collectedAt: String? = null,
    @SerialName("collected_by") val collectedBy: String? = null,
    @SerialName("created_at") val createdAt: String
)

/**
 * Response DTO for the COD cash collection endpoint.
 * Matches backend CollectCashResponse schema.
 */
@Serializable
data class CollectCashResponseDto(
    @SerialName("id") val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("status") val status: String,
    @SerialName("collected_at") val collectedAt: String? = null,
    @SerialName("collected_by") val collectedBy: String? = null
)
