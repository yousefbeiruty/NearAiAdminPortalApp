package com.nearai.admin.feature.auditlogs.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for admin audit logs endpoints.
 *
 * Backend contract:
 * - GET /api/v1/admin/audit-logs — list audit logs (paginated, filtered)
 */
interface AuditLogsApiService {

    @GET("api/v1/admin/audit-logs")
    suspend fun listAuditLogs(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("actor_id") actorId: String? = null,
        @Query("action") action: String? = null,
        @Query("resource_type") resourceType: String? = null,
        @Query("resource_id") resourceId: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): AuditLogsListResponseDto
}

// ─── DTOs ────────────────────────────────────────────────────────────────────

@Serializable
data class AuditLogsListResponseDto(
    val items: List<AuditLogDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    @SerialName("total_pages")
    val totalPages: Int
)

@Serializable
data class AuditLogDto(
    val id: String,
    @SerialName("actor_id")
    val actorId: String?,
    @SerialName("actor_role")
    val actorRole: String,
    val action: String,
    @SerialName("resource_type")
    val resourceType: String,
    @SerialName("resource_id")
    val resourceId: String?,
    @SerialName("request_id")
    val requestId: String?,
    val changes: JsonElement?,
    val metadata: JsonElement?,
    @SerialName("created_at")
    val createdAt: String
)
