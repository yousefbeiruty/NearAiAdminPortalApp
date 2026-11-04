package com.nearai.admin.feature.systemhealth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

/**
 * Retrofit service for the admin system health endpoint.
 *
 * Backend contract:
 * - GET /api/v1/admin/system-health — system health overview (admin-only)
 */
interface SystemHealthApiService {

    @GET("api/v1/admin/system-health")
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
    val details: Map<String, String>? = null
)
