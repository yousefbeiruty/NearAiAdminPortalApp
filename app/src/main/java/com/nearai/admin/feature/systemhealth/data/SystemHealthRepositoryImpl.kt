package com.nearai.admin.feature.systemhealth.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.systemhealth.domain.SystemHealthData
import com.nearai.admin.feature.systemhealth.domain.SystemHealthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [SystemHealthRepository] that calls the real FastAPI backend.
 */
@Singleton
class SystemHealthRepositoryImpl @Inject constructor(
    private val apiService: SystemHealthApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : SystemHealthRepository {

    override suspend fun getSystemHealth(): Result<SystemHealthData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getSystemHealth()
                Result.Success(SystemHealthMapper.mapSystemHealth(response))
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
