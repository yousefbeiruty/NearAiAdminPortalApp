package com.nearai.admin.feature.aiusage.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearai.admin.core.designsystem.components.*
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.R

/**
 * AI Usage & Budget Health screen.
 *
 * Displays AI usage metrics when the backend API is available.
 * Currently shows an unavailable state since the backend AI usage API is not yet implemented.
 * No fabricated data is displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiUsageScreen(
    viewModel: AiUsageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ai_usage_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingScreen(
                    message = stringResource(R.string.ai_usage_loading),
                    modifier = modifier.padding(padding)
                )
            }
            uiState.error != null && !uiState.isFeatureAvailable -> {
                ErrorState(
                    message = uiState.error.orEmpty(),
                    modifier = modifier.padding(padding),
                    onRetry = { viewModel.onRetry() }
                )
            }
            uiState.isFeatureAvailable -> {
                // TODO: Render actual AI usage data when the backend API is available.
                // Restore BudgetEnvelopeCard, ModelDistributionSection, QuotaSafeguardsSection,
                // and LiveIngestionSection using real data from the ViewModel.
                EmptyState(
                    title = stringResource(R.string.ai_usage_title),
                    message = stringResource(R.string.ai_usage_no_data),
                    modifier = modifier.padding(padding)
                )
            }
        }
    }
}

/**
 * Preview for the AI Usage screen showing the unavailable state.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AiUsageScreenPreview() {
    NearAiAdminTheme {
        // Preview shows the error/unavailable state since the backend API is not yet available.
        // The actual screen requires Hilt ViewModel injection, which is not available in previews.
        ErrorState(
            message = "AI usage data is not yet available. This feature is coming soon.",
            onRetry = null
        )
    }
}
