package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
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
 * Displays a colored status dot with a label.
 */
@Composable
fun StatusIndicator(
    status: StatusType,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = when (status) {
            StatusType.Active -> MaterialTheme.colorScheme.primary
            StatusType.Inactive -> MaterialTheme.colorScheme.onSurfaceVariant
            StatusType.Warning -> AmberWarning
            StatusType.Error -> MaterialTheme.colorScheme.error
        }
        val icon = when (status) {
            StatusType.Active -> Icons.Default.CheckCircle
            StatusType.Inactive -> Icons.Default.Info
            StatusType.Warning -> Icons.Default.WarningAmber
            StatusType.Error -> Icons.Default.Error
        }
        Icon(
            imageVector = icon,
            contentDescription = status.label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class StatusType(val label: String) {
    Active("Active"),
    Inactive("Inactive"),
    Warning("Warning"),
    Error("Error")
}

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

enum class ActivityStatus(
    val label: String,
    val color: Color
) {
    Success("Success", EmeraldSuccess),
    Warning("Warning", AmberWarning),
    Error("Error", RoseError),
    Info("Info", Indigo)
}

/**
 * Preview for StatusIndicator.
 */
@Preview(showBackground = true, widthDp = 300)
@Composable
fun StatusIndicatorPreview() {
    NearAiAdminTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            StatusIndicator(status = StatusType.Active, label = "Active")
            StatusIndicator(status = StatusType.Warning, label = "Warning")
            StatusIndicator(status = StatusType.Error, label = "Error")
            StatusIndicator(status = StatusType.Inactive, label = "Inactive")
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
            StatusPill(label = "Active", dotColor = EmeraldSuccess)
            StatusPill(label = "Warning", dotColor = AmberWarning)
            StatusPill(label = "Error", dotColor = RoseError)
        }
    }
}
