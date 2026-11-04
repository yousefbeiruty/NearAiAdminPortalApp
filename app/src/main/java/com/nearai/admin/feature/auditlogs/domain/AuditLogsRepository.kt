package com.nearai.admin.feature.auditlogs.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model representing a single audit log entry.
 */
data class AuditLog(
    val id: String,
    val actorId: String?,
    val actorRole: String,
    val action: String,
    val resourceType: String,
    val resourceId: String?,
    val requestId: String?,
    val changes: String?,
    val metadata: String?,
    val createdAt: String
)

/**
 * Paginated list of audit logs.
 */
data class PaginatedAuditLogs(
    val logs: List<AuditLog>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Repository interface for audit log operations.
 */
interface AuditLogsRepository {

    /**
     * Fetches a paginated list of audit logs with optional filters.
     */
    suspend fun listAuditLogs(
        page: Int = 1,
        size: Int = 20,
        actorId: String? = null,
        action: String? = null,
        resourceType: String? = null,
        resourceId: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<PaginatedAuditLogs>
}
