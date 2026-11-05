package com.nearai.admin.feature.catalog.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import com.nearai.admin.R
import com.nearai.admin.core.designsystem.components.StatusPill
import com.nearai.admin.core.designsystem.theme.*
import com.nearai.admin.feature.catalog.domain.*

/**
 * Immutable UI state for the Product Details screen.
 */
data class ProductDetailsUiState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val variants: List<ProductVariant> = emptyList(),
    val images: List<ProductImage> = emptyList(),
    val error: String? = null,
    val isStockActionLoading: Boolean = false,
    val stockActionError: String? = null,
    val showReceiveStockDialog: Boolean = false,
    val showAdjustStockDialog: Boolean = false,
    val selectedVariantId: String? = null,
    val isImageUploading: Boolean = false,
    val imageUploadError: String? = null,
    val showDeleteImageConfirm: Boolean = false,
    val selectedImageId: String? = null
)

sealed interface ProductDetailsEvent {
    object StockUpdated : ProductDetailsEvent
    object ImagesUpdated : ProductDetailsEvent
}

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<ProductDetailsEvent?>(null)
    val event: StateFlow<ProductDetailsEvent?> = _event.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = catalogRepository.getProduct(productId)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            product = result.data,
                            error = null
                        )
                    }
                    // Load variants and images
                    catalogRepository.listVariants(productId).let { variantResult ->
                        if (variantResult is com.nearai.admin.core.common.Result.Success) {
                            _uiState.update { it.copy(variants = variantResult.data.variants) }
                        }
                    }
                    catalogRepository.listProductImages(productId).let { imageResult ->
                        if (imageResult is com.nearai.admin.core.common.Result.Success) {
                            _uiState.update { it.copy(images = imageResult.data) }
                        }
                    }
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun onReceiveStock(variantId: String, quantity: Int, reason: String) {
        if (quantity <= 0) {
            _uiState.update { it.copy(stockActionError = "Quantity must be greater than 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStockActionLoading = true, stockActionError = null) }
            when (val result = catalogRepository.receiveStock(variantId, quantity, reason)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update { it.copy(isStockActionLoading = false, showReceiveStockDialog = false) }
                    _event.value = ProductDetailsEvent.StockUpdated
                    refreshVariants()
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isStockActionLoading = false,
                            stockActionError = result.exception.message
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun onAdjustStock(variantId: String, quantityDelta: Int, reason: String) {
        if (reason.isBlank()) {
            _uiState.update { it.copy(stockActionError = "Reason is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStockActionLoading = true, stockActionError = null) }
            when (val result = catalogRepository.adjustStock(variantId, quantityDelta, reason)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update { it.copy(isStockActionLoading = false, showAdjustStockDialog = false) }
                    _event.value = ProductDetailsEvent.StockUpdated
                    refreshVariants()
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isStockActionLoading = false,
                            stockActionError = result.exception.message
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    private fun refreshVariants() {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            catalogRepository.listVariants(productId).let { result ->
                if (result is com.nearai.admin.core.common.Result.Success) {
                    _uiState.update { it.copy(variants = result.data.variants) }
                }
            }
        }
    }

    fun onShowReceiveStockDialog(variantId: String) {
        _uiState.update {
            it.copy(
                showReceiveStockDialog = true,
                selectedVariantId = variantId,
                stockActionError = null
            )
        }
    }

    fun onShowAdjustStockDialog(variantId: String) {
        _uiState.update {
            it.copy(
                showAdjustStockDialog = true,
                selectedVariantId = variantId,
                stockActionError = null
            )
        }
    }

    fun onHideStockDialog() {
        _uiState.update {
            it.copy(
                showReceiveStockDialog = false,
                showAdjustStockDialog = false,
                selectedVariantId = null,
                stockActionError = null
            )
        }
    }

    fun onRetry() {
        val productId = _uiState.value.product?.id
        if (productId != null) loadProduct(productId)
    }

    // === Image Operations ===

    /**
     * Maximum allowed image file size: 10 MB.
     */
    private val MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024L

    /**
     * Allowed image MIME types for upload.
     */
    private val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")

    fun onUploadImage(productId: String, uri: Uri, context: android.content.Context) {
        // Prevent duplicate uploads
        if (_uiState.value.isImageUploading) {
            _uiState.update { it.copy(imageUploadError = "Upload already in progress") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImageUploading = true, imageUploadError = null) }
            try {
                val contentResolver = context.contentResolver

                // Detect MIME type from ContentResolver
                val mimeType = contentResolver.getType(uri)
                if (mimeType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(mimeType.lowercase())) {
                    _uiState.update {
                        it.copy(
                            isImageUploading = false,
                            imageUploadError = "Unsupported image format. Please use JPEG, PNG, WebP, or GIF."
                        )
                    }
                    return@launch
                }

                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _uiState.update { it.copy(isImageUploading = false, imageUploadError = "Cannot read selected file") }
                    return@launch
                }
                val bytes = inputStream.readBytes()
                inputStream.close()

                // Validate file size
                if (bytes.size > MAX_IMAGE_FILE_SIZE) {
                    _uiState.update {
                        it.copy(
                            isImageUploading = false,
                            imageUploadError = "Image too large. Maximum size is 10 MB."
                        )
                    }
                    return@launch
                }

                // Extract filename from URI, falling back to ContentResolver metadata
                val fileName = extractFileName(context, uri)

                // Create RequestBody with the correct MIME type
                val requestBody = RequestBody.create(mimeType.toMediaType(), bytes)
                val multipartPart = MultipartBody.Part.createFormData("file", fileName, requestBody)

                when (val result = catalogRepository.uploadProductImage(productId, multipartPart)) {
                    is com.nearai.admin.core.common.Result.Success -> {
                        _uiState.update { it.copy(isImageUploading = false, imageUploadError = null) }
                        _event.value = ProductDetailsEvent.ImagesUpdated
                        refreshImages(productId)
                    }
                    is com.nearai.admin.core.common.Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isImageUploading = false,
                                imageUploadError = result.exception.message ?: "Upload failed"
                            )
                        }
                    }
                    is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImageUploading = false, imageUploadError = e.message ?: "Upload failed")
                }
            }
        }
    }

    /**
     * Extracts a meaningful filename from a [Uri].
     * Tries: URI path segment → ContentResolver DISPLAY_NAME → fallback with extension.
     */
    private fun extractFileName(context: android.content.Context, uri: Uri): String {
        // Try last path segment first (works for content:// URIs from file pickers)
        val pathSegment = uri.lastPathSegment
        if (pathSegment != null && pathSegment.contains('.')) {
            return pathSegment
        }

        // Try ContentResolver DISPLAY_NAME column
        uri.scheme?.let {
            if (it == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        val displayName = cursor.getString(nameIndex)
                        if (displayName != null && displayName.contains('.')) {
                            return displayName
                        }
                    }
                }
            }
        }

        // Fallback: generate a name with a safe extension
        val extension = pathSegment?.substringAfterLast('.', "")?.takeIf { it.length <= 10 } ?: "jpg"
        return "image_${System.currentTimeMillis()}.${extension}"
    }

    fun onDeleteImage(productId: String, imageId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageUploading = true, imageUploadError = null) }
            when (val result = catalogRepository.deleteProductImage(productId, imageId)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isImageUploading = false,
                            showDeleteImageConfirm = false,
                            selectedImageId = null,
                            images = it.images.filter { img -> img.id != imageId }
                        )
                    }
                    _event.value = ProductDetailsEvent.ImagesUpdated
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isImageUploading = false,
                            imageUploadError = result.exception.message ?: "Delete failed"
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun onSetPrimaryImage(productId: String, imageId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImageUploading = true, imageUploadError = null) }
            when (val result = catalogRepository.setPrimaryImage(productId, imageId)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isImageUploading = false,
                            images = it.images.map { img ->
                                if (img.id == imageId) img.copy(isPrimary = true)
                                else img.copy(isPrimary = false)
                            }
                        )
                    }
                    _event.value = ProductDetailsEvent.ImagesUpdated
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isImageUploading = false,
                            imageUploadError = result.exception.message ?: "Set primary failed"
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    private fun refreshImages(productId: String) {
        viewModelScope.launch {
            catalogRepository.listProductImages(productId).let { result ->
                if (result is com.nearai.admin.core.common.Result.Success) {
                    _uiState.update { it.copy(images = result.data) }
                }
            }
        }
    }

    fun onShowDeleteImageConfirm(imageId: String) {
        _uiState.update {
            it.copy(showDeleteImageConfirm = true, selectedImageId = imageId)
        }
    }

    fun onHideDeleteImageConfirm() {
        _uiState.update {
            it.copy(showDeleteImageConfirm = false, selectedImageId = null)
        }
    }

    fun onClearImageUploadError() {
        _uiState.update { it.copy(imageUploadError = null) }
    }
}

