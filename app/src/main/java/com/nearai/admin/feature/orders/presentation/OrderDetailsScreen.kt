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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.components.NearAiAlertDialog
import com.nearai.admin.core.designsystem.components.NearAiAlertType
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

    // Load payment info when order loads
    LaunchedEffect(uiState.order) {
        if (uiState.order != null) {
            viewModel.loadPaymentInfo()
        }
    }

    // Rejection dialog — must stay in route composable as it involves ViewModel state
    if (uiState.showRejectDialog) {
        RejectDialog(
            reason = uiState.rejectReason,
            onReasonChanged = { viewModel.onRejectReasonChanged(it) },
            onDismiss = { viewModel.dismissRejectDialog() },
            onConfirm = { viewModel.rejectOrder() },
            isLoading = uiState.isActionLoading
        )
    }

    // Reject approval dialog — for orders awaiting approval review
    if (uiState.showRejectApprovalDialog) {
        RejectApprovalDialog(
            reason = uiState.rejectApprovalReason,
            onReasonChanged = { viewModel.onRejectApprovalReasonChanged(it) },
            onDismiss = { viewModel.dismissRejectApprovalDialog() },
            onConfirm = { viewModel.rejectForApproval() },
            isLoading = uiState.isActionLoading
        )
    }

    // Collect cash confirmation dialog
    NearAiAlertDialog(
        isVisible = uiState.showCollectCashDialog,
        title = stringResource(R.string.order_collect_cash_title),
        message = buildString {
            append(stringResource(R.string.order_collect_cash_description))
            uiState.paymentInfo?.let { payment ->
                append("\n")
                append(stringResource(R.string.order_currency_format, payment.currency, payment.amount))
            }
        },
        confirmText = stringResource(R.string.order_collect_cash_confirm),
        dismissText = stringResource(R.string.order_details_cancel),
        type = NearAiAlertType.SuccessType,
        onConfirm = {
            viewModel.dismissCollectCashDialog()
            viewModel.collectCash()
        },
        onDismiss = { viewModel.dismissCollectCashDialog() },
        isLoading = uiState.isCollectingCash,
    )

    // Cancel order confirmation dialog
    NearAiAlertDialog(
        isVisible = uiState.showCancelDialog,
        title = stringResource(R.string.order_cancel_title),
        message = stringResource(R.string.order_cancel_description),
        confirmText = stringResource(R.string.order_cancel_confirm),
        dismissText = stringResource(R.string.order_details_cancel),
        type = NearAiAlertType.WarningType,
        onConfirm = {
            viewModel.dismissCancelDialog()
            viewModel.cancelOrder()
        },
        onDismiss = { viewModel.dismissCancelDialog() },
        isLoading = uiState.isActionLoading,
    )

    // Confirmation dialog — must stay in route composable as it involves ViewModel state
    uiState.showConfirmDialog?.let { actionLabel ->
        NearAiAlertDialog(
            isVisible = true,
            title = stringResource(R.string.order_details_confirm_title, actionLabel),
            message = stringResource(R.string.order_details_confirm_message, actionLabel.lowercase()),
            confirmText = stringResource(R.string.order_details_confirm_yes, actionLabel),
            dismissText = stringResource(R.string.order_details_cancel),
            type = NearAiAlertType.NoneType,
            onConfirm = {
                viewModel.dismissConfirmDialog()
                viewModel.executeConfirmedAction()
            },
            onDismiss = { viewModel.dismissConfirmDialog() },
            isLoading = uiState.isActionLoading,
        )
    }

    // Resolve action labels for ViewModel callbacks
    val confirmLabel = stringResource(R.string.order_action_confirm)
    val processLabel = stringResource(R.string.order_action_process)
    val dispatchLabel = stringResource(R.string.order_action_dispatch)
    val deliverLabel = stringResource(R.string.order_action_deliver)

    OrderDetailsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.onRetry() },
        onApproveForReview = { viewModel.approveForReview() },
        onRejectApproval = { viewModel.showRejectApprovalDialog() },
        onConfirm = { viewModel.showConfirmDialog(confirmLabel) },
        onReject = { viewModel.showRejectDialog() },
        onProcess = { viewModel.showConfirmDialog(processLabel) },
        onDispatch = { viewModel.showConfirmDialog(dispatchLabel) },
        onDeliver = { viewModel.showConfirmDialog(deliverLabel) },
        onCollectCash = { viewModel.showCollectCashDialog() },
        onCancel = { viewModel.showCancelDialog() },
        onClearActionError = { viewModel.clearActionError() },
        onClearCollectionError = { viewModel.clearCollectionError() }
    )
}

