package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.CurrencyAmount
import com.nearai.admin.feature.dashboard.domain.DailyOrderCounts
import com.nearai.admin.feature.dashboard.domain.FinancialOverview
import com.nearai.admin.feature.dashboard.domain.LowStockAlert
import com.nearai.admin.feature.dashboard.domain.OrderCounts
import com.nearai.admin.feature.dashboard.domain.OperationalOverview
import com.nearai.admin.feature.dashboard.domain.PlatformDashboardData
import com.nearai.admin.feature.dashboard.domain.RecentOrder
import com.nearai.admin.feature.dashboard.domain.RecentRegistration
import com.nearai.admin.feature.dashboard.domain.RevenueByCurrency
import com.nearai.admin.feature.dashboard.domain.TenantCounts
import com.nearai.admin.feature.dashboard.domain.UserCounts

/**
 * Maps dashboard DTOs to domain models.
 */
object DashboardMapper {

    fun mapDashboardData(dto: DashboardResponseDto): DashboardData {
        return DashboardData(
            totalUsers = dto.totalUsers,
            totalProducts = dto.totalProducts,
            totalCategories = dto.totalCategories,
            totalOrders = dto.totalOrders,
            pendingOrders = dto.pendingOrders,
            completedOrders = dto.completedOrders,
            cancelledOrders = dto.cancelledOrders,
            rejectedOrders = dto.rejectedOrders,
            totalRevenue = dto.totalRevenue,
            revenueCurrency = dto.revenueCurrency,
            recentOrders = dto.recentOrders.map { mapRecentOrder(it) },
            recentRegistrations = dto.recentRegistrations.map { mapRecentRegistration(it) },
            lowStockAlerts = dto.lowStockAlerts.map { mapLowStockAlert(it) }
        )
    }

    fun mapRevenue(dto: RevenueDto): RevenueByCurrency {
        return RevenueByCurrency(
            currency = dto.currency,
            amount = dto.amount
        )
    }

    fun mapRecentOrder(dto: RecentOrderDto): RecentOrder {
        return RecentOrder(
            id = dto.id,
            userName = dto.userName,
            orderDate = dto.orderDate,
            status = dto.status,
            total = dto.total,
            currency = dto.currency
        )
    }

    fun mapRecentRegistration(dto: RecentRegistrationDto): RecentRegistration {
        return RecentRegistration(
            id = dto.id,
            email = dto.email,
            role = dto.role,
            registrationDate = dto.registrationDate
        )
    }

    fun mapLowStockAlert(dto: LowStockAlertDto): LowStockAlert {
        return LowStockAlert(
            variantId = dto.variantId,
            productName = dto.productName,
            sku = dto.sku,
            onHand = dto.onHand,
            reserved = dto.reserved,
            available = dto.available
        )
    }

    // ─── Platform Dashboard Mapping ─────────────────────────────────────────

    fun mapPlatformDashboardData(dto: PlatformDashboardResponseDto): PlatformDashboardData {
        return PlatformDashboardData(
            tenants = mapTenantCounts(dto.tenants),
            users = mapUserCounts(dto.users),
            orders = mapOrderCounts(dto.orders),
            dailyOrders = mapDailyOrderCounts(dto.dailyOrders),
            financial = mapFinancialOverview(dto.financial),
            operational = mapOperationalOverview(dto.operational)
        )
    }

    fun mapTenantCounts(dto: TenantCountsDto): TenantCounts {
        return TenantCounts(
            total = dto.total,
            active = dto.active,
            pending = dto.pending,
            suspended = dto.suspended,
            deleted = dto.deleted
        )
    }

    fun mapUserCounts(dto: UserCountsDto): UserCounts {
        return UserCounts(
            total = dto.total,
            platformAdmins = dto.platformAdmins,
            merchantAdmins = dto.merchantAdmins,
            customers = dto.customers
        )
    }

    fun mapOrderCounts(dto: OrderCountsDto): OrderCounts {
        return OrderCounts(
            total = dto.total,
            pending = dto.pending,
            completed = dto.completed,
            cancelled = dto.cancelled,
            rejected = dto.rejected
        )
    }

    fun mapDailyOrderCounts(dto: DailyOrderCountsDto): DailyOrderCounts {
        return DailyOrderCounts(
            today = dto.today,
            currentMonth = dto.currentMonth
        )
    }

    fun mapCurrencyAmount(dto: CurrencyAmountDto): CurrencyAmount {
        return CurrencyAmount(
            currency = dto.currency,
            amount = dto.amount
        )
    }

    fun mapFinancialOverview(dto: FinancialOverviewDto): FinancialOverview {
        return FinancialOverview(
            revenueByCurrency = dto.revenueByCurrency.map { mapCurrencyAmount(it) },
            paymentTotalsByCurrency = dto.paymentTotalsByCurrency.map { mapCurrencyAmount(it) },
            codTotalsByCurrency = dto.codTotalsByCurrency.map { mapCurrencyAmount(it) }
        )
    }

    fun mapOperationalOverview(dto: OperationalOverviewDto): OperationalOverview {
        return OperationalOverview(
            pendingApprovals = dto.pendingApprovals,
            failedJobs = dto.failedJobs,
            pendingJobs = dto.pendingJobs
        )
    }
}
