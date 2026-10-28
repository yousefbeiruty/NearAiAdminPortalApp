package com.nearai.admin.feature.dashboard.presentation

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nearai.admin.core.designsystem.theme.*

// =============================================================================
// Helper: Map iconLabel from domain model to ImageVector
// =============================================================================

fun iconFromLabel(label: String): ImageVector {
    return when (label.lowercase()) {
        "people" -> Icons.Outlined.People
        "bolt" -> Icons.Outlined.Bolt
        "memory" -> Icons.Outlined.Memory
        "accountbalancewallet" -> Icons.Outlined.AccountBalanceWallet
        "explore" -> Icons.Outlined.Explore
        "creditcard" -> Icons.Outlined.CreditCard
        else -> Icons.Outlined.Dashboard
    }
}

// =============================================================================
// Helper: Map statusType to status color and icon
// =============================================================================

data class ActivityStreamEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val statusLabel: String,
    val statusColor: Color,
    val icon: ImageVector
)

// =============================================================================
// Screen-level KPI model (includes ImageVector icon for DashboardKpiCard)
// =============================================================================

data class KpiMetric(
    val title: String,
    val value: String,
    val delta: String,
    val isPositive: Boolean,
    val icon: ImageVector,
    val deltaColorOverride: Color? = null
)

fun ActivityEventItem.toActivityStreamEvent(): ActivityStreamEvent {
    val (color, icon) = when (statusType.lowercase()) {
        "success" -> EmeraldSuccess to Icons.Outlined.Diamond
        "warning" -> AmberWarning to Icons.Outlined.WarningAmber
        "error" -> RoseError to Icons.Outlined.ErrorOutline
        "info" -> Indigo to Icons.Outlined.Info
        else -> TextSecondaryLight to Icons.Outlined.Info
    }
    return ActivityStreamEvent(
        id = id,
        title = title,
        subtitle = subtitle,
        timestamp = timestamp,
        statusLabel = statusLabel,
        statusColor = color,
        icon = icon
    )
}

// =============================================================================
// Overview Dashboard Composable
// =============================================================================

