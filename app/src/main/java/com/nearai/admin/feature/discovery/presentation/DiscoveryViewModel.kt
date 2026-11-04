package com.nearai.admin.feature.discovery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.discovery.domain.ConversionFunnelData
import com.nearai.admin.feature.discovery.domain.DiscoveryOverviewData
import com.nearai.admin.feature.discovery.domain.DiscoveryRepository
import com.nearai.admin.feature.discovery.domain.MetricComparison
import com.nearai.admin.feature.discovery.domain.PopularCategoriesData
import com.nearai.admin.feature.discovery.domain.PopularProductsData
import com.nearai.admin.feature.discovery.domain.SearchAnalyticsData
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
 * Immutable UI state for the Discovery Analytics screen.
 */
data class DiscoveryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // Overview section
    val periodStart: String = "",
    val periodEnd: String = "",
    val dailyActiveUsers: MetricUi = MetricUi(),
    val weeklyActiveUsers: MetricUi = MetricUi(),
    val totalSearches: MetricUi = MetricUi(),
    val totalProductViews: MetricUi = MetricUi(),
    val totalCartAdds: MetricUi = MetricUi(),
    val totalPurchases: MetricUi = MetricUi(),
    val conversionRate: MetricUi = MetricUi(),

    // Search analytics section
    val searchTotalSearches: Int = 0,
    val searchZeroResultSearches: Int = 0,
    val searchZeroResultRatePct: Double = 0.0,
    val searchAvgResultsPerSearch: Double = 0.0,
    val searchAvgLatencyMs: Double = 0.0,
    val searchPopularTerms: List<SearchTermUi> = emptyList(),
    val searchZeroResultTerms: List<SearchTermUi> = emptyList(),

    // Popular products section
    val popularProductsMetric: String = "views",
    val popularProducts: List<ProductUi> = emptyList(),

    // Popular categories section
    val popularCategories: List<CategoryUi> = emptyList(),

    // Conversion funnel section
    val funnelSteps: List<FunnelStepUi> = emptyList(),
    val funnelOverallConversionPct: Double = 0.0,

    // Selected time range
    val selectedDays: Int = 7
)

/**
 * UI-specific metric with formatted display and change indicator.
 */
data class MetricUi(
    val current: Double = 0.0,
    val previous: Double = 0.0,
    val changePct: Double = 0.0,
    val formattedCurrent: String = "0",
    val formattedPrevious: String = "0"
)

/**
 * UI-specific search term.
 */
data class SearchTermUi(
    val query: String = "",
    val count: Int = 0,
    val formattedCount: String = "0",
    val avgResults: Double? = null
)

/**
 * UI-specific product analytics item.
 */
data class ProductUi(
    val productId: String = "",
    val name: String = "",
    val categoryName: String = "",
    val count: Int = 0,
    val uniqueViewers: Int = 0,
    val cartAddRatePct: Double = 0.0,
    val formattedCount: String = "0"
)

/**
 * UI-specific category analytics item.
 */
data class CategoryUi(
    val categoryId: String = "",
    val name: String = "",
    val views: Int = 0,
    val productViews: Int = 0,
    val cartAdds: Int = 0,
    val purchases: Int = 0,
    val formattedViews: String = "0"
)

/**
 * UI-specific funnel step.
 */
data class FunnelStepUi(
    val step: String = "",
    val count: Int = 0,
    val rate: Double = 0.0,
    val formattedCount: String = "0"
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface DiscoveryEvent {
    object Retry : DiscoveryEvent
}

/**
 * ViewModel for the Discovery Analytics screen.
 * Manages loading all analytics data, error handling, and retry logic.
 */
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<DiscoveryEvent?>(null)
    val event: StateFlow<DiscoveryEvent?> = _event.asStateFlow()

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US)

    init {
        loadAllAnalytics()
    }

    /**
     * Loads all analytics data from the repository.
     */
    fun loadAllAnalytics() {
        viewModelScope.launch {
            val days = _uiState.value.selectedDays
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.popularProducts.isEmpty(),
                    isRefreshing = !_uiState.value.popularProducts.isEmpty()
                )
            }

            // Load all analytics in parallel
            val overview = discoveryRepository.getDiscoveryOverview(days, compare = true)
            val search = discoveryRepository.getSearchAnalytics(days)
            val products = discoveryRepository.getPopularProducts(days)
            val categories = discoveryRepository.getPopularCategories(days)
            val funnel = discoveryRepository.getConversionFunnel(days)

            // Process results
            when (overview) {
                is Result.Success -> _uiState.update { it.copyOverview(it, overview.data) }
                is Result.Error -> _uiState.update { it.copy(error = overview.exception.message) }
                is Result.Loading -> {}
            }

            when (search) {
                is Result.Success -> _uiState.update { it.copySearch(it, search.data) }
                is Result.Error -> _uiState.update { it.copy(error = it.error ?: search.exception.message) }
                is Result.Loading -> {}
            }

            when (products) {
                is Result.Success -> _uiState.update { it.copyProducts(it, products.data) }
                is Result.Error -> _uiState.update { it.copy(error = it.error ?: products.exception.message) }
                is Result.Loading -> {}
            }

            when (categories) {
                is Result.Success -> _uiState.update { it.copyCategories(it, categories.data) }
                is Result.Error -> _uiState.update { it.copy(error = it.error ?: categories.exception.message) }
                is Result.Loading -> {}
            }

            when (funnel) {
                is Result.Success -> _uiState.update { it.copyFunnel(it, funnel.data) }
                is Result.Error -> _uiState.update { it.copy(error = it.error ?: funnel.exception.message) }
                is Result.Loading -> {}
            }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    /**
     * Changes the selected time range and reloads data.
     */
    fun setSelectedDays(days: Int) {
        _uiState.update { it.copy(selectedDays = days) }
        loadAllAnalytics()
    }

    /**
     * Retries loading all analytics after an error.
     */
    fun retry() {
        _uiState.update { it.copy(error = null) }
        loadAllAnalytics()
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            numberFormat.format(value.toLong())
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    private fun formatNumber(value: Int): String {
        return numberFormat.format(value)
    }
}

