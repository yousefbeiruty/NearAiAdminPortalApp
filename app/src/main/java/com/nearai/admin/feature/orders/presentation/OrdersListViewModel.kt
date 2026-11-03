package com.nearai.admin.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderRepository
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Orders List screen.
 */
data class OrdersListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val orders: List<OrderSummary> = emptyList(),
    val error: String? = null,
    val filterOrderStatus: OrderStatus? = null,
    val filterFulfillmentStatus: FulfillmentStatus? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface OrdersListEvent {
    data class NavigateToOrder(val orderId: String) : OrdersListEvent
}

/**
 * ViewModel for the Orders List screen.
 * Manages order listing, pagination, filtering, and pull-to-refresh.
 */
@HiltViewModel
class OrdersListViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersListUiState())
    val uiState: StateFlow<OrdersListUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<OrdersListEvent?>(null)
    val event: StateFlow<OrdersListEvent?> = _event.asStateFlow()

    private companion object {
        const val PAGE_SIZE = 20
    }

    init {
        loadOrders()
    }

    /**
     * Loads the first page of orders with current filters.
     */
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.orders.isEmpty(),
                    isRefreshing = _uiState.value.orders.isNotEmpty(),
                    error = null,
                    currentPage = 1,
                    hasMore = false,
                    isLoadingMore = false
                )
            }

            val state = _uiState.value
            when (val result = orderRepository.listOrders(
                page = 1,
                size = PAGE_SIZE,
                status = state.filterOrderStatus,
                fulfillmentStatus = state.filterFulfillmentStatus
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            orders = result.data.orders,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages
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
     * Loads the next page of orders (pagination).
     */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            when (val result = orderRepository.listOrders(
                page = nextPage,
                size = PAGE_SIZE,
                status = state.filterOrderStatus,
                fulfillmentStatus = state.filterFulfillmentStatus
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            orders = it.orders + result.data.orders,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    /**
     * Updates the order status filter and reloads.
     */
    fun setFilterOrderStatus(status: OrderStatus?) {
        _uiState.update { it.copy(filterOrderStatus = status) }
        loadOrders()
    }

    /**
     * Updates the fulfillment status filter and reloads.
     */
    fun setFilterFulfillmentStatus(status: FulfillmentStatus?) {
        _uiState.update { it.copy(filterFulfillmentStatus = status) }
        loadOrders()
    }

    /**
     * Clears all filters and reloads.
     */
    fun clearFilters() {
        _uiState.update {
            it.copy(filterOrderStatus = null, filterFulfillmentStatus = null)
        }
        loadOrders()
    }

    /**
     * Navigates to order details.
     */
    fun onOrderClicked(orderId: String) {
        _event.value = OrdersListEvent.NavigateToOrder(orderId)
    }

    /**
     * Retries loading after an error.
     */
    fun onRetry() {
        loadOrders()
    }

    /**
     * Pull-to-refresh action.
     */
    fun onRefresh() {
        loadOrders()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
