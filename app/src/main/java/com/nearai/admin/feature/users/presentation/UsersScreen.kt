package com.nearai.admin.feature.users.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.core.designsystem.components.AppScreen
import com.nearai.admin.core.designsystem.components.EmptyState
import com.nearai.admin.core.designsystem.components.ErrorState
import com.nearai.admin.core.designsystem.components.LoadMoreIndicator
import com.nearai.admin.core.designsystem.components.PaginatedLazyColumn
import com.nearai.admin.core.designsystem.components.LoadingScreen
import com.nearai.admin.core.designsystem.components.SearchBar
import com.nearai.admin.core.designsystem.components.StatusPill
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.R
import com.nearai.admin.feature.users.domain.AdminUser

/**
 * User Management screen connected to real backend via [UsersViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UsersScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUserClicked = { viewModel.onUserClicked(it) },
        onRetry = { viewModel.onRetry() },
        onRefresh = { viewModel.onRefresh() },
        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
        onClearSearch = { viewModel.setSearchQuery("") },
        onFilterRole = { viewModel.setFilterRole(it) },
        onFilterIsActive = { viewModel.setFilterIsActive(it) },
        onClearFilters = { viewModel.clearFilters() },
        onLoadMore = { viewModel.loadMore() }
    )

    // User detail bottom sheet (read-only inspection)
    uiState.selectedUser?.let { user ->
        UserDetailBottomSheet(
            user = user,
            onDismiss = { viewModel.dismissSelectedUser() }
        )
    }
}

// =============================================================================
// Stateless Content Composable — renders users list from UiState
// =============================================================================

/**
 * Stateless users list content that renders the full screen from [UsersUiState].
 *
 * This composable can be used independently in tests and screenshot previews
 * without requiring a ViewModel or Hilt dependency injection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreenContent(
    uiState: UsersUiState,
    onNavigateBack: () -> Unit,
    onUserClicked: (AdminUser) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterRole: (String?) -> Unit,
    onFilterIsActive: (Boolean?) -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppScreen(
        title = stringResource(R.string.users_title),
        onNavigateBack = onNavigateBack,
        actions = {
            if (uiState.users.isNotEmpty()) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_refresh))
                }
            }
        },
        modifier = modifier
    ) { padding ->
        when {
            uiState.isLoading && uiState.users.isEmpty() -> {
                LoadingScreen(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier
                )
            }
            uiState.error != null && uiState.users.isEmpty() -> {
                ErrorState(
                    message = uiState.error.orEmpty(),
                    retryText = stringResource(R.string.common_retry),
                    onRetry = onRetry
                )
            }
            uiState.users.isEmpty() && uiState.error == null -> {
                EmptyState(
                    title = stringResource(R.string.users_empty),
                    message = stringResource(R.string.common_no_data_message)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search bar
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = onSearchQueryChanged,
                        placeholder = stringResource(R.string.users_search_hint),
                        onClear = onClearSearch
                    )

                    // Filter chips in a scrollable row
                    LazyRow(
                        modifier = Modifier.height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        contentPadding = PaddingValues(horizontal = PaddingLarge)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterRole == null && uiState.filterIsActive == null,
                                onClick = onClearFilters,
                                label = { Text(stringResource(R.string.users_filter_all)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterRole == "admin",
                                onClick = { onFilterRole("admin") },
                                label = { Text(stringResource(R.string.users_filter_admin)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterRole == "customer",
                                onClick = { onFilterRole("customer") },
                                label = { Text(stringResource(R.string.users_filter_customer)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterIsActive == true,
                                onClick = { onFilterIsActive(true) },
                                label = { Text(stringResource(R.string.users_filter_active)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterIsActive == false,
                                onClick = { onFilterIsActive(false) },
                                label = { Text(stringResource(R.string.users_filter_inactive)) }
                            )
                        }
                    }

                    // User count pill
                    Row(
                        modifier = Modifier.padding(horizontal = PaddingLarge, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusPill(
                            label = stringResource(R.string.users_count, uiState.totalItems),
                            dotColor = EmeraldSuccess
                        )
                    }

                    // User list with pagination
                    PaginatedLazyColumn(
                        dataItems = uiState.users,
                        onLoadMore = onLoadMore,
                        isLoadingMore = uiState.isLoadingMore,
                        hasMore = uiState.hasMore,
                        loadMoreThreshold = 2,
                        key = { it.id },
                        contentPadding = PaddingValues(horizontal = PaddingLarge, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        item = { user ->
                            UserListItem(
                                user = user,
                                onClick = { onUserClicked(user) }
                            )
                        },
                        footer = {
                            item { Spacer(modifier = Modifier.height(76.dp)) }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual user list item showing email, role, and status.
 */
@Composable
fun UserListItem(
    user: AdminUser,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Indigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(24.dp)
                )
            }
            // User info — email on its own line to avoid overflow with role pill
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, true)
                    )
                    StatusPill(
                        label = user.role,
                        dotColor = if (user.role == "admin") Indigo else EmeraldSuccess
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        label = if (user.isActive) stringResource(R.string.status_active) else stringResource(R.string.status_inactive),
                        dotColor = if (user.isActive) EmeraldSuccess else RoseError
                    )
                    if (user.emailVerified) {
                        StatusPill(label = stringResource(R.string.users_verified), dotColor = Info)
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.user_detail_view_details),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * User detail bottom sheet — read-only inspection for platform admin.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailBottomSheet(
    user: AdminUser,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User header
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Indigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Indigo,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.user_detail_id, user.id),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusPill(
                            label = user.role,
                            dotColor = if (user.role == "admin" || user.role == "platform_admin") Indigo else EmeraldSuccess
                        )
                        StatusPill(
                            label = if (user.isActive) stringResource(R.string.status_active) else stringResource(R.string.status_inactive),
                            dotColor = if (user.isActive) EmeraldSuccess else RoseError
                        )
                    }
                }
            }

            // Account details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.user_detail_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UserDetailRow(label = stringResource(R.string.user_detail_email_verified), value = if (user.emailVerified) stringResource(R.string.user_detail_yes) else stringResource(R.string.user_detail_no))
                    user.tenantId?.let {
                        UserDetailRow(label = "Tenant ID", value = it)
                    }
                    UserDetailRow(label = stringResource(R.string.user_detail_created), value = user.createdAt)
                    UserDetailRow(label = stringResource(R.string.user_detail_updated), value = user.updatedAt)
                }
            }

            Text(
                text = "Read-only inspection — no mutation operations available.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserDetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Preview for the User Management screen.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun UsersScreenPreview() {
    NearAiAdminTheme {
        val sampleUsers = listOf(
            AdminUser("u1", "admin@nearai.com", "admin", true, true, null, "2024-01-15T10:30:00Z", "2024-01-15T10:30:00Z"),
            AdminUser("u2", "customer@example.com", "customer", true, true, "tenant-1", "2024-02-20T08:00:00Z", "2024-02-20T08:00:00Z"),
            AdminUser("u3", "suspended@example.com", "customer", false, false, "tenant-2", "2024-03-10T12:00:00Z", "2024-03-10T12:00:00Z"),
            AdminUser("u4", "a.very.long.email.address.that.should.not.overflow.the.card@verylongdomain.com", "customer", true, false, "tenant-1", "2024-04-01T00:00:00Z", "2024-04-01T00:00:00Z")
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.users_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Navy)
            sampleUsers.forEach { user ->
                UserListItem(user = user, onClick = {})
            }
        }
    }
}
