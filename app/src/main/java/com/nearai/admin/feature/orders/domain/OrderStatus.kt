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
 * Backend values: awaiting_confirmation, confirmed, processing, out_for_delivery, delivered
 */
enum class FulfillmentStatus {
    AWAITING_CONFIRMATION,
    CONFIRMED,
    PROCESSING,
    OUT_FOR_DELIVERY,
    DELIVERED;

    companion object {
        /**
         * Safely parses a backend string value to [FulfillmentStatus].
         * Falls back to [AWAITING_CONFIRMATION] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): FulfillmentStatus {
            // Backend uses snake_case (e.g. "awaiting_confirmation"); enum names are UPPER_SNAKE_CASE.
            // Lowercasing both sides produces an exact match.
            return values().find { it.name.lowercase() == value.lowercase() }
                ?: AWAITING_CONFIRMATION
        }
    }
}

/**
 * Payment status enum matching the backend's PaymentStatus enum.
 *
 * Backend values: pending, completed, collected, failed, cancelled, refunded
 *
 * This is separate from [OrderStatus] and [FulfillmentStatus] to preserve
 * the independence of payment lifecycle from order and fulfillment lifecycles.
 */
enum class PaymentStatus {
    PENDING,
    COMPLETED,
    COLLECTED,
    FAILED,
    CANCELLED,
    REFUNDED;

    companion object {
        /**
         * Safely parses a backend string value to [PaymentStatus].
         * Falls back to [PENDING] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): PaymentStatus {
            // Backend uses lowercase (e.g. "pending", "collected"); enum names are UPPER_SNAKE_CASE.
            // Lowercasing both sides produces an exact match.
            return values().find { it.name.lowercase() == value.lowercase() }
                ?: PENDING
        }
    }
}
