package com.nearai.admin.feature.users.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.users.domain.AdminUser
import com.nearai.admin.feature.users.domain.PaginatedAdminUsers
import com.nearai.admin.feature.users.domain.UsersRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [UsersRepository] that calls the real FastAPI backend.
 */
@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val apiService: UsersApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : UsersRepository {

    override suspend fun listUsers(
        page: Int,
        size: Int,
        search: String?,
        role: String?,
        isActive: Boolean?
    ): Result<PaginatedAdminUsers> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listUsers(page, size, search, role, isActive)
                Result.Success(UsersMapper.mapPaginatedUsers(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getUser(userId: String): Result<AdminUser> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getUser(userId)
                Result.Success(UsersMapper.mapUser(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun updateUser(
        userId: String,
        role: String?,
        isActive: Boolean?
    ): Result<AdminUser> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val request = UpdateUserRequestDto(role = role, isActive = isActive)
                val response = apiService.updateUser(userId, request)
                Result.Success(UsersMapper.mapUser(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun revokeSessions(userId: String): Result<Int> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.revokeSessions(userId)
                Result.Success(response.revokedCount)
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
