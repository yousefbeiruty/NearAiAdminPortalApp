package com.nearai.admin.feature.orders.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.components.AppScreen
import com.nearai.admin.core.designsystem.components.EmptyState
import com.nearai.admin.core.designsystem.components.ErrorState
import com.nearai.admin.core.designsystem.components.LoadMoreIndicator
import com.nearai.admin.core.designsystem.components.PaginatedLazyColumn
import com.nearai.admin.core.designsystem.components.LoadingScreen
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary

/**
 * Orders list screen with filters, pagination, and pull-to-refresh.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    viewModel: OrdersListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()

    // Handle navigation events — consume and clear the event in the ViewModel to prevent
    // duplicate navigation when returning from Order Details.
    LaunchedEffect(event) {
        event?.let {
            if (it is OrdersListEvent.NavigateToOrder) {
                onNavigateToOrder(it.orderId)
                viewModel.onEventConsumed()
            }
        }
    }

    OrdersListScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onOrderClicked = { viewModel.onOrderClicked(it) },
        onRetry = { viewModel.onRetry() },
        onOrderStatusFilter = { viewModel.setFilterOrderStatus(it) },
        onFulfillmentStatusFilter = { viewModel.setFilterFulfillmentStatus(it) },
        onClearFilters = { viewModel.clearFilters() },
        onLoadMore = { viewModel.loadMore() }
    )
}

// =============================================================================
// Stateless Content Composable — renders orders list from UiState
// =============================================================================

/**
 * Stateless orders list content that renders the full screen from [OrdersListUiState].
 *
 * This composable can be used independently in tests and screenshot previews
 * without requiring a ViewModel or Hilt dependency injection.
 */
