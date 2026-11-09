package com.nearai.admin.feature.catalog.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model for a product category.
 */
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val parentId: String?,
    val createdAt: String
)

/**
 * Domain model for a product.
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val slug: String,
    val priceAmount: Double,
    val priceCurrency: String,
    val images: List<String>,
    val status: ProductStatus,
    val categoryId: String?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Domain model for a product image.
 */
data class ProductImage(
    val id: String,
    val productId: String,
    val url: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Int,
    val displayOrder: Int,
    val isPrimary: Boolean,
    val uploadedAt: String
)

/**
 * Domain model for a product variant.
 */
data class ProductVariant(
    val id: String,
    val productId: String,
    val sku: String,
    val attributes: Map<String, String>,
    val status: VariantStatus,
    val availableQuantity: Int,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Domain model for stock balance.
 */
data class StockBalance(
    val variantId: String,
    val onHand: Int,
    val reserved: Int,
    val availableQuantity: Int
)

/**
 * Domain model for a stock movement record.
 */
data class StockMovement(
    val id: String,
    val variantId: String,
    val quantityDelta: Int,
    val movementType: StockMovementType,
    val reason: String,
    val createdAt: String
)

/**
 * Paginated list of categories.
 */
data class PaginatedCategories(
    val categories: List<Category>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Paginated list of products.
 */
data class PaginatedProducts(
    val products: List<Product>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Paginated list of variants.
 */
data class PaginatedVariants(
    val variants: List<ProductVariant>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Paginated list of stock movements.
 */
data class PaginatedStockMovements(
    val movements: List<StockMovement>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

/**
 * Sort order for product listing.
 */
enum class ProductSortBy {
    NEWEST, PRICE_ASC, PRICE_DESC;

    fun toBackendValue(): String = when (this) {
        NEWEST -> "newest"
        PRICE_ASC -> "price_asc"
        PRICE_DESC -> "price_desc"
    }

    companion object {
        fun fromString(value: String): ProductSortBy {
            return values().find { it.toBackendValue() == value.lowercase() }
                ?: NEWEST
        }
    }
}

/**
 * Repository interface for catalog management operations.
 */
interface CatalogRepository {

    // === Categories ===

    suspend fun listCategories(
        page: Int = 1,
        size: Int = 20,
        parentId: String? = null
    ): Result<PaginatedCategories>

    suspend fun getCategory(categoryId: String): Result<Category>

    suspend fun createCategory(
        name: String,
        slug: String? = null,
        description: String = "",
        parentId: String? = null
    ): Result<Category>

    // === Products ===

    suspend fun listProducts(
        page: Int = 1,
        size: Int = 20,
        categoryId: String? = null,
        status: ProductStatus? = null,
        search: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: ProductSortBy = ProductSortBy.NEWEST
    ): Result<PaginatedProducts>

    suspend fun searchProducts(
        query: String,
        page: Int = 1,
        size: Int = 20,
        categoryId: String? = null,
        status: ProductStatus? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: ProductSortBy = ProductSortBy.NEWEST
    ): Result<PaginatedProducts>

    suspend fun getProduct(productId: String): Result<Product>

    suspend fun createProduct(
        name: String,
        description: String,
        slug: String? = null,
        priceAmount: Double,
        priceCurrency: String = "USD",
        images: List<String>? = null,
        categoryId: String? = null,
        status: ProductStatus = ProductStatus.ACTIVE
    ): Result<Product>

    suspend fun updateProduct(
        productId: String,
        name: String? = null,
        description: String? = null,
        slug: String? = null,
        priceAmount: Double? = null,
        priceCurrency: String? = null,
        images: List<String>? = null,
        categoryId: String? = null,
        status: ProductStatus? = null
    ): Result<Product>

    // === Product Images ===

    suspend fun uploadProductImage(
        productId: String,
        file: okhttp3.MultipartBody.Part
    ): Result<ProductImage>

    suspend fun listProductImages(productId: String): Result<List<ProductImage>>

    suspend fun reorderProductImages(
        productId: String,
        imageOrders: List<Pair<String, Int>>
    ): Result<List<ProductImage>>

    suspend fun setPrimaryImage(productId: String, imageId: String): Result<ProductImage>

    suspend fun deleteProductImage(productId: String, imageId: String): Result<ProductImage>

    // === Variants ===

    suspend fun listVariants(
        productId: String,
        page: Int = 1,
        size: Int = 20,
        status: VariantStatus? = null
    ): Result<PaginatedVariants>

    suspend fun getVariant(variantId: String): Result<ProductVariant>

    suspend fun getVariantBySku(sku: String): Result<ProductVariant>

    suspend fun createVariant(
        productId: String,
        sku: String,
        attributes: Map<String, String> = emptyMap()
    ): Result<ProductVariant>

    suspend fun updateVariant(
        variantId: String,
        sku: String? = null,
        attributes: Map<String, String>? = null
    ): Result<ProductVariant>

    suspend fun updateVariantStatus(
        variantId: String,
        status: VariantStatus
    ): Result<ProductVariant>

    // === Stock ===

    suspend fun getStockAvailability(variantId: String): Result<StockBalance>

    suspend fun receiveStock(
        variantId: String,
        quantity: Int,
        reason: String = ""
    ): Result<StockBalance>

    suspend fun adjustStock(
        variantId: String,
        quantityDelta: Int,
        reason: String
    ): Result<StockBalance>

    suspend fun getStockMovements(
        variantId: String,
        page: Int = 1,
        size: Int = 20
    ): Result<PaginatedStockMovements>
}
