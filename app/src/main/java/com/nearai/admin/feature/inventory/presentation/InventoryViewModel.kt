package com.nearai.admin.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import com.nearai.admin.feature.catalog.domain.Product
import com.nearai.admin.feature.catalog.domain.ProductStatus
import com.nearai.admin.feature.catalog.domain.ProductVariant
import com.nearai.admin.feature.catalog.domain.StockBalance
import com.nearai.admin.feature.catalog.domain.StockMovement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Combined inventory item that aggregates a product, its variants, and stock data.
 */
data class InventoryItem(
    val product: Product,
    val variants: List<InventoryVariant> = emptyList()
)

/**
 * A variant with its stock balance.
 */
data class InventoryVariant(
    val variant: ProductVariant,
    val stock: StockBalance? = null
)

/**
 * Immutable UI state for the Inventory screen.
 */
data class InventoryUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val inventoryItems: List<InventoryItem> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val filterStatus: ProductStatus? = null,
    val filterStock: StockFilter? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val kpiActiveSkus: Int = 0,
    val kpiTotalProducts: Int = 0,
    val kpiLowStockCount: Int = 0,
    val kpiOutOfStockCount: Int = 0,
    val selectedVariantId: String? = null,
    val stockMovements: List<StockMovement> = emptyList(),
    val stockMovementsPage: Int = 1,
    val stockMovementsTotalPages: Int = 1,
    val stockMovementsHasMore: Boolean = false,
    val isLoadingMovements: Boolean = false,
    val isStockOperationLoading: Boolean = false
)

/**
 * Stock level filter for inventory listing.
 */
enum class StockFilter {
    LOW_STOCK,
    OUT_OF_STOCK
}

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface InventoryEvent {
    data class NavigateToEditProduct(val productId: String) : InventoryEvent
    object NavigateToCreateProduct : InventoryEvent
    data class StockOperationSuccess(val message: String) : InventoryEvent
    data class StockOperationError(val message: String) : InventoryEvent
}

