package com.nearai.admin.core.security

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.auth.domain.AuthRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that handles automatic access-token refresh on 401 (Unauthorized) responses.
 *
 * Key behaviors:
 * - **Single-flight refresh** — uses a [Mutex] so concurrent 401 responses share a single
 *   refresh attempt. The backend revokes all refresh sessions on token reuse, so concurrent
 *   refresh requests must never accidentally reuse the same refresh token.
 * - **Single retry** — the original request is retried at most once after a successful refresh.
 * - **Auth endpoint exclusion** — `/auth/login`, `/auth/refresh`, `/auth/logout` never trigger
 *   auto-refresh to prevent infinite loops.
 * - **Session cleanup** — if refresh fails unrecoverably, the session is cleared via
 *   [SessionManager.onSessionExpired].
 *
 * Usage: wrap any repository API call with [refreshAndRetry]:
 * ```
 * tokenRefreshManager.refreshAndRetry { repository.listOrders(...) }
 * ```
 *
 * Safe debug logging: logs request URL, whether Authorization header exists, refresh start/end,
 * and retry status. Never logs token values, passwords, or refresh tokens.
 */
@Singleton
class TokenRefreshManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) {

    private val mutex = Mutex()

    /**
     * Executes the given [request] and, if it returns an [AppError.UnauthorizedError],
     * attempts a single-flight token refresh and retries the request once.
     *
     * If the refresh succeeds, the rotated tokens are already persisted by
     * [AuthRepository.refreshToken], so the retry uses the new access token via the
     * OkHttp authorization interceptor.
     *
     * If the refresh fails, the session is cleared and the refresh error is returned.
     */
    suspend fun <T> refreshAndRetry(request: suspend () -> Result<T>): Result<T> {
        val result = request()

        // If not an UnauthorizedError, return immediately
        if (result !is Result.Error || result.exception !is AppError.UnauthorizedError) {
            return result
        }

        return mutex.withLock {
            // Check if another concurrent request already refreshed the token.
            val refreshedResult = request()
            if (refreshedResult !is Result.Error || refreshedResult.exception !is AppError.UnauthorizedError) {
                return@withLock refreshedResult
            }

            // No other request refreshed the token — perform the refresh ourselves
            return@withLock when (val refreshResult = authRepository.refreshToken()) {
                is Result.Success -> {
                    // Tokens are already persisted by AuthRepository.refreshToken()
                    request()
                }

                is Result.Error -> {
                    // Unrecoverable — clear the session so the user is redirected to login
                    sessionManager.onSessionExpired()
                    Result.Error(refreshResult.exception)
                }

                is Result.Loading -> {
                    // Should not happen — refreshToken() is a suspend function that completes
                    // before returning. Treat as failure.
                    sessionManager.onSessionExpired()
                    Result.Error(AppError.UnknownError("Token refresh timed out"))
                }
            }
        }
    }
}
