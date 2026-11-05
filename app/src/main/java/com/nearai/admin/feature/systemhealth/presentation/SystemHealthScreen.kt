package com.nearai.admin.feature.systemhealth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.core.designsystem.components.*
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.R
import com.nearai.admin.feature.systemhealth.domain.ApplicationHealth
import com.nearai.admin.feature.systemhealth.domain.ComponentHealth
import com.nearai.admin.feature.systemhealth.domain.SystemHealthData
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * System Health screen connected to the real backend API.
 *
 * Displays overall system status, application info, and component health
 * for database, storage, scheduler, and email.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHealthScreen(
    viewModel: SystemHealthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.system_health_title),
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
                    IconButton(onClick = { viewModel.onRefresh() }) {
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
        when {
            uiState.isLoading && uiState.healthData == null -> {
                LoadingScreen(modifier = modifier.fillMaxSize().padding(padding))
            }
            uiState.error != null && uiState.healthData == null -> {
                ErrorState(
                    message = uiState.error.orEmpty(),
                    modifier = modifier.fillMaxSize().padding(padding),
                    onRetry = { viewModel.clearError(); viewModel.onRefresh() }
                )
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error banner when health data exists but there was an error
                    if (uiState.error != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = RoseBg
                                )
                            ) {
                                Text(
                                    text = uiState.error.orEmpty(),
                                    modifier = Modifier.padding(12.dp),
                                    color = RoseDark,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Overall Status Banner
                    if (uiState.healthData != null) {
                        item {
                            OverallStatusBanner(
                                status = uiState.overallStatus,
                                checkedAt = uiState.checkedAt,
                                healthyCount = uiState.healthyCount,
                                totalComponents = uiState.components.size
                            )
                        }

                        // Application Info
                        item {
                            SectionHeader(
                                title = "Application",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        item {
                            ApplicationInfoCard(
                                environment = uiState.environment,
                                uptimeSeconds = uiState.uptimeSeconds
                            )
                        }

                        // Component Health
                        item {
                            SectionHeader(
                                title = "Components",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(uiState.components) { component ->
                            ComponentHealthCard(component = component)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overall system status banner showing health status and component counts.
 */
@Composable
fun OverallStatusBanner(
    status: String,
    checkedAt: String,
    healthyCount: Int,
    totalComponents: Int
) {
    val (statusColor, statusLabel) = when (status) {
        "healthy" -> EmeraldSuccess to "All Systems Operational"
        "degraded" -> AmberWarning to "System Degraded"
        "unhealthy" -> RoseError to "System Unhealthy"
        else -> Color.Gray to "Status Unknown"
    }

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
                            .background(statusColor)
                    )
                    Column {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                        Text(
                            text = formatCheckedAt(checkedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(
                    label = "$healthyCount/$totalComponents healthy",
                    dotColor = statusColor
                )
            }
        }
    }
}

/**
 * Application info card showing environment and uptime.
 */
@Composable
fun ApplicationInfoCard(
    environment: String,
    uptimeSeconds: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            HealthInfoRow(
                label = "Environment",
                value = environment.ifBlank { "Unknown" }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            HealthInfoRow(
                label = "Uptime",
                value = formatUptime(uptimeSeconds)
            )
        }
    }
}

/**
 * Component health card for a single infrastructure component.
 */
@Composable
fun ComponentHealthCard(
    component: ComponentHealth
) {
    val (statusColor, statusLabel) = when (component.status) {
        "healthy" -> EmeraldSuccess to "Healthy"
        "degraded" -> AmberWarning to "Degraded"
        "unhealthy" -> RoseError to "Unhealthy"
        "not_configured" -> Color.Gray to "Not Configured"
        else -> Color.Gray to component.status.replace("_", " ").replaceFirstChar { it.uppercase(Locale.US) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                            .background(statusColor)
                    )
                    Text(
                        text = formatComponentName(component.name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                StatusPill(
                    label = statusLabel,
                    dotColor = statusColor
                )
            }

            // Display component-specific details
            component.details?.forEach { (key, value) ->
                Spacer(modifier = Modifier.height(6.dp))
                when (key) {
                    "latency_ms" -> {
                        HealthInfoRow(
                            label = "Latency",
                            value = "${value}ms"
                        )
                    }
                    "backend" -> {
                        HealthInfoRow(
                            label = "Backend",
                            value = value
                        )
                    }
                    "provider" -> {
                        HealthInfoRow(
                            label = "Provider",
                            value = value
                        )
                    }
                    "running" -> {
                        HealthInfoRow(
                            label = "Running",
                            value = value
                        )
                    }
                    "error" -> {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoseError,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    "note" -> {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    else -> {
                        HealthInfoRow(
                            label = key.replace("_", " ").replaceFirstChar { it.uppercase(Locale.US) },
                            value = value
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simple info row for health cards.
 */
@Composable
fun HealthInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Formatting helpers ──────────────────────────────────────────────────────

private fun formatComponentName(name: String): String {
    return name.replace("_", " ").split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase(Locale.US) }
    }
}

private fun formatUptime(seconds: Long): String {
    return if (seconds <= 0) {
        "Just started"
    } else {
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("$hours h ")
            append("$minutes min")
        }.trim()
    }
}

private fun formatCheckedAt(checkedAt: String): String {
    return if (checkedAt.isBlank()) {
        "Unknown"
    } else {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            val outputFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)
            val date = inputFormat.parse(checkedAt)
            date?.let { "Checked ${outputFormat.format(it)}" } ?: "Checked $checkedAt"
        } catch (_: Exception) {
            "Checked $checkedAt"
        }
    }
}

/**
 * Preview for the System Health screen using sample data.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun SystemHealthScreenPreview() {
    val sampleData = SystemHealthData(
        overallStatus = "healthy",
        checkedAt = "2026-11-04T18:30:00Z",
        application = ApplicationHealth("healthy", "dev", 3600),
        components = listOf(
            ComponentHealth("database", "healthy", mapOf("latency_ms" to "12")),
            ComponentHealth("storage", "healthy", mapOf("backend" to "local")),
            ComponentHealth("scheduler", "healthy", mapOf("running" to "true")),
            ComponentHealth("email", "healthy", mapOf("provider" to "mock"))
        )
    )

    NearAiAdminTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OverallStatusBanner(
                    status = sampleData.overallStatus,
                    checkedAt = sampleData.checkedAt,
                    healthyCount = sampleData.components.count { it.status == "healthy" },
                    totalComponents = sampleData.components.size
                )
            }
            item {
                ApplicationInfoCard(
                    environment = sampleData.application.environment,
                    uptimeSeconds = sampleData.application.uptimeSeconds
                )
            }
            items(sampleData.components) { component ->
                ComponentHealthCard(component = component)
            }
        }
    }
}
