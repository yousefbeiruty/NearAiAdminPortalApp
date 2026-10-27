package com.nearai.admin.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.ActivityEvent
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import com.nearai.admin.feature.dashboard.domain.KpiMetric
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val kpiMetrics: List<KpiMetricItem> = emptyList(),
    val activityEvents: List<ActivityEventItem> = emptyList(),
    val lastUpdated: String = "",
    val clusterStatus: String = "",
    val error: String? = null,
    val isDevelopmentMode: Boolean = true
)

/**
 * UI-specific KPI metric with display formatting.
 */
data class KpiMetricItem(
    val title: String,
    val value: String,
    val delta: String,
    val isPositive: Boolean
)

/**
 * UI-specific activity event with display formatting.
 */
data class ActivityEventItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val statusLabel: String,
    val statusType: String
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
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = dashboardRepository.getDashboardData()) {
                is Result.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            kpiMetrics = data.kpiMetrics.map { kpi ->
                                KpiMetricItem(
                                    title = kpi.title,
                                    value = kpi.value,
                                    delta = kpi.delta,
                                    isPositive = kpi.isPositive
                                )
                            },
                            activityEvents = data.activityEvents.map { evt ->
                                ActivityEventItem(
                                    id = evt.id,
                                    title = evt.title,
                                    subtitle = evt.subtitle,
                                    timestamp = evt.timestamp,
                                    statusLabel = evt.statusLabel,
                                    statusType = evt.statusType
                                )
                            },
                            lastUpdated = data.lastUpdated,
                            clusterStatus = data.clusterStatus
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
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
        loadDashboard()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
