package com.nearai.admin.feature.dashboard.data

import com.nearai.admin.feature.dashboard.domain.DashboardData
import com.nearai.admin.feature.dashboard.domain.LowStockAlert
import com.nearai.admin.feature.dashboard.domain.RecentOrder
import com.nearai.admin.feature.dashboard.domain.RecentRegistration
import com.nearai.admin.feature.dashboard.domain.RevenueByCurrency

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
}
