package com.nearai.admin.feature.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.nearai.admin.R
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.designsystem.components.StatusPill
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.catalog.domain.Category
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Categories list screen with pagination, pull-to-refresh, and create category dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListScreen(
    viewModel: CategoriesListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProducts: (categoryId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.event.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false )}
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") }

    LaunchedEffect(event) {
        when (event) {
            is CategoriesListEvent.ShowCreateDialog -> showCreateDialog = true
            is CategoriesListEvent.HideCreateDialog -> {
                showCreateDialog = false
                categoryName = ""
                categoryDescription = ""
            }
            is CategoriesListEvent.CategoryCreated -> {
                showCreateDialog = false
                categoryName = ""
                categoryDescription = ""
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.categories_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.categories_back),
                            tint = Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onShowCreateDialog,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.categories_new_category), fontWeight = FontWeight.Bold) },
                containerColor = Navy,
                contentColor = Color.White
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Indigo)
                }
            }
            uiState.error != null && uiState.categories.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.categories_error_loading),
                        style = MaterialTheme.typography.bodyLarge.copy(color = RoseError)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text(stringResource(R.string.categories_retry), color = Color.White)
                    }
                }
            }
            uiState.categories.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = TextSecondaryLight,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.categories_empty),
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondaryLight)
                    )
                    Text(
                        text = stringResource(R.string.categories_empty_message),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.categories) { index, category ->
                        CategoryCard(
                            category = category,
                            onClick = { onNavigateToProducts(category.id) }
                        )
                        if (index == uiState.categories.lastIndex && uiState.hasMore) {
                            if (uiState.isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Indigo,
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else {
                                LaunchedEffect(Unit) { viewModel.loadMore() }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Category Dialog
    if (showCreateDialog) {
        CreateCategoryDialog(
            categoryName = categoryName,
            categoryDescription = categoryDescription,
            onNameChange = { categoryName = it },
            onDescriptionChange = { categoryDescription = it },
            onDismiss = viewModel::onHideCreateDialog,
            onCreate = {
                viewModel.onCreateCategory(categoryName, categoryDescription, null)
            },
            isLoading = uiState.isCreating,
            error = uiState.createError
        )
    }
}

/**
 * Category card composable.
 */
@Composable
fun CategoryCard(
    category: Category,
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
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Indigo.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Navy
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (category.description.isNotEmpty()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight,
                            fontSize = 13.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        label = category.slug,
                        dotColor = Indigo,
                        backgroundColor = Indigo.copy(alpha = 0.1f),
                        contentColor = Indigo
                    )
                    Text(
                        text = formatCategoryDate(category.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryLight
                        )
                    )
                }
            }
            // Navigation arrow
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.categories_view_products),
                tint = TextSecondaryLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Format the category creation date for display.
 */
private fun formatCategoryDate(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(createdAt)
        date?.let { outputFormat.format(it) } ?: createdAt
    } catch (e: Exception) {
        // Fallback: show a shortened version of the raw date string
        if (createdAt.length > 10) createdAt.substring(0, 10) else createdAt
    }
}

/**
 * Dialog for creating a new category.
 */
@Composable
fun CreateCategoryDialog(
    categoryName: String,
    categoryDescription: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.categories_new_category), fontWeight = FontWeight.Bold, color = Navy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.categories_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (error != null) {
                            Text(error, color = RoseError)
                        }
                    }
                )
                OutlinedTextField(
                    value = categoryDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.categories_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = !isLoading && categoryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.categories_create), color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.categories_cancel))
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun CategoriesListScreenPreview() {
    NearAiAdminTheme {
        // Preview with sample category cards — avoids Hilt ViewModel dependency
        val sampleCategories = listOf(
            Category(
                id = "c1",
                name = "Electronics",
                slug = "electronics",
                description = "Electronic devices and accessories",
                parentId = null,
                createdAt = "2024-01-15T10:30:00+00:00"
            ),
            Category(
                id = "c2",
                name = "Home & Garden",
                slug = "home-garden",
                description = "Furniture, decor, and outdoor items",
                parentId = null,
                createdAt = "2024-02-20T08:00:00+00:00"
            ),
            Category(
                id = "c3",
                name = "Clothing",
                slug = "clothing",
                description = "Apparel and fashion accessories",
                parentId = null,
                createdAt = "2024-03-10T12:00:00+00:00"
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.categories_title), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Navy)
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.categories_new_category),
                        tint = Navy
                    )
                }
            }
            sampleCategories.forEach { category ->
                CategoryCard(category = category, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun CategoryCardPreview() {
    NearAiAdminTheme {
        CategoryCard(
            category = Category(
                id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                name = "Electronics",
                slug = "electronics",
                description = "Electronic devices and accessories",
                parentId = null,
                createdAt = "2024-01-15T10:30:00+00:00"
            ),
            onClick = {}
        )
    }
}
