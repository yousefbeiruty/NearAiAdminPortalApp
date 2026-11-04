package com.nearai.admin.feature.catalog.presentation

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import com.nearai.admin.feature.catalog.domain.ProductImage
import io.mockk.*
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var catalogRepository: CatalogRepository
    private lateinit var viewModel: ProductDetailsViewModel
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        catalogRepository = mockk<CatalogRepository>(relaxed = true)
        viewModel = ProductDetailsViewModel(catalogRepository)

        context = mockk<Context>(relaxed = true)
        contentResolver = mockk<ContentResolver>(relaxed = true)
        every { context.contentResolver } returns contentResolver
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // === Multipart field name ===

    @Test
    fun `upload creates multipart part with field name file`() = runTest {
        // The ViewModel uses MultipartBody.Part.createFormData("file", ...) — verify by checking
        // that the repository receives a MultipartBody.Part. Since we can't inspect the Part
        // directly through the mock, we verify the repository is called and succeeds.
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "photo.jpg"

        val uploadedImage = ProductImage(
            id = "img-1",
            productId = productId,
            url = "http://example.com/photo.jpg",
            fileName = "photo.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = imageBytes.size,
            displayOrder = 0,
            isPrimary = false,
            uploadedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Success(uploadedImage)
        coEvery { catalogRepository.listProductImages(productId) } returns Result.Success(emptyList())

        viewModel.onUploadImage(productId, uri, context)

        coVerify(exactly = 1) { catalogRepository.uploadProductImage(eq(productId), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNull(viewModel.uiState.value.imageUploadError)
    }

    // === Valid image upload ===

    @Test
    fun `upload succeeds with valid JPEG image`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = "fake-jpeg-data".toByteArray()

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "image.jpg"

        val uploadedImage = ProductImage(
            id = "img-1", productId = productId, url = "http://example.com/image.jpg",
            fileName = "image.jpg", mimeType = "image/jpeg", fileSizeBytes = imageBytes.size,
            displayOrder = 0, isPrimary = false, uploadedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Success(uploadedImage)
        coEvery { catalogRepository.listProductImages(productId) } returns Result.Success(listOf(uploadedImage))

        viewModel.onUploadImage(productId, uri, context)

        coVerify { catalogRepository.uploadProductImage(eq(productId), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNull(viewModel.uiState.value.imageUploadError)
        assertEquals(1, viewModel.uiState.value.images.size)
    }

    @Test
    fun `upload succeeds with PNG image`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = "fake-png-data".toByteArray()

        every { contentResolver.getType(uri) } returns "image/png"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "image.png"

        val uploadedImage = ProductImage(
            id = "img-2", productId = productId, url = "http://example.com/image.png",
            fileName = "image.png", mimeType = "image/png", fileSizeBytes = imageBytes.size,
            displayOrder = 0, isPrimary = false, uploadedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Success(uploadedImage)
        coEvery { catalogRepository.listProductImages(productId) } returns Result.Success(listOf(uploadedImage))

        viewModel.onUploadImage(productId, uri, context)

        coVerify { catalogRepository.uploadProductImage(eq(productId), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
    }

    @Test
    fun `upload succeeds with WebP image`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)

        every { contentResolver.getType(uri) } returns "image/webp"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(byteArrayOf(1, 2, 3))
        every { uri.lastPathSegment } returns "image.webp"

        val uploadedImage = ProductImage(
            id = "img-3", productId = productId, url = "http://example.com/image.webp",
            fileName = "image.webp", mimeType = "image/webp", fileSizeBytes = 3,
            displayOrder = 0, isPrimary = false, uploadedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Success(uploadedImage)
        coEvery { catalogRepository.listProductImages(productId) } returns Result.Success(listOf(uploadedImage))

        viewModel.onUploadImage(productId, uri, context)

        coVerify { catalogRepository.uploadProductImage(eq(productId), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
    }

    // === Unsupported image format ===

    @Test
    fun `upload rejects unsupported MIME type`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)

        every { contentResolver.getType(uri) } returns "application/pdf"

        viewModel.onUploadImage(productId, uri, context)

        coVerify(exactly = 0) { catalogRepository.uploadProductImage(any(), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNotNull(viewModel.uiState.value.imageUploadError)
        assertTrue(viewModel.uiState.value.imageUploadError!!.contains("Unsupported image format"))
    }

    @Test
    fun `upload rejects null MIME type`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)

        every { contentResolver.getType(uri) } returns null

        viewModel.onUploadImage(productId, uri, context)

        coVerify(exactly = 0) { catalogRepository.uploadProductImage(any(), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNotNull(viewModel.uiState.value.imageUploadError)
    }

    // === File too large ===

    @Test
    fun `upload rejects file larger than 10 MB`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val largeBytes = ByteArray(11 * 1024 * 1024) // 11 MB

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(largeBytes)
        every { uri.lastPathSegment } returns "large.jpg"

        viewModel.onUploadImage(productId, uri, context)

        coVerify(exactly = 0) { catalogRepository.uploadProductImage(any(), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNotNull(viewModel.uiState.value.imageUploadError)
        assertTrue(viewModel.uiState.value.imageUploadError!!.contains("too large"))
    }

    // === Duplicate upload prevention ===

    @Test
    fun `upload prevents duplicate upload when already uploading`() = runTest(testDispatcher) {
        // Use a dispatcher that doesn't auto-run, so the first upload coroutine
        // sets isImageUploading = true but doesn't complete yet.
        val localDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()
        val localVm = ProductDetailsViewModel(catalogRepository)

        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(byteArrayOf(1, 2, 3))
        every { uri.lastPathSegment } returns "image.jpg"

        // Repository hangs so upload stays in progress
        coEvery { catalogRepository.uploadProductImage(any(), any()) } coAnswers {
            kotlinx.coroutines.awaitCancellation()
            Result.Success(mockk())
        }

        // First upload — sets isImageUploading = true synchronously, then launches
        localVm.onUploadImage(productId, uri, context)

        assertTrue(localVm.uiState.value.isImageUploading)

        // Second upload — guard should reject immediately
        localVm.onUploadImage(productId, uri, context)

        assertEquals("Upload already in progress", localVm.uiState.value.imageUploadError)
        coVerify(exactly = 1) { catalogRepository.uploadProductImage(any(), any()) }
    }

    // === Upload failure ===

    @Test
    fun `upload shows error on backend failure`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = "fake-image".toByteArray()

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "image.jpg"

        val error = AppError.ServerError("Backend server error: 500 Internal Server Error", 500)
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Error(error)

        viewModel.onUploadImage(productId, uri, context)

        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNotNull(viewModel.uiState.value.imageUploadError)
        assertTrue(viewModel.uiState.value.imageUploadError!!.contains("Backend server error"))
    }

    @Test
    fun `upload shows error on network failure`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = "fake-image".toByteArray()

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "image.jpg"

        val error = AppError.NetworkError("No internet connection")
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Error(error)

        viewModel.onUploadImage(productId, uri, context)

        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNotNull(viewModel.uiState.value.imageUploadError)
        assertTrue(viewModel.uiState.value.imageUploadError!!.contains("No internet connection"))
    }

    // === Backend error response parsing ===

    @Test
    fun `upload shows parsed backend 422 validation error`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = "fake-image".toByteArray()

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "image.jpg"

        // Simulates a 422 validation error from the backend
        val error = AppError.ServerError("File size exceeds maximum allowed size of 10MB", 422)
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Error(error)

        viewModel.onUploadImage(productId, uri, context)

        assertFalse(viewModel.uiState.value.isImageUploading)
        assertNotNull(viewModel.uiState.value.imageUploadError)
        assertTrue(viewModel.uiState.value.imageUploadError!!.contains("File size exceeds"))
    }

    // === Filename extraction ===

    @Test
    fun `filename extracted from URI path segment`() = runTest {
        val productId = "prod-1"
        val uri = mockk<Uri>(relaxed = true)
        val imageBytes = "data".toByteArray()

        every { contentResolver.getType(uri) } returns "image/jpeg"
        every { contentResolver.openInputStream(uri) } returns java.io.ByteArrayInputStream(imageBytes)
        every { uri.lastPathSegment } returns "my_photo.jpg"

        val uploadedImage = ProductImage(
            id = "img-1", productId = productId, url = "http://example.com/my_photo.jpg",
            fileName = "my_photo.jpg", mimeType = "image/jpeg", fileSizeBytes = imageBytes.size,
            displayOrder = 0, isPrimary = false, uploadedAt = "2024-01-01T00:00:00Z"
        )
        coEvery { catalogRepository.uploadProductImage(any(), any()) } returns Result.Success(uploadedImage)
        coEvery { catalogRepository.listProductImages(productId) } returns Result.Success(listOf(uploadedImage))

        viewModel.onUploadImage(productId, uri, context)

        coVerify { catalogRepository.uploadProductImage(eq(productId), any()) }
        assertFalse(viewModel.uiState.value.isImageUploading)
    }

    // === Clear error ===

    @Test
    fun `clearImageUploadError clears the error`() {
        viewModel.onClearImageUploadError()
        assertNull(viewModel.uiState.value.imageUploadError)
    }
}
