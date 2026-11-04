package com.nearai.admin.feature.catalog.data

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Product Image DTO serialization/deserialization.
 * Verifies the Admin app correctly parses the backend image upload response
 * and product image list responses.
 */
class ProductImageDtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `ProductImageResponseDto deserializes upload response`() {
        val jsonStr = """
            {
                "id": "img-abc-123",
                "product_id": "prod-456",
                "url": "http://10.0.2.2:8081/uploads/2024/01/photo-abc123.jpg",
                "file_name": "photo-abc123.jpg",
                "mime_type": "image/jpeg",
                "file_size_bytes": 204800,
                "display_order": 0,
                "is_primary": true,
                "uploaded_at": "2024-01-15T10:30:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductImageResponseDto>(jsonStr)

        assertEquals("img-abc-123", dto.id)
        assertEquals("prod-456", dto.productId)
        assertEquals("http://10.0.2.2:8081/uploads/2024/01/photo-abc123.jpg", dto.url)
        assertEquals("photo-abc123.jpg", dto.fileName)
        assertEquals("image/jpeg", dto.mimeType)
        assertEquals(204800, dto.fileSizeBytes)
        assertEquals(0, dto.displayOrder)
        assertTrue(dto.isPrimary)
        assertEquals("2024-01-15T10:30:00Z", dto.uploadedAt)
    }

    @Test
    fun `ProductImageResponseDto deserializes PNG image`() {
        val jsonStr = """
            {
                "id": "img-png-1",
                "product_id": "prod-789",
                "url": "http://10.0.2.2:8081/uploads/image.png",
                "file_name": "image.png",
                "mime_type": "image/png",
                "file_size_bytes": 51200,
                "display_order": 1,
                "is_primary": false,
                "uploaded_at": "2024-02-01T00:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductImageResponseDto>(jsonStr)

        assertEquals("image/png", dto.mimeType)
        assertFalse(dto.isPrimary)
        assertEquals(1, dto.displayOrder)
    }

    @Test
    fun `ProductImageResponseDto deserializes WebP image`() {
        val jsonStr = """
            {
                "id": "img-webp-1",
                "product_id": "prod-101",
                "url": "http://10.0.2.2:8081/uploads/photo.webp",
                "file_name": "photo.webp",
                "mime_type": "image/webp",
                "file_size_bytes": 30720,
                "display_order": 2,
                "is_primary": false,
                "uploaded_at": "2024-03-01T00:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductImageResponseDto>(jsonStr)

        assertEquals("image/webp", dto.mimeType)
        assertEquals(30720, dto.fileSizeBytes)
    }

    @Test
    fun `ProductImageResponseDto deserializes GIF image`() {
        val jsonStr = """
            {
                "id": "img-gif-1",
                "product_id": "prod-202",
                "url": "http://10.0.2.2:8081/uploads/animated.gif",
                "file_name": "animated.gif",
                "mime_type": "image/gif",
                "file_size_bytes": 999999,
                "display_order": 0,
                "is_primary": false,
                "uploaded_at": "2024-04-01T00:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductImageResponseDto>(jsonStr)

        assertEquals("image/gif", dto.mimeType)
        assertEquals("animated.gif", dto.fileName)
    }

    @Test
    fun `list of ProductImageResponseDto deserializes image list response`() {
        val jsonStr = """
            [
                {
                    "id": "img-1",
                    "product_id": "prod-1",
                    "url": "http://10.0.2.2:8081/uploads/1.jpg",
                    "file_name": "1.jpg",
                    "mime_type": "image/jpeg",
                    "file_size_bytes": 100,
                    "display_order": 0,
                    "is_primary": true,
                    "uploaded_at": "2024-01-01T00:00:00Z"
                },
                {
                    "id": "img-2",
                    "product_id": "prod-1",
                    "url": "http://10.0.2.2:8081/uploads/2.jpg",
                    "file_name": "2.jpg",
                    "mime_type": "image/jpeg",
                    "file_size_bytes": 200,
                    "display_order": 1,
                    "is_primary": false,
                    "uploaded_at": "2024-01-02T00:00:00Z"
                }
            ]
        """.trimIndent()

        val list = json.decodeFromString<List<ProductImageResponseDto>>(jsonStr)

        assertEquals(2, list.size)
        assertTrue(list[0].isPrimary)
        assertFalse(list[1].isPrimary)
        assertEquals(0, list[0].displayOrder)
        assertEquals(1, list[1].displayOrder)
    }

    @Test
    fun `ProductImageResponseDto handles HTTPS URLs`() {
        val jsonStr = """
            {
                "id": "img-https-1",
                "product_id": "prod-303",
                "url": "https://cdn.nearai.dev/uploads/product-image.jpg",
                "file_name": "product-image.jpg",
                "mime_type": "image/jpeg",
                "file_size_bytes": 150000,
                "display_order": 0,
                "is_primary": true,
                "uploaded_at": "2024-05-01T00:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductImageResponseDto>(jsonStr)

        assertTrue(dto.url.startsWith("https://"))
        assertEquals("https://cdn.nearai.dev/uploads/product-image.jpg", dto.url)
    }

    @Test
    fun `ProductResponseDto deserializes images list from product details`() {
        val jsonStr = """
            {
                "id": "prod-full",
                "name": "Full Product",
                "description": "Product with images",
                "slug": "full-product",
                "price_amount": 149.99,
                "price_currency": "USD",
                "images": [
                    "http://10.0.2.2:8081/uploads/img1.jpg",
                    "http://10.0.2.2:8081/uploads/img2.png"
                ],
                "status": "active",
                "category_id": "cat-1",
                "created_at": "2024-01-01T00:00:00Z",
                "updated_at": "2024-01-02T00:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductResponseDto>(jsonStr)

        assertEquals(2, dto.images.size)
        assertEquals("http://10.0.2.2:8081/uploads/img1.jpg", dto.images[0])
        assertEquals("http://10.0.2.2:8081/uploads/img2.png", dto.images[1])
    }

    @Test
    fun `ProductResponseDto handles empty images list`() {
        val jsonStr = """
            {
                "id": "prod-no-img",
                "name": "No Image Product",
                "description": "No images",
                "slug": "no-image",
                "price_amount": 10.0,
                "price_currency": "USD",
                "images": [],
                "status": "active",
                "category_id": null,
                "created_at": "2024-01-01T00:00:00Z",
                "updated_at": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductResponseDto>(jsonStr)

        assertTrue(dto.images.isEmpty())
    }

    @Test
    fun `ReorderImagesRequest serializes correctly`() {
        val encodeJson = Json { encodeDefaults = true }
        val request = ReorderImagesRequest(
            imageOrders = listOf(
                ImageOrderEntry(imageId = "img-1", displayOrder = 0),
                ImageOrderEntry(imageId = "img-2", displayOrder = 1),
                ImageOrderEntry(imageId = "img-3", displayOrder = 2)
            )
        )

        val serialized = encodeJson.encodeToString(ReorderImagesRequest.serializer(), request)

        assertTrue(serialized.contains("\"image_orders\""))
        assertTrue(serialized.contains("\"img-1\""))
        assertTrue(serialized.contains("\"img-2\""))
        assertTrue(serialized.contains("\"img-3\""))
        assertTrue(serialized.contains("\"display_order\""))
    }
}
