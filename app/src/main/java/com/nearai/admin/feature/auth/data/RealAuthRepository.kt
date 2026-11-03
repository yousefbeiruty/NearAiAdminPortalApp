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

            // Fetch full user info to get role, is_active, email_verified
            val userInfo = authApiService.getCurrentUser()

            // Validate account is active
            if (!userInfo.is_active) {
                return Result.Error(AppError.AuthenticationError("Account is inactive. Contact administrator."))
            }

            // Validate email is verified
            if (!userInfo.emailVerified) {
                return Result.Error(AppError.AuthenticationError("Email not verified. Please verify your email first."))
            }

            val session = AdminSession(
                email = userInfo.email,
                role = userInfo.role,
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            // Store tokens immediately on successful login
            tokenStorage.saveAccessToken(session.accessToken)
            tokenStorage.saveRefreshToken(session.refreshToken)
            tokenStorage.saveUserRole(session.role)
            Result.Success(SignInResponse.Session(session))
        } catch (e: HttpException) {
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

            // Get current user info to retrieve role
            val userInfo = authApiService.getCurrentUser()

            val session = AdminSession(
                email = userInfo.email,
                role = userInfo.role,
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            // Store rotated tokens atomically — new refresh token replaces old one immediately
            tokenStorage.saveAccessToken(session.accessToken)
            tokenStorage.saveRefreshToken(session.refreshToken)
            tokenStorage.saveUserRole(session.role)

            Result.Success(session)
        } catch (e: HttpException) {
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
