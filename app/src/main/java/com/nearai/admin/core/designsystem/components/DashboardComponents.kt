package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme

/**
 * A small pill-shaped badge with a colored dot indicator and label.
 */
@Composable
fun StatusPill(
    label: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Activity stream item card showing a real-time event with status pill.
 */
@Composable
fun ActivityStreamCard(
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
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.CenterVertically)
                    .clip(CircleShape)
                    .background(status.color)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusPill(
                    label = status.label,
                    dotColor = status.color
                )
            }
        }
    }
}

enum class ActivityStatus(
    val label: String,
    val color: Color
) {
    Success("Success", Color(0xFF10B981)),
    Warning("Warning", Color(0xFFF59E0B)),
    Error("Error", Color(0xFFEF4444)),
    Info("Info", Color(0xFF6366F1))
}

/**
 * Simple horizontal progress bar with threshold markers for budget visualization.
 */
@Composable
fun BudgetProgressBar(
    consumed: Double,
    total: Double,
    modifier: Modifier = Modifier,
    thresholds: Map<Double, Color> = mapOf(
        0.5 to Color(0xFF10B981),   // 50% - green
        0.75 to Color(0xFFF59E0B),  // 75% - amber
        0.9 to Color(0xFFEF4444)    // 90% - red
    )
) {
    val percentage = (consumed / total).coerceIn(0.0, 1.0)
    val barColor = thresholds.entries
        .filter { it.key <= percentage }
        .maxByOrNull { it.key }?.value ?: Color(0xFF10B981)

    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = percentage.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$consumed of $total consumed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
    }
}

/**
 * Simple bar chart sparkline using a series of data points.
 */
@Composable
fun SimpleBarChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Int = 40
) {
    if (dataPoints.isEmpty()) return

    val max = dataPoints.maxOrNull() ?: 1f
    val min = dataPoints.minOrNull() ?: 0f
    val range = (max - min).coerceAtLeast(0.001f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        dataPoints.forEachIndexed { index, value ->
            val normalizedHeight = ((value - min) / range) * height
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(normalizedHeight.dp)
                    .align(Alignment.Bottom)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = 0.5f + (index.toFloat() / dataPoints.size) * 0.5f))
            )
        }
    }
}

/**
 * Preview for StatusPill.
 */
@Preview(showBackground = true, widthDp = 300)
@Composable
fun StatusPillPreview() {
    NearAiAdminTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            StatusPill(label = "Active", dotColor = Color(0xFF10B981))
            StatusPill(label = "Warning", dotColor = Color(0xFFF59E0B))
            StatusPill(label = "Error", dotColor = Color(0xFFEF4444))
        }
    }
}

/**
 * Preview for ActivityStreamCard.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun ActivityStreamCardPreview() {
    NearAiAdminTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            ActivityStreamCard(
                title = "New Pro Subscription",
                description = "sarah.k@gmail.com · Annual Plan",
                timeAgo = "4m ago",
                status = ActivityStatus.Success
            )
            ActivityStreamCard(
                title = "AI Budget Soft Cap (70%)",
                description = "Google Cloud Run / OpenAI Routing",
                timeAgo = "28m ago",
                status = ActivityStatus.Warning
            )
            ActivityStreamCard(
                title = "Failed Discovery Query",
                description = "Places timeout (Tokyo, JP) · status 504",
                timeAgo = "1h ago",
                status = ActivityStatus.Error
            )
        }
    }
}

/**
 * Preview for BudgetProgressBar.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun BudgetProgressBarPreview() {
    NearAiAdminTheme {
        BudgetProgressBar(
            consumed = 428.50,
            total = 600.00,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview for SimpleBarChart.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun SimpleBarChartPreview() {
    NearAiAdminTheme {
        SimpleBarChart(
            dataPoints = listOf(0.35f, 0.45f, 0.38f, 0.55f, 0.65f, 0.72f, 0.88f, 0.60f, 0.50f, 0.62f, 0.40f),
            modifier = Modifier.padding(16.dp),
            height = 60
        )
    }
}
