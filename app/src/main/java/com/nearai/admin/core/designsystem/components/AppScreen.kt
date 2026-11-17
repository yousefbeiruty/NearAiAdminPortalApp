package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.nearai.admin.core.designsystem.theme.BackgroundLight
import com.nearai.admin.core.designsystem.theme.IconButtonSize
import com.nearai.admin.core.designsystem.theme.Navy
import com.nearai.admin.core.designsystem.theme.SurfaceLight

/**
 * Shared screen wrapper that provides a consistent Scaffold + TopAppBar with a back button.
 *
 * This composable eliminates the repeated Scaffold + TopAppBar pattern found across
 * all sub-destination screens. It uses the established visual style: Navy title,
 * AutoMirrored back button, surface container color, and BackgroundLight scaffold background.
 *
 * @param title Screen title displayed in the top app bar.
 * @param onNavigateBack Called when the back button is pressed. Pass null for screens
 *                       without a back button (e.g., Dashboard).
 * @param actions Optional action buttons in the top app bar.
 * @param floatingActionButton Optional floating action button.
 * @param content Screen content, padded by the scaffold inner padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                },
                navigationIcon = {
                    onNavigateBack?.let { onBack ->
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(IconButtonSize)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Navy
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight
                )
            )
        },
        floatingActionButton = floatingActionButton,
        containerColor = BackgroundLight
    ) { padding ->
        content(padding)
    }
}

/**
 * Scaffold variant without a top app bar, for full-screen layouts.
 *
 * Uses the established BackgroundLight container color for consistency.
 *
 * @param modifier Modifier for the scaffold.
 * @param content Screen content, padded by the scaffold inner padding.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = BackgroundLight
    ) { padding ->
        content(padding)
    }
}

// =============================================================================
// Previews
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppScreenWithBackPreview() {
    MaterialTheme {
        AppScreen(
            title = "Orders",
            onNavigateBack = { }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Content")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppScreenWithoutBackPreview() {
    MaterialTheme {
        AppScreen(
            title = "Dashboard"
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Content")
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppScaffoldPreview() {
    MaterialTheme {
        AppScaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Full screen content")
            }
        }
    }
}
