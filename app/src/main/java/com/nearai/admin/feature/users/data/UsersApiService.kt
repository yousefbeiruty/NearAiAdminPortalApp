package com.nearai.admin.feature.users.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for admin users management endpoints.
 *
 * Backend contract:
 * - GET    /api/v1/admin/users              — list users (paginated, filtered)
 * - GET    /api/v1/admin/users/{id}         — get user by ID
 * - PATCH  /api/v1/admin/users/{id}         — update user role/active status
 * - POST   /api/v1/admin/users/{id}/revoke-sessions — revoke all sessions
 */
interface UsersApiService {

    @GET("api/v1/admin/users")
    suspend fun listUsers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("is_active") isActive: Boolean? = null
    ): UsersListResponseDto

    @GET("api/v1/admin/users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): UserResponseDto

    @PATCH("api/v1/admin/users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: String,
        @Body request: UpdateUserRequestDto
    ): UserResponseDto

    @POST("api/v1/admin/users/{user_id}/revoke-sessions")
    suspend fun revokeSessions(@Path("user_id") userId: String): RevokeSessionsResponseDto
}

// ─── DTOs ────────────────────────────────────────────────────────────────────

@Serializable
data class UsersListResponseDto(
    val items: List<UserResponseDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    @SerialName("total_pages")
    val totalPages: Int
)

@Serializable
data class UserResponseDto(
    val id: String,
    val email: String,
    val role: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("email_verified")
    val emailVerified: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class UpdateUserRequestDto(
    val role: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)

@Serializable
data class RevokeSessionsResponseDto(
    @SerialName("revoked_count")
    val revokedCount: Int
)