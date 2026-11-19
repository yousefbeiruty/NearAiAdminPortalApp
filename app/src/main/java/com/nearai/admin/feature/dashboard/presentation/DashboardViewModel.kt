package com.nearai.admin.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.MonetaryFormatter
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.CurrencyAmount
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import com.nearai.admin.feature.dashboard.domain.FinancialOverview
import com.nearai.admin.feature.dashboard.domain.OrderCounts
import com.nearai.admin.feature.dashboard.domain.OperationalOverview
import com.nearai.admin.feature.dashboard.domain.PlatformDashboardData
import com.nearai.admin.feature.dashboard.domain.TenantCounts
import com.nearai.admin.feature.dashboard.domain.UserCounts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Immutable UI state for the Dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    // Merchant dashboard fields
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val rejectedOrders: Int = 0,
    val totalRevenue: BigDecimal = BigDecimal.ZERO,
    val revenueCurrency: String = "",
    val revenueByCurrency: List<RevenueItem> = emptyList(),
    val recentOrders: List<RecentOrderItem> = emptyList(),
    val recentRegistrations: List<RecentRegistrationItem> = emptyList(),
    val lowStockAlerts: List<LowStockAlertItem> = emptyList(),
    // Platform dashboard fields
    val platformTenants: TenantCounts? = null,
    val platformUsers: UserCounts? = null,
    val platformOrders: OrderCounts? = null,
    val platformFinancialRevenue: List<RevenueItem> = emptyList(),
    val platformFinancialPayments: List<RevenueItem> = emptyList(),
    val platformFinancialCod: List<RevenueItem> = emptyList(),
    val platformPendingApprovals: Int = 0,
    val platformFailedJobs: Int = 0,
    val platformPendingJobs: Int = 0,
    val platformOrdersToday: Int = 0,
    val platformOrdersThisMonth: Int = 0,
    val error: String? = null
)

/**
 * UI-specific revenue item with formatted display.
 */
data class RevenueItem(
    val currency: String,
    val amount: BigDecimal,
    val formattedAmount: String
)

/**
 * UI-specific recent order item.
 */
data class RecentOrderItem(
    val id: String,
    val userName: String,
    val orderDate: String,
    val status: String,
    val total: BigDecimal,
    val currency: String,
    val formattedTotal: String
)

/**
 * UI-specific recent registration item.
 */
data class RecentRegistrationItem(
    val id: String,
    val email: String,
    val role: String,
    val registrationDate: String
)

/**
 * UI-specific low stock alert item.
 */
data class LowStockAlertItem(
    val variantId: String,
    val productName: String,
    val sku: String,
    val onHand: Int,
    val reserved: Int,
    val available: Int
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface DashboardEvent {
    object Retry : DashboardEvent
}

/**
 * ViewModel for the Overview Dashboard screen.
 * Manages dashboard data loading, error handling, and retry logic.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<DashboardEvent?>(null)
    val event: StateFlow<DashboardEvent?> = _event.asStateFlow()

    init {
        loadPlatformDashboard()
    }

    /**
     * Loads platform-wide dashboard data from the repository.
     */
    fun loadPlatformDashboard() {
        viewModelScope.launch {
            val hasData = _uiState.value.platformTenants != null
            _uiState.update {
                it.copy(
                    isLoading = !hasData,
                    isRefreshing = hasData,
                    error = null
                )
            }

            when (val result = dashboardRepository.getPlatformDashboardData()) {
                is Result.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            platformTenants = data.tenants,
                            platformUsers = data.users,
                            platformOrders = data.orders,
                            platformFinancialRevenue = data.financial.revenueByCurrency.map { rev ->
                                RevenueItem(
                                    currency = rev.currency,
                                    amount = rev.amount,
                                    formattedAmount = MonetaryFormatter.formatWithCurrencyCode(rev.amount, rev.currency)
                                )
                            },
                            platformFinancialPayments = data.financial.paymentTotalsByCurrency.map { pay ->
                                RevenueItem(
                                    currency = pay.currency,
                                    amount = pay.amount,
                                    formattedAmount = MonetaryFormatter.formatWithCurrencyCode(pay.amount, pay.currency)
                                )
                            },
                            platformFinancialCod = data.financial.codTotalsByCurrency.map { cod ->
                                RevenueItem(
                                    currency = cod.currency,
                                    amount = cod.amount,
                                    formattedAmount = MonetaryFormatter.formatWithCurrencyCode(cod.amount, cod.currency)
                                )
                            },
                            platformPendingApprovals = data.operational.pendingApprovals,
                            platformFailedJobs = data.operational.failedJobs,
                            platformPendingJobs = data.operational.pendingJobs,
                            platformOrdersToday = data.dailyOrders.today,
                            platformOrdersThisMonth = data.dailyOrders.currentMonth
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Loads merchant-scoped dashboard data from the repository.
     * Kept for backward compatibility.
     */
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.recentOrders.isEmpty(),
                    isRefreshing = _uiState.value.recentOrders.isNotEmpty(),
                    error = null
                )
            }

            when (val result = dashboardRepository.getDashboardData()) {
                is Result.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            totalUsers = data.totalUsers,
                            totalProducts = data.totalProducts,
                            totalCategories = data.totalCategories,
                            totalOrders = data.totalOrders,
                            pendingOrders = data.pendingOrders,
                            completedOrders = data.completedOrders,
                            cancelledOrders = data.cancelledOrders,
                            rejectedOrders = data.rejectedOrders,
                            totalRevenue = data.totalRevenue,
                            revenueCurrency = data.revenueCurrency,
                            revenueByCurrency = data.revenueByCurrency.map { rev ->
                                RevenueItem(
                                    currency = rev.currency,
                                    amount = rev.amount,
                                    formattedAmount = MonetaryFormatter.formatWithCurrencyCode(rev.amount, rev.currency)
                                )
                            },
                            recentOrders = data.recentOrders.map { order ->
                                RecentOrderItem(
                                    id = order.id,
                                    userName = order.userName,
                                    orderDate = order.orderDate,
                                    status = order.status,
                                    total = order.total,
                                    currency = order.currency,
                                    formattedTotal = MonetaryFormatter.formatWithCurrencyCode(order.total, order.currency)
                                )
                            },
                            recentRegistrations = data.recentRegistrations.map { reg ->
                                RecentRegistrationItem(
                                    id = reg.id,
                                    email = reg.email,
                                    role = reg.role,
                                    registrationDate = reg.registrationDate
                                )
                            },
                            lowStockAlerts = data.lowStockAlerts.map { alert ->
                                LowStockAlertItem(
                                    variantId = alert.variantId,
                                    productName = alert.productName,
                                    sku = alert.sku,
                                    onHand = alert.onHand,
                                    reserved = alert.reserved,
                                    available = alert.available
                                )
                            }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }


    /**
     * Retries loading after an error.
     */
    fun onRetry() {
        loadPlatformDashboard()
    }

    /**
     * Pull-to-refresh action.
     */
    fun onRefresh() {
        loadPlatformDashboard()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
