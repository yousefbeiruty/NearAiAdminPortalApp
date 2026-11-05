package com.nearai.admin.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.theme.*

// =============================================================================
// Overview Dashboard Composable
// =============================================================================

/**
 * Overview Dashboard — connected to [DashboardViewModel] for state-driven rendering.
 *
 * Displays real KPI metrics, revenue by currency, recent orders, recent registrations,
 * and low stock alerts from the ViewModel's StateFlow. Handles loading, error, and
 * content states with pull-to-refresh support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToDetails: () -> Unit = {},
    onNavigateToSystemHealth: () -> Unit = {},
    onNavigateToAuditLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val hasData = uiState.totalUsers > 0 ||
        uiState.totalProducts > 0 ||
        uiState.recentOrders.isNotEmpty() ||
        uiState.recentRegistrations.isNotEmpty() ||
        uiState.lowStockAlerts.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Navy,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = stringResource(R.string.dashboard_brand_mark),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                            Text(
                                text = stringResource(R.string.dashboard_overview),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: notifications */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = RoseError,
                                    contentColor = Color.White
                                ) {
                                    Text("3")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = stringResource(R.string.dashboard_notifications),
                                tint = Navy
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToAuditLogs,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = stringResource(R.string.dashboard_audit_logs),
                            tint = Navy
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Indigo.copy(alpha = 0.15f))
                            .border(1.dp, Indigo.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AF",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        if (uiState.isLoading && !hasData) {
            // Initial loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Indigo,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_loading),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight
                        )
                    )
                }
            }
        } else if (uiState.error != null && !hasData) {
            // Error state with retry
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = RoseError,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_load_error),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    val errorMessage = uiState.error ?: stringResource(R.string.common_unknown_error)
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight
                        )
                    )
                    Button(
                        onClick = { viewModel.onRetry() },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.common_retry),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Content state (may have previous data while refreshing)
            DashboardContent(
                uiState = uiState,
                onNavigateToSystemHealth = onNavigateToSystemHealth,
                onRefresh = { viewModel.onRefresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

/**
 * Dashboard content rendered from ViewModel state.
 */
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToSystemHealth: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Greeting
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_greeting),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight
                        )
                    )
                    Text(
                        text = stringResource(R.string.dashboard_user_name),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_live),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // 2. KPI Grid — 6 metric cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = stringResource(R.string.dashboard_total_users),
                        value = uiState.totalUsers.toString(),
                        icon = Icons.Outlined.People,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = stringResource(R.string.dashboard_products),
                        value = uiState.totalProducts.toString(),
                        icon = Icons.Outlined.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = stringResource(R.string.dashboard_categories),
                        value = uiState.totalCategories.toString(),
                        icon = Icons.Outlined.Category,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = stringResource(R.string.dashboard_total_orders),
                        value = uiState.totalOrders.toString(),
                        icon = Icons.Outlined.ShoppingCart,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = stringResource(R.string.dashboard_pending_orders),
                        value = uiState.pendingOrders.toString(),
                        icon = Icons.Outlined.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = stringResource(R.string.dashboard_completed_orders),
                        value = uiState.completedOrders.toString(),
                        icon = Icons.Outlined.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Revenue by Currency
        if (uiState.revenueByCurrency.isNotEmpty()) {
            item {
                RevenueSection(uiState.revenueByCurrency)
            }
        }

        // 4. Recent Orders
        if (uiState.recentOrders.isNotEmpty()) {
            item {
                SectionHeaderWithCount(
                    title = stringResource(R.string.dashboard_recent_orders),
                    count = uiState.recentOrders.size
                )
            }
            items(uiState.recentOrders) { order ->
                RecentOrderCard(order = order)
            }
        }

        // 5. Recent Registrations
        if (uiState.recentRegistrations.isNotEmpty()) {
            item {
                SectionHeaderWithCount(
                    title = stringResource(R.string.dashboard_recent_registrations),
                    count = uiState.recentRegistrations.size
                )
            }
            items(uiState.recentRegistrations) { reg ->
                RecentRegistrationCard(registration = reg)
            }
        }

        // 6. Low Stock Alerts
        if (uiState.lowStockAlerts.isNotEmpty()) {
            item {
                SectionHeaderWithCount(
                    title = stringResource(R.string.dashboard_low_stock_alerts),
                    count = uiState.lowStockAlerts.size
                )
            }
            items(uiState.lowStockAlerts) { alert ->
                LowStockAlertCard(alert = alert)
            }
        }

        // 7. Spacer at bottom
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// =============================================================================
// Section Header with item count badge
// =============================================================================

@Composable
private fun SectionHeaderWithCount(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Indigo.copy(alpha = 0.15f)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Indigo,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

// =============================================================================
// Metric Card
// =============================================================================

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryLight,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Indigo.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            )
        }
    }
}

// =============================================================================
// Revenue Section
// =============================================================================

@Composable
private fun RevenueSection(revenues: List<RevenueItem>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_revenue_by_currency),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            revenues.forEachIndexed { index, revenue ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Indigo)
                        )
                        Text(
                            text = revenue.currency,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Navy,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    Text(
                        text = revenue.formattedAmount,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (index < revenues.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// =============================================================================
// Recent Order Card
// =============================================================================

@Composable
private fun RecentOrderCard(order: RecentOrderItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Indigo.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.userName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                )
                Text(
                    text = stringResource(R.string.dashboard_order_id, order.id.take(8), order.orderDate),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = order.formattedTotal,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                )
                val statusColor = when (order.status.lowercase()) {
                    "completed", "delivered" -> EmeraldSuccess
                    "pending" -> AmberWarning
                    "cancelled", "rejected" -> RoseError
                    else -> Indigo
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = order.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// Recent Registration Card
// =============================================================================

@Composable
private fun RecentRegistrationCard(registration: RecentRegistrationItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmeraldSuccess.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = registration.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                )
                Text(
                    text = registration.email,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight
                    )
                )
            }
            Text(
                text = registration.registrationDate,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryLight
                )
            )
        }
    }
}

// =============================================================================
// Low Stock Alert Card
// =============================================================================

@Composable
private fun LowStockAlertCard(alert: LowStockAlertItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AmberWarning.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.productName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                )
                Text(
                    text = stringResource(R.string.dashboard_sku_label, alert.sku),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.dashboard_stock_available, alert.available),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning
                    )
                )
                Text(
                    text = stringResource(R.string.dashboard_stock_reserved, alert.reserved),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryLight
                    )
                )
            }
        }
    }
}

// =============================================================================
// Jetpack Compose Preview
// =============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OverviewDashboardLoadingPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = Indigo,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(R.string.dashboard_loading),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondaryLight
                    )
                )
            }
        }
    }
}
