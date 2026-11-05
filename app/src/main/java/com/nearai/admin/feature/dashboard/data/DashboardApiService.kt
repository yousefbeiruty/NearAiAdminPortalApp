package com.nearai.admin.feature.dashboard.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

/**
 * Retrofit service interface for dashboard endpoints.
 *
 * Backend contract:
 * - GET /api/v1/admin/dashboard  — Dashboard metrics backed by real database queries
 */
interface DashboardApiService {

    @GET("api/v1/admin/dashboard")
    suspend fun getDashboard(): DashboardResponseDto
}

// =============================================================================
// Response DTOs
// =============================================================================

@Serializable
data class DashboardResponseDto(
    @SerialName("total_users") val totalUsers: Int = 0,
    @SerialName("total_products") val totalProducts: Int = 0,
    @SerialName("total_categories") val totalCategories: Int = 0,
    @SerialName("total_orders") val totalOrders: Int = 0,
    @SerialName("pending_orders") val pendingOrders: Int = 0,
    @SerialName("completed_orders") val completedOrders: Int = 0,
    @SerialName("cancelled_orders") val cancelledOrders: Int = 0,
    @SerialName("rejected_orders") val rejectedOrders: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("revenue_currency") val revenueCurrency: String = "",
    @SerialName("recent_orders") val recentOrders: List<RecentOrderDto> = emptyList(),
    @SerialName("recent_users") val recentRegistrations: List<RecentRegistrationDto> = emptyList(),
    @SerialName("low_stock_alerts") val lowStockAlerts: List<LowStockAlertDto> = emptyList()
)

@Serializable
data class RevenueDto(
    val currency: String,
    val amount: Double = 0.0
)

@Serializable
data class RecentOrderDto(
    val id: String,
    @SerialName("user_email") val userName: String = "",
    @SerialName("created_at") val orderDate: String = "",
    val status: String = "",
    @SerialName("total_amount") val total: Double = 0.0,
    val currency: String = ""
)

@Serializable
data class RecentRegistrationDto(
    val id: String,
    val email: String = "",
    val role: String = "",
    @SerialName("created_at") val registrationDate: String = ""
)

@Serializable
data class LowStockAlertDto(
    @SerialName("variant_id") val variantId: String = "",
    @SerialName("product_name") val productName: String = "",
    val sku: String = "",
    @SerialName("on_hand") val onHand: Int = 0,
    @SerialName("reserved") val reserved: Int = 0,
    @SerialName("available") val available: Int = 0
)