// =============================================================================
// Extension functions for cleaner state updates
// =============================================================================

private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

private fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        numberFormat.format(value.toLong())
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun formatNumber(value: Int): String {
    return numberFormat.format(value)
}

private fun DiscoveryUiState.copyOverview(
    state: DiscoveryUiState,
    data: DiscoveryOverviewData
): DiscoveryUiState {
    return state.copy(
        periodStart = data.periodStart,
        periodEnd = data.periodEnd,
        dailyActiveUsers = mapMetric(data.dailyActiveUsers),
        weeklyActiveUsers = mapMetric(data.weeklyActiveUsers),
        totalSearches = mapMetric(data.totalSearches),
        totalProductViews = mapMetric(data.totalProductViews),
        totalCartAdds = mapMetric(data.totalCartAdds),
        totalPurchases = mapMetric(data.totalPurchases),
        conversionRate = mapMetric(data.conversionRate)
    )
}

private fun mapMetric(mc: MetricComparison): MetricUi {
    return MetricUi(
        current = mc.current,
        previous = mc.previous,
        changePct = mc.changePct,
        formattedCurrent = formatNumber(mc.current),
        formattedPrevious = formatNumber(mc.previous)
    )
}

private fun DiscoveryUiState.copySearch(
    state: DiscoveryUiState,
    data: SearchAnalyticsData
): DiscoveryUiState {
    return state.copy(
        searchTotalSearches = data.totalSearches,
        searchZeroResultSearches = data.zeroResultSearches,
        searchZeroResultRatePct = data.zeroResultRatePct,
        searchAvgResultsPerSearch = data.avgResultsPerSearch,
        searchAvgLatencyMs = data.avgLatencyMs,
        searchPopularTerms = data.popularTerms.map { term ->
            SearchTermUi(
                query = term.query,
                count = term.count,
                formattedCount = formatNumber(term.count),
                avgResults = term.avgResults
            )
        },
        searchZeroResultTerms = data.zeroResultTerms.map { term ->
            SearchTermUi(
                query = term.query,
                count = term.count,
                formattedCount = formatNumber(term.count),
                avgResults = term.avgResults
            )
        }
    )
}

private fun DiscoveryUiState.copyProducts(
    state: DiscoveryUiState,
    data: PopularProductsData
): DiscoveryUiState {
    return state.copy(
        popularProductsMetric = data.metric,
        popularProducts = data.products.map { p ->
            ProductUi(
                productId = p.productId,
                name = p.name,
                categoryName = p.categoryName,
                count = p.count,
                uniqueViewers = p.uniqueViewers,
                cartAddRatePct = p.cartAddRatePct,
                formattedCount = formatNumber(p.count)
            )
        }
    )
}

private fun DiscoveryUiState.copyCategories(
    state: DiscoveryUiState,
    data: PopularCategoriesData
): DiscoveryUiState {
    return state.copy(
        popularCategories = data.categories.map { c ->
            CategoryUi(
                categoryId = c.categoryId,
                name = c.name,
                views = c.views,
                productViews = c.productViews,
                cartAdds = c.cartAdds,
                purchases = c.purchases,
                formattedViews = formatNumber(c.views)
            )
        }
    )
}

private fun DiscoveryUiState.copyFunnel(
    state: DiscoveryUiState,
    data: ConversionFunnelData
): DiscoveryUiState {
    return state.copy(
        funnelSteps = data.steps.map { s ->
            FunnelStepUi(
                step = s.step,
                count = s.count,
                rate = s.rate,
                formattedCount = formatNumber(s.count)
            )
        },
        funnelOverallConversionPct = data.overallConversionPct
    )
}
