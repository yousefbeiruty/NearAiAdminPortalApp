package com.nearai.admin.feature.discovery.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.auth.domain.AdminSession
import com.nearai.admin.feature.auth.domain.AuthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException

/**
 * Tests that [BusinessAnalyticsRepositoryImpl] correctly integrates with [TokenRefreshManager]
 * so that HTTP 401 responses trigger automatic token refresh and retry.
 *
 * Key scenarios:
 * - Successful analytics fetch returns data
 * - HTTP 401 triggers token refresh via TokenRefreshManager
 * - HTTP 403 does NOT trigger token refresh
 * - HTTP 500 server error is handled
 * - Network errors are handled gracefully
 * - Token refresh success allows retry to succeed
 * - Token refresh failure clears session and propagates error
 * - Query parameters are correctly mapped
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessAnalyticsRepositoryImplTest {

    @RelaxedMockK
    private lateinit var apiService: BusinessAnalyticsApiService

    @RelaxedMockK
    private lateinit var authRepository: AuthRepository

    @RelaxedMockK
    private lateinit var sessionManager: SessionManager

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

    private fun createRepository(): BusinessAnalyticsRepositoryImpl {
        val tokenRefreshManager = TokenRefreshManager(authRepository, sessionManager)
        return BusinessAnalyticsRepositoryImpl(apiService, tokenRefreshManager)
    }

    // ========================================================================
    // Success scenarios
    // ========================================================================

    @Test
    fun `getBusinessOverview returns success when API succeeds`() = runTest {
        val repo = createRepository()
        val dto = BusinessAnalyticsResponseDto(
            totalOrders = 150,
            completedOrders = 120,
            revenueByCurrency = listOf(
                RevenueByCurrencyDto("USD", 5000.50, 80, 62.51)
            )
        )

        coEvery { apiService.getBusinessOverview(7, null, null, true) } returns dto

        val result = repo.getBusinessOverview(days = 7, startDate = null, endDate = null, compare = true)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(150, data.totalOrders)
        assertEquals(120, data.completedOrders)
        assertEquals(1, data.revenueByCurrency.size)
    }

    @Test
    fun `getBusinessOverview maps query parameters correctly`() = runTest {
        val repo = createRepository()
        val dto = BusinessAnalyticsResponseDto(totalOrders = 100)

        coEvery { apiService.getBusinessOverview(30, "2024-01-01", "2024-01-30", false) } returns dto

        repo.getBusinessOverview(days = 30, startDate = "2024-01-01", endDate = "2024-01-30", compare = false)

        coVerify(exactly = 1) { apiService.getBusinessOverview(30, "2024-01-01", "2024-01-30", false) }
    }

    // ========================================================================
    // Error mapping
    // ========================================================================

    @Test
    fun `getBusinessOverview 401 triggers refresh proving UnauthorizedError mapping`() = runTest {
        val repo = createRepository()

        val refreshError = AppError.AuthenticationError("Refresh token expired")
        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired","error_code":"AUTHENTICATION_REQUIRED"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws http401

        val result = repo.getBusinessOverview()

        coVerify(exactly = 1) { authRepository.refreshToken() }
        assertTrue(result is Result.Error)
        verify { sessionManager.onSessionExpired() }
    }

    @Test
    fun `getBusinessOverview maps HTTP 403 to AuthenticationError`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Access denied"}"""
        )
        val http403 = HttpException(retrofit2.Response.error<String>(403, errorBody))

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws http403

        val result = repo.getBusinessOverview()

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue("Expected AuthenticationError but got ${error::class.simpleName}", error is AppError.AuthenticationError)
    }

    @Test
    fun `getBusinessOverview maps HTTP 500 to ServerError`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Internal server error"}"""
        )
        val http500 = HttpException(retrofit2.Response.error<String>(500, errorBody))

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws http500

        val result = repo.getBusinessOverview()

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue("Expected ServerError but got ${error::class.simpleName}", error is AppError.ServerError)
    }

    @Test
    fun `getBusinessOverview handles network error`() = runTest {
        val repo = createRepository()

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws IOException("No internet")

        val result = repo.getBusinessOverview()

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppError.NetworkError)
    }

    // ========================================================================
    // Token refresh integration
    // ========================================================================

    @Test
    fun `getBusinessOverview with 401 triggers refresh and retry on success`() = runTest {
        val repo = createRepository()
        val dto = BusinessAnalyticsResponseDto(totalOrders = 150)
        val newSession = AdminSession("admin@nearai.dev", "admin", "newAccess", "newRefresh")

        coEvery { authRepository.refreshToken() } returns Result.Success(newSession)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        var callCount = 0
        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } answers {
            callCount++
            if (callCount < 3) {
                throw http401
            } else {
                dto
            }
        }

        val result = repo.getBusinessOverview()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(150, data.totalOrders)
        assertEquals(3, callCount)
        coVerify(exactly = 1) { authRepository.refreshToken() }
    }

    @Test
    fun `getBusinessOverview with 401 and failed refresh clears session`() = runTest {
        val repo = createRepository()
        val refreshError = AppError.AuthenticationError("Refresh token expired")

        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws http401

        val result = repo.getBusinessOverview()

        assertTrue(result is Result.Error)
        assertEquals(refreshError, (result as Result.Error).exception)
        verify { sessionManager.onSessionExpired() }
    }

    @Test
    fun `getBusinessOverview with 403 does not trigger token refresh`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Access denied"}"""
        )
        val http403 = HttpException(retrofit2.Response.error<String>(403, errorBody))

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws http403

        repo.getBusinessOverview()

        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `getBusinessOverview with network error does not trigger token refresh`() = runTest {
        val repo = createRepository()

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws IOException("No internet")

        repo.getBusinessOverview()

        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `getBusinessOverview with 500 does not trigger token refresh`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Server error"}"""
        )
        val http500 = HttpException(retrofit2.Response.error<String>(500, errorBody))

        coEvery { apiService.getBusinessOverview(any(), any(), any(), any()) } throws http500

        repo.getBusinessOverview()

        coVerify(exactly = 0) { authRepository.refreshToken() }
    }
}