// =============================================================================
// Stateless Content Composable — renders order details from UiState
// =============================================================================

/**
 * Stateless order details content that renders the full screen from [OrderDetailsUiState].
 *
 * This composable can be used independently in tests and screenshot previews
 * without requiring a ViewModel or Hilt dependency injection.
 *
 * Note: Dialogs (reject/confirm) are not included here as they require ViewModel
 * interaction. They are rendered by the route composable [OrderDetailsScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreenContent(
    uiState: OrderDetailsUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onApproveForReview: () -> Unit = {},
    onRejectApproval: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onReject: () -> Unit = {},
    onProcess: () -> Unit = {},
    onDispatch: () -> Unit = {},
    onDeliver: () -> Unit = {},
    onCollectCash: () -> Unit = {},
    onCancel: () -> Unit = {},
    onClearActionError: () -> Unit = {},
    onClearCollectionError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dynamic title from order ID
    val title = uiState.order?.id?.let { "#${it.take(8)}" } ?: stringResource(R.string.order_details_title)

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
                        .padding(padding)
                        .testTag("orderDetailsLoading"),
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
                        .padding(32.dp)
                        .testTag("orderDetailsError"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.order_details_error), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.error.orEmpty(), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Navy), modifier = Modifier.testTag("orderDetailsRetryButton")) {
                        Text(stringResource(R.string.order_details_retry), color = Color.White)
                    }
                }
            }
            else -> {
                val order = uiState.order ?: return@Scaffold
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).testTag("orderDetailsContent"),
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

                    // Approval info
                    item {
                        ApprovalInfoCard(
                            approvalStatus = uiState.approvalStatus,
                            approvalDecision = uiState.approvalDecision,
                            currency = order.currency
                        )
                    }

                    // Payment status & COD collection
                    item {
                        PaymentStatusCard(
                            paymentInfo = uiState.paymentInfo,
                            isCollectingCash = uiState.isCollectingCash,
                            collectionError = uiState.collectionError,
                            onCollectCash = onCollectCash,
                            onClearCollectionError = onClearCollectionError
                        )
                    }

                    // Status history
                    if (uiState.statusHistory.isNotEmpty()) {
                        item {
                            StatusHistoryCard(uiState.statusHistory)
                        }
                    }

                    // Contextual action
                    item {
                        ContextualActionCard(
                            order = order,
                            approvalStatus = uiState.approvalStatus,
                            paymentInfo = uiState.paymentInfo,
                            isActionLoading = uiState.isActionLoading,
                            isCollectingCash = uiState.isCollectingCash,
                            onApproveForReview = onApproveForReview,
                            onRejectApproval = onRejectApproval,
                            onConfirm = onConfirm,
                            onProcess = onProcess,
                            onDispatch = onDispatch,
                            onDeliver = onDeliver,
                            onCollectCash = onCollectCash,
                            onCancel = onCancel
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
                                    TextButton(onClick = onClearActionError) {
                                        Text(stringResource(R.string.order_details_dismiss), color = RoseDark, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Collection error
                    if (uiState.collectionError != null) {
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
                                    Text(uiState.collectionError.orEmpty(), style = MaterialTheme.typography.bodySmall.copy(color = RoseDark), modifier = Modifier.weight(1f))
                                    TextButton(onClick = onClearCollectionError) {
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
 * Payment summary card with delivery fee and discount.
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
            order.deliveryFeeAmount?.let { fee ->
                if (fee > 0) {
                    InfoRow(label = stringResource(R.string.order_label_delivery_fee), value = stringResource(R.string.order_currency_format, order.currency, fee))
                }
            }
            order.discountAmount?.let { discount ->
                if (discount > 0) {
                    InfoRow(label = stringResource(R.string.order_label_discount), value = "-${stringResource(R.string.order_currency_format, order.currency, discount)}")
                }
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
 * Approval information card showing the automation decision details.
 */
