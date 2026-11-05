package com.nearai.admin.feature.users.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display update messages as snackbar — consumed once via key tracking
    var lastShownMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.updateMessage) {
        uiState.updateMessage?.let { msg ->
            if (msg != lastShownMessage) {
                snackbarHostState.showSnackbar(msg)
                lastShownMessage = msg
            }
        }
    }

    // Trigger loadMore when user scrolls near the end — keyed by currentPage
    // to avoid re-firing on every recomposition
    LaunchedEffect(uiState.hasMore, uiState.currentPage) {
        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
        val totalItems = listState.layoutInfo.totalItemsCount
        if (lastVisibleItem != null && lastVisibleItem.index >= totalItems - 2 && uiState.hasMore) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.users_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (uiState.users.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onRefresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.users.isEmpty() -> {
                val errorMessage = uiState.error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Failed to load users", color = RoseError)
                        Text(errorMessage ?: "", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { viewModel.onRetry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No users found", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                }
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
                        onQueryChanged = { viewModel.setSearchQuery(it) },
                        placeholder = "Search by email...",
                        onClear = { viewModel.setSearchQuery("") }
                    )

                    // Filter chips in a scrollable row
                    LazyRow(
                        modifier = Modifier.height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        // "All" clears all filters
                        item {
                            FilterChip(
                                selected = uiState.filterRole == null && uiState.filterIsActive == null,
                                onClick = { viewModel.clearFilters() },
                                label = { Text("All") }
                            )
                        }
                        // Role group — mutually exclusive
                        item {
                            FilterChip(
                                selected = uiState.filterRole == "admin",
                                onClick = { viewModel.setFilterRole("admin") },
                                label = { Text("Admin") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterRole == "customer",
                                onClick = { viewModel.setFilterRole("customer") },
                                label = { Text("Customer") }
                            )
                        }
                        // Status group — mutually exclusive
                        item {
                            FilterChip(
                                selected = uiState.filterIsActive == true,
                                onClick = { viewModel.setFilterIsActive(true) },
                                label = { Text("Active") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filterIsActive == false,
                                onClick = { viewModel.setFilterIsActive(false) },
                                label = { Text("Inactive") }
                            )
                        }
                    }

                    // User count pill
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusPill(
                            label = "${uiState.totalItems} users",
                            dotColor = EmeraldSuccess
                        )
                    }

                    // User list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.users) { user ->
                            UserListItem(
                                user = user,
                                onClick = { viewModel.onUserClicked(user) }
                            )
                        }
                        if (uiState.isLoadingMore) {
                            item {
                                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(76.dp)) }
                    }
                }
            }
        }
    }


    // User detail bottom sheet
    uiState.selectedUser?.let { user ->
        UserDetailBottomSheet(
            user = user,
            onDismiss = { viewModel.dismissSelectedUser() },
            onToggleActive = { isActive ->
                viewModel.toggleUserActive(user.id, isActive)
            },
            onRevokeSessions = {
                viewModel.revokeSessions(user.id)
            },
            onUpdateRole = { role ->
                viewModel.updateUserRole(user.id, role)
            }
        )
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
                        label = if (user.isActive) "Active" else "Inactive",
                        dotColor = if (user.isActive) EmeraldSuccess else RoseError
                    )
                    if (user.emailVerified) {
                        StatusPill(label = "Verified", dotColor = Info)
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * User detail bottom sheet with account info and admin actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailBottomSheet(
    user: AdminUser,
    onDismiss: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onRevokeSessions: () -> Unit,
    onUpdateRole: (String) -> Unit
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
                        text = "ID: ${user.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusPill(
                            label = user.role,
                            dotColor = if (user.role == "admin") Indigo else EmeraldSuccess
                        )
                        StatusPill(
                            label = if (user.isActive) "Active" else "Inactive",
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
                    UserDetailRow(label = "Email Verified", value = if (user.emailVerified) "Yes" else "No")
                    UserDetailRow(label = "Created", value = user.createdAt)
                    UserDetailRow(label = "Updated", value = user.updatedAt)
                }
            }

            // Admin actions
            Text(
                text = stringResource(R.string.user_detail_admin_actions),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Toggle active
            if (user.isActive) {
                Button(
                    onClick = { onToggleActive(false) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Icon(Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Suspend Account")
                }
            } else {
                FilledTonalButton(
                    onClick = { onToggleActive(true) },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Activate Account")
                }
            }

            // Revoke sessions
            FilledTonalButton(
                onClick = onRevokeSessions,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Revoke All Sessions")
            }

            // Change role
            if (user.role != "admin") {
                FilledTonalButton(
                    onClick = { onUpdateRole("admin") },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Promote to Admin")
                }
            } else {
                FilledTonalButton(
                    onClick = { onUpdateRole("customer") },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Demote to Customer")
                }
            }
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
            AdminUser("u1", "admin@nearai.com", "admin", true, true, "2024-01-15T10:30:00Z", "2024-01-15T10:30:00Z"),
            AdminUser("u2", "customer@example.com", "customer", true, true, "2024-02-20T08:00:00Z", "2024-02-20T08:00:00Z"),
            AdminUser("u3", "suspended@example.com", "customer", false, false, "2024-03-10T12:00:00Z", "2024-03-10T12:00:00Z"),
            AdminUser("u4", "a.very.long.email.address.that.should.not.overflow.the.card@verylongdomain.com", "customer", true, false, "2024-04-01T00:00:00Z", "2024-04-01T00:00:00Z")
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("User Management", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Navy)
            sampleUsers.forEach { user ->
                UserListItem(user = user, onClick = {})
            }
        }
    }
}
