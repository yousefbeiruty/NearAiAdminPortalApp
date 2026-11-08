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
 * Compose UI tests for [OrderDetailsScreenContent].
 *
 * Verifies that the stateless order details screen renders correctly for loading,
 * populated, error, and various status states. Specifically tests that order status
 * and fulfillment status render independently (e.g., pending + out_for_delivery).
 */
@RunWith(AndroidJUnit4::class)
class OrderDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun orderDetailsScreenContent_pendingOutForDelivery_rendersIndependently() {
        // Given — pending order with out_for_delivery fulfillment
        val order = TestDataFactory.orderDetailsPendingOutForDelivery()
        val history = TestDataFactory.statusHistoryPopulated()
        val uiState = OrderDetailsUiState(
            order = order,
            statusHistory = history
        )

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — content is visible with both status badges rendered independently
        composeRule.onNodeWithTag("orderDetailsContent").assertExists()
        // Order status badge shows "Order: Pending" (Surface merges semantics, so use unmerged tree)
        composeRule.onNodeWithText("Order: Pending", useUnmergedTree = true).assertExists()
        // Fulfillment status badge shows "Fulfillment: Out for Delivery"
        composeRule.onNodeWithText("Fulfillment: Out for Delivery", useUnmergedTree = true).assertExists()
    }

    @Test
    fun orderDetailsScreenContent_completedDelivered_rendersCorrectly() {
        // Given — completed and delivered order
        val order = TestDataFactory.orderDetailsCompletedDelivered()
        val uiState = OrderDetailsUiState(
            order = order,
            statusHistory = emptyList()
        )

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — content shows completed and delivered
        composeRule.onNodeWithTag("orderDetailsContent").assertExists()
        composeRule.onNodeWithText("Order: Completed", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Fulfillment: Delivered", useUnmergedTree = true).assertExists()
    }

    @Test
    fun orderDetailsScreenContent_loadingState_rendersLoadingIndicator() {
        // Given — loading state
        val uiState = OrderDetailsUiState(isLoading = true)

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — loading indicator is visible
        composeRule.onNodeWithTag("orderDetailsLoading").assertExists()
    }

    @Test
    fun orderDetailsScreenContent_errorState_rendersRetryButton() {
        // Given — error state
        val uiState = OrderDetailsUiState(error = "Failed to load order")

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — error state with retry button
        composeRule.onNodeWithTag("orderDetailsError").assertExists()
        composeRule.onNodeWithTag("orderDetailsRetryButton").assertExists()
    }

    @Test
    fun orderDetailsScreenContent_errorState_retryInvokesCallback() {
        // Given — error state
        val uiState = OrderDetailsUiState(error = "Failed to load order")
        var retryInvoked = false

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = { retryInvoked = true }
                )
            }
        }

        // Then — clicking retry invokes callback
        composeRule.onNodeWithTag("orderDetailsRetryButton").performClick()
        assert(retryInvoked) { "onRetry should have been invoked" }
    }

    @Test
    fun orderDetailsScreenContent_populatedState_rendersOrderInfo() {
        // Given — populated order
        val order = TestDataFactory.orderDetailsPendingOutForDelivery()
        val uiState = OrderDetailsUiState(order = order)

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — order information is visible
        composeRule.onNodeWithTag("orderDetailsContent").assertExists()
        // Recipient name is rendered
        composeRule.onNodeWithText(order.deliveryRecipientName!!).assertExists()
        // City is rendered
        composeRule.onNodeWithText(order.deliveryCity!!).assertExists()
    }

    @Test
    fun orderDetailsScreenContent_cancelledOrder_rendersTerminalState() {
        // Given — cancelled order
        val order = TestDataFactory.orderDetailsCompletedDelivered().copy(
            status = OrderStatus.CANCELLED,
            fulfillmentStatus = FulfillmentStatus.UNFULFILLED
        )
        val uiState = OrderDetailsUiState(order = order)

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — cancelled status is visible
        composeRule.onNodeWithText("Order: Cancelled", useUnmergedTree = true).assertExists()
    }

    @Test
    fun orderDetailsScreenContent_rejectedOrder_rendersTerminalState() {
        // Given — rejected order
        val order = TestDataFactory.orderDetailsCompletedDelivered().copy(
            status = OrderStatus.REJECTED,
            fulfillmentStatus = FulfillmentStatus.UNFULFILLED
        )
        val uiState = OrderDetailsUiState(order = order)

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                OrderDetailsScreenContent(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetry = {}
                )
            }
        }

        // Then — rejected status is visible
        composeRule.onNodeWithText("Order: Rejected", useUnmergedTree = true).assertExists()
    }
}
