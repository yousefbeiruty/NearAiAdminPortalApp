package com.nearai.admin.feature.orders.data

import com.nearai.admin.feature.orders.domain.ApprovalDecisionSnapshot
import com.nearai.admin.feature.orders.domain.ApprovalPolicyConfig
import com.nearai.admin.feature.orders.domain.ApprovalStatus
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderDetails
import com.nearai.admin.feature.orders.domain.OrderItem
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary
import com.nearai.admin.feature.orders.domain.PaginatedOrders
import com.nearai.admin.feature.orders.domain.PaginatedReviewQueue
import com.nearai.admin.feature.orders.domain.ReviewQueueOrder
import com.nearai.admin.feature.orders.domain.StatusHistoryEntry

/**
 * Mapper functions that convert API DTOs to domain models.
 * Uses safe enum parsing so unknown backend values don't crash the app.
 */
object OrderMapper {

    fun mapOrderSummary(dto: OrderSummaryDto): OrderSummary = OrderSummary(
        id = dto.id,
        createdAt = dto.createdAt,
        status = OrderStatus.fromString(dto.status),
        fulfillmentStatus = FulfillmentStatus.fromString(dto.fulfillmentStatus),
        totalAmount = dto.totalAmount,
        subtotalAmount = dto.subtotalAmount,
        deliveryFeeAmount = dto.deliveryFeeAmount,
        discountAmount = dto.discountAmount,
        currency = dto.currency,
        items = dto.items.map { mapOrderItem(it) },
        deliveryRecipientName = dto.deliveryRecipientName,
        deliveryPhone = dto.deliveryPhone,
        deliveryCity = dto.deliveryCity,
        deliveryArea = dto.deliveryArea,
        deliveryStreet = dto.deliveryStreet,
        deliveryBuildingNumber = dto.deliveryBuildingNumber,
        deliveryApartment = dto.deliveryApartment,
        deliveryInstructions = dto.deliveryInstructions,
        approvalStatus = dto.approvalStatus?.let { ApprovalStatus.fromString(it) },
        approvalDecision = dto.approvalDecision?.let { mapApprovalDecision(it) }
    )

    fun mapOrderDetails(dto: OrderDetailsResponse): OrderDetails = OrderDetails(
        id = dto.id,
        createdAt = dto.createdAt,
        status = OrderStatus.fromString(dto.status),
        fulfillmentStatus = FulfillmentStatus.fromString(dto.fulfillmentStatus),
        items = dto.items.map { mapOrderItem(it) },
        subtotalAmount = dto.subtotalAmount,
        deliveryFeeAmount = dto.deliveryFeeAmount,
        discountAmount = dto.discountAmount,
        totalAmount = dto.totalAmount,
        currency = dto.currency,
        deliveryRecipientName = dto.deliveryRecipientName,
        deliveryPhone = dto.deliveryPhone,
        deliveryCity = dto.deliveryCity,
        deliveryArea = dto.deliveryArea,
        deliveryStreet = dto.deliveryStreet,
        deliveryBuildingNumber = dto.deliveryBuildingNumber,
        deliveryApartment = dto.deliveryApartment,
        deliveryInstructions = dto.deliveryInstructions,
        approvalStatus = dto.approvalStatus?.let { ApprovalStatus.fromString(it) },
        approvalDecision = dto.approvalDecision?.let { mapApprovalDecision(it) }
    )

    fun mapOrderItem(dto: OrderItemDto): OrderItem = OrderItem(
        variantId = dto.variantId,
        productName = dto.productName,
        sku = dto.sku,
        quantity = dto.quantity,
        unitPriceAmount = dto.unitPriceAmount,
        unitPriceCurrency = dto.unitPriceCurrency,
        subtotalAmount = dto.subtotalAmount
    )

    fun mapStatusHistoryEntry(dto: StatusHistoryEntryDto): StatusHistoryEntry = StatusHistoryEntry(
        previousStatus = dto.previousStatus,
        newStatus = dto.newStatus,
        statusType = dto.statusType,
        actorRole = dto.actorRole,
        reason = dto.reason,
        timestamp = dto.timestamp
    )

    fun mapPaginatedOrders(dto: OrdersListResponse): PaginatedOrders = PaginatedOrders(
        orders = dto.items.map { mapOrderSummary(it) },
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )

    // =============================================================================
    // Approval Mappings
    // =============================================================================

    fun mapApprovalDecision(dto: ApprovalDecisionDto): ApprovalDecisionSnapshot =
        ApprovalDecisionSnapshot(
            decision = ApprovalStatus.fromString(dto.decision),
            evaluatedAmount = dto.evaluatedAmount,
            currency = dto.currency,
            decisionReason = dto.decisionReason,
            policyVersion = dto.policyVersion,
            evaluatedAt = dto.evaluatedAt,
            approvedBy = dto.approvedBy,
            approvedAt = dto.approvedAt
        )

    fun mapReviewQueueOrder(dto: ReviewQueueOrderDto): ReviewQueueOrder = ReviewQueueOrder(
        id = dto.id,
        createdAt = dto.createdAt,
        status = OrderStatus.fromString(dto.status),
        fulfillmentStatus = FulfillmentStatus.fromString(dto.fulfillmentStatus),
        approvalStatus = ApprovalStatus.fromString(dto.approvalStatus),
        totalAmount = dto.totalAmount,
        currency = dto.currency,
        items = dto.items.map { mapOrderItem(it) },
        deliveryRecipientName = dto.deliveryRecipientName,
        deliveryPhone = dto.deliveryPhone,
        approvalDecision = dto.approvalDecision?.let { mapApprovalDecision(it) },
        isMandatory = dto.isMandatory
    )

    fun mapPaginatedReviewQueue(dto: ReviewQueueResponse): PaginatedReviewQueue =
        PaginatedReviewQueue(
            orders = dto.items.map { mapReviewQueueOrder(it) },
            page = dto.page,
            size = dto.size,
            totalPages = dto.totalPages,
            totalItems = dto.total
        )

    fun mapApprovalPolicy(dto: ApprovalPolicyDto): ApprovalPolicyConfig = ApprovalPolicyConfig(
        autoApprovalLimit = dto.autoApprovalLimit,
        mandatoryReviewThreshold = dto.mandatoryReviewThreshold,
        currency = dto.currency,
        enabled = dto.enabled,
        version = dto.version,
        updatedAt = dto.updatedAt,
        updatedBy = dto.updatedBy
    )
}
