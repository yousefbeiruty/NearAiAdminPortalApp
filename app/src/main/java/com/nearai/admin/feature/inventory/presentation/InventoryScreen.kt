package com.nearai.admin.feature.inventory.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.catalog.domain.ProductStatus
import com.nearai.admin.feature.catalog.domain.StockMovementType
import java.text.SimpleDateFormat
import java.util.Locale

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

    // Two-stage event consumption to prevent duplicate navigation
    var pendingEvent: InventoryEvent? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { ev ->
            pendingEvent = ev
        }
    }
    // Consume pending event
    when (val event = pendingEvent) {
        is InventoryEvent.NavigateToEditProduct -> {
            pendingEvent = null
            onNavigateToEditProduct(event.productId)
            viewModel.clearEvent()
        }
        is InventoryEvent.NavigateToCreateProduct -> {
            pendingEvent = null
            onNavigateToCreateProduct()
            viewModel.clearEvent()
        }
        is InventoryEvent.StockOperationSuccess -> {
            pendingEvent = null
            viewModel.clearEvent()
        }
        is InventoryEvent.StockOperationError -> {
            pendingEvent = null
            viewModel.clearEvent()
        }
        else -> pendingEvent = null
    }

    // Snackbar for stock operations
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage: String? by remember { mutableStateOf(null) }
    var snackbarSuccess: Boolean by remember { mutableStateOf(true) }

    // Dialogs state
    var showReceiveDialog by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var showMovementsPanel by remember { mutableStateOf(false) }
    var selectedVariantForAction: InventoryVariant? by remember { mutableStateOf(null) }

    // Determine active tab based on filters
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.filterStatus, uiState.filterStock) {
        selectedTab = when {
            uiState.filterStock == StockFilter.LOW_STOCK -> 2
            uiState.filterStock == StockFilter.OUT_OF_STOCK -> 3
            uiState.filterStatus == ProductStatus.ACTIVE -> 1
            else -> 0
        }
    }
    val tabs = listOf(
        "All (${uiState.totalItems})",
        "Active (${uiState.kpiActiveSkus})",
        "Low Stock (${uiState.kpiLowStockCount})",
        "Out of Stock (${uiState.kpiOutOfStockCount})"
    )

    // Show dialogs
    selectedVariantForAction?.let { variant ->
        if (showReceiveDialog) {
            StockOperationDialog(
                title = "Receive Stock",
                subtitle = "SKU: ${variant.variant.sku}",
                currentStock = variant.stock?.availableQuantity ?: 0,
                onDismiss = { showReceiveDialog = false },
                onConfirm = { quantity, reason ->
                    viewModel.receiveStock(variant.variant.id, quantity, reason)
                    showReceiveDialog = false
                }
            )
        }
        if (showAdjustDialog) {
            StockAdjustDialog(
                title = "Adjust Stock",
                subtitle = "SKU: ${variant.variant.sku}",
                currentStock = variant.stock?.availableQuantity ?: 0,
                onDismiss = { showAdjustDialog = false },
                onConfirm = { delta, reason ->
                    viewModel.adjustStock(variant.variant.id, delta, reason)
                    showAdjustDialog = false
                }
            )
        }
        if (showMovementsPanel) {
            StockMovementsBottomSheet(
                variantSku = variant.variant.sku,
                movements = uiState.stockMovements,
                isLoading = uiState.isLoadingMovements,
                hasMore = uiState.stockMovementsHasMore,
                onLoadMore = { viewModel.loadMoreStockMovements() },
                onDismiss = {
                    showMovementsPanel = false
                },
                onLoadInitial = {
                    viewModel.loadStockMovements(variant.variant.id)
                }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            // 3. Filter Tabs Row
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
                                    2 -> viewModel.setFilterStock(StockFilter.LOW_STOCK)
                                    3 -> viewModel.setFilterStock(StockFilter.OUT_OF_STOCK)
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
                val hasLowStock = item.variants.any { iv ->
                    val qty = iv.stock?.availableQuantity ?: 0
                    qty > 0 && qty < 10
                }
                val hasOutOfStock = item.variants.any { iv ->
                    iv.stock?.availableQuantity ?: 0 == 0
                }
                // Find the first variant with stock issues for quick actions
                val actionVariant = item.variants.firstOrNull { iv ->
                    val qty = iv.stock?.availableQuantity ?: 0
                    qty < 10
                } ?: item.variants.firstOrNull()
                SkuProductCard(
                    item = skuItem,
                    onEdit = { viewModel.onEditProduct(item.product.id) },
                    onDelete = { /* Delete not yet implemented */ },
                    hasLowStock = hasLowStock,
                    hasOutOfStock = hasOutOfStock,
                    onReceiveStock = {
                        actionVariant?.let {
                            selectedVariantForAction = it
                            showReceiveDialog = true
                        }
                    },
                    onAdjustStock = {
                        actionVariant?.let {
                            selectedVariantForAction = it
                            showAdjustDialog = true
                        }
                    },
                    onViewMovements = {
                        actionVariant?.let {
                            selectedVariantForAction = it
                            showMovementsPanel = true
                        }
                    }
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
    val totalStock = variants.sumOf { iv -> iv.stock?.availableQuantity ?: 0 }
    val hasLowStock = variants.any { iv ->
        val qty = iv.stock?.availableQuantity ?: 0
        qty > 0 && qty < 10
    }
    val hasOutOfStock = variants.any { iv -> iv.stock?.availableQuantity ?: 0 == 0 }

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
        tag1Text = if (stock != null) "Stock: $totalStock" else "No stock data",
        tag1Icon = Icons.Outlined.Inventory2,
        tag2Text = if (variants.isNotEmpty()) "${variants.size} variant${if (variants.size > 1) "s" else ""}" else "",
        tag2Icon = Icons.Outlined.ViewList,
        footerNote = when {
            hasOutOfStock -> "Out of stock — needs restock"
            hasLowStock -> "Low stock — reorder soon"
            product.status == ProductStatus.ACTIVE -> "Active product"
            product.status == ProductStatus.INACTIVE -> "Inactive product"
            else -> "Draft"
        },
        footerNoteIcon = when {
            hasOutOfStock -> Icons.Outlined.WarningAmber
            hasLowStock -> Icons.Outlined.WarningAmber
            product.status == ProductStatus.ACTIVE -> Icons.Outlined.Autorenew
            product.status == ProductStatus.INACTIVE -> Icons.Outlined.History
            else -> Icons.Outlined.Schedule
        },
        footerNoteColor = when {
            hasOutOfStock -> RoseDark
            hasLowStock -> RoseDark
            product.status == ProductStatus.ACTIVE -> TextSecondaryLight
            product.status == ProductStatus.INACTIVE -> RoseDark
            else -> TextSecondaryLight
        },
        noticeBanner = if (hasOutOfStock) "One or more variants are out of stock" else null,
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
    onDelete: () -> Unit,
    hasLowStock: Boolean = false,
    hasOutOfStock: Boolean = false,
    onReceiveStock: () -> Unit = {},
    onAdjustStock: () -> Unit = {},
    onViewMovements: () -> Unit = {}
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

            // Stock action buttons (Receive, Adjust, History)
            if (hasLowStock || hasOutOfStock) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReceiveStock,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receive", color = EmeraldDark, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    OutlinedButton(
                        onClick = onAdjustStock,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = Indigo,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjust", color = Indigo, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    OutlinedButton(
                        onClick = onViewMovements,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("History", color = Navy, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

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
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
// Stock Operation Dialog (Receive Stock)
// =============================================================================

@Composable
fun StockOperationDialog(
    title: String,
    subtitle: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, reason: String) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Navy))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                Text(text = "Current stock: $currentStock", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity to receive") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (qty > 0) {
                        onConfirm(qty, reason)
                    }
                },
                enabled = quantity.toIntOrNull() != null && quantity.toIntOrNull()!! > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryLight)
            }
        }
    )
}

