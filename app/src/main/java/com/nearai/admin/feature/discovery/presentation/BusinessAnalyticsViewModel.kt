package com.nearai.admin.feature.discovery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.discovery.domain.BusinessAnalyticsData
import com.nearai.admin.feature.discovery.domain.BusinessAnalyticsRepository
import com.nearai.admin.feature.discovery.domain.RevenueByCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Immutable UI state for the Business Analytics screen.
 */
data class BusinessAnalyticsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // Date range
    val startDate: String = "",
    val endDate: String = "",
    val days: Int = 0,

    // Order analytics
    val totalOrders: Int = 0,
    val totalOrdersFormatted: String = "0",
    val completedOrders: Int = 0,
    val completedOrdersFormatted: String = "0",
    val pendingOrders: Int = 0,
    val pendingOrdersFormatted: String = "0",
    val cancelledOrders: Int = 0,
    val cancelledOrdersFormatted: String = "0",
    val rejectedOrders: Int = 0,
    val rejectedOrdersFormatted: String = "0",
    val ordersByStatus: List<OrderStatusUi> = emptyList(),
    val dailyOrders: List<DailyOrderUi> = emptyList(),

    // Revenue analytics
    val revenueByCurrency: List<RevenueByCurrencyUi> = emptyList(),
    val dailyRevenue: List<DailyRevenueUi> = emptyList(),

    // Product analytics
    val bestSellingProducts: List<ProductSalesUi> = emptyList(),
    val categorySales: List<CategorySalesUi> = emptyList(),

    // Customer analytics
    val newRegistrations: Int = 0,
    val newRegistrationsFormatted: String = "0",
    val dailyRegistrations: List<DailyRegistrationUi> = emptyList(),
    val activeCustomers: Int = 0,
    val activeCustomersFormatted: String = "0",
    val repeatCustomers: Int = 0,
    val repeatCustomersFormatted: String = "0",

    // Period comparison
    val compare: Boolean = false,
    val previousStartDate: String? = null,
    val previousEndDate: String? = null,
    val totalOrdersPercentageChange: Double? = null,
    val completedOrdersPercentageChange: Double? = null,
    val revenuePercentageChanges: List<RevenueComparisonUi>? = null,
    val newRegistrationsPercentageChange: Double? = null,
    val activeCustomersPercentageChange: Double? = null,

    // Selected time range
    val selectedDays: Int = 7
)

data class OrderStatusUi(
    val status: String = "",
    val count: Int = 0,
    val formattedCount: String = "0"
)

data class DailyOrderUi(
    val date: String = "",
    val count: Int = 0,
    val formattedDate: String = "",
    val formattedCount: String = "0"
)

data class RevenueByCurrencyUi(
    val currency: String = "",
    val totalAmount: Double = 0.0,
    val formattedTotal: String = "0.00",
    val orderCount: Int = 0,
    val formattedOrderCount: String = "0",
    val avgOrderValue: Double = 0.0,
    val formattedAvgOrderValue: String = "0.00"
)

data class DailyRevenueUi(
    val date: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val formattedDate: String = "",
    val formattedAmount: String = "0.00"
)

data class ProductSalesUi(
    val productId: String = "",
    val productName: String = "",
    val sku: String = "",
    val unitsSold: Int = 0,
    val formattedUnitsSold: String = "0",
    val revenue: Double = 0.0,
    val currency: String = "",
    val formattedRevenue: String = "0.00"
)

data class CategorySalesUi(
    val categoryId: String = "",
    val categoryName: String = "",
    val unitsSold: Int = 0,
    val formattedUnitsSold: String = "0",
    val revenue: Double = 0.0,
    val currency: String = "",
    val formattedRevenue: String = "0.00"
)

data class DailyRegistrationUi(
    val date: String = "",
    val count: Int = 0,
    val formattedDate: String = "",
    val formattedCount: String = "0"
)

