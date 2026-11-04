package com.nearai.admin.feature.systemhealth.data

import com.nearai.admin.feature.systemhealth.domain.ApplicationHealth
import com.nearai.admin.feature.systemhealth.domain.ComponentHealth
import com.nearai.admin.feature.systemhealth.domain.SystemHealthData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for [SystemHealthMapper] DTO-to-domain mapping.
 */
class SystemHealthMapperTest {

    @Test
    fun `mapApplication maps all fields correctly`() {
        val dto = ApplicationHealthDto(
            status = "healthy",
            environment = "dev",
            uptimeSeconds = 3600
        )

        val result = SystemHealthMapper.mapApplication(dto)

        assertEquals("healthy", result.status)
        assertEquals("dev", result.environment)
        assertEquals(3600L, result.uptimeSeconds)
    }

    @Test
    fun `mapComponent maps name and status correctly`() {
        val dto = ComponentHealthDto(
            status = "healthy",
            details = mapOf("latency_ms" to "12")
        )

        val result = SystemHealthMapper.mapComponent("database", dto)

        assertEquals("database", result.name)
        assertEquals("healthy", result.status)
        assertNotNull(result.details)
        assertEquals("12", result.details!!["latency_ms"])
    }

    @Test
    fun `mapComponent handles null details`() {
        val dto = ComponentHealthDto(
            status = "not_configured",
            details = null
        )

        val result = SystemHealthMapper.mapComponent("redis", dto)

        assertEquals("redis", result.name)
        assertEquals("not_configured", result.status)
        assertEquals(null, result.details)
    }

    @Test
    fun `mapSystemHealth maps complete backend response`() {
        val dto = SystemHealthResponseDto(
            status = "healthy",
            checkedAt = "2026-11-04T18:30:00Z",
            application = ApplicationHealthDto("healthy", "dev", 7200),
            components = mapOf(
                "database" to ComponentHealthDto("healthy", mapOf("latency_ms" to "12")),
                "storage" to ComponentHealthDto("healthy", mapOf("backend" to "local")),
                "scheduler" to ComponentHealthDto("healthy", mapOf("running" to "true")),
                "email" to ComponentHealthDto("healthy", mapOf("provider" to "mock"))
            )
        )

        val result = SystemHealthMapper.mapSystemHealth(dto)

        assertEquals("healthy", result.overallStatus)
        assertEquals("2026-11-04T18:30:00Z", result.checkedAt)
        assertEquals("healthy", result.application.status)
        assertEquals("dev", result.application.environment)
        assertEquals(7200L, result.application.uptimeSeconds)
        assertEquals(4, result.components.size)

        val db = result.components.find { it.name == "database" }
        assertNotNull(db)
        assertEquals("healthy", db!!.status)
        assertEquals("12", db.details!!["latency_ms"])
    }

    @Test
    fun `mapSystemHealth handles degraded status`() {
        val dto = SystemHealthResponseDto(
            status = "degraded",
            checkedAt = "2026-11-04T19:00:00Z",
            application = ApplicationHealthDto("healthy", "prod", 86400),
            components = mapOf(
                "database" to ComponentHealthDto("healthy", mapOf("latency_ms" to "45")),
                "storage" to ComponentHealthDto("degraded", mapOf("backend" to "local", "error" to "Storage directory is not writable")),
                "scheduler" to ComponentHealthDto("healthy", mapOf("running" to "true")),
                "email" to ComponentHealthDto("healthy", mapOf("provider" to "smtp"))
            )
        )

        val result = SystemHealthMapper.mapSystemHealth(dto)

        assertEquals("degraded", result.overallStatus)
        assertEquals("prod", result.application.environment)
        assertEquals(86400L, result.application.uptimeSeconds)

        val storage = result.components.find { it.name == "storage" }
        assertNotNull(storage)
        assertEquals("degraded", storage!!.status)
        assertEquals("Storage directory is not writable", storage.details!!["error"])
    }

    @Test
    fun `mapSystemHealth handles unknown status`() {
        val dto = SystemHealthResponseDto(
            status = "healthy",
            checkedAt = "2026-11-04T20:00:00Z",
            application = ApplicationHealthDto("healthy", "dev", 100),
            components = mapOf(
                "database" to ComponentHealthDto("healthy", mapOf("latency_ms" to "5")),
                "scheduler" to ComponentHealthDto("unknown", mapOf("error" to "Check timed out or failed"))
            )
        )

        val result = SystemHealthMapper.mapSystemHealth(dto)

        assertEquals(2, result.components.size)
        val scheduler = result.components.find { it.name == "scheduler" }
        assertNotNull(scheduler)
        assertEquals("unknown", scheduler!!.status)
    }
}
