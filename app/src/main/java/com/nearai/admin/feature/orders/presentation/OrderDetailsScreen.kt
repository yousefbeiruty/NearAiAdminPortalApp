package com.nearai.admin.feature.orders.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.R
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
            title = { Text(stringResource(R.string.order_details_confirm_title, actionLabel)) },
            text = { Text(stringResource(R.string.order_details_confirm_message, actionLabel.lowercase())) },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeConfirmedAction() },
                    enabled = !uiState.isActionLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    if (uiState.isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.order_details_confirm_yes, actionLabel), color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text(stringResource(R.string.order_details_cancel), color = TextSecondaryLight)
                }
            }
        )
    }

    // Dynamic title from order ID
    val title = uiState.order?.id?.let { "#${it.take(8)}" } ?: stringResource(R.string.order_details_title)

    // Resolve action labels for ViewModel callbacks
    val confirmLabel = stringResource(R.string.order_action_confirm)
    val processLabel = stringResource(R.string.order_action_process)
    val dispatchLabel = stringResource(R.string.order_action_dispatch)
    val deliverLabel = stringResource(R.string.order_action_deliver)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        color = Navy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.order_details_back), tint = Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Indigo)
                }
            }
            uiState.error != null && uiState.order == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.order_details_error), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.error.orEmpty(), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onRetry() }, colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                        Text(stringResource(R.string.order_details_retry), color = Color.White)
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
                            onConfirm = { viewModel.showConfirmDialog(confirmLabel) },
                            onReject = { viewModel.showRejectDialog() },
                            onProcess = { viewModel.showConfirmDialog(processLabel) },
                            onDispatch = { viewModel.showConfirmDialog(dispatchLabel) },
                            onDeliver = { viewModel.showConfirmDialog(deliverLabel) }
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
                                        Text(stringResource(R.string.order_details_dismiss), color = RoseDark, fontSize = 12.sp)
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
            Text(stringResource(R.string.order_details_order_info), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            // Order ID — always on its own row to avoid overlap with Created date
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.order_details_order_id), style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                Text("#${order.id}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = Navy), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Created date on its own row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.order_details_created), style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                Text(order.createdAt.take(16), style = MaterialTheme.typography.bodyMedium.copy(color = Navy))
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Status badges — each on its own row to avoid overlap
            Column {
                StatusBadge(label = stringResource(R.string.order_label_order_status, order.status.displayLabel), tint = order.status.badgeTint)
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(label = stringResource(R.string.order_label_fulfillment_status, order.fulfillmentStatus.displayLabel), tint = order.fulfillmentStatus.badgeTint)
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
            Text(stringResource(R.string.order_details_customer_delivery), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = stringResource(R.string.order_label_recipient), value = order.deliveryRecipientName ?: "—")
            InfoRow(label = stringResource(R.string.order_label_phone), value = order.deliveryPhone ?: "—")
            if (!order.deliveryCity.isNullOrBlank()) {
                InfoRow(label = stringResource(R.string.order_label_city), value = order.deliveryCity)
            }
            if (!order.deliveryArea.isNullOrBlank()) {
                InfoRow(label = stringResource(R.string.order_label_area), value = order.deliveryArea)
            }
            if (!order.deliveryStreet.isNullOrBlank()) {
                InfoRow(label = stringResource(R.string.order_label_street), value = order.deliveryStreet)
            }
            if (!order.deliveryBuildingNumber.isNullOrBlank() || !order.deliveryApartment.isNullOrBlank()) {
                val building = listOf(order.deliveryBuildingNumber, order.deliveryApartment).filterNotNull().joinToString(", ")
                InfoRow(label = stringResource(R.string.order_label_building), value = building)
            }
            if (!order.deliveryInstructions.isNullOrBlank()) {
                InfoRow(label = stringResource(R.string.order_label_instructions), value = order.deliveryInstructions)
            }
            Spacer(modifier = Modifier.height(4.dp))
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
            Text(stringResource(R.string.order_details_items_count, order.items.size), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            order.items.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(item.productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.order_details_quantity, item.quantity, order.currency, item.unitPriceAmount), style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
                    }
                    Text(stringResource(R.string.order_currency_format, order.currency, item.subtotalAmount ?: (item.unitPriceAmount * item.quantity)), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Navy), modifier = Modifier.align(Alignment.Top))
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
            Text(stringResource(R.string.order_details_payment_summary), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))
            order.subtotalAmount?.let {
                InfoRow(label = stringResource(R.string.order_label_subtotal), value = stringResource(R.string.order_currency_format, order.currency, it))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.order_label_total), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
                Text(stringResource(R.string.order_currency_format, order.currency, order.totalAmount), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Navy))
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
            Text(stringResource(R.string.order_details_status_history), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
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
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(if (entry.statusType == "order") Indigo else EmeraldSuccess)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(entry.newStatus, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Navy), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.width(8.dp))
                Text(entry.timestamp.take(16), style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight, fontFamily = FontFamily.Monospace), modifier = Modifier.align(Alignment.CenterVertically))
            }
            if (!entry.previousStatus.isNullOrBlank()) {
                Text(stringResource(R.string.order_details_from, entry.previousStatus), style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
            }
            Text(entry.statusType.uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = if (entry.statusType == "order") Indigo else EmeraldSuccess, fontWeight = FontWeight.Bold))
            if (!entry.actorRole.isNullOrBlank()) {
                Text(stringResource(R.string.order_details_by, entry.actorRole), style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight))
            }
            if (!entry.reason.isNullOrBlank()) {
                Text(stringResource(R.string.order_details_reason, entry.reason), style = MaterialTheme.typography.bodySmall.copy(color = RoseDark), maxLines = 2, overflow = TextOverflow.Ellipsis)
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
            Text(stringResource(R.string.order_details_actions), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
            Spacer(modifier = Modifier.height(12.dp))

            // Confirm: available when order is pending and fulfillment is awaiting confirmation
            if (order.status == OrderStatus.PENDING && order.fulfillmentStatus == FulfillmentStatus.AWAITING_CONFIRMATION) {
                ActionButton(label = stringResource(R.string.order_action_confirm_order), icon = Icons.Outlined.CheckCircle, onClick = onConfirm, isLoading = isActionLoading)
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(label = stringResource(R.string.order_action_reject_order), icon = Icons.Outlined.Block, onClick = onReject, isLoading = isActionLoading, isDestructive = true)
            }

            // Process: available when order is pending and fulfillment is confirmed
            if (order.status == OrderStatus.PENDING && order.fulfillmentStatus == FulfillmentStatus.CONFIRMED) {
                ActionButton(label = stringResource(R.string.order_action_process_order), icon = Icons.Outlined.Build, onClick = onProcess, isLoading = isActionLoading)
            }

            // Dispatch: available when fulfillment is processing
            if (order.fulfillmentStatus == FulfillmentStatus.PROCESSING) {
                ActionButton(label = stringResource(R.string.order_action_dispatch_order), icon = Icons.Outlined.LocalShipping, onClick = onDispatch, isLoading = isActionLoading)
            }

            // Deliver: available when fulfillment is out for delivery
            if (order.fulfillmentStatus == FulfillmentStatus.OUT_FOR_DELIVERY) {
                ActionButton(label = stringResource(R.string.order_action_mark_delivered), icon = Icons.Outlined.CheckCircle, onClick = onDeliver, isLoading = isActionLoading)
            }

            // No actions available
            if (order.status == OrderStatus.COMPLETED || order.status == OrderStatus.CANCELLED || order.status == OrderStatus.REJECTED) {
                Text(stringResource(R.string.order_details_no_actions, order.status.displayLabel.lowercase()), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
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
        title = { Text(stringResource(R.string.order_reject_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.order_reject_description), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.order_reject_reason_hint)) },
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
                    Text(stringResource(R.string.order_reject_button), color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.order_details_cancel), color = TextSecondaryLight)
            }
        }
    )
}

