package com.nearai.admin.feature.systemhealth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET

/**
 * Retrofit service for the platform system health endpoint.
 *
 * Backend contract:
 * - GET /api/v1/platform/system-health — system health overview (platform admin only)
 */
interface SystemHealthApiService {

    @GET("api/v1/platform/system-health")
    suspend fun getSystemHealth(): SystemHealthResponseDto
}

// ─── DTOs ────────────────────────────────────────────────────────────────────

@Serializable
data class SystemHealthResponseDto(
    val status: String,
    @SerialName("checked_at")
    val checkedAt: String,
    val application: ApplicationHealthDto,
    val components: Map<String, ComponentHealthDto>
)

@Serializable
data class ApplicationHealthDto(
    val status: String,
    val environment: String,
    @SerialName("uptime_seconds")
    val uptimeSeconds: Long
)

@Serializable
data class ComponentHealthDto(
    val status: String,
    val details: Map<String, JsonElement>? = null
)