@Composable
private fun ApprovalInfoCard(
    approvalStatus: ApprovalStatus?,
    approvalDecision: ApprovalDecisionSnapshot?,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.approval_info_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (approvalStatus != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.approval_label),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight, fontWeight = FontWeight.SemiBold)
                    )
                    StatusBadge(
                        label = approvalStatus.displayLabel,
                        tint = approvalStatus.badgeTint
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (approvalDecision != null) {
                InfoRow(
                    label = stringResource(R.string.approval_info_evaluated_amount),
                    value = stringResource(R.string.order_currency_format, approvalDecision.currency, approvalDecision.evaluatedAmount)
                )
                if (!approvalDecision.decisionReason.isNullOrBlank()) {
                    InfoRow(
                        label = stringResource(R.string.approval_info_reason),
                        value = approvalDecision.decisionReason
                    )
                }
                approvalDecision.policyVersion?.let { version ->
                    InfoRow(
                        label = stringResource(R.string.approval_info_policy_version),
                        value = version.toString()
                    )
                }
                InfoRow(
                    label = stringResource(R.string.approval_info_evaluated_at),
                    value = approvalDecision.evaluatedAt.take(16)
                )
                if (!approvalDecision.approvedBy.isNullOrBlank()) {
                    InfoRow(
                        label = stringResource(R.string.approval_info_approved_by),
                        value = approvalDecision.approvedBy
                    )
                }
                if (!approvalDecision.approvedAt.isNullOrBlank()) {
                    InfoRow(
                        label = stringResource(R.string.approval_info_approved_at),
                        value = approvalDecision.approvedAt.take(16)
                    )
                }
            } else if (approvalStatus == null) {
                Text(
                    stringResource(R.string.approval_info_no_decision),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                )
            }
        }
    }
}

/**
 * Contextual action card that shows one primary action based on approval, order,
 * fulfillment, and payment state. Replaces the old four-button layout.
 */
@Composable
private fun ContextualActionCard(
    order: OrderDetails,
    approvalStatus: ApprovalStatus?,
    paymentInfo: com.nearai.admin.feature.orders.domain.PaymentInfo?,
    isActionLoading: Boolean,
    isCollectingCash: Boolean,
    onApproveForReview: () -> Unit,
    onRejectApproval: () -> Unit,
    onConfirm: () -> Unit,
    onProcess: () -> Unit,
    onDispatch: () -> Unit,
    onDeliver: () -> Unit,
    onCollectCash: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.order_details_actions),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy)
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                // 1. Order is cancelled, rejected, or expired — no actions
                order.status == OrderStatus.CANCELLED || order.status == OrderStatus.REJECTED -> {
                    Text(
                        stringResource(R.string.order_details_no_actions, order.status.displayLabel.lowercase()),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                    )
                }

                // 2. Approval rejected — no processing actions
                approvalStatus == ApprovalStatus.REJECTED -> {
                    Text(
                        stringResource(R.string.order_details_no_actions, "rejected"),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                    )
                }

                // 3. Awaiting standard or mandatory approval review
                approvalStatus == ApprovalStatus.AWAITING_STANDARD_REVIEW ||
                        approvalStatus == ApprovalStatus.AWAITING_MANDATORY_APPROVAL -> {
                    // Primary: Review and Approve
                    ActionButton(
                        label = stringResource(R.string.order_action_review_and_approve),
                        icon = Icons.Outlined.CheckCircle,
                        onClick = onApproveForReview,
                        isLoading = isActionLoading,
                        isDestructive = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Secondary: Reject
                    ActionButton(
                        label = stringResource(R.string.order_action_reject_approval),
                        icon = Icons.Outlined.Block,
                        onClick = onRejectApproval,
                        isLoading = isActionLoading,
                        isDestructive = true
                    )
                }

                // 4. Auto-approved or manually approved order — skip confirmation, go straight to preparing
                //    The backend already transitions fulfillment to CONFIRMED for AUTO_APPROVED orders.
                //    For MANUALLY_APPROVED orders, the backend also confirms fulfillment.
                //    Show "Start Preparing" regardless of whether fulfillment is CONFIRMED or still
                //    AWAITING_CONFIRMATION (in case of timing lag).
                (approvalStatus == ApprovalStatus.AUTO_APPROVED ||
                 approvalStatus == ApprovalStatus.MANUALLY_APPROVED) &&
                order.status == OrderStatus.PENDING -> {
                    ActionButton(
                        label = stringResource(R.string.order_action_start_preparing),
                        icon = Icons.Outlined.Build,
                        onClick = onProcess,
                        isLoading = isActionLoading
                    )
                }

                // 5. Order awaiting confirmation (not yet approved) — show Confirm Order
                //    This branch only reaches orders that are NOT auto/manual approved, so they
                //    genuinely need manual confirmation.
                order.status == OrderStatus.PENDING &&
                        order.fulfillmentStatus == FulfillmentStatus.AWAITING_CONFIRMATION &&
                        approvalStatus != ApprovalStatus.AUTO_APPROVED &&
                        approvalStatus != ApprovalStatus.MANUALLY_APPROVED -> {
                    ActionButton(
                        label = stringResource(R.string.order_action_confirm_order),
                        icon = Icons.Outlined.CheckCircle,
                        onClick = onConfirm,
                        isLoading = isActionLoading
                    )
                }

                // 6. Order confirmed, ready to process (start preparing)
                //    Catches pre-automation orders with CONFIRMED fulfillment and no approval data.
                order.status == OrderStatus.PENDING &&
                        order.fulfillmentStatus == FulfillmentStatus.CONFIRMED -> {
                    ActionButton(
                        label = stringResource(R.string.order_action_start_preparing),
                        icon = Icons.Outlined.Build,
                        onClick = onProcess,
                        isLoading = isActionLoading
                    )
                }

                // 7. Order is being processed — mark ready for delivery
                order.fulfillmentStatus == FulfillmentStatus.PROCESSING -> {
                    ActionButton(
                        label = stringResource(R.string.order_action_mark_ready),
                        icon = Icons.Outlined.LocalShipping,
                        onClick = onDispatch,
                        isLoading = isActionLoading
                    )
                }

                // 8. Out for delivery — confirm delivery
                order.fulfillmentStatus == FulfillmentStatus.OUT_FOR_DELIVERY -> {
                    ActionButton(
                        label = stringResource(R.string.order_action_confirm_delivery),
                        icon = Icons.Outlined.CheckCircle,
                        onClick = onDeliver,
                        isLoading = isActionLoading
                    )
                }

                // 9. Delivered with COD payment pending — collect cash
                order.fulfillmentStatus == FulfillmentStatus.DELIVERED &&
                        paymentInfo?.status == "pending" &&
                        paymentInfo.paymentMethod == "cash_on_delivery" -> {
                    ActionButton(
                        label = stringResource(R.string.order_action_record_cash_collection),
                        icon = Icons.Outlined.MonetizationOn,
                        onClick = onCollectCash,
                        isLoading = isCollectingCash,
                        isDestructive = false
                    )
                }

                // 10. Payment already collected — show confirmation info
                paymentInfo?.status == "collected" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val collectedAt = paymentInfo.collectedAt?.take(16) ?: stringResource(R.string.order_payment_collected)
                        Text(
                            stringResource(R.string.order_details_payment_collected_info, collectedAt),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // 10. Default: no specific action available
                else -> {
                    Text(
                        stringResource(R.string.order_details_no_actions, order.status.displayLabel.lowercase()),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                    )
                }
            }

            // Secondary actions section (Cancel where permitted)
            if (order.status == OrderStatus.PENDING && approvalStatus != ApprovalStatus.REJECTED) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onCancel,
                    enabled = !isActionLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = RoseError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.order_action_cancel_order),
                            color = RoseError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
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
 * Rejection dialog with mandatory reason input (for order rejection).
 * Uses NearAiAlertDialog styling with a text input field.
 */
