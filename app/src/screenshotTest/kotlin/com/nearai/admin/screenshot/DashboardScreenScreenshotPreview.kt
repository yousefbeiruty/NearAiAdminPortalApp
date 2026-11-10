package com.nearai.admin.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.feature.dashboard.presentation.DashboardScreenContent
import com.nearai.admin.feature.dashboard.presentation.DashboardUiState
import com.nearai.admin.test.TestDataFactory
import java.util.Locale

/**
 * Screenshot previews for the Dashboard screen.
 *
 * These previews use the actual production [DashboardScreenContent] composable
 * with deterministic test data so that visual regressions are detectable across builds.
 */
@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Dashboard Loading")
@Composable
fun DashboardScreenLoadingPreview() {
    NearAiAdminTheme(darkTheme = false) {
        DashboardScreenContent(
            uiState = DashboardUiState(isLoading = true),
            onRetry = {},
            onRefresh = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Dashboard Populated")
@Composable
fun DashboardScreenPopulatedPreview() {
    NearAiAdminTheme(darkTheme = false) {
        val data = TestDataFactory.dashboardDataPopulated()
        val uiState = DashboardUiState(
            totalUsers = data.totalUsers,
            totalProducts = data.totalProducts,
            totalCategories = data.totalCategories,
            totalOrders = data.totalOrders,
            pendingOrders = data.pendingOrders,
            completedOrders = data.completedOrders,
            cancelledOrders = data.cancelledOrders,
            rejectedOrders = data.rejectedOrders,
            totalRevenue = data.totalRevenue,
            revenueCurrency = data.revenueCurrency,
            revenueByCurrency = data.revenueByCurrency.map { rev ->
                com.nearai.admin.feature.dashboard.presentation.RevenueItem(
                    currency = rev.currency,
                    amount = rev.amount,
                    formattedAmount = String.format(Locale.US, "%.2f %s", rev.amount, rev.currency)
                )
            },
            recentOrders = data.recentOrders.map { order ->
                com.nearai.admin.feature.dashboard.presentation.RecentOrderItem(
                    id = order.id,
                    userName = order.userName,
                    orderDate = order.orderDate,
                    status = order.status,
                    total = order.total,
                    currency = order.currency,
                    formattedTotal = String.format(Locale.US, "%.2f %s", order.total, order.currency)
                )
            },
            recentRegistrations = data.recentRegistrations.map { reg ->
                com.nearai.admin.feature.dashboard.presentation.RecentRegistrationItem(
                    id = reg.id,
                    email = reg.email,
                    role = reg.role,
                    registrationDate = reg.registrationDate
                )
            },
            lowStockAlerts = data.lowStockAlerts.map { alert ->
                com.nearai.admin.feature.dashboard.presentation.LowStockAlertItem(
                    variantId = alert.variantId,
                    productName = alert.productName,
                    sku = alert.sku,
                    onHand = alert.onHand,
                    reserved = alert.reserved,
                    available = alert.available
                )
            }
        )
        DashboardScreenContent(
            uiState = uiState,
            onRetry = {},
            onRefresh = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Dashboard Error")
@Composable
fun DashboardScreenErrorPreview() {
    NearAiAdminTheme(darkTheme = false) {
        DashboardScreenContent(
            uiState = DashboardUiState(error = "Unable to load dashboard data. Please try again."),
            onRetry = {},
            onRefresh = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Dashboard Empty")
@Composable
fun DashboardScreenEmptyPreview() {
    NearAiAdminTheme(darkTheme = false) {
        DashboardScreenContent(
            uiState = DashboardUiState(),
            onRetry = {},
            onRefresh = {}
        )
    }
}
