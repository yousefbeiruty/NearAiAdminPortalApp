package com.nearai.admin.feature.discovery.data

import com.nearai.admin.feature.discovery.domain.*

/**
 * Maps business analytics DTOs to domain models.
 */
object BusinessAnalyticsMapper {

    fun mapBusinessOverview(dto: BusinessAnalyticsResponseDto): BusinessAnalyticsData {
        return BusinessAnalyticsData(
            startDate = dto.startDate,
            endDate = dto.endDate,
            days = dto.days,

            // Order analytics
            totalOrders = dto.totalOrders,
            ordersByStatus = dto.ordersByStatus.map { mapOrderStatusBreakdown(it) },
            dailyOrders = dto.dailyOrders.map { mapDailyOrderCount(it) },
            completedOrders = dto.completedOrders,
            cancelledOrders = dto.cancelledOrders,
            pendingOrders = dto.pendingOrders,
            rejectedOrders = dto.rejectedOrders,

            // Revenue analytics
            revenueByCurrency = dto.revenueByCurrency.map { mapRevenueByCurrency(it) },
            dailyRevenue = dto.dailyRevenue.map { mapDailyRevenue(it) },

            // Product analytics
            bestSellingProducts = dto.bestSellingProducts.map { mapBestSellingProduct(it) },
            categorySales = dto.categorySales.map { mapCategorySales(it) },

            // Customer analytics
            newRegistrations = dto.newRegistrations,
            dailyRegistrations = dto.dailyRegistrations.map { mapDailyUserRegistration(it) },
            activeCustomers = dto.activeCustomers,
            repeatCustomers = dto.repeatCustomers,

            // Period comparison
            compare = dto.compare,
            previousStartDate = dto.previousStartDate,
            previousEndDate = dto.previousEndDate,
            totalOrdersComparison = dto.totalOrdersComparison?.let { mapIntPeriodComparison(it) },
            completedOrdersComparison = dto.completedOrdersComparison?.let { mapIntPeriodComparison(it) },
            revenueComparison = dto.revenueComparison?.map { mapRevenuePeriodComparison(it) },
            newRegistrationsComparison = dto.newRegistrationsComparison?.let { mapIntPeriodComparison(it) },
            activeCustomersComparison = dto.activeCustomersComparison?.let { mapIntPeriodComparison(it) }
        )
    }

    private fun mapDailyOrderCount(dto: DailyOrderCountDto): DailyOrderCount {
        return DailyOrderCount(
            date = dto.date,
            count = dto.count
        )
    }

    private fun mapOrderStatusBreakdown(dto: OrderStatusBreakdownDto): OrderStatusBreakdown {
        return OrderStatusBreakdown(
            status = dto.status,
            count = dto.count
        )
    }

    private fun mapDailyRevenue(dto: DailyRevenueDto): DailyRevenue {
        return DailyRevenue(
            date = dto.date,
            amount = dto.amount,
            currency = dto.currency
        )
    }

    private fun mapRevenueByCurrency(dto: RevenueByCurrencyDto): RevenueByCurrency {
        return RevenueByCurrency(
            currency = dto.currency,
            totalAmount = dto.totalAmount,
            orderCount = dto.orderCount,
            avgOrderValue = dto.avgOrderValue
        )
    }

    private fun mapBestSellingProduct(dto: BestSellingProductDto): BestSellingProduct {
        return BestSellingProduct(
            productId = dto.productId,
            productName = dto.productName,
            sku = dto.sku,
            unitsSold = dto.unitsSold,
            revenue = dto.revenue,
            currency = dto.currency
        )
    }

    private fun mapCategorySales(dto: CategorySalesDto): CategorySales {
        return CategorySales(
            categoryId = dto.categoryId,
            categoryName = dto.categoryName,
            unitsSold = dto.unitsSold,
            revenue = dto.revenue,
            currency = dto.currency
        )
    }

    private fun mapDailyUserRegistration(dto: DailyUserRegistrationDto): DailyUserRegistration {
        return DailyUserRegistration(
            date = dto.date,
            count = dto.count
        )
    }

    private fun mapRevenuePeriodComparison(dto: PeriodComparisonDto): RevenuePeriodComparison {
        return RevenuePeriodComparison(
            current = dto.current,
            previous = dto.previous,
            percentageChange = dto.percentageChange
        )
    }

    private fun mapIntPeriodComparison(dto: IntPeriodComparisonDto): IntPeriodComparison {
        return IntPeriodComparison(
            current = dto.current,
            previous = dto.previous,
            percentageChange = dto.percentageChange
        )
    }
}