/**
 * Product details screen with product info, images, variants, and stock actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // File picker launcher for image upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onUploadImage(productId, it, context)
        }
    }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    // Show error banner for image upload
    uiState.imageUploadError?.let { error ->
        LaunchedEffect(error) {
            // Auto-clear after 5 seconds
            kotlinx.coroutines.delay(5000)
            viewModel.onClearImageUploadError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.product?.name ?: "Product Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Indigo)
                }
            }
            uiState.error != null && uiState.product == null -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error loading product",
                        style = MaterialTheme.typography.bodyLarge.copy(color = RoseError)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product info card
                    uiState.product?.let { product ->
                        item {
                            ProductInfoCard(product = product, images = uiState.images)
                        }
                    }

                    // Images section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeaderWithIcon(
                                title = "Images (${uiState.images.size})",
                                icon = Icons.Default.Image,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = !uiState.isImageUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (uiState.isImageUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Image", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.images.isEmpty()) {
                            EmptyImagesState()
                        } else {
                            ImageCarousel(
                                images = uiState.images,
                                onSetPrimary = { viewModel.onSetPrimaryImage(productId, it) },
                                onDelete = { viewModel.onShowDeleteImageConfirm(it) }
                            )
                        }
                    }

                    // Image upload error banner
                    uiState.imageUploadError?.let { error ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RoseError),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.onClearImageUploadError() }) {
                                        Text("Dismiss", color = RoseError, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    // Variants section
                    item {
                        SectionHeaderWithIcon(
                            title = "Variants (${uiState.variants.size})",
                            icon = Icons.Default.Tune
                        )
                    }

                    if (uiState.variants.isEmpty()) {
                        item {
                            EmptyVariantsState()
                        }
                    } else {
                        items(uiState.variants) { variant ->
                            VariantCard(
                                variant = variant,
                                onReceiveStock = { viewModel.onShowReceiveStockDialog(variant.id) },
                                onAdjustStock = { viewModel.onShowAdjustStockDialog(variant.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Stock dialogs
    if (uiState.showReceiveStockDialog) {
        ReceiveStockDialog(
            onDismiss = viewModel::onHideStockDialog,
            onConfirm = { quantity, reason ->
                uiState.selectedVariantId?.let { variantId ->
                    viewModel.onReceiveStock(variantId, quantity, reason)
                }
            },
            isLoading = uiState.isStockActionLoading,
            error = uiState.stockActionError
        )
    }

    if (uiState.showAdjustStockDialog) {
        AdjustStockDialog(
            onDismiss = viewModel::onHideStockDialog,
            onConfirm = { delta, reason ->
                uiState.selectedVariantId?.let { variantId ->
                    viewModel.onAdjustStock(variantId, delta, reason)
                }
            },
            isLoading = uiState.isStockActionLoading,
            error = uiState.stockActionError
        )
    }

    // Delete image confirmation dialog
    if (uiState.showDeleteImageConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onHideDeleteImageConfirm,
            title = { Text("Delete Image", fontWeight = FontWeight.Bold, color = Navy) },
            text = {
                Text(
                    "Are you sure you want to delete this image? This action cannot be undone.",
                    color = TextSecondaryLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        uiState.selectedImageId?.let { imageId ->
                            viewModel.onDeleteImage(productId, imageId)
                        }
                    },
                    enabled = !uiState.isImageUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    if (uiState.isImageUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Delete", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onHideDeleteImageConfirm) {
                    Text("Cancel", color = TextSecondaryLight)
                }
            }
        )
    }
}

/**
 * Product info card with name, description, price, status, and category.
 */
