package com.nearai.admin.feature.orders.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model for a single item in an order.
 */
data class OrderItem(
    val variantId: String,
    val productName: String,
    val sku: String?,
    val quantity: Int,
    val unitPriceAmount: Double,
    val unitPriceCurrency: String?,
    val subtotalAmount: Double?
)

/**
 * Compact order summary for the orders list screen.
 */
data class OrderSummary(
    val id: String,
    val createdAt: String,
    val status: OrderStatus,
    val fulfillmentStatus: FulfillmentStatus,
    val totalAmount: Double,
    val subtotalAmount: Double?,
    val deliveryFeeAmount: Double?,
    val discountAmount: Double?,
    val currency: String,
    val items: List<OrderItem>,
    val deliveryRecipientName: String?,
    val deliveryPhone: String?,
    val deliveryCity: String?,
    val deliveryArea: String?,
    val deliveryStreet: String?,
    val deliveryBuildingNumber: String?,
    val deliveryApartment: String?,
    val deliveryInstructions: String?,
    val approvalStatus: ApprovalStatus? = null,
    val approvalDecision: ApprovalDecisionSnapshot? = null
)

/**
 * Full order details including items, delivery address, and payment summary.
 */
data class OrderDetails(
    val id: String,
    val createdAt: String,
    val status: OrderStatus,
    val fulfillmentStatus: FulfillmentStatus,
    val items: List<OrderItem>,
    val subtotalAmount: Double?,
    val deliveryFeeAmount: Double?,
    val discountAmount: Double?,
    val totalAmount: Double,
    val currency: String,
    val deliveryRecipientName: String?,
    val deliveryPhone: String?,
    val deliveryCity: String?,
    val deliveryArea: String?,
    val deliveryStreet: String?,
    val deliveryBuildingNumber: String?,
    val deliveryApartment: String?,
    val deliveryInstructions: String?,
    val approvalStatus: ApprovalStatus? = null,
    val approvalDecision: ApprovalDecisionSnapshot? = null
)

/**
 * Single entry in the order status history timeline.
 */
data class StatusHistoryEntry(
    val previousStatus: String?,
    val newStatus: String,
    val statusType: String, // "order" or "fulfillment"
    val actorRole: String?,
    val reason: String?,
    val timestamp: String
)

/**
 * Payment information for an order.
 */
data class PaymentInfo(
    val id: String,
    val orderId: String,
    val paymentMethod: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val collectedAt: String? = null,
    val collectedBy: String? = null
)

/**
 * Paginated list of order summaries.
 */
data class PaginatedOrders(
    val orders: List<OrderSummary>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

// =============================================================================
// Approval Domain Models
// =============================================================================

/**
 * Snapshot of the approval decision made by the automation engine or administrator.
 *
 * Preserved per-order so historical decisions remain unchanged after policy updates.
 */
data class ApprovalDecisionSnapshot(
    val decision: ApprovalStatus,
    val evaluatedAmount: Double,
    val currency: String,
    val decisionReason: String?,
    val policyVersion: Int?,
    val evaluatedAt: String,
    val approvedBy: String?,
    val approvedAt: String?
)

/**
 * Current approval policy configuration from the backend.
 *
 * Thresholds are backend-driven; never hardcoded in Android.
 */
data class ApprovalPolicyConfig(
    val autoApprovalLimit: Double,
    val mandatoryReviewThreshold: Double,
    val currency: String,
    val enabled: Boolean,
    val version: Int?,
    val updatedAt: String?,
    val updatedBy: String?
)

/**
 * Single entry in the review queue.
 */
data class ReviewQueueOrder(
    val id: String,
    val createdAt: String,
    val status: OrderStatus,
    val fulfillmentStatus: FulfillmentStatus,
    val approvalStatus: ApprovalStatus,
    val totalAmount: Double,
    val currency: String,
    val items: List<OrderItem>,
    val deliveryRecipientName: String?,
    val deliveryPhone: String?,
    val approvalDecision: ApprovalDecisionSnapshot?,
    val isMandatory: Boolean
)

/**
 * Paginated review queue response.
 */
data class PaginatedReviewQueue(
    val orders: List<ReviewQueueOrder>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Repository interface for order management operations.
 * Belongs in the domain layer so the presentation layer depends on an abstraction.
 */
interface OrderRepository {

    /**
     * Fetches a paginated list of orders with optional filters.
     */
    suspend fun listOrders(
        page: Int = 1,
        size: Int = 20,
        status: OrderStatus? = null,
        fulfillmentStatus: FulfillmentStatus? = null
    ): Result<PaginatedOrders>

    /**
     * Fetches full details for a specific order.
     */
    suspend fun getOrderDetails(orderId: String): Result<OrderDetails>

    /**
     * Fetches the status history timeline for a specific order.
     */
    suspend fun getStatusHistory(orderId: String): Result<List<StatusHistoryEntry>>

    /**
     * Confirms a pending order.
     */
    suspend fun confirmOrder(orderId: String): Result<Unit>

    /**
     * Rejects a pending order with a mandatory reason.
     */
    suspend fun rejectOrder(orderId: String, reason: String): Result<Unit>

    /**
     * Processes a confirmed order.
     */
    suspend fun processOrder(orderId: String): Result<Unit>

    /**
     * Dispatches a processed order for delivery.
     */
    suspend fun dispatchOrder(orderId: String): Result<Unit>

    /**
     * Marks an order as delivered.
     */
    suspend fun deliverOrder(orderId: String): Result<Unit>

    /**
     * Cancels an order (admin only).
     */
    suspend fun cancelOrder(orderId: String): Result<Unit>

    /**
     * Fetches payment information for a specific order.
     */
    suspend fun getPaymentByOrderId(orderId: String): Result<PaymentInfo>

    /**
     * Collects COD cash for a payment.
     */
    suspend fun collectCash(paymentId: String): Result<PaymentInfo>

    // =============================================================================
    // Approval Operations
    // =============================================================================

    /**
     * Fetches a paginated list of orders awaiting approval review.
     */
    suspend fun listReviewQueue(
        page: Int = 1,
        size: Int = 20,
        mandatoryOnly: Boolean = false
    ): Result<PaginatedReviewQueue>

    /**
     * Approves an order that is awaiting manual review.
     */
    suspend fun approveOrder(orderId: String): Result<Unit>

    /**
     * Rejects an order that is awaiting manual approval with a reason.
     */
    suspend fun rejectForApproval(orderId: String, reason: String): Result<Unit>

    /**
     * Fetches the current approval policy configuration from the backend.
     */
    suspend fun getApprovalPolicy(): Result<ApprovalPolicyConfig>

    /**
     * Updates the approval policy configuration.
     */
    suspend fun updateApprovalPolicy(
        autoApprovalLimit: Double,
        mandatoryReviewThreshold: Double,
        enabled: Boolean
    ): Result<ApprovalPolicyConfig>
}
