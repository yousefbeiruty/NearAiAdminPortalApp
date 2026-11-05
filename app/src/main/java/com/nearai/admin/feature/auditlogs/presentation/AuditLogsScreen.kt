package com.nearai.admin.feature.auditlogs.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.core.designsystem.components.EmptyState
import com.nearai.admin.core.designsystem.components.ErrorState
import com.nearai.admin.core.designsystem.components.LoadingScreen
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Audit Logs screen connected to the real backend API.
 *
 * Displays chronological audit entries with expandable details,
 * filtering by action/resource type, pagination, and proper loading/error states.
 */

// =============================================================================
// Helpers
// =============================================================================

private val timestampFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

/**
 * Formats an ISO-8601 timestamp string into a human-readable display time.
 */
fun formatTimestamp(isoString: String): String {
    return try {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone("UTC")
        val date = df.parse(isoString) ?: df.parse(isoString.replace("Z", "")) ?: return isoString
        timestampFormat.format(date)
    } catch (_: Exception) {
        // Fallback: try without Z suffix
        try {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = df.parse(isoString) ?: return isoString
            timestampFormat.format(date)
        } catch (_: Exception) {
            isoString
        }
    }
}

/**
 * Returns a display label for an action string.
 */
fun actionDisplayLabel(action: String): String {
    return action.replace(".", " ").replaceFirstChar { it.uppercase() }
}

/**
 * Returns a color for a resource type badge.
 */
fun resourceTypeColor(resourceType: String): Color {
    return when (resourceType.lowercase()) {
        "product" -> EmeraldSuccess
        "order" -> Indigo
        "user" -> PurpleDark
        "stock" -> AmberWarning
        else -> Info
    }
}

/**
 * Returns an icon for an action.
 */
fun actionIcon(action: String) = when {
    action.contains("create", ignoreCase = true) -> Icons.Filled.AddCircle
    action.contains("update", ignoreCase = true) -> Icons.Filled.Edit
    action.contains("suspend", ignoreCase = true) -> Icons.Filled.Block
    action.contains("cancel", ignoreCase = true) -> Icons.Filled.Cancel
    action.contains("receive", ignoreCase = true) -> Icons.Filled.Download
    action.contains("adjust", ignoreCase = true) -> Icons.Filled.Tune
    action.contains("revoke", ignoreCase = true) -> Icons.Filled.LockReset
    action.contains("confirm", ignoreCase = true) -> Icons.Filled.CheckCircle
    action.contains("deliver", ignoreCase = true) -> Icons.Filled.LocalShipping
    else -> Icons.Filled.Assignment
}

