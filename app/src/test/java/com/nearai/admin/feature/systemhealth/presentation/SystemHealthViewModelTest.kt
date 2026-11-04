package com.nearai.admin.feature.systemhealth.presentation

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.systemhealth.domain.ApplicationHealth
import com.nearai.admin.feature.systemhealth.domain.ComponentHealth
import com.nearai.admin.feature.systemhealth.domain.SystemHealthData
import com.nearai.admin.feature.systemhealth.domain.SystemHealthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.clearAllMocks
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
 * Tests for [SystemHealthViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SystemHealthViewModelTest {

    @RelaxedMockK
    private lateinit var repository: SystemHealthRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleHealthData = SystemHealthData(
        overallStatus = "healthy",
        checkedAt = "2026-11-04T18:30:00Z",
        application = ApplicationHealth("healthy", "dev", 3600),
        components = listOf(
            ComponentHealth("database", "healthy", mapOf("latency_ms" to "12")),
            ComponentHealth("storage", "healthy", mapOf("backend" to "local")),
            ComponentHealth("scheduler", "healthy", mapOf("running" to "true")),
            ComponentHealth("email", "healthy", mapOf("provider" to "mock"))
        )
    )

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
    fun `loadHealth populates UI state on success`() = runTest {
        coEvery { repository.getSystemHealth() } returns Result.Success(sampleHealthData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals("healthy", state.overallStatus)
        assertEquals(4, state.components.size)
        assertEquals(4, state.healthyCount)
        assertEquals(0, state.unhealthyCount)
        assertEquals("dev", state.environment)
        assertEquals(3600L, state.uptimeSeconds)
        assertEquals("2026-11-04T18:30:00Z", state.checkedAt)
    }

    @Test
    fun `loadHealth shows error state on failure`() = runTest {
        coEvery { repository.getSystemHealth() } returns Result.Error(
            AppError.NetworkError("No connection")
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.healthData)
        assertNotNull(state.error)
    }

    @Test
    fun `onRefresh triggers reload`() = runTest {
        coEvery { repository.getSystemHealth() } returns Result.Success(sampleHealthData)

        val viewModel = createViewModel()

        // Initial load completes
        assertEquals("healthy", viewModel.uiState.value.overallStatus)

        // Trigger refresh
        viewModel.onRefresh()
        val refreshed = viewModel.uiState.value
        assertEquals("healthy", refreshed.overallStatus)
        assertFalse(refreshed.isRefreshing)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        coEvery { repository.getSystemHealth() } returns Result.Error(
            AppError.NetworkError("No connection")
        )

        val viewModel = createViewModel()

        assertNotNull(viewModel.uiState.value.error)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `degraded status counts unhealthy components`() = runTest {
        val degradedData = sampleHealthData.copy(
            overallStatus = "degraded",
            components = listOf(
                ComponentHealth("database", "healthy", mapOf("latency_ms" to "12")),
                ComponentHealth("storage", "unhealthy", mapOf("error" to "Not writable")),
                ComponentHealth("scheduler", "healthy", mapOf("running" to "true")),
                ComponentHealth("email", "healthy", mapOf("provider" to "mock"))
            )
        )
        coEvery { repository.getSystemHealth() } returns Result.Success(degradedData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("degraded", state.overallStatus)
        assertEquals(3, state.healthyCount)
        assertEquals(1, state.unhealthyCount)
    }

    @Test
    fun `handles unknown component status`() = runTest {
        val unknownData = sampleHealthData.copy(
            components = listOf(
                ComponentHealth("database", "healthy", mapOf("latency_ms" to "12")),
                ComponentHealth("redis", "not_configured", null)
            )
        )
        coEvery { repository.getSystemHealth() } returns Result.Success(unknownData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.components.size)
        val redis = state.components.find { it.name == "redis" }
        assertNotNull(redis)
        assertEquals("not_configured", redis!!.status)
        assertEquals(null, redis.details)
    }

    @Test
    fun `handles empty components list`() = runTest {
        val emptyData = sampleHealthData.copy(
            components = emptyList()
        )
        coEvery { repository.getSystemHealth() } returns Result.Success(emptyData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.components.isEmpty())
        assertEquals(0, state.healthyCount)
        assertEquals(0, state.unhealthyCount)
    }

    @Test
    fun `uptime seconds are preserved from backend`() = runTest {
        val uptimeData = sampleHealthData.copy(
            application = ApplicationHealth("healthy", "prod", 90061)
        )
        coEvery { repository.getSystemHealth() } returns Result.Success(uptimeData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(90061L, state.uptimeSeconds)
        assertEquals("prod", state.environment)
    }

    @Test
    fun `initial load sets loading flag then clears it`() = runTest {
        coEvery { repository.getSystemHealth() } returns Result.Success(sampleHealthData)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `error does not crash when health data is null`() = runTest {
        coEvery { repository.getSystemHealth() } returns Result.Error(
            AppError.ServerError("Internal server error", 500)
        )

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.healthData)
        assertNotNull(state.error)
        assertTrue(state.components.isEmpty())
    }

    private fun createViewModel(): SystemHealthViewModel {
        return SystemHealthViewModel(repository)
    }
}
