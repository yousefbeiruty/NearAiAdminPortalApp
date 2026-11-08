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
 * Approval status enum matching the backend's approval_status field.
 *
 * Backend values: auto_approved, awaiting_standard_review, awaiting_mandatory_approval,
 * manually_approved, rejected
 *
 * This is separate from [OrderStatus] and [FulfillmentStatus] to avoid coupling
 * approval state with fulfillment or payment state.
 */
enum class ApprovalStatus {
    AUTO_APPROVED,
    AWAITING_STANDARD_REVIEW,
    AWAITING_MANDATORY_APPROVAL,
    MANUALLY_APPROVED,
    REJECTED;

    companion object {
        /**
         * Safely parses a backend string value to [ApprovalStatus].
         * Falls back to [AWAITING_STANDARD_REVIEW] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): ApprovalStatus {
            return values().find { it.name.lowercase() == value.lowercase() }
                ?: values().find { it.name == value.uppercase() }
                ?: AWAITING_STANDARD_REVIEW
        }
    }
}

/**
 * Fulfillment status enum matching the backend's fulfillment_status field.
 *
 * Backend values: unfulfilled, awaiting_confirmation, confirmed, processing, out_for_delivery, delivered
 */
enum class FulfillmentStatus {
    UNFULFILLED,
    AWAITING_CONFIRMATION,
    CONFIRMED,
    PROCESSING,
    OUT_FOR_DELIVERY,
    DELIVERED;

    companion object {
        /**
         * Safely parses a backend string value to [FulfillmentStatus].
         * Falls back to [UNFULFILLED] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): FulfillmentStatus {
            // Backend uses snake_case; convert to UPPER_SNAKE_CASE for matching
            val normalized = value.lowercase().replace("_", "_")
            return values().find { it.name.lowercase() == normalized }
                ?: UNFULFILLED
        }
    }
}
