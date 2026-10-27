package com.nearai.admin.feature.users.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.components.*
import com.nearai.admin.core.designsystem.theme.*

/**
 * User Management screen with search, filter chips, user list,
 * and a user detail bottom sheet.
 *
 * Uses development-only placeholder data until the backend API is connected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedUser by remember { mutableStateOf<UserItem?>(null) }

    val filterChips = listOf(
        FilterChipItem("all", "All Users"),
        FilterChipItem("pro", "Pro Tier"),
        FilterChipItem("free", "Free Plan"),
        FilterChipItem("flagged", "Flagged")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "User Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        StatusPill(
                            label = "14,820 total",
                            dotColor = Color(0xFF10B981)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    placeholder = "Search users by name, email, or ID...",
                    onClear = { searchQuery = "" }
                )
            }

            // Filter chips
            item {
                FilterChips(
                    filters = filterChips,
                    selectedFilters = selectedFilters,
                    onFilterSelected = { selectedFilters = selectedFilters + it },
                    onFilterDeselected = { selectedFilters = selectedFilters - it }
                )
            }

            // User list
            items(sampleUsers) { user ->
                UserListItem(
                    user = user,
                    onClick = { selectedUser = user }
                )
            }
        }
    }

    // User detail bottom sheet
    selectedUser?.let { user ->
        UserDetailBottomSheet(
            user = user,
            onDismiss = { selectedUser = null }
        )
    }
}

/**
 * User item data class.
 */
data class UserItem(
    val id: String,
    val name: String,
    val email: String,
    val tier: String,
    val status: String,
    val creditsRemaining: Int,
    val subscriptionPrice: String,
    val lastActive: String,
    val avatarColor: Color
)

/**
 * Sample user data for development.
 */
val sampleUsers = listOf(
    UserItem(
        id = "user_8472",
        name = "Maya Lin",
        email = "maya.lin@example.com",
        tier = "Pro",
        status = "Active",
        creditsRemaining = 84,
        subscriptionPrice = "$29/mo",
        lastActive = "2m ago",
        avatarColor = Indigo
    ),
    UserItem(
        id = "user_3921",
        name = "James Chen",
        email = "james.chen@example.com",
        tier = "Free",
        status = "Active",
        creditsRemaining = 12,
        subscriptionPrice = "Free",
        lastActive = "15m ago",
        avatarColor = Color(0xFF10B981)
    ),
    UserItem(
        id = "user_1205",
        name = "Sarah Williams",
        email = "sarah.w@example.com",
        tier = "Pro",
        status = "Flagged",
        creditsRemaining = 5,
        subscriptionPrice = "$29/mo",
        lastActive = "1h ago",
        avatarColor = Color(0xFFEF4444)
    ),
    UserItem(
        id = "user_6789",
        name = "Alex Rivera",
        email = "alex.r@nearai.internal",
        tier = "Pro",
        status = "Active",
        creditsRemaining = 92,
        subscriptionPrice = "$29/mo",
        lastActive = "5m ago",
        avatarColor = Color(0xFFF59E0B)
    ),
    UserItem(
        id = "user_4532",
        name = "Priya Patel",
        email = "priya.p@example.com",
        tier = "Free",
        status = "Active",
        creditsRemaining = 45,
        subscriptionPrice = "Free",
        lastActive = "30m ago",
        avatarColor = Color(0xFF3B82F6)
    )
)

/**
 * Individual user list item.
 */
@Composable
fun UserListItem(
    user: UserItem,
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
                    .background(user.avatarColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = user.avatarColor
                )
            }
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusPill(
                        label = user.tier,
                        dotColor = if (user.tier == "Pro") Indigo else Color(0xFF94A3B8)
                    )
                    if (user.status == "Flagged") {
                        StatusPill(
                            label = "Flagged",
                            dotColor = Color(0xFFEF4444)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${user.creditsRemaining}% credits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Active ${user.lastActive}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Chevron
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
 * User detail bottom sheet with credits gauge, model logs, and admin actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailBottomSheet(
    user: UserItem,
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
                        .background(user.avatarColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = user.avatarColor
                    )
                }
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusPill(
                            label = user.tier,
                            dotColor = if (user.tier == "Pro") Indigo else Color(0xFF94A3B8)
                        )
                        StatusPill(
                            label = user.status,
                            dotColor = if (user.status == "Active") Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }

            // Subscription details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Subscription",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user.tier} Plan · ${user.subscriptionPrice}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Credits balance gauge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Credits Balance",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${user.creditsRemaining}% remaining",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (user.creditsRemaining > 30) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { user.creditsRemaining / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = if (user.creditsRemaining > 30) Color(0xFF10B981) else Color(0xFFEF4444),
                        trackColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Resets on Nov 1, 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Model invocation logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Recent Model Invocations",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ModelInvocationRow(model = "Llama-3", tokens = 1240, time = "5m ago")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ModelInvocationRow(model = "Mistral", tokens = 890, time = "12m ago")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ModelInvocationRow(model = "GPT-4o-mini", tokens = 2100, time = "28m ago")
                }
            }

            // Admin action buttons
            Text(
                text = "Administrative Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { /* TODO: Grant bonus credits */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Grant Credits")
                }
                FilledTonalButton(
                    onClick = { /* TODO: Reset daily quota */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Quota")
                }
            }
            Button(
                onClick = { /* TODO: Suspend account */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Suspend Account")
            }
        }
    }
}

/**
 * Model invocation log row.
 */
@Composable
fun ModelInvocationRow(
    model: String,
    tokens: Int,
    time: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = model,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$tokens tokens",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Preview for the User Management screen.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun UsersScreenPreview() {
    NearAiAdminTheme {
        UsersScreen()
    }
}
