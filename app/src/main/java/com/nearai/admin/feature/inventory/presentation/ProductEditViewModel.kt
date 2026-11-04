package com.nearai.admin.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import com.nearai.admin.feature.catalog.domain.Product
import com.nearai.admin.feature.catalog.domain.ProductStatus
import com.nearai.admin.feature.catalog.domain.ProductVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Form data for editing a product.
 */
data class ProductFormData(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val status: ProductStatus = ProductStatus.ACTIVE,
    val categoryId: String? = null
)

/**
 * Validation result for product form.
 */
data class ValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null
)

/**
 * Immutable UI state for the Product Edit screen.
 */
data class ProductEditUiState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val variants: List<ProductVariant> = emptyList(),
    val formData: ProductFormData = ProductFormData(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val validationError: ValidationResult? = null
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface ProductEditEvent {
    object SaveSuccess : ProductEditEvent
    data class SaveError(val message: String) : ProductEditEvent
    object NavigateBack : ProductEditEvent
}

/**
 * ViewModel for the Product Edit screen.
 * Manages product loading, editing, validation, and saving.
 */
@HiltViewModel
class ProductEditViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<ProductEditEvent?>(null)
    val event: StateFlow<ProductEditEvent?> = _event.asStateFlow()

    /**
     * Sets the product ID and loads product details. Called from navigation.
     */
    fun setProductId(productId: String) {
        loadProduct(productId)
    }

    /**
     * Loads product details by ID.
     */
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = catalogRepository.getProduct(productId)) {
                is Result.Success -> {
                    val product = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = product,
                            formData = ProductFormData(
                                name = product.name,
                                description = product.description,
                                price = product.priceAmount.toString(),
                                status = product.status,
                                categoryId = product.categoryId
                            )
                        )
                    }
                    // Load variants
                    loadVariants(productId)
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
     * Loads variants for the current product.
     */
    private fun loadVariants(productId: String) {
        viewModelScope.launch {
            when (val result = catalogRepository.listVariants(productId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(variants = result.data.variants)
                    }
                }
                else -> {
                    // Silently fail - variants are optional
                }
            }
        }
    }

    /**
     * Updates the form name field.
     */
    fun setName(name: String) {
        _uiState.update {
            it.copy(
                formData = it.formData.copy(name = name),
                validationError = null
            )
        }
    }

    /**
     * Updates the form description field.
     */
    fun setDescription(description: String) {
        _uiState.update {
            it.copy(
                formData = it.formData.copy(description = description),
                validationError = null
            )
        }
    }

    /**
     * Updates the form price field.
     */
    fun setPrice(price: String) {
        _uiState.update {
            it.copy(
                formData = it.formData.copy(price = price),
                validationError = null
            )
        }
    }

    /**
     * Updates the form status field.
     */
    fun setStatus(status: ProductStatus) {
        _uiState.update {
            it.copy(formData = it.formData.copy(status = status))
        }
    }

    /**
     * Validates the current form data.
     */
    fun validate(): ValidationResult {
        val formData = _uiState.value.formData
        val nameError = if (formData.name.isBlank()) "Product name is required" else null
        val descriptionError = if (formData.description.isBlank()) "Description is required" else null
        val priceError = when {
            formData.price.isBlank() -> "Price is required"
            formData.price.toDoubleOrNull() == null -> "Invalid price format"
            formData.price.toDouble() <= 0 -> "Price must be greater than 0"
            else -> null
        }
        val isValid = nameError == null && descriptionError == null && priceError == null
        return ValidationResult(isValid, nameError, descriptionError, priceError)
    }

    /**
     * Saves the product changes.
     */
    fun saveProduct() {
        val validation = validate()
        if (!validation.isValid) {
            _uiState.update { it.copy(validationError = validation) }
            return
        }

        val product = _uiState.value.product ?: return
        val formData = _uiState.value.formData

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, validationError = null) }

            val priceAmount = formData.price.toDoubleOrNull()
            when (val result = catalogRepository.updateProduct(
                productId = product.id,
                name = formData.name,
                description = formData.description,
                priceAmount = priceAmount,
                status = formData.status
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            product = result.data,
                            successMessage = "Product updated successfully"
                        )
                    }
                    _event.value = ProductEditEvent.SaveSuccess
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = result.exception.message
                        )
                    }
                    _event.value = ProductEditEvent.SaveError(result.exception.message ?: "Save failed")
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
        val product = _uiState.value.product
        if (product != null) {
            loadProduct(product.id)
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null, validationError = null) }
    }

    /**
     * Clears the success message.
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Clears the current event.
     */
    fun clearEvent() {
        _event.value = null
    }

    /**
     * Navigates back.
     */
    fun onNavigateBack() {
        _event.value = ProductEditEvent.NavigateBack
    }
}
