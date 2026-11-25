package com.nearai.admin.feature.orders.domain

import com.nearai.admin.core.common.Result
import java.math.BigDecimal

/**
 * Domain model for a single item in an order.
 */
data class OrderItem(
    val variantId: String,
    val productName: String,
    val sku: String?,
    val quantity: Int,
    val unitPriceAmount: BigDecimal,
    val unitPriceCurrency: String?,
    val subtotalAmount: BigDecimal?
)

/**
 * Compact order summary for the orders list screen.
 */
data class OrderSummary(
    val id: String,
    val createdAt: String,
    val status: OrderStatus,
    val fulfillmentStatus: FulfillmentStatus,
    val totalAmount: BigDecimal,
    val subtotalAmount: BigDecimal?,
    val deliveryFeeAmount: BigDecimal?,
    val discountAmount: BigDecimal?,
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
    val approvalDecision: ApprovalDecisionSnapshot? = null,
    val isGuest: Boolean = false
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
    val subtotalAmount: BigDecimal?,
    val deliveryFeeAmount: BigDecimal?,
    val discountAmount: BigDecimal?,
    val totalAmount: BigDecimal,
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
    val approvalDecision: ApprovalDecisionSnapshot? = null,
    val shipmentInfo: ShipmentInfo? = null,
    val isGuest: Boolean = false
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
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
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
    val evaluatedAmount: BigDecimal,
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
    val autoApprovalLimit: BigDecimal,
    val mandatoryReviewThreshold: BigDecimal,
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
    val totalAmount: BigDecimal,
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
 * Shipment information for an order.
 * Contains delivery notes (admin-only and customer-facing), tracking info, etc.
 */
data class ShipmentInfo(
    val id: String?,
    val orderId: String?,
    val courierName: String?,
    val trackingNumber: String?,
    val trackingUrl: String?,
    val dispatchedAt: String?,
    val dispatchedBy: String?,
    val deliveryNotes: String?,
    val customerDeliveryNote: String?,
    val estimatedDeliveryDate: String?
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
     * Dispatches a processed order for delivery with optional delivery notes.
     */
    suspend fun dispatchOrder(
        orderId: String,
        deliveryNotes: String? = null,
        customerDeliveryNote: String? = null
    ): Result<Unit>

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
        autoApprovalLimit: BigDecimal,
        mandatoryReviewThreshold: BigDecimal,
        enabled: Boolean
    ): Result<ApprovalPolicyConfig>
}
