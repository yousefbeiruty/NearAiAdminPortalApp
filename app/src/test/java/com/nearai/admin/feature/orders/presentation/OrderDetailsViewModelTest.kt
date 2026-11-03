package com.nearai.admin.feature.orders.presentation

import androidx.lifecycle.SavedStateHandle
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderDetails
import com.nearai.admin.feature.orders.domain.OrderItem
import com.nearai.admin.feature.orders.domain.OrderRepository
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.StatusHistoryEntry
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
class OrderDetailsViewModelTest {

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
    fun `initial state loads order and history`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals(2, state.statusHistory.size)
        assertNull(state.error)
    }

    @Test
    fun `loadOrder error sets error message`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Error(
            AppError.NotFoundError("Order not found")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.order)
        assertEquals("Order not found", state.error)
    }

    @Test
    fun `history error does not block main content`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Error(
            AppError.NetworkError("No internet")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNotNull(state.order)
        assertTrue(state.statusHistory.isEmpty())
        assertNull(state.error) // order loaded, history error is non-fatal
    }

    @Test
    fun `confirmOrder success reloads order`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.confirmOrder("order-123") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.confirmOrder()

        coVerify { orderRepository.confirmOrder("order-123") }
        assertFalse(viewModel.uiState.value.isActionLoading)
        assertNull(viewModel.uiState.value.actionError)
    }

    @Test
    fun `confirmOrder failure shows action error`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.confirmOrder("order-123") } returns Result.Error(
            AppError.ServerError("Invalid transition", 422)
        )

        val viewModel = createViewModel()
        viewModel.confirmOrder()

        assertEquals("Invalid transition", viewModel.uiState.value.actionError)
        assertFalse(viewModel.uiState.value.isActionLoading)
    }

    @Test
    fun `rejectOrder with empty reason shows validation error`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()
        viewModel.rejectOrder()

        assertEquals("Rejection reason is required", viewModel.uiState.value.actionError)
        coVerify(exactly = 0) { orderRepository.rejectOrder(any(), any()) }
    }

    @Test
    fun `rejectOrder with reason succeeds`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.rejectOrder("order-123", "Wrong item") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onRejectReasonChanged("Wrong item")
        viewModel.rejectOrder()

        coVerify { orderRepository.rejectOrder("order-123", "Wrong item") }
        assertFalse(viewModel.uiState.value.isActionLoading)
        assertNull(viewModel.uiState.value.actionError)
    }

    @Test
    fun `rejectOrder prevents duplicate submission`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.rejectOrder("order-123", any()) } returns Result.Error(
            AppError.ServerError("Error", 500)
        )

        val viewModel = createViewModel()
        viewModel.onRejectReasonChanged("Reason")
        viewModel.uiState.value.let { it.copy(isActionLoading = true) }
        viewModel.rejectOrder()

        coVerify(exactly = 1) { orderRepository.rejectOrder("order-123", "Reason") }
    }

    @Test
    fun `processOrder success reloads order`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.processOrder("order-123") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.processOrder()

        coVerify { orderRepository.processOrder("order-123") }
        assertFalse(viewModel.uiState.value.isActionLoading)
    }

    @Test
    fun `dispatchOrder success reloads order`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.dispatchOrder("order-123") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.dispatchOrder()

        coVerify { orderRepository.dispatchOrder("order-123") }
        assertFalse(viewModel.uiState.value.isActionLoading)
    }

    @Test
    fun `deliverOrder success reloads order`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.deliverOrder("order-123") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.deliverOrder()

        coVerify { orderRepository.deliverOrder("order-123") }
        assertFalse(viewModel.uiState.value.isActionLoading)
    }

    @Test
    fun `showRejectDialog opens dialog`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()
        viewModel.showRejectDialog()

        assertTrue(viewModel.uiState.value.showRejectDialog)
        assertEquals("", viewModel.uiState.value.rejectReason)
    }

    @Test
    fun `dismissRejectDialog closes dialog`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()
        viewModel.showRejectDialog()
        viewModel.onRejectReasonChanged("Some reason")
        viewModel.dismissRejectDialog()

        assertFalse(viewModel.uiState.value.showRejectDialog)
        assertEquals("", viewModel.uiState.value.rejectReason)
    }

    @Test
    fun `onRetry reloads order after error`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returnsMany listOf(
            Result.Error(AppError.NetworkError("No internet")),
            Result.Success(orderDetails())
        )
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()
        assertEquals("No internet", viewModel.uiState.value.error)

        viewModel.onRetry()
        assertNull(viewModel.uiState.value.error)
        assertNotNull(viewModel.uiState.value.order)
    }

    @Test
    fun `executeConfirmedAction executes correct action`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())
        coEvery { orderRepository.processOrder("order-123") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.showConfirmDialog("Process")
        assertEquals("Process", viewModel.uiState.value.showConfirmDialog)

        viewModel.executeConfirmedAction()
        assertNull(viewModel.uiState.value.showConfirmDialog)
        coVerify { orderRepository.processOrder("order-123") }
    }

    @Test
    fun `order details contain correct items`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()

        val order = viewModel.uiState.value.order
        assertNotNull(order)
        assertEquals(2, order!!.items.size)
        assertEquals("Widget A", order.items[0].productName)
        assertEquals(3, order.items[0].quantity)
        assertEquals(29.99, order.items[0].unitPriceAmount, 0.001)
        assertEquals("USD", order.currency)
    }

    @Test
    fun `status history entries are ordered`() = runTest {
        coEvery { orderRepository.getOrderDetails("order-123") } returns Result.Success(orderDetails())
        coEvery { orderRepository.getStatusHistory("order-123") } returns Result.Success(statusHistory())

        val viewModel = createViewModel()

        val history = viewModel.uiState.value.statusHistory
        assertEquals(2, history.size)
        assertEquals("order", history[0].statusType)
        assertEquals("fulfillment", history[1].statusType)
    }

    // =============================================================================
    // Helpers
    // =============================================================================

    private fun createViewModel() = OrderDetailsViewModel(
        orderRepository = orderRepository,
        savedStateHandle = SavedStateHandle(mapOf("orderId" to "order-123"))
    )

    private fun orderDetails() = OrderDetails(
        id = "order-123",
        createdAt = "2024-01-15T10:30:00Z",
        status = OrderStatus.PENDING,
        fulfillmentStatus = FulfillmentStatus.AWAITING_CONFIRMATION,
        items = listOf(
            OrderItem("var-1", "Widget A", "W-001", 3, 29.99, "USD", 89.97),
            OrderItem("var-2", "Widget B", "W-002", 1, 15.00, "USD", 15.00)
        ),
        subtotalAmount = 104.97,
        totalAmount = 104.97,
        currency = "USD",
        deliveryRecipientName = "John Doe",
        deliveryPhone = "+1234567890",
        deliveryCity = "New York",
        deliveryArea = "Manhattan",
        deliveryStreet = "123 Main St",
        deliveryBuildingNumber = "4A",
        deliveryApartment = "Apt 5",
        deliveryInstructions = "Leave at door"
    )

    private fun statusHistory() = listOf(
        StatusHistoryEntry(null, "pending", "order", "customer", null, "2024-01-15T10:30:00Z"),
        StatusHistoryEntry(null, "awaiting_confirmation", "fulfillment", "system", null, "2024-01-15T10:31:00Z")
    )
}
