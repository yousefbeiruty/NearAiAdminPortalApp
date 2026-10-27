package com.nearai.admin.feature.auditlogs.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        EmptyState(
            title = "Audit Logs",
            message = "Audit log viewing will be available once the backend API is connected.",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
