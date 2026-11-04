package com.nearai.admin.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import com.nearai.admin.feature.catalog.domain.ProductSortBy
import com.nearai.admin.feature.catalog.domain.ProductStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductCreateUiState(
    val categoriesLoaded: Boolean = false,
    val availableCategories: List<com.nearai.admin.feature.catalog.domain.Category> = emptyList(),
    val isCreating: Boolean = false,
    val error: String? = null
)

sealed interface ProductCreateEvent {
    object ProductCreated : ProductCreateEvent
}

@HiltViewModel
class ProductCreateViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductCreateUiState())
    val uiState: StateFlow<ProductCreateUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<ProductCreateEvent?>(null)
    val event: StateFlow<ProductCreateEvent?> = _event.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            catalogRepository.listCategories(page = 1, size = 100).let { result ->
                if (result is Result.Success) {
                    _uiState.update {
                        it.copy(
                            categoriesLoaded = true,
                            availableCategories = result.data.categories
                        )
                    }
                }
            }
        }
    }

    fun onCreateProduct(
        name: String,
        description: String,
        priceAmount: String,
        priceCurrency: String,
        categoryId: String?,
        status: ProductStatus,
        onCreated: () -> Unit
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Product name is required") }
            return
        }
        if (description.isBlank()) {
            _uiState.update { it.copy(error = "Description is required") }
            return
        }

        val price = priceAmount.toDoubleOrNull()
        if (price == null || price <= 0) {
            _uiState.update { it.copy(error = "Valid price is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }

            when (val result = catalogRepository.createProduct(
                name = name.trim(),
                description = description.trim(),
                priceAmount = price,
                priceCurrency = priceCurrency.ifBlank { "USD" },
                categoryId = categoryId,
                status = status
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(isCreating = false) }
                    _event.value = ProductCreateEvent.ProductCreated
                    onCreated()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
