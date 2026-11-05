package com.nearai.admin.feature.aiusage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.aiusage.domain.AiUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the AI Usage screen.
 */
data class AiUsageUiState(
    val isLoading: Boolean = false,
    val isFeatureAvailable: Boolean = false,
    val monthlyBudgetConsumed: Double = 0.0,
    val monthlyBudgetTotal: Double = 0.0,
    val error: String? = null
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface AiUsageEvent {
    object Retry : AiUsageEvent
}

/**
 * ViewModel for the AI Usage screen.
 *
 * Currently reports the feature as unavailable since the backend AI usage API
 * is not yet implemented. No fabricated data is displayed.
 */
@HiltViewModel
class AiUsageViewModel @Inject constructor(
    private val aiUsageRepository: AiUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUsageUiState())
    val uiState: StateFlow<AiUsageUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<AiUsageEvent?>(null)
    val event: StateFlow<AiUsageEvent?> = _event.asStateFlow()

    init {
        loadAiUsage()
    }

    /**
     * Attempts to load AI usage data from the repository.
     * Since the backend API is not yet available, this will report the feature as unavailable.
     */
    fun loadAiUsage() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            when (val result = aiUsageRepository.getAiUsageData()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isFeatureAvailable = true,
                            monthlyBudgetConsumed = result.data.monthlyBudgetConsumed,
                            monthlyBudgetTotal = result.data.monthlyBudgetTotal,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isFeatureAvailable = false,
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
        loadAiUsage()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
