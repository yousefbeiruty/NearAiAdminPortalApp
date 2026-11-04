package com.nearai.admin.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Immutable UI state for the Dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val revenueByCurrency: List<RevenueItem> = emptyList(),
    val recentOrders: List<RecentOrderItem> = emptyList(),
    val recentRegistrations: List<RecentRegistrationItem> = emptyList(),
    val lowStockAlerts: List<LowStockAlertItem> = emptyList(),
    val error: String? = null
)

/**
 * UI-specific revenue item with formatted display.
 */
data class RevenueItem(
    val currency: String,
    val amount: Double,
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
    val total: Double,
    val currency: String,
    val formattedTotal: String
)

/**
 * UI-specific recent registration item.
 */
data class RecentRegistrationItem(
    val id: String,
    val name: String,
    val email: String,
    val registrationDate: String
)

/**
 * UI-specific low stock alert item.
 */
data class LowStockAlertItem(
    val productName: String,
    val sku: String,
    val currentStock: Int,
    val threshold: Int
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
        loadDashboard()
    }

    /**
     * Loads dashboard data from the repository.
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
                            revenueByCurrency = data.revenueByCurrency.map { rev ->
                                RevenueItem(
                                    currency = rev.currency,
                                    amount = rev.amount,
                                    formattedAmount = formatCurrency(rev.amount, rev.currency)
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
                                    formattedTotal = formatCurrency(order.total, order.currency)
                                )
                            },
                            recentRegistrations = data.recentRegistrations.map { reg ->
                                RecentRegistrationItem(
                                    id = reg.id,
                                    name = reg.name,
                                    email = reg.email,
                                    registrationDate = reg.registrationDate
                                )
                            },
                            lowStockAlerts = data.lowStockAlerts.map { alert ->
                                LowStockAlertItem(
                                    productName = alert.productName,
                                    sku = alert.sku,
                                    currentStock = alert.currentStock,
                                    threshold = alert.threshold
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
     * Formats a currency amount using the locale-aware formatter.
     */
    private fun formatCurrency(amount: Double, currencyCode: String): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = java.util.Currency.getInstance(currencyCode.ifBlank { "USD" })
            }
            format.format(amount)
        } catch (_: Exception) {
            // Fallback if currency code is invalid
            String.format(Locale.getDefault(), "%.2f %s", amount, currencyCode)
        }
    }

    /**
     * Retries loading after an error.
     */
    fun onRetry() {
        loadDashboard()
    }

    /**
     * Pull-to-refresh action.
     */
    fun onRefresh() {
        loadDashboard()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
