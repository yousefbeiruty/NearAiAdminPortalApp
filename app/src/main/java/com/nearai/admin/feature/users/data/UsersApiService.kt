package com.nearai.admin.feature.users.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for platform user inspection endpoints (read-only).
 *
 * Backend contract:
 * - GET /api/v1/platform/users       — list users across all tenants (paginated, filtered)
 * - GET /api/v1/platform/users/{id}  — get user detail by ID
 */
interface UsersApiService {

    @GET("api/v1/platform/users")
    suspend fun listUsers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("is_active") isActive: Boolean? = null
    ): UsersListResponseDto

    @GET("api/v1/platform/users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): UserResponseDto
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
    @SerialName("tenant_id")
    val tenantId: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)