// =============================================================================
// Audit Logs Screen Composable
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(
    viewModel: AuditLogsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Trigger loadMore when scrolling near the end
    LaunchedEffect(uiState.hasMore, uiState.currentPage) {
        if (!uiState.hasMore || uiState.isLoadingMore) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisibleIndex ->
                val totalItems = uiState.logs.size
                if (lastVisibleIndex >= totalItems - 2) {
                    viewModel.loadMore()
                }
            }
    }

    // Snackbar for errors
    var lastShownError by remember { mutableStateOf<String?>(null) }
    if (uiState.error != null && uiState.error != lastShownError) {
        lastShownError = uiState.error
        viewModel.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.audit_logs_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Clear filters button when filters are active
                    if (uiState.filterAction != null ||
                        uiState.filterResourceType != null ||
                        uiState.filterResourceId != null ||
                        uiState.filterStartDate != null ||
                        uiState.filterEndDate != null) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.logs.isEmpty() -> {
                LoadingScreen(message = "Loading audit logs...")
            }
            uiState.error != null && uiState.logs.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: "Failed to load audit logs",
                    onRetry = { viewModel.onRefresh() }
                )
            }
            uiState.logs.isEmpty() -> {
                EmptyState(
                    title = "No Audit Logs",
                    message = "No audit logs match the current filters.",
                    actionText = if (uiState.filterAction != null || uiState.filterResourceType != null) "Clear Filters" else null,
                    onActionClick = { viewModel.clearFilters() }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Filter chips
                    AuditLogsFilterBar(
                        uiState = uiState,
                        onFilterAction = { viewModel.setFilterAction(it) },
                        onFilterResourceType = { viewModel.setFilterResourceType(it) },
                        onClearFilters = { viewModel.clearFilters() }
                    )

                    // Log count
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.audit_logs_entries_count, uiState.totalItems),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Log list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.logs, key = { it.id }) { log ->
                            AuditLogCard(
                                log = log,
                                isExpanded = uiState.expandedLogId == log.id,
                                onToggleExpand = { viewModel.onToggleExpand(log.id) }
                            )
                        }
                        // Loading more indicator
                        if (uiState.isLoadingMore) {
                            items(1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
// Filter Bar
// =============================================================================

@Composable
private fun AuditLogsFilterBar(
    uiState: AuditLogsUiState,
    onFilterAction: (String?) -> Unit,
    onFilterResourceType: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    // Collect unique actions and resource types from the current logs for filter chips
    val actions = remember(uiState.logs) {
        uiState.logs.mapNotNull { it.action }.distinct().sorted()
    }
    val resourceTypes = remember(uiState.logs) {
        uiState.logs.mapNotNull { it.resourceType }.distinct().sorted()
    }

    Column {
        // Action filter chips
        if (actions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterAction == null,
                    onClick = { onFilterAction(null) },
                    label = { Text("All") }
                )
                actions.take(5).forEach { action ->
                    FilterChip(
                        selected = uiState.filterAction == action,
                        onClick = { onFilterAction(action) },
                        label = {
                            Text(
                                text = actionDisplayLabel(action),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }

        // Resource type filter chips
        if (resourceTypes.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterResourceType == null,
                    onClick = { onFilterResourceType(null) },
                    label = { Text("All Types") }
                )
                resourceTypes.forEach { type ->
                    FilterChip(
                        selected = uiState.filterResourceType == type,
                        onClick = { onFilterResourceType(type) },
                        label = { Text(type) }
                    )
                }
            }
        }
    }
}

// =============================================================================
// Audit Log Card
// =============================================================================

@Composable
private fun AuditLogCard(
    log: com.nearai.admin.feature.auditlogs.domain.AuditLog,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onToggleExpand
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row: icon, action, timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action icon
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = resourceTypeColor(log.resourceType).copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = actionIcon(log.action),
                            contentDescription = null,
                            tint = resourceTypeColor(log.resourceType),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Action and resource info
                Column(modifier = Modifier.weight(1f, true)) {
                    Text(
                        text = actionDisplayLabel(log.action),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Resource type badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = resourceTypeColor(log.resourceType).copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                resourceTypeColor(log.resourceType).copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = log.resourceType,
                                style = MaterialTheme.typography.labelSmall,
                                color = resourceTypeColor(log.resourceType),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        // Resource ID
                        if (log.resourceId != null) {
                            Text(
                                text = stringResource(R.string.audit_logs_resource_id, log.resourceId.take(8)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Timestamp and actor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(log.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.actorId != null) {
                    Text(
                        text = stringResource(R.string.audit_logs_by_actor, log.actorId.take(8)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expanded details
            if (isExpanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Actor details
                AuditLogDetailRow(
                    label = "Actor ID",
                    value = log.actorId ?: "System"
                )
                AuditLogDetailRow(
                    label = "Actor Role",
                    value = log.actorRole
                )
                AuditLogDetailRow(
                    label = "Action",
                    value = log.action
                )
                AuditLogDetailRow(
                    label = "Resource",
                    value = "${log.resourceType}${if (log.resourceId != null) " / ${log.resourceId}" else ""}"
                )
                log.requestId?.let {
                    AuditLogDetailRow(
                        label = "Request ID",
                        value = it
                    )
                }

                // Changes JSON
                log.changes?.let { changesJson ->
                    if (changesJson.isNotBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.audit_logs_changes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        JsonDisplay(jsonString = changesJson)
                    }
                }

                // Metadata JSON
                log.metadata?.let { metadataJson ->
                    if (metadataJson.isNotBlank()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.audit_logs_metadata),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        JsonDisplay(jsonString = metadataJson)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Detail Row
// =============================================================================

@Composable
private fun AuditLogDetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// Changes Display — renders key/value pairs from the changes map
// =============================================================================

@Composable
private fun JsonDisplay(jsonString: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = jsonString,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp),
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// Preview
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 390, heightDp = 700)
@Composable
fun AuditLogsScreenPreview() {
    NearAiAdminTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top app bar
            TopAppBar(
                title = { Text("Audit Logs") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf(
                        com.nearai.admin.feature.auditlogs.domain.AuditLog(
                            id = "log-1",
                            actorId = "admin-uuid-1234",
                            actorRole = "admin",
                            action = "product.create",
                            resourceType = "product",
                            resourceId = "prod-uuid-5678",
                            requestId = "req-123",
                            changes = """{"name":"New Product","price":29.99,"status":"active"}""",
                            metadata = """{"ip_address":"192.168.1.1"}""",
                            createdAt = "2025-11-04T10:30:00Z"
                        ),
                        com.nearai.admin.feature.auditlogs.domain.AuditLog(
                            id = "log-2",
                            actorId = "admin-uuid-1234",
                            actorRole = "admin",
                            action = "stock.adjust",
                            resourceType = "stock",
                            resourceId = "variant-uuid-9012",
                            requestId = "req-456",
                            changes = """{"before":100,"after":85,"delta":-15,"reason":"Damaged goods"}""",
                            metadata = null,
                            createdAt = "2025-11-04T09:15:00Z"
                        ),
                        com.nearai.admin.feature.auditlogs.domain.AuditLog(
                            id = "log-3",
                            actorId = "admin-uuid-1234",
                            actorRole = "admin",
                            action = "user.suspend",
                            resourceType = "user",
                            resourceId = "user-uuid-3456",
                            requestId = "req-789",
                            changes = """{"is_active":false}""",
                            metadata = """{"reason":"Policy violation"}""",
                            createdAt = "2025-11-04T08:00:00Z"
                        ),
                        com.nearai.admin.feature.auditlogs.domain.AuditLog(
                            id = "log-4",
                            actorId = null,
                            actorRole = "system",
                            action = "order.cancel",
                            resourceType = "order",
                            resourceId = "order-uuid-7890",
                            requestId = null,
                            changes = """{"status":"cancelled","reason":"Expired reservation"}""",
                            metadata = null,
                            createdAt = "2025-11-03T23:45:00Z"
                        )
                    ),
                    key = { it.id }
                ) { log ->
                    AuditLogCard(
                        log = log,
                        isExpanded = log.id == "log-1",
                        onToggleExpand = {}
                    )
                }
            }
        }
    }
}
