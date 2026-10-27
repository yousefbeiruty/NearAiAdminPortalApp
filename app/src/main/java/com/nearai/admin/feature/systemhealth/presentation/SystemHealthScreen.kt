package com.nearai.admin.feature.systemhealth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.components.*
import com.nearai.admin.core.designsystem.theme.*

/**
 * System Health & Infrastructure screen with fleet status banner,
 * service health cards, deployment info, incident journal,
 * and quick administrative actions.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHealthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: refresh */ }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fleet Status Banner
            item {
                FleetStatusBanner()
            }

            // Service Health Cards
            item {
                SectionHeader(
                    title = "Service Health",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                ServiceHealthSection()
            }

            // Deployment Info
            item {
                SectionHeader(
                    title = "Deployment Info",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                DeploymentInfoSection()
            }

            // Incident Journal
            item {
                SectionHeader(
                    title = "Incident Journal",
                    actionText = "View All",
                    onActionClick = { /* TODO */ }
                )
            }
            item {
                IncidentJournalSection()
            }

            // Quick Administrative Actions
            item {
                SectionHeader(
                    title = "Quick Actions",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                QuickActionsSection()
            }
        }
    }
}

/**
 * Fleet status banner showing 30-day uptime and incident counter.
 */
@Composable
fun FleetStatusBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Column {
                        Text(
                            text = "Fleet Operational",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "All systems running normally",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(
                    label = "0 incidents",
                    dotColor = Color(0xFF10B981)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "99.98%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "30-day fleet uptime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "5/5",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "services healthy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Service health cards for each infrastructure component.
 */
@Composable
fun ServiceHealthSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ServiceHealthCard(
            name = "FastAPI Backend",
            platform = "Cloud Run",
            status = ServiceStatus.Healthy,
            p95Latency = "45ms",
            errorRate = "0.02%",
            uptime = "99.99%"
        )
        ServiceHealthCard(
            name = "PostgreSQL",
            platform = "Cloud SQL",
            status = ServiceStatus.Healthy,
            p95Latency = "12ms",
            errorRate = "0.00%",
            uptime = "100.00%"
        )
        ServiceHealthCard(
            name = "AI Inference Pipeline",
            platform = "Cloud Run",
            status = ServiceStatus.Healthy,
            p95Latency = "320ms",
            errorRate = "0.15%",
            uptime = "99.95%"
        )
        ServiceHealthCard(
            name = "Google Places API",
            platform = "External",
            status = ServiceStatus.Degraded,
            p95Latency = "240ms",
            errorRate = "0.85%",
            uptime = "99.80%"
        )
        ServiceHealthCard(
            name = "Redis Edge Cache",
            platform = "Memorystore",
            status = ServiceStatus.Healthy,
            p95Latency = "3ms",
            errorRate = "0.00%",
            uptime = "100.00%"
        )
    }
}

/**
 * Individual service health card.
 */
@Composable
fun ServiceHealthCard(
    name: String,
    platform: String,
    status: ServiceStatus,
    p95Latency: String,
    errorRate: String,
    uptime: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(status.color)
                    )
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = platform,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(
                    label = status.label,
                    dotColor = status.color
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ServiceMetric(label = "p95 Latency", value = p95Latency)
                ServiceMetric(label = "Error Rate", value = errorRate, warning = status == ServiceStatus.Degraded)
                ServiceMetric(label = "Uptime", value = uptime)
            }
        }
    }
}

/**
 * Service metric display.
 */
@Composable
fun ServiceMetric(
    label: String,
    value: String,
    warning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (warning) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class ServiceStatus(
    val label: String,
    val color: Color
) {
    Healthy("Healthy", Color(0xFF10B981)),
    Degraded("Degraded", Color(0xFFF59E0B)),
    Down("Down", Color(0xFFEF4444))
}

/**
 * Deployment info section with release hash and CI/CD build info.
 */
@Composable
fun DeploymentInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            DeploymentInfoRow(
                label = "Active Release",
                value = "#8f31c0a",
                icon = Icons.Default.Tag
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DeploymentInfoRow(
                label = "CI/CD Build",
                value = "Build 8902-prod · Oct 27, 2026",
                icon = Icons.Default.Build
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DeploymentInfoRow(
                label = "Environment",
                value = "gcp-nearai-prod · us-central1",
                icon = Icons.Default.Cloud
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DeploymentInfoRow(
                label = "TLS",
                value = "1.3 · Verified",
                icon = Icons.Default.Lock
            )
        }
    }
}

/**
 * Deployment info row.
 */
@Composable
fun DeploymentInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Incident journal with recent automated recovery logs.
 */
@Composable
fun IncidentJournalSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IncidentItem(
            title = "Auto-scaled AI Inference",
            description = "Cloud Run scaled from 3 to 8 instances to handle traffic spike",
            timeAgo = "2h ago",
            status = ActivityStatus.Info
        )
        IncidentItem(
            title = "Redis Cache Eviction",
            description = "Memory threshold reached, LRU eviction cleared 12% of cache entries",
            timeAgo = "5h ago",
            status = ActivityStatus.Warning
        )
        IncidentItem(
            title = "PostgreSQL Connection Pool Reset",
            description = "Idle connections reclaimed, pool size normalized from 95 to 40",
            timeAgo = "1d ago",
            status = ActivityStatus.Success
        )
    }
}

/**
 * Individual incident journal item.
 */
@Composable
fun IncidentItem(
    title: String,
    description: String,
    timeAgo: String,
    status: ActivityStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.CenterVertically)
                    .clip(CircleShape)
                    .background(status.color)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Quick administrative actions section.
 */
@Composable
fun QuickActionsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickActionButton(
            title = "Purge Edge Cache",
            description = "Clear all Redis edge cache entries for fresh data",
            icon = Icons.Default.Cached,
            onClick = { /* TODO: Purge cache */ }
        )
        QuickActionButton(
            title = "Export Telemetry",
            description = "Download 30-day telemetry data as CSV",
            icon = Icons.Default.Download,
            onClick = { /* TODO: Export telemetry */ }
        )
        QuickActionButton(
            title = "Maintenance Mode",
            description = "Enable maintenance mode to pause non-critical operations",
            icon = Icons.Default.DoNotDisturb,
            onClick = { /* TODO: Toggle maintenance mode */ },
            isDestructive = true
        )
    }
}

/**
 * Quick action button card.
 */
@Composable
fun QuickActionButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isDestructive) Color(0xFFEF4444).copy(alpha = 0.1f)
                        else Indigo.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) Color(0xFFEF4444) else Indigo,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) Color(0xFFEF4444)
                            else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
