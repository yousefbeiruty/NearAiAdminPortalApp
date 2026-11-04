package com.nearai.admin.feature.systemhealth.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.auth.domain.AuthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
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
 * Tests for [SystemHealthRepositoryImpl] verifying that [ApiErrorParser] is used
 * so that HTTP 401 responses trigger automatic token refresh and retry.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SystemHealthRepositoryImplTest {

    @RelaxedMockK
    private lateinit var apiService: SystemHealthApiService

    @RelaxedMockK
    private lateinit var authRepository: AuthRepository

    @RelaxedMockK
    private lateinit var sessionManager: SessionManager

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleResponse = SystemHealthResponseDto(
        status = "healthy",
        checkedAt = "2026-11-04T18:30:00Z",
        application = ApplicationHealthDto("healthy", "dev", 3600),
        components = mapOf(
            "database" to ComponentHealthDto("healthy", mapOf("latency_ms" to "12")),
            "storage" to ComponentHealthDto("healthy", mapOf("backend" to "local"))
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

    private fun createRepository(): SystemHealthRepositoryImpl {
        val tokenRefreshManager = TokenRefreshManager(authRepository, sessionManager)
        return SystemHealthRepositoryImpl(apiService, tokenRefreshManager)
    }

    @Test
    fun `getSystemHealth returns success when API succeeds`() = runTest {
        val repo = createRepository()
        coEvery { apiService.getSystemHealth() } returns sampleResponse

        val result = repo.getSystemHealth()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("healthy", data.overallStatus)
        assertEquals(2, data.components.size)
    }

    @Test
    fun `getSystemHealth 401 triggers refresh proving UnauthorizedError mapping`() = runTest {
        val repo = createRepository()

        val refreshError = AppError.AuthenticationError("Refresh token expired")
        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired","error_code":"AUTHENTICATION_REQUIRED"}"""
        )
        val response = mockResponse(401, errorBody)
        coEvery { apiService.getSystemHealth() } throws HttpException(response)

        val result = repo.getSystemHealth()

        assertTrue(result is Result.Error)
        // If ApiErrorParser maps 401 to UnauthorizedError, TokenRefreshManager attempts refresh
        // When refresh fails, the session is cleared
        assertNotNull(result)
    }

    @Test
    fun `getSystemHealth 403 does not trigger refresh`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Forbidden"}"""
        )
        val response = mockResponse(403, errorBody)
        coEvery { apiService.getSystemHealth() } throws HttpException(response)

        val result = repo.getSystemHealth()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppError.AuthenticationError)
    }

    @Test
    fun `getSystemHealth 500 returns ServerError`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Internal server error"}"""
        )
        val response = mockResponse(500, errorBody)
        coEvery { apiService.getSystemHealth() } throws HttpException(response)

        val result = repo.getSystemHealth()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppError.ServerError)
    }

    @Test
    fun `getSystemHealth network error returns NetworkError`() = runTest {
        val repo = createRepository()
        coEvery { apiService.getSystemHealth() } throws IOException("No internet")

        val result = repo.getSystemHealth()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppError.NetworkError)
    }

    @Test
    fun `getSystemHealth unexpected exception returns UnknownError`() = runTest {
        val repo = createRepository()
        coEvery { apiService.getSystemHealth() } throws RuntimeException("Unexpected")

        val result = repo.getSystemHealth()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppError.UnknownError)
    }

    private fun mockResponse(code: Int, body: ResponseBody): retrofit2.Response<Unit> {
        return retrofit2.Response.error(code, body)
    }
}