// =============================================================================
// Stock Adjustment Dialog
// =============================================================================

@Composable
fun StockAdjustDialog(
    title: String,
    subtitle: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: String) -> Unit
) {
    var delta by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Navy))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                Text(text = "Current stock: $currentStock", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
                Text(text = "Use negative values to decrease stock.", style = MaterialTheme.typography.bodySmall.copy(color = RoseDark))
                OutlinedTextField(
                    value = delta,
                    onValueChange = { delta = it },
                    label = { Text("Quantity delta (e.g. +5 or -3)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (required)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    isError = reason.isNotBlank() && reason.length < 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = delta.toIntOrNull() ?: 0
                    if (d != 0 && reason.isNotBlank() && reason.length >= 3) {
                        onConfirm(d, reason)
                    }
                },
                enabled = delta.toIntOrNull() != null && delta.toIntOrNull()!! != 0 && reason.isNotBlank() && reason.length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryLight)
            }
        }
    )
}

// =============================================================================
// Stock Movements Bottom Sheet
// =============================================================================

private val movementDateFormat by lazy { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementsBottomSheet(
    variantSku: String,
    movements: List<com.nearai.admin.feature.catalog.domain.StockMovement>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit,
    onLoadInitial: () -> Unit
) {
    // Load movements on first show
    LaunchedEffect(Unit) {
        onLoadInitial()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock Movement History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Navy, modifier = Modifier.size(20.dp))
                }
            }
            Text(text = "SKU: $variantSku", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight, fontFamily = FontFamily.Monospace))
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading && movements.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Navy)
                }
            } else if (movements.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.History, contentDescription = null, tint = TextSecondaryLight, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No movement history", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movements) { movement ->
                        StockMovementCard(movement)
                    }
                    if (hasMore) {
                        item {
                            Button(
                                onClick = onLoadMore,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy)
                            ) {
                                Text("Load More", color = Color.White)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StockMovementCard(movement: com.nearai.admin.feature.catalog.domain.StockMovement) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (movement.movementType) {
                        StockMovementType.RECEIPT -> EmeraldBg
                        StockMovementType.ADJUSTMENT -> PurpleBg
                        StockMovementType.SALE -> SurfaceVariantLight
                        StockMovementType.RETURN -> Indigo.copy(alpha = 0.15f)
                        StockMovementType.RESERVATION -> RoseBg
                        StockMovementType.RESERVATION_RELEASE -> EmeraldBg
                    }
                ) {
                    Text(
                        text = movement.movementType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (movement.movementType) {
                                StockMovementType.RECEIPT -> EmeraldDark
                                StockMovementType.ADJUSTMENT -> PurpleDark
                                else -> TextSecondaryLight
                            },
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = if (movement.quantityDelta >= 0) "+${movement.quantityDelta}" else "${movement.quantityDelta}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (movement.quantityDelta >= 0) EmeraldDark else RoseDark
                    )
                )
            }
            if (movement.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = movement.reason, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight), maxLines = 2)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = try {
                    movementDateFormat.format(java.util.Date(movement.createdAt.toLongOrNull() ?: System.currentTimeMillis()))
                } catch (_: Exception) {
                    movement.createdAt
                },
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight)
            )
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
        // Preview with sample product cards — avoids Hilt ViewModel dependency
        val sampleProduct = com.nearai.admin.feature.catalog.domain.Product(
            id = "p1",
            name = "Wireless Headphones Pro",
            description = "Premium noise-canceling headphones",
            slug = "wireless-headphones-pro",
            priceAmount = 199.99,
            priceCurrency = "USD",
            images = emptyList(),
            status = ProductStatus.ACTIVE,
            categoryId = "c1",
            createdAt = "2024-01-15T10:30:00Z",
            updatedAt = "2024-01-15T10:30:00Z"
        )
        val sampleItem = InventoryItem(
            product = sampleProduct,
            variants = listOf(
                InventoryVariant(
                    variant = com.nearai.admin.feature.catalog.domain.ProductVariant(
                        id = "v1",
                        productId = "p1",
                        sku = "WH-PRO-001-BLK",
                        attributes = mapOf("color" to "Black"),
                        status = com.nearai.admin.feature.catalog.domain.VariantStatus.ACTIVE,
                        availableQuantity = 45,
                        createdAt = "2024-01-15T10:30:00Z",
                        updatedAt = "2024-01-15T10:30:00Z"
                    ),
                    stock = com.nearai.admin.feature.catalog.domain.StockBalance(
                        variantId = "v1",
                        onHand = 50,
                        reserved = 5,
                        availableQuantity = 45
                    )
                )
            )
        )
        val skuItem = sampleItem.toSkuItem()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Products & Inventory", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Navy)
            SkuProductCard(item = skuItem, onEdit = {}, onDelete = {})
            val lowStockProduct = com.nearai.admin.feature.catalog.domain.Product(
                id = "p2",
                name = "USB-C Charging Cable",
                description = "Durable charging cable",
                slug = "usb-c-charging-cable",
                priceAmount = 12.99,
                priceCurrency = "USD",
                images = emptyList(),
                status = ProductStatus.ACTIVE,
                categoryId = "c1",
                createdAt = "2024-02-20T08:00:00Z",
                updatedAt = "2024-02-20T08:00:00Z"
            )
            val lowStockItem = InventoryItem(
                product = lowStockProduct,
                variants = listOf(
                    InventoryVariant(
                        variant = com.nearai.admin.feature.catalog.domain.ProductVariant(
                            id = "v2",
                            productId = "p2",
                            sku = "USB-C-002-WHT",
                            attributes = mapOf("color" to "White"),
                            status = com.nearai.admin.feature.catalog.domain.VariantStatus.ACTIVE,
                            availableQuantity = 3,
                            createdAt = "2024-02-20T08:00:00Z",
                            updatedAt = "2024-02-20T08:00:00Z"
                        ),
                        stock = com.nearai.admin.feature.catalog.domain.StockBalance(
                            variantId = "v2",
                            onHand = 3,
                            reserved = 0,
                            availableQuantity = 3
                        )
                    )
                )
            )
            SkuProductCard(
                item = lowStockItem.toSkuItem(),
                onEdit = {},
                onDelete = {},
                hasLowStock = true,
                onReceiveStock = {},
                onAdjustStock = {},
                onViewMovements = {}
            )
        }
    }
}
