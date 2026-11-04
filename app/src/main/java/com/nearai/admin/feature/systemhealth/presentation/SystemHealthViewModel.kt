package com.nearai.admin.feature.systemhealth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.systemhealth.domain.ComponentHealth
import com.nearai.admin.feature.systemhealth.domain.SystemHealthData
import com.nearai.admin.feature.systemhealth.domain.SystemHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the System Health screen.
 */
data class SystemHealthUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val healthData: SystemHealthData? = null,
    val overallStatus: String = "",
    val checkedAt: String = "",
    val environment: String = "",
    val uptimeSeconds: Long = 0,
    val components: List<ComponentHealth> = emptyList(),
    val healthyCount: Int = 0,
    val unhealthyCount: Int = 0
)

/**
 * ViewModel for the System Health screen.
 * Manages health data loading, refresh, and stale response rejection.
 */
@HiltViewModel
class SystemHealthViewModel @Inject constructor(
    private val repository: SystemHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemHealthUiState())
    val uiState: StateFlow<SystemHealthUiState> = _uiState.asStateFlow()

    // Generation counter to reject stale refresh responses
    private var loadGeneration = 0

    init {
        loadHealth()
    }

    /**
     * Loads the system health data from the backend.
     */
    fun loadHealth() {
        loadGeneration++
        val currentGeneration = loadGeneration

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.healthData == null,
                    isRefreshing = _uiState.value.healthData != null,
                    error = null
                )
            }

            when (val result = repository.getSystemHealth()) {
                is Result.Success -> {
                    // Reject stale responses
                    if (loadGeneration != currentGeneration) return@launch

                    val data = result.data
                    val components = data.components
                    val healthyCount = components.count { it.status == "healthy" }
                    val unhealthyCount = components.count { it.status == "unhealthy" }

                    _uiState.update {
                        it.copy(
                            healthData = data,
                            overallStatus = data.overallStatus,
                            checkedAt = data.checkedAt,
                            environment = data.application.environment,
                            uptimeSeconds = data.application.uptimeSeconds,
                            components = components,
                            healthyCount = healthyCount,
                            unhealthyCount = unhealthyCount,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    if (loadGeneration != currentGeneration) return@launch
                    _uiState.update {
                        it.copy(
                            error = result.exception.message,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is Result.Loading -> {
                    // No-op: loading state already set before the call
                }
            }
        }
    }

    /**
     * Manually refreshes the system health data.
     */
    fun onRefresh() {
        loadHealth()
    }

    /**
     * Clears the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
