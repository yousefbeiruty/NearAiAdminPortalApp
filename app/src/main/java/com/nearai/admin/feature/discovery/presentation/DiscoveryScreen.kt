package com.nearai.admin.feature.discovery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nearai.admin.core.designsystem.theme.Indigo
import com.nearai.admin.core.designsystem.theme.Navy

/**
 * Places & Discovery Monitoring screen with pipeline telemetry,
 * real-time search feed, and trace inspector deep-dive.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */

// =============================================================================
// UI Models
// =============================================================================

data class DiscoveryKpi(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color = Indigo,
    val subtitleColor: Color = Color(0xFF64748B)
)

data class IngestionItem(
    val id: String,
    val query: String,
    val location: String,
    val userTag: String,
    val placeCount: Int,
    val latencyMs: Int,
    val statusLabel: String,
    val isDegraded: Boolean = false,
    val icon: ImageVector = Icons.Outlined.NearMe
)

data class DiscoveredPlace(
    val id: String,
    val name: String,
    val tags: String,
    val distance: String,
    val rating: String,
    val score: String,
    val isOpen: Boolean,
    val imageUrl: String
)

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
private val RoseError = Color(0xFFEF4444)
private val RoseBg = Color(0xFFFEF2F2)
private val DarkClusterBg = Color(0xFF062E25)

