package com.nearai.admin.feature.auditlogs.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.auditlogs.domain.PaginatedAuditLogs
import com.nearai.admin.feature.auth.domain.AuthRepository
import io.mockk.MockKAnnotations
import kotlinx.serialization.json.Json
import io.mockk.coEvery
import io.mockk.coVerify
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

@OptIn(ExperimentalCoroutinesApi::class)
class AuditLogsRepositoryImplTest {

    @RelaxedMockK
    private lateinit var apiService: AuditLogsApiService

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
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createRepository(): AuditLogsRepositoryImpl {
        val tokenRefreshManager = TokenRefreshManager(authRepository, sessionManager)
        return AuditLogsRepositoryImpl(apiService, tokenRefreshManager)
    }

    @Test
    fun `listAuditLogs returns success with mapped data`() = runTest {
        val repo = createRepository()
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val response = AuditLogsListResponseDto(
            items = listOf(
                AuditLogDto(
                    id = "log-1",
                    actorId = "actor-1",
                    actorRole = "admin",
                    action = "product.create",
                    resourceType = "product",
                    resourceId = "prod-1",
                    requestId = "req-1",
                    changes = json.parseToJsonElement("""{"name":"Test"}"""),
                    metadata = null,
                    createdAt = "2025-11-04T10:00:00Z"
                )
            ),
            total = 1,
            page = 1,
            size = 20,
            totalPages = 1
        )

        coEvery { apiService.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns response

        val result = repo.listAuditLogs()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.logs.size)
        assertEquals("log-1", data.logs[0].id)
        assertEquals("product.create", data.logs[0].action)
    }

    @Test
    fun `listAuditLogs passes filter parameters to API`() = runTest {
        val repo = createRepository()
        val response = AuditLogsListResponseDto(
            items = emptyList(),
            total = 0,
            page = 1,
            size = 20,
            totalPages = 0
        )

        coEvery { apiService.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns response

        repo.listAuditLogs(
            page = 2,
            size = 10,
            action = "product.create",
            resourceType = "product",
            resourceId = "prod-1",
            startDate = "2025-11-01",
            endDate = "2025-11-04"
        )

        coVerify {
            apiService.listAuditLogs(
                page = 2,
                size = 10,
                actorId = null,
                action = "product.create",
                resourceType = "product",
                resourceId = "prod-1",
                startDate = "2025-11-01",
                endDate = "2025-11-04"
            )
        }
    }

    @Test
    fun `listAuditLogs 401 triggers refresh proving UnauthorizedError mapping`() = runTest {
        val repo = createRepository()

        val refreshError = AppError.AuthenticationError("Refresh token expired")
        coEvery { authRepository.refreshToken() } returns Result.Error(refreshError)

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Signature has expired","error_code":"AUTHENTICATION_REQUIRED"}"""
        )
        val http401 = HttpException(retrofit2.Response.error<String>(401, errorBody))
        coEvery { apiService.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } throws http401

        val result = repo.listAuditLogs()

        coVerify { authRepository.refreshToken() }
        assertTrue(result is Result.Error)
    }

    @Test
    fun `listAuditLogs 403 does NOT trigger token refresh`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"detail":"Forbidden"}""")
        val http403 = HttpException(retrofit2.Response.error<String>(403, errorBody))
        coEvery { apiService.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } throws http403

        val result = repo.listAuditLogs()

        coVerify(exactly = 0) { authRepository.refreshToken() }
        assertTrue(result is Result.Error)
        // 403 maps to AuthenticationError, NOT UnauthorizedError — no refresh should occur
        assertTrue((result as Result.Error).exception is AppError.AuthenticationError)
    }

    @Test
    fun `listAuditLogs returns NetworkError on IOException`() = runTest {
        val repo = createRepository()

        coEvery { apiService.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } throws IOException()

        val result = repo.listAuditLogs()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppError.NetworkError)
    }

    @Test
    fun `listAuditLogs returns ServerError on 500`() = runTest {
        val repo = createRepository()

        val errorBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"detail":"Internal error"}"""
        )
        val http500 = HttpException(retrofit2.Response.error<String>(500, errorBody))
        coEvery { apiService.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } throws http500

        val result = repo.listAuditLogs()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppError.ServerError)
    }
}
