package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.auth.domain.AdminSession
import com.nearai.admin.feature.auth.domain.AuthRepository
import com.nearai.admin.feature.dashboard.domain.DashboardData
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
 * Tests that [DashboardRepositoryImpl] correctly integrates with [TokenRefreshManager]
 * so that HTTP 401 responses trigger automatic token refresh and retry.
 *
 * Key scenarios:
 * - Successful dashboard fetch returns data
 * - HTTP 401 triggers token refresh via TokenRefreshManager
 * - HTTP 403 does NOT trigger token refresh
 * - Network errors are handled gracefully
 * - Token refresh success allows retry to succeed
 * - Token refresh failure clears session and propagates error
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardRepositoryImplTest {

    @RelaxedMockK
    private lateinit var apiService: DashboardApiService

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

    private fun createRepository(): DashboardRepositoryImpl {
        val tokenRefreshManager = TokenRefreshManager(authRepository, sessionManager)
        return DashboardRepositoryImpl(apiService, tokenRefreshManager)
    }

    // ========================================================================
    // Success scenarios
    // ========================================================================

    @Test
    fun `getDashboardData returns success when API succeeds`() = runTest {
        val repo = createRepository()
        val dto = DashboardResponseDto(
            totalUsers = 100,
            totalProducts = 50,
            totalOrders = 200
        )

        coEvery { apiService.getDashboard() } returns dto

        val result = repo.getDashboardData()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(100, data.totalUsers)
        assertEquals(50, data.totalProducts)
        assertEquals(200, data.totalOrders)
    }

    // ========================================================================
    // Error mapping — verifies ApiErrorParser is used (the fix)
    // ========================================================================

    @Test
    fun `getDashboardData 401 triggers refresh proving UnauthorizedError mapping`() = runTest {
        val repo = createRepository()

        // If 401 is mapped to UnauthorizedError, TokenRefreshManager will attempt refresh.
        // We verify refresh is called, proving the mapping is correct.
        val refreshError = AppError.AuthenticationError("Refresh token expired")
        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired","error_code":"AUTHENTICATION_REQUIRED"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        coEvery { apiService.getDashboard() } throws http401

        val result = repo.getDashboardData()

        // The fact that refreshToken() was called proves 401 → UnauthorizedError mapping works
        coVerify(exactly = 1) { authRepository.refreshToken() }
        assertTrue(result is Result.Error)
        verify { sessionManager.onSessionExpired() }
    }

    @Test
    fun `handleException maps 401 to UnauthorizedError via ApiErrorParser`() = runTest {
        // Direct verification that ApiErrorParser.parse maps 401 to UnauthorizedError.
        // DashboardRepositoryImpl.handleException delegates to ApiErrorParser.parse for HttpException.
        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired","error_code":"AUTHENTICATION_REQUIRED"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        val error = ApiErrorParser.parse(http401)

        assertTrue("Expected UnauthorizedError but got ${error::class.simpleName}", error is AppError.UnauthorizedError)
    }

    @Test
    fun `getDashboardData maps HTTP 403 to AuthenticationError`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Access denied"}"""
        )
        val http403 = HttpException(retrofit2.Response.error<String>(403, errorBody))

        coEvery { apiService.getDashboard() } throws http403

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        // 403 maps to AuthenticationError, NOT UnauthorizedError — no refresh should occur
        assertTrue("Expected AuthenticationError but got ${error::class.simpleName}", error is AppError.AuthenticationError)
    }

    @Test
    fun `getDashboardData maps HTTP 500 to ServerError`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Internal server error"}"""
        )
        val http500 = HttpException(retrofit2.Response.error<String>(500, errorBody))

        coEvery { apiService.getDashboard() } throws http500

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue("Expected ServerError but got ${error::class.simpleName}", error is AppError.ServerError)
    }

    @Test
    fun `getDashboardData handles network error`() = runTest {
        val repo = createRepository()

        coEvery { apiService.getDashboard() } throws IOException("No internet")

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        val error = (result as Result.Error).exception
        assertTrue(error is AppError.NetworkError)
    }

    // ========================================================================
    // Token refresh integration — end-to-end with real TokenRefreshManager
    // ========================================================================

    @Test
    fun `getDashboardData with 401 triggers refresh and retry on success`() = runTest {
        val repo = createRepository()
        val dto = DashboardResponseDto(totalUsers = 100)
        val newSession = AdminSession("admin@nearai.dev", "admin", "newAccess", "newRefresh")

        coEvery { authRepository.refreshToken() } returns Result.Success(newSession)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        // First two calls return 401 (initial + re-check inside mutex), third succeeds
        var callCount = 0
        coEvery { apiService.getDashboard() } answers {
            callCount++
            if (callCount < 3) {
                throw http401
            } else {
                dto
            }
        }

        val result = repo.getDashboardData()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(100, data.totalUsers)
        assertEquals(3, callCount)
        coVerify(exactly = 1) { authRepository.refreshToken() }
    }

    @Test
    fun `getDashboardData with 401 and failed refresh clears session`() = runTest {
        val repo = createRepository()
        val refreshError = AppError.AuthenticationError("Refresh token expired")

        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))

        coEvery { apiService.getDashboard() } throws http401

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        assertEquals(refreshError, (result as Result.Error).exception)
        verify { sessionManager.onSessionExpired() }
    }

    @Test
    fun `getDashboardData with 403 does not trigger token refresh`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Access denied"}"""
        )
        val http403 = HttpException(retrofit2.Response.error<String>(403, errorBody))

        coEvery { apiService.getDashboard() } throws http403

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `getDashboardData with network error does not trigger token refresh`() = runTest {
        val repo = createRepository()

        coEvery { apiService.getDashboard() } throws IOException("No internet")

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `getDashboardData with 500 does not trigger token refresh`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Server error"}"""
        )
        val http500 = HttpException(retrofit2.Response.error<String>(500, errorBody))

        coEvery { apiService.getDashboard() } throws http500

        val result = repo.getDashboardData()

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }
}
