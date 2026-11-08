package com.nearai.admin.feature.orders.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.orders.domain.ApprovalPolicyConfig
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderDetails
import com.nearai.admin.feature.orders.domain.OrderRepository
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.PaginatedOrders
import com.nearai.admin.feature.orders.domain.PaginatedReviewQueue
import com.nearai.admin.feature.orders.domain.PaymentInfo
import com.nearai.admin.feature.orders.domain.StatusHistoryEntry
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [OrderRepository] that calls the real FastAPI backend.
 *
 * Uses [OrderApiService] for HTTP calls and [OrderMapper] for DTO-to-domain conversion.
 * All errors are caught and converted to [Result.Error] with appropriate [AppError] types.
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: OrderApiService,
    private val paymentApiService: PaymentApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : OrderRepository {

    override suspend fun listOrders(
        page: Int,
        size: Int,
        status: OrderStatus?,
        fulfillmentStatus: FulfillmentStatus?
    ): Result<PaginatedOrders> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listOrders(
                    page = page,
                    size = size,
                    status = status?.name?.lowercase(),
                    fulfillmentStatus = fulfillmentStatus?.name?.lowercase()
                )
                Result.Success(OrderMapper.mapPaginatedOrders(response))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun getOrderDetails(orderId: String): Result<OrderDetails> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getOrderDetails(orderId)
                Result.Success(OrderMapper.mapOrderDetails(response))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun getStatusHistory(orderId: String): Result<List<StatusHistoryEntry>> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getStatusHistory(orderId)
                Result.Success(response.history.map { OrderMapper.mapStatusHistoryEntry(it) })
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun confirmOrder(orderId: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.confirmOrder(orderId)
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun rejectOrder(orderId: String, reason: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.rejectOrder(orderId, RejectRequest(reason))
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun processOrder(orderId: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.processOrder(orderId)
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun dispatchOrder(orderId: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.dispatchOrder(orderId)
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun deliverOrder(orderId: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.deliverOrder(orderId)
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.cancelOrder(orderId)
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun getPaymentByOrderId(orderId: String): Result<PaymentInfo> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = paymentApiService.getPaymentByOrderId(orderId)
                Result.Success(PaymentInfo(
                    id = response.id,
                    orderId = response.orderId,
                    paymentMethod = response.paymentMethod,
                    amount = response.amount,
                    currency = response.currency,
                    status = response.status,
                    collectedAt = response.collectedAt,
                    collectedBy = response.collectedBy
                ))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun collectCash(paymentId: String): Result<PaymentInfo> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = paymentApiService.collectCash(paymentId)
                Result.Success(PaymentInfo(
                    id = response.id,
                    orderId = response.orderId,
                    paymentMethod = response.paymentMethod,
                    amount = response.amount,
                    currency = response.currency,
                    status = response.status,
                    collectedAt = response.collectedAt,
                    collectedBy = response.collectedBy
                ))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    // =============================================================================
    // Approval Operations
    // =============================================================================

    override suspend fun listReviewQueue(
        page: Int,
        size: Int,
        mandatoryOnly: Boolean
    ): Result<PaginatedReviewQueue> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listReviewQueue(
                    page = page,
                    size = size,
                    mandatoryOnly = if (mandatoryOnly) true else null
                )
                Result.Success(OrderMapper.mapPaginatedReviewQueue(response))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun approveOrder(orderId: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.approveOrder(orderId)
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun rejectForApproval(orderId: String, reason: String): Result<Unit> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                apiService.rejectForApproval(orderId, RejectApprovalRequest(reason))
                Result.Success(Unit)
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun getApprovalPolicy(): Result<ApprovalPolicyConfig> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getApprovalPolicy()
                Result.Success(OrderMapper.mapApprovalPolicy(response))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    override suspend fun updateApprovalPolicy(
        autoApprovalLimit: Double,
        mandatoryReviewThreshold: Double,
        enabled: Boolean
    ): Result<ApprovalPolicyConfig> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.updateApprovalPolicy(
                    UpdatePolicyRequest(
                        autoApprovalLimit = autoApprovalLimit,
                        mandatoryReviewThreshold = mandatoryReviewThreshold,
                        enabled = enabled
                    )
                )
                Result.Success(OrderMapper.mapApprovalPolicy(response))
            } catch (e: Exception) {
                handleOrderException(e)
            }
        }
    }

    /**
     * Centralized error handling for all order API calls.
     * Converts HTTP exceptions and network errors to appropriate [AppError] types.
     */
    private fun handleOrderException(e: Throwable): Result.Error {
        return when (e) {
            is HttpException -> Result.Error(ApiErrorParser.parse(e))
            is IOException -> Result.Error(AppError.NetworkError("No internet connection"))
            else -> Result.Error(AppError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
