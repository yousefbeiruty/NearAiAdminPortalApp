package com.nearai.admin.feature.orders.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.orders.domain.ApprovalStatus
import com.nearai.admin.feature.orders.domain.ReviewQueueOrder

/**
 * Review queue screen showing orders awaiting manual approval.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewQueueScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    viewModel: ReviewQueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Approve confirmation dialog
    if (uiState.showApproveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissApproveDialog() },
            title = { Text(stringResource(R.string.review_queue_approve_title)) },
            text = { Text(stringResource(R.string.review_queue_approve_description)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.approveOrder() },
                    enabled = !uiState.isActionLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    if (uiState.isActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.review_queue_approve_confirm), color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissApproveDialog() }) {
                    Text(stringResource(R.string.review_queue_cancel), color = TextSecondaryLight)
                }
            }
        )
    }

    // Reject dialog
    if (uiState.showRejectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRejectDialog() },
            title = { Text(stringResource(R.string.review_queue_reject_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.review_queue_reject_description), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight))
                    OutlinedTextField(
                        value = uiState.rejectReason,
                        onValueChange = { viewModel.onRejectReasonChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.review_queue_reject_reason_hint)) },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.rejectOrder() },
                    enabled = uiState.rejectReason.trim().isNotEmpty() && !uiState.isActionLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    if (uiState.isActionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.review_queue_reject_confirm), color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRejectDialog() }) {
                    Text(stringResource(R.string.review_queue_cancel), color = TextSecondaryLight)
                }
            }
        )
    }

    ReviewQueueScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToOrder = onNavigateToOrder,
        onFilterChanged = { viewModel.onFilterChanged(it) },
        onApprove = { viewModel.showApproveDialog(it) },
        onReject = { viewModel.showRejectDialog(it) },
        onRefresh = { viewModel.onRefresh() },
        onRetry = { viewModel.onRetry() },
        onClearActionError = { viewModel.clearActionError() }
    )
}

// =============================================================================
// Stateless Content Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewQueueScreenContent(
    uiState: ReviewQueueUiState,
    onNavigateBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit,
    onFilterChanged: (ReviewQueueFilter) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onClearActionError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.review_queue_title),
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.order_details_back),
                            tint = Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().testTag("reviewQueueLoading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Indigo)
                    }
                }
                uiState.error != null && uiState.orders.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp).testTag("reviewQueueError"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.review_queue_loading_error),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            uiState.error.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = Navy),
                            modifier = Modifier.testTag("reviewQueueRetryButton")
                        ) {
                            Text(stringResource(R.string.review_queue_retry), color = Color.White)
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filter chips
                        FilterChipRow(
                            filter = uiState.filter,
                            onFilterChanged = onFilterChanged,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Order list
                        if (uiState.orders.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().testTag("reviewQueueEmpty"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        stringResource(R.string.review_queue_empty),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        stringResource(R.string.review_queue_empty_message),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag("reviewQueueList"),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.orders, key = { it.id }) { order ->
                                    ReviewQueueOrderCard(
                                        order = order,
                                        onClick = { onNavigateToOrder(order.id) },
                                        onApprove = { onApprove(order.id) },
                                        onReject = { onReject(order.id) }
                                    )
                                }
                            }
                        }
                    }

                    // Action error banner (overlay at bottom)
                    if (uiState.actionError != null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RoseBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Block,
                                    contentDescription = null,
                                    tint = RoseError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    uiState.actionError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall.copy(color = RoseDark),
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onClearActionError) {
                                    Text(
                                        stringResource(R.string.review_queue_dismiss),
                                        color = RoseDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Filter Chips
// =============================================================================

@Composable
private fun FilterChipRow(
    filter: ReviewQueueFilter,
    onFilterChanged: (ReviewQueueFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filter == ReviewQueueFilter.ALL,
            onClick = { onFilterChanged(ReviewQueueFilter.ALL) },
            label = { Text(stringResource(R.string.review_queue_filter_all)) }
        )
        FilterChip(
            selected = filter == ReviewQueueFilter.STANDARD,
            onClick = { onFilterChanged(ReviewQueueFilter.STANDARD) },
            label = { Text(stringResource(R.string.review_queue_filter_standard)) }
        )
        FilterChip(
            selected = filter == ReviewQueueFilter.MANDATORY,
            onClick = { onFilterChanged(ReviewQueueFilter.MANDATORY) },
            label = { Text(stringResource(R.string.review_queue_filter_mandatory)) },
            border = if (filter == ReviewQueueFilter.MANDATORY) null else androidx.compose.foundation.BorderStroke(
                1.dp,
                RoseError.copy(alpha = 0.5f)
            ),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoseError.copy(alpha = 0.1f),
                selectedLabelColor = RoseError,
                labelColor = RoseError.copy(alpha = 0.7f)
            )
        )
    }
}

// =============================================================================
// Order Card
// =============================================================================

@Composable
private fun ReviewQueueOrderCard(
    order: ReviewQueueOrder,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (order.isMandatory) {
            androidx.compose.foundation.BorderStroke(2.dp, RoseError)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: order number, customer, and badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "#${order.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    order.deliveryRecipientName?.let { name ->
                        Text(
                            stringResource(R.string.review_queue_customer, name),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Mandatory/Standard badge
                val badgeColor = if (order.isMandatory) RoseError else AmberWarning
                val badgeBg = if (order.isMandatory) RoseBg else Color(0xFFFFF8E1)
                val badgeText = if (order.isMandatory) {
                    stringResource(R.string.review_queue_mandatory_badge)
                } else {
                    stringResource(R.string.review_queue_standard_badge)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Amount and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.order_currency_format, order.currency, order.totalAmount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                )
                Text(
                    order.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight)
                )
            }

            // Approval reason
            order.approvalDecision?.decisionReason?.let { reason ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.review_queue_reason, reason),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Approve button
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.review_queue_approve),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                // Reject button
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseError)
                ) {
                    Icon(
                        Icons.Outlined.Block,
                        contentDescription = null,
                        tint = RoseError,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.review_queue_reject),
                        color = RoseError,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
