package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.core.common.BigDecimalJsonSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import java.math.BigDecimal

/**
 * Retrofit service interface for the platform-wide dashboard endpoint.
 *
 * Backend contract:
 * - GET /api/v1/platform/dashboard — Platform-wide metrics across all tenants
 */
interface PlatformDashboardApiService {

    @GET("api/v1/platform/dashboard")
    suspend fun getPlatformDashboard(): PlatformDashboardResponseDto
}

// =============================================================================
// Response DTOs — match backend Pydantic schemas exactly
// =============================================================================

@Serializable
data class PlatformDashboardResponseDto(
    @SerialName("tenants") val tenants: TenantCountsDto = TenantCountsDto(),
    @SerialName("users") val users: UserCountsDto = UserCountsDto(),
    @SerialName("orders") val orders: OrderCountsDto = OrderCountsDto(),
    @SerialName("daily_orders") val dailyOrders: DailyOrderCountsDto = DailyOrderCountsDto(),
    @SerialName("financial") val financial: FinancialOverviewDto = FinancialOverviewDto(),
    @SerialName("operational") val operational: OperationalOverviewDto = OperationalOverviewDto()
)

@Serializable
data class TenantCountsDto(
    @SerialName("total") val total: Int = 0,
    @SerialName("active") val active: Int = 0,
    @SerialName("pending") val pending: Int = 0,
    @SerialName("suspended") val suspended: Int = 0,
    @SerialName("deleted") val deleted: Int = 0
)

@Serializable
data class UserCountsDto(
    @SerialName("total") val total: Int = 0,
    @SerialName("platform_admins") val platformAdmins: Int = 0,
    @SerialName("merchant_admins") val merchantAdmins: Int = 0,
    @SerialName("customers") val customers: Int = 0
)

@Serializable
data class OrderCountsDto(
    @SerialName("total") val total: Int = 0,
    @SerialName("pending") val pending: Int = 0,
    @SerialName("completed") val completed: Int = 0,
    @SerialName("cancelled") val cancelled: Int = 0,
    @SerialName("rejected") val rejected: Int = 0
)

@Serializable
data class DailyOrderCountsDto(
    @SerialName("today") val today: Int = 0,
    @SerialName("current_month") val currentMonth: Int = 0
)

@Serializable
data class FinancialOverviewDto(
    @SerialName("revenue_by_currency") val revenueByCurrency: List<CurrencyAmountDto> = emptyList(),
    @SerialName("payment_totals_by_currency") val paymentTotalsByCurrency: List<CurrencyAmountDto> = emptyList(),
    @SerialName("cod_totals_by_currency") val codTotalsByCurrency: List<CurrencyAmountDto> = emptyList()
)

@Serializable
data class CurrencyAmountDto(
    @SerialName("currency") val currency: String = "",
    @SerialName("amount") @Serializable(BigDecimalJsonSerializer::class) val amount: BigDecimal = BigDecimal.ZERO
)

@Serializable
data class OperationalOverviewDto(
    @SerialName("pending_approvals") val pendingApprovals: Int = 0,
    @SerialName("failed_jobs") val failedJobs: Int = 0,
    @SerialName("pending_jobs") val pendingJobs: Int = 0
)
