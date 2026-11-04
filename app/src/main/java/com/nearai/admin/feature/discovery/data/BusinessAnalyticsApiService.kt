package com.nearai.admin.feature.discovery.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the Business Analytics endpoint.
 *
 * Backend contract (Phase 0):
 * - GET /api/v1/admin/analytics/business/overview — Full business analytics overview
 */
interface BusinessAnalyticsApiService {

    @GET("api/v1/admin/analytics/business/overview")
    suspend fun getBusinessOverview(
        @Query("days") days: Int = 7,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("compare") compare: Boolean = true
    ): BusinessAnalyticsResponseDto
}

// =============================================================================
// Business Analytics Response DTOs — match backend Pydantic schemas exactly
// =============================================================================

/**
 * Complete business analytics overview response.
 * Matches `BusinessAnalyticsResponse` from the backend.
 */
@Serializable
data class BusinessAnalyticsResponseDto(
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("days") val days: Int = 0,

    // Order analytics
    @SerialName("total_orders") val totalOrders: Int = 0,
    @SerialName("orders_by_status") val ordersByStatus: List<OrderStatusBreakdownDto> = emptyList(),
    @SerialName("daily_orders") val dailyOrders: List<DailyOrderCountDto> = emptyList(),
    @SerialName("completed_orders") val completedOrders: Int = 0,
    @SerialName("cancelled_orders") val cancelledOrders: Int = 0,
    @SerialName("pending_orders") val pendingOrders: Int = 0,
    @SerialName("rejected_orders") val rejectedOrders: Int = 0,

    // Revenue analytics
    @SerialName("revenue_by_currency") val revenueByCurrency: List<RevenueByCurrencyDto> = emptyList(),
    @SerialName("daily_revenue") val dailyRevenue: List<DailyRevenueDto> = emptyList(),

    // Product analytics
    @SerialName("best_selling_products") val bestSellingProducts: List<BestSellingProductDto> = emptyList(),
    @SerialName("category_sales") val categorySales: List<CategorySalesDto> = emptyList(),

    // Customer analytics
    @SerialName("new_registrations") val newRegistrations: Int = 0,
    @SerialName("daily_registrations") val dailyRegistrations: List<DailyUserRegistrationDto> = emptyList(),
    @SerialName("active_customers") val activeCustomers: Int = 0,
    @SerialName("repeat_customers") val repeatCustomers: Int = 0,

    // Previous period comparison (optional)
    @SerialName("compare") val compare: Boolean = false,
    @SerialName("previous_start_date") val previousStartDate: String? = null,
    @SerialName("previous_end_date") val previousEndDate: String? = null,
    @SerialName("total_orders_comparison") val totalOrdersComparison: IntPeriodComparisonDto? = null,
    @SerialName("completed_orders_comparison") val completedOrdersComparison: IntPeriodComparisonDto? = null,
    @SerialName("revenue_comparison") val revenueComparison: List<PeriodComparisonDto>? = null,
    @SerialName("new_registrations_comparison") val newRegistrationsComparison: IntPeriodComparisonDto? = null,
    @SerialName("active_customers_comparison") val activeCustomersComparison: IntPeriodComparisonDto? = null
)

// ─── Order Analytics ────────────────────────────────────────────────────────

@Serializable
data class DailyOrderCountDto(
    @SerialName("date") val date: String = "",
    @SerialName("count") val count: Int = 0
)

@Serializable
data class OrderStatusBreakdownDto(
    @SerialName("status") val status: String = "",
    @SerialName("count") val count: Int = 0
)

// ─── Revenue Analytics ──────────────────────────────────────────────────────

@Serializable
data class DailyRevenueDto(
    @SerialName("date") val date: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("currency") val currency: String = ""
)

@Serializable
data class RevenueByCurrencyDto(
    @SerialName("currency") val currency: String = "",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("order_count") val orderCount: Int = 0,
    @SerialName("avg_order_value") val avgOrderValue: Double = 0.0
)

// ─── Product Analytics ──────────────────────────────────────────────────────

@Serializable
data class BestSellingProductDto(
    @SerialName("product_id") val productId: String = "",
    @SerialName("product_name") val productName: String = "",
    @SerialName("sku") val sku: String = "",
    @SerialName("units_sold") val unitsSold: Int = 0,
    @SerialName("revenue") val revenue: Double = 0.0,
    @SerialName("currency") val currency: String = ""
)

@Serializable
data class CategorySalesDto(
    @SerialName("category_id") val categoryId: String = "",
    @SerialName("category_name") val categoryName: String = "",
    @SerialName("units_sold") val unitsSold: Int = 0,
    @SerialName("revenue") val revenue: Double = 0.0,
    @SerialName("currency") val currency: String = ""
)

// ─── Customer Analytics ─────────────────────────────────────────────────────

@Serializable
data class DailyUserRegistrationDto(
    @SerialName("date") val date: String = "",
    @SerialName("count") val count: Int = 0
)

// ─── Period Comparison ──────────────────────────────────────────────────────

@Serializable
data class PeriodComparisonDto(
    @SerialName("current") val current: Double = 0.0,
    @SerialName("previous") val previous: Double = 0.0,
    @SerialName("percentage_change") val percentageChange: Double? = null
)

@Serializable
data class IntPeriodComparisonDto(
    @SerialName("current") val current: Int = 0,
    @SerialName("previous") val previous: Int = 0,
    @SerialName("percentage_change") val percentageChange: Double? = null
)
