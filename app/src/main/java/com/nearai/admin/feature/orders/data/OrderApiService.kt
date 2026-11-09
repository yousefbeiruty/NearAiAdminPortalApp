package com.nearai.admin.feature.orders.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for order management endpoints.
 *
 * Backend contract from ORDER_API_CONTRACTS.md:
 * - GET  /api/v1/orders/admin/orders          — list orders with pagination and filters
 * - GET  /api/v1/orders/{order_id}            — order details
 * - GET  /api/v1/orders/{order_id}/status-history — status history timeline
 * - POST /api/v1/orders/{order_id}/confirm    — confirm pending order
 * - POST /api/v1/orders/admin/{order_id}/reject — reject with reason
 * - POST /api/v1/orders/{order_id}/process    — process confirmed order
 * - POST /api/v1/orders/{order_id}/dispatch   — dispatch order
 * - POST /api/v1/orders/{order_id}/deliver    — deliver order
 */
interface OrderApiService {

    @GET("api/v1/orders/admin/orders")
    suspend fun listOrders(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("status") status: String?,
        @Query("fulfillment_status") fulfillmentStatus: String?
    ): OrdersListResponse

    @GET("api/v1/orders/{order_id}")
    suspend fun getOrderDetails(@Path("order_id") orderId: String): OrderDetailsResponse

    @GET("api/v1/orders/{order_id}/status-history")
    suspend fun getStatusHistory(@Path("order_id") orderId: String): StatusHistoryResponse

    @POST("api/v1/orders/{order_id}/confirm")
    suspend fun confirmOrder(@Path("order_id") orderId: String)

    @POST("api/v1/orders/admin/{order_id}/reject")
    suspend fun rejectOrder(@Path("order_id") orderId: String, @Body request: RejectRequest)

    @POST("api/v1/orders/{order_id}/process")
    suspend fun processOrder(@Path("order_id") orderId: String)

    @POST("api/v1/orders/{order_id}/dispatch")
    suspend fun dispatchOrder(@Path("order_id") orderId: String, @Body request: DispatchOrderRequest?)

    @POST("api/v1/orders/{order_id}/deliver")
    suspend fun deliverOrder(@Path("order_id") orderId: String)

    @POST("api/v1/orders/admin/{order_id}/cancel")
    suspend fun cancelOrder(@Path("order_id") orderId: String)

    // =============================================================================
    // Approval Endpoints
    // =============================================================================

    @GET("api/v1/orders/admin/review-queue")
    suspend fun listReviewQueue(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("mandatory_only") mandatoryOnly: Boolean? = null
    ): ReviewQueueResponse

    @POST("api/v1/orders/admin/{order_id}/approve")
    suspend fun approveOrder(@Path("order_id") orderId: String)

    @POST("api/v1/orders/admin/{order_id}/reject-for-approval")
    suspend fun rejectForApproval(
        @Path("order_id") orderId: String,
        @Body request: RejectApprovalRequest
    )

    @GET("api/v1/admin/approval-policy")
    suspend fun getApprovalPolicy(): ApprovalPolicyDto

    @PUT("api/v1/admin/approval-policy")
    suspend fun updateApprovalPolicy(@Body request: UpdatePolicyRequest): ApprovalPolicyDto
}

// =============================================================================
// Request DTOs
// =============================================================================

/**
 * Reject order request body.
 * Backend expects: { "reason": "..." }
 */
@Serializable
data class RejectRequest(val reason: String)

/**
 * Reject for approval request body.
 * Backend expects: { "reason": "..." }
 */
@Serializable
data class RejectApprovalRequest(@SerialName("reason") val reason: String)

/**
 * Update approval policy request body.
 */
@Serializable
data class UpdatePolicyRequest(
    @SerialName("auto_approval_limit") val autoApprovalLimit: Double,
    @SerialName("mandatory_review_threshold") val mandatoryReviewThreshold: Double,
    @SerialName("enabled") val enabled: Boolean
)

// =============================================================================
// Response DTOs
// =============================================================================

/**
 * Paginated list of orders response.
 * Backend returns: { "items": [...], "total": N, "page": N, "size": N, "total_pages": N }
 */
@Serializable
data class OrdersListResponse(
    @SerialName("items") val items: List<OrderSummaryDto>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int,
    @SerialName("size") val size: Int,
    @SerialName("total_pages") val totalPages: Int
)

/**
 * Order summary DTO for the list response.
 * Backend fields: id, status, fulfillment_status, currency, subtotal_amount, total_amount,
 * items, delivery_recipient_name, delivery_phone, delivery_city, delivery_area,
 * delivery_street, delivery_building_number, delivery_apartment, delivery_instructions, created_at
 */
@Serializable
data class OrderSummaryDto(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("status") val status: String,
    @SerialName("fulfillment_status") val fulfillmentStatus: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("subtotal_amount") val subtotalAmount: Double? = null,
    @SerialName("delivery_fee_amount") val deliveryFeeAmount: Double? = null,
    @SerialName("discount_amount") val discountAmount: Double? = null,
    @SerialName("currency") val currency: String,
    @SerialName("items") val items: List<OrderItemDto>,
    @SerialName("delivery_recipient_name") val deliveryRecipientName: String? = null,
    @SerialName("delivery_phone") val deliveryPhone: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("delivery_area") val deliveryArea: String? = null,
    @SerialName("delivery_street") val deliveryStreet: String? = null,
    @SerialName("delivery_building_number") val deliveryBuildingNumber: String? = null,
    @SerialName("delivery_apartment") val deliveryApartment: String? = null,
    @SerialName("delivery_instructions") val deliveryInstructions: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("approval_decision") val approvalDecision: ApprovalDecisionDto? = null
)