@Composable
fun OrdersListScreenContent(
    uiState: OrdersListUiState,
    onNavigateBack: () -> Unit,
    onOrderClicked: (String) -> Unit,
    onRetry: () -> Unit,
    onOrderStatusFilter: (OrderStatus?) -> Unit,
    onFulfillmentStatusFilter: (FulfillmentStatus?) -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppScreen(
        title = stringResource(R.string.orders_title),
        onNavigateBack = onNavigateBack,
        modifier = modifier
    ) { padding ->
        when {
            uiState.isLoading && uiState.orders.isEmpty() -> {
                LoadingScreen(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier.testTag("ordersLoading")
                )
            }
            uiState.error != null && uiState.orders.isEmpty() -> {
                ErrorState(
                    message = uiState.error.orEmpty(),
                    retryText = stringResource(R.string.common_retry),
                    onRetry = onRetry,
                    modifier = Modifier.testTag("ordersError")
                )
            }
            uiState.orders.isEmpty() && uiState.error == null -> {
                val hasActiveFilter = uiState.filterOrderStatus != null ||
                    uiState.filterFulfillmentStatus != null
                EmptyState(
                    title = stringResource(R.string.orders_empty),
                    message = stringResource(R.string.orders_empty_message),
                    actionText = if (hasActiveFilter) stringResource(R.string.orders_clear_filters) else null,
                    onActionClick = if (hasActiveFilter) onClearFilters else null,
                    modifier = Modifier.testTag("ordersEmpty")
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .testTag("ordersContent")
                ) {
                    // Filter chips
                    FilterSection(
                        uiState = uiState,
                        onOrderStatusFilter = onOrderStatusFilter,
                        onFulfillmentStatusFilter = onFulfillmentStatusFilter,
                        onClearFilters = onClearFilters,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Order count
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.orders_count, uiState.totalItems),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        if (uiState.totalPages > 1) {
                            Text(
                                text = stringResource(R.string.orders_page, uiState.currentPage, uiState.totalPages),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight
                                )
                            )
                        }
                    }

                    // Orders list with pagination
                    PaginatedLazyColumn(
                        dataItems = uiState.orders,
                        onLoadMore = onLoadMore,
                        isLoadingMore = uiState.isLoadingMore,
                        hasMore = uiState.hasMore,
                        loadMoreThreshold = 3,
                        key = { it.id },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        item = { order ->
                            OrderSummaryCard(
                                order = order,
                                onClick = { onOrderClicked(order.id) },
                                modifier = Modifier.testTag("orderCard_${order.id}")
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Filter section with order status and fulfillment status chips.
 */
@Composable
private fun FilterSection(
    uiState: OrdersListUiState,
    onOrderStatusFilter: (OrderStatus?) -> Unit,
    onFulfillmentStatusFilter: (FulfillmentStatus?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Order status filters
        Text(
            text = stringResource(R.string.orders_filter_order_status),
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryLight,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.filterOrderStatus == null,
                    onClick = { onOrderStatusFilter(null) },
                    label = { Text(stringResource(R.string.orders_filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            items(OrderStatus.values().toList()) { status ->
                FilterChip(
                    selected = uiState.filterOrderStatus == status,
                    onClick = { onOrderStatusFilter(status) },
                    label = { Text(status.displayLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fulfillment status filters
        Text(
            text = stringResource(R.string.orders_filter_fulfillment),
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryLight,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.filterFulfillmentStatus == null,
                    onClick = { onFulfillmentStatusFilter(null) },
                    label = { Text(stringResource(R.string.orders_filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            items(FulfillmentStatus.values().toList()) { status ->
                FilterChip(
                    selected = uiState.filterFulfillmentStatus == status,
                    onClick = { onFulfillmentStatusFilter(status) },
                    label = { Text(status.displayLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Clear filters button
        if (uiState.filterOrderStatus != null || uiState.filterFulfillmentStatus != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.align(androidx.compose.ui.Alignment.Start)
            ) {
                Text(stringResource(R.string.orders_clear_filters), color = Indigo, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OrdersListScreenPreview() {
    NearAiAdminTheme {
        // Preview with sample order cards — avoids Hilt ViewModel dependency
        val sampleOrders = listOf(
            com.nearai.admin.feature.orders.domain.OrderSummary(
                id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                createdAt = "2024-06-15T14:30:00Z",
                status = com.nearai.admin.feature.orders.domain.OrderStatus.PENDING,
                fulfillmentStatus = com.nearai.admin.feature.orders.domain.FulfillmentStatus.AWAITING_CONFIRMATION,
                totalAmount = java.math.BigDecimal("35.49"),
                subtotalAmount = java.math.BigDecimal("29.99"),
                deliveryFeeAmount = java.math.BigDecimal("5.50"),
                discountAmount = java.math.BigDecimal("0.00"),
                currency = "JOD",
                items = emptyList(),
                deliveryRecipientName = "John Doe",
                deliveryPhone = "+15551234567",
                deliveryCity = "New York",
                deliveryArea = "Manhattan",
                deliveryStreet = "Broadway",
                deliveryBuildingNumber = "123",
                deliveryApartment = "Apt 4B",
                deliveryInstructions = null
            ),
            com.nearai.admin.feature.orders.domain.OrderSummary(
                id = "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                createdAt = "2024-06-14T10:00:00Z",
                status = com.nearai.admin.feature.orders.domain.OrderStatus.COMPLETED,
                fulfillmentStatus = com.nearai.admin.feature.orders.domain.FulfillmentStatus.PROCESSING,
                totalAmount = java.math.BigDecimal("129.99"),
                subtotalAmount = java.math.BigDecimal("124.49"),
                deliveryFeeAmount = java.math.BigDecimal("5.50"),
                discountAmount = java.math.BigDecimal("0.00"),
                currency = "JOD",
                items = emptyList(),
                deliveryRecipientName = "Jane Smith",
                deliveryPhone = "+15559876543",
                deliveryCity = "Los Angeles",
                deliveryArea = "Hollywood",
                deliveryStreet = "Sunset Blvd",
                deliveryBuildingNumber = "456",
                deliveryApartment = null,
                deliveryInstructions = null
            )
        )
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.orders_title), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Navy)
            sampleOrders.forEach { order ->
                OrderSummaryCard(
                    order = order,
                    onClick = { }
                )
            }
        }
    }
}
