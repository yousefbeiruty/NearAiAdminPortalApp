package com.nearai.admin.feature.inventory.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.catalog.domain.ProductStatus

/**
 * Products & Inventory screen connected to [InventoryViewModel] for real backend data.
 * Displays KPI summary cards, filter tabs, product catalog with variants and stock,
 * status tags, action buttons, and bottom floating action bar.
 */

// =============================================================================
// UI Data Models (preserved for SkuProductCard compatibility)
// =============================================================================

enum class SkuStatus(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color,
    val showDot: Boolean = false
) {
    ACTIVE("Active", EmeraldDark, EmeraldBg, showDot = true),
    REVIEWING("Reviewing", PurpleDark, PurpleBg, showDot = false),
    DEPRECATED("Deprecated", RoseDark, RoseBg, showDot = false)
}

data class ProductKpi(
    val title: String,
    val value: String,
    val delta: String,
    val deltaColor: Color,
    val icon: ImageVector,
    val iconTint: Color = Indigo
)

data class SkuItem(
    val id: String,
    val title: String,
    val skuCode: String,
    val price: String,
    val billingCycle: String,
    val status: SkuStatus,
    val leadingIcon: ImageVector,
    val leadingIconBg: Color,
    val leadingIconTint: Color,
    val tag1Text: String,
    val tag1Icon: ImageVector,
    val tag2Text: String,
    val tag2Icon: ImageVector,
    val footerNote: String,
    val footerNoteIcon: ImageVector? = null,
    val footerNoteColor: Color = TextSecondaryLight,
    val noticeBanner: String? = null,
    val isStrikethrough: Boolean = false
)

/**
 * Maps [ProductStatus] to [SkuStatus] for display.
 */
fun ProductStatus.toSkuStatus(): SkuStatus = when (this) {
    ProductStatus.ACTIVE -> SkuStatus.ACTIVE
    ProductStatus.INACTIVE -> SkuStatus.DEPRECATED
    ProductStatus.DRAFT -> SkuStatus.REVIEWING
}

/**
 * Maps [ProductStatus] to a display icon.
 */
fun ProductStatus.toLeadingIcon(): ImageVector = when (this) {
    ProductStatus.ACTIVE -> Icons.Outlined.Security
    ProductStatus.INACTIVE -> Icons.Outlined.Archive
    ProductStatus.DRAFT -> Icons.Outlined.Campaign
}

/**
 * Maps [ProductStatus] to a display icon background color.
 */
fun ProductStatus.toLeadingIconBg(): Color = when (this) {
    ProductStatus.ACTIVE -> EmeraldBg
    ProductStatus.INACTIVE -> SurfaceVariantLight
    ProductStatus.DRAFT -> RoseBg
}

/**
 * Maps [ProductStatus] to a display icon tint color.
 */
fun ProductStatus.toLeadingIconTint(): Color = when (this) {
    ProductStatus.ACTIVE -> EmeraldDark
    ProductStatus.INACTIVE -> TextSecondaryLight
    ProductStatus.DRAFT -> RoseDark
}

