package com.nearai.admin.feature.catalog.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface for catalog management endpoints.
 *
 * Backend contract from CATALOG_MANAGEMENT_API.md:
 * - GET    /api/v1/categories/              — list categories
 * - GET    /api/v1/categories/{id}          — get category
 * - POST   /api/v1/categories/              — create category
 * - GET    /api/v1/products/                — list products
 * - GET    /api/v1/products/search          — search products
 * - GET    /api/v1/products/{id}            — get product
 * - POST   /api/v1/products/                — create product
 * - POST   /api/v1/products/{id}/images     — upload image (multipart)
 * - GET    /api/v1/products/{id}/images     — list images
 * - PATCH  /api/v1/products/{id}/images/reorder — reorder images
 * - PATCH  /api/v1/products/{id}/images/{imageId}/primary — set primary
 * - DELETE /api/v1/products/{id}/images/{imageId} — delete image
 * - GET    /api/v1/products/{id}/variants   — list variants
 * - GET    /api/v1/variants/{id}            — get variant
 * - GET    /api/v1/variants/by-sku/{sku}    — get variant by SKU
 * - POST   /api/v1/products/{id}/variants   — create variant
 * - GET    /api/v1/variants/{id}/stock      — get stock availability
 * - POST   /api/v1/variants/{id}/stock/receipts — receive stock
 * - POST   /api/v1/variants/{id}/stock/adjustments — adjust stock
 * - GET    /api/v1/variants/{id}/stock/movements — stock movements
 */
interface CatalogApiService {

    // === Categories ===

    @GET("api/v1/categories/")
    suspend fun listCategories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("parent_id") parentId: String? = null
    ): CategoryListResponse

    @GET("api/v1/categories/{category_id}")
    suspend fun getCategory(@Path("category_id") categoryId: String): CategoryResponseDto

    @POST("api/v1/categories/")
    suspend fun createCategory(@Body request: CategoryCreateRequest): CategoryResponseDto

    // === Products ===

    @GET("api/v1/products/")
    suspend fun listProducts(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("category_id") categoryId: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("sort_by") sortBy: String? = null
    ): ProductListResponse

    @GET("api/v1/products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("category_id") categoryId: String? = null,
        @Query("status") status: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("sort_by") sortBy: String? = null
    ): ProductListResponse

    @GET("api/v1/products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: String): ProductResponseDto

    @POST("api/v1/products/")
    suspend fun createProduct(@Body request: ProductCreateRequest): ProductResponseDto

    @PATCH("api/v1/products/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: String,
        @Body request: ProductUpdateRequest
    ): ProductResponseDto

    // === Product Images ===

    @Multipart
    @POST("api/v1/products/{product_id}/images")
    suspend fun uploadProductImage(
        @Path("product_id") productId: String,
        @Part file: MultipartBody.Part
    ): ProductImageResponseDto

    @GET("api/v1/products/{product_id}/images")
    suspend fun listProductImages(@Path("product_id") productId: String): List<ProductImageResponseDto>

    @PATCH("api/v1/products/{product_id}/images/reorder")
    suspend fun reorderProductImages(
        @Path("product_id") productId: String,
        @Body request: ReorderImagesRequest
    ): List<ProductImageResponseDto>

    @PATCH("api/v1/products/{product_id}/images/{image_id}/primary")
    suspend fun setPrimaryImage(
        @Path("product_id") productId: String,
        @Path("image_id") imageId: String
    ): ProductImageResponseDto

    @DELETE("api/v1/products/{product_id}/images/{image_id}")
    suspend fun deleteProductImage(
        @Path("product_id") productId: String,
        @Path("image_id") imageId: String
    ): ProductImageResponseDto

    // === Variants ===

    @GET("api/v1/products/{product_id}/variants")
    suspend fun listVariants(
        @Path("product_id") productId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("status") status: String? = "active"
    ): VariantListResponse

    @GET("api/v1/variants/{variant_id}")
    suspend fun getVariant(@Path("variant_id") variantId: String): VariantResponseDto

    @GET("api/v1/variants/by-sku/{sku}")
    suspend fun getVariantBySku(@Path("sku") sku: String): VariantResponseDto

    @POST("api/v1/products/{product_id}/variants")
    suspend fun createVariant(
        @Path("product_id") productId: String,
        @Body request: VariantCreateRequest
    ): VariantResponseDto

    @PATCH("api/v1/variants/{variant_id}")
    suspend fun updateVariant(
        @Path("variant_id") variantId: String,
        @Body request: VariantUpdateRequest
    ): VariantResponseDto

    @PATCH("api/v1/variants/{variant_id}/status")
    suspend fun updateVariantStatus(
        @Path("variant_id") variantId: String,
        @Body request: VariantStatusUpdateRequest
    ): VariantResponseDto

    // === Stock ===

    @GET("api/v1/variants/{variant_id}/stock")
    suspend fun getStockAvailability(@Path("variant_id") variantId: String): StockBalanceResponseDto

    @POST("api/v1/variants/{variant_id}/stock/receipts")
    suspend fun receiveStock(
        @Path("variant_id") variantId: String,
        @Body request: StockReceiptRequest
    ): StockBalanceResponseDto

    @POST("api/v1/variants/{variant_id}/stock/adjustments")
    suspend fun adjustStock(
        @Path("variant_id") variantId: String,
        @Body request: StockAdjustmentRequest
    ): StockBalanceResponseDto

    @GET("api/v1/variants/{variant_id}/stock/movements")
    suspend fun getStockMovements(
        @Path("variant_id") variantId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StockMovementListResponse
}

// =============================================================================
// Request DTOs
// =============================================================================

@Serializable
data class CategoryCreateRequest(
    val name: String,
    val slug: String? = null,
    val description: String = "",
    @SerialName("parent_id") val parentId: String? = null
)

@Serializable
data class ProductCreateRequest(
    val name: String,
    val description: String,
    val slug: String? = null,
    @SerialName("price_amount") val priceAmount: Double,
    @SerialName("price_currency") val priceCurrency: String = "USD",
    val images: List<String>? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val status: String = "active"
)

@Serializable
data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val slug: String? = null,
    @SerialName("price_amount") val priceAmount: Double? = null,
    @SerialName("price_currency") val priceCurrency: String? = null,
    val images: List<String>? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val status: String? = null
)

