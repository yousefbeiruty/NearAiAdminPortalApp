package com.nearai.admin.feature.dashboard.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model for a single KPI metric displayed on the dashboard.
 */
data class KpiMetric(
    val title: String,
    val value: String,
    val delta: String,
    val isPositive: Boolean,
    val iconLabel: String
)

/**
 * Domain model for a live activity stream event.
 */
data class ActivityEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val statusLabel: String,
    val statusType: String // "success", "warning", "error", "info"
)

/**
 * Aggregated dashboard data returned by the repository.
 */
data class DashboardData(
    val kpiMetrics: List<KpiMetric>,
    val activityEvents: List<ActivityEvent>,
    val lastUpdated: String,
    val clusterStatus: String
)

/**
 * Repository interface for dashboard data.
 * Belongs in the domain layer so the presentation layer depends on an abstraction.
 */
interface DashboardRepository {

    /**
     * Fetches the current dashboard data from the backend.
     */
    suspend fun getDashboardData(): Result<DashboardData>
}
