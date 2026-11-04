package com.nearai.admin.feature.discovery.domain

import com.nearai.admin.core.common.Result

/**
 * Repository interface for business analytics data.
 * Belongs in the domain layer so the presentation layer depends on an abstraction.
 */
interface BusinessAnalyticsRepository {

    /**
     * Fetches the business analytics overview with optional period comparison.
     */
    suspend fun getBusinessOverview(
        days: Int = 7,
        startDate: String? = null,
        endDate: String? = null,
        compare: Boolean = true
    ): Result<BusinessAnalyticsData>
}

// =============================================================================
// Domain data classes
// =============================================================================

/**
 * Complete business analytics overview with optional period comparison.
 */
data class BusinessAnalyticsData(
    val startDate: String = "",
    val endDate: String = "",
    val days: Int = 0,

    // Order analytics
    val totalOrders: Int = 0,
    val ordersByStatus: List<OrderStatusBreakdown> = emptyList(),
    val dailyOrders: List<DailyOrderCount> = emptyList(),
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val pendingOrders: Int = 0,
    val rejectedOrders: Int = 0,

    // Revenue analytics
    val revenueByCurrency: List<RevenueByCurrency> = emptyList(),
    val dailyRevenue: List<DailyRevenue> = emptyList(),

    // Product analytics
    val bestSellingProducts: List<BestSellingProduct> = emptyList(),
    val categorySales: List<CategorySales> = emptyList(),

    // Customer analytics
    val newRegistrations: Int = 0,
    val dailyRegistrations: List<DailyUserRegistration> = emptyList(),
    val activeCustomers: Int = 0,
    val repeatCustomers: Int = 0,

    // Previous period comparison (optional)
    val compare: Boolean = false,
    val previousStartDate: String? = null,
    val previousEndDate: String? = null,
    val totalOrdersComparison: IntPeriodComparison? = null,
    val completedOrdersComparison: IntPeriodComparison? = null,
    val revenueComparison: List<RevenuePeriodComparison>? = null,
    val newRegistrationsComparison: IntPeriodComparison? = null,
    val activeCustomersComparison: IntPeriodComparison? = null
)

// ─── Order Analytics ────────────────────────────────────────────────────────

data class DailyOrderCount(
    val date: String = "",
    val count: Int = 0
)

data class OrderStatusBreakdown(
    val status: String = "",
    val count: Int = 0
)

// ─── Revenue Analytics ──────────────────────────────────────────────────────

data class DailyRevenue(
    val date: String = "",
    val amount: Double = 0.0,
    val currency: String = ""
)

data class RevenueByCurrency(
    val currency: String = "",
    val totalAmount: Double = 0.0,
    val orderCount: Int = 0,
    val avgOrderValue: Double = 0.0
)

// ─── Product Analytics ──────────────────────────────────────────────────────

data class BestSellingProduct(
    val productId: String = "",
    val productName: String = "",
    val sku: String = "",
    val unitsSold: Int = 0,
    val revenue: Double = 0.0,
    val currency: String = ""
)

data class CategorySales(
    val categoryId: String = "",
    val categoryName: String = "",
    val unitsSold: Int = 0,
    val revenue: Double = 0.0,
    val currency: String = ""
)

// ─── Customer Analytics ─────────────────────────────────────────────────────

data class DailyUserRegistration(
    val date: String = "",
    val count: Int = 0
)

// ─── Period Comparison ──────────────────────────────────────────────────────

data class RevenuePeriodComparison(
    val current: Double = 0.0,
    val previous: Double = 0.0,
    val percentageChange: Double? = null
)

data class IntPeriodComparison(
    val current: Int = 0,
    val previous: Int = 0,
    val percentageChange: Double? = null
)
