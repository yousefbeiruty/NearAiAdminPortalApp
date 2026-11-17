package com.nearai.admin.feature.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.components.AppScreen
import com.nearai.admin.core.designsystem.components.EmptyState
import com.nearai.admin.core.designsystem.components.ErrorState
import com.nearai.admin.core.designsystem.components.PaginatedLazyColumn
import com.nearai.admin.core.designsystem.components.LoadingScreen
import com.nearai.admin.core.designsystem.components.SearchBar
import com.nearai.admin.core.designsystem.components.StatusPill
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.catalog.domain.Product
import com.nearai.admin.feature.catalog.domain.ProductStatus

/**
 * Products list screen with search, status filter, pagination, and create product dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
    viewModel: ProductsListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        when (event) {
            is ProductsListEvent.ProductCreated -> {
                // Navigate to the newly created product or stay on list
            }
            else -> {}
        }
    }

    ProductsListScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToProduct = onNavigateToProduct,
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onClearSearch = { viewModel.onSearchQueryChanged("") },
        onStatusFilterChanged = { viewModel.onStatusFilterChanged(it) },
        onRetry = { viewModel.onRetry() },
        onLoadMore = { viewModel.loadMore() },
        modifier = modifier
    )
}

// =============================================================================
// Stateless Content Composable — renders products list from UiState
// =============================================================================

/**
 * Stateless products list content that renders the full screen from [ProductsListUiState].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreenContent(
    uiState: ProductsListUiState,
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onStatusFilterChanged: (ProductStatus?) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppScreen(
        title = stringResource(R.string.products_title),
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.products_new_product), fontWeight = FontWeight.Bold) },
                containerColor = Navy,
                contentColor = Color.White
            )
        },
        modifier = modifier
    ) { padding ->
        when {
            uiState.isLoading && uiState.products.isEmpty() -> {
                LoadingScreen(
                    message = stringResource(R.string.common_loading)
                )
            }
            uiState.error != null && uiState.products.isEmpty() -> {
                ErrorState(
                    message = uiState.error.orEmpty(),
                    retryText = stringResource(R.string.common_retry),
                    onRetry = onRetry
                )
            }
            uiState.products.isEmpty() && uiState.error == null -> {
                EmptyState(
                    title = stringResource(R.string.products_empty),
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
                        placeholder = stringResource(R.string.products_search_hint),
                        onClear = onClearSearch
                    )

                    // Status filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = PaddingLarge)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedStatus == null,
                                onClick = { onStatusFilterChanged(null) },
                                label = { Text(stringResource(R.string.products_filter_all)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedStatus == ProductStatus.ACTIVE,
                                onClick = { onStatusFilterChanged(ProductStatus.ACTIVE) },
                                label = { Text(stringResource(R.string.products_status_active)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedStatus == ProductStatus.INACTIVE,
                                onClick = { onStatusFilterChanged(ProductStatus.INACTIVE) },
                                label = { Text(stringResource(R.string.products_status_inactive)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedStatus == ProductStatus.DRAFT,
                                onClick = { onStatusFilterChanged(ProductStatus.DRAFT) },
                                label = { Text(stringResource(R.string.products_status_draft)) }
                            )
                        }
                    }

                    // Product list with pagination
                    PaginatedLazyColumn(
                        dataItems = uiState.products,
                        onLoadMore = onLoadMore,
                        isLoadingMore = uiState.isLoadingMore,
                        hasMore = uiState.hasMore,
                        loadMoreThreshold = 3,
                        key = { it.id },
                        contentPadding = PaddingValues(horizontal = PaddingLarge, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        item = { product ->
                            ProductCard(
                                product = product,
                                onClick = { onNavigateToProduct(product.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Product card composable.
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image — backend returns full URLs in product.images.
            // Falls back to placeholder when no images are available.
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val firstImage = product.images.firstOrNull()
                if (firstImage != null && firstImage.isNotBlank()) {
                    AsyncImage(
                        model = firstImage,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = TextSecondaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Navy
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondaryLight,
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.priceCurrency} ${"%.2f".format(product.priceAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    StatusPill(
                        label = when (product.status) {
                            ProductStatus.ACTIVE -> stringResource(R.string.products_status_active)
                            ProductStatus.INACTIVE -> stringResource(R.string.products_status_inactive)
                            ProductStatus.DRAFT -> stringResource(R.string.products_status_draft)
                        },
                        dotColor = when (product.status) {
                            ProductStatus.ACTIVE -> EmeraldSuccess
                            ProductStatus.INACTIVE -> TextSecondaryLight
                            ProductStatus.DRAFT -> AmberWarning
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun ProductCardPreview() {
    NearAiAdminTheme {
        ProductCard(
            product = Product(
                id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                name = "Wireless Headphones",
                description = "Premium noise-canceling headphones with 30hr battery life",
                slug = "wireless-headphones",
                priceAmount = 99.99,
                priceCurrency = "USD",
                images = emptyList(),
                status = ProductStatus.ACTIVE,
                categoryId = null,
                createdAt = "2024-01-15T10:30:00+00:00",
                updatedAt = "2024-01-15T10:30:00+00:00"
            ),
            onClick = {}
        )
    }
}
