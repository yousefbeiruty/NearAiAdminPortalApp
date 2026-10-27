package com.nearai.admin.feature.aiusage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.components.*
import com.nearai.admin.core.designsystem.theme.*

/**
 * AI Usage & Budget Health screen with budget envelope, model distribution,
 * quota safeguards, and live ingestion telemetry.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiUsageScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Usage & Credits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
            // Budget Envelope Card
            item {
                BudgetEnvelopeCard()
            }

            // Provider & Model Distribution
            item {
                SectionHeader(
                    title = "Provider & Model Distribution",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                ModelDistributionSection()
            }

            // Quota Safeguards
            item {
                SectionHeader(
                    title = "Quota Safeguards",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                QuotaSafeguardsSection()
            }

            // Live Ingestion Telemetry
            item {
                SectionHeader(
                    title = "Live Ingestion Telemetry",
                    actionText = "View All",
                    onActionClick = { /* TODO */ }
                )
            }
            item {
                LiveIngestionSection()
            }
        }
    }
}

/**
 * Budget envelope card showing consumed vs total budget with visual thresholds.
 */
@Composable
fun BudgetEnvelopeCard() {
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
                Column {
                    Text(
                        text = "Monthly Budget Envelope",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Webhook dispatch notifications enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(
                    label = "71% used",
                    dotColor = AmberWarning
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Large budget display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "$428.50",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "of $600.00 consumed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$171.50",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldSuccess
                    )
                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            BudgetProgressBar(
                consumed = 428.50,
                total = 600.00
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Threshold markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThresholdMarker(label = "50%", value = "$300", reached = true)
                ThresholdMarker(label = "75%", value = "$450", reached = false)
                ThresholdMarker(label = "90%", value = "$540", reached = false)
            }
        }
    }
}

/**
 * Threshold marker indicator.
 */
@Composable
fun ThresholdMarker(
    label: String,
    value: String,
    reached: Boolean,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    if (reached) AmberWarning
                    else MaterialTheme.colorScheme.outlineVariant
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (reached) AmberWarning
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Model distribution section showing provider breakdown.
 */
@Composable
fun ModelDistributionSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ModelDistributionCard(
            provider = "OpenAI",
            model = "GPT-4o-mini",
            requests = "32.4k",
            percentage = 38,
            cost = "$186.20",
            color = EmeraldSuccess
        )
        ModelDistributionCard(
            provider = "Anthropic",
            model = "Claude 3.5 Sonnet",
            requests = "24.1k",
            percentage = 28,
            cost = "$142.80",
            color = Indigo
        )
        ModelDistributionCard(
            provider = "Self-Hosted",
            model = "Embedding Vectors",
            requests = "28.1k",
            percentage = 34,
            cost = "$99.50",
            color = AmberWarning
        )
    }
}

/**
 * Individual model distribution card.
 */
@Composable
fun ModelDistributionCard(
    provider: String,
    model: String,
    requests: String,
    percentage: Int,
    cost: String,
    color: Color,
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = provider.take(2).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Column {
                        Text(
                            text = model,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = provider,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = cost,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$requests requests",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percentage% of total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Quota safeguards configuration cards.
 */
@Composable
fun QuotaSafeguardsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QuotaSafeguardCard(
            title = "Default Free Tier",
            value = "100 credits/mo",
            description = "Per-user monthly credit limit for free tier accounts",
            status = "Active"
        )
        QuotaSafeguardCard(
            title = "Burst Rate Limits",
            value = "50 req/min",
            description = "Maximum requests per minute per user to prevent abuse",
            status = "Active"
        )
        QuotaSafeguardCard(
            title = "Budget Circuit Breaker",
            value = "Armed at 90%",
            description = "Automatically pauses AI inference when budget exceeds 90%",
            status = "Armed"
        )
    }
}

/**
 * Individual quota safeguard card.
 */
@Composable
fun QuotaSafeguardCard(
    title: String,
    value: String,
    description: String,
    status: String,
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
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    StatusPill(
                        label = status,
                        dotColor = if (status == "Active") EmeraldSuccess else AmberWarning
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Indigo
                )
            }
            IconButton(onClick = { /* TODO: Edit safeguard */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Live ingestion telemetry feed.
 */
@Composable
fun LiveIngestionSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IngestionTelemetryItem(
            model = "GPT-4o-mini",
            tokens = 1240,
            cost = "$0.082",
            latency = "340ms",
            status = TelemetryStatus.Success
        )
        IngestionTelemetryItem(
            model = "Claude 3.5 Sonnet",
            tokens = 2100,
            cost = "$0.156",
            latency = "520ms",
            status = TelemetryStatus.Success
        )
        IngestionTelemetryItem(
            model = "GPT-4o-mini",
            tokens = 0,
            cost = "$0.00",
            latency = "—",
            status = TelemetryStatus.RateLimited
        )
        IngestionTelemetryItem(
            model = "Embedding Vector",
            tokens = 450,
            cost = "$0.012",
            latency = "85ms",
            status = TelemetryStatus.Success
        )
    }
}

/**
 * Individual telemetry feed item.
 */
@Composable
fun IngestionTelemetryItem(
    model: String,
    tokens: Int,
    cost: String,
    latency: String,
    status: TelemetryStatus,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(status.color)
                )
                Column {
                    Text(
                        text = model,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = status.color
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "$tokens tokens",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = cost,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = latency,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class TelemetryStatus(
    val label: String,
    val color: Color
) {
    Success("Completed", EmeraldSuccess),
    RateLimited("429 Rate Limited", RoseError),
    Timeout("Timeout", AmberWarning)
}

/**
 * Preview for the AI Usage & Credits screen.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AiUsageScreenPreview() {
    NearAiAdminTheme {
        AiUsageScreen()
    }
}
