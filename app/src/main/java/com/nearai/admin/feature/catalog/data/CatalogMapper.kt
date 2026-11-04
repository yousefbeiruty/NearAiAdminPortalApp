package com.nearai.admin.feature.catalog.data

import com.nearai.admin.feature.catalog.domain.Category
import com.nearai.admin.feature.catalog.domain.PaginatedCategories
import com.nearai.admin.feature.catalog.domain.PaginatedProducts
import com.nearai.admin.feature.catalog.domain.PaginatedStockMovements
import com.nearai.admin.feature.catalog.domain.PaginatedVariants
import com.nearai.admin.feature.catalog.domain.Product
import com.nearai.admin.feature.catalog.domain.ProductImage
import com.nearai.admin.feature.catalog.domain.ProductStatus
import com.nearai.admin.feature.catalog.domain.ProductVariant
import com.nearai.admin.feature.catalog.domain.StockBalance
import com.nearai.admin.feature.catalog.domain.StockMovement
import com.nearai.admin.feature.catalog.domain.StockMovementType
import com.nearai.admin.feature.catalog.domain.VariantStatus

/**
 * Maps API DTOs to domain models for the catalog feature.
 */
object CatalogMapper {

    fun mapCategory(dto: CategoryResponseDto): Category = Category(
        id = dto.id,
        name = dto.name,
        slug = dto.slug,
        description = dto.description,
        parentId = dto.parentId,
        createdAt = dto.createdAt
    )

    fun mapPaginatedCategories(dto: CategoryListResponse): PaginatedCategories = PaginatedCategories(
        categories = dto.items.map(::mapCategory),
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )

    fun mapProduct(dto: ProductResponseDto): Product = Product(
        id = dto.id,
        name = dto.name,
        description = dto.description,
        slug = dto.slug,
        priceAmount = dto.priceAmount,
        priceCurrency = dto.priceCurrency,
        images = dto.images,
        status = ProductStatus.fromString(dto.status),
        categoryId = dto.categoryId,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )

    fun mapPaginatedProducts(dto: ProductListResponse): PaginatedProducts = PaginatedProducts(
        products = dto.items.map(::mapProduct),
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )

    fun mapProductImage(dto: ProductImageResponseDto): ProductImage = ProductImage(
        id = dto.id,
        productId = dto.productId,
        url = dto.url,
        fileName = dto.fileName,
        mimeType = dto.mimeType,
        fileSizeBytes = dto.fileSizeBytes,
        displayOrder = dto.displayOrder,
        isPrimary = dto.isPrimary,
        uploadedAt = dto.uploadedAt
    )

    fun mapVariant(dto: VariantResponseDto): ProductVariant = ProductVariant(
        id = dto.id,
        productId = dto.productId,
        sku = dto.sku,
        attributes = dto.attributes,
        status = VariantStatus.fromString(dto.status),
        availableQuantity = dto.availableQuantity,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )

    fun mapPaginatedVariants(dto: VariantListResponse): PaginatedVariants = PaginatedVariants(
        variants = dto.items.map(::mapVariant),
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )

    fun mapStockBalance(dto: StockBalanceResponseDto): StockBalance = StockBalance(
        variantId = dto.variantId,
        onHand = dto.onHand,
        reserved = dto.reserved,
        availableQuantity = dto.availableQuantity
    )

    fun mapStockMovement(dto: StockMovementResponseDto): StockMovement = StockMovement(
        id = dto.id,
        variantId = dto.variantId,
        quantityDelta = dto.quantityDelta,
        movementType = StockMovementType.fromString(dto.movementType),
        reason = dto.reason,
        createdAt = dto.createdAt
    )

    fun mapPaginatedStockMovements(dto: StockMovementListResponse): PaginatedStockMovements = PaginatedStockMovements(
        movements = dto.items.map(::mapStockMovement),
        page = dto.page,
        size = dto.size,
        totalPages = dto.totalPages,
        totalItems = dto.total
    )
}
