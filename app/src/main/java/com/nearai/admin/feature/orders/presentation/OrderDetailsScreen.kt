package com.nearai.admin.feature.orders.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.orders.domain.*

/**
 * Order details screen with order info, delivery, items, payment, status history, and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Rejection dialog
    if (uiState.showRejectDialog) {
        RejectDialog(
            reason = uiState.rejectReason,
            onReasonChanged = { viewModel.onRejectReasonChanged(it) },
            onDismiss = { viewModel.dismissRejectDialog() },
            onConfirm = { viewModel.rejectOrder() },
            isLoading = uiState.isActionLoading
        )
    }

    // Confirmation dialog
    uiState.showConfirmDialog?.let { actionLabel ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = { Text("Confirm $actionLabel") },
            text = { Text("Are you sure you want to $actionLabel.lowercase() this order?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeConfirmedAction() },
                    enabled = !uiState.isActionLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    if (uiState.isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Yes, $actionLabel", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Cancel", color = TextSecondaryLight)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order Details",
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.onNavigateBack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Indigo)
                }
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error loading order", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.error.orEmpty(), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onRetry() }, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            else -> {
                val order = uiState.order ?: return@Scaffold
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Order info header
                    item {
                        OrderInfoCard(order)
                    }

                    // Customer & delivery
                    item {
                        CustomerDeliveryCard(order)
                    }

                    // Order items
                    item {
                        OrderItemsCard(order)
                    }

                    // Payment summary
                    item {
                        PaymentSummaryCard(order)
                    }

                    // Status history
                    if (uiState.statusHistory.isNotEmpty()) {
                        item {
                            StatusHistoryCard(uiState.statusHistory)
                        }
                    }

                    // Action buttons
                    item {
                        OrderActionButtons(
                            order = order,
                            isActionLoading = uiState.isActionLoading,
                            onConfirm = { viewModel.showConfirmDialog("Confirm") },
                            onReject = { viewModel.showRejectDialog() },
                            onProcess = { viewModel.showConfirmDialog("Process") },
                            onDispatch = { viewModel.showConfirmDialog("Dispatch") },
                            onDeliver = { viewModel.showConfirmDialog("Deliver") }
                        )
                    }

                    // Action error
                    if (uiState.actionError != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = RoseBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = RoseError, modifier = Modifier.size(20.dp))
                                    Text(uiState.actionError.orEmpty(), style = MaterialTheme.typography.bodySmall.copy(color = RoseDark), modifier = Modifier.weight(1f))
                                    TextButton(onClick = { viewModel.clearActionError() }) {
                                        Text("Dismiss", color = RoseDark, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

/**
 * Order info card with ID, date, and status pills.
 */
@Composable
private fun OrderInfoCard(order: OrderDetails) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Order ID", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                    Text("#${order.id}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = Navy))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Created", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                    Text(order.createdAt.take(16), style = MaterialTheme.typography.bodyMedium.copy(color = Navy))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(label = "Order: ${order.status.displayLabel}", tint = order.status.badgeTint)
                StatusBadge(label = "Fulfillment: ${order.fulfillmentStatus.displayLabel}", tint = order.fulfillmentStatus.badgeTint)
            }
        }
    }
}

/**
 * Customer and delivery address card.
 */
@Composable
private fun CustomerDeliveryCard(order: OrderDetails) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Customer & Delivery", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Recipient", value = order.deliveryRecipientName ?: "—")
            InfoRow(label = "Phone", value = order.deliveryPhone ?: "—")
            if (!order.deliveryCity.isNullOrBlank()) {
                InfoRow(label = "City", value = order.deliveryCity)
            }
            if (!order.deliveryArea.isNullOrBlank()) {
                InfoRow(label = "Area", value = order.deliveryArea)
            }
            if (!order.deliveryStreet.isNullOrBlank()) {
                InfoRow(label = "Street", value = order.deliveryStreet)
            }
            if (!order.deliveryBuildingNumber.isNullOrBlank() || !order.deliveryApartment.isNullOrBlank()) {
                val building = listOf(order.deliveryBuildingNumber, order.deliveryApartment).filterNotNull().joinToString(", ")
                InfoRow(label = "Building", value = building)
            }
            if (!order.deliveryInstructions.isNullOrBlank()) {
                InfoRow(label = "Instructions", value = order.deliveryInstructions)
            }
        }
    }
}

/**
 * Order items card.
 */
