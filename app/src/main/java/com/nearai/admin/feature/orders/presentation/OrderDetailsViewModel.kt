package com.nearai.admin.feature.orders.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.orders.domain.OrderDetails
import com.nearai.admin.feature.orders.domain.OrderRepository
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.StatusHistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Order Details screen.
 */
data class OrderDetailsUiState(
    val isLoading: Boolean = false,
    val order: OrderDetails? = null,
    val statusHistory: List<StatusHistoryEntry> = emptyList(),
    val error: String? = null,
    val isActionLoading: Boolean = false,
    val actionError: String? = null,
    val showRejectDialog: Boolean = false,
    val rejectReason: String = "",
    val showConfirmDialog: String? = null // action label to confirm
)

/**
 * ViewModel for the Order Details screen.
 * Manages order details loading, status history, and lifecycle actions.
 */
@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    /**
     * Loads order details and status history.
     */
    fun loadOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, actionError = null) }

            // Load order details
            when (val result = orderRepository.getOrderDetails(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(order = result.data, isLoading = false) }
                    // Load status history
                    when (val historyResult = orderRepository.getStatusHistory(orderId)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(statusHistory = historyResult.data) }
                        }
                        is Result.Error -> {
                            // Don't block the main content for history errors
                        }
                        is Result.Loading -> {
                            // No-op
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Confirms a pending order.
     */
    fun confirmOrder() {
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val result = orderRepository.confirmOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadOrder()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Rejects an order with the provided reason.
     */
    fun rejectOrder() {
        val reason = _uiState.value.rejectReason.trim()
        if (reason.isEmpty()) {
            _uiState.update { it.copy(actionError = "Rejection reason is required") }
            return
        }
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null, showRejectDialog = false) }
            when (val result = orderRepository.rejectOrder(orderId, reason)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadOrder()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Processes a confirmed order.
     */
    fun processOrder() {
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val result = orderRepository.processOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadOrder()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Dispatches an order for delivery.
     */
    fun dispatchOrder() {
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val result = orderRepository.dispatchOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadOrder()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Marks an order as delivered.
     */
    fun deliverOrder() {
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionError = null) }
            when (val result = orderRepository.deliverOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadOrder()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isActionLoading = false, actionError = result.exception.message) }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Shows the rejection dialog.
     */
    fun showRejectDialog() {
        _uiState.update { it.copy(showRejectDialog = true, rejectReason = "") }
    }

    /**
     * Dismisses the rejection dialog.
     */
    fun dismissRejectDialog() {
        _uiState.update { it.copy(showRejectDialog = false, rejectReason = "") }
    }

    /**
     * Updates the rejection reason text.
     */
    fun onRejectReasonChanged(reason: String) {
        _uiState.update { it.copy(rejectReason = reason) }
    }

    /**
     * Shows a confirmation dialog for the given action.
     */
    fun showConfirmDialog(actionLabel: String) {
        _uiState.update { it.copy(showConfirmDialog = actionLabel) }
    }

    /**
     * Dismisses the confirmation dialog.
     */
    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = null) }
    }

    /**
     * Executes the confirmed action.
     */
    fun executeConfirmedAction() {
        val action = _uiState.value.showConfirmDialog
        dismissConfirmDialog()
        when (action) {
            "Confirm" -> confirmOrder()
            "Process" -> processOrder()
            "Dispatch" -> dispatchOrder()
            "Deliver" -> deliverOrder()
        }
    }

    /**
     * Updates the rejection reason.
     */
    fun onRejectReasonUpdated(reason: String) {
        _uiState.update { it.copy(rejectReason = reason) }
    }

    /**
     * Retries loading after an error.
     */
    fun onRetry() {
        loadOrder()
    }

    /**
     * Refreshes order details (used for pull-to-refresh).
     */
    fun onRefresh() {
        loadOrder()
    }

    /**
     * Clears the action error.
     */
    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }
}
