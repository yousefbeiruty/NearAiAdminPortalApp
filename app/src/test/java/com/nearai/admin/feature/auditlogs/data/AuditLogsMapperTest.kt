package com.nearai.admin.feature.auditlogs.data

import com.nearai.admin.feature.auditlogs.domain.AuditLog
import com.nearai.admin.feature.auditlogs.domain.PaginatedAuditLogs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.*
import org.junit.Test

class AuditLogsMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `mapLog converts all DTO fields correctly`() {
        val changesJson = json.parseToJsonElement("""{"name":"Test"}""")
        val metadataJson = json.parseToJsonElement("""{"ip":"1.2.3.4"}""")
        val dto = AuditLogDto(
            id = "log-1",
            actorId = "actor-123",
            actorRole = "admin",
            action = "product.create",
            resourceType = "product",
            resourceId = "prod-456",
            requestId = "req-789",
            changes = changesJson,
            metadata = metadataJson,
            createdAt = "2025-11-04T10:30:00Z"
        )

        val log = AuditLogsMapper.mapLog(dto)

        assertEquals("log-1", log.id)
        assertEquals("actor-123", log.actorId)
        assertEquals("admin", log.actorRole)
        assertEquals("product.create", log.action)
        assertEquals("product", log.resourceType)
        assertEquals("prod-456", log.resourceId)
        assertEquals("req-789", log.requestId)
        assertNotNull(log.changes)
        assertTrue(log.changes!!.contains("\"name\""))
        assertNotNull(log.metadata)
        assertTrue(log.metadata!!.contains("\"ip\""))
        assertEquals("2025-11-04T10:30:00Z", log.createdAt)
    }

    @Test
    fun `mapLog handles null fields`() {
        val dto = AuditLogDto(
            id = "log-2",
            actorId = null,
            actorRole = "system",
            action = "order.cancel",
            resourceType = "order",
            resourceId = null,
            requestId = null,
            changes = null,
            metadata = null,
            createdAt = "2025-11-04T09:00:00Z"
        )

        val log = AuditLogsMapper.mapLog(dto)

        assertNull(log.actorId)
        assertNull(log.resourceId)
        assertNull(log.requestId)
        assertNull(log.changes)
        assertNull(log.metadata)
    }

    @Test
    fun `mapLog handles exact backend response with null actor_id and metadata`() {
        // Simulates the exact backend response:
        // {"id":"8a41c486-...","actor_id":null,"actor_role":"admin","action":"product.create",
        //  "resource_type":"product","resource_id":"10b8eef8-...","request_id":null,
        //  "changes":{"name":"Test Product for Audit Logs","status":"active"},
        //  "metadata":null,"created_at":"2026-11-04T18:18:47.720004Z"}
        val changesJson = json.parseToJsonElement(
            """{"name":"Test Product for Audit Logs","status":"active"}"""
        )
        val dto = AuditLogDto(
            id = "8a41c486-33ac-475c-9b82-e21d05a060cb",
            actorId = null,
            actorRole = "admin",
            action = "product.create",
            resourceType = "product",
            resourceId = "10b8eef8-de88-424b-878e-39b2ac26ed38",
            requestId = null,
            changes = changesJson,
            metadata = null,
            createdAt = "2026-11-04T18:18:47.720004Z"
        )

        val log = AuditLogsMapper.mapLog(dto)

        assertEquals("8a41c486-33ac-475c-9b82-e21d05a060cb", log.id)
        assertNull(log.actorId)
        assertEquals("admin", log.actorRole)
        assertEquals("product.create", log.action)
        assertEquals("product", log.resourceType)
        assertEquals("10b8eef8-de88-424b-878e-39b2ac26ed38", log.resourceId)
        assertNull(log.requestId)
        assertNotNull(log.changes)
        assertTrue(log.changes!!.contains("Test Product for Audit Logs"))
        assertTrue(log.changes!!.contains("active"))
        assertNull(log.metadata)
        assertEquals("2026-11-04T18:18:47.720004Z", log.createdAt)
    }

    @Test
    fun `deserialize exact backend JSON response`() {
        // Full backend response JSON
        val jsonResponse = """
            {
              "items": [
                {
                  "id": "8a41c486-33ac-475c-9b82-e21d05a060cb",
                  "actor_id": null,
                  "actor_role": "admin",
                  "action": "product.create",
                  "resource_type": "product",
                  "resource_id": "10b8eef8-de88-424b-878e-39b2ac26ed38",
                  "request_id": null,
                  "changes": {
                    "name": "Test Product for Audit Logs",
                    "status": "active"
                  },
                  "metadata": null,
                  "created_at": "2026-11-04T18:18:47.720004Z"
                }
              ],
              "total": 1,
              "page": 1,
              "size": 20,
              "total_pages": 1
            }
        """.trimIndent()

        val response = json.parseToJsonElement(jsonResponse)
        val jsonObject = response as JsonObject

        // Verify the JSON structure matches expectations
        val items = jsonObject["items"]
        assertNotNull(items)
        val total = jsonObject["total"]
        assertEquals(JsonPrimitive(1), total)
    }

    @Test
    fun `mapPaginatedLogs converts pagination fields correctly`() {
        val dto = AuditLogsListResponseDto(
            items = listOf(
                AuditLogDto(
                    id = "log-1",
                    actorId = "actor-1",
                    actorRole = "admin",
                    action = "test",
                    resourceType = "product",
                    resourceId = "prod-1",
                    requestId = null,
                    changes = null,
                    metadata = null,
                    createdAt = "2025-11-04T10:00:00Z"
                )
            ),
            total = 42,
            page = 1,
            size = 20,
            totalPages = 3
        )

        val paginated = AuditLogsMapper.mapPaginatedLogs(dto)

        assertEquals(1, paginated.logs.size)
        assertEquals(42, paginated.totalItems)
        assertEquals(1, paginated.page)
        assertEquals(20, paginated.size)
        assertEquals(3, paginated.totalPages)
    }

    @Test
    fun `mapPaginatedLogs handles empty list`() {
        val dto = AuditLogsListResponseDto(
            items = emptyList(),
            total = 0,
            page = 1,
            size = 20,
            totalPages = 0
        )

        val paginated = AuditLogsMapper.mapPaginatedLogs(dto)

        assertTrue(paginated.logs.isEmpty())
        assertEquals(0, paginated.totalItems)
        assertEquals(0, paginated.totalPages)
    }
}
