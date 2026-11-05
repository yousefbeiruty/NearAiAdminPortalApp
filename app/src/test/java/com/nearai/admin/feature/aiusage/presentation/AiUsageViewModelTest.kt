package com.nearai.admin.feature.aiusage.presentation

import app.cash.turbine.test
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.aiusage.domain.AiUsageData
import com.nearai.admin.feature.aiusage.domain.AiUsageRepository
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
 * Tests for [AiUsageViewModel] verifying that the AI Usage feature
 * displays an honest unavailable state rather than fabricated data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiUsageViewModelTest {

    @RelaxedMockK
    private lateinit var aiUsageRepository: AiUsageRepository

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
    fun `initial state is not loading and feature is not available`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns unavailableResult()

        val viewModel = createViewModel()

        // After init, loading should be false and feature unavailable
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isFeatureAvailable)
        assertNotNull(state.error)
    }

    @Test
    fun `unavailable feature shows error message and no fabricated data`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns unavailableResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isFeatureAvailable)
        assertTrue(state.error!!.contains("not yet available", ignoreCase = true))
        assertEquals(0.0, state.monthlyBudgetConsumed, 0.001)
        assertEquals(0.0, state.monthlyBudgetTotal, 0.001)
    }

    @Test
    fun `ui state does not contain fabricated budget values`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns unavailableResult()

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        // Verify no fabricated data — budget values should be zero
        assertEquals(0.0, state.monthlyBudgetConsumed, 0.001)
        assertEquals(0.0, state.monthlyBudgetTotal, 0.001)
    }

    @Test
    fun `onRetry reloads AI usage data`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returnsMany listOf(
            unavailableResult(),
            unavailableResult()
        )

        val viewModel = createViewModel()

        viewModel.onRetry()

        coVerify(exactly = 2) { aiUsageRepository.getAiUsageData() }
    }

    @Test
    fun `clearError removes error message`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns unavailableResult()

        val viewModel = createViewModel()
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `ai usage loads automatically on init`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns unavailableResult()

        val viewModel = createViewModel()

        coVerify { aiUsageRepository.getAiUsageData() }
    }

    @Test
    fun `state transitions from loading to error via turbine`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns unavailableResult()

        val viewModel = AiUsageViewModel(aiUsageRepository)

        // With UnconfinedTestDispatcher, all emissions happen before turbine starts collecting.
        // Turbine will receive the final state (error) as the first emission.
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertFalse(state.isFeatureAvailable)
            assertNotNull(state.error)
            // Confirm we ended in the error state, not still loading
            assertTrue(state.error!!.contains("not yet available", ignoreCase = true))
        }
    }

    @Test
    fun `success result sets feature available when API becomes available`() = runTest {
        val data = AiUsageData(
            monthlyBudgetConsumed = 100.0,
            monthlyBudgetTotal = 500.0
        )
        coEvery { aiUsageRepository.getAiUsageData() } returns Result.Success(data)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isFeatureAvailable)
        assertEquals(100.0, state.monthlyBudgetConsumed, 0.001)
        assertEquals(500.0, state.monthlyBudgetTotal, 0.001)
        assertNull(state.error)
    }

    @Test
    fun `network error displays appropriate error message`() = runTest {
        coEvery { aiUsageRepository.getAiUsageData() } returns Result.Error(
            AppError.NetworkError("No internet connection")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isFeatureAvailable)
        assertEquals("No internet connection", state.error)
    }

    private fun createViewModel(): AiUsageViewModel {
        return AiUsageViewModel(aiUsageRepository)
    }

    private fun unavailableResult(): Result<AiUsageData> {
        return Result.Error(
            AppError.ValidationError(
                message = "AI usage data is not yet available. This feature is coming soon.",
                errorCode = "FEATURE_UNAVAILABLE"
            )
        )
    }
}
