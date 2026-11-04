package com.nearai.admin.feature.catalog.data

import com.nearai.admin.feature.catalog.domain.ProductImage
import org.junit.Assert.*
import org.junit.Test

class CatalogMapperTest {

    // === Image DTO mapping ===

    @Test
    fun `mapProductImage preserves all fields`() {
        val dto = ProductImageResponseDto(
            id = "img-123",
            productId = "prod-456",
            url = "http://10.0.2.2:8081/uploads/2024/01/photo.jpg",
            fileName = "photo.jpg",
            mimeType = "image/jpeg",
            fileSizeBytes = 204800,
            displayOrder = 0,
            isPrimary = true,
            uploadedAt = "2024-01-15T10:30:00Z"
        )

        val result = CatalogMapper.mapProductImage(dto)

        assertEquals("img-123", result.id)
        assertEquals("prod-456", result.productId)
        assertEquals("http://10.0.2.2:8081/uploads/2024/01/photo.jpg", result.url)
        assertEquals("photo.jpg", result.fileName)
        assertEquals("image/jpeg", result.mimeType)
        assertEquals(204800, result.fileSizeBytes)
        assertEquals(0, result.displayOrder)
        assertTrue(result.isPrimary)
        assertEquals("2024-01-15T10:30:00Z", result.uploadedAt)
    }

    @Test
    fun `mapProductImage preserves URL for image rendering`() {
        val dto = ProductImageResponseDto(
            id = "img-1",
            productId = "prod-1",
            url = "https://api.nearai.dev/uploads/images/abc123.png",
            fileName = "abc123.png",
            mimeType = "image/png",
            fileSizeBytes = 51200,
            displayOrder = 1,
            isPrimary = false,
            uploadedAt = "2024-02-01T00:00:00Z"
        )

        val result = CatalogMapper.mapProductImage(dto)

        // URL must be non-empty so Coil AsyncImage can load it
        assertNotNull(result.url)
        assertTrue(result.url.startsWith("http"))
        assertEquals("https://api.nearai.dev/uploads/images/abc123.png", result.url)
    }

    @Test
    fun `mapProductImage handles non-primary image`() {
        val dto = ProductImageResponseDto(
            id = "img-2",
            productId = "prod-1",
            url = "http://10.0.2.2:8081/uploads/image.webp",
            fileName = "image.webp",
            mimeType = "image/webp",
            fileSizeBytes = 10240,
            displayOrder = 2,
            isPrimary = false,
            uploadedAt = "2024-03-01T00:00:00Z"
        )

        val result = CatalogMapper.mapProductImage(dto)

        assertFalse(result.isPrimary)
        assertEquals(2, result.displayOrder)
        assertEquals("image/webp", result.mimeType)
    }

    @Test
    fun `mapProductImage handles GIF image`() {
        val dto = ProductImageResponseDto(
            id = "img-3",
            productId = "prod-2",
            url = "http://10.0.2.2:8081/uploads/animated.gif",
            fileName = "animated.gif",
            mimeType = "image/gif",
            fileSizeBytes = 999999,
            displayOrder = 0,
            isPrimary = false,
            uploadedAt = "2024-04-01T00:00:00Z"
        )

        val result = CatalogMapper.mapProductImage(dto)

        assertEquals("image/gif", result.mimeType)
        assertEquals(999999, result.fileSizeBytes)
        assertEquals("animated.gif", result.fileName)
    }

    @Test
    fun `mapProductImage maps multiple images correctly`() {
        val dtos = listOf(
            ProductImageResponseDto(
                id = "img-1", productId = "prod-1",
                url = "http://10.0.2.2:8081/uploads/1.jpg",
                fileName = "1.jpg", mimeType = "image/jpeg",
                fileSizeBytes = 100, displayOrder = 0,
                isPrimary = true, uploadedAt = "2024-01-01T00:00:00Z"
            ),
            ProductImageResponseDto(
                id = "img-2", productId = "prod-1",
                url = "http://10.0.2.2:8081/uploads/2.jpg",
                fileName = "2.jpg", mimeType = "image/jpeg",
                fileSizeBytes = 200, displayOrder = 1,
                isPrimary = false, uploadedAt = "2024-01-02T00:00:00Z"
            ),
            ProductImageResponseDto(
                id = "img-3", productId = "prod-1",
                url = "http://10.0.2.2:8081/uploads/3.jpg",
                fileName = "3.jpg", mimeType = "image/jpeg",
                fileSizeBytes = 300, displayOrder = 2,
                isPrimary = false, uploadedAt = "2024-01-03T00:00:00Z"
            )
        )

        val results = dtos.map { CatalogMapper.mapProductImage(it) }

        assertEquals(3, results.size)
        assertTrue(results[0].isPrimary)
        assertFalse(results[1].isPrimary)
        assertFalse(results[2].isPrimary)
        assertEquals(0, results[0].displayOrder)
        assertEquals(1, results[1].displayOrder)
        assertEquals(2, results[2].displayOrder)
    }

    // === Product DTO mapping ===

    @Test
    fun `mapProduct preserves images list`() {
        val dto = ProductResponseDto(
            id = "prod-1",
            name = "Test Product",
            description = "A test product",
            slug = "test-product",
            priceAmount = 99.99,
            priceCurrency = "USD",
            images = listOf("http://10.0.2.2:8081/uploads/img1.jpg", "http://10.0.2.2:8081/uploads/img2.jpg"),
            status = "active",
            categoryId = "cat-1",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        val result = CatalogMapper.mapProduct(dto)

        assertEquals(2, result.images.size)
        assertEquals("http://10.0.2.2:8081/uploads/img1.jpg", result.images[0])
        assertEquals("http://10.0.2.2:8081/uploads/img2.jpg", result.images[1])
    }

    @Test
    fun `mapProduct handles empty images list`() {
        val dto = ProductResponseDto(
            id = "prod-2",
            name = "No Image Product",
            description = "No images",
            slug = "no-image",
            priceAmount = 10.0,
            priceCurrency = "USD",
            images = emptyList(),
            status = "active",
            categoryId = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        val result = CatalogMapper.mapProduct(dto)

        assertTrue(result.images.isEmpty())
    }
}