/**
 * Full order details response.
 * Backend fields: id, status, fulfillment_status, currency, subtotal_amount, total_amount,
 * items, delivery_recipient_name, delivery_phone, delivery_city, delivery_area,
 * delivery_street, delivery_building_number, delivery_apartment, delivery_instructions, created_at
 */
@Serializable
data class OrderDetailsResponse(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("status") val status: String,
    @SerialName("fulfillment_status") val fulfillmentStatus: String,
    @SerialName("items") val items: List<OrderItemDto>,
    @SerialName("subtotal_amount") val subtotalAmount: Double? = null,
    @SerialName("delivery_fee_amount") val deliveryFeeAmount: Double? = null,
    @SerialName("discount_amount") val discountAmount: Double? = null,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("delivery_recipient_name") val deliveryRecipientName: String? = null,
    @SerialName("delivery_phone") val deliveryPhone: String? = null,
    @SerialName("delivery_city") val deliveryCity: String? = null,
    @SerialName("delivery_area") val deliveryArea: String? = null,
    @SerialName("delivery_street") val deliveryStreet: String? = null,
    @SerialName("delivery_building_number") val deliveryBuildingNumber: String? = null,
    @SerialName("delivery_apartment") val deliveryApartment: String? = null,
    @SerialName("delivery_instructions") val deliveryInstructions: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("approval_decision") val approvalDecision: ApprovalDecisionDto? = null,
    @SerialName("shipment_info") val shipmentInfo: ShipmentDto? = null
)

/**
 * Order item DTO.
 * Backend fields: id, variant_id, product_name, sku, quantity, unit_price_amount,
 * unit_price_currency, subtotal_amount
 */
@Serializable
data class OrderItemDto(
    @SerialName("id") val id: String? = null,
    @SerialName("variant_id") val variantId: String,
    @SerialName("product_name") val productName: String,
    @SerialName("sku") val sku: String? = null,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unit_price_amount") val unitPriceAmount: Double,
    @SerialName("unit_price_currency") val unitPriceCurrency: String? = null,
    @SerialName("subtotal_amount") val subtotalAmount: Double? = null
)

/**
 * Status history response.
 * Backend returns: { "history": [...] }
 */
@Serializable
data class StatusHistoryResponse(
    @SerialName("history") val history: List<StatusHistoryEntryDto>
)

/**
 * Status history entry DTO.
 */
@Serializable
data class StatusHistoryEntryDto(
    @SerialName("previous_status") val previousStatus: String? = null,
    @SerialName("new_status") val newStatus: String,
    @SerialName("status_type") val statusType: String,
    @SerialName("actor_role") val actorRole: String? = null,
    @SerialName("reason") val reason: String? = null,
    @SerialName("timestamp") val timestamp: String
)

// =============================================================================
// Approval DTOs
// =============================================================================

/**
 * Approval decision snapshot DTO.
 */
@Serializable
data class ApprovalDecisionDto(
    @SerialName("decision") val decision: String,
    @SerialName("evaluated_amount") val evaluatedAmount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("decision_reason") val decisionReason: String? = null,
    @SerialName("policy_version") val policyVersion: Int? = null,
    @SerialName("evaluated_at") val evaluatedAt: String,
    @SerialName("approved_by") val approvedBy: String? = null,
    @SerialName("approved_at") val approvedAt: String? = null
)

/**
 * Approval policy configuration DTO.
 */
@Serializable
data class ApprovalPolicyDto(
    @SerialName("auto_approval_limit") val autoApprovalLimit: Double,
    @SerialName("mandatory_review_threshold") val mandatoryReviewThreshold: Double,
    @SerialName("currency") val currency: String,
    @SerialName("enabled") val enabled: Boolean,
    @SerialName("version") val version: Int? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("updated_by") val updatedBy: String? = null
)

/**
 * Review queue order DTO.
 */
@Serializable
data class ReviewQueueOrderDto(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("status") val status: String,
    @SerialName("fulfillment_status") val fulfillmentStatus: String,
    @SerialName("approval_status") val approvalStatus: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("items") val items: List<OrderItemDto>,
    @SerialName("delivery_recipient_name") val deliveryRecipientName: String? = null,
    @SerialName("delivery_phone") val deliveryPhone: String? = null,
    @SerialName("approval_decision") val approvalDecision: ApprovalDecisionDto? = null,
    @SerialName("is_mandatory") val isMandatory: Boolean
)

/**
 * Paginated review queue response.
 */
@Serializable
data class ReviewQueueResponse(
    @SerialName("items") val items: List<ReviewQueueOrderDto>,
    @SerialName("total") val total: Int,
    @SerialName("page") val page: Int,
    @SerialName("size") val size: Int,
    @SerialName("total_pages") val totalPages: Int
)

// =============================================================================
// Shipment DTOs
// =============================================================================

/**
 * Shipment information DTO.
 * Backend fields: delivery_notes (admin-only), customer_delivery_note,
 * dispatched_at, dispatched_by, tracking_number, carrier
 */
@Serializable
data class ShipmentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("delivery_notes") val deliveryNotes: String? = null,
    @SerialName("customer_delivery_note") val customerDeliveryNote: String? = null,
    @SerialName("dispatched_at") val dispatchedAt: String? = null,
    @SerialName("dispatched_by") val dispatchedBy: String? = null,
    @SerialName("tracking_number") val trackingNumber: String? = null,
    @SerialName("carrier") val carrier: String? = null
)

/**
 * Dispatch order request body.
 * Backend expects: { "delivery_notes": "...", "customer_delivery_note": "..." }
 */
@Serializable
data class DispatchOrderRequest(
    @SerialName("delivery_notes") val deliveryNotes: String? = null,
    @SerialName("customer_delivery_note") val customerDeliveryNote: String? = null
)
