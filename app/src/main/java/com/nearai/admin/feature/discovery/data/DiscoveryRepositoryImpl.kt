package com.nearai.admin.feature.discovery.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.discovery.domain.DiscoveryRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [DiscoveryRepository] that calls the real FastAPI backend.
 *
 * Uses [ApiErrorParser.parse] to correctly map HTTP 401 to [AppError.UnauthorizedError]
 * so that [TokenRefreshManager.refreshAndRetry] can trigger automatic token refresh.
 */
@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val apiService: DiscoveryApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : DiscoveryRepository {

    override suspend fun getDiscoveryOverview(days: Int, compare: Boolean): Result<com.nearai.admin.feature.discovery.domain.DiscoveryOverviewData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getDiscoveryOverview(days, compare)
                Result.Success(DiscoveryMapper.mapDiscoveryOverview(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getSearchAnalytics(days: Int, page: Int, size: Int): Result<com.nearai.admin.feature.discovery.domain.SearchAnalyticsData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getSearchAnalytics(days, page, size)
                Result.Success(DiscoveryMapper.mapSearchAnalytics(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getPopularProducts(days: Int, metric: String, size: Int): Result<com.nearai.admin.feature.discovery.domain.PopularProductsData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getPopularProducts(days, metric, size)
                Result.Success(DiscoveryMapper.mapPopularProducts(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getPopularCategories(days: Int, size: Int): Result<com.nearai.admin.feature.discovery.domain.PopularCategoriesData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getPopularCategories(days, size)
                Result.Success(DiscoveryMapper.mapPopularCategories(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getConversionFunnel(days: Int): Result<com.nearai.admin.feature.discovery.domain.ConversionFunnelData> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getConversionFunnel(days)
                Result.Success(DiscoveryMapper.mapConversionFunnel(response))
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
