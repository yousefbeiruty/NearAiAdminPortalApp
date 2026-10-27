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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.*

/**
 * Displays a single KPI metric with a label, value, and optional trend indicator.
 */
@Composable
fun KpiCard(
    label: String,
    value: String,
    trend: KpiTrend? = null,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                icon?.invoke()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            trend?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val trendColor = when (it) {
                        is KpiTrend.Up -> MaterialTheme.colorScheme.primary
                        is KpiTrend.Down -> MaterialTheme.colorScheme.error
                    }
                    val trendPrefix = when (it) {
                        is KpiTrend.Up -> "↑"
                        is KpiTrend.Down -> "↓"
                    }
                    Text(
                        text = "$trendPrefix ${it.percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = it.period,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

sealed interface KpiTrend {
    val percentage: Int
    val period: String
    data class Up(override val percentage: Int, override val period: String) : KpiTrend
    data class Down(override val percentage: Int, override val period: String) : KpiTrend
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

/**
 * Preview for KpiCard with upward trend.
 */
@Preview(showBackground = true, widthDp = 300)
@Composable
fun KpiCardPreview() {
    NearAiAdminTheme {
        KpiCard(
            label = "Total Users",
            value = "14,820",
            trend = KpiTrend.Up(12, "vs last week")
        )
    }
}

/**
 * Preview for KpiCard without trend.
 */
@Preview(showBackground = true, widthDp = 300)
@Composable
fun KpiCardNoTrendPreview() {
    NearAiAdminTheme {
        KpiCard(
            label = "AI Requests",
            value = "84.6k"
        )
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
