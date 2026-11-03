package com.nearai.admin.feature.auth.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.common.toAppError
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenStorage
import com.nearai.admin.feature.auth.domain.AdminSession
import com.nearai.admin.feature.auth.domain.AuthRepository
import com.nearai.admin.feature.auth.domain.SignInResponse
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [AuthRepository] that communicates with the
 * NearAI FastAPI backend via Retrofit.
 *
 * Handles login, token refresh, logout, and session validation.
 * MFA is not yet supported by the backend; if the backend adds MFA support,
 * the signIn method can be extended to return [SignInResponse.MfaRequired].
 *
 * Backend contract (AUTH_API_CONTRACTS.md):
 * - Login returns access_token, refresh_token, token_type, expires_in, user_id, email
 * - Refresh returns new access_token and refresh_token (rotation)
 * - Logout revokes the refresh token
 * - /me returns id, email, role, is_active, email_verified, created_at
 */
@Singleton
class RealAuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<SignInResponse> {
        return try {
            val response = authApiService.login(LoginRequest(email, password))

            // Store tokens BEFORE calling /auth/me so the authorization interceptor
            // can attach the access token to the request. Without this, the interceptor
            // reads an empty TokenStorage and sends no Authorization header, causing
            // the backend to return 401 "session expired".
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)

            // Fetch full user info to get role, is_active, email_verified
            val userInfo = authApiService.getCurrentUser()

            // Validate account is active
            if (!userInfo.is_active) {
                // Clear tokens for inactive accounts — they should not be stored
                tokenStorage.clearAll()
                return Result.Error(AppError.AuthenticationError("Account is inactive. Contact administrator."))
            }

            // Validate email is verified
            if (!userInfo.emailVerified) {
                // Clear tokens for unverified accounts
                tokenStorage.clearAll()
                return Result.Error(AppError.AuthenticationError("Email not verified. Please verify your email first."))
            }

            // Save role after validation passes
            tokenStorage.saveUserRole(userInfo.role)
            tokenStorage.saveUserEmail(userInfo.email)

            val session = AdminSession(
                email = userInfo.email,
                role = userInfo.role,
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            Result.Success(SignInResponse.Session(session))
        } catch (e: HttpException) {
            // If login itself failed (401), clear any partially stored tokens
            if (e.code() == 401) {
                tokenStorage.clearAll()
            }
            val appError = ApiErrorParser.parse(e)
            Result.Error(appError)
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    override suspend fun verifyMfa(challengeId: String, code: String): Result<AdminSession> {
        // MFA is not yet supported by the backend.
        // When the backend adds MFA endpoints, implement here.
        return Result.Error(AppError.ServerError(
            message = "MFA verification is not yet supported by the backend.",
            statusCode = 501
        ))
    }

    override suspend fun refreshToken(): Result<AdminSession> {
        return try {
            val storedRefreshToken = tokenStorage.getRefreshToken()
                ?: return Result.Error(AppError.AuthenticationError("No refresh token available"))

            val response = authApiService.refreshToken(RefreshRequest(storedRefreshToken))

            // Store rotated tokens BEFORE calling /auth/me so the authorization interceptor
            // can attach the NEW access token. Without this, /auth/me uses the old expired
            // token and returns 401. New refresh token replaces old one immediately to
            // prevent concurrent reuse (backend revokes all sessions on reuse detection).
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)

            // Get current user info to retrieve role
            val userInfo = authApiService.getCurrentUser()

            tokenStorage.saveUserRole(userInfo.role)
            tokenStorage.saveUserEmail(userInfo.email)

            val session = AdminSession(
                email = userInfo.email,
                role = userInfo.role,
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            Result.Success(session)
        } catch (e: HttpException) {
            // If refresh failed (401), the refresh token was revoked — clear all tokens
            if (e.code() == 401) {
                tokenStorage.clearAll()
            }
            val appError = ApiErrorParser.parse(e)
            Result.Error(appError)
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    override suspend fun signOut() {
        try {
            // Send refresh token to backend to revoke it
            val refreshToken = tokenStorage.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                authApiService.logout(LogoutRequest(refreshToken))
            }
        } catch (_: Exception) {
            // Ignore logout errors — always clear local data
        } finally {
            tokenStorage.clearAll()
        }
    }

    /**
     * Fetches the current user info from the backend to validate the session.
     * Returns null if the request fails (e.g., expired token).
     */
    suspend fun getCurrentUserInfo(): UserInfoDto? {
        return try {
            authApiService.getCurrentUser()
        } catch (e: HttpException) {
            // 401 means token is expired — caller should handle refresh
            null
        } catch (e: Exception) {
            null
        }
    }
}
