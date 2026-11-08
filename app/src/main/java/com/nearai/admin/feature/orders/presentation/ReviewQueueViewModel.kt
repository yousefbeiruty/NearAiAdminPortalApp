package com.nearai.admin.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.orders.domain.ApprovalStatus
import com.nearai.admin.feature.orders.domain.OrderRepository
import com.nearai.admin.feature.orders.domain.ReviewQueueOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Filter options for the review queue.
 */
enum class ReviewQueueFilter {
    ALL,
    STANDARD,
    MANDATORY
}

/**
 * Immutable UI state for the Review Queue screen.
 */
data class ReviewQueueUiState(
    val isLoading: Boolean = false,
    val orders: List<ReviewQueueOrder> = emptyList(),
    val error: String? = null,
    val filter: ReviewQueueFilter = ReviewQueueFilter.ALL,
    val page: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val isActionLoading: Boolean = false,
    val actionError: String? = null,
    val showRejectDialog: Boolean = false,
    val rejectOrderId: String? = null,
    val rejectReason: String = "",
    val showApproveDialog: Boolean = false,
    val approveOrderId: String? = null
)

/**
 * ViewModel for the Review Queue screen.
 * Manages loading, filtering, pagination, and approve/reject actions.
 */
@HiltViewModel
class ReviewQueueViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewQueueUiState())
    val uiState: StateFlow<ReviewQueueUiState> = _uiState.asStateFlow()

    init {
        loadReviewQueue()
    }

    /**
     * Loads the review queue from the backend.
     */
    fun loadReviewQueue() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val filter = _uiState.value.filter
            val mandatoryOnly = filter == ReviewQueueFilter.MANDATORY

            when (val result = orderRepository.listReviewQueue(
                page = 1,
                size = 20,
                mandatoryOnly = mandatoryOnly
            )) {
                is Result.Success -> {
                    var orders = result.data.orders
                    // Apply standard filter locally if needed
                    if (filter == ReviewQueueFilter.STANDARD) {
                        orders = orders.filter { !it.isMandatory }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = orders,
                            page = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems
                        )
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
     * Approves an order after confirmation.
     */
    fun approveOrder() {
        val orderId = _uiState.value.approveOrderId ?: return
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true, actionError = null, showApproveDialog = false)
            }
            when (val result = orderRepository.approveOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadReviewQueue()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isActionLoading = false, actionError = result.exception.message)
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Rejects an order with a reason.
     */
    fun rejectOrder() {
        val orderId = _uiState.value.rejectOrderId ?: return
        val reason = _uiState.value.rejectReason.trim()
        if (reason.isEmpty()) {
            _uiState.update { it.copy(actionError = "Rejection reason is required") }
            return
        }
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true, actionError = null, showRejectDialog = false)
            }
            when (val result = orderRepository.rejectForApproval(orderId, reason)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    loadReviewQueue()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isActionLoading = false, actionError = result.exception.message)
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                }
            }
        }
    }

    /**
     * Changes the filter and reloads the queue.
     */
    fun onFilterChanged(filter: ReviewQueueFilter) {
        _uiState.update { it.copy(filter = filter) }
        loadReviewQueue()
    }

    /**
     * Shows the approve confirmation dialog for the given order.
     */
    fun showApproveDialog(orderId: String) {
        _uiState.update { it.copy(showApproveDialog = true, approveOrderId = orderId) }
    }

    /**
     * Dismisses the approve confirmation dialog.
     */
    fun dismissApproveDialog() {
        _uiState.update { it.copy(showApproveDialog = false, approveOrderId = null) }
    }

    /**
     * Shows the reject dialog for the given order.
     */
    fun showRejectDialog(orderId: String) {
        _uiState.update {
            it.copy(showRejectDialog = true, rejectOrderId = orderId, rejectReason = "")
        }
    }

    /**
     * Dismisses the reject dialog.
     */
    fun dismissRejectDialog() {
        _uiState.update {
            it.copy(showRejectDialog = false, rejectOrderId = null, rejectReason = "")
        }
    }

    /**
     * Updates the rejection reason text.
     */
    fun onRejectReasonChanged(reason: String) {
        _uiState.update { it.copy(rejectReason = reason) }
    }

    /**
     * Refreshes the review queue (pull-to-refresh).
     */
    fun onRefresh() {
        loadReviewQueue()
    }

    /**
     * Retries after an error.
     */
    fun onRetry() {
        loadReviewQueue()
    }

    /**
     * Clears the action error.
     */
    fun clearActionError() {
        _uiState.update { it.copy(actionError = null) }
    }
}
