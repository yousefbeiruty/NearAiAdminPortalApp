package com.nearai.admin.feature.orders.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.orders.domain.FulfillmentStatus
import com.nearai.admin.feature.orders.domain.OrderStatus
import com.nearai.admin.feature.orders.domain.OrderSummary

/**
 * Compact card for a single order in the orders list.
 */
@Composable
fun OrderSummaryCard(
    order: OrderSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: order ID on top, status pills below
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id.take(8)}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(
                            label = order.status.displayLabel,
                            tint = order.status.badgeTint
                        )
                        StatusBadge(
                            label = order.fulfillmentStatus.displayLabel,
                            tint = order.fulfillmentStatus.badgeTint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recipient and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = order.deliveryRecipientName ?: "No recipient",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = order.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${order.currency} ${"%.2f".format(order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

/**
 * Small status badge with label and background color.
 */
@Composable
fun StatusBadge(
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = tint.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = tint,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// =============================================================================
// Status display helpers
// =============================================================================

val OrderStatus.displayLabel: String
    get() = when (this) {
        OrderStatus.PENDING -> "Pending"
        OrderStatus.COMPLETED -> "Completed"
        OrderStatus.CANCELLED -> "Cancelled"
        OrderStatus.REJECTED -> "Rejected"
    }

val OrderStatus.badgeTint: Color
    get() = when (this) {
        OrderStatus.PENDING -> AmberWarning
        OrderStatus.COMPLETED -> EmeraldSuccess
        OrderStatus.CANCELLED -> TextSecondaryLight
        OrderStatus.REJECTED -> RoseError
    }

val FulfillmentStatus.displayLabel: String
    get() = when (this) {
        FulfillmentStatus.UNFULFILLED -> "Unfulfilled"
        FulfillmentStatus.AWAITING_CONFIRMATION -> "Awaiting"
        FulfillmentStatus.CONFIRMED -> "Confirmed"
        FulfillmentStatus.PROCESSING -> "Processing"
        FulfillmentStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
        FulfillmentStatus.DELIVERED -> "Delivered"
    }

val FulfillmentStatus.badgeTint: Color
    get() = when (this) {
        FulfillmentStatus.UNFULFILLED -> Indigo
        FulfillmentStatus.AWAITING_CONFIRMATION -> Indigo
        FulfillmentStatus.CONFIRMED -> EmeraldSuccess
        FulfillmentStatus.PROCESSING -> Indigo
        FulfillmentStatus.OUT_FOR_DELIVERY -> AmberWarning
        FulfillmentStatus.DELIVERED -> EmeraldSuccess
    }

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, widthDp = 390)
@Composable
fun OrderSummaryCardPreview() {
    NearAiAdminTheme {
        OrderSummaryCard(
            order = OrderSummary(
                id = "abc12345-def0-1234-5678-9abcdef01234",
                createdAt = "2024-01-15T10:30:00Z",
                status = OrderStatus.PENDING,
                fulfillmentStatus = FulfillmentStatus.AWAITING_CONFIRMATION,
                totalAmount = 45.50,
                subtotalAmount = 40.00,
                deliveryFeeAmount = 5.50,
                discountAmount = 0.0,
                currency = "JOD",
                items = emptyList(),
                deliveryRecipientName = "John Doe",
                deliveryPhone = "+1234567890",
                deliveryCity = null,
                deliveryArea = null,
                deliveryStreet = null,
                deliveryBuildingNumber = null,
                deliveryApartment = null,
                deliveryInstructions = null
            ),
            onClick = { }
        )
    }
}
