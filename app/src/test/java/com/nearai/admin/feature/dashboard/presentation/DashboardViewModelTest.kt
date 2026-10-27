package com.nearai.admin.feature.dashboard.presentation

import app.cash.turbine.test
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.ActivityEvent
import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import com.nearai.admin.feature.dashboard.domain.KpiMetric
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
        // With UnconfinedTestDispatcher, init block runs immediately,
        // so we verify the final state after automatic load
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        // After init, loading should be complete with data populated
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(6, state.kpiMetrics.size)
        assertEquals(4, state.activityEvents.size)
        assertNull(state.error)
    }

    @Test
    fun `loadDashboard success populates KPI metrics and activity events`() = runTest {
        val data = successData()
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(6, state.kpiMetrics.size)
        assertEquals(4, state.activityEvents.size)
        assertEquals("2m ago", state.lastUpdated)
        assertEquals("100% Operational", state.clusterStatus)
        assertNull(state.error)

        // Verify KPI mapping
        val firstKpi = state.kpiMetrics[0]
        assertEquals("TOTAL USERS", firstKpi.title)
        assertEquals("14,820", firstKpi.value)
        assertEquals("↗ +12.4% vs last wk", firstKpi.delta)
        assertTrue(firstKpi.isPositive)

        // Verify activity event mapping
        val firstEvent = state.activityEvents[0]
        assertEquals("evt-1", firstEvent.id)
        assertEquals("New Pro Subscription", firstEvent.title)
        assertEquals("success", firstEvent.statusType)
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
        assertTrue(state.kpiMetrics.isEmpty())
        assertTrue(state.activityEvents.isEmpty())
    }

    @Test
    fun `onRetry reloads dashboard after error`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returnsMany listOf(
            Result.Error(AppError.NetworkError("No internet")),
            Result.Success(successData())
        )

        val viewModel = createViewModel()

        // First load fails
        assertEquals("No internet", viewModel.uiState.value.error)

        // Retry succeeds
        viewModel.onRetry()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(6, state.kpiMetrics.size)

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
    fun `empty KPI list renders empty state`() = runTest {
        val emptyData = DashboardData(
            kpiMetrics = emptyList(),
            activityEvents = emptyList(),
            lastUpdated = "just now",
            clusterStatus = "Unknown"
        )
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(emptyData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.kpiMetrics.isEmpty())
        assertTrue(state.activityEvents.isEmpty())
        assertEquals("just now", state.lastUpdated)
        assertEquals("Unknown", state.clusterStatus)
    }

    @Test
    fun `empty activity events with KPI data`() = runTest {
        val data = DashboardData(
            kpiMetrics = listOf(
                KpiMetric("TOTAL USERS", "14,820", "↗ +12.4%", true, "People")
            ),
            activityEvents = emptyList(),
            lastUpdated = "1m ago",
            clusterStatus = "Operational"
        )
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.kpiMetrics.size)
        assertTrue(state.activityEvents.isEmpty())
    }

    @Test
    fun `activity event status types are preserved`() = runTest {
        val data = successData()
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val events = viewModel.uiState.value.activityEvents
        assertEquals("success", events[0].statusType)
        assertEquals("warning", events[1].statusType)
        assertEquals("error", events[2].statusType)
        assertEquals("success", events[3].statusType)
    }

    @Test
    fun `KPI metric values are correctly mapped`() = runTest {
        val data = successData()
        coEvery { dashboardRepository.getDashboardData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val kpis = viewModel.uiState.value.kpiMetrics

        // Verify all 6 KPIs
        assertEquals("TOTAL USERS", kpis[0].title)
        assertEquals("ACTIVE (DAU)", kpis[1].title)
        assertEquals("AI REQUESTS", kpis[2].title)
        assertEquals("AI SPEND", kpis[3].title)
        assertEquals("PLACES FOUND", kpis[4].title)
        assertEquals("EST. REVENUE", kpis[5].title)

        // Verify positive/negative flags
        assertTrue(kpis[0].isPositive)
        assertTrue(kpis[1].isPositive)
        assertTrue(kpis[2].isPositive)
        assertFalse(kpis[3].isPositive)
        assertTrue(kpis[4].isPositive)
        assertTrue(kpis[5].isPositive)
    }

    @Test
    fun `dashboard loads automatically on init`() = runTest {
        coEvery { dashboardRepository.getDashboardData() } returns successResult()

        val viewModel = createViewModel()

        // Verify repository was called automatically
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

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(dashboardRepository)
    }

    private fun successData(): DashboardData {
        return DashboardData(
            kpiMetrics = listOf(
                KpiMetric("TOTAL USERS", "14,820", "↗ +12.4% vs last wk", true, "People"),
                KpiMetric("ACTIVE (DAU)", "3,410", "↗ +8.2% vs yest.", true, "Bolt"),
                KpiMetric("AI REQUESTS", "84.6k", "99.4% ok · 512 err", true, "Memory"),
                KpiMetric("AI SPEND", "$428.50", "● 71% of \$600 cap", false, "AccountBalanceWallet"),
                KpiMetric("PLACES FOUND", "192.4k", "↑ +18% queries", true, "Explore"),
                KpiMetric("EST. REVENUE", "$4,820", "Pro + 18 trials", true, "CreditCard")
            ),
            activityEvents = listOf(
                ActivityEvent("evt-1", "New Pro Subscription", "sarah.k@gmail.com · Annual Plan", "4m ago", "Active", "success"),
                ActivityEvent("evt-2", "AI Budget Soft Cap (70%)", "Google Cloud Run / OpenAI Routing", "28m ago", "Warning", "warning"),
                ActivityEvent("evt-3", "Failed Discovery Query", "Places timeout (Tokyo, JP) · status 504", "1h ago", "Investigate", "error"),
                ActivityEvent("evt-4", "Batch User Import", "142 new signups from ProductHunt", "2h ago", "Success", "success")
            ),
            lastUpdated = "2m ago",
            clusterStatus = "100% Operational"
        )
    }

    private fun successResult(): Result<DashboardData> {
        return Result.Success(successData())
    }
}
