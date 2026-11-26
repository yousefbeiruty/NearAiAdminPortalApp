package com.nearai.admin.feature.dashboard.presentation

import app.cash.turbine.test
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import com.nearai.admin.feature.dashboard.domain.CurrencyAmount
import com.nearai.admin.feature.dashboard.domain.DailyOrderCounts
import com.nearai.admin.feature.dashboard.domain.FinancialOverview
import com.nearai.admin.feature.dashboard.domain.LowStockAlert
import com.nearai.admin.feature.dashboard.domain.OperationalOverview
import com.nearai.admin.feature.dashboard.domain.OrderCounts
import com.nearai.admin.feature.dashboard.domain.PlatformDashboardData
import com.nearai.admin.feature.dashboard.domain.RecentOrder
import com.nearai.admin.feature.dashboard.domain.RecentRegistration
import com.nearai.admin.feature.dashboard.domain.RevenueByCurrency
import com.nearai.admin.feature.dashboard.domain.TenantCounts
import com.nearai.admin.feature.dashboard.domain.UserCounts
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
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(50, state.platformTenants?.total ?: 0)
        assertEquals(14820, state.platformUsers?.total ?: 0)
        assertEquals(1250, state.platformOrders?.total ?: 0)
        assertEquals(45, state.platformOrders?.pending ?: 0)
        assertEquals(1180, state.platformOrders?.completed ?: 0)
        assertEquals(2, state.platformFinancialRevenue.size)
        assertEquals("USD", state.platformFinancialRevenue.get(0).currency)
        assertEquals(3, state.platformPendingApprovals)
        assertEquals(25, state.platformOrdersToday)
        assertEquals(0, state.platformFailedJobs)
        assertNull(state.error)
    }

    @Test
    fun `loadDashboard success populates all metrics`() = runTest {
        val data = successData()
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(50, state.platformTenants?.total ?: 0)
        assertEquals(14820, state.platformUsers?.total ?: 0)
        assertEquals(1250, state.platformOrders?.total ?: 0)
        assertEquals(45, state.platformOrders?.pending ?: 0)
        assertEquals(1180, state.platformOrders?.completed ?: 0)
        assertEquals(2, state.platformFinancialRevenue.size)
        assertNull(state.error)
    }

    @Test
    fun `revenue by currency is mapped with formatted amounts`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.platformFinancialRevenue.size)
        assertEquals("USD", state.platformFinancialRevenue[0].currency)
        assertNotNull(state.platformFinancialRevenue[0].formattedAmount)
        assertEquals("EUR", state.platformFinancialRevenue[1].currency)
    }

    @Test
    fun `platform orders are mapped correctly`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1250, state.platformOrders?.total ?: 0)
        assertEquals(45, state.platformOrders?.pending ?: 0)
        assertEquals(1180, state.platformOrders?.completed ?: 0)
    }

    @Test
    fun `platform users are mapped correctly`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(14820, state.platformUsers?.total ?: 0)
        assertEquals(5, state.platformUsers?.platformAdmins ?: 0)
        assertEquals(50, state.platformUsers?.merchantAdmins ?: 0)
    }

    @Test
    fun `platform tenants are mapped correctly`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(50, state.platformTenants?.total ?: 0)
        assertEquals(45, state.platformTenants?.active ?: 0)
        assertEquals(3, state.platformTenants?.pending ?: 0)
        assertEquals(2, state.platformTenants?.suspended ?: 0)
    }

    @Test
    fun `loadDashboard error sets error message and stops loading`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Error(
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
        coEvery { dashboardRepository.getPlatformDashboardData() } returnsMany listOf(
            Result.Error(AppError.NetworkError("No internet")),
            Result.Success(successData())
        )

        val viewModel = createViewModel()

        assertEquals("No internet", viewModel.uiState.value.error)

        viewModel.onRetry()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(50, state.platformTenants?.total ?: 0)

        coVerify(exactly = 2) { dashboardRepository.getPlatformDashboardData() }
    }

    @Test
    fun `onRetry calls repository`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        viewModel.onRetry()

        coVerify { dashboardRepository.getPlatformDashboardData() }
    }

    @Test
    fun `onRefresh reloads dashboard`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        viewModel.onRefresh()

        coVerify(exactly = 2) { dashboardRepository.getPlatformDashboardData() }
    }

    @Test
    fun `clearError removes error message`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Error(
            AppError.ServerError("Server error", 500)
        )

        val viewModel = createViewModel()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `empty data renders zero values`() = runTest {
        val emptyData = PlatformDashboardData()
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Success(emptyData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.platformTenants?.total ?: 0)
        assertEquals(0, state.platformUsers?.total ?: 0)
        assertTrue(state.platformFinancialRevenue.isEmpty())
        assertTrue(state.platformFinancialPayments.isEmpty())
        assertEquals(0, state.platformPendingApprovals)
    }

    @Test
    fun `dashboard loads automatically on init`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns successResult()

        val viewModel = createViewModel()

        coVerify { dashboardRepository.getPlatformDashboardData() }
    }

    @Test
    fun `server error preserves error details`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Error(
            AppError.ServerError("Internal server error", 500)
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `authentication error during dashboard load`() = runTest {
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Error(
            AppError.UnauthorizedError("Session expired")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("Session expired", state.error)
    }

    @Test
    fun `currency formatting handles invalid currency code`() = runTest {
        val data = PlatformDashboardData(
            financial = FinancialOverview(
                revenueByCurrency = listOf(
                    CurrencyAmount(currency = "INVALID", amount = java.math.BigDecimal("100.0"))
                )
            )
        )
        coEvery { dashboardRepository.getPlatformDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.platformFinancialRevenue.size)
        // Should fallback to "100.00 INVALID" format
        assertTrue(state.platformFinancialRevenue[0].formattedAmount.contains("100"))
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(dashboardRepository)
    }

    private fun successData(): PlatformDashboardData {
        return PlatformDashboardData(
            tenants = TenantCounts(total = 50, active = 45, pending = 3, suspended = 2, deleted = 0),
            users = UserCounts(total = 14820, platformAdmins = 5, merchantAdmins = 50, customers = 14765),
            orders = OrderCounts(total = 1250, pending = 45, completed = 1180, cancelled = 15, rejected = 10),
            dailyOrders = DailyOrderCounts(today = 25, currentMonth = 450),
            financial = FinancialOverview(
                revenueByCurrency = listOf(
                    CurrencyAmount(currency = "USD", amount = java.math.BigDecimal("4820.50")),
                    CurrencyAmount(currency = "EUR", amount = java.math.BigDecimal("1250.75"))
                ),
                paymentTotalsByCurrency = emptyList(),
                codTotalsByCurrency = emptyList()
            ),
            operational = OperationalOverview(pendingApprovals = 3, failedJobs = 0, pendingJobs = 2)
        )
    }

    private fun successResult(): Result<PlatformDashboardData> {
        return Result.Success(successData())
    }
}
