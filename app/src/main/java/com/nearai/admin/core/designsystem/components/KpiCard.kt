package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
