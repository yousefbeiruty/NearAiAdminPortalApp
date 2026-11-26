package com.nearai.admin.test

import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.LowStockAlert
import com.nearai.admin.feature.dashboard.domain.RecentOrder
import com.nearai.admin.feature.dashboard.domain.RecentRegistration
import com.nearai.admin.feature.dashboard.domain.RevenueByCurrency
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderDetails
import com.nearai.admin.feature.orders.domain.OrderItem
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary
import com.nearai.admin.feature.orders.domain.PaginatedOrders
import com.nearai.admin.feature.orders.domain.StatusHistoryEntry
import java.math.BigDecimal

/**
 * Deterministic test data factory for UI tests.
 *
 * All values are fixed so that screenshot baselines remain stable across runs.
 * Never use random or time-based values in test data.
 */
object TestDataFactory {

    // ========================================================================
    // Dashboard
    // ========================================================================

    fun dashboardDataPopulated(): DashboardData = DashboardData(
        totalUsers = 14820,
        totalProducts = 342,
        totalCategories = 12,
        totalOrders = 1250,
        pendingOrders = 45,
        completedOrders = 1180,
        cancelledOrders = 15,
        rejectedOrders = 10,
        totalRevenue = parseDecimalAmount("6071.25"),
        revenueCurrency = "USD",
        revenueByCurrency = listOf(
            RevenueByCurrency("USD", parseDecimalAmount("4820.50")),
            RevenueByCurrency("EUR", parseDecimalAmount("1250.75"))
        ),
        recentOrders = listOf(
            RecentOrder(
                "ord-001", "Alice Martin", "2026-11-01", "completed", parseDecimalAmount("120.00"), "USD"
            ),
            RecentOrder(
                "ord-002", "Bob Smith", "2026-11-02", "pending", parseDecimalAmount("85.50"), "USD"
            ),
            RecentOrder(
                "ord-003", "Carol White", "2026-11-03", "completed", parseDecimalAmount("210.00"), "EUR"
            )
        ),
        recentRegistrations = listOf(
            RecentRegistration("usr-001", "alice@example.com", "customer", "2026-11-01"),
            RecentRegistration("usr-002", "bob@example.com", "customer", "2026-11-02")
        ),
        lowStockAlerts = listOf(
            LowStockAlert("var-001", "Wireless Mouse", "WM-001", 5, 2, 3),
            LowStockAlert("var-002", "USB-C Cable", "UC-002", 3, 1, 2)
        )
    )

    fun dashboardDataEmpty(): DashboardData = DashboardData()

    // ========================================================================
    // Orders List
    // ========================================================================

    fun paginatedOrdersPopulated(): PaginatedOrders = PaginatedOrders(
        orders = listOf(
            orderSummary(
                id = "ord-001",
                status = OrderStatus.COMPLETED,
                fulfillmentStatus = FulfillmentStatus.DELIVERED,
                totalAmount = parseDecimalAmount("120.00"),
                currency = "USD",
                recipientName = "Alice Martin",
                city = "New York"
            ),
            orderSummary(
                id = "ord-002",
                status = OrderStatus.PENDING,
                fulfillmentStatus = FulfillmentStatus.AWAITING_CONFIRMATION,
                totalAmount = parseDecimalAmount("85.50"),
                currency = "USD",
                recipientName = "Bob Smith",
                city = "London"
            ),
            orderSummary(
                id = "ord-003",
                status = OrderStatus.PENDING,
                fulfillmentStatus = FulfillmentStatus.OUT_FOR_DELIVERY,
                totalAmount = parseDecimalAmount("210.00"),
                currency = "EUR",
                recipientName = "Carol White",
                city = "Paris"
            )
        ),
        page = 1,
        size = 20,
        totalPages = 1,
        totalItems = 3
    )

    fun paginatedOrdersEmpty(): PaginatedOrders = PaginatedOrders(
        orders = emptyList(),
        page = 1,
        size = 20,
        totalPages = 0,
        totalItems = 0
    )

