package com.nearai.admin.feature.orders.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.test.TestDataFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [OrdersListScreenContent].
 *
 * Verifies that the stateless orders list renders correctly for loading,
 * populated, error, and empty states, and that user interactions invoke the
 * expected callbacks.
 */
@RunWith(AndroidJUnit4::class)
class OrdersListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ordersListScreenContent_populatedState_rendersOrderCards() {
        // Given — populated UI state
        val data = TestDataFactory.paginatedOrdersPopulated()
        val uiState = OrdersListUiState(
            orders = data.orders,
            currentPage = data.page,
            totalPages = data.totalPages,
            totalItems = data.totalItems
        )

        // When
        composeRule.setContent {
            NearAiAdminTheme {
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

        // Then — content is visible with order cards
        composeRule.onNodeWithTag("ordersContent").assertExists()
        // First order card is rendered
        composeRule.onNodeWithTag("orderCard_${data.orders[0].id}").assertExists()
    }

    @Test
    fun ordersListScreenContent_populatedState_orderClickInvokesCallback() {
        // Given — populated UI state
        val data = TestDataFactory.paginatedOrdersPopulated()
        val uiState = OrdersListUiState(
            orders = data.orders,
            currentPage = data.page,
            totalPages = data.totalPages,
            totalItems = data.totalItems
        )
        var clickedOrderId: String? = null

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrdersListScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onOrderClicked = { clickedOrderId = it },
                    onRetry = {},
                    onOrderStatusFilter = {},
                    onFulfillmentStatusFilter = {},
                    onClearFilters = {},
                    onLoadMore = {}
                )
            }
        }

        // Then — clicking order card invokes callback
        composeRule.onNodeWithTag("orderCard_${data.orders[0].id}").performClick()
        assert(clickedOrderId == data.orders[0].id) {
            "Expected ${data.orders[0].id}, got $clickedOrderId"
        }
    }

    @Test
    fun ordersListScreenContent_loadingState_rendersLoadingIndicator() {
        // Given — loading state
        val uiState = OrdersListUiState(isLoading = true)

        // When
        composeRule.setContent {
            NearAiAdminTheme {
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

        // Then — loading indicator is visible
        composeRule.onNodeWithTag("ordersLoading").assertExists()
    }

    @Test
    fun ordersListScreenContent_errorState_rendersRetryButton() {
        // Given — error state
        val uiState = OrdersListUiState(error = "Network error")

        // When
        composeRule.setContent {
            NearAiAdminTheme {
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

        // Then — error state with retry button
        composeRule.onNodeWithTag("ordersError").assertExists()
        composeRule.onNodeWithTag("ordersRetryButton").assertExists()
    }

    @Test
    fun ordersListScreenContent_errorState_retryInvokesCallback() {
        // Given — error state
        val uiState = OrdersListUiState(error = "Network error")
        var retryInvoked = false

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrdersListScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onOrderClicked = {},
                    onRetry = { retryInvoked = true },
                    onOrderStatusFilter = {},
                    onFulfillmentStatusFilter = {},
                    onClearFilters = {},
                    onLoadMore = {}
                )
            }
        }

        // Then — clicking retry invokes callback
        composeRule.onNodeWithTag("ordersRetryButton").performClick()
        assert(retryInvoked) { "onRetry should have been invoked" }
    }

    @Test
    fun ordersListScreenContent_emptyState_rendersEmptyMessage() {
        // Given — empty state
        val data = TestDataFactory.paginatedOrdersEmpty()
        val uiState = OrdersListUiState(
            orders = data.orders,
            currentPage = data.page,
            totalPages = data.totalPages,
            totalItems = data.totalItems
        )

        // When
        composeRule.setContent {
            NearAiAdminTheme {
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

        // Then — empty state is visible
        composeRule.onNodeWithTag("ordersEmpty").assertExists()
    }
}