@Composable
private fun RejectDialog(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                )
                .background(SurfaceLight, RoundedCornerShape(24.dp)),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                AlertIconInline(NearAiAlertType.WarningType)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.order_reject_title),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 26.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.order_reject_description),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.order_reject_reason_hint)) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondaryLight,
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = androidx.compose.ui.unit.Dp.Hairline,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.order_details_cancel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = reason.trim().isNotEmpty() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoseError,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                            focusedElevation = 4.dp,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.order_reject_button),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reject approval dialog with mandatory reason input (for approval review rejection).
 * Uses NearAiAlertDialog styling with a text input field.
 */
@Composable
private fun RejectApprovalDialog(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                )
                .background(SurfaceLight, RoundedCornerShape(24.dp)),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                AlertIconInline(NearAiAlertType.WarningType)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.review_queue_reject_title),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 26.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.review_queue_reject_description),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.review_queue_reject_reason_hint)) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondaryLight,
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = androidx.compose.ui.unit.Dp.Hairline,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.order_details_cancel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = reason.trim().isNotEmpty() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoseError,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                            focusedElevation = 4.dp,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.review_queue_reject_confirm),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Inline alert icon for use inside dialogs with custom content (e.g., text input).
 * Mirrors the [AlertIcon] from Dialogs.kt but is accessible here.
 */
