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
    @SerialName("revenue_by_currency") val revenueByCurrency: List<RevenueDto> = emptyList(),
    @SerialName("recent_orders") val recentOrders: List<RecentOrderDto> = emptyList(),
    @SerialName("recent_registrations") val recentRegistrations: List<RecentRegistrationDto> = emptyList(),
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
    @SerialName("user_name") val userName: String = "",
    @SerialName("order_date") val orderDate: String = "",
    val status: String = "",
    val total: Double = 0.0,
    val currency: String = ""
)

@Serializable
data class RecentRegistrationDto(
    val id: String,
    val name: String = "",
    val email: String = "",
    @SerialName("registration_date") val registrationDate: String = ""
)

@Serializable
data class LowStockAlertDto(
    @SerialName("product_name") val productName: String = "",
    val sku: String = "",
    @SerialName("current_stock") val currentStock: Int = 0,
    @SerialName("threshold") val threshold: Int = 0
)
