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
    val deliveryInstructions: String?
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
    val deliveryInstructions: String?
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
}