@Composable
private fun AlertIconInline(type: NearAiAlertType) {
    val accent = when (type) {
        NearAiAlertType.ErrorType -> RoseError
        NearAiAlertType.WarningType -> AmberWarning
        NearAiAlertType.InfoType -> Info
        NearAiAlertType.SuccessType -> EmeraldSuccess
        NearAiAlertType.NoneType -> Navy
    }
    val bg = when (type) {
        NearAiAlertType.ErrorType -> RoseBg
        NearAiAlertType.WarningType -> AmberBg
        NearAiAlertType.InfoType -> PurpleBg
        NearAiAlertType.SuccessType -> EmeraldBg
        NearAiAlertType.NoneType -> PrimaryContainerLight
    }

    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(56.dp),
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val iconSize = diameter * 0.32f
        val strokeWidth = 3.5f

        drawCircle(
            color = bg,
            radius = radius,
            center = Offset(radius, radius)
        )

        when (type) {
            NearAiAlertType.WarningType -> {
                val lineTop = radius - iconSize * 0.5f
                val lineBottom = radius + iconSize * 0.1f
                val dotCenterY = radius + iconSize * 0.4f
                val dotRadius = iconSize * 0.18f

                drawLine(
                    color = accent,
                    start = Offset(radius, lineTop),
                    end = Offset(radius, lineBottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = accent,
                    radius = dotRadius,
                    center = Offset(radius, dotCenterY)
                )
            }
            NearAiAlertType.ErrorType -> {
                val half = iconSize * 0.7f
                drawLine(
                    color = accent,
                    start = Offset(radius - half, radius - half),
                    end = Offset(radius + half, radius + half),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = accent,
                    start = Offset(radius + half, radius - half),
                    end = Offset(radius - half, radius + half),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            else -> {
                val lineTop = radius - iconSize * 0.5f
                val lineBottom = radius + iconSize * 0.1f
                val dotCenterY = radius + iconSize * 0.4f
                val dotRadius = iconSize * 0.18f
                drawLine(
                    color = accent,
                    start = Offset(radius, lineTop),
                    end = Offset(radius, lineBottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = accent,
                    radius = dotRadius,
                    center = Offset(radius, dotCenterY)
                )
            }
        }
    }
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

/**
 * Payment status card showing method, status, and collection info.
 */
@Composable
private fun PaymentStatusCard(
    paymentInfo: com.nearai.admin.feature.orders.domain.PaymentInfo?,
    isCollectingCash: Boolean,
    collectionError: String?,
    onCollectCash: () -> Unit,
    onClearCollectionError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.order_details_payment_status),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Navy)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (paymentInfo == null) {
                Text(
                    stringResource(R.string.order_payment_not_available),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                )
            } else {
                InfoRow(
                    label = stringResource(R.string.order_label_payment_method),
                    value = if (paymentInfo.paymentMethod == "cash_on_delivery") stringResource(R.string.order_payment_cod) else paymentInfo.paymentMethod
                )
                val statusColor = when (paymentInfo.status) {
                    "collected" -> EmeraldSuccess
                    "pending" -> AmberWarning
                    "cancelled" -> TextSecondaryLight
                    else -> Indigo
                }
                val statusLabel = when (paymentInfo.status) {
                    "collected" -> stringResource(R.string.order_payment_collected)
                    "pending" -> stringResource(R.string.order_payment_pending)
                    "cancelled" -> stringResource(R.string.order_payment_cancelled)
                    else -> paymentInfo.status
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.order_label_payment_status),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight, fontWeight = FontWeight.SemiBold)
                    )
                    StatusBadge(label = statusLabel, tint = statusColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow(
                    label = stringResource(R.string.order_label_payment_amount),
                    value = stringResource(R.string.order_currency_format, paymentInfo.currency, paymentInfo.amount)
                )
                if (!paymentInfo.collectedAt.isNullOrBlank()) {
                    InfoRow(
                        label = stringResource(R.string.order_label_collected_at),
                        value = paymentInfo.collectedAt.take(16)
                    )
                }
            }
        }
    }
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
                    unitPriceCurrency = "JOD",
                    subtotalAmount = 29.99
                )
            ),
            subtotalAmount = 29.99,
            deliveryFeeAmount = 5.50,
            discountAmount = 0.0,
            totalAmount = 35.49,
            currency = "JOD",
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
                    ApprovalInfoCard(
                        approvalStatus = com.nearai.admin.feature.orders.domain.ApprovalStatus.AWAITING_STANDARD_REVIEW,
                        approvalDecision = null,
                        currency = "JOD"
                    )
                }
                item {
                    ContextualActionCard(
                        order = sampleOrder,
                        approvalStatus = com.nearai.admin.feature.orders.domain.ApprovalStatus.AWAITING_STANDARD_REVIEW,
                        paymentInfo = null,
                        isActionLoading = false,
                        isCollectingCash = false,
                        onApproveForReview = {},
                        onRejectApproval = {},
                        onConfirm = {},
                        onProcess = {},
                        onDispatch = {},
                        onDeliver = {},
                        onCollectCash = {},
                        onCancel = {}
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
