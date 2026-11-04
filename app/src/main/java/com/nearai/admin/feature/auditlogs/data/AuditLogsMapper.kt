package com.nearai.admin.feature.auditlogs.data

import com.nearai.admin.feature.auditlogs.domain.AuditLog
import com.nearai.admin.feature.auditlogs.domain.PaginatedAuditLogs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Mapper functions that convert API DTOs to domain models.
 */
object AuditLogsMapper {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Converts a [JsonElement] back to its JSON string representation.
     */
    private fun JsonElement.toStringify(): String {
        return buildString {
            serialize(this)
        }
    }

    private fun JsonElement.serialize(sb: StringBuilder) {
        when (this) {
            is kotlinx.serialization.json.JsonNull -> sb.append("null")
            is kotlinx.serialization.json.JsonPrimitive -> {
                if (this.isString) {
                    sb.append("\"").append(content.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"")
                } else {
                    sb.append(content)
                }
            }
            is kotlinx.serialization.json.JsonObject -> {
                sb.append("{")
                val entries = this.entries
                entries.forEachIndexed { index, (key, value) ->
                    if (index > 0) sb.append(",")
                    sb.append("\"").append(key).append("\"").append(":")
                    value.serialize(sb)
                }
                sb.append("}")
            }
            is kotlinx.serialization.json.JsonArray -> {
                sb.append("[")
                this.forEachIndexed { index, element ->
                    if (index > 0) sb.append(",")
                    element.serialize(sb)
                }
                sb.append("]")
            }
        }
    }

    fun mapLog(dto: AuditLogDto): AuditLog = AuditLog(
        id = dto.id,
        actorId = dto.actorId,
        actorRole = dto.actorRole,
        action = dto.action,
        resourceType = dto.resourceType,
        resourceId = dto.resourceId,
        requestId = dto.requestId,
        changes = dto.changes?.toStringify(),
        metadata = dto.metadata?.toStringify(),
        createdAt = dto.createdAt
    )

    fun mapPaginatedLogs(dto: AuditLogsListResponseDto): PaginatedAuditLogs = PaginatedAuditLogs(
        logs = dto.items.map { mapLog(it) },
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )
}