/**
 * Overview Dashboard — connected to [DashboardViewModel] for state-driven rendering.
 *
 * Displays KPI metrics, telemetry charts, budget utilization, and live activity
 * stream from the ViewModel's StateFlow. Handles loading, error, and content states.
 *
 * Uses development-only fake data via [FakeDashboardRepository] until the backend
 * API is connected.
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
                                text = "OVERVIEW",
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
                                contentDescription = "Alerts & Notifications",
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
                            contentDescription = "Audit Logs",
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
        if (uiState.isLoading && uiState.kpiMetrics.isEmpty()) {
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
                        text = "Loading dashboard...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight
                        )
                    )
                }
            }
        } else if (uiState.error != null && uiState.kpiMetrics.isEmpty()) {
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
                        text = "Failed to load dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    val errorMessage = uiState.error ?: "Unknown error"
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
                            text = "Retry",
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
                onRefresh = { viewModel.onRetry() },
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
    val kpis: List<KpiMetric> = uiState.kpiMetrics.map { kpi ->
        KpiMetric(
            title = kpi.title,
            value = kpi.value,
            delta = kpi.delta,
            isPositive = kpi.isPositive,
            icon = iconFromLabel(kpi.title)
        )
    }

    val events = uiState.activityEvents.map { it.toActivityStreamEvent() }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Greeting & Synchronization Status
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good evening,",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight
                        )
                    )
                    Text(
                        text = "Alex",
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
                            text = if (uiState.lastUpdated.isNotBlank()) "Live · Synced ${uiState.lastUpdated}" else "Loading...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryLight,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // 2. Horizon & Cluster Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Last 24 Hours", fontWeight = FontWeight.Medium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("US-East Cluster") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Dns,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceLight
                    )
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("GPT-4o / Claude 3.5") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceLight
                    )
                )
            }
        }

        // 3. KPI Grid from ViewModel state
        item {
            if (kpis.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in kpis.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardKpiCard(kpi = kpis[i], modifier = Modifier.weight(1f))
                            if (i + 1 < kpis.size) {
                                DashboardKpiCard(kpi = kpis[i + 1], modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 4. User Velocity & DAU Telemetry Curve
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "User Velocity & DAU",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                            Text(
                                text = "14-day rolling telemetry",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryLight
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Indigo.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Indigo,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Peak: 3,410",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Indigo,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Smooth Bezier Curve Canvas with Gradient Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            drawLine(
                                color = BorderSubtle,
                                start = Offset(0f, h * 0.35f),
                                end = Offset(w, h * 0.35f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = BorderSubtle,
                                start = Offset(0f, h * 0.7f),
                                end = Offset(w, h * 0.7f),
                                strokeWidth = 1f
                            )

                            val points = listOf(
                                Offset(0f, h * 0.85f),
                                Offset(w * 0.14f, h * 0.78f),
                                Offset(w * 0.22f, h * 0.82f),
                                Offset(w * 0.35f, h * 0.65f),
                                Offset(w * 0.48f, h * 0.72f),
                                Offset(w * 0.58f, h * 0.60f),
                                Offset(w * 0.70f, h * 0.62f),
                                Offset(w * 0.82f, h * 0.45f),
                                Offset(w * 0.98f, h * 0.20f)
                            )

                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val cx = (p0.x + p1.x) / 2
                                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(w * 0.98f, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Indigo.copy(alpha = 0.25f),
                                        Indigo.copy(alpha = 0.02f)
                                    )
                                )
                            )

                            drawPath(
                                path = linePath,
                                color = Indigo,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            val peak = points.last()
                            drawCircle(
                                color = Indigo.copy(alpha = 0.3f),
                                radius = 10.dp.toPx(),
                                center = peak
                            )
                            drawCircle(
                                color = Navy,
                                radius = 5.dp.toPx(),
                                center = peak
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = peak
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Oct 11", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                        Text("Oct 18", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                        Text(
                            "Today (All-Time High)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Indigo,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 5. AI Inference Load & Monthly Envelope Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AI Inference Load & Retries",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                            )
                            Text(
                                text = "Tokens / Sec vs Fallbacks (24h)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryLight
                                )
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Text(
                                text = "99.4% Pass Rate",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Compact 24h Bar Histogram
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val bars = listOf(
                            0.35f to Indigo.copy(alpha = 0.25f),
                            0.45f to Indigo.copy(alpha = 0.25f),
                            0.38f to Indigo.copy(alpha = 0.25f),
                            0.55f to Indigo.copy(alpha = 0.35f),
                            0.65f to Indigo.copy(alpha = 0.4f),
                            0.72f to Indigo.copy(alpha = 0.5f),
                            0.88f to Navy,
                            0.60f to Indigo.copy(alpha = 0.35f),
                            0.50f to Indigo.copy(alpha = 0.25f),
                            0.62f to RoseError.copy(alpha = 0.85f),
                            0.40f to Indigo.copy(alpha = 0.25f)
                        )
                        bars.forEach { (heightFraction, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Monthly Compute Progress Gauge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BackgroundLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Monthly Compute Envelope",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Navy
                                    )
                                )
                                Text(
                                    text = "$428.50 / $600.00",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { 428.50f / 600f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Indigo,
                                trackColor = BorderSubtle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "● 71% Consumed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Indigo,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "$171.50 remaining",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryLight
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Live Activity Stream Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Live Activity Stream",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    if (events.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Indigo.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${events.size} New",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Indigo,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "View audit log",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Indigo,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { /* TODO: onViewAuditLog */ }
                )
            }
        }

        // 7. Live Activity Events from ViewModel state
        if (events.isNotEmpty()) {
            items(events) { event ->
                ActivityStreamCard(
                    event = event,
                    onClick = { /* TODO: onEventClick(event.id) */ }
                )
            }
        } else {
            item {
                EmptyStateCard()
            }
        }

        // 8. Cluster Status Operational Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSystemHealth() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.clusterStatus.isNotBlank()) "Cluster Fleet ${uiState.clusterStatus}" else "Cluster Fleet Status",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Tap to view system health details",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate to System Health",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Empty state card for activity stream when no events are available.
 */
@Composable
private fun EmptyStateCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EventNote,
                contentDescription = null,
                tint = TextSecondaryLight,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// =============================================================================
// Reusable KPI Metric Card
// =============================================================================

@Composable
fun DashboardKpiCard(
    kpi: KpiMetric,
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
                    text = kpi.title,
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
                        imageVector = kpi.icon,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = kpi.value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kpi.delta,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = kpi.deltaColorOverride
                        ?: if (kpi.isPositive) EmeraldSuccess else TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// =============================================================================
// Reusable Activity Stream Card Item
// =============================================================================

@Composable
fun ActivityStreamCard(
    event: ActivityStreamEvent,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    .background(event.statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon,
                    contentDescription = null,
                    tint = event.statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    Text(
                        text = "• ${event.timestamp}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryLight
                        )
                    )
                }
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryLight
                    )
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = event.statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = event.statusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = event.statusColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
        // Preview shows the loading state since Hilt ViewModel cannot be used in previews
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
                    text = "Loading dashboard...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondaryLight
                    )
                )
            }
        }
    }
}
