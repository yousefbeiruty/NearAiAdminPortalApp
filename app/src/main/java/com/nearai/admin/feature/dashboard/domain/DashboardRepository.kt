package com.nearai.admin.feature.dashboard.domain

import com.nearai.admin.core.common.Result

/**
 * Revenue amount for a single currency.
 */
data class RevenueByCurrency(
    val currency: String,
    val amount: Double
)

/**
 * A recent order summary for the dashboard.
 */
data class RecentOrder(
    val id: String,
    val userName: String,
    val orderDate: String,
    val status: String,
    val total: Double,
    val currency: String
)

/**
 * A recent user registration for the dashboard.
 */
data class RecentRegistration(
    val id: String,
    val email: String,
    val role: String,
    val registrationDate: String
)

/**
 * A low stock alert for a product variant.
 */
data class LowStockAlert(
    val variantId: String,
    val productName: String,
    val sku: String,
    val onHand: Int,
    val reserved: Int,
    val available: Int
)

/**
 * Aggregated dashboard data returned by the repository.
 */
data class DashboardData(
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val rejectedOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val revenueCurrency: String = "",
    val revenueByCurrency: List<RevenueByCurrency> = emptyList(),
    val recentOrders: List<RecentOrder> = emptyList(),
    val recentRegistrations: List<RecentRegistration> = emptyList(),
    val lowStockAlerts: List<LowStockAlert> = emptyList()
)

/**
 * Repository interface for dashboard data.
 * Belongs in the domain layer so the presentation layer depends on an abstraction.
 */
interface DashboardRepository {

    /**
     * Fetches the current dashboard data from the backend.
     */
    suspend fun getDashboardData(): Result<DashboardData>
}
