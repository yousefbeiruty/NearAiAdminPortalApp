package com.nearai.admin.feature.inventory.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nearai.admin.core.designsystem.theme.*
import kotlin.math.roundToInt

/**
 * Product Creation and Editing screen with billing classification, metadata, pricing,
 * quota limits, model routing, and lifecycle operations.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */

// =============================================================================
// Color Tokens
// =============================================================================


// =============================================================================
// UI State & Models
// =============================================================================

enum class BillingClassification(val title: String, val subtitle: String) {
    SUBSCRIPTION("Subscription", "Recurring"),
    CREDIT_PACK("Credit Pack", "One-time"),
    PERK_BOOST("Perk Boost", "Merchant")
}

// =============================================================================
// Product Creation & Editing Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    onDismiss: () -> Unit = {},
    onSaveHeaderClick: () -> Unit = {},
    onDiscard: () -> Unit = {},
    onPublishAndSave: () -> Unit = {},
    onDeprecateSku: () -> Unit = {},
    onDeleteSku: () -> Unit = {}
) {
    // Form State
    var selectedClassification by remember { mutableStateOf(BillingClassification.SUBSCRIPTION) }
    var displayName by remember { mutableStateOf("Pro Tier Monthly") }
    val internalIdentifier = "sub_pro_monthly" // Locked
    var selectedCategoryTag by remember { mutableStateOf("Developer / Pro Consumer") }
    var listPrice by remember { mutableStateOf("29.00") }
    var cadenceExpanded by remember { mutableStateOf(false) }
    var selectedCadence by remember { mutableStateOf("Monthly") }
    val cadenceOptions = listOf("Monthly", "Quarterly", "Annual", "Weekly")
    var hasFreeTrial by remember { mutableStateOf(true) }
    var monthlyCredits by remember { mutableFloatStateOf(1000f) }
    var burstGovernor by remember { mutableStateOf("60") }
    var gpt4oEnabled by remember { mutableStateOf(true) }
    var claudeSonnetEnabled by remember { mutableStateOf(true) }
    var llama70bEnabled by remember { mutableStateOf(true) }
    var cacheBypassEnabled by remember { mutableStateOf(true) }

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
                                text = "PRODUCT EDIT",
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
                    IconButton(onClick = {}) {
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
                                contentDescription = "Alerts",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
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
            // 1. Modal Sub-Header Bar (Dismiss, Title, SKU Badge, Quick Save)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Navy
                            )
                        }
                        Column {
                            Text(
                                text = "Configure Product S...",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldDark)
                                        )
                                        Text(
                                            text = "Active",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = EmeraldDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = "SKU: #SUB-PRO-M",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryLight,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }
                    Button(
                        onClick = onSaveHeaderClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // 2. Billing Classification Segmented Selector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "BILLING CLASSIFICATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundLight, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BillingClassification.values().forEach { classification ->
                                val isSelected = selectedClassification == classification
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) SurfaceLight else Color.Transparent,
                                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedClassification = classification }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = classification.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Navy else TextSecondaryLight
                                            )
                                        )
                                        Text(
                                            text = classification.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Indigo else TextSecondaryLight,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Metadata & Identity Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Badge,
                                    contentDescription = null,
                                    tint = Indigo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Metadata & Identity",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy
                                    )
                                )
                            }
                            Text(
                                text = "Core Spec",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Public Display Name Input
                        Text(
                            text = "Public Display Name",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = TextSecondaryLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BackgroundLight,
                                unfocusedContainerColor = BackgroundLight,
                                focusedBorderColor = Indigo,
                                unfocusedBorderColor = BorderSubtle
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Internal Identifier (Locked)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Internal Identifier",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "LOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Indigo,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BackgroundLight,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = TextSecondaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = internalIdentifier,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Navy,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = TextSecondaryLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Immutable once deployed to global billing engine.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Classification Category Tag
                        Text(
                            text = "Classification Category",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleBg,
                                border = BorderStroke(1.dp, PurpleDark.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Code,
                                        contentDescription = null,
                                        tint = PurpleDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = selectedCategoryTag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PurpleDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove tag",
                                        tint = PurpleDark,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { }
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundLight,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.clickable { }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = TextSecondaryLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Add Tag",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondaryLight,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Pricing & Cadence Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = null,
                                    tint = Indigo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Pricing & Cadence",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy
                                    )
                                )
                            }
                            Text(
                                text = "USD Auto-Convert",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // List Price Input
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "List Price",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = listPrice,
                                    onValueChange = { listPrice = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = {
                                        Text(
                                            text = "$",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = Navy,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = BackgroundLight,
                                        unfocusedContainerColor = BackgroundLight,
                                        focusedBorderColor = Indigo,
                                        unfocusedBorderColor = BorderSubtle
                                    ),
                                    singleLine = true
                                )
                            }
                            // Cadence Dropdown
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cadence",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = BackgroundLight,
                                        border = BorderStroke(1.dp, BorderSubtle),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clickable { cadenceExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedCadence,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Navy,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = TextSecondaryLight
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = cadenceExpanded,
                                        onDismissRequest = { cadenceExpanded = false },
                                        modifier = Modifier.background(SurfaceLight)
                                    ) {
                                        cadenceOptions.forEach { cadence ->
                                            DropdownMenuItem(
                                                text = { Text(cadence) },
                                                onClick = {
                                                    selectedCadence = cadence
                                                    cadenceExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // 14-Day Free Trial Toggle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PurpleBg,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.HourglassEmpty,
                                                contentDescription = null,
                                                tint = Indigo,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "14-Day Free Trial",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Navy
                                            )
                                        )
                                        Text(
                                            text = "Zero charge upfront verification",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondaryLight
                                            )
                                        )
                                    }
                                }
                                Switch(
                                    checked = hasFreeTrial,
                                    onCheckedChange = { hasFreeTrial = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Indigo,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = BorderSubtle
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Inference & Quota Limits Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SmartToy,
                                    contentDescription = null,
                                    tint = Indigo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Inference & Quota Limits",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy
                                    )
                                )
                            }
                            Text(
                                text = "Cluster Alpha",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Indigo,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Monthly Included Credits Slider
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Monthly Included Credits",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = TextSecondaryLight,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Navy
                                    ) {
                                        Text(
                                            text = "${"%,d".format(monthlyCredits.roundToInt())} Credits",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Slider(
                                    value = monthlyCredits,
                                    onValueChange = { monthlyCredits = it },
                                    valueRange = 100f..10000f,
                                    steps = 99,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Indigo,
                                        activeTrackColor = Indigo,
                                        inactiveTrackColor = BorderSubtle
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("100 min", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                                    Text("5,000", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                                    Text("10,000 max", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Burst Rate Governor
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Burst Rate Governor",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy
                                        )
                                    )
                                    Text(
                                        text = "Maximum concurrent pipeline saturation",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondaryLight
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceLight,
                                    border = BorderStroke(1.dp, BorderSubtle)
                                ) {
                                    Text(
                                        text = "$burstGovernor req/min",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Navy,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Model Routing Access List
                        Text(
                            text = "MODEL ROUTING ACCESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModelAccessRow(
                                icon = Icons.Outlined.Psychology,
                                title = "GPT-4o Omnimodal",
                                subtitle = "Standard high-throughput routing",
                                isChecked = gpt4oEnabled,
                                onCheckedChange = { gpt4oEnabled = it }
                            )
                            ModelAccessRow(
                                icon = Icons.Outlined.Hub,
                                title = "Claude 3.5 Sonnet",
                                subtitle = "Advanced synthetic reasoning",
                                isChecked = claudeSonnetEnabled,
                                onCheckedChange = { claudeSonnetEnabled = it }
                            )
                            ModelAccessRow(
                                icon = Icons.Outlined.Share,
                                title = "Llama 3 70B Instruct",
                                subtitle = "NearAI Dedicated GPU Pods",
                                isChecked = llama70bEnabled,
                                onCheckedChange = { llama70bEnabled = it }
                            )
                            // Places & Search API Cache Bypass
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BackgroundLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { cacheBypassEnabled = !cacheBypassEnabled }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Checkbox(
                                        checked = cacheBypassEnabled,
                                        onCheckedChange = { cacheBypassEnabled = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Indigo,
                                            uncheckedColor = BorderSubtle
                                        )
                                    )
                                    Column {
                                        Text(
                                            text = "Places & Search API Cache Bypass",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Navy
                                            )
                                        )
                                        Text(
                                            text = "Enables real-time synthetic web queries without stale cache hits.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondaryLight
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Lifecycle Operations (Deprecate & Delete SKU)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lifecycle Operations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                            Text(
                                text = "STATE: DEPLOYED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Deprecate SKU Row
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Deprecate SKU",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy
                                        )
                                    )
                                    Button(
                                        onClick = onDeprecateSku,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BorderSubtle.copy(alpha = 0.8f),
                                            contentColor = Navy
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Archive SKU",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Existing active subscribers will continue to renew until bulk contract migration occurs.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryLight
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Delete Product SKU Row (Destructive)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoseBg,
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.WarningAmber,
                                            contentDescription = null,
                                            tint = RoseDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Delete Product SKU",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RoseDark
                                            )
                                        )
                                    }
                                    Button(
                                        onClick = onDeleteSku,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseDark),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Delete",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Permanently removes catalog entry. Requires root cluster administrator biometric re-auth.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = RoseDark
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 7. Bottom Sticky Action Buttons (Discard & Publish)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        modifier = Modifier
                            .weight(0.4f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BackgroundLight,
                            contentColor = Navy
                        ),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = "Discard",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Button(
                        onClick = onPublishAndSave,
                        modifier = Modifier
                            .weight(0.6f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Publish & Save SKU",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// =============================================================================
// Helper Component: Model Access Row
// =============================================================================

@Composable
fun ModelAccessRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = BackgroundLight,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryLight
                        )
                    )
                }
            }
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Indigo,
                    uncheckedColor = BorderSubtle
                )
            )
        }
    }
}

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
fun ProductEditScreenPreview() {
    MaterialTheme {
        ProductEditScreen()
    }
}
