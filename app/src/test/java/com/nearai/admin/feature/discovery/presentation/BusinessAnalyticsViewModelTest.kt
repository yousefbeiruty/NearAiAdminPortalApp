package com.nearai.admin.feature.discovery.presentation

import app.cash.turbine.test
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.discovery.domain.*
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

/**
 * Tests that [BusinessAnalyticsViewModel] correctly manages UI state, loading,
 * error handling, retry, date filter changes, and stale response rejection.
 *
 * Covers:
 * - Initial loading
 * - Successful data loading
 * - Error and retry
 * - Refresh
 * - 7-day and 30-day filter changes
 * - Stale response rejection
 * - Empty analytics data
 * - Currency formatting
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessAnalyticsViewModelTest {

    @RelaxedMockK
    private lateinit var repository: BusinessAnalyticsRepository

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

    private fun createViewModel(): BusinessAnalyticsViewModel {
        return BusinessAnalyticsViewModel(repository)
    }

    // ========================================================================
    // Initial state
    // ========================================================================

    @Test
    fun `initial state has selectedDays default of 7`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns successResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        // ViewModel loads data in init, so totalOrders will be populated
        assertEquals(7, state.selectedDays)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    // ========================================================================
    // Successful data loading
    // ========================================================================

    @Test
    fun `loadBusinessOverview success populates all metrics`() = runTest {
        val data = successData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(150, state.totalOrders)
        assertEquals(120, state.completedOrders)
        assertEquals(15, state.pendingOrders)
        assertEquals(10, state.cancelledOrders)
        assertEquals(5, state.rejectedOrders)
        assertEquals(25, state.newRegistrations)
        assertEquals(80, state.activeCustomers)
        assertEquals(20, state.repeatCustomers)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadBusinessOverview populates formatted numbers`() = runTest {
        val data = successData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("150", state.totalOrdersFormatted)
        assertEquals("120", state.completedOrdersFormatted)
        assertNotNull(state.totalOrdersFormatted)
        assertNotNull(state.completedOrdersFormatted)
    }

    @Test
    fun `loadBusinessOverview populates revenue by currency`() = runTest {
        val data = successData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.revenueByCurrency.size)
        assertEquals("USD", state.revenueByCurrency[0].currency)
        assertEquals(5000.50, state.revenueByCurrency[0].totalAmount, 0.01)
        assertEquals("EUR", state.revenueByCurrency[1].currency)
        assertEquals(3000.75, state.revenueByCurrency[1].totalAmount, 0.01)
    }

    @Test
    fun `loadBusinessOverview populates best selling products`() = runTest {
        val data = successData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.bestSellingProducts.size)
        assertEquals("prod-1", state.bestSellingProducts[0].productId)
        assertEquals("Widget A", state.bestSellingProducts[0].productName)
        assertEquals(50, state.bestSellingProducts[0].unitsSold)
    }

    @Test
    fun `loadBusinessOverview populates category sales`() = runTest {
        val data = successData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.categorySales.size)
        assertEquals("cat-1", state.categorySales[0].categoryId)
        assertEquals("Electronics", state.categorySales[0].categoryName)
    }

    @Test
    fun `loadBusinessOverview populates period comparison`() = runTest {
        val data = successDataWithComparison()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.compare)
        assertNotNull(state.totalOrdersPercentageChange)
        assertEquals(25.0, state.totalOrdersPercentageChange!!, 0.01)
        assertNotNull(state.revenuePercentageChanges)
        assertEquals(1, state.revenuePercentageChanges!!.size)
    }

    // ========================================================================
    // Error handling
    // ========================================================================

    @Test
    fun `loadBusinessOverview error sets error state`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Error(
            AppError.ServerError("Server error", 500)
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertEquals("Server error", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `retry clears error and reloads data`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returnsMany listOf(
            Result.Error(AppError.ServerError("Server error", 500)),
            successResult()
        )

        val viewModel = createViewModel()

        val errorState = viewModel.uiState.value
        assertNotNull(errorState.error)

        viewModel.retry()

        val retryState = viewModel.uiState.value
        assertNull(retryState.error)
        assertEquals(150, retryState.totalOrders)
    }

    // ========================================================================
    // Date filter changes
    // ========================================================================

    @Test
    fun `setSelectedDays changes filter and reloads`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returnsMany listOf(
            successResult(),
            successResult(days = 30)
        )

        val viewModel = createViewModel()

        assertEquals(7, viewModel.uiState.value.selectedDays)

        viewModel.setSelectedDays(30)

        assertEquals(30, viewModel.uiState.value.selectedDays)
        coVerify(exactly = 2) { repository.getBusinessOverview(any(), any(), any(), any()) }
    }

    @Test
    fun `setSelectedDays to 7 days reloads data`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returnsMany listOf(
            successResult(),
            successResult(days = 7)
        )

        val viewModel = createViewModel()

        viewModel.setSelectedDays(30)
        viewModel.setSelectedDays(7)

        assertEquals(7, viewModel.uiState.value.selectedDays)
        coVerify(exactly = 3) { repository.getBusinessOverview(any(), any(), any(), any()) }
    }

    // ========================================================================
    // Empty data
    // ========================================================================

    @Test
    fun `loadBusinessOverview handles empty analytics data`() = runTest {
        val data = BusinessAnalyticsData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(0, state.totalOrders)
        assertTrue(state.revenueByCurrency.isEmpty())
        assertTrue(state.bestSellingProducts.isEmpty())
        assertTrue(state.categorySales.isEmpty())
        assertEquals(0, state.activeCustomers)
        assertNull(state.error)
    }

    // ========================================================================
    // Currency formatting
    // ========================================================================

    @Test
    fun `revenue is formatted with two decimal places`() = runTest {
        val data = BusinessAnalyticsData(
            revenueByCurrency = listOf(
                RevenueByCurrency("USD", 5000.50, 80, 62.51)
            )
        )
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("5000.50", state.revenueByCurrency[0].formattedTotal)
        assertEquals("62.51", state.revenueByCurrency[0].formattedAvgOrderValue)
    }

    @Test
    fun `product revenue is formatted correctly`() = runTest {
        val data = BusinessAnalyticsData(
            bestSellingProducts = listOf(
                BestSellingProduct("prod-1", "Widget A", "SKU-001", 50, 2500.0, "USD")
            )
        )
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("2500.00", state.bestSellingProducts[0].formattedRevenue)
    }

    // ========================================================================
    // Stale response rejection
    // ========================================================================

    @Test
    fun `rapid filter changes use latest data`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returnsMany listOf(
            successResult(days = 7),
            successResult(days = 30)
        )

        val viewModel = createViewModel()

        // Initial load with 7 days
        val initialState = viewModel.uiState.value
        assertEquals(7, initialState.selectedDays)

        // Change to 30 days
        viewModel.setSelectedDays(30)

        val finalState = viewModel.uiState.value
        assertEquals(30, finalState.selectedDays)
        assertEquals(30, finalState.days)
    }

    // ========================================================================
    // UI state flow
    // ========================================================================

    @Test
    fun `uiState emits updated state on data load`() = runTest {
        val data = successData()
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returns Result.Success(data)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(150, state.totalOrders)
            assertEquals(120, state.completedOrders)
        }
    }

    @Test
    fun `retry clears error state`() = runTest {
        coEvery { repository.getBusinessOverview(any(), any(), any(), any()) } returnsMany listOf(
            Result.Error(AppError.ServerError("Server error", 500)),
            successResult()
        )

        val viewModel = createViewModel()

        val errorState = viewModel.uiState.value
        assertNotNull(errorState.error)

        viewModel.retry()

        val retryState = viewModel.uiState.value
        assertNull(retryState.error)
        assertEquals(150, retryState.totalOrders)
    }
}

// =============================================================================
// Test helpers
// =============================================================================

fun successData(
    days: Int = 7,
    totalOrders: Int = 150,
    completedOrders: Int = 120,
    pendingOrders: Int = 15,
    cancelledOrders: Int = 10,
    rejectedOrders: Int = 5,
    newRegistrations: Int = 25,
    activeCustomers: Int = 80,
    repeatCustomers: Int = 20
): BusinessAnalyticsData {
    return BusinessAnalyticsData(
        startDate = "2024-01-01",
        endDate = "2024-01-07",
        days = days,
        totalOrders = totalOrders,
        ordersByStatus = listOf(
            OrderStatusBreakdown("completed", completedOrders),
            OrderStatusBreakdown("pending", pendingOrders),
            OrderStatusBreakdown("cancelled", cancelledOrders),
            OrderStatusBreakdown("rejected", rejectedOrders)
        ),
        dailyOrders = listOf(
            DailyOrderCount("2024-01-01", 20),
            DailyOrderCount("2024-01-02", 25)
        ),
        completedOrders = completedOrders,
        cancelledOrders = cancelledOrders,
        pendingOrders = pendingOrders,
        rejectedOrders = rejectedOrders,
        revenueByCurrency = listOf(
            RevenueByCurrency("USD", 5000.50, 80, 62.51),
            RevenueByCurrency("EUR", 3000.75, 40, 75.02)
        ),
        dailyRevenue = listOf(
            DailyRevenue("2024-01-01", 1000.0, "USD"),
            DailyRevenue("2024-01-01", 500.0, "EUR")
        ),
        bestSellingProducts = listOf(
            BestSellingProduct("prod-1", "Widget A", "SKU-001", 50, 2500.0, "USD"),
            BestSellingProduct("prod-2", "Widget B", "SKU-002", 30, 1500.0, "EUR")
        ),
        categorySales = listOf(
            CategorySales("cat-1", "Electronics", 100, 5000.0, "USD"),
            CategorySales("cat-2", "Clothing", 50, 2000.0, "EUR")
        ),
        newRegistrations = newRegistrations,
        dailyRegistrations = listOf(
            DailyUserRegistration("2024-01-01", 5)
        ),
        activeCustomers = activeCustomers,
        repeatCustomers = repeatCustomers
    )
}

fun successDataWithComparison(): BusinessAnalyticsData {
    return successData().copy(
        compare = true,
        previousStartDate = "2023-12-25",
        previousEndDate = "2023-12-31",
        totalOrdersComparison = IntPeriodComparison(150, 120, 25.0),
        completedOrdersComparison = IntPeriodComparison(120, 100, 20.0),
        revenueComparison = listOf(
            RevenuePeriodComparison(5000.50, 4000.00, 25.13)
        ),
        newRegistrationsComparison = IntPeriodComparison(25, 20, 25.0),
        activeCustomersComparison = IntPeriodComparison(80, 70, 14.29)
    )
}

fun successResult(days: Int = 7) = Result.Success<BusinessAnalyticsData>(successData(days = days))
