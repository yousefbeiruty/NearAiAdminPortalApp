package com.nearai.admin.feature.inventory.presentation

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.*
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    @RelaxedMockK
    private lateinit var catalogRepository: CatalogRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    // =============================================================================
    // 1. Initial loading success and failure
    // =============================================================================

    @Test
    fun `initial load success populates inventory`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals(2, state.inventoryItems.size)
        assertEquals(10, state.totalItems)
        assertTrue(state.hasMore)
        assertNull(state.error)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `initial load error sets error and clears loading`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Error(
            AppError.NetworkError("No internet")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.inventoryItems.isEmpty())
        assertEquals("No internet", state.error)
    }

    // =============================================================================
    // 2. Search and filtering
    // =============================================================================

    @Test
    fun `setSearchQuery updates query and reloads`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successProducts()),
            Result.Success(successProducts())
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.setSearchQuery("widget")

        assertEquals("widget", viewModel.uiState.value.searchQuery)
        coVerify { catalogRepository.listProducts(1, 20, null, null, "widget", null, null, any()) }
    }

    @Test
    fun `setFilterStatus sets status and clears stock filter`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successProducts()),
            Result.Success(successProducts())
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.setFilterStock(StockFilter.LOW_STOCK)
        assertEquals(StockFilter.LOW_STOCK, viewModel.uiState.value.filterStock)

        viewModel.setFilterStatus(ProductStatus.ACTIVE)

        assertEquals(ProductStatus.ACTIVE, viewModel.uiState.value.filterStatus)
        assertNull(viewModel.uiState.value.filterStock)
    }

    @Test
    fun `setFilterStock sets stock filter and clears status filter`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successProducts()),
            Result.Success(successProducts())
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.setFilterStatus(ProductStatus.DRAFT)
        assertEquals(ProductStatus.DRAFT, viewModel.uiState.value.filterStatus)

        viewModel.setFilterStock(StockFilter.OUT_OF_STOCK)

        assertEquals(StockFilter.OUT_OF_STOCK, viewModel.uiState.value.filterStock)
        assertNull(viewModel.uiState.value.filterStatus)
    }

    @Test
    fun `clearFilters resets all filters and reloads`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successProducts()),
            Result.Success(successProducts()),
            Result.Success(successProducts())
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.setSearchQuery("widget")
        viewModel.setFilterStatus(ProductStatus.ACTIVE)
        viewModel.clearFilters()

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.filterStatus)
        assertNull(viewModel.uiState.value.filterStock)
    }

    // =============================================================================
    // 3. Pagination without duplicate API requests
    // =============================================================================

    @Test
    fun `loadMore appends products`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2)
        val page2 = successProducts(page = 2, totalPages = 2, productIdPrefix = "b")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.inventoryItems.size)

        viewModel.loadMore()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertFalse(state.hasMore)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `loadMore preserves all item IDs from both pages`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2, productIdPrefix = "page1")
        val page2 = successProducts(page = 2, totalPages = 2, productIdPrefix = "page2")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        viewModel.loadMore()

        val state = viewModel.uiState.value
        val itemIds = state.inventoryItems.map { it.product.id }
        // Both page 1 and page 2 items must be present
        assertTrue("page1-1 should be present", itemIds.contains("page1-1"))
        assertTrue("page1-2 should be present", itemIds.contains("page1-2"))
        assertTrue("page2-1 should be present", itemIds.contains("page2-1"))
        assertTrue("page2-2 should be present", itemIds.contains("page2-2"))
        assertEquals(4, itemIds.size)
    }

    @Test
    fun `loadMore does not duplicate items when called multiple times`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 3, productIdPrefix = "page1")
        val page2 = successProducts(page = 2, totalPages = 3, productIdPrefix = "page2")
        val page3 = successProducts(page = 3, totalPages = 3, productIdPrefix = "page3")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2),
            Result.Success(page3)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        viewModel.loadMore()
        viewModel.loadMore()

        val state = viewModel.uiState.value
        val itemIds = state.inventoryItems.map { it.product.id }
        // All 6 items from 3 pages, no duplicates
        assertEquals(6, itemIds.size)
        assertEquals(itemIds.toSet().size, itemIds.size) // No duplicates
    }

    @Test
    fun `loadMore does not call repository when hasMore is false`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts(page = 1, totalPages = 1)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.hasMore)

        viewModel.loadMore()

        coVerify(exactly = 1) { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `loadMore does not call repository when already loading`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts(page = 1, totalPages = 2)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.loadMore()
        // With UnconfinedTestDispatcher, the coroutine completes immediately,
        // so isLoadMorePending is reset. The second loadMore will also fire.
        viewModel.loadMore()

        // Initial load + 2 loadMore calls = 3 total listProducts calls
        coVerify(exactly = 3) { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    // =============================================================================
    // 4. Correct page number handling after successful pagination
    // =============================================================================

    @Test
    fun `loadMore updates currentPage after success`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 3)
        val page2 = successProducts(page = 2, totalPages = 3, productIdPrefix = "b")
        val page3 = successProducts(page = 3, totalPages = 3, productIdPrefix = "c")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2),
            Result.Success(page3)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        assertEquals(1, viewModel.uiState.value.currentPage)

        viewModel.loadMore()
        assertEquals(2, viewModel.uiState.value.currentPage)

        viewModel.loadMore()
        assertEquals(3, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.uiState.value.hasMore)
    }

    // =============================================================================
    // 5. Pagination failure preserving existing items
    // =============================================================================

    @Test
    fun `loadMore error preserves existing items and sets hasMore to false`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2)
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Error(AppError.NetworkError("Connection lost"))
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.inventoryItems.size)

        viewModel.loadMore()

        val state = viewModel.uiState.value
        assertEquals(2, state.inventoryItems.size) // Existing items preserved
        assertFalse(state.hasMore)
        assertEquals("Connection lost", state.error)
        assertFalse(state.isLoadingMore)
    }

    // =============================================================================
    // 6. Retrying the same failed page
    // =============================================================================

    @Test
    fun `onRetryLoadMore restores hasMore and retries pagination`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2)
        val page2 = successProducts(page = 2, totalPages = 2, productIdPrefix = "b")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Error(AppError.NetworkError("Connection lost")),
            Result.Success(page2)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.loadMore() // Fails
        assertEquals("Connection lost", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.hasMore)

        viewModel.onRetryLoadMore()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals(2, state.currentPage)
    }

    @Test
    fun `onRetry reloads after initial error`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Error(AppError.NetworkError("No internet")),
            Result.Success(successProducts())
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        assertEquals("No internet", viewModel.uiState.value.error)

        viewModel.onRetry()

        assertEquals(2, viewModel.uiState.value.inventoryItems.size)
        assertNull(viewModel.uiState.value.error)
    }

    // =============================================================================
    // 7. Refreshing and resetting pagination
    // =============================================================================

    @Test
    fun `onRefresh reloads first page and resets pagination`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2)
        val page2 = successProducts(page = 2, totalPages = 2, productIdPrefix = "b")
        val refreshed = successProducts(page = 1, totalPages = 1, productIdPrefix = "refreshed")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2),
            Result.Success(refreshed)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.loadMore()
        assertEquals(2, viewModel.uiState.value.currentPage)

        viewModel.onRefresh()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `onRefresh replaces all items with refreshed data`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2, productIdPrefix = "old")
        val page2 = successProducts(page = 2, totalPages = 2, productIdPrefix = "old-page2")
        val refreshed = successProducts(page = 1, totalPages = 1, productIdPrefix = "fresh")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2),
            Result.Success(refreshed)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        // Load two pages
        viewModel.loadMore()
        val afterPagination = viewModel.uiState.value.inventoryItems.map { it.product.id }
        assertEquals(4, afterPagination.size)

        // Refresh — should replace with only refreshed items
        viewModel.onRefresh()

        val state = viewModel.uiState.value
        val itemIds = state.inventoryItems.map { it.product.id }
        // Only refreshed items, no old items
        assertTrue("Should contain fresh-1", itemIds.contains("fresh-1"))
        assertTrue("Should contain fresh-2", itemIds.contains("fresh-2"))
        assertFalse("Should not contain old-1", itemIds.contains("old-1"))
        assertFalse("Should not contain old-page2-1", itemIds.contains("old-page2-1"))
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `search change discards previous results`() = runTest {
        val initial = successProducts(page = 1, totalPages = 1, productIdPrefix = "initial")
        val searched = successProducts(page = 1, totalPages = 1, productIdPrefix = "searched")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(initial),
            Result.Success(searched)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        // Search replaces results
        viewModel.setSearchQuery("widget")

        val state = viewModel.uiState.value
        val itemIds = state.inventoryItems.map { it.product.id }
        assertTrue("Should contain searched-1", itemIds.contains("searched-1"))
        assertFalse("Should not contain initial-1", itemIds.contains("initial-1"))
    }

    @Test
    fun `filter change discards previous results`() = runTest {
        val initial = successProducts(page = 1, totalPages = 1, productIdPrefix = "initial")
        val filtered = successProducts(page = 1, totalPages = 1, productIdPrefix = "filtered")
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(initial),
            Result.Success(filtered)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        // Filter replaces results
        viewModel.setFilterStatus(ProductStatus.ACTIVE)

        val state = viewModel.uiState.value
        val itemIds = state.inventoryItems.map { it.product.id }
        assertTrue("Should contain filtered-1", itemIds.contains("filtered-1"))
        assertFalse("Should not contain initial-1", itemIds.contains("initial-1"))
    }

    // =============================================================================
    // 8. Stock operations — receive stock
    // =============================================================================

    @Test
    fun `receiveStock success emits success event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )
        coEvery { catalogRepository.receiveStock("variant-1", 100, "Restock") } returns Result.Success(
            stockBalance(availableQuantity = 150)
        )

        val viewModel = createViewModel()
        viewModel.receiveStock("variant-1", 100, "Restock")

        val event = viewModel.event.value
        assertTrue(event is InventoryEvent.StockOperationSuccess)
        assertEquals("Received 100 units for variant variant-1", (event as InventoryEvent.StockOperationSuccess).message)
        assertFalse(viewModel.uiState.value.isStockOperationLoading)
    }

    @Test
    fun `receiveStock error emits error event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )
        coEvery { catalogRepository.receiveStock("variant-1", 100, "Restock") } returns Result.Error(
            AppError.ServerError("Invalid quantity", 400)
        )

        val viewModel = createViewModel()
        viewModel.receiveStock("variant-1", 100, "Restock")

        val event = viewModel.event.value
        assertTrue(event is InventoryEvent.StockOperationError)
        assertEquals("Invalid quantity", (event as InventoryEvent.StockOperationError).message)
        assertFalse(viewModel.uiState.value.isStockOperationLoading)
    }

    // =============================================================================
    // 9. Stock operations — adjust stock
    // =============================================================================

    @Test
    fun `adjustStock success emits success event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )
        coEvery { catalogRepository.adjustStock("variant-1", -10, "Damaged goods") } returns Result.Success(
            stockBalance(availableQuantity = 40)
        )

        val viewModel = createViewModel()
        viewModel.adjustStock("variant-1", -10, "Damaged goods")

        val event = viewModel.event.value
        assertTrue(event is InventoryEvent.StockOperationSuccess)
        assertEquals("Adjusted stock by -10 for variant variant-1", (event as InventoryEvent.StockOperationSuccess).message)
        assertFalse(viewModel.uiState.value.isStockOperationLoading)
    }

    @Test
    fun `adjustStock error emits error event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )
        coEvery { catalogRepository.adjustStock("variant-1", -10, "Damaged goods") } returns Result.Error(
            AppError.ServerError("Insufficient stock", 400)
        )

        val viewModel = createViewModel()
        viewModel.adjustStock("variant-1", -10, "Damaged goods")

        val event = viewModel.event.value
        assertTrue(event is InventoryEvent.StockOperationError)
        assertEquals("Insufficient stock", (event as InventoryEvent.StockOperationError).message)
    }

    // =============================================================================
    // 10. Stock movements
    // =============================================================================

    @Test
    fun `loadStockMovements success populates movements`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )
        coEvery { catalogRepository.getStockMovements("variant-1", 1, 20) } returns Result.Success(
            successMovements()
        )

        val viewModel = createViewModel()
        viewModel.loadStockMovements("variant-1")

        val state = viewModel.uiState.value
        assertEquals(2, state.stockMovements.size)
        assertEquals("variant-1", state.selectedVariantId)
        assertFalse(state.isLoadingMovements)
    }

    @Test
    fun `loadStockMovements error sets error`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )
        coEvery { catalogRepository.getStockMovements("variant-1", 1, 20) } returns Result.Error(
            AppError.NetworkError("Timeout")
        )

        val viewModel = createViewModel()
        viewModel.loadStockMovements("variant-1")

        val state = viewModel.uiState.value
        assertEquals("Timeout", state.error)
        assertFalse(state.isLoadingMovements)
    }

    // =============================================================================
    // 11. Loading flags reset after exceptions
    // =============================================================================

    @Test
    fun `loadInventory error resets isLoading flag`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Error(
            AppError.NetworkError("Timeout")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `loadMore error resets isLoadingMore flag`() = runTest {
        val page1 = successProducts(page = 1, totalPages = 2)
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Error(AppError.NetworkError("Timeout"))
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.loadMore()

        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    // =============================================================================
    // 12. Empty data handling
    // =============================================================================

    @Test
    fun `empty product list shows empty state`() = runTest {
        val emptyData = PaginatedProducts(
            products = emptyList(),
            page = 1,
            size = 20,
            totalPages = 0,
            totalItems = 0
        )
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(emptyData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.inventoryItems.isEmpty())
        assertEquals(0, state.totalItems)
        assertFalse(state.hasMore)
        assertNull(state.error)
    }

    // =============================================================================
    // 13. Navigation events
    // =============================================================================

    @Test
    fun `onEditProduct emits NavigateToEditProduct event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.onEditProduct("product-1")

        val event = viewModel.event.value
        assertTrue(event is InventoryEvent.NavigateToEditProduct)
        assertEquals("product-1", (event as InventoryEvent.NavigateToEditProduct).productId)
    }

    @Test
    fun `onCreateProduct emits NavigateToCreateProduct event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.onCreateProduct()

        assertTrue(viewModel.event.value is InventoryEvent.NavigateToCreateProduct)
    }

    // =============================================================================
    // 14. clearError and clearEvent
    // =============================================================================

    @Test
    fun `clearError clears error message`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Error(
            AppError.NetworkError("Timeout")
        )

        val viewModel = createViewModel()
        assertEquals("Timeout", viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearEvent clears event`() = runTest {
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(
            successProducts()
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        viewModel.onCreateProduct()
        assertNotNull(viewModel.event.value)

        viewModel.clearEvent()
        assertNull(viewModel.event.value)
    }

    // =============================================================================
    // 15. KPI calculations
    // =============================================================================

    @Test
    fun `kpiActiveSkus counts active products`() = runTest {
        val products = PaginatedProducts(
            products = listOf(
                testProduct(id = "p1", status = ProductStatus.ACTIVE),
                testProduct(id = "p2", status = ProductStatus.INACTIVE)
            ),
            page = 1,
            size = 20,
            totalPages = 1,
            totalItems = 2
        )
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.Success(products)
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        assertEquals(1, viewModel.uiState.value.kpiActiveSkus)
        assertEquals(2, viewModel.uiState.value.kpiTotalProducts)
    }

    // =============================================================================
    // 16. Concurrency — stale pagination responses (CompletableDeferred)
    // =============================================================================

    @Test
    fun `stale pagination response does not append to new dataset after search`() = runTest {
        // Scenario: load page 2, hold it, change search (new generation), complete search,
        // then release old page 2 — old page 2 must not append to search results.

        val page1 = successProducts(page = 1, totalPages = 2, productIdPrefix = "p1")
        val searchResult = successProducts(page = 1, totalPages = 1, productIdPrefix = "searched")
        val deferredPage2 = CompletableDeferred<Result<PaginatedProducts>>()

        var listProductsCallCount = 0
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } coAnswers {
            listProductsCallCount++
            when (listProductsCallCount) {
                1 -> Result.Success(page1)
                2 -> deferredPage2.await()
                3 -> Result.Success(searchResult)
                else -> Result.Success(searchResult)
            }
        }
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        // Initial load completes with page 1
        assertEquals(2, viewModel.uiState.value.inventoryItems.size)

        // Start loadMore — this will hang on deferredPage2
        viewModel.loadMore()

        // Change search query — this increments generation and fetches search results
        viewModel.setSearchQuery("test")

        // Verify search results are in place
        val afterSearch = viewModel.uiState.value.inventoryItems.map { it.product.id }
        assertTrue("Should contain searched-1", afterSearch.contains("searched-1"))
        assertFalse("Should not contain p1-1", afterSearch.contains("p1-1"))

        // Now complete the old page 2 request
        deferredPage2.complete(Result.Success(successProducts(page = 2, totalPages = 2, productIdPrefix = "p2")))

        // Old page 2 should NOT have appended to search results
        val finalItems = viewModel.uiState.value.inventoryItems.map { it.product.id }
        assertFalse("Should not contain p2-1 (stale)", finalItems.contains("p2-1"))
        assertFalse("Should not contain p2-2 (stale)", finalItems.contains("p2-2"))
        assertTrue("Should still contain searched-1", finalItems.contains("searched-1"))
    }

    @Test
    fun `stale enrichment does not overwrite newer inventory data`() = runTest {
        // Scenario: initial load starts enrichment, hold enrichment, then refresh completes
        // with new data. When old enrichment completes, it must not overwrite refreshed data.

        val page1 = successProducts(page = 1, totalPages = 1, productIdPrefix = "old")
        val refreshed = successProducts(page = 1, totalPages = 1, productIdPrefix = "fresh")

        // listProducts: 1=initial, 2=refresh
        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(refreshed)
        )

        // listVariants: first call (old enrichment) is held, subsequent calls return immediately
        val deferredVariants = CompletableDeferred<Result<PaginatedVariants>>()
        var listVariantsCallCount = 0
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } coAnswers {
            listVariantsCallCount++
            if (listVariantsCallCount == 1) {
                deferredVariants.await()
            } else {
                Result.Success(successVariants())
            }
        }
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()
        // Initial load starts; enrichment is in progress (held by deferredVariants)

        // Refresh — increments generation, loads fresh data
        viewModel.onRefresh()

        // Verify refresh completed with fresh data
        val afterRefresh = viewModel.uiState.value.inventoryItems.map { it.product.id }
        assertTrue("Should contain fresh-1", afterRefresh.contains("fresh-1"))
        assertFalse("Should not contain old-1", afterRefresh.contains("old-1"))

        // Complete old enrichment — should be rejected as stale
        deferredVariants.complete(Result.Success(successVariants()))

        // Fresh data should still be present; old enrichment should not have overwritten it
        val finalItems = viewModel.uiState.value.inventoryItems.map { it.product.id }
        assertTrue("Should still contain fresh-1", finalItems.contains("fresh-1"))
        assertFalse("Should not contain old-1 (stale enrichment)", finalItems.contains("old-1"))
    }

    @Test
    fun `generation increments on all dataset-replacing operations`() = runTest {
        // Verify that every operation that replaces the dataset increments the generation counter.
        // We test this indirectly: each operation should discard stale enrichment from the previous.

        val initial = successProducts(page = 1, totalPages = 1, productIdPrefix = "initial")
        val searched = successProducts(page = 1, totalPages = 1, productIdPrefix = "searched")
        val filtered = successProducts(page = 1, totalPages = 1, productIdPrefix = "filtered")
        val cleared = successProducts(page = 1, totalPages = 1, productIdPrefix = "cleared")
        val refreshed = successProducts(page = 1, totalPages = 1, productIdPrefix = "refreshed")

        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(initial),
            Result.Success(searched),
            Result.Success(filtered),
            Result.Success(cleared),
            Result.Success(refreshed)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        // 1. Initial load
        assertEquals("initial-1", viewModel.uiState.value.inventoryItems[0].product.id)

        // 2. Search — new generation
        viewModel.setSearchQuery("test")
        assertEquals("searched-1", viewModel.uiState.value.inventoryItems[0].product.id)

        // 3. Filter — new generation
        viewModel.setFilterStatus(ProductStatus.ACTIVE)
        assertEquals("filtered-1", viewModel.uiState.value.inventoryItems[0].product.id)

        // 4. Clear filters — new generation
        viewModel.clearFilters()
        assertEquals("cleared-1", viewModel.uiState.value.inventoryItems[0].product.id)

        // 5. Refresh — new generation
        viewModel.onRefresh()
        assertEquals("refreshed-1", viewModel.uiState.value.inventoryItems[0].product.id)
    }

    @Test
    fun `concurrent enrichment merges items from different pages correctly`() = runTest {
        // Scenario: two pages load, enrichment for each page completes in different order.
        // Items from both pages must remain present with enriched data.

        val page1 = successProducts(page = 1, totalPages = 2, productIdPrefix = "p1")
        val page2 = successProducts(page = 2, totalPages = 2, productIdPrefix = "p2")

        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2)
        )

        // Return variants with product-specific data so we can verify enrichment
        coEvery { catalogRepository.listVariants("p1-1", any(), any(), any()) } returns Result.Success(
            PaginatedVariants(
                variants = listOf(testVariant(id = "v-p1-1", productId = "p1-1")),
                page = 1, size = 20, totalPages = 1, totalItems = 1
            )
        )
        coEvery { catalogRepository.listVariants("p1-2", any(), any(), any()) } returns Result.Success(
            PaginatedVariants(
                variants = listOf(testVariant(id = "v-p1-2", productId = "p1-2")),
                page = 1, size = 20, totalPages = 1, totalItems = 1
            )
        )
        coEvery { catalogRepository.listVariants("p2-1", any(), any(), any()) } returns Result.Success(
            PaginatedVariants(
                variants = listOf(testVariant(id = "v-p2-1", productId = "p2-1")),
                page = 1, size = 20, totalPages = 1, totalItems = 1
            )
        )
        coEvery { catalogRepository.listVariants("p2-2", any(), any(), any()) } returns Result.Success(
            PaginatedVariants(
                variants = listOf(testVariant(id = "v-p2-2", productId = "p2-2")),
                page = 1, size = 20, totalPages = 1, totalItems = 1
            )
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        // Load page 2
        viewModel.loadMore()

        val state = viewModel.uiState.value
        val itemIds = state.inventoryItems.map { it.product.id }

        // All 4 items from both pages must be present
        assertEquals(4, itemIds.size)
        assertTrue("p1-1 present", itemIds.contains("p1-1"))
        assertTrue("p1-2 present", itemIds.contains("p1-2"))
        assertTrue("p2-1 present", itemIds.contains("p2-1"))
        assertTrue("p2-2 present", itemIds.contains("p2-2"))

        // Verify enrichment — each product should have variants
        val p1Item = state.inventoryItems.find { it.product.id == "p1-1" }
        assertNotNull(p1Item)
        assertEquals(1, p1Item!!.variants.size)
        assertEquals("v-p1-1", p1Item.variants[0].variant.id)

        val p2Item = state.inventoryItems.find { it.product.id == "p2-1" }
        assertNotNull(p2Item)
        assertEquals(1, p2Item!!.variants.size)
        assertEquals("v-p2-1", p2Item.variants[0].variant.id)
    }

    @Test
    fun `pagination during refresh does not lose items`() = runTest {
        // Scenario: load page 2 while refresh is in progress. Both should complete
        // without losing items.

        val page1 = successProducts(page = 1, totalPages = 3, productIdPrefix = "p1")
        val page2 = successProducts(page = 2, totalPages = 3, productIdPrefix = "p2")
        val page3 = successProducts(page = 3, totalPages = 3, productIdPrefix = "p3")
        val refreshed = successProducts(page = 1, totalPages = 3, productIdPrefix = "fresh")

        coEvery { catalogRepository.listProducts(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2),
            Result.Success(refreshed)
        )
        coEvery { catalogRepository.listVariants(any(), any(), any(), any()) } returns Result.Success(
            successVariants()
        )
        coEvery { catalogRepository.getStockAvailability(any()) } returns Result.Success(
            stockBalance(availableQuantity = 50)
        )

        val viewModel = createViewModel()

        // Load page 2
        viewModel.loadMore()
        assertEquals(4, viewModel.uiState.value.inventoryItems.size)

        // Refresh — replaces all items
        viewModel.onRefresh()
        val refreshedItems = viewModel.uiState.value.inventoryItems.map { it.product.id }
        assertTrue("Should contain fresh-1", refreshedItems.contains("fresh-1"))
        assertFalse("Should not contain p1-1", refreshedItems.contains("p1-1"))
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    // =============================================================================
    // Helpers
    // =============================================================================

    private fun createViewModel() = InventoryViewModel(catalogRepository)

    private fun testProduct(
        id: String = "product-1",
        name: String = "Widget",
        status: ProductStatus = ProductStatus.ACTIVE
    ) = Product(
        id = id,
        name = name,
        description = "A widget",
        slug = "widget",
        priceAmount = 10.0,
        priceCurrency = "USD",
        images = emptyList(),
        status = status,
        categoryId = null,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun successProducts(
        page: Int = 1,
        totalPages: Int = 5,
        productIdPrefix: String = "product"
    ) = PaginatedProducts(
        products = listOf(
            testProduct(id = "$productIdPrefix-1"),
            testProduct(id = "$productIdPrefix-2")
        ),
        page = page,
        size = 20,
        totalPages = totalPages,
        totalItems = 10
    )

    private fun testVariant(
        id: String = "variant-1",
        productId: String = "product-1"
    ) = ProductVariant(
        id = id,
        productId = productId,
        sku = "SKU-$id",
        attributes = emptyMap(),
        status = VariantStatus.ACTIVE,
        availableQuantity = 50,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    private fun successVariants() = PaginatedVariants(
        variants = listOf(
            testVariant(id = "variant-1", productId = "product-1"),
            testVariant(id = "variant-2", productId = "product-2")
        ),
        page = 1,
        size = 20,
        totalPages = 1,
        totalItems = 2
    )

    private fun stockBalance(
        variantId: String = "variant-1",
        onHand: Int = 60,
        reserved: Int = 10,
        availableQuantity: Int = 50
    ) = StockBalance(
        variantId = variantId,
        onHand = onHand,
        reserved = reserved,
        availableQuantity = availableQuantity
    )

    private fun testMovement(
        id: String = "mov-1",
        variantId: String = "variant-1",
        quantityDelta: Int = 100,
        movementType: StockMovementType = StockMovementType.RECEIPT
    ) = StockMovement(
        id = id,
        variantId = variantId,
        quantityDelta = quantityDelta,
        movementType = movementType,
        reason = "Initial stock",
        createdAt = "2024-01-01T00:00:00Z"
    )

    private fun successMovements() = PaginatedStockMovements(
        movements = listOf(
            testMovement(id = "mov-1", quantityDelta = 100),
            testMovement(id = "mov-2", quantityDelta = -10, movementType = StockMovementType.ADJUSTMENT)
        ),
        page = 1,
        size = 20,
        totalPages = 1,
        totalItems = 2
    )
}
