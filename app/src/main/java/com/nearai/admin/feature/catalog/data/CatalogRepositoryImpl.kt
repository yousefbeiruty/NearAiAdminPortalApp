package com.nearai.admin.feature.catalog.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.network.ApiErrorParser
import com.nearai.admin.core.security.TokenRefreshManager
import com.nearai.admin.feature.catalog.domain.CatalogRepository
import com.nearai.admin.feature.catalog.domain.PaginatedCategories
import com.nearai.admin.feature.catalog.domain.PaginatedProducts
import com.nearai.admin.feature.catalog.domain.PaginatedStockMovements
import com.nearai.admin.feature.catalog.domain.PaginatedVariants
import com.nearai.admin.feature.catalog.domain.ProductSortBy
import com.nearai.admin.feature.catalog.domain.ProductStatus
import com.nearai.admin.feature.catalog.domain.VariantStatus
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [CatalogRepository] that calls the real FastAPI backend.
 */
@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val apiService: CatalogApiService,
    private val tokenRefreshManager: TokenRefreshManager
) : CatalogRepository {

    // === Categories ===

    override suspend fun listCategories(page: Int, size: Int, parentId: String?): Result<PaginatedCategories> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listCategories(page, size, parentId)
                Result.Success(CatalogMapper.mapPaginatedCategories(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getCategory(categoryId: String): Result<com.nearai.admin.feature.catalog.domain.Category> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getCategory(categoryId)
                Result.Success(CatalogMapper.mapCategory(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun createCategory(name: String, slug: String?, description: String, parentId: String?): Result<com.nearai.admin.feature.catalog.domain.Category> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.createCategory(CategoryCreateRequest(name, slug, description, parentId))
                Result.Success(CatalogMapper.mapCategory(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    // === Products ===

    override suspend fun listProducts(
        page: Int, size: Int, categoryId: String?, status: ProductStatus?,
        search: String?, minPrice: Double?, maxPrice: Double?, sortBy: ProductSortBy
    ): Result<PaginatedProducts> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listProducts(page, size, categoryId, status?.name?.lowercase(), search, minPrice, maxPrice, sortBy.toBackendValue())
                Result.Success(CatalogMapper.mapPaginatedProducts(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun searchProducts(
        query: String, page: Int, size: Int, categoryId: String?, status: ProductStatus?,
        minPrice: Double?, maxPrice: Double?, sortBy: ProductSortBy
    ): Result<PaginatedProducts> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.searchProducts(query, page, size, categoryId, status?.name?.lowercase(), minPrice, maxPrice, sortBy.toBackendValue())
                Result.Success(CatalogMapper.mapPaginatedProducts(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getProduct(productId: String): Result<com.nearai.admin.feature.catalog.domain.Product> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getProduct(productId)
                Result.Success(CatalogMapper.mapProduct(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun createProduct(
        name: String, description: String, slug: String?, priceAmount: Double,
        priceCurrency: String, images: List<String>?, categoryId: String?, status: ProductStatus
    ): Result<com.nearai.admin.feature.catalog.domain.Product> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.createProduct(ProductCreateRequest(name, description, slug, priceAmount, priceCurrency, images, categoryId, status.name.lowercase()))
                Result.Success(CatalogMapper.mapProduct(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun updateProduct(
        productId: String,
        name: String?,
        description: String?,
        slug: String?,
        priceAmount: Double?,
        priceCurrency: String?,
        images: List<String>?,
        categoryId: String?,
        status: ProductStatus?
    ): Result<com.nearai.admin.feature.catalog.domain.Product> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.updateProduct(
                    productId,
                    ProductUpdateRequest(name, description, slug, priceAmount, priceCurrency, images, categoryId, status?.name?.lowercase())
                )
                Result.Success(CatalogMapper.mapProduct(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    // === Product Images ===

    override suspend fun uploadProductImage(productId: String, file: MultipartBody.Part): Result<com.nearai.admin.feature.catalog.domain.ProductImage> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.uploadProductImage(productId, file)
                Result.Success(CatalogMapper.mapProductImage(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun listProductImages(productId: String): Result<List<com.nearai.admin.feature.catalog.domain.ProductImage>> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listProductImages(productId)
                Result.Success(response.map { CatalogMapper.mapProductImage(it) })
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun reorderProductImages(productId: String, imageOrders: List<Pair<String, Int>>): Result<List<com.nearai.admin.feature.catalog.domain.ProductImage>> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val request = ReorderImagesRequest(imageOrders.map { (id, order) -> ImageOrderEntry(id, order) })
                val response = apiService.reorderProductImages(productId, request)
                Result.Success(response.map { CatalogMapper.mapProductImage(it) })
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun setPrimaryImage(productId: String, imageId: String): Result<com.nearai.admin.feature.catalog.domain.ProductImage> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.setPrimaryImage(productId, imageId)
                Result.Success(CatalogMapper.mapProductImage(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun deleteProductImage(productId: String, imageId: String): Result<com.nearai.admin.feature.catalog.domain.ProductImage> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.deleteProductImage(productId, imageId)
                Result.Success(CatalogMapper.mapProductImage(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    // === Variants ===

    override suspend fun listVariants(productId: String, page: Int, size: Int, status: VariantStatus?): Result<PaginatedVariants> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.listVariants(productId, page, size, status?.name?.lowercase())
                Result.Success(CatalogMapper.mapPaginatedVariants(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getVariant(variantId: String): Result<com.nearai.admin.feature.catalog.domain.ProductVariant> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getVariant(variantId)
                Result.Success(CatalogMapper.mapVariant(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getVariantBySku(sku: String): Result<com.nearai.admin.feature.catalog.domain.ProductVariant> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getVariantBySku(sku)
                Result.Success(CatalogMapper.mapVariant(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun createVariant(productId: String, sku: String, attributes: Map<String, String>): Result<com.nearai.admin.feature.catalog.domain.ProductVariant> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.createVariant(productId, VariantCreateRequest(sku, attributes))
                Result.Success(CatalogMapper.mapVariant(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun updateVariant(
        variantId: String,
        sku: String?,
        attributes: Map<String, String>?
    ): Result<com.nearai.admin.feature.catalog.domain.ProductVariant> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.updateVariant(variantId, VariantUpdateRequest(sku, attributes))
                Result.Success(CatalogMapper.mapVariant(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun updateVariantStatus(
        variantId: String,
        status: VariantStatus
    ): Result<com.nearai.admin.feature.catalog.domain.ProductVariant> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.updateVariantStatus(variantId, VariantStatusUpdateRequest(status.name.lowercase()))
                Result.Success(CatalogMapper.mapVariant(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    // === Stock ===

    override suspend fun getStockAvailability(variantId: String): Result<com.nearai.admin.feature.catalog.domain.StockBalance> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getStockAvailability(variantId)
                Result.Success(CatalogMapper.mapStockBalance(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun receiveStock(variantId: String, quantity: Int, reason: String): Result<com.nearai.admin.feature.catalog.domain.StockBalance> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.receiveStock(variantId, StockReceiptRequest(quantity, reason))
                Result.Success(CatalogMapper.mapStockBalance(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun adjustStock(variantId: String, quantityDelta: Int, reason: String): Result<com.nearai.admin.feature.catalog.domain.StockBalance> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.adjustStock(variantId, StockAdjustmentRequest(quantityDelta, reason))
                Result.Success(CatalogMapper.mapStockBalance(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override suspend fun getStockMovements(variantId: String, page: Int, size: Int): Result<PaginatedStockMovements> {
        return tokenRefreshManager.refreshAndRetry {
            try {
                val response = apiService.getStockMovements(variantId, page, size)
                Result.Success(CatalogMapper.mapPaginatedStockMovements(response))
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun handleException(e: Throwable): Result.Error {
        return when (e) {
            is HttpException -> Result.Error(ApiErrorParser.parse(e))
            is IOException -> Result.Error(AppError.NetworkError("No internet connection"))
            else -> Result.Error(AppError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
