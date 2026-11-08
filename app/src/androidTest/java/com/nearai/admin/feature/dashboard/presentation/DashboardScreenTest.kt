package com.nearai.admin.feature.dashboard.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme
import com.nearai.admin.test.TestDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [DashboardScreenContent].
 *
 * Verifies that the stateless dashboard content renders correctly for loading,
 * populated, error, and empty states, and that user interactions invoke the
 * expected callbacks.
 */
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var dashboardUiState: DashboardUiState

    @Before
    fun setup() {
        dashboardUiState = DashboardUiState()
    }

    @Test
    fun dashboardScreenContent_populatedState_rendersKpiCards() {
        // Given — populated UI state
        val data = TestDataFactory.dashboardDataPopulated()
        val uiState = DashboardUiState(
            totalUsers = data.totalUsers,
            totalProducts = data.totalProducts,
            totalCategories = data.totalCategories,
            totalOrders = data.totalOrders,
            pendingOrders = data.pendingOrders,
            completedOrders = data.completedOrders,
            revenueByCurrency = data.revenueByCurrency.map { rev ->
                RevenueItem(
                    currency = rev.currency,
                    amount = rev.amount,
                    formattedAmount = String.format("%.2f %s", rev.amount, rev.currency)
                )
            },
            recentOrders = data.recentOrders.map { order ->
                RecentOrderItem(
                    id = order.id,
                    userName = order.userName,
                    orderDate = order.orderDate,
                    status = order.status,
                    total = order.total,
                    currency = order.currency,
                    formattedTotal = String.format("%.2f %s", order.total, order.currency)
                )
            },
            recentRegistrations = data.recentRegistrations.map { reg ->
                RecentRegistrationItem(
                    id = reg.id,
                    email = reg.email,
                    role = reg.role,
                    registrationDate = reg.registrationDate
                )
            },
            lowStockAlerts = data.lowStockAlerts.map { alert ->
                LowStockAlertItem(
                    variantId = alert.variantId,
                    productName = alert.productName,
                    sku = alert.sku,
                    onHand = alert.onHand,
                    reserved = alert.reserved,
                    available = alert.available
                )
            }
        )

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                DashboardScreenContent(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {}
                )
            }
        }

        // Then — dashboard content is visible
        composeRule.onNodeWithTag("dashboardContent").assertExists()
        // KPI values are rendered
        composeRule.onNodeWithText("${data.totalUsers}").assertExists()
        composeRule.onNodeWithText("${data.totalOrders}").assertExists()
    }

    @Test
    fun dashboardScreenContent_loadingState_rendersLoadingIndicator() {
        // Given — loading state
        val uiState = DashboardUiState(isLoading = true)

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                DashboardScreenContent(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {}
                )
            }
        }

        // Then — loading indicator is visible
        composeRule.onNodeWithTag("dashboardLoading").assertExists()
    }

    @Test
    fun dashboardScreenContent_errorState_rendersRetryButton() {
        // Given — error state
        val uiState = DashboardUiState(error = "Connection failed")

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                DashboardScreenContent(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {}
                )
            }
        }

        // Then — error state is visible with retry button
        composeRule.onNodeWithTag("dashboardError").assertExists()
        composeRule.onNodeWithTag("dashboardRetryButton").assertExists()
    }

    @Test
    fun dashboardScreenContent_errorState_retryButtonInvokesCallback() {
        // Given — error state
        val uiState = DashboardUiState(error = "Connection failed")
        var retryInvoked = false

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                DashboardScreenContent(
                    uiState = uiState,
                    onRetry = { retryInvoked = true },
                    onRefresh = {}
                )
            }
        }

        // Then — clicking retry invokes the callback
        composeRule.onNodeWithTag("dashboardRetryButton").performClick()
        assert(retryInvoked) { "onRetry callback should have been invoked" }
    }

    @Test
    fun dashboardScreenContent_emptyState_rendersContentWithNoData() {
        // Given — empty state (no data loaded)
        val uiState = DashboardUiState()

        // When
        composeRule.setContent {
            NearAiAdminTheme {
                DashboardScreenContent(
                    uiState = uiState,
                    onRetry = {},
                    onRefresh = {}
                )
            }
        }

        // Then — with no data and no loading/error, the content area renders (empty sections)
        composeRule.onNodeWithTag("dashboardContent").assertExists()
    }
}
