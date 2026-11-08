package com.nearai.admin.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.feature.orders.presentation.OrdersListScreenContent
import com.nearai.admin.feature.orders.presentation.OrdersListUiState
import com.nearai.admin.test.TestDataFactory

/**
 * Screenshot previews for the Orders List screen.
 *
 * These previews use the actual production [OrdersListScreenContent] composable
 * with deterministic test data so that visual regressions are detectable across builds.
 */
@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Orders List Loading")
@Composable
fun OrdersListLoadingPreview() {
    NearAiAdminTheme(darkTheme = false) {
        OrdersListScreenContent(
            uiState = OrdersListUiState(isLoading = true),
            onNavigateBack = {},
            onOrderClicked = {},
            onRetry = {},
            onOrderStatusFilter = {},
            onFulfillmentStatusFilter = {},
            onClearFilters = {},
            onLoadMore = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Orders List Populated")
@Composable
fun OrdersListPopulatedPreview() {
    NearAiAdminTheme(darkTheme = false) {
        val data = TestDataFactory.paginatedOrdersPopulated()
        val uiState = OrdersListUiState(
            orders = data.orders,
            currentPage = data.page,
            totalPages = data.totalPages,
            totalItems = data.totalItems,
            hasMore = data.page < data.totalPages
        )
        OrdersListScreenContent(
            uiState = uiState,
            onNavigateBack = {},
            onOrderClicked = {},
            onRetry = {},
            onOrderStatusFilter = {},
            onFulfillmentStatusFilter = {},
            onClearFilters = {},
            onLoadMore = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Orders List Empty")
@Composable
fun OrdersListEmptyPreview() {
    NearAiAdminTheme(darkTheme = false) {
        val data = TestDataFactory.paginatedOrdersEmpty()
        val uiState = OrdersListUiState(
            orders = data.orders,
            currentPage = data.page,
            totalPages = data.totalPages,
            totalItems = data.totalItems
        )
        OrdersListScreenContent(
            uiState = uiState,
            onNavigateBack = {},
            onOrderClicked = {},
            onRetry = {},
            onOrderStatusFilter = {},
            onFulfillmentStatusFilter = {},
            onClearFilters = {},
            onLoadMore = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Orders List Error")
@Composable
fun OrdersListErrorPreview() {
    NearAiAdminTheme(darkTheme = false) {
        OrdersListScreenContent(
            uiState = OrdersListUiState(error = "Unable to load orders. Please try again."),
            onNavigateBack = {},
            onOrderClicked = {},
            onRetry = {},
            onOrderStatusFilter = {},
            onFulfillmentStatusFilter = {},
            onClearFilters = {},
            onLoadMore = {}
        )
    }
}
