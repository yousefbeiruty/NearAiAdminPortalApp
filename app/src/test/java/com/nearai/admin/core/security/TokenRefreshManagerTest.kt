package com.nearai.admin.core.security

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.auth.domain.AdminSession
import com.nearai.admin.feature.auth.domain.AuthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TokenRefreshManagerTest {

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

    private fun createManager(): TokenRefreshManager {
        return TokenRefreshManager(authRepository, sessionManager)
    }

    @Test
    fun `success request returns immediately without refresh`() = runTest {
        val manager = createManager()
        val result = manager.refreshAndRetry { Result.Success("data") }

        assertEquals(Result.Success("data"), result)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `non-401 error returns without refresh`() = runTest {
        val manager = createManager()
        val error = AppError.ServerError("Server error", 500)
        val result = manager.refreshAndRetry { Result.Error(error) }

        assertEquals(Result.Error(error), result)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `401 triggers refresh and retry on success`() = runTest {
        val manager = createManager()
        val newSession = AdminSession("admin@nearai.dev", "admin", "newAccess", "newRefresh")

        coEvery { authRepository.refreshToken() } returns Result.Success(newSession)

        var callCount = 0
        val result = manager.refreshAndRetry {
            callCount++
            // First two calls return 401 (initial + re-check inside mutex)
            // Third call is the retry after refresh — returns success
            if (callCount < 3) {
                Result.Error(AppError.UnauthorizedError("Session expired"))
            } else {
                Result.Success("retried data")
            }
        }

        assertEquals(Result.Success("retried data"), result)
        assertEquals(3, callCount)
        coVerify(exactly = 1) { authRepository.refreshToken() }
    }

    @Test
    fun `401 with failed refresh clears session`() = runTest {
        val manager = createManager()
        val refreshError = AppError.AuthenticationError("Refresh token expired")
        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val result = manager.refreshAndRetry {
            Result.Error(AppError.UnauthorizedError("Session expired"))
        }

        assertEquals(Result.Error(refreshError), result)
        verify { sessionManager.onSessionExpired() }
    }

    @Test
    fun `401 with retry still failing returns retry error`() = runTest {
        val manager = createManager()
        val newSession = AdminSession("admin@nearai.dev", "admin", "newAccess", "newRefresh")
        coEvery { authRepository.refreshToken() } returns Result.Success(newSession)

        val result = manager.refreshAndRetry {
            Result.Error(AppError.UnauthorizedError("Still unauthorized"))
        }

        assertEquals(Result.Error(AppError.UnauthorizedError("Still unauthorized")), result)
        coVerify(exactly = 1) { authRepository.refreshToken() }
        verify(exactly = 0) { sessionManager.onSessionExpired() }
    }

    @Test
    fun `concurrent 401 requests do not crash`() = runTest {
        val manager = createManager()
        val newSession = AdminSession("admin@nearai.dev", "admin", "newAccess", "newRefresh")
        coEvery { authRepository.refreshToken() } returns Result.Success(newSession)

        // Launch two concurrent requests that always return 401
        val job1 = launch {
            manager.refreshAndRetry<String> {
                Result.Error(AppError.UnauthorizedError("Session expired"))
            }
        }
        val job2 = launch {
            manager.refreshAndRetry<String> {
                Result.Error(AppError.UnauthorizedError("Session expired"))
            }
        }

        job1.join()
        job2.join()

        // Both should complete without crashing; refresh is called at least once
        coVerify(atLeast = 1) { authRepository.refreshToken() }
    }

    @Test
    fun `concurrent 401 - at least one succeeds after refresh`() = runTest {
        val manager = createManager()
        val newSession = AdminSession("admin@nearai.dev", "admin", "newAccess", "newRefresh")
        coEvery { authRepository.refreshToken() } returns Result.Success(newSession)

        var callCount = 0
        val results = mutableListOf<Result<String>>()

        val job1 = launch {
            results.add(manager.refreshAndRetry<String> {
                callCount++
                if (callCount < 4) {
                    Result.Error(AppError.UnauthorizedError("Session expired"))
                } else {
                    Result.Success("success")
                }
            })
        }
        val job2 = launch {
            results.add(manager.refreshAndRetry<String> {
                callCount++
                if (callCount < 4) {
                    Result.Error(AppError.UnauthorizedError("Session expired"))
                } else {
                    Result.Success("success")
                }
            })
        }

        job1.join()
        job2.join()

        coVerify(atLeast = 1) { authRepository.refreshToken() }
        // At least one request should succeed after refresh
        assertTrue(results.any { it is Result.Success<*> })
    }

    @Test
    fun `network error does not trigger refresh`() = runTest {
        val manager = createManager()
        val error = AppError.NetworkError("No internet")
        val result = manager.refreshAndRetry { Result.Error(error) }

        assertEquals(Result.Error(error), result)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `not found error does not trigger refresh`() = runTest {
        val manager = createManager()
        val error = AppError.NotFoundError("Order not found")
        val result = manager.refreshAndRetry { Result.Error(error) }

        assertEquals(Result.Error(error), result)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `validation error does not trigger refresh`() = runTest {
        val manager = createManager()
        val error = AppError.ValidationError("Invalid input")
        val result = manager.refreshAndRetry { Result.Error(error) }

        assertEquals(Result.Error(error), result)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `unknown error does not trigger refresh`() = runTest {
        val manager = createManager()
        val error = AppError.UnknownError("Something went wrong")
        val result = manager.refreshAndRetry { Result.Error(error) }

        assertEquals(Result.Error(error), result)
        coVerify(exactly = 0) { authRepository.refreshToken() }
    }

    @Test
    fun `refresh with Loading result clears session`() = runTest {
        val manager = createManager()
        coEvery { authRepository.refreshToken() } returns Result.Loading

        val result = manager.refreshAndRetry {
            Result.Error(AppError.UnauthorizedError("Session expired"))
        }

        assertTrue(result is Result.Error)
        verify { sessionManager.onSessionExpired() }
    }
}
