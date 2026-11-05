package com.nearai.admin.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.Category
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Categories List screen.
 */
data class CategoriesListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isCreating: Boolean = false,
    val createError: String? = null
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface CategoriesListEvent {
    object ShowCreateDialog : CategoriesListEvent
    object HideCreateDialog : CategoriesListEvent
    object CategoryCreated : CategoriesListEvent
}

/**
 * ViewModel for the Categories List screen.
 * Manages category listing, pagination, and category creation.
 */
@HiltViewModel
class CategoriesListViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesListUiState())
    val uiState: StateFlow<CategoriesListUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<CategoriesListEvent?>(null)
    val event: StateFlow<CategoriesListEvent?> = _event.asStateFlow()

    private companion object {
        const val PAGE_SIZE = 20
    }

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.categories.isEmpty(),
                    isRefreshing = _uiState.value.categories.isNotEmpty(),
                    error = null,
                    currentPage = 1,
                    hasMore = false,
                    isLoadingMore = false
                )
            }

            when (val result = catalogRepository.listCategories(page = 1, size = PAGE_SIZE)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            categories = result.data.categories,
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
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = state.currentPage + 1

            when (val result = catalogRepository.listCategories(page = nextPage, size = PAGE_SIZE)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            categories = it.categories + result.data.categories,
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

    fun onCreateCategory(name: String, description: String, parentId: String?) {
        if (name.isBlank()) {
            _uiState.update { it.copy(createError = "Category name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }

            when (val result = catalogRepository.createCategory(name.trim(), description = description, parentId = parentId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            createError = null,
                            categories = listOf(result.data) + it.categories
                        )
                    }
                    _event.value = CategoriesListEvent.CategoryCreated
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            createError = result.exception.message
                        )
                    }
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun onShowCreateDialog() {
        _event.value = CategoriesListEvent.ShowCreateDialog
    }

    fun onHideCreateDialog() {
        _uiState.update { it.copy(createError = null) }
        _event.value = CategoriesListEvent.HideCreateDialog
    }

    fun onRetry() = loadCategories()

    fun onRefresh() = loadCategories()

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
