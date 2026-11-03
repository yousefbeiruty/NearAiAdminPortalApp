package com.nearai.admin.feature.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit service interface for authentication endpoints.
 *
 * Backend contract (FastAPI) — AUTH_API_CONTRACTS.md:
 *
 * POST /api/v1/auth/login
 *   Request: { "email": "...", "password": "..." }
 *   Response: { "access_token": "...", "refresh_token": "...", "token_type": "bearer",
 *               "expires_in": 3600, "user_id": "...", "email": "..." }
 *
 * POST /api/v1/auth/refresh
 *   Request: { "refresh_token": "..." }
 *   Response: { "access_token": "...", "refresh_token": "...", "token_type": "bearer",
 *               "expires_in": 3600 }
 *   Note: New refresh token must immediately replace the old one (rotation).
 *
 * POST /api/v1/auth/logout
 *   Request: { "refresh_token": "..." }
 *   Response: { "message": "..." }
 *   Note: Revokes the refresh token but does not immediately blacklist the access token.
 *
 * GET /api/v1/auth/me
 *   Header: Authorization: Bearer <access_token>
 *   Response: { "id": "...", "email": "...", "role": "...", "is_active": true,
 *               "email_verified": true, "created_at": "..." }
 *
 * Error responses:
 * - 401: Invalid credentials or expired token
 * - 403: User does not have admin role (on admin endpoints)
 * - 422: Validation error (e.g., missing fields)
 */
interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): TokenResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: LogoutRequest)

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): UserInfoDto
}

// --- Request DTOs ---

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token") val refreshToken: String
)

// --- Response DTOs ---

@Serializable
data class LoginResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("expires_in") val expiresIn: Int = 3600,
    @SerialName("user_id") val userId: String? = null,
    val email: String? = null
)

@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("expires_in") val expiresIn: Int = 3600
)

@Serializable
data class UserInfoDto(
    val id: String,
    val email: String,
    val role: String,
    val is_active: Boolean = true,
    @SerialName("email_verified") val emailVerified: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class LogoutResponseDto(
    val message: String
)