// =============================================================================
// Products & Inventory Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEditProduct: (String) -> Unit = {},
    onNavigateToCreateProduct: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.event.collectAsState()

    // Handle navigation events
    LaunchedEffect(event) {
        when (event) {
            is InventoryEvent.NavigateToEditProduct -> {
                onNavigateToEditProduct((event as InventoryEvent.NavigateToEditProduct).productId)
                viewModel.clearEvent()
            }
            is InventoryEvent.NavigateToCreateProduct -> {
                onNavigateToCreateProduct()
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "All (${uiState.totalItems})",
        "Active (${uiState.kpiActiveSkus})",
        "Low Stock (${uiState.kpiLowStockCount})"
    )

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
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = "NearAI Brand Mark",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "NearAI Admin",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                            Text(
                                text = "INVENTORY",
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
                    IconButton(onClick = onNotificationClick) {
                        BadgedBox(
                            badge = {
                                if (uiState.kpiLowStockCount > 0) {
                                    Badge(
                                        containerColor = RoseError,
                                        contentColor = Color.White
                                    ) {
                                        Text(uiState.kpiLowStockCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Navy
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Indigo.copy(alpha = 0.15f))
                            .border(1.dp, Indigo.copy(alpha = 0.35f), CircleShape)
                            .clickable { onProfileClick() },
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onCreateProduct() },
                containerColor = Navy,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = "New Product",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                }
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sub-Header: Back Navigation & Title Bar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Navy
                            )
                        }
                        Text(
                            text = "Inventory & Products",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { /* Search handled by filter tabs */ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search Catalog",
                                tint = Navy
                            )
                        }
                        IconButton(
                            onClick = {
                                if (uiState.filterStatus != null) {
                                    viewModel.clearFilters()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Filter Catalog",
                                tint = Navy
                            )
                        }
                    }
                }
            }

            // 2. Three Metric KPI Cards
            item {
                val kpis = listOf(
                    ProductKpi(
                        title = "Active SKUs",
                        value = uiState.kpiActiveSkus.toString(),
                        delta = "Total products",
                        deltaColor = EmeraldDark,
                        icon = Icons.Outlined.Inventory2,
                        iconTint = Indigo
                    ),
                    ProductKpi(
                        title = "Total Products",
                        value = uiState.kpiTotalProducts.toString(),
                        delta = "In catalog",
                        deltaColor = TextSecondaryLight,
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconTint = Indigo
                    ),
                    ProductKpi(
                        title = "Low Stock",
                        value = uiState.kpiLowStockCount.toString(),
                        delta = if (uiState.kpiLowStockCount > 0) "Needs attention" else "All good",
                        deltaColor = if (uiState.kpiLowStockCount > 0) RoseDark else EmeraldDark,
                        icon = Icons.Outlined.WarningAmber,
                        iconTint = if (uiState.kpiLowStockCount > 0) RoseError else EmeraldDark
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    kpis.forEach { kpi ->
                        ProductKpiCard(
                            kpi = kpi,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Category Filter Tabs Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, tabName ->
                        val isSelected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Navy else SurfaceLight,
                            border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.clickable {
                                selectedTab = index
                                when (index) {
                                    0 -> viewModel.clearFilters()
                                    1 -> viewModel.setFilterStatus(ProductStatus.ACTIVE)
                                    2 -> viewModel.setFilterStatus(null) // Low stock shown in KPI
                                }
                            }
                        ) {
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.White else TextSecondaryLight,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 4. Catalog Directory Header & Add SKU Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Catalog Directory",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        )
                        Text(
                            text = "(${uiState.inventoryItems.size} Showing)",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
                        )
                    }
                    Button(
                        onClick = { viewModel.onCreateProduct() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add SKU",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // 5. Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Navy)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading inventory...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondaryLight
                                )
                            )
                        }
                    }
                }
            }

            // 6. Error state
            val errorMessage = uiState.error
            if (errorMessage != null && uiState.inventoryItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = RoseError,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondaryLight
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.onRetry() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 7. Empty state
            if (uiState.inventoryItems.isEmpty() && !uiState.isLoading && uiState.error == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No products found",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondaryLight
                                )
                            )
                        }
                    }
                }
            }

            // 8. Product Items List
            items(uiState.inventoryItems) { item ->
                val skuItem = item.toSkuItem()
                SkuProductCard(
                    item = skuItem,
                    onEdit = { viewModel.onEditProduct(item.product.id) },
                    onDelete = { /* Delete not yet implemented */ }
                )
            }

            // 9. Load more indicator
            if (uiState.isLoadingMore) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Navy, modifier = Modifier.size(24.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Clearance for bottom FAB
            }
        }
    }
}

/**
 * Converts an [InventoryItem] to a [SkuItem] for display in SkuProductCard.
 */
