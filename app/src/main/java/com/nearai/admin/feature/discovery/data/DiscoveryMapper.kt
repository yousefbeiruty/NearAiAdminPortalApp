package com.nearai.admin.feature.discovery.data

import com.nearai.admin.feature.discovery.domain.CategoryAnalytics
import com.nearai.admin.feature.discovery.domain.ConversionFunnelData
import com.nearai.admin.feature.discovery.domain.DiscoveryOverviewData
import com.nearai.admin.feature.discovery.domain.FunnelStep
import com.nearai.admin.feature.discovery.domain.MetricComparison
import com.nearai.admin.feature.discovery.domain.PopularCategoriesData
import com.nearai.admin.feature.discovery.domain.PopularProductsData
import com.nearai.admin.feature.discovery.domain.ProductAnalytics
import com.nearai.admin.feature.discovery.domain.SearchAnalyticsData
import com.nearai.admin.feature.discovery.domain.SearchTerm

/**
 * Maps discovery analytics DTOs to domain models.
 */
object DiscoveryMapper {

    fun mapDiscoveryOverview(dto: DiscoveryOverviewResponseDto): DiscoveryOverviewData {
        return DiscoveryOverviewData(
            periodStart = dto.period?.start ?: "",
            periodEnd = dto.period?.end ?: "",
            dailyActiveUsers = mapMetricComparison(dto.dailyActiveUsers),
            weeklyActiveUsers = mapMetricComparison(dto.weeklyActiveUsers),
            totalSearches = mapMetricComparison(dto.totalSearches),
            totalProductViews = mapMetricComparison(dto.totalProductViews),
            totalCartAdds = mapMetricComparison(dto.totalCartAdds),
            totalPurchases = mapMetricComparison(dto.totalPurchases),
            conversionRate = mapMetricComparison(dto.conversionRate)
        )
    }

    fun mapMetricComparison(dto: MetricComparisonDto?): MetricComparison {
        return MetricComparison(
            current = dto?.current ?: 0.0,
            previous = dto?.previous ?: 0.0,
            changePct = dto?.changePct ?: 0.0
        )
    }

    fun mapSearchAnalytics(dto: SearchAnalyticsResponseDto): SearchAnalyticsData {
        return SearchAnalyticsData(
            totalSearches = dto.totalSearches,
            zeroResultSearches = dto.zeroResultSearches,
            zeroResultRatePct = dto.zeroResultRatePct,
            avgResultsPerSearch = dto.avgResultsPerSearch,
            avgLatencyMs = dto.avgLatencyMs,
            popularTerms = dto.popularTerms.map { mapSearchTerm(it) },
            zeroResultTerms = dto.zeroResultTerms.map { mapSearchTerm(it) },
            totalPages = dto.pagination?.total ?: 0
        )
    }

    fun mapSearchTerm(dto: SearchTermDto): SearchTerm {
        return SearchTerm(
            query = dto.query,
            count = dto.count,
            avgResults = dto.avgResults
        )
    }

    fun mapPopularProducts(dto: PopularProductsResponseDto): PopularProductsData {
        return PopularProductsData(
            metric = dto.metric,
            periodDays = dto.periodDays,
            products = dto.products.map { mapProductAnalytics(it) }
        )
    }

    fun mapProductAnalytics(dto: ProductAnalyticsDto): ProductAnalytics {
        return ProductAnalytics(
            productId = dto.productId,
            name = dto.name,
            categoryName = dto.categoryName,
            count = dto.count,
            uniqueViewers = dto.uniqueViewers,
            cartAddRatePct = dto.cartAddRatePct
        )
    }

    fun mapPopularCategories(dto: PopularCategoriesResponseDto): PopularCategoriesData {
        return PopularCategoriesData(
            periodDays = dto.periodDays,
            categories = dto.categories.map { mapCategoryAnalytics(it) }
        )
    }

    fun mapCategoryAnalytics(dto: CategoryAnalyticsDto): CategoryAnalytics {
        return CategoryAnalytics(
            categoryId = dto.categoryId,
            name = dto.name,
            views = dto.views,
            productViews = dto.productViews,
            cartAdds = dto.cartAdds,
            purchases = dto.purchases
        )
    }

    fun mapConversionFunnel(dto: ConversionFunnelResponseDto): ConversionFunnelData {
        return ConversionFunnelData(
            periodDays = dto.periodDays,
            steps = dto.steps.map { mapFunnelStep(it) },
            overallConversionPct = dto.overallConversionPct
        )
    }

    fun mapFunnelStep(dto: FunnelStepDto): FunnelStep {
        return FunnelStep(
            step = dto.step,
            count = dto.count,
            rate = dto.rate
        )
    }
}