data class RevenueComparisonUi(
    val current: Double = 0.0,
    val previous: Double = 0.0,
    val percentageChange: Double? = null,
    val formattedCurrent: String = "0.00",
    val formattedPrevious: String = "0.00"
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface BusinessAnalyticsEvent {
    object Retry : BusinessAnalyticsEvent
}

/**
 * ViewModel for the Business Analytics screen.
 * Manages loading analytics data, error handling, and retry logic.
 */
@HiltViewModel
class BusinessAnalyticsViewModel @Inject constructor(
    private val repository: BusinessAnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessAnalyticsUiState())
    val uiState: StateFlow<BusinessAnalyticsUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<BusinessAnalyticsEvent?>(null)
    val event: StateFlow<BusinessAnalyticsEvent?> = _event.asStateFlow()

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("MMM d", Locale.US)

    init {
        loadBusinessOverview()
    }

    /**
     * Loads the business analytics overview from the repository.
     */
    fun loadBusinessOverview() {
        viewModelScope.launch {
            val days = _uiState.value.selectedDays
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.totalOrders == 0,
                    isRefreshing = _uiState.value.totalOrders > 0
                )
            }

            val result = repository.getBusinessOverview(days, compare = true)

            when (result) {
                is Result.Success -> _uiState.update { it.copyData(it, result.data) }
                is Result.Error -> _uiState.update {
                    it.copy(
                        error = result.exception.message ?: "Failed to load analytics",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Changes the selected time range and reloads data.
     */
    fun setSelectedDays(days: Int) {
        _uiState.update { it.copy(selectedDays = days) }
        loadBusinessOverview()
    }

    /**
     * Retries loading analytics after an error.
     */
    fun retry() {
        _uiState.update { it.copy(error = null) }
        loadBusinessOverview()
    }

    private fun formatNumber(value: Int): String {
        return numberFormat.format(value)
    }

    private fun formatCurrency(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val fullFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = fullFormat.parse(dateStr)
            dateFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }
}

// =============================================================================
// Extension functions for cleaner state updates
// =============================================================================

private val numberFormat = NumberFormat.getNumberInstance(Locale.US)
private val dateFormat = SimpleDateFormat("MMM d", Locale.US)

private fun formatNumber(value: Int): String {
    return numberFormat.format(value)
}

private fun formatCurrency(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatDate(dateStr: String): String {
    return try {
        val fullFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = fullFormat.parse(dateStr)
        dateFormat.format(date)
    } catch (_: Exception) {
        dateStr
    }
}

private fun BusinessAnalyticsUiState.copyData(
    state: BusinessAnalyticsUiState,
    data: BusinessAnalyticsData
): BusinessAnalyticsUiState {
    return state.copy(
        startDate = data.startDate,
        endDate = data.endDate,
        days = data.days,

        // Order analytics
        totalOrders = data.totalOrders,
        totalOrdersFormatted = formatNumber(data.totalOrders),
        completedOrders = data.completedOrders,
        completedOrdersFormatted = formatNumber(data.completedOrders),
        pendingOrders = data.pendingOrders,
        pendingOrdersFormatted = formatNumber(data.pendingOrders),
        cancelledOrders = data.cancelledOrders,
        cancelledOrdersFormatted = formatNumber(data.cancelledOrders),
        rejectedOrders = data.rejectedOrders,
        rejectedOrdersFormatted = formatNumber(data.rejectedOrders),
        ordersByStatus = data.ordersByStatus.map {
            OrderStatusUi(
                status = it.status,
                count = it.count,
                formattedCount = formatNumber(it.count)
            )
        },
        dailyOrders = data.dailyOrders.map {
            DailyOrderUi(
                date = it.date,
                count = it.count,
                formattedDate = formatDate(it.date),
                formattedCount = formatNumber(it.count)
            )
        },

        // Revenue analytics
        revenueByCurrency = data.revenueByCurrency.map {
            RevenueByCurrencyUi(
                currency = it.currency,
                totalAmount = it.totalAmount,
                formattedTotal = formatCurrency(it.totalAmount),
                orderCount = it.orderCount,
                formattedOrderCount = formatNumber(it.orderCount),
                avgOrderValue = it.avgOrderValue,
                formattedAvgOrderValue = formatCurrency(it.avgOrderValue)
            )
        },
        dailyRevenue = data.dailyRevenue.map {
            DailyRevenueUi(
                date = it.date,
                amount = it.amount,
                currency = it.currency,
                formattedDate = formatDate(it.date),
                formattedAmount = formatCurrency(it.amount)
            )
        },

        // Product analytics
        bestSellingProducts = data.bestSellingProducts.map {
            ProductSalesUi(
                productId = it.productId,
                productName = it.productName,
                sku = it.sku,
                unitsSold = it.unitsSold,
                formattedUnitsSold = formatNumber(it.unitsSold),
                revenue = it.revenue,
                currency = it.currency,
                formattedRevenue = formatCurrency(it.revenue)
            )
        },
        categorySales = data.categorySales.map {
            CategorySalesUi(
                categoryId = it.categoryId,
                categoryName = it.categoryName,
                unitsSold = it.unitsSold,
                formattedUnitsSold = formatNumber(it.unitsSold),
                revenue = it.revenue,
                currency = it.currency,
                formattedRevenue = formatCurrency(it.revenue)
            )
        },

        // Customer analytics
        newRegistrations = data.newRegistrations,
        newRegistrationsFormatted = formatNumber(data.newRegistrations),
        dailyRegistrations = data.dailyRegistrations.map {
            DailyRegistrationUi(
                date = it.date,
                count = it.count,
                formattedDate = formatDate(it.date),
                formattedCount = formatNumber(it.count)
            )
        },
        activeCustomers = data.activeCustomers,
        activeCustomersFormatted = formatNumber(data.activeCustomers),
        repeatCustomers = data.repeatCustomers,
        repeatCustomersFormatted = formatNumber(data.repeatCustomers),

        // Period comparison
        compare = data.compare,
        previousStartDate = data.previousStartDate,
        previousEndDate = data.previousEndDate,
        totalOrdersPercentageChange = data.totalOrdersComparison?.percentageChange,
        completedOrdersPercentageChange = data.completedOrdersComparison?.percentageChange,
        revenuePercentageChanges = data.revenueComparison?.map {
            RevenueComparisonUi(
                current = it.current,
                previous = it.previous,
                percentageChange = it.percentageChange,
                formattedCurrent = formatCurrency(it.current),
                formattedPrevious = formatCurrency(it.previous)
            )
        },
        newRegistrationsPercentageChange = data.newRegistrationsComparison?.percentageChange,
        activeCustomersPercentageChange = data.activeCustomersComparison?.percentageChange,

        isLoading = false,
        isRefreshing = false
    )
}
