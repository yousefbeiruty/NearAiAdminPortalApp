package com.nearai.admin.feature.auditlogs.presentation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nearai.admin.core.designsystem.theme.Indigo
import com.nearai.admin.core.designsystem.theme.Navy

/**
 * Audit Logs & Settings screen with chronological audit stream, security key management,
 * governance policy toggles, and cryptographic bundle export.
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
private val DarkClusterBg = Color(0xFF062E25)

// =============================================================================
// UI Data Models
// =============================================================================

enum class AuditStatus(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
) {
    SUCCESS("SUCCESS · +500", EmeraldDark, EmeraldBg),
    CONFIG_COMMITTED("CONFIG_COMMITTED", PurpleDark, PurpleBg),
    SUSPENDED("SUSPENDED · 403", RoseDark, RoseBg)
}

data class AuditLogEntry(
    val id: String,
    val title: String,
    val actor: String,
    val timeAgo: String,
    val status: AuditStatus,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val targetLabel: String,
    val targetValue: String,
    val description: String? = null,
    val isAlertDescription: Boolean = false,
    val diffFrom: String? = null,
    val diffTo: String? = null,
    val originIp: String? = null,
    val actionText: String? = null,
    val actionSecondaryText: String? = null
)

// =============================================================================
// Audit Logs & Settings Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(
    onNavigateBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onInspectJson: (String) -> Unit = {},
    onDiffView: (String) -> Unit = {},
    onUnbanIp: (String) -> Unit = {},
    onCopyApiKey: () -> Unit = {},
    onRotateApiKey: () -> Unit = {},
    onExportAuditBundle: () -> Unit = {}
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All Actions (842)", "Credit Grants", "Config", "Security")

    // Policy Toggles
    var mfaRequired by remember { mutableStateOf(true) }
    var telemetryThreshold by remember { mutableStateOf("30s") }
    var sessionTimeout by remember { mutableStateOf("15m") }

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
                                    imageVector = Icons.Default.Shield,
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
                                text = "AUDIT LOGS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
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
                                contentDescription = "Alerts",
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
        containerColor = BackgroundSurface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Navigation Pill Tabs (Audit Logs vs System Settings)
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Navy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Audit Logs",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "System Settings",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // 2. Production Security Cluster Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkClusterBg,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Production (gcp-nearai-pr...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    maxLines = 1
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                            }
                            Text(
                                text = "Zero-Trust Strict Policy Active ...",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 1
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "Filter",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterTabs.forEachIndexed { index, title ->
                        val isSelected = selectedFilterIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterIndex = index },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Circle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = CardSurface,
                                labelColor = TextSecondary,
                                iconColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Navy else BorderSubtle
                            )
                        )
                    }
                }
            }

            // 4. Audit Stream Ingest Invariant Tile
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUDIT STREAM INGEST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "100% Retained",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "24.2 ev/min",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Indigo,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = "Immutable Write Log",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundSurface,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = Indigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section Header: Today · Chronological Stream
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
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Indigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Today · Chronological Stream",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Text(
                        text = "Sync: 4s ago",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // 6. Chronological Stream Entries
            val auditEvents = listOf(
                AuditLogEntry(
                    id = "tr_audit_883a9e4...92c",
                    title = "Granted 500 Bonus Cr...",
                    actor = "alex.founder (Root)",
                    timeAgo = "12 min ago",
                    status = AuditStatus.SUCCESS,
                    icon = Icons.Outlined.CreditCard,
                    iconTint = EmeraldDark,
                    iconBg = EmeraldBg,
                    targetLabel = "Target User",
                    targetValue = "maya.lin@domain.com",
                    description = "Compensation for Places API elevated latency incident #8902",
                    isAlertDescription = false,
                    actionText = "Inspect JSON"
                ),
                AuditLogEntry(
                    id = "tr_audit_192bb3c...aa4",
                    title = "Circuit Breaker Arm...",
                    actor = "alex.founder",
                    timeAgo = "2 hours ago",
                    status = AuditStatus.CONFIG_COMMITTED,
                    icon = Icons.Outlined.Bolt,
                    iconTint = Indigo,
                    iconBg = PurpleBg,
                    targetLabel = "Compute Target",
                    targetValue = "AI Compute Pool Alpha",
                    diffFrom = "$600.00",
                    diffTo = "$650.00 / hr cap",
                    actionText = "Diff View"
                ),
                AuditLogEntry(
                    id = "tr_audit_004ee29...7f1",
                    title = "Account Suspension",
                    actor = "Automated Shield Policy",
                    timeAgo = "3 hours ago",
                    status = AuditStatus.SUSPENDED,
                    icon = Icons.Outlined.Block,
                    iconTint = RoseDark,
                    iconBg = RoseBg,
                    targetLabel = "Target Entity",
                    targetValue = "bot_probe_22",
                    originIp = "194.26.29.11 (ASN4982)",
                    description = "Exceeded burst rate limit (320 req/min vs 25 max allowed)",
                    isAlertDescription = true,
                    actionText = "Details",
                    actionSecondaryText = "Unban IP"
                )
            )

            items(auditEvents) { event ->
                AuditLogCard(
                    event = event,
                    onInspectJson = { onInspectJson(event.id) },
                    onDiffView = { onDiffView(event.id) },
                    onUnbanIp = { onUnbanIp(event.id) }
                )
            }

            // Sync Footer Callout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted stream synchronized with GCP Cloud Logging",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 7. Security & Key Management Section Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
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
                            imageVector = Icons.Outlined.Key,
                            contentDescription = null,
                            tint = Indigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Security & Key Management",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Indigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "KMS Managed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Indigo,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 8. Key Management Cards
            item {
                // Google Places API Key Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundSurface,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.VpnKey,
                                            contentDescription = null,
                                            tint = Navy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Google Places API Key",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Inference & GEO Enrichment",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldDark
                            ) {
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Masked API Key Display
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BackgroundSurface,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AIzaSyD...••••••••77a",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                )
                                IconButton(onClick = onCopyApiKey, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy API Key",
                                        tint = Indigo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last rotated: 14 days ago",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Button(
                                onClick = onRotateApiKey,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BackgroundSurface,
                                    contentColor = TextPrimary
                                ),
                                border = BorderStroke(1.dp, BorderSubtle),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RotateRight,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rotate Key", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Cloud Run Service Account Tile
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundSurface,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.CloudQueue,
                                            contentDescription = null,
                                            tint = Navy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Cloud Run Service Account",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                            Text(
                                text = "OAuth 2.0",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BackgroundSurface,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "nearai-core@appspot.gserviceaccount.com",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    ),
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Verified Service Account",
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 9. Governance Policies Section Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
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
                            imageVector = Icons.Outlined.Gavel,
                            contentDescription = null,
                            tint = Indigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Governance Policies",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Text(
                        text = "v2.4 Enforced",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // 10. Governance Policies Toggles List
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Policy 1: Require MFA for Grants > 1,000
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Require MFA for Grants > 1,000",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Prompts hardware token for high-quota credits",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            Switch(
                                checked = mfaRequired,
                                onCheckedChange = { mfaRequired = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Indigo,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BorderSubtle
                                )
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = BorderSubtle
                        )

                        // Policy 2: Stale Telemetry Threshold
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Stale Telemetry Threshold",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Heartbeat interval before node failsafe triggers",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundSurface,
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = telemetryThreshold,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.UnfoldMore,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = BorderSubtle
                        )

                        // Policy 3: Ephemeral Session Timeout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ephemeral Session Timeout",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Strict idle logout for mobile console sessions",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundSurface,
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = sessionTimeout,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.UnfoldMore,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 11. Bottom Action CTA: Export Cryptographic Audit Bundle
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onExportAuditBundle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Cryptographic Audit Bundle (.gz)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Signed SHA-256 fingerprint verified by Root Authority",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// =============================================================================
// Helper Component: Audit Log Card
// =============================================================================

@Composable
fun AuditLogCard(
    event: AuditLogEntry,
    onInspectJson: () -> Unit,
    onDiffView: () -> Unit,
    onUnbanIp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon, Title & Actor, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = event.iconBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = event.icon,
                            contentDescription = null,
                            tint = event.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${event.actor} · ${event.timeAgo}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = event.status.backgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(event.status.textColor)
                        )
                        Text(
                            text = event.status.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = event.status.textColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Body Detail Container
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BackgroundSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Target Key-Value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.targetLabel,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = event.targetValue,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (event.status == AuditStatus.SUSPENDED) RoseDark else TextPrimary
                            )
                        )
                    }

                    // Origin IPv4 (if present)
                    if (event.originIp != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Origin IPv4",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(
                                text = event.originIp,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    // Diff Values (if present)
                    if (event.diffFrom != null && event.diffTo != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Threshold Change",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = event.diffFrom,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                )
                                Text(
                                    text = "→ ${event.diffTo}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Indigo
                                    )
                                )
                            }
                        }
                    }

                    // Description / Incident Note
                    if (event.description != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (event.isAlertDescription) Icons.Outlined.WarningAmber else Icons.Outlined.Info,
                                contentDescription = null,
                                tint = if (event.isAlertDescription) RoseDark else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (event.isAlertDescription) RoseDark else TextSecondary,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Monospace Hash ID & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "# ${event.id}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.actionSecondaryText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BackgroundSurface,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.clickable { onUnbanIp() }
                        ) {
                            Text(
                                text = event.actionSecondaryText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (event.actionText != null) {
                        Row(
                            modifier = Modifier.clickable {
                                when (event.actionText) {
                                    "Inspect JSON" -> onInspectJson()
                                    "Diff View" -> onDiffView()
                                    else -> onInspectJson()
                                }
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = event.actionText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Indigo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Indigo,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
fun AuditLogsScreenPreview() {
    MaterialTheme {
        AuditLogsScreen()
    }
}
