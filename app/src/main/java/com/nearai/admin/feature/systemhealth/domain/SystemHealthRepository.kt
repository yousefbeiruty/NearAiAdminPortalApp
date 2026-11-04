package com.nearai.admin.feature.systemhealth.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model for application-level health information.
 */
data class ApplicationHealth(
    val status: String,
    val environment: String,
    val uptimeSeconds: Long
)

/**
 * Domain model for a single component health check.
 */
data class ComponentHealth(
    val name: String,
    val status: String,
    val details: Map<String, String>? = null
)

/**
 * Domain model for the complete system health report.
 */
data class SystemHealthData(
    val overallStatus: String,
    val checkedAt: String,
    val application: ApplicationHealth,
    val components: List<ComponentHealth>
)

/**
 * Repository interface for system health operations.
 */
interface SystemHealthRepository {

    /**
     * Fetches the current system health status from the backend.
     */
    suspend fun getSystemHealth(): Result<SystemHealthData>
}
