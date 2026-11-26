package com.nearai.admin.core.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Kotlinx Serialization serializer for [BigDecimal] that preserves exact decimal precision.
 *
 * - **Deserialization:** Accepts both JSON strings ("12.345") and JSON numbers (12.345).
 *   String values are parsed directly to BigDecimal without floating-point conversion.
 *   Numeric JSON values are parsed from their raw string representation to avoid Double imprecision.
 * - **Serialization:** Always emits a JSON string to preserve exact decimal representation.
 *   This ensures the backend receives exact decimal strings, not floating-point approximations.
 *
 * This serializer is mandatory for all monetary fields to comply with the exact monetary
 * precision contract. Never use Double or Float for monetary values.
 */
object BigDecimalJsonSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("BigDecimalJsonSerializer requires a JsonDecoder")

        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> {
                if (element.isString) {
                    // JSON string: parse directly
                    BigDecimal(element.content)
                } else {
                    // JSON number: parse from the raw string representation to avoid Double imprecision
                    BigDecimal(element.content)
                }
            }
            else -> throw IllegalStateException("Expected a JSON string or number for BigDecimal")
        }
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw IllegalStateException("BigDecimalJsonSerializer requires a JsonEncoder")
        // Always serialize as a JSON string to preserve exact precision
        jsonEncoder.encodeString(value.toString())
    }
}

/**
 * Parses a string into a [BigDecimal], returning null if the string is blank or invalid.
 */
fun String.parseBigDecimalOrNull(): BigDecimal? {
    if (this.isBlank()) return null
    return try {
        BigDecimal(this)
    } catch (e: NumberFormatException) {
        null
    }
}

/**
 * Validates that a string represents a valid monetary amount for the given currency.
 *
 * Returns null if valid, or an error message describing the validation failure.
 *
 * @param currency The ISO 4217 currency code (e.g., "JOD", "USD", "SAR").
 */
fun String.validateMonetaryAmountForCurrency(currency: String): String? {
    if (this.isBlank()) return "Amount is required"

    val amount = parseBigDecimalOrNull() ?: return "Invalid amount format"

    if (amount <= BigDecimal.ZERO) return "Amount must be greater than 0"

    // Validate decimal places based on currency
    val scale = amount.scale()
    val maxScale = getCurrencyMaxDecimalPlaces(currency)

    if (scale > maxScale) {
        return "Amount has too many decimal places for $currency (maximum $maxScale)"
    }

    return null
}

/**
 * Returns the maximum number of decimal places allowed for a given currency.
 *
 * Currency precision rules:
 * - JOD: 3 decimal places (Jordanian Dinar)
 * - BHD: 3 decimal places (Bahraini Dinar)
 * - KWD: 3 decimal places (Kuwaiti Dinar)
 * - OMR: 3 decimal places (Omani Rial)
 * - JPY, KRW: 0 decimal places (zero-decimal currencies)
 * - All others: 2 decimal places (standard)
 */
fun getCurrencyMaxDecimalPlaces(currency: String): Int {
    return when (currency.uppercase()) {
        "JOD", "BHD", "KWD", "OMR" -> 3
        "JPY", "KRW" -> 0
        else -> 2
    }
}

/**
 * Formats a [BigDecimal] monetary value for display with the correct currency precision.
 *
 * @param currency The ISO 4217 currency code.
 * @return The formatted string with the correct number of decimal places.
 */
fun BigDecimal.formatForCurrency(currency: String): String {
    val maxScale = getCurrencyMaxDecimalPlaces(currency)
    return this.setScale(maxScale, RoundingMode.HALF_UP).toString()
}

/**
 * Rounds a [BigDecimal] to the appropriate precision for the given currency.
 */
fun BigDecimal.roundForCurrency(currency: String): BigDecimal {
    val maxScale = getCurrencyMaxDecimalPlaces(currency)
    return this.setScale(maxScale, RoundingMode.HALF_UP)
}
