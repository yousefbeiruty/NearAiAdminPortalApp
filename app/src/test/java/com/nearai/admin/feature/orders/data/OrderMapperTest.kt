package com.nearai.admin.feature.orders.data

import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderStatus
import org.junit.Assert.*
import org.junit.Test

class OrderMapperTest {

    @Test
    fun `mapOrderSummary converts DTO to domain model`() {
        val dto = OrderSummaryDto(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "awaiting_confirmation",
            totalAmount = 45.50,
            currency = "USD",
            recipientName = "John Doe",
            recipientPhone = "+1234567890"
        )

        val summary = OrderMapper.mapOrderSummary(dto)

        assertEquals("abc-123", summary.id)
        assertEquals(OrderStatus.PENDING, summary.status)
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, summary.fulfillmentStatus)
        assertEquals(45.50, summary.totalAmount, 0.001)
        assertEquals("USD", summary.currency)
        assertEquals("John Doe", summary.recipientName)
    }

    @Test
    fun `mapOrderSummary handles nullable fields`() {
        val dto = OrderSummaryDto(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "awaiting_confirmation",
            totalAmount = 45.50,
            currency = "USD",
            recipientName = null,
            recipientPhone = null
        )

        val summary = OrderMapper.mapOrderSummary(dto)

        assertNull(summary.recipientName)
        assertNull(summary.recipientPhone)
    }

    @Test
    fun `mapOrderDetails converts DTO to domain model`() {
        val dto = OrderDetailsResponse(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "awaiting_confirmation",
            items = listOf(
                OrderItemDto("prod-1", "Widget A", 3, 29.99, 89.97)
            ),
            subtotal = 89.97,
            totalAmount = 89.97,
            currency = "USD",
            recipientName = "John Doe",
            recipientPhone = "+1234567890",
            deliveryAddress = "123 Main St",
            buildingNumber = "4A",
            apartment = "Apt 5",
            deliveryInstructions = "Leave at door"
        )

        val details = OrderMapper.mapOrderDetails(dto)

        assertEquals("abc-123", details.id)
        assertEquals(OrderStatus.PENDING, details.status)
        assertEquals(1, details.items.size)
        assertEquals("Widget A", details.items[0].productName)
        assertEquals(3, details.items[0].quantity)
        assertEquals("123 Main St", details.deliveryAddress)
        assertEquals("4A", details.buildingNumber)
        assertEquals("Apt 5", details.apartment)
        assertEquals("Leave at door", details.deliveryInstructions)
    }

    @Test
    fun `mapOrderDetails handles nullable delivery fields`() {
        val dto = OrderDetailsResponse(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "awaiting_confirmation",
            items = emptyList(),
            subtotal = 0.0,
            totalAmount = 0.0,
            currency = "USD",
            recipientName = null,
            recipientPhone = null,
            deliveryAddress = null,
            buildingNumber = null,
            apartment = null,
            deliveryInstructions = null
        )

        val details = OrderMapper.mapOrderDetails(dto)

        assertNull(details.recipientName)
        assertNull(details.recipientPhone)
        assertNull(details.deliveryAddress)
        assertNull(details.buildingNumber)
        assertNull(details.apartment)
        assertNull(details.deliveryInstructions)
    }

    @Test
    fun `mapOrderItem converts DTO to domain model`() {
        val dto = OrderItemDto("prod-1", "Widget A", 3, 29.99, 89.97)

        val item = OrderMapper.mapOrderItem(dto)

        assertEquals("prod-1", item.productId)
        assertEquals("Widget A", item.productName)
        assertEquals(3, item.quantity)
        assertEquals(29.99, item.price, 0.001)
        assertEquals(89.97, item.lineTotal!!, 0.001)
    }

    @Test
    fun `mapOrderItem handles nullable lineTotal`() {
        val dto = OrderItemDto("prod-1", "Widget A", 3, 29.99, null)

        val item = OrderMapper.mapOrderItem(dto)

        assertNull(item.lineTotal)
    }

    @Test
    fun `mapStatusHistoryEntry converts DTO to domain model`() {
        val dto = StatusHistoryEntryDto(
            previousStatus = "pending",
            newStatus = "confirmed",
            statusType = "fulfillment",
            actorRole = "admin",
            reason = "Manual confirmation",
            timestamp = "2024-01-15T11:00:00Z"
        )

        val entry = OrderMapper.mapStatusHistoryEntry(dto)

        assertEquals("pending", entry.previousStatus)
        assertEquals("confirmed", entry.newStatus)
        assertEquals("fulfillment", entry.statusType)
        assertEquals("admin", entry.actorRole)
        assertEquals("Manual confirmation", entry.reason)
        assertEquals("2024-01-15T11:00:00Z", entry.timestamp)
    }

    @Test
    fun `mapStatusHistoryEntry handles nullable fields`() {
        val dto = StatusHistoryEntryDto(
            previousStatus = null,
            newStatus = "pending",
            statusType = "order",
            actorRole = null,
            reason = null,
            timestamp = "2024-01-15T10:30:00Z"
        )

        val entry = OrderMapper.mapStatusHistoryEntry(dto)

        assertNull(entry.previousStatus)
        assertNull(entry.actorRole)
        assertNull(entry.reason)
    }

    @Test
    fun `mapPaginatedOrders converts DTO to domain model`() {
        val dto = OrdersListResponse(
            items = listOf(
                OrderSummaryDto(
                    id = "abc-123",
                    createdAt = "2024-01-15T10:30:00Z",
                    status = "pending",
                    fulfillmentStatus = "awaiting_confirmation",
                    totalAmount = 45.50,
                    currency = "USD",
                    recipientName = "John Doe",
                    recipientPhone = "+1234567890"
                )
            ),
            page = 1,
            size = 20,
            totalPages = 5,
            totalItems = 100
        )

        val paginated = OrderMapper.mapPaginatedOrders(dto)

        assertEquals(1, paginated.orders.size)
        assertEquals(1, paginated.page)
        assertEquals(20, paginated.size)
        assertEquals(5, paginated.totalPages)
        assertEquals(100, paginated.totalItems)
    }

    @Test
    fun `OrderStatus fromString handles lowercase`() {
        assertEquals(OrderStatus.PENDING, OrderStatus.fromString("pending"))
        assertEquals(OrderStatus.COMPLETED, OrderStatus.fromString("completed"))
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromString("cancelled"))
        assertEquals(OrderStatus.REJECTED, OrderStatus.fromString("rejected"))
    }

    @Test
    fun `OrderStatus fromString handles uppercase`() {
        assertEquals(OrderStatus.PENDING, OrderStatus.fromString("PENDING"))
        assertEquals(OrderStatus.COMPLETED, OrderStatus.fromString("COMPLETED"))
    }

    @Test
    fun `OrderStatus fromString falls back to PENDING for unknown`() {
        assertEquals(OrderStatus.PENDING, OrderStatus.fromString("unknown_status"))
    }

    @Test
    fun `FulfillmentStatus fromString handles snake_case`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.fromString("awaiting_confirmation"))
        assertEquals(FulfillmentStatus.CONFIRMED, FulfillmentStatus.fromString("confirmed"))
        assertEquals(FulfillmentStatus.PROCESSING, FulfillmentStatus.fromString("processing"))
        assertEquals(FulfillmentStatus.OUT_FOR_DELIVERY, FulfillmentStatus.fromString("out_for_delivery"))
        assertEquals(FulfillmentStatus.DELIVERED, FulfillmentStatus.fromString("delivered"))
    }

    @Test
    fun `FulfillmentStatus fromString falls back to AWAITING_CONFIRMATION for unknown`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.fromString("unknown_status"))
    }

    @Test
    fun `empty paginated orders maps correctly`() {
        val dto = OrdersListResponse(
            items = emptyList(),
            page = 1,
            size = 20,
            totalPages = 0,
            totalItems = 0
        )

        val paginated = OrderMapper.mapPaginatedOrders(dto)

        assertTrue(paginated.orders.isEmpty())
        assertEquals(0, paginated.totalItems)
        assertEquals(0, paginated.totalPages)
    }
}