    private fun orderSummary(
        id: String,
        status: OrderStatus,
        fulfillmentStatus: FulfillmentStatus,
        totalAmount: BigDecimal,
        currency: String,
        recipientName: String?,
        city: String?
    ): OrderSummary = OrderSummary(
        id = id,
        createdAt = "2026-11-01T10:00:00Z",
        status = status,
        fulfillmentStatus = fulfillmentStatus,
        totalAmount = totalAmount,
        subtotalAmount = totalAmount,
        deliveryFeeAmount = BigDecimal.ZERO,
        discountAmount = BigDecimal.ZERO,
        currency = currency,
        items = listOf(
            OrderItem(
                "var-001", "Wireless Mouse", "WM-001", 2,
                parseDecimalAmount("25.00"), "USD", parseDecimalAmount("50.00")
            ),
            OrderItem(
                "var-002", "USB-C Cable", "UC-002", 1,
                parseDecimalAmount("15.00"), "USD", parseDecimalAmount("15.00")
            )
        ),
        deliveryRecipientName = recipientName,
        deliveryPhone = "+1-555-0100",
        deliveryCity = city,
        deliveryArea = "Downtown",
        deliveryStreet = "Main Street",
        deliveryBuildingNumber = "123",
        deliveryApartment = "4B",
        deliveryInstructions = "Leave at front door"
    )

    // ========================================================================
    // Order Details
    // ========================================================================

    fun orderDetailsPendingOutForDelivery(): OrderDetails = OrderDetails(
        id = "ord-003",
        createdAt = "2026-11-03T14:30:00Z",
        status = OrderStatus.PENDING,
        fulfillmentStatus = FulfillmentStatus.OUT_FOR_DELIVERY,
        items = listOf(
            OrderItem(
                "var-010", "Mechanical Keyboard", "MK-010", 1,
                parseDecimalAmount("150.00"), "EUR", parseDecimalAmount("150.00")
            ),
            OrderItem(
                "var-011", "Keyboard Wrist Rest", "KW-011", 1,
                parseDecimalAmount("60.00"), "EUR", parseDecimalAmount("60.00")
            )
        ),
        subtotalAmount = parseDecimalAmount("210.00"),
        deliveryFeeAmount = BigDecimal.ZERO,
        discountAmount = BigDecimal.ZERO,
        totalAmount = parseDecimalAmount("210.00"),
        currency = "EUR",
        deliveryRecipientName = "Carol White",
        deliveryPhone = "+33-555-0200",
        deliveryCity = "Paris",
        deliveryArea = "Le Marais",
        deliveryStreet = "Rue de Rivoli",
        deliveryBuildingNumber = "45",
        deliveryApartment = null,
        deliveryInstructions = "Ring bell twice"
    )

    fun orderDetailsCompletedDelivered(): OrderDetails = OrderDetails(
        id = "ord-001",
        createdAt = "2026-11-01T10:00:00Z",
        status = OrderStatus.COMPLETED,
        fulfillmentStatus = FulfillmentStatus.DELIVERED,
        items = listOf(
            OrderItem(
                "var-001", "Wireless Mouse", "WM-001", 2,
                parseDecimalAmount("25.00"), "USD", parseDecimalAmount("50.00")
            ),
            OrderItem(
                "var-002", "USB-C Cable", "UC-002", 1,
                parseDecimalAmount("15.00"), "USD", parseDecimalAmount("15.00")
            )
        ),
        subtotalAmount = parseDecimalAmount("120.00"),
        deliveryFeeAmount = BigDecimal.ZERO,
        discountAmount = BigDecimal.ZERO,
        totalAmount = parseDecimalAmount("120.00"),
        currency = "USD",
        deliveryRecipientName = "Alice Martin",
        deliveryPhone = "+1-555-0100",
        deliveryCity = "New York",
        deliveryArea = "Manhattan",
        deliveryStreet = "5th Avenue",
        deliveryBuildingNumber = "100",
        deliveryApartment = "12A",
        deliveryInstructions = null
    )

    fun statusHistoryPopulated(): List<StatusHistoryEntry> = listOf(
        StatusHistoryEntry(null, "pending", "order", "system", null, "2026-11-03T14:30:00Z"),
        StatusHistoryEntry("awaiting_confirmation", "confirmed", "fulfillment", "admin", null, "2026-11-03T15:00:00Z"),
        StatusHistoryEntry("confirmed", "processing", "fulfillment", "admin", null, "2026-11-03T16:00:00Z"),
        StatusHistoryEntry("processing", "out_for_delivery", "fulfillment", "driver", null, "2026-11-04T08:00:00Z")
    )
}

private fun parseDecimalAmount(value: String): BigDecimal = BigDecimal(value)