// =============================================================================
// Discovery Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReplayTrace: () -> Unit = {},
    onInspectJson: () -> Unit = {},
    onSelectTrace: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All (48.9k)", "Cache Hit", "Cache Miss", "Places API Live")

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
                                    imageVector = Icons.Default.Explore,
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
                                text = "DISCOVERY",
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
            // 1. Cluster Status Pill Bar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DarkClusterBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                                text = "CLUSTER: AP-NORTHEAST-1",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "GEO-PIPELINE v2.4",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Live 1s",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. Discovery KPI 2x2 Grid
            item {
                val discoveryKpis = listOf(
                    DiscoveryKpi(
                        title = "DISCOVERY VOL",
                        value = "48,910",
                        subtitle = "↗ +22.4% vs w-1",
                        icon = Icons.Outlined.Explore,
                        subtitleColor = EmeraldDark
                    ),
                    DiscoveryKpi(
                        title = "PLACES SPEND",
                        value = "$312.40",
                        subtitle = "GCP Billing Pool A",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        subtitleColor = TextSecondary
                    ),
                    DiscoveryKpi(
                        title = "AVG LATENCY",
                        value = "240ms",
                        subtitle = "FastAPI + Places",
                        icon = Icons.Outlined.Speed,
                        subtitleColor = TextSecondary
                    ),
                    DiscoveryKpi(
                        title = "CACHE HIT",
                        value = "68.4%",
                        subtitle = "⚡ Redis Geo Cluster",
                        icon = Icons.Outlined.Memory,
                        subtitleColor = EmeraldDark
                    )
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in discoveryKpis.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DiscoveryKpiCard(kpi = discoveryKpis[i], modifier = Modifier.weight(1f))
                            if (i + 1 < discoveryKpis.size) {
                                DiscoveryKpiCard(kpi = discoveryKpis[i + 1], modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 3. Search Filter Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Filter searches by query, city, or status...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        focusedBorderColor = Navy,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    singleLine = true
                )
            }

            // 4. Ingestion Filter Pills
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
                                containerColor = CardSurface,
                                labelColor = TextSecondary
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

            // 5. Real-Time Ingestion Feed Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME INGESTION FEED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Tap row to inspect",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            // Ingestion items
            val ingestionItems = listOf(
                IngestionItem(
                    id = "trc-9942a",
                    query = "\"Quiet cafes with power outlets near...\"",
                    location = "Tokyo, JP",
                    userTag = "user_8492",
                    placeCount = 12,
                    latencyMs = 210,
                    statusLabel = "200 OK",
                    icon = Icons.Outlined.NearMe
                ),
                IngestionItem(
                    id = "trc-9942b",
                    query = "\"Authentic sourdough bakeries\"",
                    location = "San Francisco, CA",
                    userTag = "maya.lin",
                    placeCount = 8,
                    latencyMs = 185,
                    statusLabel = "200 OK",
                    icon = Icons.Outlined.Storefront
                ),
                IngestionItem(
                    id = "trc-9942c",
                    query = "\"Late night pharmacy open now\"",
                    location = "Austin, TX",
                    userTag = "user_1904",
                    placeCount = 3,
                    latencyMs = 890,
                    statusLabel = "Degraded",
                    isDegraded = true,
                    icon = Icons.Outlined.WarningAmber
                )
            )
            items(ingestionItems) { item ->
                IngestionRowCard(
                    item = item,
                    onClick = { onSelectTrace(item.id) }
                )
            }

            // 6. Trace Inspector & Semantic NLP Extraction Card
            item {
                TraceInspectorSection(
                    traceId = "#trc-9942a",
                    promptPayload = "\"Cozy bookstores with craft coffee\"",
                    clientContext = "Client: iOS Native 4.12.0\nLoc: 37.7749° N, 122.4194° W",
                    onReplayTrace = onReplayTrace,
                    onInspectJson = onInspectJson
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// =============================================================================
// Discovery KPI Card
// =============================================================================

@Composable
fun DiscoveryKpiCard(
    kpi: DiscoveryKpi,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                    text = kpi.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Icon(
                    imageVector = kpi.icon,
                    contentDescription = null,
                    tint = kpi.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = kpi.value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kpi.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = kpi.subtitleColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// =============================================================================
// Real-Time Ingestion Row Item
// =============================================================================

@Composable
fun IngestionRowCard(
    item: IngestionItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.isDegraded) AmberWarning else Indigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.query,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (item.isDegraded) RoseBg else EmeraldBg
                ) {
                    Text(
                        text = item.statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (item.isDegraded) RoseError else EmeraldDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.location} • ${item.userTag}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${item.placeCount} places",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${item.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (item.latencyMs > 500) RoseError else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

// =============================================================================
// Trace Inspector & Pipeline Waterfall Section
// =============================================================================

@Composable
fun TraceInspectorSection(
    traceId: String,
    promptPayload: String,
    clientContext: String,
    onReplayTrace: () -> Unit,
    onInspectJson: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                        imageVector = Icons.Outlined.Radar,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "TRACE INSPECTOR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Indigo.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Trace ID: $traceId",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Indigo,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Prompt Payload Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BackgroundSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PROMPT PAYLOAD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Privacy Masked [PII Cleared]",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = promptPayload,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = clientContext,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Semantic Intent Extraction Pills
            Text(
                text = "SEMANTIC INTENT EXTRACTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IntentTag(icon = Icons.Outlined.MenuBook, label = "Bookstore", isPrimary = true)
                IntentTag(icon = Icons.Outlined.LocalCafe, label = "Coffee", isPrimary = true)
                IntentTag(label = "Ambience: Quiet", isPrimary = false)
                IntentTag(label = "Vibe: Cozy", isPrimary = false)
                IntentTag(label = "Radius: 3.5km", isPrimary = false)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Pipeline Latency Waterfall
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PIPELINE LATENCY WATERFALL (265MS TOTAL)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "PASS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Multi-segment horizontal progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(120f)
                        .fillMaxHeight()
                        .background(Indigo)
                )
                Box(
                    modifier = Modifier
                        .weight(85f)
                        .fillMaxHeight()
                        .background(Indigo.copy(alpha = 0.55f))
                )
                Box(
                    modifier = Modifier
                        .weight(60f)
                        .fillMaxHeight()
                        .background(EmeraldSuccess)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Nearby Search", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("120ms", style = MaterialTheme.typography.labelSmall.copy(color = Indigo, fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Details Batch", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("85ms", style = MaterialTheme.typography.labelSmall.copy(color = Indigo.copy(alpha = 0.75f), fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("AI Ranker", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("60ms", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDark, fontWeight = FontWeight.Bold))
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Discovered Places Carousel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVERED PLACES (6 RESULTS)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Top Relevance",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Indigo,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val places = listOf(
                DiscoveredPlace(
                    id = "pl-1",
                    name = "The Reader's Archive",
                    tags = "Single-origin drip · Silent attic",
                    distance = "0.4 mi",
                    rating = "4.9",
                    score = "98%",
                    isOpen = true,
                    imageUrl = "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=600&q=80"
                ),
                DiscoveredPlace(
                    id = "pl-2",
                    name = "Black Oak Books & Roastery",
                    tags = "Artisan roasts · Rare prints",
                    distance = "0.8 mi",
                    rating = "4.8",
                    score = "94%",
                    isOpen = true,
                    imageUrl = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=600&q=80"
                )
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(places) { place ->
                    DiscoveredPlaceCard(place = place)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Action Triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReplayTrace,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replay Trace", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onInspectJson,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DataObject,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inspect Raw JSON", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// =============================================================================
// Helper Chip for Semantic NLP Tags
// =============================================================================

@Composable
fun IntentTag(
    label: String,
    icon: ImageVector? = null,
    isPrimary: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPrimary) Indigo.copy(alpha = 0.12f) else BackgroundSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrimary) Indigo.copy(alpha = 0.3f) else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Indigo else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isPrimary) Indigo else TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// =============================================================================
// Discovered Place Carousel Card
// =============================================================================

@Composable
fun DiscoveredPlaceCard(place: DiscoveredPlace) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.width(220.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Rating pill overlay
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Navy.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = place.rating,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = place.tags,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${place.distance} • Open now",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldDark,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Score ${place.score}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Indigo,
                            fontWeight = FontWeight.Bold
                        )
                    )
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
fun DiscoveryScreenPreview() {
    MaterialTheme {
        DiscoveryScreen()
    }
}
