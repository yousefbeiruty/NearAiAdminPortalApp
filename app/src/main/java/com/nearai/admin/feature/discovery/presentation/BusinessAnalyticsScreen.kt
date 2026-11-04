package com.nearai.admin.feature.discovery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.core.designsystem.components.KpiCard
import com.nearai.admin.core.designsystem.components.KpiTrend
import com.nearai.admin.core.designsystem.components.SimpleBarChart
import com.nearai.admin.core.designsystem.theme.*

/**
 * Business Analytics screen displaying real backend metrics.
 *
 * Shows order KPIs, revenue by currency, best-selling products, category sales,
 * and customer analytics — all sourced from the backend business analytics API.
 */

// =============================================================================
// Business Analytics Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessAnalyticsScreen(
    viewModel: BusinessAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val daysOptions = listOf(7 to "7D", 30 to "30D")
    var selectedDaysIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is BusinessAnalyticsEvent.Retry) {
                viewModel.retry()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Business Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    daysOptions.forEachIndexed { index, (days, label) ->
                        FilterChip(
                            selected = index == selectedDaysIndex,
                            onClick = {
                                selectedDaysIndex = index
                                viewModel.setSelectedDays(days)
                            },
                            label = { Text(label) }
                        )
                        if (index < daysOptions.size - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Loading / Error states
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.error != null && uiState.totalOrders == 0) {
                item {
                    ErrorState(message = uiState.error!!, onRetry = { viewModel.retry() })
                }
            } else {

                // Period subtitle
                if (uiState.startDate.isNotBlank() && uiState.endDate.isNotBlank()) {
                    item {
                        Text(
                            text = "Period: ${uiState.startDate} — ${uiState.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.compare && uiState.previousStartDate != null && uiState.previousEndDate != null) {
                            Text(
                                text = "vs previous: ${uiState.previousStartDate} — ${uiState.previousEndDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ─── Order KPIs ───────────────────────────────────────────
                item {
                    SectionHeader(title = "Orders")
                }
                item {
                    OrderKpiGrid(uiState = uiState)
                }

                // ─── Order Status Breakdown ───────────────────────────────
                if (uiState.ordersByStatus.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Order Status Breakdown")
                    }
                    item {
                        OrderStatusList(statuses = uiState.ordersByStatus)
                    }
                }

                // ─── Daily Order Trend ────────────────────────────────────
                if (uiState.dailyOrders.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Daily Orders")
                    }
                    item {
                        DailyOrderChart(dailyOrders = uiState.dailyOrders)
                    }
                }

                // ─── Revenue by Currency ──────────────────────────────────
                if (uiState.revenueByCurrency.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Revenue by Currency")
                    }
                    item {
                        RevenueByCurrencyList(revenues = uiState.revenueByCurrency)
                    }
                }

                // ─── Daily Revenue Trend ──────────────────────────────────
                if (uiState.dailyRevenue.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Daily Revenue")
                    }
                    item {
                        DailyRevenueChart(dailyRevenues = uiState.dailyRevenue)
                    }
                }

                // ─── Best-Selling Products ────────────────────────────────
                if (uiState.bestSellingProducts.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Best-Selling Products")
                    }
                    item {
                        ProductSalesList(products = uiState.bestSellingProducts)
                    }
                }

                // ─── Category Sales ───────────────────────────────────────
                if (uiState.categorySales.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Sales by Category")
                    }
                    item {
                        CategorySalesList(categories = uiState.categorySales)
                    }
                }

                // ─── Customer Analytics ───────────────────────────────────
                item {
                    SectionHeader(title = "Customer Analytics")
                }
                item {
                    CustomerAnalyticsGrid(uiState = uiState)
                }

                // ─── Daily Registrations ──────────────────────────────────
                if (uiState.dailyRegistrations.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Daily Registrations")
                    }
                    item {
                        DailyRegistrationChart(registrations = uiState.dailyRegistrations)
                    }
                }

                // Refreshing indicator
                if (uiState.isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Section Header
// =============================================================================

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// Order KPI Grid
// =============================================================================

@Composable
private fun OrderKpiGrid(uiState: BusinessAnalyticsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                label = "Total Orders",
                value = uiState.totalOrdersFormatted,
                trend = uiState.totalOrdersPercentageChange?.toKpiTrend(),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            KpiCard(
                label = "Completed",
                value = uiState.completedOrdersFormatted,
                trend = uiState.completedOrdersPercentageChange?.toKpiTrend(),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                label = "Pending",
                value = uiState.pendingOrdersFormatted,
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            KpiCard(
                label = "Cancelled",
                value = uiState.cancelledOrdersFormatted,
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        tint = RoseError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

private fun Double?.toKpiTrend(): KpiTrend? {
    val pct = this?.toInt() ?: return null
    if (pct == 0) return null
    return if (this >= 0) {
        KpiTrend.Up(pct.coerceAtLeast(0), "vs prev")
    } else {
        KpiTrend.Down(-pct, "vs prev")
    }
}

// =============================================================================
// Order Status List
// =============================================================================

@Composable
private fun OrderStatusList(statuses: List<OrderStatusUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            statuses.forEachIndexed { index, status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = status.status.replace("_", " ").replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = status.formattedCount,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (index < statuses.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// =============================================================================
// Daily Order Chart
// =============================================================================

@Composable
private fun DailyOrderChart(dailyOrders: List<DailyOrderUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dataPoints = dailyOrders.map { it.count.toFloat() }
            SimpleBarChart(
                dataPoints = dataPoints,
                color = Indigo,
                height = 60
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (dailyOrders.isNotEmpty()) {
                    Text(
                        text = dailyOrders.first().formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dailyOrders.last().formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================================
// Revenue by Currency List
// =============================================================================

@Composable
private fun RevenueByCurrencyList(revenues: List<RevenueByCurrencyUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            revenues.forEachIndexed { index, revenue ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = revenue.currency,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${revenue.formattedOrderCount} orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${revenue.currency} ${revenue.formattedTotal}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "Avg: ${revenue.currency} ${revenue.formattedAvgOrderValue}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (index < revenues.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Daily Revenue Chart
// =============================================================================

@Composable
private fun DailyRevenueChart(dailyRevenues: List<DailyRevenueUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Group by currency and show each
            val currencies = dailyRevenues.map { it.currency }.distinct()
            currencies.forEachIndexed { currencyIndex, currency ->
                if (currencyIndex > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(
                    text = currency,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val currencyData = dailyRevenues
                    .filter { it.currency == currency }
                    .map { it.amount.toFloat() }
                if (currencyData.isNotEmpty()) {
                    SimpleBarChart(
                        dataPoints = currencyData,
                        color = EmeraldSuccess,
                        height = 40
                    )
                }
            }
        }
    }
}

// =============================================================================
// Product Sales List
// =============================================================================

@Composable
private fun ProductSalesList(products: List<ProductSalesUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            products.forEachIndexed { index, product ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = product.sku,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${product.formattedUnitsSold} units",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${product.currency} ${product.formattedRevenue}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                        }
                    }
                    if (index < products.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Category Sales List
// =============================================================================

@Composable
private fun CategorySalesList(categories: List<CategorySalesUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            categories.forEachIndexed { index, category ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${category.formattedUnitsSold} units",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${category.currency} ${category.formattedRevenue}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                        }
                    }
                    if (index < categories.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Customer Analytics Grid
// =============================================================================

@Composable
private fun CustomerAnalyticsGrid(uiState: BusinessAnalyticsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                label = "New Registrations",
                value = uiState.newRegistrationsFormatted,
                trend = uiState.newRegistrationsPercentageChange?.toKpiTrend(),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            KpiCard(
                label = "Active Customers",
                value = uiState.activeCustomersFormatted,
                trend = uiState.activeCustomersPercentageChange?.toKpiTrend(),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        KpiCard(
            label = "Repeat Customers",
            value = uiState.repeatCustomersFormatted,
            modifier = Modifier.fillMaxWidth(),
            icon = {
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }
}

// =============================================================================
// Daily Registration Chart
// =============================================================================

@Composable
private fun DailyRegistrationChart(registrations: List<DailyRegistrationUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dataPoints = registrations.map { it.count.toFloat() }
            SimpleBarChart(
                dataPoints = dataPoints,
                color = Indigo,
                height = 60
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (registrations.isNotEmpty()) {
                    Text(
                        text = registrations.first().formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = registrations.last().formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================================
// Error State
// =============================================================================

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = RoseError,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Failed to load analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
