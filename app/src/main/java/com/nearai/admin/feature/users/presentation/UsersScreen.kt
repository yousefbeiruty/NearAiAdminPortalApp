package com.nearai.admin.feature.users.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users") }
            )
        }
    ) { padding ->
        EmptyState(
            title = "Users Management",
            message = "User management features will be available once the backend API is connected.",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