fun InventoryItem.toSkuItem(): SkuItem {
    val status = product.status.toSkuStatus()
    val variant = variants.firstOrNull()
    val stock = variant?.stock

    return SkuItem(
        id = product.id,
        title = product.name,
        skuCode = variant?.variant?.sku ?: product.slug,
        price = "$${String.format("%.2f", product.priceAmount)}",
        billingCycle = "per unit",
        status = status,
        leadingIcon = product.status.toLeadingIcon(),
        leadingIconBg = product.status.toLeadingIconBg(),
        leadingIconTint = product.status.toLeadingIconTint(),
        tag1Text = if (stock != null) "Stock: ${stock.availableQuantity}" else "",
        tag1Icon = Icons.Outlined.Inventory2,
        tag2Text = if (variants.isNotEmpty()) "${variants.size} variant${if (variants.size > 1) "s" else ""}" else "",
        tag2Icon = Icons.Outlined.ViewList,
        footerNote = when (product.status) {
            ProductStatus.ACTIVE -> "Active product"
            ProductStatus.INACTIVE -> "Inactive product"
            ProductStatus.DRAFT -> "Draft"
        },
        footerNoteIcon = when (product.status) {
            ProductStatus.ACTIVE -> Icons.Outlined.Autorenew
            ProductStatus.INACTIVE -> Icons.Outlined.History
            ProductStatus.DRAFT -> Icons.Outlined.Schedule
        },
        footerNoteColor = when (product.status) {
            ProductStatus.ACTIVE -> TextSecondaryLight
            ProductStatus.INACTIVE -> RoseDark
            ProductStatus.DRAFT -> TextSecondaryLight
        },
        noticeBanner = null,
        isStrikethrough = product.status == ProductStatus.INACTIVE
    )
}

// =============================================================================
// Product KPI Card
// =============================================================================

@Composable
fun ProductKpiCard(
    kpi: ProductKpi,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = kpi.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryLight,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Icon(
                    imageVector = kpi.icon,
                    contentDescription = null,
                    tint = kpi.iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kpi.value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = kpi.delta,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = kpi.deltaColor,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
        }
    }
}

// =============================================================================
// SKU Product Item Card Composable
// =============================================================================

@Composable
fun SkuProductCard(
    item: SkuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Leading Icon, Title + SKU, Price + Billing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Leading Icon Container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = item.leadingIconBg,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.leadingIcon,
                            contentDescription = null,
                            tint = item.leadingIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                // Title, Status Pill, SKU
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            textDecoration = if (item.isStrikethrough) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = item.status.backgroundColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (item.status.showDot) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldDark)
                                    )
                                }
                                Text(
                                    text = item.status.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = item.status.textColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Text(
                            text = "SKU: ${item.skuCode}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
                // Price and Billing Cycle
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = item.price,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    Text(
                        text = item.billingCycle,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight)
                    )
                }
            }

            // Middle Tags (AI Credits, Subscriber Count, Impressions, etc.)
            if (item.tag1Text.isNotEmpty() || item.tag2Text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.tag1Text.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundLight,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = item.tag1Icon,
                                    contentDescription = null,
                                    tint = Indigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = item.tag1Text,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Navy,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                    if (item.tag2Text.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundLight,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = item.tag2Icon,
                                    contentDescription = null,
                                    tint = Indigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = item.tag2Text,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Navy,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Notice Banner (for Deprecated / Grandfathered products)
            if (item.noticeBanner != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BackgroundLight,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = RoseDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.noticeBanner,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryLight,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Row: Footer Note & Action Buttons (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.footerNoteIcon != null) {
                        Icon(
                            imageVector = item.footerNoteIcon,
                            contentDescription = null,
                            tint = item.footerNoteColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = item.footerNote,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = item.footerNoteColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundLight,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.clickable { onEdit() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = Navy,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Navy,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete SKU",
                            tint = RoseError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun InventoryScreenPreview() {
    MaterialTheme {
        InventoryScreen()
    }
}
