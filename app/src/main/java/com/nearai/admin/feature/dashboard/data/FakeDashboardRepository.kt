package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.dashboard.domain.ActivityEvent
import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.DashboardRepository
import com.nearai.admin.feature.dashboard.domain.KpiMetric
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Development-only fake implementation of [DashboardRepository].
 *
 * ⚠️ Returns placeholder data. DO NOT USE IN PRODUCTION.
 * Replace with a real repository when the backend API is connected.
 */
@Singleton
class FakeDashboardRepository @Inject constructor() : DashboardRepository {

    override suspend fun getDashboardData(): Result<DashboardData> {
        return try {
            delay(600) // Simulate network latency

            val kpiMetrics = listOf(
                KpiMetric("TOTAL USERS", "14,820", "↗ +12.4% vs last wk", true, "People"),
                KpiMetric("ACTIVE (DAU)", "3,410", "↗ +8.2% vs yest.", true, "Bolt"),
                KpiMetric("AI REQUESTS", "84.6k", "99.4% ok · 512 err", true, "Memory"),
                KpiMetric("AI SPEND", "$428.50", "● 71% of $600 cap", false, "AccountBalanceWallet"),
                KpiMetric("PLACES FOUND", "192.4k", "↑ +18% queries", true, "Explore"),
                KpiMetric("EST. REVENUE", "$4,820", "Pro + 18 trials", true, "CreditCard")
            )

            val activityEvents = listOf(
                ActivityEvent(
                    id = "evt-1",
                    title = "New Pro Subscription",
                    subtitle = "sarah.k@gmail.com · Annual Plan",
                    timestamp = "4m ago",
                    statusLabel = "Active",
                    statusType = "success"
                ),
                ActivityEvent(
                    id = "evt-2",
                    title = "AI Budget Soft Cap (70%)",
                    subtitle = "Google Cloud Run / OpenAI Routing",
                    timestamp = "28m ago",
                    statusLabel = "Warning",
                    statusType = "warning"
                ),
                ActivityEvent(
                    id = "evt-3",
                    title = "Failed Discovery Query",
                    subtitle = "Places timeout (Tokyo, JP) · status 504",
                    timestamp = "1h ago",
                    statusLabel = "Investigate",
                    statusType = "error"
                ),
                ActivityEvent(
                    id = "evt-4",
                    title = "Batch User Import",
                    subtitle = "142 new signups from ProductHunt",
                    timestamp = "2h ago",
                    statusLabel = "Success",
                    statusType = "success"
                )
            )

            Result.Success(
                DashboardData(
                    kpiMetrics = kpiMetrics,
                    activityEvents = activityEvents,
                    lastUpdated = "2m ago",
                    clusterStatus = "100% Operational"
                )
            )
        } catch (e: Exception) {
            Result.Error(com.nearai.admin.core.common.AppError.UnknownError(e.message ?: "Failed to load dashboard"))
        }
    }
}
