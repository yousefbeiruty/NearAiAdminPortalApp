package com.nearai.admin.feature.auditlogs.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.auditlogs.domain.AuditLogsRepository
import com.nearai.admin.feature.auditlogs.domain.PaginatedAuditLogs
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [AuditLogsRepository] that calls the real FastAPI backend.
 */
@Singleton
class AuditLogsRepositoryImpl @Inject constructor(
    private val apiService: AuditLogsApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : AuditLogsRepository {

    override suspend fun listAuditLogs(
        page: Int,
        size: Int,
        actorId: String?,
        action: String?,
        resourceType: String?,
        resourceId: String?,
        startDate: String?,
        endDate: String?
    ): Result<PaginatedAuditLogs> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listAuditLogs(
                    page = page,
                    size = size,
                    actorId = actorId,
                    action = action,
                    resourceType = resourceType,
                    resourceId = resourceId,
                    startDate = startDate,
                    endDate = endDate
                )
                Result.Success(AuditLogsMapper.mapPaginatedLogs(response))
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
