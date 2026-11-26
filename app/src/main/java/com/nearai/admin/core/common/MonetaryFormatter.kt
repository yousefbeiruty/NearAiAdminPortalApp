package com.nearai.admin.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

/**
 * Centralized utility for formatting monetary values for display.
 *
 * This class handles:
 * - Currency-specific decimal precision (JOD: 3, SAR: 2, etc.)
 * - Locale-aware number formatting
 * - Currency symbol and code display
 * - RTL support for Arabic locales
 *
 * Never use this formatter for serialization — it produces display strings,
 * not API-compatible values. Use [BigDecimalJsonSerializer] for serialization.
 */
object MonetaryFormatter {

    /**
     * Formats a [BigDecimal] amount with the currency code and correct decimal precision.
     *
     * Example: "12.345 JOD", "1,234.56 USD"
     *
     * @param amount The monetary amount.
     * @param currency The ISO 4217 currency code.
     * @param locale The locale for number formatting (thousands separator, etc.).
     * @return Formatted display string.
     */
    fun formatWithCurrencyCode(
        amount: BigDecimal,
        currency: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val maxScale = getCurrencyMaxDecimalPlaces(currency)
        val rounded = amount.setScale(maxScale, RoundingMode.HALF_UP)
        val formattedNumber = formatNumber(rounded, maxScale, locale)
        return "$formattedNumber $currency"
    }

    /**
     * Formats a [BigDecimal] amount with the currency symbol and correct decimal precision.
     *
     * Example: "JOD 12.345", "$1,234.56"
     *
     * @param amount The monetary amount.
     * @param currency The ISO 4217 currency code.
     * @param locale The locale for number formatting.
     * @return Formatted display string.
     */
    fun formatWithCurrencySymbol(
        amount: BigDecimal,
        currency: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val maxScale = getCurrencyMaxDecimalPlaces(currency)
        val rounded = amount.setScale(maxScale, RoundingMode.HALF_UP)
        val formattedNumber = formatNumber(rounded, maxScale, locale)

        return try {
            val javaCurrency = java.util.Currency.getInstance(currency)
            val symbol = javaCurrency.symbol
            "$symbol$formattedNumber"
        } catch (e: Exception) {
            // Fallback to currency code if symbol is not available
            "$currency $formattedNumber"
        }
    }

    /**
     * Formats just the numeric part of a monetary amount with correct decimal precision.
     *
     * Example: "12.345", "1,234.56"
     *
     * @param amount The monetary amount.
     * @param currency The ISO 4217 currency code.
     * @param locale The locale for number formatting.
     * @return Formatted number string without currency.
     */
    fun formatAmountOnly(
        amount: BigDecimal,
        currency: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val maxScale = getCurrencyMaxDecimalPlaces(currency)
        val rounded = amount.setScale(maxScale, RoundingMode.HALF_UP)
        return formatNumber(rounded, maxScale, locale)
    }

    /**
     * Formats a nullable [BigDecimal] amount, returning a placeholder for null values.
     */
    fun formatNullable(
        amount: BigDecimal?,
        currency: String,
        locale: Locale = Locale.getDefault(),
        placeholder: String = "—"
    ): String {
        return amount?.let { formatWithCurrencyCode(it, currency, locale) } ?: placeholder
    }

    /**
     * Formats a percentage change value for display.
     *
     * @param value The percentage value (e.g., 12.5 for +12.5%).
     * @return Formatted string with sign and percentage symbol.
     */
    fun formatPercentageChange(value: BigDecimal?): String {
        if (value == null) return "—"
        val sign = if (value > BigDecimal.ZERO) "+" else ""
        val formatted = value.setScale(1, RoundingMode.HALF_UP).toString()
        return "$sign$formatted%"
    }

    /**
     * Internal helper to format a BigDecimal with the correct number of decimal places.
     */
    private fun formatNumber(
        amount: BigDecimal,
        scale: Int,
        locale: Locale
    ): String {
        val pattern = if (scale == 0) {
            "#,##0"
        } else {
            "#,##0." + "0".repeat(scale)
        }
        val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
        val formatter = java.text.DecimalFormat(pattern, symbols)
        formatter.roundingMode = RoundingMode.HALF_UP
        return formatter.format(amount)
    }
}