@Serializable
data class ImageOrderEntry(
    @SerialName("image_id") val imageId: String,
    @SerialName("display_order") val displayOrder: Int
)

@Serializable
data class ReorderImagesRequest(
    @SerialName("image_orders") val imageOrders: List<ImageOrderEntry>
)

@Serializable
data class VariantCreateRequest(
    val sku: String,
    val attributes: Map<String, String> = emptyMap()
)

@Serializable
data class VariantUpdateRequest(
    val sku: String? = null,
    val attributes: Map<String, String>? = null,
    val status: String? = null
)

@Serializable
data class VariantStatusUpdateRequest(
    val status: String
)

@Serializable
data class StockReceiptRequest(
    val quantity: Int,
    val reason: String = ""
)

@Serializable
data class StockAdjustmentRequest(
    @SerialName("quantity_delta") val quantityDelta: Int,
    val reason: String
)

// =============================================================================
// Response DTOs
// =============================================================================

@Serializable
data class CategoryResponseDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CategoryListResponse(
    val items: List<CategoryResponseDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class ProductResponseDto(
    val id: String,
    val name: String,
    val description: String,
    val slug: String,
    @SerialName("price_amount") val priceAmount: Double,
    @SerialName("price_currency") val priceCurrency: String,
    val images: List<String>,
    val status: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class ProductListResponse(
    val items: List<ProductResponseDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class ProductImageResponseDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    val url: String,
    @SerialName("file_name") val fileName: String,
    @SerialName("mime_type") val mimeType: String,
    @SerialName("file_size_bytes") val fileSizeBytes: Int,
    @SerialName("display_order") val displayOrder: Int,
    @SerialName("is_primary") val isPrimary: Boolean,
    @SerialName("uploaded_at") val uploadedAt: String
)

@Serializable
data class VariantResponseDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    val sku: String,
    val attributes: Map<String, String>,
    val status: String,
    @SerialName("available_quantity") val availableQuantity: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class VariantListResponse(
    val items: List<VariantResponseDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class StockBalanceResponseDto(
    @SerialName("variant_id") val variantId: String,
    @SerialName("on_hand") val onHand: Int,
    val reserved: Int,
    @SerialName("available_quantity") val availableQuantity: Int
)

@Serializable
data class StockMovementResponseDto(
    val id: String,
    @SerialName("variant_id") val variantId: String,
    @SerialName("quantity_delta") val quantityDelta: Int,
    @SerialName("movement_type") val movementType: String,
    val reason: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class StockMovementListResponse(
    val items: List<StockMovementResponseDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    @SerialName("total_pages") val totalPages: Int
)
