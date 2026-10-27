package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme

/**
 * Full-screen loading indicator with a centered CircularProgressIndicator.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state with an icon, title, message, and optional action button.
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(onClick = onActionClick) {
                Text(text = actionText)
            }
        }
    }
}

/**
 * Error state with error message and retry button.
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        onRetry?.let {
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(onClick = it) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

/**
 * Confirmation dialog for sensitive administrative actions.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDestructive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}

/**
 * Preview for LoadingScreen.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 500)
@Composable
fun LoadingScreenPreview() {
    NearAiAdminTheme {
        LoadingScreen(message = "Loading dashboard...")
    }
}

/**
 * Preview for EmptyState.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 500)
@Composable
fun EmptyStatePreview() {
    NearAiAdminTheme {
        EmptyState(
            title = "No Data Available",
            message = "There are no results to display. Try adjusting your filters.",
            actionText = "Clear Filters",
            onActionClick = { }
        )
    }
}

/**
 * Preview for ErrorState.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 500)
@Composable
fun ErrorStatePreview() {
    NearAiAdminTheme {
        ErrorState(
            message = "Failed to load data. Please check your connection and try again.",
            onRetry = { }
        )
    }
}

/**
 * Preview for ConfirmationDialog.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun ConfirmationDialogPreview() {
    NearAiAdminTheme {
        ConfirmationDialog(
            title = "Suspend Account",
            message = "Are you sure you want to suspend this user account? This action will require re-authentication.",
            confirmText = "Suspend",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = { },
            onDismiss = { }
        )
    }
}
