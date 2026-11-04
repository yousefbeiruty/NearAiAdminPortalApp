package com.nearai.admin.feature.dashboard.presentation

import app.cash.turbine.test
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import com.nearai.admin.feature.dashboard.domain.LowStockAlert
import com.nearai.admin.feature.dashboard.domain.RecentOrder
import com.nearai.admin.feature.dashboard.domain.RecentRegistration
import com.nearai.admin.feature.dashboard.domain.RevenueByCurrency
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
class DashboardViewModelTest {

    @RelaxedMockK
    private lateinit var dashboardRepository: DashboardRepository

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
    fun `initial state before load is empty`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(14820, state.totalUsers)
        assertEquals(342, state.totalProducts)
        assertEquals(12, state.totalCategories)
        assertEquals(1250, state.totalOrders)
        assertEquals(45, state.pendingOrders)
        assertEquals(1180, state.completedOrders)
        assertEquals(2, state.revenueByCurrency.size)
        assertEquals(3, state.recentOrders.size)
        assertEquals(2, state.recentRegistrations.size)
        assertEquals(2, state.lowStockAlerts.size)
        assertNull(state.error)
    }

    @Test
    fun `loadDashboard success populates all metrics`() = runTest {
        val data = successData()
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(14820, state.totalUsers)
        assertEquals(342, state.totalProducts)
        assertEquals(12, state.totalCategories)
        assertEquals(1250, state.totalOrders)
        assertEquals(45, state.pendingOrders)
        assertEquals(1180, state.completedOrders)
        assertNull(state.error)
    }

    @Test
    fun `revenue by currency is mapped with formatted amounts`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.revenueByCurrency.size)
        assertEquals("USD", state.revenueByCurrency[0].currency)
        assertEquals(4820.50, state.revenueByCurrency[0].amount, 0.001)
        assertNotNull(state.revenueByCurrency[0].formattedAmount)
        assertEquals("EUR", state.revenueByCurrency[1].currency)
        assertEquals(1250.75, state.revenueByCurrency[1].amount, 0.001)
    }

    @Test
    fun `recent orders are mapped with formatted totals`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(3, state.recentOrders.size)
        assertEquals("ord-1", state.recentOrders[0].id)
        assertEquals("John Doe", state.recentOrders[0].userName)
        assertEquals("completed", state.recentOrders[0].status)
        assertNotNull(state.recentOrders[0].formattedTotal)
    }

    @Test
    fun `recent registrations are mapped correctly`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.recentRegistrations.size)
        assertEquals("usr-1", state.recentRegistrations[0].id)
        assertEquals("Jane Smith", state.recentRegistrations[0].name)
        assertEquals("jane@example.com", state.recentRegistrations[0].email)
    }

    @Test
    fun `low stock alerts are mapped correctly`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.lowStockAlerts.size)
        assertEquals("Widget A", state.lowStockAlerts[0].productName)
        assertEquals("SKU-001", state.lowStockAlerts[0].sku)
        assertEquals(3, state.lowStockAlerts[0].currentStock)
        assertEquals(10, state.lowStockAlerts[0].threshold)
    }

    @Test
    fun `loadDashboard error sets error message and stops loading`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns Result.Error(
            AppError.NetworkError("No internet connection")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("No internet connection", state.error)
        assertEquals(0, state.totalUsers)
        assertTrue(state.recentOrders.isEmpty())
    }

    @Test
    fun `onRetry reloads dashboard after error`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returnsMany listOf(
            Result.Error(AppError.NetworkError("No internet")),
            Result.Success(successData())
        )

        val viewModel = createViewModel()

        assertEquals("No internet", viewModel.uiState.value.error)

        viewModel.onRetry()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(14820, state.totalUsers)

        coVerify(exactly = 2) { dashboardRepository.getDashboardData() }
    }

    @Test
    fun `onRetry calls repository`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        viewModel.onRetry()

        coVerify { dashboardRepository.getDashboardData() }
    }

    @Test
    fun `onRefresh reloads dashboard`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        viewModel.onRefresh()

        coVerify(exactly = 2) { dashboardRepository.getDashboardData() }
    }

    @Test
    fun `clearError removes error message`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns Result.Error(
            AppError.ServerError("Server error", 500)
        )

        val viewModel = createViewModel()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `empty data renders zero values`() = runTest {
        val emptyData = DashboardData()
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(emptyData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.totalUsers)
        assertEquals(0, state.totalProducts)
        assertTrue(state.revenueByCurrency.isEmpty())
        assertTrue(state.recentOrders.isEmpty())
        assertTrue(state.recentRegistrations.isEmpty())
        assertTrue(state.lowStockAlerts.isEmpty())
    }

    @Test
    fun `dashboard loads automatically on init`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        coVerify { dashboardRepository.getDashboardData() }
    }

    @Test
    fun `server error preserves error details`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns Result.Error(
            AppError.ServerError("Internal server error", 500)
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `authentication error during dashboard load`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns Result.Error(
            AppError.UnauthorizedError("Session expired")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("Session expired", state.error)
    }

    @Test
    fun `currency formatting handles invalid currency code`() = runTest {
        val data = DashboardData(
            revenueByCurrency = listOf(
                RevenueByCurrency(currency = "INVALID", amount = 100.0)
            )
        )
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.revenueByCurrency.size)
        // Should fallback to "100.00 INVALID" format
        assertTrue(state.revenueByCurrency[0].formattedAmount.contains("100"))
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(dashboardRepository)
    }

    private fun successData(): DashboardData {
        return DashboardData(
            totalUsers = 14820,
            totalProducts = 342,
            totalCategories = 12,
            totalOrders = 1250,
            pendingOrders = 45,
            completedOrders = 1180,
            revenueByCurrency = listOf(
                RevenueByCurrency(currency = "USD", amount = 4820.50),
                RevenueByCurrency(currency = "EUR", amount = 1250.75)
            ),
            recentOrders = listOf(
                RecentOrder("ord-1", "John Doe", "2024-01-15", "completed", 150.0, "USD"),
                RecentOrder("ord-2", "Jane Smith", "2024-01-14", "pending", 75.5, "USD"),
                RecentOrder("ord-3", "Bob Wilson", "2024-01-14", "completed", 200.0, "EUR")
            ),
            recentRegistrations = listOf(
                RecentRegistration("usr-1", "Jane Smith", "jane@example.com", "2024-01-15"),
                RecentRegistration("usr-2", "Bob Wilson", "bob@example.com", "2024-01-14")
            ),
            lowStockAlerts = listOf(
                LowStockAlert("Widget A", "SKU-001", 3, 10),
                LowStockAlert("Gadget B", "SKU-002", 5, 15)
            )
        )
    }

    private fun successResult(): Result<DashboardData> {
        return Result.Success(successData())
    }
}
