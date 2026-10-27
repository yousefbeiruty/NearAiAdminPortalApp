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
import com.nearai.admin.core.designsystem.theme.*

// =============================================================================
// UI Models
// =============================================================================

data class KpiMetric(
    val title: String,
    val value: String,
    val delta: String,
    val isPositive: Boolean,
    val icon: ImageVector,
    val deltaColorOverride: Color? = null
)

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
// Overview Dashboard Composable
// =============================================================================

/**
 * Overview Dashboard — high-fidelity admin dashboard with KPI metrics,
 * Bezier curve telemetry, inference histograms, budget gauges, and
 * a live activity stream.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToDetails: () -> Unit = {},
    onNavigateToSystemHealth: () -> Unit = {},
    onNavigateToAuditLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                            color = NavyPrimary,
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
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "OVERVIEW",
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
                                tint = TextPrimary
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
                            tint = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoAccent.copy(alpha = 0.15f))
                            .border(1.dp, IndigoAccent.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AF",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface
                )
            )
        },
        containerColor = BackgroundSurface
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
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
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "Alex",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
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
                                text = "Live · Synced 2m ago",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
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
                            selectedContainerColor = NavyPrimary,
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
                            containerColor = CardSurface
                        )
                    )
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text("GPT-4o / Claude 3.5") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardSurface
                        )
                    )
                }
            }

            // 3. Six-Card Executive KPI Grid
            item {
                val kpis = listOf(
                    KpiMetric("TOTAL USERS", "14,820", "↗ +12.4% vs last wk", true, Icons.Outlined.People),
                    KpiMetric("ACTIVE (DAU)", "3,410", "↗ +8.2% vs yest.", true, Icons.Outlined.Bolt),
                    KpiMetric("AI REQUESTS", "84.6k", "99.4% ok · 512 err", true, Icons.Outlined.Memory),
                    KpiMetric("AI SPEND", "$428.50", "● 71% of $600 cap", false, Icons.Outlined.AccountBalanceWallet, IndigoAccent),
                    KpiMetric("PLACES FOUND", "192.4k", "↑ +18% queries", true, Icons.Outlined.Explore),
                    KpiMetric("EST. REVENUE", "$4,820", "Pro + 18 trials", true, Icons.Outlined.CreditCard, IndigoAccent)
                )
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

            // 4. User Velocity & DAU Telemetry Curve
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "14-day rolling telemetry",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IndigoAccent.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = IndigoAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Peak: 3,410",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = IndigoAccent,
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

                                // Grid guidelines
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
                                            IndigoAccent.copy(alpha = 0.25f),
                                            IndigoAccent.copy(alpha = 0.02f)
                                        )
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    color = IndigoAccent,
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Apex indicator dot at Today's Peak
                                val peak = points.last()
                                drawCircle(
                                    color = IndigoAccent.copy(alpha = 0.3f),
                                    radius = 10.dp.toPx(),
                                    center = peak
                                )
                                drawCircle(
                                    color = NavyPrimary,
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
                            Text("Oct 11", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text("Oct 18", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(
                                "Today (All-Time High)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = IndigoAccent,
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
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Tokens / Sec vs Fallbacks (24h)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary
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
                                0.35f to IndigoAccent.copy(alpha = 0.25f),
                                0.45f to IndigoAccent.copy(alpha = 0.25f),
                                0.38f to IndigoAccent.copy(alpha = 0.25f),
                                0.55f to IndigoAccent.copy(alpha = 0.35f),
                                0.65f to IndigoAccent.copy(alpha = 0.4f),
                                0.72f to IndigoAccent.copy(alpha = 0.5f),
                                0.88f to NavyPrimary,
                                0.60f to IndigoAccent.copy(alpha = 0.35f),
                                0.50f to IndigoAccent.copy(alpha = 0.25f),
                                0.62f to RoseError.copy(alpha = 0.85f),
                                0.40f to IndigoAccent.copy(alpha = 0.25f)
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
                            color = BackgroundSurface,
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
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "$428.50 / $600.00",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
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
                                    color = IndigoAccent,
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
                                            color = IndigoAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "$171.50 remaining",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary
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
                                color = TextPrimary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "4 New",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = IndigoAccent,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "View audit log",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = IndigoAccent,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { /* TODO: onViewAuditLog */ }
                    )
                }
            }

            // 7. Live Activity Events List
            val events = listOf(
                ActivityStreamEvent(
                    id = "evt-1",
                    title = "New Pro Subscription",
                    subtitle = "sarah.k@gmail.com · Annual Plan",
                    timestamp = "4m ago",
                    statusLabel = "Active",
                    statusColor = EmeraldSuccess,
                    icon = Icons.Outlined.Diamond
                ),
                ActivityStreamEvent(
                    id = "evt-2",
                    title = "AI Budget Soft Cap (70%)",
                    subtitle = "Google Cloud Run / OpenAI Routing",
                    timestamp = "28m ago",
                    statusLabel = "Warning",
                    statusColor = AmberWarning,
                    icon = Icons.Outlined.WarningAmber
                ),
                ActivityStreamEvent(
                    id = "evt-3",
                    title = "Failed Discovery Query",
                    subtitle = "Places timeout (Tokyo, JP) · status 504",
                    timestamp = "1h ago",
                    statusLabel = "Investigate",
                    statusColor = RoseError,
                    icon = Icons.Outlined.ErrorOutline
                ),
                ActivityStreamEvent(
                    id = "evt-4",
                    title = "Batch User Import",
                    subtitle = "142 new signups from ProductHunt",
                    timestamp = "2h ago",
                    statusLabel = "Success",
                    statusColor = EmeraldSuccess,
                    icon = Icons.Outlined.CloudDone
                )
            )
            items(events) { event ->
                ActivityStreamCard(
                    event = event,
                    onClick = { /* TODO: onEventClick(event.id) */ }
                )
            }

            // 8. Cluster Status Operational Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
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
                                text = "Cluster Fleet 100% Operational",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "12 of 12 worker nodes responding smoothly",
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
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(IndigoAccent.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = kpi.icon,
                        contentDescription = null,
                        tint = IndigoAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
                text = kpi.delta,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = kpi.deltaColorOverride
                        ?: if (kpi.isPositive) EmeraldSuccess else TextSecondary,
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
        colors = CardDefaults.cardColors(containerColor = CardSurface),
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
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "• ${event.timestamp}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary
                        )
                    )
                }
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
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
// Design System Tokens (matching core design system)
// =============================================================================

val NavyPrimary = Color(0xFF1E1B4B)
val IndigoAccent = Color(0xFF6366F1)
val BackgroundSurface = Color(0xFFF7F9FB)
val CardSurface = Color(0xFFFFFFFF)
val BorderSubtle = Color(0xFFE2E8F0)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val EmeraldSuccess = Color(0xFF10B981)
val AmberWarning = Color(0xFFF59E0B)
val RoseError = Color(0xFFEF4444)

// =============================================================================
// Jetpack Compose Preview
// =============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OverviewDashboardPreview() {
    MaterialTheme {
        DashboardScreen(onNavigateToDetails = {}, onNavigateToSystemHealth = {})
    }
}
