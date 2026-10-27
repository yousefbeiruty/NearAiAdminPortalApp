package com.nearai.admin.feature.auth.data

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service interface for authentication endpoints.
 *
 * NOTE: These endpoints are defined based on the expected FastAPI backend contract.
 * The exact paths and response shapes must be confirmed with the backend team.
 *
 * Expected contract:
 * - POST /auth/sign-in  → { "accessToken": "...", "refreshToken": "..." }
 *   or { "challengeId": "...", "method": "totp" } when MFA is required
 * - POST /auth/verify-mfa → { "accessToken": "...", "refreshToken": "..." }
 * - POST /auth/refresh   → { "accessToken": "...", "refreshToken": "..." }
 */
interface AuthApiService {

    @POST("auth/sign-in")
    suspend fun signIn(@Body request: SignInRequest): SignInResponseDto

    @POST("auth/verify-mfa")
    suspend fun verifyMfa(@Body request: MfaVerifyRequest): SessionDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): SessionDto
}

// --- Request DTOs ---

data class SignInRequest(
    val email: String,
    val password: String
)

data class MfaVerifyRequest(
    val challengeId: String,
    val code: String
)

data class RefreshRequest(
    val refreshToken: String
)

// --- Response DTOs ---

/**
 * Sign-in response may contain either a session or an MFA challenge.
 * FastAPI typically returns one shape or the other.
 */
sealed interface SignInResponseDto {
    data class Session(val session: SessionDto) : SignInResponseDto
    data class MfaRequired(val challengeId: String, val method: String, val hint: String? = null) : SignInResponseDto
}

data class SessionDto(
    val accessToken: String,
    val refreshToken: String,
    val email: String? = null
)
