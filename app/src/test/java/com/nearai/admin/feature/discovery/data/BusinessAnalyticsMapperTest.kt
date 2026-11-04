package com.nearai.admin.feature.discovery.data

import com.nearai.admin.feature.discovery.domain.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests that [BusinessAnalyticsMapper] correctly maps all 12 DTO types to domain models.
 *
 * Covers:
 * - Correct mapping of all fields
 * - Revenue remains separated by currency
 * - Correct period comparison mapping
 * - Null and empty data handling
 */
class BusinessAnalyticsMapperTest {

    @Test
    fun `mapBusinessOverview maps all order analytics fields`() {
        val dto = BusinessAnalyticsResponseDto(
            startDate = "2024-01-01",
            endDate = "2024-01-07",
            days = 7,
            totalOrders = 150,
            completedOrders = 120,
            cancelledOrders = 10,
            pendingOrders = 15,
            rejectedOrders = 5,
            ordersByStatus = listOf(
                OrderStatusBreakdownDto("completed", 120),
                OrderStatusBreakdownDto("pending", 15)
            ),
            dailyOrders = listOf(
                DailyOrderCountDto("2024-01-01", 20),
                DailyOrderCountDto("2024-01-02", 25)
            )
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals("2024-01-01", data.startDate)
        assertEquals("2024-01-07", data.endDate)
        assertEquals(7, data.days)
        assertEquals(150, data.totalOrders)
        assertEquals(120, data.completedOrders)
        assertEquals(10, data.cancelledOrders)
        assertEquals(15, data.pendingOrders)
        assertEquals(5, data.rejectedOrders)
        assertEquals(2, data.ordersByStatus.size)
        assertEquals("completed", data.ordersByStatus[0].status)
        assertEquals(120, data.ordersByStatus[0].count)
        assertEquals(2, data.dailyOrders.size)
        assertEquals("2024-01-01", data.dailyOrders[0].date)
        assertEquals(20, data.dailyOrders[0].count)
    }

    @Test
    fun `mapBusinessOverview maps revenue by currency correctly`() {
        val dto = BusinessAnalyticsResponseDto(
            revenueByCurrency = listOf(
                RevenueByCurrencyDto("USD", 5000.50, 80, 62.51),
                RevenueByCurrencyDto("EUR", 3000.75, 40, 75.02)
            ),
            dailyRevenue = listOf(
                DailyRevenueDto("2024-01-01", 1000.0, "USD"),
                DailyRevenueDto("2024-01-01", 500.0, "EUR")
            )
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals(2, data.revenueByCurrency.size)
        assertEquals("USD", data.revenueByCurrency[0].currency)
        assertEquals(5000.50, data.revenueByCurrency[0].totalAmount, 0.01)
        assertEquals(80, data.revenueByCurrency[0].orderCount)
        assertEquals(62.51, data.revenueByCurrency[0].avgOrderValue, 0.01)
        assertEquals("EUR", data.revenueByCurrency[1].currency)
        assertEquals(3000.75, data.revenueByCurrency[1].totalAmount, 0.01)

        assertEquals(2, data.dailyRevenue.size)
        assertEquals("USD", data.dailyRevenue[0].currency)
        assertEquals(1000.0, data.dailyRevenue[0].amount, 0.01)
        assertEquals("EUR", data.dailyRevenue[1].currency)
        assertEquals(500.0, data.dailyRevenue[1].amount, 0.01)
    }

    @Test
    fun `mapBusinessOverview maps best selling products correctly`() {
        val dto = BusinessAnalyticsResponseDto(
            bestSellingProducts = listOf(
                BestSellingProductDto("prod-1", "Widget A", "SKU-001", 50, 2500.0, "USD"),
                BestSellingProductDto("prod-2", "Widget B", "SKU-002", 30, 1500.0, "EUR")
            )
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals(2, data.bestSellingProducts.size)
        assertEquals("prod-1", data.bestSellingProducts[0].productId)
        assertEquals("Widget A", data.bestSellingProducts[0].productName)
        assertEquals("SKU-001", data.bestSellingProducts[0].sku)
        assertEquals(50, data.bestSellingProducts[0].unitsSold)
        assertEquals(2500.0, data.bestSellingProducts[0].revenue, 0.01)
        assertEquals("USD", data.bestSellingProducts[0].currency)
        assertEquals("EUR", data.bestSellingProducts[1].currency)
    }

    @Test
    fun `mapBusinessOverview maps category sales correctly`() {
        val dto = BusinessAnalyticsResponseDto(
            categorySales = listOf(
                CategorySalesDto("cat-1", "Electronics", 100, 5000.0, "USD"),
                CategorySalesDto("cat-2", "Clothing", 50, 2000.0, "EUR")
            )
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals(2, data.categorySales.size)
        assertEquals("cat-1", data.categorySales[0].categoryId)
        assertEquals("Electronics", data.categorySales[0].categoryName)
        assertEquals(100, data.categorySales[0].unitsSold)
        assertEquals(5000.0, data.categorySales[0].revenue, 0.01)
        assertEquals("USD", data.categorySales[0].currency)
    }

    @Test
    fun `mapBusinessOverview maps customer analytics correctly`() {
        val dto = BusinessAnalyticsResponseDto(
            newRegistrations = 25,
            dailyRegistrations = listOf(
                DailyUserRegistrationDto("2024-01-01", 5),
                DailyUserRegistrationDto("2024-01-02", 8)
            ),
            activeCustomers = 80,
            repeatCustomers = 20
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals(25, data.newRegistrations)
        assertEquals(2, data.dailyRegistrations.size)
        assertEquals("2024-01-01", data.dailyRegistrations[0].date)
        assertEquals(5, data.dailyRegistrations[0].count)
        assertEquals(80, data.activeCustomers)
        assertEquals(20, data.repeatCustomers)
    }

    @Test
    fun `mapBusinessOverview maps period comparison correctly`() {
        val dto = BusinessAnalyticsResponseDto(
            compare = true,
            previousStartDate = "2023-12-25",
            previousEndDate = "2023-12-31",
            totalOrdersComparison = IntPeriodComparisonDto(150, 120, 25.0),
            completedOrdersComparison = IntPeriodComparisonDto(120, 100, 20.0),
            revenueComparison = listOf(
                PeriodComparisonDto(5000.50, 4000.00, 25.13)
            ),
            newRegistrationsComparison = IntPeriodComparisonDto(25, 20, 25.0),
            activeCustomersComparison = IntPeriodComparisonDto(80, 70, 14.29)
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertTrue(data.compare)
        assertEquals("2023-12-25", data.previousStartDate)
        assertEquals("2023-12-31", data.previousEndDate)

        assertNotNull(data.totalOrdersComparison)
        assertEquals(150, data.totalOrdersComparison!!.current)
        assertEquals(120, data.totalOrdersComparison!!.previous)
        assertEquals(25.0, data.totalOrdersComparison!!.percentageChange!!, 0.01)

        assertNotNull(data.completedOrdersComparison)
        assertEquals(120, data.completedOrdersComparison!!.current)

        assertNotNull(data.revenueComparison)
        assertEquals(1, data.revenueComparison!!.size)
        assertEquals(5000.50, data.revenueComparison!![0].current, 0.01)
        assertEquals(4000.00, data.revenueComparison!![0].previous, 0.01)

        assertNotNull(data.newRegistrationsComparison)
        assertNotNull(data.activeCustomersComparison)
    }

    @Test
    fun `mapBusinessOverview handles null period comparison fields`() {
        val dto = BusinessAnalyticsResponseDto(
            compare = false,
            previousStartDate = null,
            previousEndDate = null,
            totalOrdersComparison = null,
            completedOrdersComparison = null,
            revenueComparison = null,
            newRegistrationsComparison = null,
            activeCustomersComparison = null
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertFalse(data.compare)
        assertNull(data.previousStartDate)
        assertNull(data.previousEndDate)
        assertNull(data.totalOrdersComparison)
        assertNull(data.completedOrdersComparison)
        assertNull(data.revenueComparison)
        assertNull(data.newRegistrationsComparison)
        assertNull(data.activeCustomersComparison)
    }

    @Test
    fun `mapBusinessOverview handles empty data`() {
        val dto = BusinessAnalyticsResponseDto()

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals("", data.startDate)
        assertEquals(0, data.days)
        assertEquals(0, data.totalOrders)
        assertTrue(data.ordersByStatus.isEmpty())
        assertTrue(data.dailyOrders.isEmpty())
        assertTrue(data.revenueByCurrency.isEmpty())
        assertTrue(data.dailyRevenue.isEmpty())
        assertTrue(data.bestSellingProducts.isEmpty())
        assertTrue(data.categorySales.isEmpty())
        assertEquals(0, data.newRegistrations)
        assertTrue(data.dailyRegistrations.isEmpty())
        assertEquals(0, data.activeCustomers)
        assertEquals(0, data.repeatCustomers)
    }

    @Test
    fun `mapBusinessOverview handles period comparison with null percentage change`() {
        val dto = BusinessAnalyticsResponseDto(
            totalOrdersComparison = IntPeriodComparisonDto(10, 0, null)
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertNotNull(data.totalOrdersComparison)
        assertEquals(10, data.totalOrdersComparison!!.current)
        assertEquals(0, data.totalOrdersComparison!!.previous)
        assertNull(data.totalOrdersComparison!!.percentageChange)
    }

    @Test
    fun `revenue remains separated by currency after mapping`() {
        val dto = BusinessAnalyticsResponseDto(
            revenueByCurrency = listOf(
                RevenueByCurrencyDto("USD", 5000.50, 80, 62.51),
                RevenueByCurrencyDto("EUR", 3000.75, 40, 75.02),
                RevenueByCurrencyDto("GBP", 1500.00, 20, 75.00)
            )
        )

        val data = BusinessAnalyticsMapper.mapBusinessOverview(dto)

        assertEquals(3, data.revenueByCurrency.size)
        val currencies = data.revenueByCurrency.map { it.currency }
        assertTrue(currencies.contains("USD"))
        assertTrue(currencies.contains("EUR"))
        assertTrue(currencies.contains("GBP"))

        // Verify currencies are not combined
        val usdRevenue = data.revenueByCurrency.find { it.currency == "USD" }
        val eurRevenue = data.revenueByCurrency.find { it.currency == "EUR" }
        val gbpRevenue = data.revenueByCurrency.find { it.currency == "GBP" }

        assertNotNull(usdRevenue)
        assertNotNull(eurRevenue)
        assertNotNull(gbpRevenue)
        assertEquals(5000.50, usdRevenue!!.totalAmount, 0.01)
        assertEquals(3000.75, eurRevenue!!.totalAmount, 0.01)
        assertEquals(1500.00, gbpRevenue!!.totalAmount, 0.01)
    }
}
