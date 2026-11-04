package com.nearai.admin.feature.users.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model representing a user in the admin portal.
 */
data class AdminUser(
    val id: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Paginated list of admin users.
 */
data class PaginatedAdminUsers(
    val users: List<AdminUser>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Repository interface for user management operations.
 */
interface UsersRepository {

    /**
     * Fetches a paginated list of users with optional filters.
     */
    suspend fun listUsers(
        page: Int = 1,
        size: Int = 20,
        search: String? = null,
        role: String? = null,
        isActive: Boolean? = null
    ): Result<PaginatedAdminUsers>

    /**
     * Fetches a single user by ID.
     */
    suspend fun getUser(userId: String): Result<AdminUser>

    /**
     * Updates a user's role and/or active status.
     */
    suspend fun updateUser(
        userId: String,
        role: String? = null,
        isActive: Boolean? = null
    ): Result<AdminUser>

    /**
     * Revokes all refresh sessions for a user.
     */
    suspend fun revokeSessions(userId: String): Result<Int>
}
