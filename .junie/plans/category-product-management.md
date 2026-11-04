# Category and Product Management — Implementation Plan

## Phase 1 — Audit Complete

### Backend API Contracts (20 endpoints confirmed)

| Module | Endpoints | Auth Required |
|--------|-----------|---------------|
| **Categories** | GET /categories/, GET /categories/{id}, POST /categories/ | Write: admin only |
| **Products** | GET /products/, GET /products/search, GET /products/{id}, POST /products/ | Write: admin only |
| **Images** | POST /products/{id}/images (multipart), GET /products/{id}/images, PATCH /products/{id}/images/reorder, PATCH /products/{id}/images/{imageId}/primary, DELETE /products/{id}/images/{imageId} | Write: admin only |
| **Variants** | GET /products/{id}/variants, GET /variants/{id}, GET /variants/by-sku/{sku}, POST /products/{id}/variants | Write: admin only |
| **Stock** | GET /variants/{id}/stock, POST /variants/{id}/stock/receipts, POST /variants/{id}/stock/adjustments, GET /variants/{id}/stock/movements | Write: admin only |

### Backend Gaps (not implemented)
- Update category, delete category
- Update product, delete product
- Update variant, delete variant
- Category tree endpoint
- Bulk operations

### Architecture Pattern (from Orders feature)

```
feature/orders/
├── domain/
│   ├── OrderRepository.kt      # Interface + domain models
│   └── OrderStatus.kt          # Enums with safe fromString
├── data/
│   ├── OrderApiService.kt      # Retrofit service + DTOs
│   ├── OrderMapper.kt          # DTO-to-domain mappers
│   ├── OrderRepositoryImpl.kt  # Real API calls + TokenRefreshManager
│   └── OrderModule.kt          # Hilt @Binds + @Provides
└── presentation/
    ├── OrdersListViewModel.kt  # Pagination, filtering
    ├── OrdersListScreen.kt     # Filter chips, LazyColumn
    ├── OrderDetailsViewModel.kt# Actions, loading states
    ├── OrderDetailsScreen.kt   # Details, timeline, actions
    └── OrderComponents.kt      # Reusable composables
```

### Existing References
- `OrderApiService.kt` — Retrofit service pattern, DTOs with `@Serializable`/`@SerialName`
- `OrderRepository.kt` — Domain models + repository interface pattern
- `OrderRepositoryImpl.kt` — Real API calls with `TokenRefreshManager.refreshAndRetry`
- `OrderModule.kt` — Hilt module with `@Binds` and `@Provides`
- `OrderMapper.kt` — DTO-to-domain mapping functions
- `OrdersListViewModel.kt` — Pagination, filtering, StateFlow UI state
- `OrdersListScreen.kt` — Filter chips, LazyColumn, loading/empty/error states
- `OrderComponents.kt` — Reusable composables
- `NetworkModule.kt` — Retrofit/OkHttp with auth interceptor
- `TokenRefreshManager.kt` — Automatic 401 token refresh
- `MoreScreen.kt` — Menu hub (will add Categories here)
- `AppDestination.kt` — @Serializable route objects
- `AppNavHost.kt` — NavGraph with session-gated destinations

---

## Delivery Steps

### ✓ Step 1: Create Catalog API service, domain models, and repository

**New files (6):**
- `CatalogStatus.kt` — `ProductStatus`, `VariantStatus`, `StockMovementType` enums with safe `fromString`
- `CatalogRepository.kt` — Domain models (Category, Product, ProductImage, ProductVariant, StockBalance, StockMovement) + repository interface
- `CatalogApiService.kt` — Retrofit service with all 20 catalog endpoints + DTOs
- `CatalogMapper.kt` — DTO-to-domain mapping functions
- `CatalogRepositoryImpl.kt` — Production repository with real API calls + TokenRefreshManager
- `CatalogModule.kt` — Hilt module binding CatalogRepository and providing CatalogApiService

### ✓ Step 2: Implement Category Management screens

**New files (2):**
- `CategoriesListViewModel.kt` — Category list, pagination, create category with validation
- `CategoriesListScreen.kt` — Category list with FAB, loading/empty/error states, create dialog, CategoryCard composable

**Modified files (3):**
- `AppDestination.kt` — Added `CategoriesRoute`, `ProductsRoute`, `ProductDetailsRoute`, `ProductCreateRoute`
- `AppNavHost.kt` — Registered `CategoriesRoute` with Hilt ViewModel, added `onNavigateToCategories` to MoreScreen
- `MoreScreen.kt` — Added "Categories" menu item with Category icon

### ✓ Step 3: Implement Product Management screens

**New files (2):**
- `ProductsListViewModel.kt` — Product list, search, status filter, sort, pagination, create product with validation
- `ProductsListScreen.kt` — Product list with search bar, status filter chips, ProductCard composable with status pill, loading/empty/error states

**Modified files (3):**
- `AppDestination.kt` — Already has `ProductsRoute`, `ProductDetailsRoute`, `ProductCreateRoute`
- `AppNavHost.kt` — Registered `ProductsRoute` with Hilt ViewModel, navigation to product details and create
- `MoreScreen.kt` — Added "Products" menu item with ShoppingBag icon

### Step 4: Implement Product Create/Edit screen

**New files (2):**
- `ProductCreateViewModel.kt` — Create product form, variant management, image upload
- `ProductCreateScreen.kt` — Full product form with category selection, variants, images

**Modified files (2):**
- `AppDestination.kt` — Add `ProductCreateRoute`
- `AppNavHost.kt` — Register product create route

### Step 5: Add unit tests and verify build

**New files (3):**
- `CatalogMapperTest.kt` — DTO-to-domain mapping tests
- `CategoriesListViewModelTest.kt` — Category list and create tests
- `ProductsListViewModelTest.kt` — Product list, search, filter tests

**Verification:**
- `./gradlew assembleDebug` — BUILD SUCCESSFUL
- `./gradlew testDebugUnitTest` — All tests pass

---

## Key Decisions

1. **Pagination** — Same pattern as Orders: `total` (not `total_items`), `page`, `size`, `total_pages`, `items`
2. **Image upload** — Multipart/form-data via Retrofit `@Multipart` with `MultipartBody.Part`
3. **Product update/delete** — Not implemented in backend; Android will not expose these actions
4. **Category update/delete** — Not implemented in backend; Android will not expose these actions
5. **Variant update/delete** — Not implemented in backend; Android will not expose these actions
6. **TokenRefreshManager** — All catalog API calls wrapped with `refreshAndRetry` for automatic 401 token refresh
7. **Price type** — `Double` in Kotlin DTOs, matching backend JSON serialization
8. **Dates** — ISO 8601 strings, stored as `String` in domain models
9. **UUIDs** — Standard strings, no special serialization
