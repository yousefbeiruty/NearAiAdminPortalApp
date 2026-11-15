package com.nearai.admin.feature.orders.data

import com.nearai.admin.feature.orders.domain.ApprovalDecisionSnapshot
import com.nearai.admin.feature.orders.domain.ApprovalStatus
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.PaymentStatus
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
            deliveryFeeAmount = 5.50,
            discountAmount = 0.0,
            currency = "JOD",
            items = listOf(
                OrderItemDto(variantId = "var-1", productName = "Widget", sku = "W-001", quantity = 2, unitPriceAmount = 20.0, unitPriceCurrency = "JOD", subtotalAmount = 40.0)
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
        assertEquals(5.50, summary.deliveryFeeAmount!!, 0.001)
        assertEquals(0.0, summary.discountAmount!!, 0.001)
        assertEquals("JOD", summary.currency)
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
            deliveryFeeAmount = null,
            discountAmount = null,
            currency = "JOD",
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
        assertNull(summary.deliveryFeeAmount)
        assertNull(summary.discountAmount)
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
                OrderItemDto(variantId = "var-1", productName = "Widget A", sku = "W-001", quantity = 3, unitPriceAmount = 29.99, unitPriceCurrency = "JOD", subtotalAmount = 89.97)
            ),
            subtotalAmount = 84.47,
            deliveryFeeAmount = 5.50,
            discountAmount = 0.0,
            totalAmount = 89.97,
            currency = "JOD",
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
        assertEquals("JOD", details.items[0].unitPriceCurrency)
        assertEquals(89.97, details.items[0].subtotalAmount!!, 0.001)
        assertEquals(5.50, details.deliveryFeeAmount!!, 0.001)
        assertEquals(0.0, details.discountAmount!!, 0.001)
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
            deliveryFeeAmount = null,
            discountAmount = null,
            totalAmount = 0.0,
            currency = "JOD",
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
        assertNull(details.deliveryFeeAmount)
        assertNull(details.discountAmount)
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
            unitPriceCurrency = "JOD",
            subtotalAmount = 89.97
        )

        val item = OrderMapper.mapOrderItem(dto)

        assertEquals("var-1", item.variantId)
        assertEquals("Widget A", item.productName)
        assertEquals("W-001", item.sku)
        assertEquals(3, item.quantity)
        assertEquals(29.99, item.unitPriceAmount, 0.001)
        assertEquals("JOD", item.unitPriceCurrency)
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
                    deliveryFeeAmount = 5.50,
                    discountAmount = 0.0,
                    currency = "JOD",
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
    fun `OrderStatus parseOrPending handles lowercase`() {
        assertEquals(OrderStatus.PENDING, OrderStatus.parseOrPending("pending"))
        assertEquals(OrderStatus.COMPLETED, OrderStatus.parseOrPending("completed"))
        assertEquals(OrderStatus.CANCELLED, OrderStatus.parseOrPending("cancelled"))
        assertEquals(OrderStatus.REJECTED, OrderStatus.parseOrPending("rejected"))
    }

    @Test
    fun `OrderStatus parseOrPending handles uppercase`() {
        assertEquals(OrderStatus.PENDING, OrderStatus.parseOrPending("PENDING"))
        assertEquals(OrderStatus.COMPLETED, OrderStatus.parseOrPending("COMPLETED"))
    }

    @Test
    fun `OrderStatus parseOrPending falls back to PENDING for unknown`() {
        assertEquals(OrderStatus.PENDING, OrderStatus.parseOrPending("unknown_status"))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation handles snake_case`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("awaiting_confirmation"))
        assertEquals(FulfillmentStatus.CONFIRMED, FulfillmentStatus.parseOrAwaitingConfirmation("confirmed"))
        assertEquals(FulfillmentStatus.PROCESSING, FulfillmentStatus.parseOrAwaitingConfirmation("processing"))
        assertEquals(FulfillmentStatus.OUT_FOR_DELIVERY, FulfillmentStatus.parseOrAwaitingConfirmation("out_for_delivery"))
        assertEquals(FulfillmentStatus.DELIVERED, FulfillmentStatus.parseOrAwaitingConfirmation("delivered"))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation falls back to AWAITING_CONFIRMATION for unknown`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("unknown_status"))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation falls back for blank string`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation(""))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation falls back for whitespace`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("  "))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation handles uppercase`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("AWAITING_CONFIRMATION"))
        assertEquals(FulfillmentStatus.CONFIRMED, FulfillmentStatus.parseOrAwaitingConfirmation("CONFIRMED"))
        assertEquals(FulfillmentStatus.PROCESSING, FulfillmentStatus.parseOrAwaitingConfirmation("PROCESSING"))
        assertEquals(FulfillmentStatus.OUT_FOR_DELIVERY, FulfillmentStatus.parseOrAwaitingConfirmation("OUT_FOR_DELIVERY"))
        assertEquals(FulfillmentStatus.DELIVERED, FulfillmentStatus.parseOrAwaitingConfirmation("DELIVERED"))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation falls back for legacy unfulfilled`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("unfulfilled"))
    }

    @Test
    fun `FulfillmentStatus parseOrAwaitingConfirmation falls back for unknown future value`() {
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("returned"))
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, FulfillmentStatus.parseOrAwaitingConfirmation("refunded"))
    }

    // =============================================================================
    // Business safety — unknown fulfillment status cannot enable wrong actions
    // =============================================================================

    @Test
    fun `unknown fulfillment value maps to AWAITING_CONFIRMATION and does not match other actions`() {
        // Simulate the backend returning an unknown future fulfillment value
        val unknown = FulfillmentStatus.parseOrAwaitingConfirmation("returned")

        // The fallback is AWAITING_CONFIRMATION
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, unknown)

        // It must NOT match any other fulfillment-dependent action guard
        assertNotEquals(FulfillmentStatus.CONFIRMED, unknown)
        assertNotEquals(FulfillmentStatus.PROCESSING, unknown)
        assertNotEquals(FulfillmentStatus.OUT_FOR_DELIVERY, unknown)
        assertNotEquals(FulfillmentStatus.DELIVERED, unknown)
    }

    @Test
    fun `legacy unfulfilled maps to AWAITING_CONFIRMATION and does not match other actions`() {
        val legacy = FulfillmentStatus.parseOrAwaitingConfirmation("unfulfilled")

        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, legacy)
        assertNotEquals(FulfillmentStatus.CONFIRMED, legacy)
        assertNotEquals(FulfillmentStatus.PROCESSING, legacy)
        assertNotEquals(FulfillmentStatus.OUT_FOR_DELIVERY, legacy)
        assertNotEquals(FulfillmentStatus.DELIVERED, legacy)
    }

    @Test
    fun `blank fulfillment value maps to AWAITING_CONFIRMATION and does not match other actions`() {
        val blank = FulfillmentStatus.parseOrAwaitingConfirmation("")

        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, blank)
        assertNotEquals(FulfillmentStatus.CONFIRMED, blank)
        assertNotEquals(FulfillmentStatus.PROCESSING, blank)
        assertNotEquals(FulfillmentStatus.OUT_FOR_DELIVERY, blank)
        assertNotEquals(FulfillmentStatus.DELIVERED, blank)
    }

    @Test
    fun `all five canonical fulfillment statuses parse to distinct enum values`() {
        // Verify no two canonical values map to the same enum entry
        val statuses = listOf(
            FulfillmentStatus.parseOrAwaitingConfirmation("awaiting_confirmation"),
            FulfillmentStatus.parseOrAwaitingConfirmation("confirmed"),
            FulfillmentStatus.parseOrAwaitingConfirmation("processing"),
            FulfillmentStatus.parseOrAwaitingConfirmation("out_for_delivery"),
            FulfillmentStatus.parseOrAwaitingConfirmation("delivered")
        )

        assertEquals(5, statuses.toSet().size)
        assertEquals(FulfillmentStatus.AWAITING_CONFIRMATION, statuses[0])
        assertEquals(FulfillmentStatus.CONFIRMED, statuses[1])
        assertEquals(FulfillmentStatus.PROCESSING, statuses[2])
        assertEquals(FulfillmentStatus.OUT_FOR_DELIVERY, statuses[3])
        assertEquals(FulfillmentStatus.DELIVERED, statuses[4])
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

    // =============================================================================
    // ApprovalStatus enum parsing
    // =============================================================================

    @Test
    fun `ApprovalStatus parseOrAwaitingReview handles snake_case`() {
        assertEquals(ApprovalStatus.AUTO_APPROVED, ApprovalStatus.parseOrAwaitingReview("auto_approved"))
        assertEquals(ApprovalStatus.AWAITING_STANDARD_REVIEW, ApprovalStatus.parseOrAwaitingReview("awaiting_standard_review"))
        assertEquals(ApprovalStatus.AWAITING_MANDATORY_APPROVAL, ApprovalStatus.parseOrAwaitingReview("awaiting_mandatory_approval"))
        assertEquals(ApprovalStatus.MANUALLY_APPROVED, ApprovalStatus.parseOrAwaitingReview("manually_approved"))
        assertEquals(ApprovalStatus.REJECTED, ApprovalStatus.parseOrAwaitingReview("rejected"))
    }

    @Test
    fun `ApprovalStatus parseOrAwaitingReview handles uppercase`() {
        assertEquals(ApprovalStatus.AUTO_APPROVED, ApprovalStatus.parseOrAwaitingReview("AUTO_APPROVED"))
        assertEquals(ApprovalStatus.MANUALLY_APPROVED, ApprovalStatus.parseOrAwaitingReview("MANUALLY_APPROVED"))
    }

    @Test
    fun `ApprovalStatus parseOrAwaitingReview falls back for unknown`() {
        assertEquals(ApprovalStatus.AWAITING_STANDARD_REVIEW, ApprovalStatus.parseOrAwaitingReview("unknown_approval"))
    }

    // =============================================================================
    // Approval field mapping
    // =============================================================================

    @Test
    fun `mapOrderDetails maps approval status and decision`() {
        val dto = OrderDetailsResponse(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "confirmed",
            items = emptyList(),
            subtotalAmount = 120.0,
            deliveryFeeAmount = 0.0,
            discountAmount = 0.0,
            totalAmount = 120.0,
            currency = "JOD",
            deliveryRecipientName = "John Doe",
            deliveryPhone = "+1234567890",
            deliveryCity = null,
            deliveryArea = null,
            deliveryStreet = null,
            deliveryBuildingNumber = null,
            deliveryApartment = null,
            deliveryInstructions = null,
            approvalStatus = "auto_approved",
            approvalDecision = ApprovalDecisionDto(
                decision = "auto_approved",
                evaluatedAmount = 120.0,
                currency = "JOD",
                decisionReason = "Order total within auto-approval limit",
                policyVersion = 1,
                evaluatedAt = "2024-01-15T10:30:05Z",
                approvedBy = null,
                approvedAt = null
            )
        )

        val details = OrderMapper.mapOrderDetails(dto)

        assertEquals(ApprovalStatus.AUTO_APPROVED, details.approvalStatus)
        assertNotNull(details.approvalDecision)
        assertEquals(ApprovalStatus.AUTO_APPROVED, details.approvalDecision!!.decision)
        assertEquals(120.0, details.approvalDecision.evaluatedAmount, 0.001)
        assertEquals("JOD", details.approvalDecision.currency)
        assertEquals("Order total within auto-approval limit", details.approvalDecision.decisionReason)
        assertEquals(1, details.approvalDecision.policyVersion)
    }

    @Test
    fun `mapOrderDetails handles null approval fields for pre-automation orders`() {
        val dto = OrderDetailsResponse(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "confirmed",
            items = emptyList(),
            subtotalAmount = 120.0,
            deliveryFeeAmount = 0.0,
            discountAmount = 0.0,
            totalAmount = 120.0,
            currency = "JOD",
            deliveryRecipientName = "John Doe",
            deliveryPhone = "+1234567890",
            deliveryCity = null,
            deliveryArea = null,
            deliveryStreet = null,
            deliveryBuildingNumber = null,
            deliveryApartment = null,
            deliveryInstructions = null,
            approvalStatus = null,
            approvalDecision = null
        )

        val details = OrderMapper.mapOrderDetails(dto)

        assertNull(details.approvalStatus)
        assertNull(details.approvalDecision)
    }

    @Test
    fun `mapOrderSummary maps approval status and decision`() {
        val dto = OrderSummaryDto(
            id = "abc-123",
            createdAt = "2024-01-15T10:30:00Z",
            status = "pending",
            fulfillmentStatus = "confirmed",
            totalAmount = 120.0,
            subtotalAmount = 120.0,
            deliveryFeeAmount = 0.0,
            discountAmount = 0.0,
            currency = "JOD",
            items = emptyList(),
            deliveryRecipientName = "John Doe",
            deliveryPhone = "+1234567890",
            deliveryCity = null,
            deliveryArea = null,
            deliveryStreet = null,
            deliveryBuildingNumber = null,
            deliveryApartment = null,
            deliveryInstructions = null,
            approvalStatus = "manually_approved",
            approvalDecision = ApprovalDecisionDto(
                decision = "manually_approved",
                evaluatedAmount = 350.0,
                currency = "JOD",
                decisionReason = "Manual approval by admin",
                policyVersion = 1,
                evaluatedAt = "2024-01-15T10:30:05Z",
                approvedBy = "admin-1",
                approvedAt = "2024-01-15T11:00:00Z"
            )
        )

        val summary = OrderMapper.mapOrderSummary(dto)

        assertEquals(ApprovalStatus.MANUALLY_APPROVED, summary.approvalStatus)
        assertNotNull(summary.approvalDecision)
        assertEquals(ApprovalStatus.MANUALLY_APPROVED, summary.approvalDecision!!.decision)
        assertEquals("admin-1", summary.approvalDecision.approvedBy)
        assertEquals("2024-01-15T11:00:00Z", summary.approvalDecision.approvedAt)
    }

    @Test
    fun `mapApprovalDecision maps all fields`() {
        val dto = ApprovalDecisionDto(
            decision = "awaiting_mandatory_approval",
            evaluatedAmount = 750.0,
            currency = "JOD",
            decisionReason = "Order exceeds mandatory review threshold",
            policyVersion = 2,
            evaluatedAt = "2024-01-15T10:30:05Z",
            approvedBy = null,
            approvedAt = null
        )

        val snapshot = OrderMapper.mapApprovalDecision(dto)

        assertEquals(ApprovalStatus.AWAITING_MANDATORY_APPROVAL, snapshot.decision)
        assertEquals(750.0, snapshot.evaluatedAmount, 0.001)
        assertEquals("JOD", snapshot.currency)
        assertEquals("Order exceeds mandatory review threshold", snapshot.decisionReason)
        assertEquals(2, snapshot.policyVersion)
        assertEquals("2024-01-15T10:30:05Z", snapshot.evaluatedAt)
        assertNull(snapshot.approvedBy)
        assertNull(snapshot.approvedAt)
    }

    @Test
    fun `mapApprovalDecision handles nullable fields`() {
        val dto = ApprovalDecisionDto(
            decision = "auto_approved",
            evaluatedAmount = 50.0,
            currency = "JOD",
            decisionReason = null,
            policyVersion = null,
            evaluatedAt = "2024-01-15T10:30:05Z",
            approvedBy = null,
            approvedAt = null
        )

        val snapshot = OrderMapper.mapApprovalDecision(dto)

        assertEquals(ApprovalStatus.AUTO_APPROVED, snapshot.decision)
        assertNull(snapshot.decisionReason)
        assertNull(snapshot.policyVersion)
    }

    // =============================================================================
    // PaymentStatus enum parsing
    // =============================================================================

    @Test
    fun `PaymentStatus parseOrPending handles all six backend values`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.parseOrPending("pending"))
        assertEquals(PaymentStatus.COMPLETED, PaymentStatus.parseOrPending("completed"))
        assertEquals(PaymentStatus.COLLECTED, PaymentStatus.parseOrPending("collected"))
        assertEquals(PaymentStatus.FAILED, PaymentStatus.parseOrPending("failed"))
        assertEquals(PaymentStatus.CANCELLED, PaymentStatus.parseOrPending("cancelled"))
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.parseOrPending("refunded"))
    }

    @Test
    fun `PaymentStatus parseOrPending handles uppercase`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.parseOrPending("PENDING"))
        assertEquals(PaymentStatus.COMPLETED, PaymentStatus.parseOrPending("COMPLETED"))
        assertEquals(PaymentStatus.COLLECTED, PaymentStatus.parseOrPending("COLLECTED"))
        assertEquals(PaymentStatus.FAILED, PaymentStatus.parseOrPending("FAILED"))
        assertEquals(PaymentStatus.CANCELLED, PaymentStatus.parseOrPending("CANCELLED"))
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.parseOrPending("REFUNDED"))
    }

    @Test
    fun `PaymentStatus parseOrPending falls back to PENDING for unknown`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.parseOrPending("unknown_payment"))
    }

    @Test
    fun `PaymentStatus parseOrPending falls back for blank string`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.parseOrPending(""))
    }

    @Test
    fun `PaymentStatus parseOrPending falls back for whitespace`() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.parseOrPending("  "))
    }

    // =============================================================================
    // Business safety — unknown payment status cannot enable wrong actions
    // =============================================================================

    @Test
    fun `unknown payment value maps to PENDING and does not match other actions`() {
        val unknown = PaymentStatus.parseOrPending("returned")

        assertEquals(PaymentStatus.PENDING, unknown)
        assertNotEquals(PaymentStatus.COMPLETED, unknown)
        assertNotEquals(PaymentStatus.COLLECTED, unknown)
        assertNotEquals(PaymentStatus.FAILED, unknown)
        assertNotEquals(PaymentStatus.CANCELLED, unknown)
        assertNotEquals(PaymentStatus.REFUNDED, unknown)
    }

    @Test
    fun `blank payment value maps to PENDING and does not match other actions`() {
        val blank = PaymentStatus.parseOrPending("")

        assertEquals(PaymentStatus.PENDING, blank)
        assertNotEquals(PaymentStatus.COMPLETED, blank)
        assertNotEquals(PaymentStatus.COLLECTED, blank)
        assertNotEquals(PaymentStatus.FAILED, blank)
        assertNotEquals(PaymentStatus.CANCELLED, blank)
        assertNotEquals(PaymentStatus.REFUNDED, blank)
    }

    @Test
    fun `all six canonical payment statuses parse to distinct enum values`() {
        val statuses = listOf(
            PaymentStatus.parseOrPending("pending"),
            PaymentStatus.parseOrPending("completed"),
            PaymentStatus.parseOrPending("collected"),
            PaymentStatus.parseOrPending("failed"),
            PaymentStatus.parseOrPending("cancelled"),
            PaymentStatus.parseOrPending("refunded")
        )

        assertEquals(6, statuses.toSet().size)
        assertEquals(PaymentStatus.PENDING, statuses[0])
        assertEquals(PaymentStatus.COMPLETED, statuses[1])
        assertEquals(PaymentStatus.COLLECTED, statuses[2])
        assertEquals(PaymentStatus.FAILED, statuses[3])
        assertEquals(PaymentStatus.CANCELLED, statuses[4])
        assertEquals(PaymentStatus.REFUNDED, statuses[5])
    }

    // =============================================================================
    // Shipment Mapping Tests — Regression for carrier/courier_name API contract fix
    // =============================================================================

    @Test
    fun `mapShipmentInfo maps courier_name from backend to courierName in domain model`() {
        val dto = ShipmentDto(
            id = "ship-1",
            orderId = "order-1",
            courierName = "Aramex",
            trackingNumber = "TRK-12345",
            trackingUrl = "https://track.example.com/TRK-12345",
            dispatchedAt = "2024-01-15T14:00:00Z",
            dispatchedBy = "admin-1",
            deliveryNotes = "Handle with care",
            customerDeliveryNote = "Will be delivered tomorrow",
            estimatedDeliveryDate = "2024-01-16T18:00:00Z"
        )

        val shipment = OrderMapper.mapShipmentInfo(dto)

        assertEquals("ship-1", shipment.id)
        assertEquals("order-1", shipment.orderId)
        assertEquals("Aramex", shipment.courierName)
        assertEquals("TRK-12345", shipment.trackingNumber)
        assertEquals("https://track.example.com/TRK-12345", shipment.trackingUrl)
        assertEquals("2024-01-15T14:00:00Z", shipment.dispatchedAt)
        assertEquals("admin-1", shipment.dispatchedBy)
        assertEquals("Handle with care", shipment.deliveryNotes)
        assertEquals("Will be delivered tomorrow", shipment.customerDeliveryNote)
        assertEquals("2024-01-16T18:00:00Z", shipment.estimatedDeliveryDate)
    }

    @Test
    fun `mapShipmentInfo handles null courier_name gracefully`() {
        val dto = ShipmentDto(
            id = null,
            orderId = null,
            courierName = null,
            trackingNumber = null,
            trackingUrl = null,
            dispatchedAt = null,
            dispatchedBy = null,
            deliveryNotes = null,
            customerDeliveryNote = null,
            estimatedDeliveryDate = null
        )

        val shipment = OrderMapper.mapShipmentInfo(dto)

        assertNull(shipment.id)
        assertNull(shipment.orderId)
        assertNull(shipment.courierName)
        assertNull(shipment.trackingNumber)
        assertNull(shipment.trackingUrl)
        assertNull(shipment.dispatchedAt)
        assertNull(shipment.dispatchedBy)
        assertNull(shipment.deliveryNotes)
        assertNull(shipment.customerDeliveryNote)
        assertNull(shipment.estimatedDeliveryDate)
    }

    @Test
    fun `ShipmentDto serializes courier_name field correctly`() {
        // This test verifies that the @SerialName annotation matches the backend API field name.
        // The backend sends "courier_name", not "carrier". This regression test ensures
        // the DTO deserializes the correct JSON key so courier data is not silently lost.
        val json = """{
          "courier_name": "DHL Express",
          "tracking_number": "DHL-98765",
          "tracking_url": "https://dhl.com/track/DHL-98765",
          "dispatched_at": "2024-06-01T09:00:00Z",
          "delivery_notes": "Fragile",
          "customer_delivery_note": "Call before delivery",
          "estimated_delivery_date": "2024-06-02T17:00:00Z"
        }"""

        val dto = kotlinx.serialization.json.Json.Default.decodeFromString<ShipmentDto>(json)

        assertEquals("DHL Express", dto.courierName)
        assertEquals("DHL-98765", dto.trackingNumber)
        assertEquals("https://dhl.com/track/DHL-98765", dto.trackingUrl)
        assertEquals("2024-06-01T09:00:00Z", dto.dispatchedAt)
        assertEquals("Fragile", dto.deliveryNotes)
        assertEquals("Call before delivery", dto.customerDeliveryNote)
        assertEquals("2024-06-02T17:00:00Z", dto.estimatedDeliveryDate)
    }
}