@Composable
private fun OrderItemsCard(order: OrderDetails) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Items (${order.items.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            order.items.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy))
                        Text("Qty: ${item.quantity} × ${order.currency} ${"%.2f".format(item.unitPriceAmount)}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
                    }
                    Text("${order.currency} ${"%.2f".format(item.subtotalAmount ?: (item.unitPriceAmount * item.quantity))}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
                }
                if (index < order.items.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

/**
 * Payment summary card.
 */
@Composable
private fun PaymentSummaryCard(order: OrderDetails) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            order.subtotalAmount?.let {
                InfoRow(label = "Subtotal", value = "${order.currency} ${"%.2f".format(it)}")
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
                Text("${order.currency} ${"%.2f".format(order.totalAmount)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Navy))
            }
        }
    }
}

/**
 * Status history timeline card.
 */
@Composable
private fun StatusHistoryCard(entries: List<StatusHistoryEntry>) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Status History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            entries.forEachIndexed { index, entry ->
                StatusTimelineItem(entry = entry)
                if (index < entries.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * Single status timeline item.
 */
@Composable
private fun StatusTimelineItem(entry: StatusHistoryEntry) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Timeline dot
        Box(modifier = Modifier
            .padding(top = 4.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(if (entry.statusType == "order") Indigo else EmeraldSuccess))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.newStatus, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy))
                Text(entry.timestamp.take(16), style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight, fontFamily = FontFamily.Monospace))
            }
            if (!entry.previousStatus.isNullOrBlank()) {
                Text("from: ${entry.previousStatus}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
            }
            Text(entry.statusType.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = if (entry.statusType == "order") Indigo else EmeraldSuccess, fontWeight = FontWeight.Bold))
            if (!entry.actorRole.isNullOrBlank()) {
                Text("by: ${entry.actorRole}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
            }
            if (!entry.reason.isNullOrBlank()) {
                Text("Reason: ${entry.reason}", style = MaterialTheme.typography.bodySmall.copy(color = RoseDark))
            }
        }
    }
}

/**
 * Order action buttons based on current status.
 */
@Composable
private fun OrderActionButtons(
    order: OrderDetails,
    isActionLoading: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onProcess: () -> Unit,
    onDispatch: () -> Unit,
    onDeliver: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Actions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))

            // Confirm: available when order is pending and fulfillment is awaiting confirmation
            if (order.status == OrderStatus.PENDING && order.fulfillmentStatus == FulfillmentStatus.AWAITING_CONFIRMATION) {
                ActionButton(label = "Confirm Order", icon = Icons.Outlined.CheckCircle, onClick = onConfirm, isLoading = isActionLoading)
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(label = "Reject Order", icon = Icons.Outlined.Block, onClick = onReject, isLoading = isActionLoading, isDestructive = true)
            }

            // Process: available when order is pending and fulfillment is confirmed
            if (order.status == OrderStatus.PENDING && order.fulfillmentStatus == FulfillmentStatus.CONFIRMED) {
                ActionButton(label = "Process Order", icon = Icons.Outlined.Build, onClick = onProcess, isLoading = isActionLoading)
            }

            // Dispatch: available when fulfillment is processing
            if (order.fulfillmentStatus == FulfillmentStatus.PROCESSING) {
                ActionButton(label = "Dispatch Order", icon = Icons.Outlined.LocalShipping, onClick = onDispatch, isLoading = isActionLoading)
            }

            // Deliver: available when fulfillment is out for delivery
            if (order.fulfillmentStatus == FulfillmentStatus.OUT_FOR_DELIVERY) {
                ActionButton(label = "Mark as Delivered", icon = Icons.Outlined.CheckCircle, onClick = onDeliver, isLoading = isActionLoading)
            }

            // No actions available
            if (order.status == OrderStatus.COMPLETED || order.status == OrderStatus.CANCELLED || order.status == OrderStatus.REJECTED) {
                Text("This order is ${order.status.displayLabel.lowercase()}. No actions available.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
            }
        }
    }
}

/**
 * Action button with icon and loading state.
 */
@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean,
    isDestructive: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) RoseError else Navy,
            disabledContainerColor = if (isDestructive) RoseError.copy(alpha = 0.5f) else Navy.copy(alpha = 0.5f)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Rejection dialog with mandatory reason input.
 */
@Composable
private fun RejectDialog(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject Order") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Provide a reason for rejecting this order.", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Rejection reason (required)") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = reason.trim().isNotEmpty() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = RoseError)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Reject", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryLight)
            }
        }
    )
}

/**
 * Simple info row with label and value.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Navy), maxLines = 1)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OrderDetailsScreenPreview() {
    NearAiAdminTheme {
        OrderDetailsScreen(onNavigateBack = { })
    }
}
