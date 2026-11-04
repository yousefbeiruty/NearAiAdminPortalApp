package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [DashboardRepository] that calls the real FastAPI backend.
 *
 * Uses [ApiErrorParser.parse] to correctly map HTTP 401 to [AppError.UnauthorizedError]
 * so that [TokenRefreshManager.refreshAndRetry] can trigger automatic token refresh.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: DashboardApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : DashboardRepository {

    override suspend fun getDashboardData(): Result<DashboardData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getDashboard()
                Result.Success(DashboardMapper.mapDashboardData(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleException(e: Throwable): Result.Error {
        return when (e) {
            is HttpException -> Result.Error(ApiErrorParser.parse(e))
            is IOException -> Result.Error(AppError.NetworkError("No internet connection"))
            else -> Result.Error(AppError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
