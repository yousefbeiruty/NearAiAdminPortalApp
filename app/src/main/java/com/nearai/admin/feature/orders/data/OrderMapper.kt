package com.nearai.admin.feature.orders.data

import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderDetails
import com.nearai.admin.feature.orders.domain.OrderItem
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary
import com.nearai.admin.feature.orders.domain.PaginatedOrders
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
        currency = dto.currency,
        recipientName = dto.recipientName,
        recipientPhone = dto.recipientPhone
    )

    fun mapOrderDetails(dto: OrderDetailsResponse): OrderDetails = OrderDetails(
        id = dto.id,
        createdAt = dto.createdAt,
        status = OrderStatus.fromString(dto.status),
        fulfillmentStatus = FulfillmentStatus.fromString(dto.fulfillmentStatus),
        items = dto.items.map { mapOrderItem(it) },
        subtotal = dto.subtotal,
        totalAmount = dto.totalAmount,
        currency = dto.currency,
        recipientName = dto.recipientName,
        recipientPhone = dto.recipientPhone,
        deliveryAddress = dto.deliveryAddress,
        buildingNumber = dto.buildingNumber,
        apartment = dto.apartment,
        deliveryInstructions = dto.deliveryInstructions
    )

    fun mapOrderItem(dto: OrderItemDto): OrderItem = OrderItem(
        productId = dto.productId,
        productName = dto.productName,
        quantity = dto.quantity,
        price = dto.price,
        lineTotal = dto.lineTotal
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
        totalItems = dto.totalItems
    )
}
