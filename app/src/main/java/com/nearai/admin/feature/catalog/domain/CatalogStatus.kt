package com.nearai.admin.feature.catalog.domain

/**
 * Product status enum matching the backend's status field.
 *
 * Backend values: active, inactive, draft
 */
enum class ProductStatus {
    ACTIVE,
    INACTIVE,
    DRAFT;

    companion object {
        /**
         * Safely parses a backend string value to [ProductStatus].
         * Falls back to [ACTIVE] for unknown values so new backend statuses don't crash the app.
         */
        fun fromString(value: String): ProductStatus {
            return values().find { it.name.lowercase() == value.lowercase() }
                ?: ACTIVE
        }
    }
}

/**
 * Variant status enum matching the backend's status field.
 *
 * Backend values: active, inactive
 */
enum class VariantStatus {
    ACTIVE,
    INACTIVE;

    companion object {
        /**
         * Safely parses a backend string value to [VariantStatus].
         * Falls back to [ACTIVE] for unknown values.
         */
        fun fromString(value: String): VariantStatus {
            return values().find { it.name.lowercase() == value.lowercase() }
                ?: ACTIVE
        }
    }
}

/**
 * Stock movement type enum.
 *
 * Backend values: receipt, adjustment, sale, return, reservation, reservation_release
 */
enum class StockMovementType {
    RECEIPT,
    ADJUSTMENT,
    SALE,
    RETURN,
    RESERVATION,
    RESERVATION_RELEASE;

    companion object {
        fun fromString(value: String): StockMovementType {
            val normalized = value.lowercase().replace("_", "_")
            return values().find { it.name.lowercase() == normalized }
                ?: RECEIPT
        }
    }
}
