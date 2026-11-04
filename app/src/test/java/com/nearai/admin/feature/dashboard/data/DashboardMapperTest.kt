package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.LowStockAlert
import com.nearai.admin.feature.dashboard.domain.RecentOrder
import com.nearai.admin.feature.dashboard.domain.RecentRegistration
import com.nearai.admin.feature.dashboard.domain.RevenueByCurrency
import org.junit.Assert.*
import org.junit.Test

class DashboardMapperTest {

    @Test
    fun `mapDashboardData maps all fields correctly`() {
        val dto = DashboardResponseDto(
            totalUsers = 14820,
            totalProducts = 342,
            totalCategories = 12,
            totalOrders = 1250,
            pendingOrders = 45,
            completedOrders = 1180,
            revenueByCurrency = listOf(
                RevenueDto(currency = "USD", amount = 4820.50)
            ),
            recentOrders = listOf(
                RecentOrderDto("ord-1", "John Doe", "2024-01-15", "completed", 150.0, "USD")
            ),
            recentRegistrations = listOf(
                RecentRegistrationDto("usr-1", "Jane Smith", "jane@example.com", "2024-01-15")
            ),
            lowStockAlerts = listOf(
                LowStockAlertDto("Widget A", "SKU-001", 3, 10)
            )
        )

        val data = DashboardMapper.mapDashboardData(dto)

        assertEquals(14820, data.totalUsers)
        assertEquals(342, data.totalProducts)
        assertEquals(12, data.totalCategories)
        assertEquals(1250, data.totalOrders)
        assertEquals(45, data.pendingOrders)
        assertEquals(1180, data.completedOrders)
        assertEquals(1, data.revenueByCurrency.size)
        assertEquals(1, data.recentOrders.size)
        assertEquals(1, data.recentRegistrations.size)
        assertEquals(1, data.lowStockAlerts.size)
    }

    @Test
    fun `mapRevenue maps currency and amount`() {
        val dto = RevenueDto(currency = "EUR", amount = 1250.75)

        val domain = DashboardMapper.mapRevenue(dto)

        assertEquals("EUR", domain.currency)
        assertEquals(1250.75, domain.amount, 0.001)
    }

    @Test
    fun `mapRecentOrder maps all fields`() {
        val dto = RecentOrderDto("ord-1", "John Doe", "2024-01-15", "completed", 150.0, "USD")

        val domain = DashboardMapper.mapRecentOrder(dto)

        assertEquals("ord-1", domain.id)
        assertEquals("John Doe", domain.userName)
        assertEquals("2024-01-15", domain.orderDate)
        assertEquals("completed", domain.status)
        assertEquals(150.0, domain.total, 0.001)
        assertEquals("USD", domain.currency)
    }

    @Test
    fun `mapRecentRegistration maps all fields`() {
        val dto = RecentRegistrationDto("usr-1", "Jane Smith", "jane@example.com", "2024-01-15")

        val domain = DashboardMapper.mapRecentRegistration(dto)

        assertEquals("usr-1", domain.id)
        assertEquals("Jane Smith", domain.name)
        assertEquals("jane@example.com", domain.email)
        assertEquals("2024-01-15", domain.registrationDate)
    }

    @Test
    fun `mapLowStockAlert maps all fields`() {
        val dto = LowStockAlertDto("Widget A", "SKU-001", 3, 10)

        val domain = DashboardMapper.mapLowStockAlert(dto)

        assertEquals("Widget A", domain.productName)
        assertEquals("SKU-001", domain.sku)
        assertEquals(3, domain.currentStock)
        assertEquals(10, domain.threshold)
    }

    @Test
    fun `mapDashboardData handles empty response`() {
        val dto = DashboardResponseDto()

        val data = DashboardMapper.mapDashboardData(dto)

        assertEquals(0, data.totalUsers)
        assertEquals(0, data.totalProducts)
        assertTrue(data.revenueByCurrency.isEmpty())
        assertTrue(data.recentOrders.isEmpty())
        assertTrue(data.recentRegistrations.isEmpty())
        assertTrue(data.lowStockAlerts.isEmpty())
    }

    @Test
    fun `mapDashboardData handles multiple currencies`() {
        val dto = DashboardResponseDto(
            revenueByCurrency = listOf(
                RevenueDto(currency = "USD", amount = 100.0),
                RevenueDto(currency = "EUR", amount = 200.0),
                RevenueDto(currency = "GBP", amount = 300.0)
            )
        )

        val data = DashboardMapper.mapDashboardData(dto)

        assertEquals(3, data.revenueByCurrency.size)
        assertEquals("USD", data.revenueByCurrency[0].currency)
        assertEquals("EUR", data.revenueByCurrency[1].currency)
        assertEquals("GBP", data.revenueByCurrency[2].currency)
    }
}
