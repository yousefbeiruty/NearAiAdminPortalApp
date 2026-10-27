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
import com.nearai.admin.core.designsystem.theme.Indigo
import com.nearai.admin.core.designsystem.theme.Navy

/**
 * Products & Inventory screen with KPI summary cards, filter tabs, catalog SKU list,
 * status tags, action buttons, and bottom floating action bar.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */

// =============================================================================
// Color Tokens
// =============================================================================

private val BackgroundSurface = Color(0xFFF7F9FB)
private val CardSurface = Color(0xFFFFFFFF)
private val BorderSubtle = Color(0xFFE2E8F0)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val EmeraldSuccess = Color(0xFF10B981)
private val EmeraldBg = Color(0xFFECFDF5)
private val EmeraldDark = Color(0xFF047857)
private val AmberWarning = Color(0xFFF59E0B)
private val AmberBg = Color(0xFFFFFBEB)
private val AmberDark = Color(0xFFB45309)
private val RoseError = Color(0xFFEF4444)
private val RoseBg = Color(0xFFFEF2F2)
private val RoseDark = Color(0xFFB91C1C)
private val PurpleBg = Color(0xFFEEF2FF)
private val PurpleDark = Color(0xFF4338CA)

// =============================================================================
// UI Data Models
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
    val footerNoteColor: Color = TextSecondary,
    val noticeBanner: String? = null,
    val isStrikethrough: Boolean = false
)

// =============================================================================
// Products & Inventory Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onAddSku: () -> Unit = {},
    onEditSku: (SkuItem) -> Unit = {},
    onDeleteSku: (SkuItem) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Items (18)", "Subscriptions (3)", "AI Credits (11)", "Add-ons (4)")

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
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "INVENTORY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
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
                                Badge(
                                    containerColor = RoseError,
                                    contentColor = Color.White
                                ) {
                                    Text("2")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSku,
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
        containerColor = BackgroundSurface
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
                            onClick = onSearchClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search Catalog",
                                tint = Navy
                            )
                        }
                        IconButton(
                            onClick = onFilterClick,
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
                        value = "18",
                        delta = "↗ 100% up",
                        deltaColor = EmeraldDark,
                        icon = Icons.Outlined.Inventory2,
                        iconTint = Indigo
                    ),
                    ProductKpi(
                        title = "Gross MRR",
                        value = "$4,820",
                        delta = "Recurring",
                        deltaColor = TextSecondary,
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconTint = Indigo
                    ),
                    ProductKpi(
                        title = "Alerts",
                        value = "2",
                        delta = "Deprecated",
                        deltaColor = RoseDark,
                        icon = Icons.Outlined.WarningAmber,
                        iconTint = RoseError
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
                            color = if (isSelected) Navy else CardSurface,
                            border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.clickable { selectedTab = index }
                        ) {
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.White else TextSecondary,
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
                            text = "(4 Showing)",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Button(
                        onClick = onAddSku,
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

            // 5. SKU Items Catalog List
            val catalogItems = listOf(
                SkuItem(
                    id = "sku-1",
                    title = "Pro Tier Monthly",
                    skuCode = "#SUB-PRO-M",
                    price = "$29.00",
                    billingCycle = "/ month",
                    status = SkuStatus.ACTIVE,
                    leadingIcon = Icons.Outlined.Security,
                    leadingIconBg = PurpleBg,
                    leadingIconTint = Indigo,
                    tag1Text = "1,000 AI Credits/mo",
                    tag1Icon = Icons.Outlined.Bolt,
                    tag2Text = "142 subscribers",
                    tag2Icon = Icons.Outlined.People,
                    footerNote = "Auto-renews",
                    footerNoteIcon = Icons.Outlined.Autorenew,
                    footerNoteColor = TextSecondary
                ),
                SkuItem(
                    id = "sku-2",
                    title = "Power Compute Pack",
                    skuCode = "#ADDON-PWR-25",
                    price = "$15.00",
                    billingCycle = "one-time",
                    status = SkuStatus.ACTIVE,
                    leadingIcon = Icons.Outlined.ViewInAr,
                    leadingIconBg = Color(0xFFF0FDF4),
                    leadingIconTint = EmeraldDark,
                    tag1Text = "2,500 bonus credits",
                    tag1Icon = Icons.Outlined.Stars,
                    tag2Text = "84 sold this mo",
                    tag2Icon = Icons.Outlined.ShoppingCart,
                    footerNote = "Instant Delivery",
                    footerNoteIcon = Icons.Outlined.History,
                    footerNoteColor = TextSecondary
                ),
                SkuItem(
                    id = "sku-3",
                    title = "Sponsored Place Boost...",
                    skuCode = "#MKT-BOOST-01",
                    price = "$49.00",
                    billingCycle = "/ mo",
                    status = SkuStatus.REVIEWING,
                    leadingIcon = Icons.Outlined.Campaign,
                    leadingIconBg = RoseBg,
                    leadingIconTint = RoseDark,
                    tag1Text = "10k Impressions / mo",
                    tag1Icon = Icons.Outlined.Visibility,
                    tag2Text = "NearAI Discover Feed",
                    tag2Icon = Icons.Outlined.Storefront,
                    footerNote = "Merchant Perk",
                    footerNoteIcon = Icons.Outlined.Schedule,
                    footerNoteColor = TextSecondary
                ),
                SkuItem(
                    id = "sku-4",
                    title = "Legacy Starter Tier",
                    skuCode = "#SUB-LEGACY",
                    price = "$9.00",
                    billingCycle = "Archived",
                    status = SkuStatus.DEPRECATED,
                    leadingIcon = Icons.Outlined.Archive,
                    leadingIconBg = Color(0xFFF1F5F9),
                    leadingIconTint = TextSecondary,
                    tag1Text = "",
                    tag1Icon = Icons.Default.Check,
                    tag2Text = "",
                    tag2Icon = Icons.Default.Check,
                    footerNote = "Inactive Catalog Item",
                    footerNoteIcon = Icons.Outlined.History,
                    footerNoteColor = RoseDark,
                    noticeBanner = "Renewal stopped. 3 grandfathered accounts remain scheduled for migration to Pro Tier.",
                    isStrikethrough = true
                )
            )
            items(catalogItems) { item ->
                SkuProductCard(
                    item = item,
                    onEdit = { onEditSku(item) },
                    onDelete = { onDeleteSku(item) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp)) // Clearance for bottom FAB
            }
        }
    }
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
        colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                        color = TextSecondary,
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
        colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                            color = TextPrimary,
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
                                color = TextSecondary,
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
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
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
                            color = BackgroundSurface,
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
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                    if (item.tag2Text.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundSurface,
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
                                        color = TextPrimary,
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
                    color = BackgroundSurface,
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
                                color = TextSecondary,
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
                        color = BackgroundSurface,
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
                                tint = TextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary,
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
