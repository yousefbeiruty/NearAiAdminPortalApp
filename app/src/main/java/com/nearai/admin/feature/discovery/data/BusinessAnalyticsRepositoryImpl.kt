package com.nearai.admin.feature.discovery.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.discovery.domain.BusinessAnalyticsRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [BusinessAnalyticsRepository] that calls the real FastAPI backend.
 *
 * Uses [ApiErrorParser.parse] to correctly map HTTP 401 to [AppError.UnauthorizedError]
 * so that [TokenRefreshManager.refreshAndRetry] can trigger automatic token refresh.
 */
@Singleton
class BusinessAnalyticsRepositoryImpl @Inject constructor(
    private val apiService: BusinessAnalyticsApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : BusinessAnalyticsRepository {

    override suspend fun getBusinessOverview(
        days: Int,
        startDate: String?,
        endDate: String?,
        compare: Boolean
    ): Result<com.nearai.admin.feature.discovery.domain.BusinessAnalyticsData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getBusinessOverview(days, startDate, endDate, compare)
                Result.Success(BusinessAnalyticsMapper.mapBusinessOverview(response))
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
