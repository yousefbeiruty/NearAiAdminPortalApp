package com.nearai.admin.feature.orders.presentation

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderRepository
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary
import com.nearai.admin.feature.orders.domain.PaginatedOrders
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersListViewModelTest {

    @RelaxedMockK
    private lateinit var orderRepository: OrderRepository

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

    @Test
    fun `initial state loads orders on init`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.orders.size)
        assertNull(state.error)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `loadOrders success populates orders`() = runTest {
        val data = successData()
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.orders.size)
        assertEquals(10, state.totalItems)
        assertEquals(5, state.totalPages)
        assertTrue(state.hasMore)
        assertNull(state.error)
    }

    @Test
    fun `loadOrders error sets error message`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returns Result.Error(
            AppError.NetworkError("No internet connection")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.orders.isEmpty())
        assertEquals("No internet connection", state.error)
    }

    @Test
    fun `onRetry reloads orders after error`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returnsMany listOf(
            Result.Error(AppError.NetworkError("No internet")),
            Result.Success(successData())
        )

        val viewModel = createViewModel()

        // First load fails
        assertEquals("No internet", viewModel.uiState.value.error)

        // Retry succeeds
        viewModel.onRetry()
        assertEquals(2, viewModel.uiState.value.orders.size)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `setFilterOrderStatus updates filter and reloads`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successData()),
            Result.Success(successData(status = OrderStatus.PENDING))
        )

        val viewModel = createViewModel()
        viewModel.setFilterOrderStatus(OrderStatus.PENDING)

        assertEquals(OrderStatus.PENDING, viewModel.uiState.value.filterOrderStatus)
        coVerify { orderRepository.listOrders(1, 20, OrderStatus.PENDING, null) }
    }

    @Test
    fun `setFilterFulfillmentStatus updates filter and reloads`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successData()),
            Result.Success(successData(fulfillmentStatus = FulfillmentStatus.DELIVERED))
        )

        val viewModel = createViewModel()
        viewModel.setFilterFulfillmentStatus(FulfillmentStatus.DELIVERED)

        assertEquals(FulfillmentStatus.DELIVERED, viewModel.uiState.value.filterFulfillmentStatus)
        coVerify { orderRepository.listOrders(1, 20, null, FulfillmentStatus.DELIVERED) }
    }

    @Test
    fun `clearFilters resets filters and reloads`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(successData()),
            Result.Success(successData(status = OrderStatus.PENDING)),
            Result.Success(successData())
        )

        val viewModel = createViewModel()
        viewModel.setFilterOrderStatus(OrderStatus.PENDING)
        viewModel.setFilterFulfillmentStatus(FulfillmentStatus.DELIVERED)
        viewModel.clearFilters()

        assertNull(viewModel.uiState.value.filterOrderStatus)
        assertNull(viewModel.uiState.value.filterFulfillmentStatus)
    }

    @Test
    fun `loadMore appends orders`() = runTest {
        val page1 = successData(page = 1, totalPages = 2)
        val page2 = successData(page = 2, totalPages = 2, orderIdPrefix = "b")
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(page1),
            Result.Success(page2)
        )

        val viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.orders.size)

        viewModel.loadMore()
        val state = viewModel.uiState.value
        assertEquals(4, state.orders.size)
        assertEquals(2, state.currentPage)
        assertFalse(state.hasMore)
    }

    @Test
    fun `onOrderClicked emits NavigateToOrder event`() = runTest {
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returns successResult()

        val viewModel = createViewModel()

        viewModel.onOrderClicked("order-123")
        val event = viewModel.event.value
        assertTrue(event is OrdersListEvent.NavigateToOrder)
        assertEquals("order-123", (event as OrdersListEvent.NavigateToOrder).orderId)
    }

    @Test
    fun `empty orders list shows empty state`() = runTest {
        val emptyData = PaginatedOrders(
            orders = emptyList(),
            page = 1,
            size = 20,
            totalPages = 0,
            totalItems = 0
        )
        coEvery { orderRepository.listOrders(any(), any(), any(), any()) } returns Result.Success(emptyData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.orders.isEmpty())
        assertEquals(0, state.totalItems)
        assertFalse(state.hasMore)
        assertNull(state.error)
    }

    // =============================================================================
    // Helpers
    // =============================================================================

    private fun createViewModel() = OrdersListViewModel(orderRepository)

    private fun successResult() = Result.Success(successData())

    private fun successData(
        page: Int = 1,
        totalPages: Int = 5,
        orderIdPrefix: String = "a",
        status: OrderStatus = OrderStatus.PENDING,
        fulfillmentStatus: FulfillmentStatus = FulfillmentStatus.AWAITING_CONFIRMATION
    ) = PaginatedOrders(
        orders = listOf(
            OrderSummary(
                id = "${orderIdPrefix}bc12345-def0-1234-5678-9abcdef01234",
                createdAt = "2024-01-15T10:30:00Z",
                status = status,
                fulfillmentStatus = fulfillmentStatus,
                totalAmount = 45.50,
                subtotalAmount = 40.00,
                currency = "USD",
                items = emptyList(),
                deliveryRecipientName = "John Doe",
                deliveryPhone = "+1234567890",
                deliveryCity = null,
                deliveryArea = null,
                deliveryStreet = null,
                deliveryBuildingNumber = null,
                deliveryApartment = null,
                deliveryInstructions = null
            ),
            OrderSummary(
                id = "${orderIdPrefix}bc67890-abc1-5678-9012-34567890abcd",
                createdAt = "2024-01-14T08:15:00Z",
                status = status,
                fulfillmentStatus = fulfillmentStatus,
                totalAmount = 120.00,
                subtotalAmount = 110.00,
                currency = "USD",
                items = emptyList(),
                deliveryRecipientName = "Jane Smith",
                deliveryPhone = "+0987654321",
                deliveryCity = null,
                deliveryArea = null,
                deliveryStreet = null,
                deliveryBuildingNumber = null,
                deliveryApartment = null,
                deliveryInstructions = null
            )
        ),
        page = page,
        size = 20,
        totalPages = totalPages,
        totalItems = 10
    )
}
