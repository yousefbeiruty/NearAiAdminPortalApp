package com.nearai.admin.feature.catalog.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.nearai.admin.core.designsystem.components.NearAiAlertType
import com.nearai.admin.core.designsystem.components.NearAiAlertDialog
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
    val selectedImageId: String? = null,
    // Variant management
    val showAddVariantDialog: Boolean = false,
    val showEditVariantDialog: Boolean = false,
    val editingVariant: ProductVariant? = null,
    val showVariantStatusDialog: Boolean = false,
    val variantActionError: String? = null,
    val isVariantActionLoading: Boolean = false
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
                            val images = imageResult.data
                            // If the dedicated images endpoint returns empty, fall back to
                            // product.images URLs from the product detail response so that
                            // the hero image and carousel still display.
                            val finalImages = if (images.isNotEmpty()) {
                                images
                            } else {
                                result.data.images.filter { it.isNotBlank() && it.startsWith("http", ignoreCase = true) }
                                    .map { url ->
                                        ProductImage(
                                            id = "",
                                            productId = productId,
                                            url = url,
                                            fileName = url.substringAfterLast('/'),
                                            mimeType = "image/jpeg",
                                            fileSizeBytes = 0,
                                            displayOrder = 0,
                                            isPrimary = false,
                                            uploadedAt = ""
                                        )
                                    }
                            }
                            _uiState.update { it.copy(images = finalImages) }
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

    // === Variant Management ===

    fun onShowAddVariantDialog() {
        _uiState.update {
            it.copy(
                showAddVariantDialog = true,
                editingVariant = null,
                variantActionError = null
            )
        }
    }

    fun onShowEditVariantDialog(variant: ProductVariant) {
        _uiState.update {
            it.copy(
                showEditVariantDialog = true,
                editingVariant = variant,
                variantActionError = null
            )
        }
    }

    fun onHideVariantDialog() {
        _uiState.update {
            it.copy(
                showAddVariantDialog = false,
                showEditVariantDialog = false,
                editingVariant = null,
                variantActionError = null
            )
        }
    }

    fun onCreateVariant(sku: String, attributes: Map<String, String>) {
        if (sku.isBlank()) {
            _uiState.update { it.copy(variantActionError = "SKU is required") }
            return
        }
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isVariantActionLoading = true, variantActionError = null) }
            when (val result = catalogRepository.createVariant(productId, sku, attributes)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update { it.copy(isVariantActionLoading = false, showAddVariantDialog = false) }
                    refreshVariants()
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isVariantActionLoading = false,
                            variantActionError = result.exception.message ?: "Failed to create variant"
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun onUpdateVariant(variantId: String, sku: String, attributes: Map<String, String>) {
        if (sku.isBlank()) {
            _uiState.update { it.copy(variantActionError = "SKU is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isVariantActionLoading = true, variantActionError = null) }
            when (val result = catalogRepository.updateVariant(variantId, sku, attributes)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update { it.copy(isVariantActionLoading = false, showEditVariantDialog = false) }
                    refreshVariants()
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isVariantActionLoading = false,
                            variantActionError = result.exception.message ?: "Failed to update variant"
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
    }

    fun onShowVariantStatusDialog(variant: ProductVariant) {
        _uiState.update {
            it.copy(
                showVariantStatusDialog = true,
                editingVariant = variant,
                variantActionError = null
            )
        }
    }

    fun onHideVariantStatusDialog() {
        _uiState.update {
            it.copy(
                showVariantStatusDialog = false,
                editingVariant = null,
                variantActionError = null
            )
        }
    }

    fun onToggleVariantStatus(variantId: String, newStatus: VariantStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVariantActionLoading = true, variantActionError = null) }
            when (val result = catalogRepository.updateVariantStatus(variantId, newStatus)) {
                is com.nearai.admin.core.common.Result.Success -> {
                    _uiState.update { it.copy(isVariantActionLoading = false, showVariantStatusDialog = false) }
                    refreshVariants()
                }
                is com.nearai.admin.core.common.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isVariantActionLoading = false,
                            variantActionError = result.exception.message ?: "Failed to update status"
                        )
                    }
                }
                is com.nearai.admin.core.common.Result.Loading -> { /* no-op */ }
            }
        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeaderWithIcon(
                                title = stringResource(R.string.variant_section_title, uiState.variants.size),
                                icon = Icons.Default.Tune,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Button(
                                onClick = { viewModel.onShowAddVariantDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.variant_add), color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
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
                                onAdjustStock = { viewModel.onShowAdjustStockDialog(variant.id) },
                                onEdit = { viewModel.onShowEditVariantDialog(variant) },
                                onToggleStatus = { viewModel.onShowVariantStatusDialog(variant) }
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

    // Delete image confirmation dialog — using NearAiAlertDialog
    if (uiState.showDeleteImageConfirm) {
        NearAiAlertDialog(
            isVisible = true,
            title = stringResource(R.string.product_delete_image_title),
            message = stringResource(R.string.product_delete_image_message),
            confirmText = stringResource(R.string.product_delete_image_confirm),
            dismissText = stringResource(R.string.product_delete_image_cancel),
            type = NearAiAlertType.ErrorType,
            onConfirm = {
                uiState.selectedImageId?.let { imageId ->
                    viewModel.onDeleteImage(productId, imageId)
                }
            },
            onDismiss = viewModel::onHideDeleteImageConfirm,
            isLoading = uiState.isImageUploading
        )
    }

    // Add Variant dialog
    if (uiState.showAddVariantDialog) {
        AddVariantDialog(
            onDismiss = viewModel::onHideVariantDialog,
            onConfirm = { sku, attributes ->
                viewModel.onCreateVariant(sku, attributes)
            },
            isLoading = uiState.isVariantActionLoading,
            error = uiState.variantActionError
        )
    }

    // Edit Variant dialog
    if (uiState.showEditVariantDialog && uiState.editingVariant != null) {
        val editingV = uiState.editingVariant!!
        AddVariantDialog(
            onDismiss = viewModel::onHideVariantDialog,
            onConfirm = { sku, attrs ->
                viewModel.onUpdateVariant(editingV.id, sku, attrs)
            },
            isLoading = uiState.isVariantActionLoading,
            error = uiState.variantActionError,
            initialSku = editingV.sku,
            initialAttributes = editingV.attributes,
            title = stringResource(R.string.variant_edit_title)
        )
    }

    // Variant status toggle dialog
    if (uiState.showVariantStatusDialog && uiState.editingVariant != null) {
        val variant = uiState.editingVariant!!
        val isDeactivating = variant.status == VariantStatus.ACTIVE
        NearAiAlertDialog(
            isVisible = true,
            title = if (isDeactivating) stringResource(R.string.variant_deactivate_title) else stringResource(R.string.variant_activate_title),
            message = if (isDeactivating) {
                stringResource(R.string.variant_deactivate_message, variant.sku)
            } else {
                stringResource(R.string.variant_activate_message, variant.sku)
            },
            confirmText = if (isDeactivating) stringResource(R.string.variant_deactivate_confirm) else stringResource(R.string.variant_activate_confirm),
            dismissText = stringResource(R.string.variant_cancel),
            type = if (isDeactivating) NearAiAlertType.WarningType else NearAiAlertType.InfoType,
            onConfirm = {
                val newStatus = if (isDeactivating) VariantStatus.INACTIVE else VariantStatus.ACTIVE
                viewModel.onToggleVariantStatus(variant.id, newStatus)
            },
            onDismiss = viewModel::onHideVariantStatusDialog,
            isLoading = uiState.isVariantActionLoading
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
                                .height(80.dp),
                            contentAlignment = Alignment.Center
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
                                    modifier = Modifier.size(32.dp)
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
 * Variant card with SKU, attributes, stock actions, edit and status toggle.
 */
@Composable
fun VariantCard(
    variant: ProductVariant,
    onReceiveStock: () -> Unit,
    onAdjustStock: () -> Unit,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    label = if (variant.status == VariantStatus.ACTIVE) stringResource(R.string.variant_status_active) else stringResource(R.string.variant_status_inactive),
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (variant.availableQuantity > 0) {
                    stringResource(R.string.variant_stock_available, variant.availableQuantity)
                } else {
                    stringResource(R.string.variant_out_of_stock)
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (variant.availableQuantity > 0) EmeraldSuccess else RoseError,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Row 1: Stock actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onReceiveStock,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.inventory_receive_stock), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onAdjustStock,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.inventory_adjust_stock), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Row 2: Variant actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Indigo)
                ) {
                    Text(stringResource(R.string.variant_edit), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (variant.status == VariantStatus.ACTIVE) RoseError else EmeraldSuccess
                    )
                ) {
                    Text(
                        text = if (variant.status == VariantStatus.ACTIVE) stringResource(R.string.variant_deactivate) else stringResource(R.string.variant_activate),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
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
            text = stringResource(R.string.variant_no_variants_yet),
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryLight)
        )
    }
}

/**
 * Add/Edit Variant dialog styled to match the NearAiAlertDialog architecture.
 * Uses the same visual design: 24dp rounded corners, 16dp shadow, custom icon,
 * consistent typography and button styling as NearAiAlertDialog.
 */
@Composable
fun AddVariantDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Map<String, String>) -> Unit,
    isLoading: Boolean,
    error: String?,
    initialSku: String = "",
    initialAttributes: Map<String, String> = emptyMap(),
    title: String = stringResource(R.string.variant_add_title),
    modifier: Modifier = Modifier
) {
    var sku by remember { mutableStateOf(initialSku) }
    val attributes = remember {
        mutableStateListOf<Pair<String, String>?>().apply {
            if (initialAttributes.isNotEmpty()) {
                clear()
                addAll(initialAttributes.map { (k, v) -> k to v })
            } else {
                add(null)
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Dialog card with shadow — matching NearAiAlertDialog style
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .background(SurfaceLight, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon — Tune icon matching NearAiAlertDialog 56dp style
                Canvas(modifier = Modifier.size(56.dp)) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    // Background circle
                    drawCircle(
                        color = PrimaryContainerLight,
                        radius = radius,
                        center = Offset(radius, radius)
                    )
                    // Tune icon (slider lines)
                    val iconSize = diameter * 0.35f
                    val lineSpacing = iconSize * 0.35f
                    val startY = radius - iconSize * 0.5f
                    repeat(3) { i ->
                        val y = startY + i * lineSpacing
                        drawLine(
                            color = Indigo,
                            start = Offset(radius - iconSize * 0.5f, y),
                            end = Offset(radius + iconSize * 0.5f, y),
                            strokeWidth = 3.5f,
                            cap = StrokeCap.Round
                        )
                        // Small knob on each line
                        drawCircle(
                            color = Indigo,
                            radius = 3f,
                            center = Offset(radius - iconSize * 0.15f + i * iconSize * 0.2f, y)
                        )
                    }
                }

                // Title — matching NearAiAlertDialog typography
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 26.sp
                )

                // Scrollable form content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SKU field
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it.trim() },
                        label = { Text(stringResource(R.string.variant_sku_label)) },
                        placeholder = { Text(stringResource(R.string.variant_sku_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight)
                    )

                    // Attributes section header
                    Text(
                        text = stringResource(R.string.variant_attributes_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryLight,
                        letterSpacing = (0.5).sp
                    )

                    // Attribute rows in scrollable column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(attributes.size) { index ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = attributes[index]?.first ?: "",
                                    onValueChange = { key ->
                                        val pair = attributes[index]
                                        attributes[index] = if (pair != null) {
                                            Pair(key, pair.second)
                                        } else {
                                            Pair(key, "")
                                        }
                                    },
                                    label = { Text(stringResource(R.string.variant_attribute_key_label)) },
                                    placeholder = { Text(stringResource(R.string.variant_attribute_key_hint)) },
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                OutlinedTextField(
                                    value = attributes[index]?.second ?: "",
                                    onValueChange = { value ->
                                        val pair = attributes[index]
                                        attributes[index] = if (pair != null) {
                                            Pair(pair.first, value)
                                        } else {
                                            Pair("", value)
                                        }
                                    },
                                    label = { Text(stringResource(R.string.variant_attribute_value_label)) },
                                    placeholder = { Text(stringResource(R.string.variant_attribute_value_hint)) },
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                IconButton(
                                    onClick = {
                                        if (attributes.size > 1) {
                                            attributes.removeAt(index)
                                        }
                                    },
                                    enabled = attributes.size > 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.variant_remove_attribute),
                                        tint = if (attributes.size > 1) RoseError else TextSecondaryLight.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        // Add attribute button
                        TextButton(
                            onClick = { attributes.add(null) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.variant_add_attribute),
                                color = Indigo,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Error message
                error?.let { err ->
                    Text(
                        text = err,
                        fontSize = 12.sp,
                        color = RoseError,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Action buttons — matching NearAiAlertDialog style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondaryLight
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = androidx.compose.ui.unit.Dp.Hairline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.variant_cancel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = {
                            val attrMap = attributes
                                .filter { it != null && it.first.isNotBlank() && it.second.isNotBlank() }
                                .mapNotNull { it }
                                .associate { it.first.trim() to it.second.trim() }
                            onConfirm(sku.trim(), attrMap)
                        },
                        enabled = !isLoading && sku.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Indigo,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                            focusedElevation = 4.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.variant_save),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
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
 * Dialog for receiving stock, styled to match the NearAiAlertDialog architecture.
 * Uses the same visual design: 24dp rounded corners, 16dp shadow, custom icon,
 * consistent typography and button styling as NearAiAlertDialog.
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

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .background(SurfaceLight, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon — Package icon matching NearAiAlertDialog 56dp style
                Canvas(modifier = Modifier.size(56.dp)) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    drawCircle(
                        color = PrimaryContainerLight,
                        radius = radius,
                        center = Offset(radius, radius)
                    )
                    // Package icon
                    val box = diameter * 0.25f
                    drawLine(
                        color = Indigo,
                        start = Offset(radius - box, radius - box * 0.5f),
                        end = Offset(radius + box, radius - box * 0.5f),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Indigo,
                        start = Offset(radius - box, radius + box * 0.5f),
                        end = Offset(radius + box, radius + box * 0.5f),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Indigo,
                        start = Offset(radius - box, radius),
                        end = Offset(radius + box, radius),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                }

                Text(
                    text = stringResource(R.string.inventory_receive_stock_title),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 26.sp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(stringResource(R.string.inventory_receive_stock_quantity_label)) },
                        placeholder = { Text(stringResource(R.string.inventory_receive_stock_quantity_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight)
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(stringResource(R.string.inventory_receive_stock_reason_label)) },
                        placeholder = { Text(stringResource(R.string.inventory_receive_stock_reason_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight)
                    )
                    error?.let { err ->
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = RoseError,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryLight),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = androidx.compose.ui.unit.Dp.Hairline
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(text = stringResource(R.string.variant_cancel), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull() ?: 0
                            onConfirm(qty, reason)
                        },
                        enabled = !isLoading && quantity.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp, focusedElevation = 4.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(text = stringResource(R.string.inventory_receive_stock_confirm), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for adjusting stock, styled to match the NearAiAlertDialog architecture.
 * Uses the same visual design: 24dp rounded corners, 16dp shadow, custom icon,
 * consistent typography and button styling as NearAiAlertDialog.
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

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .background(SurfaceLight, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon — Adjust icon matching NearAiAlertDialog 56dp style
                Canvas(modifier = Modifier.size(56.dp)) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    drawCircle(
                        color = PrimaryContainerLight,
                        radius = radius,
                        center = Offset(radius, radius)
                    )
                    // Plus/minus icon
                    val half = diameter * 0.12f
                    // Plus
                    drawLine(
                        color = Indigo,
                        start = Offset(radius - half, radius - half * 0.6f),
                        end = Offset(radius + half, radius - half * 0.6f),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Indigo,
                        start = Offset(radius, radius - half * 1.2f),
                        end = Offset(radius, radius),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    // Minus
                    drawLine(
                        color = Indigo,
                        start = Offset(radius - half, radius + half * 0.8f),
                        end = Offset(radius + half, radius + half * 0.8f),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                }

                Text(
                    text = stringResource(R.string.inventory_adjust_stock_title),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 26.sp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = delta,
                        onValueChange = { delta = it },
                        label = { Text(stringResource(R.string.inventory_adjust_stock_delta_label)) },
                        placeholder = { Text(stringResource(R.string.inventory_adjust_stock_delta_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight)
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(stringResource(R.string.inventory_adjust_stock_reason_label)) },
                        placeholder = { Text(stringResource(R.string.inventory_adjust_stock_reason_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight)
                    )
                    error?.let { err ->
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = RoseError,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryLight),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = androidx.compose.ui.unit.Dp.Hairline
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(text = stringResource(R.string.variant_cancel), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val d = delta.toIntOrNull() ?: 0
                            onConfirm(d, reason)
                        },
                        enabled = !isLoading && delta.isNotBlank() && reason.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp, focusedElevation = 4.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(text = stringResource(R.string.inventory_adjust_stock_confirm), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
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
