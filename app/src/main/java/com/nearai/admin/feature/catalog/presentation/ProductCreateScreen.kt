package com.nearai.admin.feature.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.catalog.domain.ProductStatus

/**
 * Product creation screen with form fields, category picker, and validation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCreateScreen(
    onNavigateBack: () -> Unit,
    onProductCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.event.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceAmount by remember { mutableStateOf("") }
    var priceCurrency by remember { mutableStateOf("USD") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf(ProductStatus.ACTIVE) }

    LaunchedEffect(event) {
        if (event is com.nearai.admin.feature.catalog.presentation.ProductCreateEvent.ProductCreated) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.product_create_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight
                )
            )
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Error banner
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = error, color = RoseError, style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = viewModel::clearError, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RoseError, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name *") },
                placeholder = { Text("e.g., Wireless Headphones") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("1-255 characters", color = TextSecondaryLight) }
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description *") },
                placeholder = { Text("Product description...") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Price field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceAmount,
                    onValueChange = { priceAmount = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Price *") },
                    placeholder = { Text("99.99") },
                    leadingIcon = { Text("$", color = TextSecondaryLight, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
                OutlinedTextField(
                    value = priceCurrency,
                    onValueChange = { priceCurrency = it },
                    modifier = Modifier.width(100.dp),
                    label = { Text("Currency") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Status dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { statusExpanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                            Text(
                                text = when (selectedStatus) {
                                    ProductStatus.ACTIVE -> "Active"
                                    ProductStatus.INACTIVE -> "Inactive"
                                    ProductStatus.DRAFT -> "Draft"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(color = Navy)
                            )
                        }
                        Icon(Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondaryLight)
                    }
                }
                DropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                    modifier = Modifier.background(SurfaceLight)
                ) {
                    ProductStatus.ACTIVE.let { s ->
                        DropdownMenuItem(
                            text = { Text("Active") },
                            onClick = { selectedStatus = s; statusExpanded = false }
                        )
                    }
                    ProductStatus.INACTIVE.let { s ->
                        DropdownMenuItem(
                            text = { Text("Inactive") },
                            onClick = { selectedStatus = s; statusExpanded = false }
                        )
                    }
                    ProductStatus.DRAFT.let { s ->
                        DropdownMenuItem(
                            text = { Text("Draft") },
                            onClick = { selectedStatus = s; statusExpanded = false }
                        )
                    }
                }
            }

            // Category picker
            if (uiState.categoriesLoaded) {
                var categoryExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { categoryExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Category", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight))
                                Text(
                                    text = uiState.availableCategories.find { it.id == selectedCategoryId }?.name
                                        ?: "No category",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Navy)
                                )
                            }
                            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondaryLight)
                        }
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(SurfaceLight)
                    ) {
                        DropdownMenuItem(
                            text = { Text("No category") },
                            onClick = { selectedCategoryId = null; categoryExpanded = false }
                        )
                        uiState.availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategoryId = category.id; categoryExpanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create button
            Button(
                onClick = {
                    viewModel.onCreateProduct(
                        name = name,
                        description = description,
                        priceAmount = priceAmount,
                        priceCurrency = priceCurrency,
                        categoryId = selectedCategoryId,
                        status = selectedStatus,
                        onCreated = {
                            // Navigate back after creation
                        }
                    )
                },
                enabled = !uiState.isCreating && name.isNotBlank() && description.isNotBlank() && priceAmount.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (uiState.isCreating) "Creating..." else "Create Product",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProductCreateScreenPreview() {
    NearAiAdminTheme {
        Surface(color = BackgroundLight) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = "Wireless Headphones",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = "Premium noise-canceling headphones",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description *") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("Create Product", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
