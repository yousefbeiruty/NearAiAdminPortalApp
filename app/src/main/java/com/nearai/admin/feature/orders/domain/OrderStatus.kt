package com.nearai.admin.feature.orders.domain

/**
 * Order status enum matching the backend's order_status field.
 *
 * Backend values: pending, completed, cancelled, rejected
 */
enum class OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    REJECTED;

    companion object {
        /**
         * Safely parses a backend string value to [OrderStatus].
         * Falls back to [PENDING] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): OrderStatus {
            // Backend uses lowercase; try direct match first, then uppercase
            return values().find { it.name.lowercase() == value.lowercase() }
                ?: values().find { it.name == value.uppercase() }
                ?: PENDING
        }
    }
}

/**
 * Fulfillment status enum matching the backend's fulfillment_status field.
 *
 * Backend values: unfulfilled, awaiting_confirmation, confirmed, processing, out_for_delivery, delivered
 */
enum class FulfillmentStatus {
    UNFULILLED,
    AWAITING_CONFIRMATION,
    CONFIRMED,
    PROCESSING,
    OUT_FOR_DELIVERY,
    DELIVERED;

    companion object {
        /**
         * Safely parses a backend string value to [FulfillmentStatus].
         * Falls back to [UNFULILLED] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): FulfillmentStatus {
            // Backend uses snake_case; convert to UPPER_SNAKE_CASE for matching
            val normalized = value.lowercase().replace("_", "_")
            return values().find { it.name.lowercase() == normalized }
                ?: UNFULILLED
        }
    }
}
