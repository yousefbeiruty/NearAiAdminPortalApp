package com.nearai.admin.feature.aiusage.domain

import com.nearai.admin.core.common.Result

/**
 * Repository interface for AI usage data.
 * Belongs in the domain layer so the presentation layer depends on an abstraction.
 *
 * Note: The backend AI usage API is not yet implemented. The repository implementation
 * returns an unavailable state rather than fabricated data.
 */
interface AiUsageRepository {

    /**
     * Fetches the current AI usage data.
     *
     * Returns [Result.Error] with an appropriate message if the feature is not yet available.
     */
    suspend fun getAiUsageData(): Result<AiUsageData>
}

/**
 * Aggregated AI usage data returned by the repository.
 * This model is a placeholder for when the backend API becomes available.
 */
data class AiUsageData(
    val monthlyBudgetConsumed: Double = 0.0,
    val monthlyBudgetTotal: Double = 0.0,
    val providerUsage: List<ProviderUsage> = emptyList(),
    val quotaSafeguards: List<QuotaSafeguard> = emptyList(),
    val recentTelemetry: List<TelemetryEntry> = emptyList()
)

/**
 * AI provider usage breakdown.
 */
data class ProviderUsage(
    val provider: String,
    val model: String,
    val requestCount: Int,
    val percentageOfTotal: Int,
    val cost: Double
)

/**
 * A quota safeguard configuration.
 */
data class QuotaSafeguard(
    val title: String,
    val value: String,
    val description: String,
    val status: String
)

/**
 * A recent AI inference telemetry entry.
 */
data class TelemetryEntry(
    val model: String,
    val tokenCount: Int,
    val cost: Double,
    val latencyMs: Long,
    val status: String
)