@Composable
fun ProductInfoCard(product: Product, images: List<ProductImage> = emptyList(), modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Primary image hero
            val primaryImage = images.find { it.isPrimary } ?: images.firstOrNull()
            if (primaryImage != null && primaryImage.url.isNotBlank()) {
                AsyncImage(
                    model = primaryImage.url,
                    contentDescription = "${product.name} primary image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondaryLight
                        ),
                        maxLines = 3
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                StatusPill(
                    label = when (product.status) {
                        ProductStatus.ACTIVE -> "Active"
                        ProductStatus.INACTIVE -> "Inactive"
                        ProductStatus.DRAFT -> "Draft"
                    },
                    dotColor = when (product.status) {
                        ProductStatus.ACTIVE -> EmeraldSuccess
                        ProductStatus.INACTIVE -> TextSecondaryLight
                        ProductStatus.DRAFT -> AmberWarning
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${product.priceCurrency} ${"%.2f".format(product.priceAmount)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created: ${product.createdAt.take(10)}",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight)
            )
        }
    }
}

/**
 * Horizontal scrollable image carousel with management actions.
 */
@Composable
fun ImageCarousel(
    images: List<ProductImage>,
    onSetPrimary: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            images.forEach { image ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            if (image.url.isNotBlank()) {
                                AsyncImage(
                                    model = image.url,
                                    contentDescription = image.fileName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = image.fileName,
                                    tint = TextSecondaryLight,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = image.fileName,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryLight),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (image.isPrimary) {
                            Spacer(modifier = Modifier.height(2.dp))
                            StatusPill(label = stringResource(R.string.product_image_primary), dotColor = Indigo)
                        }
                    }
                }
            }
        }
        // Action buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            images.forEach { image ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Transparent),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!image.isPrimary) {
                            TextButton(
                                onClick = { onSetPrimary(image.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Set as primary",
                                    modifier = Modifier.size(14.dp),
                                    tint = Indigo
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "Primary",
                                    color = Indigo,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        TextButton(
                            onClick = { onDelete(image.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete image",
                                modifier = Modifier.size(14.dp),
                                tint = RoseError
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "Delete",
                                color = RoseError,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty images state shown when no images are uploaded yet.
 */
@Composable
fun EmptyImagesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Image,
            contentDescription = null,
            tint = TextSecondaryLight,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No images yet. Tap \"Add Image\" to upload.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
        )
    }
}

/**
 * Variant card with SKU, attributes, and stock actions.
 */
@Composable
fun VariantCard(
    variant: ProductVariant,
    onReceiveStock: () -> Unit,
    onAdjustStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = variant.sku,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                )
                StatusPill(
                    label = if (variant.status == VariantStatus.ACTIVE) "Active" else "Inactive",
                    dotColor = if (variant.status == VariantStatus.ACTIVE) EmeraldSuccess else TextSecondaryLight
                )
            }
            if (variant.attributes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    variant.attributes.forEach { (key, value) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BackgroundLight
                        ) {
                            Text(
                                text = "$key: $value",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryLight,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReceiveStock,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receive", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onAdjustStock,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjust", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Empty variants state.
 */
@Composable
fun EmptyVariantsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Tune,
            contentDescription = null,
            tint = TextSecondaryLight,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No variants yet",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
        )
    }
}

/**
 * Section header with icon.
 */
@Composable
fun SectionHeaderWithIcon(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Indigo,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        )
    }
}

/**
 * Dialog for receiving stock.
 */
@Composable
fun ReceiveStockDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receive Stock", fontWeight = FontWeight.Bold, color = Navy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reason (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                if (error != null) {
                    Text(text = error, color = RoseError, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    onConfirm(qty, reason)
                },
                enabled = !isLoading && quantity.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                else Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryLight) }
        }
    )
}

/**
 * Dialog for adjusting stock.
 */
@Composable
fun AdjustStockDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var delta by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock", fontWeight = FontWeight.Bold, color = Navy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = delta,
                    onValueChange = { delta = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Delta (positive or negative)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reason *") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                if (error != null) {
                    Text(text = error, color = RoseError, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = delta.toIntOrNull() ?: 0
                    onConfirm(d, reason)
                },
                enabled = !isLoading && delta.isNotBlank() && reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                else Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryLight) }
        }
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProductDetailsScreenPreview() {
    NearAiAdminTheme {
        ProductInfoCard(
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
            )
        )
    }
}
