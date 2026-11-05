package com.nearai.admin.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import com.nearai.admin.feature.catalog.domain.Product
import com.nearai.admin.feature.catalog.domain.ProductSortBy
import com.nearai.admin.feature.catalog.domain.ProductStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Products List screen.
 */
data class ProductsListUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val searchQuery: String = "",
    val selectedStatus: ProductStatus? = null,
    val selectedCategoryId: String? = null,
    val selectedSortBy: ProductSortBy = ProductSortBy.NEWEST,
    val isCreating: Boolean = false,
    val createError: String? = null
)

sealed interface ProductsListEvent {
    object ShowCreateDialog : ProductsListEvent
    object HideCreateDialog : ProductsListEvent
    object ProductCreated : ProductsListEvent
}

@HiltViewModel
class ProductsListViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsListUiState())
    val uiState: StateFlow<ProductsListUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<ProductsListEvent?>(null)
    val event: StateFlow<ProductsListEvent?> = _event.asStateFlow()

    private companion object {
        const val PAGE_SIZE = 20
    }

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = state.products.isEmpty(),
                    error = null,
                    currentPage = 1,
                    hasMore = false,
                    isLoadingMore = false
                )
            }

            when (val result = catalogRepository.listProducts(
                page = 1,
                size = PAGE_SIZE,
                categoryId = state.selectedCategoryId,
                status = state.selectedStatus,
                search = if (state.searchQuery.isNotBlank()) state.searchQuery else null,
                sortBy = state.selectedSortBy
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = result.data.products,
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

            when (val result = catalogRepository.listProducts(
                page = nextPage,
                size = PAGE_SIZE,
                categoryId = state.selectedCategoryId,
                status = state.selectedStatus,
                search = if (state.searchQuery.isNotBlank()) state.searchQuery else null,
                sortBy = state.selectedSortBy
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            products = it.products + result.data.products,
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadProducts()
    }

    fun onStatusFilterChanged(status: ProductStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
        loadProducts()
    }

    fun onCategoryFilterChanged(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadProducts()
    }

    fun onSortByChanged(sortBy: ProductSortBy) {
        _uiState.update { it.copy(selectedSortBy = sortBy) }
        loadProducts()
    }

    fun onCreateProduct(
        name: String,
        description: String,
        priceAmount: Double,
        priceCurrency: String,
        categoryId: String?,
        status: ProductStatus
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(createError = "Product name is required") }
            return
        }
        if (priceAmount <= 0) {
            _uiState.update { it.copy(createError = "Price must be greater than 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }

            when (val result = catalogRepository.createProduct(
                name = name.trim(),
                description = description,
                priceAmount = priceAmount,
                priceCurrency = priceCurrency,
                categoryId = categoryId,
                status = status
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            createError = null,
                            products = listOf(result.data) + it.products
                        )
                    }
                    _event.value = ProductsListEvent.ProductCreated
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
        _event.value = ProductsListEvent.ShowCreateDialog
    }

    fun onHideCreateDialog() {
        _uiState.update { it.copy(createError = null) }
        _event.value = ProductsListEvent.HideCreateDialog
    }

    fun onRetry() = loadProducts()
}
