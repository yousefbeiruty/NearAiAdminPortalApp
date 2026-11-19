package com.nearai.admin.feature.dashboard.domain

import java.math.BigDecimal

/**
 * Tenant lifecycle counts for the platform dashboard.
 */
data class TenantCounts(
    val total: Int = 0,
    val active: Int = 0,
    val pending: Int = 0,
    val suspended: Int = 0,
    val deleted: Int = 0
)

/**
 * User counts grouped by role for the platform dashboard.
 */
data class UserCounts(
    val total: Int = 0,
    val platformAdmins: Int = 0,
    val merchantAdmins: Int = 0,
    val customers: Int = 0
)

/**
 * Order counts grouped by status for the platform dashboard.
 */
data class OrderCounts(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0,
    val rejected: Int = 0
)

/**
 * Daily order counts for the platform dashboard.
 */
data class DailyOrderCounts(
    val today: Int = 0,
    val currentMonth: Int = 0
)

/**
 * Monetary amount in a specific currency.
 */
data class CurrencyAmount(
    val currency: String,
    val amount: BigDecimal
)

/**
 * Financial overview grouped by currency for the platform dashboard.
 */
data class FinancialOverview(
    val revenueByCurrency: List<CurrencyAmount> = emptyList(),
    val paymentTotalsByCurrency: List<CurrencyAmount> = emptyList(),
    val codTotalsByCurrency: List<CurrencyAmount> = emptyList()
)

/**
 * Operational metrics for the platform dashboard.
 */
data class OperationalOverview(
    val pendingApprovals: Int = 0,
    val failedJobs: Int = 0,
    val pendingJobs: Int = 0
)

/**
 * Aggregated platform-wide dashboard data returned by the repository.
 */
data class PlatformDashboardData(
    val tenants: TenantCounts = TenantCounts(),
    val users: UserCounts = UserCounts(),
    val orders: OrderCounts = OrderCounts(),
    val dailyOrders: DailyOrderCounts = DailyOrderCounts(),
    val financial: FinancialOverview = FinancialOverview(),
    val operational: OperationalOverview = OperationalOverview()
)
