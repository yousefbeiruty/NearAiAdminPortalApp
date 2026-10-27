package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.nearai.admin.core.designsystem.theme.*

/**
 * Simple horizontal progress bar with threshold markers for budget visualization.
 */
@Composable
fun BudgetProgressBar(
    consumed: Double,
    total: Double,
    modifier: Modifier = Modifier,
    thresholds: Map<Double, Color> = mapOf(
        0.5 to EmeraldSuccess,
        0.75 to AmberWarning,
        0.9 to RoseError
    )
) {
    val percentage = (consumed / total).coerceIn(0.0, 1.0)
    val barColor = thresholds.entries
        .filter { it.key <= percentage }
        .maxByOrNull { it.key }?.value ?: EmeraldSuccess

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
