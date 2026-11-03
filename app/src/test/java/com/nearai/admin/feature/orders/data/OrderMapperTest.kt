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
            subtotalAmount = 40.00,
            currency = "USD",
            items = listOf(
                OrderItemDto(variantId = "var-1", productName = "Widget", sku = "W-001", quantity = 2, unitPriceAmount = 20.0, unitPriceCurrency = "USD", subtotalAmount = 40.0)
            ),
            deliveryRecipientName = "John Doe",
            deliveryPhone = "+1234567890",
            deliveryCity = "New York",
            deliveryArea = "Manhattan",
            deliveryStreet = "5th Avenue",
            deliveryBuildingNumber = "100",
            deliveryApartment = "4B",
            deliveryInstructions = "Leave at door"
        )

        val summary = OrderMapper.mapOrderSummary(dto)

        assertEquals("abc-123", summary.id)
        assertEquals(OrderStatus.PENDING, summary.status)
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, summary.fulfillmentStatus)
        assertEquals(45.50, summary.totalAmount, 0.001)
        assertEquals(40.00, summary.subtotalAmount!!, 0.001)
        assertEquals("USD", summary.currency)
        assertEquals("John Doe", summary.deliveryRecipientName)
        assertEquals("+1234567890", summary.deliveryPhone)
        assertEquals("New York", summary.deliveryCity)
        assertEquals("Manhattan", summary.deliveryArea)
        assertEquals("5th Avenue", summary.deliveryStreet)
        assertEquals("100", summary.deliveryBuildingNumber)
        assertEquals("4B", summary.deliveryApartment)
        assertEquals("Leave at door", summary.deliveryInstructions)
        assertEquals(1, summary.items.size)
    }

    @Test
    fun `mapOrderSummary handles nullable fields`() {
        val dto = OrderSummaryDto(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "awaiting_confirmation",
            totalAmount = 45.50,
            subtotalAmount = null,
            currency = "USD",
            items = emptyList(),
            deliveryRecipientName = null,
            deliveryPhone = null,
            deliveryCity = null,
            deliveryArea = null,
            deliveryStreet = null,
            deliveryBuildingNumber = null,
            deliveryApartment = null,
            deliveryInstructions = null
        )

        val summary = OrderMapper.mapOrderSummary(dto)

        assertNull(summary.subtotalAmount)
        assertNull(summary.deliveryRecipientName)
        assertNull(summary.deliveryPhone)
        assertNull(summary.deliveryCity)
        assertNull(summary.deliveryArea)
        assertNull(summary.deliveryStreet)
        assertNull(summary.deliveryBuildingNumber)
        assertNull(summary.deliveryApartment)
        assertNull(summary.deliveryInstructions)
        assertTrue(summary.items.isEmpty())
    }

    @Test
    fun `mapOrderDetails converts DTO to domain model`() {
        val dto = OrderDetailsResponse(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "awaiting_confirmation",
            items = listOf(
                OrderItemDto(variantId = "var-1", productName = "Widget A", sku = "W-001", quantity = 3, unitPriceAmount = 29.99, unitPriceCurrency = "USD", subtotalAmount = 89.97)
            ),
            subtotalAmount = 89.97,
            totalAmount = 89.97,
            currency = "USD",
            deliveryRecipientName = "John Doe",
            deliveryPhone = "+1234567890",
            deliveryCity = "New York",
            deliveryArea = "Manhattan",
            deliveryStreet = "123 Main St",
            deliveryBuildingNumber = "4A",
            deliveryApartment = "Apt 5",
            deliveryInstructions = "Leave at door"
        )

        val details = OrderMapper.mapOrderDetails(dto)

        assertEquals("abc-123", details.id)
        assertEquals(OrderStatus.PENDING, details.status)
        assertEquals(1, details.items.size)
        assertEquals("Widget A", details.items[0].productName)
        assertEquals("var-1", details.items[0].variantId)
        assertEquals("W-001", details.items[0].sku)
        assertEquals(3, details.items[0].quantity)
        assertEquals(29.99, details.items[0].unitPriceAmount, 0.001)
        assertEquals("USD", details.items[0].unitPriceCurrency)
        assertEquals(89.97, details.items[0].subtotalAmount!!, 0.001)
        assertEquals("John Doe", details.deliveryRecipientName)
        assertEquals("+1234567890", details.deliveryPhone)
        assertEquals("New York", details.deliveryCity)
        assertEquals("Manhattan", details.deliveryArea)
        assertEquals("123 Main St", details.deliveryStreet)
        assertEquals("4A", details.deliveryBuildingNumber)
        assertEquals("Apt 5", details.deliveryApartment)
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
            subtotalAmount = null,
            totalAmount = 0.0,
            currency = "USD",
            deliveryRecipientName = null,
            deliveryPhone = null,
            deliveryCity = null,
            deliveryArea = null,
            deliveryStreet = null,
            deliveryBuildingNumber = null,
            deliveryApartment = null,
            deliveryInstructions = null
        )

        val details = OrderMapper.mapOrderDetails(dto)

        assertNull(details.subtotalAmount)
        assertNull(details.deliveryRecipientName)
        assertNull(details.deliveryPhone)
        assertNull(details.deliveryCity)
        assertNull(details.deliveryArea)
        assertNull(details.deliveryStreet)
        assertNull(details.deliveryBuildingNumber)
        assertNull(details.deliveryApartment)
        assertNull(details.deliveryInstructions)
    }

    @Test
    fun `mapOrderItem converts DTO to domain model`() {
        val dto = OrderItemDto(
            variantId = "var-1",
            productName = "Widget A",
            sku = "W-001",
            quantity = 3,
            unitPriceAmount = 29.99,
            unitPriceCurrency = "USD",
            subtotalAmount = 89.97
        )

        val item = OrderMapper.mapOrderItem(dto)

        assertEquals("var-1", item.variantId)
        assertEquals("Widget A", item.productName)
        assertEquals("W-001", item.sku)
        assertEquals(3, item.quantity)
        assertEquals(29.99, item.unitPriceAmount, 0.001)
        assertEquals("USD", item.unitPriceCurrency)
        assertEquals(89.97, item.subtotalAmount!!, 0.001)
    }

    @Test
    fun `mapOrderItem handles nullable fields`() {
        val dto = OrderItemDto(
            variantId = "var-1",
            productName = "Widget A",
            sku = null,
            quantity = 3,
            unitPriceAmount = 29.99,
            unitPriceCurrency = null,
            subtotalAmount = null
        )

        val item = OrderMapper.mapOrderItem(dto)

        assertNull(item.sku)
        assertNull(item.unitPriceCurrency)
        assertNull(item.subtotalAmount)
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
                    subtotalAmount = 40.00,
                    currency = "USD",
                    items = emptyList(),
                    deliveryRecipientName = "John Doe",
                    deliveryPhone = "+1234567890",
                    deliveryCity = null,
                    deliveryArea = null,
                    deliveryStreet = null,
                    deliveryBuildingNumber = null,
                    deliveryApartment = null,
                    deliveryInstructions = null
                )
            ),
            total = 100,
            page = 1,
            size = 20,
            totalPages = 5
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
        assertEquals(FulfillmentStatus.UNFULILLED, FulfillmentStatus.fromString("unfulfilled"))
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.fromString("awaiting_confirmation"))
        assertEquals(FulfillmentStatus.CONFIRMED, FulfillmentStatus.fromString("confirmed"))
        assertEquals(FulfillmentStatus.PROCESSING, FulfillmentStatus.fromString("processing"))
        assertEquals(FulfillmentStatus.OUT_FOR_DELIVERY, FulfillmentStatus.fromString("out_for_delivery"))
        assertEquals(FulfillmentStatus.DELIVERED, FulfillmentStatus.fromString("delivered"))
    }

    @Test
    fun `FulfillmentStatus fromString falls back to UNFULILLED for unknown`() {
        assertEquals(FulfillmentStatus.UNFULILLED, FulfillmentStatus.fromString("unknown_status"))
    }

    @Test
    fun `empty paginated orders maps correctly`() {
        val dto = OrdersListResponse(
            items = emptyList(),
            total = 0,
            page = 1,
            size = 20,
            totalPages = 0
        )

        val paginated = OrderMapper.mapPaginatedOrders(dto)

        assertTrue(paginated.orders.isEmpty())
        assertEquals(0, paginated.totalItems)
        assertEquals(0, paginated.totalPages)
    }
}
