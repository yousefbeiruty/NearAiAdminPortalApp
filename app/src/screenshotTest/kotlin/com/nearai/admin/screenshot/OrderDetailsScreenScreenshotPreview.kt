package com.nearai.admin.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.feature.orders.domain.StatusHistoryEntry
import com.nearai.admin.feature.orders.presentation.OrderDetailsScreenContent
import com.nearai.admin.feature.orders.presentation.OrderDetailsUiState
import com.nearai.admin.test.TestDataFactory

/**
 * Screenshot previews for the Order Details screen.
 *
 * These previews use the actual production [OrderDetailsScreenContent] composable
 * with deterministic test data so that visual regressions are detectable across builds.
 *
 * Includes the required test case: status=pending, fulfillment_status=out_for_delivery
 * to verify that order status and fulfillment progress display independently.
 */
@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Order Details Pending OutForDelivery")
@Composable
fun OrderDetailsPendingOutForDeliveryPreview() {
    NearAiAdminTheme(darkTheme = false) {
        val order = TestDataFactory.orderDetailsPendingOutForDelivery()
        val history = TestDataFactory.statusHistoryPopulated()
        val uiState = OrderDetailsUiState(
            order = order,
            statusHistory = history
        )
        OrderDetailsScreenContent(
            uiState = uiState,
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Order Details Completed Delivered")
@Composable
fun OrderDetailsCompletedDeliveredPreview() {
    NearAiAdminTheme(darkTheme = false) {
        val order = TestDataFactory.orderDetailsCompletedDelivered()
        val uiState = OrderDetailsUiState(
            order = order,
            statusHistory = emptyList()
        )
        OrderDetailsScreenContent(
            uiState = uiState,
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Order Details Loading")
@Composable
fun OrderDetailsLoadingPreview() {
    NearAiAdminTheme(darkTheme = false) {
        OrderDetailsScreenContent(
            uiState = OrderDetailsUiState(isLoading = true),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Order Details Error")
@Composable
fun OrderDetailsErrorPreview() {
    NearAiAdminTheme(darkTheme = false) {
        OrderDetailsScreenContent(
            uiState = OrderDetailsUiState(error = "Unable to load order details. Please try again."),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}
