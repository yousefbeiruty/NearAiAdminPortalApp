package com.nearai.admin.feature.discovery.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for discovery analytics endpoints.
 *
 * Backend contract (from audit document):
 * - GET /api/v1/admin/analytics/discovery/overview  — Discovery overview with comparison
 * - GET /api/v1/admin/analytics/search              — Search analytics
 * - GET /api/v1/admin/analytics/products/popular    — Popular products
 * - GET /api/v1/admin/analytics/categories/popular  — Popular categories
 * - GET /api/v1/admin/analytics/funnel              — Conversion funnel
 */
interface DiscoveryApiService {

    @GET("api/v1/admin/analytics/discovery/overview")
    suspend fun getDiscoveryOverview(
        @Query("days") days: Int = 7,
        @Query("compare") compare: Boolean = true
    ): DiscoveryOverviewResponseDto

    @GET("api/v1/admin/analytics/search")
    suspend fun getSearchAnalytics(
        @Query("days") days: Int = 7,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): SearchAnalyticsResponseDto

    @GET("api/v1/admin/analytics/products/popular")
    suspend fun getPopularProducts(
        @Query("days") days: Int = 7,
        @Query("metric") metric: String = "views",
        @Query("size") size: Int = 20
    ): PopularProductsResponseDto

    @GET("api/v1/admin/analytics/categories/popular")
    suspend fun getPopularCategories(
        @Query("days") days: Int = 7,
        @Query("size") size: Int = 20
    ): PopularCategoriesResponseDto

    @GET("api/v1/admin/analytics/funnel")
    suspend fun getConversionFunnel(
        @Query("days") days: Int = 7
    ): ConversionFunnelResponseDto
}

// =============================================================================
// Discovery Overview DTOs
// =============================================================================

@Serializable
data class DiscoveryOverviewResponseDto(
    @SerialName("period") val period: PeriodDto? = null,
    @SerialName("previous_period") val previousPeriod: PeriodDto? = null,
    @SerialName("daily_active_users") val dailyActiveUsers: MetricComparisonDto? = null,
    @SerialName("weekly_active_users") val weeklyActiveUsers: MetricComparisonDto? = null,
    @SerialName("total_searches") val totalSearches: MetricComparisonDto? = null,
    @SerialName("total_product_views") val totalProductViews: MetricComparisonDto? = null,
    @SerialName("total_cart_adds") val totalCartAdds: MetricComparisonDto? = null,
    @SerialName("total_purchases") val totalPurchases: MetricComparisonDto? = null,
    @SerialName("conversion_rate") val conversionRate: MetricComparisonDto? = null
)

@Serializable
data class PeriodDto(
    @SerialName("start") val start: String = "",
    @SerialName("end") val end: String = ""
)

@Serializable
data class MetricComparisonDto(
    @SerialName("current") val current: Double = 0.0,
    @SerialName("previous") val previous: Double = 0.0,
    @SerialName("change_pct") val changePct: Double = 0.0
)

// =============================================================================
// Search Analytics DTOs
// =============================================================================

@Serializable
data class SearchAnalyticsResponseDto(
    @SerialName("total_searches") val totalSearches: Int = 0,
    @SerialName("zero_result_searches") val zeroResultSearches: Int = 0,
    @SerialName("zero_result_rate_pct") val zeroResultRatePct: Double = 0.0,
    @SerialName("avg_results_per_search") val avgResultsPerSearch: Double = 0.0,
    @SerialName("avg_latency_ms") val avgLatencyMs: Double = 0.0,
    @SerialName("popular_terms") val popularTerms: List<SearchTermDto> = emptyList(),
    @SerialName("zero_result_terms") val zeroResultTerms: List<SearchTermDto> = emptyList(),
    @SerialName("pagination") val pagination: PaginationDto? = null
)

@Serializable
data class SearchTermDto(
    @SerialName("query") val query: String = "",
    @SerialName("count") val count: Int = 0,
    @SerialName("avg_results") val avgResults: Double? = null
)

@Serializable
data class PaginationDto(
    @SerialName("page") val page: Int = 1,
    @SerialName("size") val size: Int = 20,
    @SerialName("total") val total: Int = 0
)

// =============================================================================
// Popular Products DTOs
// =============================================================================

@Serializable
data class PopularProductsResponseDto(
    @SerialName("metric") val metric: String = "views",
    @SerialName("period_days") val periodDays: Int = 7,
    @SerialName("products") val products: List<ProductAnalyticsDto> = emptyList()
)

@Serializable
data class ProductAnalyticsDto(
    @SerialName("product_id") val productId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("category_name") val categoryName: String = "",
    @SerialName("count") val count: Int = 0,
    @SerialName("unique_viewers") val uniqueViewers: Int = 0,
    @SerialName("cart_add_rate_pct") val cartAddRatePct: Double = 0.0
)

// =============================================================================
// Popular Categories DTOs
// =============================================================================

@Serializable
data class PopularCategoriesResponseDto(
    @SerialName("period_days") val periodDays: Int = 7,
    @SerialName("categories") val categories: List<CategoryAnalyticsDto> = emptyList()
)

@Serializable
data class CategoryAnalyticsDto(
    @SerialName("category_id") val categoryId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("views") val views: Int = 0,
    @SerialName("product_views") val productViews: Int = 0,
    @SerialName("cart_adds") val cartAdds: Int = 0,
    @SerialName("purchases") val purchases: Int = 0
)

// =============================================================================
// Conversion Funnel DTOs
// =============================================================================

@Serializable
data class ConversionFunnelResponseDto(
    @SerialName("period_days") val periodDays: Int = 7,
    @SerialName("steps") val steps: List<FunnelStepDto> = emptyList(),
    @SerialName("overall_conversion_pct") val overallConversionPct: Double = 0.0
)

@Serializable
data class FunnelStepDto(
    @SerialName("step") val step: String = "",
    @SerialName("count") val count: Int = 0,
    @SerialName("rate") val rate: Double = 0.0
)
