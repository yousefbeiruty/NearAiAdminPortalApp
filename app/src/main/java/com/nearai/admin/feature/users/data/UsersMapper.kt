package com.nearai.admin.feature.users.data

import com.nearai.admin.feature.users.domain.AdminUser
import com.nearai.admin.feature.users.domain.PaginatedAdminUsers

/**
 * Mapper functions that convert API DTOs to domain models.
 */
object UsersMapper {

    fun mapUser(dto: UserResponseDto): AdminUser = AdminUser(
        id = dto.id,
        email = dto.email,
        role = dto.role,
        isActive = dto.isActive,
        emailVerified = dto.emailVerified,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )

    fun mapPaginatedUsers(dto: UsersListResponseDto): PaginatedAdminUsers = PaginatedAdminUsers(
        users = dto.items.map { mapUser(it) },
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )
}
