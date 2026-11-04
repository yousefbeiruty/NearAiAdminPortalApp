package com.nearai.admin.feature.discovery.domain

import com.nearai.admin.core.common.Result

/**
 * Repository interface for discovery analytics data.
 * Belongs in the domain layer so the presentation layer depends on an abstraction.
 */
interface DiscoveryRepository {

    /**
     * Fetches the discovery overview with optional period comparison.
     */
    suspend fun getDiscoveryOverview(days: Int = 7, compare: Boolean = true): Result<DiscoveryOverviewData>

    /**
     * Fetches search analytics with popular and zero-result terms.
     */
    suspend fun getSearchAnalytics(days: Int = 7, page: Int = 1, size: Int = 20): Result<SearchAnalyticsData>

    /**
     * Fetches popular products by metric (views, cart_adds, purchases).
     */
    suspend fun getPopularProducts(days: Int = 7, metric: String = "views", size: Int = 20): Result<PopularProductsData>

    /**
     * Fetches popular categories with engagement metrics.
     */
    suspend fun getPopularCategories(days: Int = 7, size: Int = 20): Result<PopularCategoriesData>

    /**
     * Fetches the conversion funnel steps.
     */
    suspend fun getConversionFunnel(days: Int = 7): Result<ConversionFunnelData>
}

// =============================================================================
// Domain data classes
// =============================================================================

/**
 * Discovery overview with period comparison metrics.
 */
data class DiscoveryOverviewData(
    val periodStart: String = "",
    val periodEnd: String = "",
    val dailyActiveUsers: MetricComparison = MetricComparison(),
    val weeklyActiveUsers: MetricComparison = MetricComparison(),
    val totalSearches: MetricComparison = MetricComparison(),
    val totalProductViews: MetricComparison = MetricComparison(),
    val totalCartAdds: MetricComparison = MetricComparison(),
    val totalPurchases: MetricComparison = MetricComparison(),
    val conversionRate: MetricComparison = MetricComparison()
)

/**
 * A metric value with optional previous period comparison.
 */
data class MetricComparison(
    val current: Double = 0.0,
    val previous: Double = 0.0,
    val changePct: Double = 0.0
)

/**
 * Search analytics with terms and performance metrics.
 */
data class SearchAnalyticsData(
    val totalSearches: Int = 0,
    val zeroResultSearches: Int = 0,
    val zeroResultRatePct: Double = 0.0,
    val avgResultsPerSearch: Double = 0.0,
    val avgLatencyMs: Double = 0.0,
    val popularTerms: List<SearchTerm> = emptyList(),
    val zeroResultTerms: List<SearchTerm> = emptyList(),
    val totalPages: Int = 0
)

/**
 * A search term with frequency count.
 */
data class SearchTerm(
    val query: String = "",
    val count: Int = 0,
    val avgResults: Double? = null
)

/**
 * Popular products with engagement metrics.
 */
data class PopularProductsData(
    val metric: String = "views",
    val periodDays: Int = 7,
    val products: List<ProductAnalytics> = emptyList()
)

/**
 * A product with analytics metrics.
 */
data class ProductAnalytics(
    val productId: String = "",
    val name: String = "",
    val categoryName: String = "",
    val count: Int = 0,
    val uniqueViewers: Int = 0,
    val cartAddRatePct: Double = 0.0
)

/**
 * Popular categories with engagement metrics.
 */
data class PopularCategoriesData(
    val periodDays: Int = 7,
    val categories: List<CategoryAnalytics> = emptyList()
)

/**
 * A category with analytics metrics.
 */
data class CategoryAnalytics(
    val categoryId: String = "",
    val name: String = "",
    val views: Int = 0,
    val productViews: Int = 0,
    val cartAdds: Int = 0,
    val purchases: Int = 0
)

/**
 * Conversion funnel with step-by-step drop-off rates.
 */
data class ConversionFunnelData(
    val periodDays: Int = 7,
    val steps: List<FunnelStep> = emptyList(),
    val overallConversionPct: Double = 0.0
)

/**
 * A single step in the conversion funnel.
 */
data class FunnelStep(
    val step: String = "",
    val count: Int = 0,
    val rate: Double = 0.0
)