/**
 * ViewModel for the Inventory screen.
 * Manages product listing with variants and stock data, search, filtering,
 * stock operations (receive/adjust), and stock movement history.
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<InventoryEvent?>(null)
    val event: StateFlow<InventoryEvent?> = _event.asStateFlow()

    private companion object {
        const val PAGE_SIZE = 20
        const val LOW_STOCK_THRESHOLD = 10
    }

    init {
        loadInventory()
    }

    /**
     * Loads the first page of inventory with current filters.
     */
    fun loadInventory() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.inventoryItems.isEmpty(),
                    isRefreshing = _uiState.value.inventoryItems.isNotEmpty(),
                    error = null,
                    currentPage = 1,
                    hasMore = false,
                    isLoadingMore = false
                )
            }

            val state = _uiState.value
            when (val result = catalogRepository.listProducts(
                page = 1,
                size = PAGE_SIZE,
                status = state.filterStatus,
                search = state.searchQuery.ifBlank { null }
            )) {
                is Result.Success -> {
                    val items = result.data.products.map { product ->
                        InventoryItem(product = product)
                    }
                    // Load variants and stock for each product
                    loadVariantsAndStock(items)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            inventoryItems = items,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages,
                            kpiActiveSkus = items.count { item ->
                                item.product.status == ProductStatus.ACTIVE
                            },
                            kpiTotalProducts = result.data.totalItems,
                            kpiLowStockCount = 0, // Updated after stock loads
                            kpiOutOfStockCount = 0 // Updated after stock loads
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
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Loads variants and stock data for each inventory item asynchronously.
     */
    private fun loadVariantsAndStock(items: List<InventoryItem>) {
        viewModelScope.launch {
            val updatedItems = items.map { item ->
                val variantsResult = catalogRepository.listVariants(item.product.id)
                val variants = when (variantsResult) {
                    is Result.Success -> variantsResult.data.variants
                    else -> emptyList()
                }

                val inventoryVariants = variants.map { variant ->
                    val stockResult = catalogRepository.getStockAvailability(variant.id)
                    val stock = when (stockResult) {
                        is Result.Success -> stockResult.data
                        else -> null
                    }
                    InventoryVariant(variant = variant, stock = stock)
                }

                InventoryItem(product = item.product, variants = inventoryVariants)
            }

            // Calculate low stock and out of stock counts
            val allVariants = updatedItems.flatMap { it.variants }
            val lowStockCount = allVariants.count { iv ->
                val qty = iv.stock?.availableQuantity ?: 0
                qty > 0 && qty < LOW_STOCK_THRESHOLD
            }
            val outOfStockCount = allVariants.count { iv ->
                iv.stock?.availableQuantity ?: 0 == 0
            }

            // Apply stock filter if active
            val filteredItems = when (_uiState.value.filterStock) {
                StockFilter.LOW_STOCK -> updatedItems.filter { item ->
                    item.variants.any { iv ->
                        val qty = iv.stock?.availableQuantity ?: 0
                        qty > 0 && qty < LOW_STOCK_THRESHOLD
                    }
                }
                StockFilter.OUT_OF_STOCK -> updatedItems.filter { item ->
                    item.variants.any { iv ->
                        iv.stock?.availableQuantity ?: 0 == 0
                    }
                }
                null -> updatedItems
            }

            _uiState.update {
                it.copy(
                    inventoryItems = filteredItems,
                    kpiLowStockCount = lowStockCount,
                    kpiOutOfStockCount = outOfStockCount
                )
            }
        }
    }

    /**
     * Loads the next page of inventory (pagination).
     */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.currentPage + 1
            when (val result = catalogRepository.listProducts(
                page = nextPage,
                size = PAGE_SIZE,
                status = state.filterStatus,
                search = state.searchQuery.ifBlank { null }
            )) {
                is Result.Success -> {
                    val items = result.data.products.map { product ->
                        InventoryItem(product = product)
                    }
                    loadVariantsAndStock(items)

                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            inventoryItems = it.inventoryItems + items,
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

    /**
     * Updates the search query and reloads inventory.
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadInventory()
    }

    /**
     * Updates the status filter and reloads inventory.
     */
    fun setFilterStatus(status: ProductStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
        loadInventory()
    }

    /**
     * Sets the stock level filter and reloads inventory.
     */
    fun setFilterStock(filter: StockFilter?) {
        _uiState.update { it.copy(filterStock = filter) }
        loadInventory()
    }

    /**
     * Clears all filters and reloads.
     */
    fun clearFilters() {
        _uiState.update {
            it.copy(filterStatus = null, searchQuery = "", filterStock = null)
        }
        loadInventory()
    }

    /**
     * Receives stock for a variant.
     */
    fun receiveStock(variantId: String, quantity: Int, reason: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isStockOperationLoading = true) }

            when (val result = catalogRepository.receiveStock(variantId, quantity, reason)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isStockOperationLoading = false,
                            selectedVariantId = variantId
                        )
                    }
                    _event.value = InventoryEvent.StockOperationSuccess(
                        "Received $quantity units for variant $variantId"
                    )
                    // Refresh the affected item
                    refreshInventory()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isStockOperationLoading = false) }
                    _event.value = InventoryEvent.StockOperationError(
                        result.exception.message ?: "Failed to receive stock"
                    )
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Adjusts stock for a variant.
     */
    fun adjustStock(variantId: String, quantityDelta: Int, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isStockOperationLoading = true) }

            when (val result = catalogRepository.adjustStock(variantId, quantityDelta, reason)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isStockOperationLoading = false,
                            selectedVariantId = variantId
                        )
                    }
                    _event.value = InventoryEvent.StockOperationSuccess(
                        "Adjusted stock by $quantityDelta for variant $variantId"
                    )
                    refreshInventory()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isStockOperationLoading = false) }
                    _event.value = InventoryEvent.StockOperationError(
                        result.exception.message ?: "Failed to adjust stock"
                    )
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Loads stock movement history for a variant.
     */
    fun loadStockMovements(variantId: String, page: Int = 1) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedVariantId = variantId,
                    isLoadingMovements = true,
                    stockMovementsPage = page
                )
            }

            when (val result = catalogRepository.getStockMovements(variantId, page, PAGE_SIZE)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMovements = false,
                            stockMovements = if (page == 1) {
                                result.data.movements
                            } else {
                                it.stockMovements + result.data.movements
                            },
                            stockMovementsPage = result.data.page,
                            stockMovementsTotalPages = result.data.totalPages,
                            stockMovementsHasMore = result.data.page < result.data.totalPages
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMovements = false,
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
     * Loads more stock movements (pagination).
     */
    fun loadMoreStockMovements() {
        val state = _uiState.value
        if (state.isLoadingMovements || !state.stockMovementsHasMore || state.selectedVariantId == null) return

        val nextPage = state.stockMovementsPage + 1
        loadStockMovements(state.selectedVariantId, nextPage)
    }

    /**
     * Navigates to edit a product.
     */
    fun onEditProduct(productId: String) {
        _event.value = InventoryEvent.NavigateToEditProduct(productId)
    }

    /**
     * Navigates to create a new product.
     */
    fun onCreateProduct() {
        _event.value = InventoryEvent.NavigateToCreateProduct
    }

    /**
     * Retries loading after an error.
     */
    fun onRetry() {
        loadInventory()
    }

    /**
     * Pull-to-refresh action.
     */
    fun onRefresh() {
        loadInventory()
    }

    /**
     * Refreshes inventory data without resetting state.
     */
    private fun refreshInventory() {
        viewModelScope.launch {
            val state = _uiState.value
            when (val result = catalogRepository.listProducts(
                page = 1,
                size = PAGE_SIZE,
                status = state.filterStatus,
                search = state.searchQuery.ifBlank { null }
            )) {
                is Result.Success -> {
                    val items = result.data.products.map { product ->
                        InventoryItem(product = product)
                    }
                    loadVariantsAndStock(items)
                    _uiState.update {
                        it.copy(
                            inventoryItems = items,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages,
                            kpiActiveSkus = items.count { item ->
                                item.product.status == ProductStatus.ACTIVE
                            },
                            kpiTotalProducts = result.data.totalItems
                        )
                    }
                }
                else -> {
                    // Silently fail on refresh
                }
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clears the current event.
     */
    fun clearEvent() {
        _event.value = null
    }
}

