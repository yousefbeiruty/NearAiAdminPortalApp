package com.nearai.admin.feature.discovery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.nearai.admin.core.designsystem.theme.*

/**
 * Discovery Analytics screen that displays real-time customer behavior metrics.
 *
 * Shows overview KPIs, search analytics, popular products, popular categories,
 * and the conversion funnel — all sourced from the backend analytics API.
 */

// =============================================================================
// Discovery Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val daysOptions = listOf(1 to "1D", 7 to "7D", 30 to "30D")
    var selectedDaysIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is DiscoveryEvent.Retry) {
                viewModel.retry()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Discovery Analytics",
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
            } else if (uiState.error != null && uiState.popularProducts.isEmpty()) {
                item {
                    ErrorState(message = uiState.error!!, onRetry = { viewModel.retry() })
                }
            } else {

                // Overview KPIs section
                item {
                    SectionHeader(title = "Overview", subtitle = uiState.periodStart.takeIf { it.isNotBlank() }
                        ?.let { "Period: $it — ${uiState.periodEnd}" })
                }
                item {
                    KpiGrid(uiState = uiState)
                }

                // Search Analytics section
                item {
                    SectionHeader(title = "Search Analytics")
                }
                item {
                    SearchAnalyticsCard(uiState = uiState)
                }

                // Popular search terms
                if (uiState.searchPopularTerms.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Popular Search Terms")
                    }
                    item {
                        SearchTermsList(terms = uiState.searchPopularTerms)
                    }
                }

                // Zero-result search terms
                if (uiState.searchZeroResultTerms.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Zero-Result Searches")
                    }
                    item {
                        SearchTermsList(terms = uiState.searchZeroResultTerms, isWarning = true)
                    }
                }

                // Popular Products section
                if (uiState.popularProducts.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Popular Products", subtitle = "By ${uiState.popularProductsMetric}")
                    }
                    item {
                        ProductsList(products = uiState.popularProducts)
                    }
                }

                // Popular Categories section
                if (uiState.popularCategories.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Popular Categories")
                    }
                    item {
                        CategoriesList(categories = uiState.popularCategories)
                    }
                }

                // Conversion Funnel section
                if (uiState.funnelSteps.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Conversion Funnel",
                            subtitle = "Overall: ${String.format("%.1f%%", uiState.funnelOverallConversionPct)}"
                        )
                    }
                    item {
                        ConversionFunnelCard(steps = uiState.funnelSteps)
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
// KPI Grid
// =============================================================================

@Composable
private fun KpiGrid(uiState: DiscoveryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                label = "Daily Active Users",
                value = uiState.dailyActiveUsers.formattedCurrent,
                trend = uiState.dailyActiveUsers.toKpiTrend("vs prev"),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            KpiCard(
                label = "Weekly Active Users",
                value = uiState.weeklyActiveUsers.formattedCurrent,
                trend = uiState.weeklyActiveUsers.toKpiTrend("vs prev"),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                label = "Searches",
                value = uiState.totalSearches.formattedCurrent,
                trend = uiState.totalSearches.toKpiTrend("vs prev"),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Info,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            KpiCard(
                label = "Product Views",
                value = uiState.totalProductViews.formattedCurrent,
                trend = uiState.totalProductViews.toKpiTrend("vs prev"),
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Info,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                label = "Cart Adds",
                value = uiState.totalCartAdds.formattedCurrent,
                trend = uiState.totalCartAdds.toKpiTrend("vs prev"),
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
            KpiCard(
                label = "Purchases",
                value = uiState.totalPurchases.formattedCurrent,
                trend = uiState.totalPurchases.toKpiTrend("vs prev"),
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
        KpiCard(
            label = "Conversion Rate",
            value = "${uiState.conversionRate.formattedCurrent}%",
            trend = uiState.conversionRate.toKpiTrend("vs prev"),
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }
}

private fun MetricUi.toKpiTrend(period: String): KpiTrend? {
    if (changePct == 0.0 && previous == 0.0) return null
    val pct = changePct.toInt().coerceAtLeast(0)
    return if (changePct >= 0) {
        KpiTrend.Up(pct, period)
    } else {
        KpiTrend.Down(pct, period)
    }
}

// =============================================================================
// Search Analytics Card
// =============================================================================

@Composable
private fun SearchAnalyticsCard(uiState: DiscoveryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MetricRow(label = "Total Searches", value = uiState.searchTotalSearches.toString())
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MetricRow(
                label = "Zero-Result Searches",
                value = "${uiState.searchZeroResultSearches} (${String.format("%.1f%%", uiState.searchZeroResultRatePct)})",
                valueColor = if (uiState.searchZeroResultRatePct > 10) RoseError else MaterialTheme.colorScheme.onSurface
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MetricRow(
                label = "Avg Results / Search",
                value = String.format("%.1f", uiState.searchAvgResultsPerSearch)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            MetricRow(
                label = "Avg Latency",
                value = "${String.format("%.0fms", uiState.searchAvgLatencyMs)}"
            )
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

// =============================================================================
// Search Terms List
// =============================================================================

@Composable
private fun SearchTermsList(terms: List<SearchTermUi>, isWarning: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) AmberBg else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            terms.forEachIndexed { index, term ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = term.query,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isWarning) AmberDark else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        term.avgResults?.let {
                            Text(
                                text = "${it.toInt()} results",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = term.formattedCount,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isWarning) AmberDark else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (index < terms.size - 1) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// =============================================================================
// Products List
// =============================================================================

@Composable
private fun ProductsList(products: List<ProductUi>) {
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
                                text = product.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = product.categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = product.formattedCount,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${product.uniqueViewers} unique",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Cart add rate: ${String.format("%.1f%%", product.cartAddRatePct)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                    if (index < products.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Categories List
// =============================================================================

@Composable
private fun CategoriesList(categories: List<CategoryUi>) {
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
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = category.formattedViews,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${category.productViews} views",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${category.cartAdds} carts",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "${category.purchases} sales",
                            style = MaterialTheme.typography.labelSmall,
                            color = Indigo
                        )
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
// Conversion Funnel Card
// =============================================================================

@Composable
private fun ConversionFunnelCard(steps: List<FunnelStepUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val maxCount = steps.maxOfOrNull { it.count } ?: 1

            steps.forEachIndexed { index, step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = step.step.replace("_", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (index > 0) {
                                Text(
                                    text = "${String.format("%.1f%%", step.rate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (step.rate >= 50) EmeraldSuccess else AmberWarning
                                )
                            }
                            Text(
                                text = step.formattedCount,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Funnel bar
                    val widthPct = (step.count.toFloat() / maxCount).coerceIn(0.1f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widthPct)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index == 0) Indigo
                                else if (step.rate >= 50) EmeraldSuccess
                                else AmberWarning
                            )
                    )
                }
                if (index < steps.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
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
