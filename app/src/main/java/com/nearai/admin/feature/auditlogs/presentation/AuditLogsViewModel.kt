package com.nearai.admin.feature.auditlogs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.auditlogs.domain.AuditLog
import com.nearai.admin.feature.auditlogs.domain.AuditLogsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Audit Logs screen.
 */
data class AuditLogsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val logs: List<AuditLog> = emptyList(),
    val selectedLog: AuditLog? = null,
    val expandedLogId: String? = null,
    val error: String? = null,
    val filterAction: String? = null,
    val filterResourceType: String? = null,
    val filterResourceId: String? = null,
    val filterStartDate: String? = null,
    val filterEndDate: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false
)

/**
 * ViewModel for the Audit Logs screen.
 * Manages audit log listing, filtering, pagination, and stale response rejection.
 */
@HiltViewModel
class AuditLogsViewModel @Inject constructor(
    private val auditLogsRepository: AuditLogsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditLogsUiState())
    val uiState: StateFlow<AuditLogsUiState> = _uiState.asStateFlow()

    // Generation counter to reject stale pagination/enrichment responses
    private var loadGeneration = 0

    // Prevent duplicate loadMore requests
    private var isLoadMorePending = false

    private companion object {
        const val PAGE_SIZE = 20
    }

    init {
        loadLogs()
    }

    /**
     * Loads the first page of audit logs.
     */
    fun loadLogs() {
        loadGeneration++
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.logs.isEmpty(),
                    isRefreshing = _uiState.value.logs.isNotEmpty(),
                    error = null,
                    currentPage = 1,
                    hasMore = false,
                    isLoadingMore = false
                )
            }

            val state = _uiState.value
            when (val result = auditLogsRepository.listAuditLogs(
                page = 1,
                size = PAGE_SIZE,
                action = state.filterAction,
                resourceType = state.filterResourceType,
                resourceId = state.filterResourceId,
                startDate = state.filterStartDate,
                endDate = state.filterEndDate
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            logs = result.data.logs,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.exception.message,
                            isLoading = false,
                            isRefreshing = false
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
     * Loads the next page of audit logs (pagination).
     * Captures loadGeneration at start so stale pagination results are rejected
     * if a newer load (filter change, refresh) started while this was in flight.
     */
    fun loadMore() {
        val state = _uiState.value
        if (isLoadMorePending || state.isLoadingMore || !state.hasMore) return

        // Capture generation at entry so we can reject stale results
        val generationAtStart = loadGeneration

        isLoadMorePending = true
        viewModelScope.launch {
            isLoadMorePending = false
            _uiState.update { it.copy(isLoadingMore = true) }

            when (val result = auditLogsRepository.listAuditLogs(
                page = state.currentPage + 1,
                size = PAGE_SIZE,
                action = state.filterAction,
                resourceType = state.filterResourceType,
                resourceId = state.filterResourceId,
                startDate = state.filterStartDate,
                endDate = state.filterEndDate
            )) {
                is Result.Success -> {
                    // Reject if a newer load started while we were fetching
                    if (loadGeneration != generationAtStart) {
                        _uiState.update { it.copy(isLoadingMore = false) }
                        return@launch
                    }

                    val newLogs = result.data.logs
                    _uiState.update {
                        it.copy(
                            logs = it.logs + newLogs,
                            currentPage = result.data.page,
                            hasMore = result.data.page < result.data.totalPages,
                            isLoadingMore = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.exception.message,
                            hasMore = false,
                            isLoadingMore = false
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
     * Retries a failed pagination load.
     */
    fun onRetryLoadMore() {
        _uiState.update { it.copy(hasMore = true, error = null) }
        loadMore()
    }

    /**
     * Refreshes the audit logs, resetting pagination.
     */
    fun onRefresh() {
        loadLogs()
    }

    /**
     * Clears the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Toggles the expanded state of a log entry.
     */
    fun onToggleExpand(logId: String) {
        _uiState.update {
            if (it.expandedLogId == logId) {
                it.copy(expandedLogId = null)
            } else {
                it.copy(expandedLogId = logId)
            }
        }
    }

    /**
     * Selects a log entry for detail view.
     */
    fun onSelectLog(log: AuditLog) {
        _uiState.update { it.copy(selectedLog = log) }
    }

    /**
     * Dismisses the selected log entry.
     */
    fun onDismissSelectedLog() {
        _uiState.update { it.copy(selectedLog = null) }
    }

    // ─── Filter setters ──────────────────────────────────────────────────────

    fun setFilterAction(action: String?) {
        _uiState.update {
            it.copy(filterAction = action)
        }
        loadLogs()
    }

    fun setFilterResourceType(resourceType: String?) {
        _uiState.update {
            it.copy(filterResourceType = resourceType)
        }
        loadLogs()
    }

    fun setFilterResourceId(resourceId: String?) {
        _uiState.update {
            it.copy(filterResourceId = resourceId)
        }
        loadLogs()
    }

    fun setFilterDateRange(startDate: String?, endDate: String?) {
        _uiState.update {
            it.copy(filterStartDate = startDate, filterEndDate = endDate)
        }
        loadLogs()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filterAction = null,
                filterResourceType = null,
                filterResourceId = null,
                filterStartDate = null,
                filterEndDate = null
            )
        }
        loadLogs()
    }
}
