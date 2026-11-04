package com.nearai.admin.feature.discovery.data

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests that [BusinessAnalyticsResponseDto] and related DTOs correctly deserialize
 * the exact JSON structure returned by the FastAPI backend.
 *
 * Covers:
 * - Full backend JSON response deserialization
 * - Multiple currencies
 * - Null optional period comparison fields
 * - Empty arrays and missing optional values
 */
class BusinessAnalyticsDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `deserialize full backend JSON response with all fields`() {
        val jsonString = """
            {
                "start_date": "2024-01-01",
                "end_date": "2024-01-07",
                "days": 7,
                "total_orders": 150,
                "orders_by_status": [
                    {"status": "completed", "count": 120},
                    {"status": "pending", "count": 15},
                    {"status": "cancelled", "count": 10},
                    {"status": "rejected", "count": 5}
                ],
                "daily_orders": [
                    {"date": "2024-01-01", "count": 20},
                    {"date": "2024-01-02", "count": 25}
                ],
                "completed_orders": 120,
                "cancelled_orders": 10,
                "pending_orders": 15,
                "rejected_orders": 5,
                "revenue_by_currency": [
                    {"currency": "USD", "total_amount": 5000.50, "order_count": 80, "avg_order_value": 62.51},
                    {"currency": "EUR", "total_amount": 3000.75, "order_count": 40, "avg_order_value": 75.02}
                ],
                "daily_revenue": [
                    {"date": "2024-01-01", "amount": 1000.0, "currency": "USD"},
                    {"date": "2024-01-01", "amount": 500.0, "currency": "EUR"}
                ],
                "best_selling_products": [
                    {"product_id": "prod-1", "product_name": "Widget A", "sku": "SKU-001", "units_sold": 50, "revenue": 2500.0, "currency": "USD"}
                ],
                "category_sales": [
                    {"category_id": "cat-1", "category_name": "Electronics", "units_sold": 100, "revenue": 5000.0, "currency": "USD"}
                ],
                "new_registrations": 25,
                "daily_registrations": [
                    {"date": "2024-01-01", "count": 5}
                ],
                "active_customers": 80,
                "repeat_customers": 20,
                "compare": false
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertEquals("2024-01-01", dto.startDate)
        assertEquals("2024-01-07", dto.endDate)
        assertEquals(7, dto.days)
        assertEquals(150, dto.totalOrders)
        assertEquals(120, dto.completedOrders)
        assertEquals(10, dto.cancelledOrders)
        assertEquals(15, dto.pendingOrders)
        assertEquals(5, dto.rejectedOrders)
        assertEquals(4, dto.ordersByStatus.size)
        assertEquals(2, dto.dailyOrders.size)
        assertEquals(2, dto.revenueByCurrency.size)
        assertEquals(2, dto.dailyRevenue.size)
        assertEquals(1, dto.bestSellingProducts.size)
        assertEquals(1, dto.categorySales.size)
        assertEquals(25, dto.newRegistrations)
        assertEquals(1, dto.dailyRegistrations.size)
        assertEquals(80, dto.activeCustomers)
        assertEquals(20, dto.repeatCustomers)
        assertFalse(dto.compare)
        assertNull(dto.previousStartDate)
        assertNull(dto.previousEndDate)
        assertNull(dto.totalOrdersComparison)
        assertNull(dto.revenueComparison)
    }

    @Test
    fun `deserialize multiple currencies correctly`() {
        val jsonString = """
            {
                "revenue_by_currency": [
                    {"currency": "USD", "total_amount": 5000.50, "order_count": 80, "avg_order_value": 62.51},
                    {"currency": "EUR", "total_amount": 3000.75, "order_count": 40, "avg_order_value": 75.02},
                    {"currency": "GBP", "total_amount": 1500.00, "order_count": 20, "avg_order_value": 75.00}
                ],
                "daily_revenue": [
                    {"date": "2024-01-01", "amount": 1000.0, "currency": "USD"},
                    {"date": "2024-01-01", "amount": 500.0, "currency": "EUR"},
                    {"date": "2024-01-01", "amount": 250.0, "currency": "GBP"}
                ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertEquals(3, dto.revenueByCurrency.size)
        assertEquals("USD", dto.revenueByCurrency[0].currency)
        assertEquals(5000.50, dto.revenueByCurrency[0].totalAmount, 0.01)
        assertEquals("EUR", dto.revenueByCurrency[1].currency)
        assertEquals(3000.75, dto.revenueByCurrency[1].totalAmount, 0.01)
        assertEquals("GBP", dto.revenueByCurrency[2].currency)
        assertEquals(1500.00, dto.revenueByCurrency[2].totalAmount, 0.01)

        assertEquals(3, dto.dailyRevenue.size)
        assertEquals("USD", dto.dailyRevenue[0].currency)
        assertEquals("EUR", dto.dailyRevenue[1].currency)
        assertEquals("GBP", dto.dailyRevenue[2].currency)
    }

    @Test
    fun `deserialize null optional period comparison fields`() {
        val jsonString = """
            {
                "compare": false,
                "previous_start_date": null,
                "previous_end_date": null,
                "total_orders_comparison": null,
                "completed_orders_comparison": null,
                "revenue_comparison": null,
                "new_registrations_comparison": null,
                "active_customers_comparison": null
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertFalse(dto.compare)
        assertNull(dto.previousStartDate)
        assertNull(dto.previousEndDate)
        assertNull(dto.totalOrdersComparison)
        assertNull(dto.completedOrdersComparison)
        assertNull(dto.revenueComparison)
        assertNull(dto.newRegistrationsComparison)
        assertNull(dto.activeCustomersComparison)
    }

    @Test
    fun `deserialize period comparison with values`() {
        val jsonString = """
            {
                "compare": true,
                "previous_start_date": "2023-12-25",
                "previous_end_date": "2023-12-31",
                "total_orders_comparison": {"current": 150, "previous": 120, "percentage_change": 25.0},
                "completed_orders_comparison": {"current": 120, "previous": 100, "percentage_change": 20.0},
                "revenue_comparison": [
                    {"current": 5000.50, "previous": 4000.00, "percentage_change": 25.13}
                ],
                "new_registrations_comparison": {"current": 25, "previous": 20, "percentage_change": 25.0},
                "active_customers_comparison": {"current": 80, "previous": 70, "percentage_change": 14.29}
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertTrue(dto.compare)
        assertEquals("2023-12-25", dto.previousStartDate)
        assertEquals("2023-12-31", dto.previousEndDate)

        assertNotNull(dto.totalOrdersComparison)
        assertEquals(150, dto.totalOrdersComparison!!.current)
        assertEquals(120, dto.totalOrdersComparison!!.previous)
        assertEquals(25.0, dto.totalOrdersComparison!!.percentageChange!!, 0.01)

        assertNotNull(dto.completedOrdersComparison)
        assertEquals(120, dto.completedOrdersComparison!!.current)

        assertNotNull(dto.revenueComparison)
        assertEquals(1, dto.revenueComparison!!.size)
        assertEquals(5000.50, dto.revenueComparison!![0].current, 0.01)

        assertNotNull(dto.newRegistrationsComparison)
        assertNotNull(dto.activeCustomersComparison)
    }

    @Test
    fun `deserialize empty arrays and missing optional values`() {
        val jsonString = """
            {
                "total_orders": 0,
                "orders_by_status": [],
                "daily_orders": [],
                "revenue_by_currency": [],
                "daily_revenue": [],
                "best_selling_products": [],
                "category_sales": [],
                "new_registrations": 0,
                "daily_registrations": [],
                "active_customers": 0,
                "repeat_customers": 0
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertEquals(0, dto.totalOrders)
        assertTrue(dto.ordersByStatus.isEmpty())
        assertTrue(dto.dailyOrders.isEmpty())
        assertTrue(dto.revenueByCurrency.isEmpty())
        assertTrue(dto.dailyRevenue.isEmpty())
        assertTrue(dto.bestSellingProducts.isEmpty())
        assertTrue(dto.categorySales.isEmpty())
        assertEquals(0, dto.newRegistrations)
        assertTrue(dto.dailyRegistrations.isEmpty())
        assertEquals(0, dto.activeCustomers)
        assertEquals(0, dto.repeatCustomers)
    }

    @Test
    fun `deserialize best selling product with all fields`() {
        val jsonString = """
            {
                "best_selling_products": [
                    {"product_id": "prod-1", "product_name": "Widget A", "sku": "SKU-001", "units_sold": 50, "revenue": 2500.0, "currency": "USD"},
                    {"product_id": "prod-2", "product_name": "Widget B", "sku": "SKU-002", "units_sold": 30, "revenue": 1500.0, "currency": "EUR"}
                ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertEquals(2, dto.bestSellingProducts.size)
        assertEquals("prod-1", dto.bestSellingProducts[0].productId)
        assertEquals("Widget A", dto.bestSellingProducts[0].productName)
        assertEquals("SKU-001", dto.bestSellingProducts[0].sku)
        assertEquals(50, dto.bestSellingProducts[0].unitsSold)
        assertEquals(2500.0, dto.bestSellingProducts[0].revenue, 0.01)
        assertEquals("USD", dto.bestSellingProducts[0].currency)
        assertEquals("EUR", dto.bestSellingProducts[1].currency)
    }

    @Test
    fun `deserialize category sales with all fields`() {
        val jsonString = """
            {
                "category_sales": [
                    {"category_id": "cat-1", "category_name": "Electronics", "units_sold": 100, "revenue": 5000.0, "currency": "USD"},
                    {"category_id": "cat-2", "category_name": "Clothing", "units_sold": 50, "revenue": 2000.0, "currency": "EUR"}
                ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertEquals(2, dto.categorySales.size)
        assertEquals("cat-1", dto.categorySales[0].categoryId)
        assertEquals("Electronics", dto.categorySales[0].categoryName)
        assertEquals(100, dto.categorySales[0].unitsSold)
        assertEquals(5000.0, dto.categorySales[0].revenue, 0.01)
        assertEquals("USD", dto.categorySales[0].currency)
    }

    @Test
    fun `deserialize period comparison with null percentage change`() {
        val jsonString = """
            {
                "total_orders_comparison": {"current": 10, "previous": 0, "percentage_change": null}
            }
        """.trimIndent()

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertNotNull(dto.totalOrdersComparison)
        assertEquals(10, dto.totalOrdersComparison!!.current)
        assertEquals(0, dto.totalOrdersComparison!!.previous)
        assertNull(dto.totalOrdersComparison!!.percentageChange)
    }

    @Test
    fun `deserialize with default values when fields are missing`() {
        val jsonString = """{}"""

        val dto = json.decodeFromString<BusinessAnalyticsResponseDto>(jsonString)

        assertEquals("", dto.startDate)
        assertEquals(0, dto.days)
        assertEquals(0, dto.totalOrders)
        assertTrue(dto.ordersByStatus.isEmpty())
        assertEquals(0, dto.completedOrders)
        assertFalse(dto.compare)
    }
}