/**
 * Simple info row with label and value.
 * Label appears above the value so long text (addresses, instructions) wraps naturally.
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight, fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Navy))
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OrderDetailsScreenPreview() {
    NearAiAdminTheme {
        val sampleOrder = com.nearai.admin.feature.orders.domain.OrderDetails(
            id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            createdAt = "2024-06-15T14:30:00Z",
            status = com.nearai.admin.feature.orders.domain.OrderStatus.PENDING,
            fulfillmentStatus = com.nearai.admin.feature.orders.domain.FulfillmentStatus.AWAITING_CONFIRMATION,
            items = listOf(
                com.nearai.admin.feature.orders.domain.OrderItem(
                    variantId = "v1",
                    productName = "NearAI Premium Subscription — Annual Plan with Extended Features and All Benefits",
                    sku = "SKU-001",
                    quantity = 1,
                    unitPriceAmount = 29.99,
                    unitPriceCurrency = "USD",
                    subtotalAmount = 29.99
                ),
                com.nearai.admin.feature.orders.domain.OrderItem(
                    variantId = "v2",
                    productName = "Delivery Fee",
                    sku = null,
                    quantity = 1,
                    unitPriceAmount = 5.50,
                    unitPriceCurrency = "USD",
                    subtotalAmount = 5.50
                )
            ),
            subtotalAmount = 29.99,
            totalAmount = 35.49,
            currency = "USD",
            deliveryRecipientName = "John Alexander Doe",
            deliveryPhone = "+1 (555) 123-4567",
            deliveryCity = "New York",
            deliveryArea = "Manhattan",
            deliveryStreet = "Broadway",
            deliveryBuildingNumber = "123",
            deliveryApartment = "Apt 4B",
            deliveryInstructions = "Ring doorbell twice, leave at front door if no answer. Building has a security code."
        )
        val sampleHistory = listOf(
            com.nearai.admin.feature.orders.domain.StatusHistoryEntry(
                previousStatus = null,
                newStatus = "Pending",
                statusType = "order",
                actorRole = "customer",
                reason = null,
                timestamp = "2024-06-15T14:30:00Z"
            ),
            com.nearai.admin.feature.orders.domain.StatusHistoryEntry(
                previousStatus = "Pending",
                newStatus = "Awaiting Confirmation",
                statusType = "fulfillment",
                actorRole = "system",
                reason = null,
                timestamp = "2024-06-15T14:30:05Z"
            )
        )
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("#a1b2c3d4", fontWeight = FontWeight.Bold, color = Navy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Navy)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { OrderInfoCard(sampleOrder) }
                item { CustomerDeliveryCard(sampleOrder) }
                item { OrderItemsCard(sampleOrder) }
                item { PaymentSummaryCard(sampleOrder) }
                item { StatusHistoryCard(sampleHistory) }
                item {
                    OrderActionButtons(
                        order = sampleOrder,
                        isActionLoading = false,
                        onConfirm = {},
                        onReject = {},
                        onProcess = {},
                        onDispatch = {},
                        onDeliver = {}
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
