---
sessionId: session-261104-151120-tnbq
---

# Requirements

### Overview & Goals

Transform the NearAI Admin Android app from a mixture of real and hardcoded data into a fully functional production-ready administration application. Connect all feature screens to real backend endpoints through the existing Clean Architecture layers.

### Scope

#### In Scope
- **Phase 1 — Inventory**: Connect InventoryScreen to existing stock endpoints (getStockAvailability, receiveStock, adjustStock, getStockMovements) with real inventory listing, search, filtering, stock operations, and movement history
- **Phase 2 — Product Editing**: Implement ProductEditScreen using existing product/variant update endpoints with load, edit, save, validate, and refresh
- **Phase 3 — Dashboard**: Replace FakeDashboardRepository with real repository connected to backend metrics endpoints
- **Phase 4 — Remaining Modules**: Implement Users Management, AI Compute Analytics, Discovery Analytics, System Health, and Audit Logs with real backend integration

#### Out of Scope
- Rewriting working features (auth, catalog listing, orders)
- Introducing fake backend responses or hardcoded production data
- Breaking existing Admin app functionality
- Backend implementation beyond what's required for missing endpoints

### User Stories

1. As an admin, I want to view real inventory data with stock availability so I can manage product stock levels
2. As an admin, I want to receive and adjust stock with movement history so I can track inventory changes
3. As an admin, I want to edit product details and save changes so I can maintain the product catalog
4. As an admin, I want to see real dashboard metrics so I can monitor platform health
5. As an admin, I want to manage users with real data so I can perform administrative actions
6. As an admin, I want to view AI usage, discovery analytics, system health, and audit logs with real data

### Functional Requirements

- Real inventory listing with search and filtering
- Stock availability display per variant
- Receive stock and adjust stock operations with backend validation
- Stock movement history with pagination
- Product editing (name, description, SKU, price, status, category)
- Real dashboard metrics (total users, DAU, AI requests, spending, places found, revenue, activity, charts)
- Users management with search, filters, and admin actions
- AI usage analytics with budget, provider distribution, and telemetry
- Discovery analytics with pipeline telemetry and trace inspection
- System health with service status and incident journal
- Audit logs with chronological stream and filtering
- Proper loading, empty, error, and retry states for all screens
- Pagination for large datasets
- Admin-only authorization enforced on backend

### Non-Functional Requirements

- No sensitive information exposed in logs
- No blocking operations on the main thread
- Unit tests for repositories, ViewModels, and mappers
- API integration tests for new endpoints
- Regression tests for existing functionality
- Preserve existing Clean Architecture, MVVM, Kotlin, Jetpack Compose, Hilt, Retrofit, and repository patterns

# Technical Design

### Current Implementation

**Architecture**: Clean Architecture with MVVM pattern. Feature-based package organization where each feature contains presentation (Compose + ViewModels), domain (repository interfaces + models), and data (repository impl + API service + mapper) layers.

**Key Patterns**:
- `Result<T>` sealed class for API outcomes (Success/Error/Loading)
- `StateFlow<UiState>` in ViewModels with immutable UI state
- Hilt DI with `@Module` and `@Binds` for repository binding
- `TokenRefreshManager.refreshAndRetry{}` for automatic token refresh on 401
- `CatalogMapper` object for DTO → domain mapping
- Pagination with `Paginated<T>` domain models
- `ApiErrorParser.parse(httpException)` for error extraction

**Already Complete**:
- `CatalogApiService` — all stock endpoints (getStockAvailability, receiveStock, adjustStock, getStockMovements), product CRUD, variant CRUD, image management
- `CatalogRepository` interface — all stock operations defined
- `CatalogRepositoryImpl` — all stock operations implemented with token refresh
- `CatalogMapper` — stock balance, stock movement, and pagination mappers
- `CatalogModule` — Hilt binding to real implementation
- Domain models: `StockBalance`, `StockMovement`, `PaginatedStockMovements`, `ProductVariant`, `Product`
- Orders module — complete reference implementation (API, repository, mapper, ViewModel, tests)
- ProductsListViewModel, ProductCreateViewModel — connected to real backend

**Needs Implementation**:
- `InventoryScreen.kt` (831 lines) — UI-only with hardcoded `SkuItem` data, no ViewModel
- `ProductEditScreen.kt` (1243 lines) — UI-only with hardcoded form data, no ViewModel
- `FakeDashboardRepository.kt` — returns hardcoded KPI metrics and activity events
- `DashboardModule.kt` — binds to FakeDashboardRepository
- `UsersScreen.kt` (561 lines) — UI-only with hardcoded `UserItem` data
- `AiUsageScreen.kt` (572 lines) — UI-only with hardcoded budget and telemetry
- `DiscoveryScreen.kt` (1084 lines) — UI-only with hardcoded discovery data
- `AuditLogsScreen.kt` (1335 lines) — UI-only with hardcoded audit entries
- `SystemHealthScreen.kt` (642 lines) — UI-only with hardcoded health data

### Key Decisions

1. **InventoryScreen will use CatalogRepository directly** — the stock endpoints already exist in `CatalogApiService` and are wired through `CatalogRepository`. No new API service needed. Create an `InventoryViewModel` that aggregates products with their variants and stock data.

2. **ProductEditScreen will use CatalogRepository** — product updates need a PATCH endpoint. The `CatalogApiService` has `createProduct` but no `updateProduct`. A `ProductUpdateRequest` and PATCH endpoint will be added to `CatalogApiService` if the backend supports it; otherwise the backend needs the endpoint first.

3. **Dashboard will get a RealDashboardRepository** — the README documents `/admin/dashboard` GET endpoint. Create `DashboardApiService`, `DashboardRepositoryImpl`, `DashboardMapper`, and update `DashboardModule` to bind the real implementation.

4. **Remaining modules follow the same pattern** — each gets an API service, repository impl, mapper, domain models, ViewModel, and UI integration following the Orders module as reference.

5. **Product update via PATCH** — the backend contract in README shows `PUT /admin/products/{id}`. The Android app will use PATCH to match the existing pattern (setPrimaryImage uses PATCH). A `ProductUpdateRequest` DTO will be added.

### Proposed Changes

#### Phase 1 — Inventory

**New files**:
- `feature/inventory/presentation/InventoryViewModel.kt` — manages product listing with variants and stock, search, filtering, receive/adjust stock operations, stock movement history
- `feature/inventory/data/InventoryModule.kt` — Hilt module (if needed, otherwise use CatalogModule)

**Modified files**:
- `feature/inventory/presentation/InventoryScreen.kt` — replace hardcoded data with ViewModel-driven UI, connect to `InventoryViewModel`
- `navigation/AppNavHost.kt` — inject `InventoryViewModel` via `hiltViewModel()`

**Approach**:
The `InventoryViewModel` will use `CatalogRepository` to:
1. Load products via `listProducts()` with search and status filters
2. For each product, load variants via `listVariants(productId)`
3. For each variant, load stock via `getStockAvailability(variantId)`
4. Expose combined UI state with products, variants, and stock levels
5. Handle `receiveStock()` and `adjustStock()` operations
6. Load `getStockMovements()` for movement history

The `InventoryScreen` will be refactored to accept a `ViewModel` parameter and display real data with loading, empty, and error states.

#### Phase 2 — Product Editing

**New files**:
- `feature/inventory/presentation/ProductEditViewModel.kt` — manages product loading, editing, validation, and saving
- `feature/catalog/data/ProductUpdateRequest.kt` (in CatalogApiService) — DTO for product updates

**Modified files**:
- `feature/catalog/data/CatalogApiService.kt` — add PATCH `/api/v1/products/{product_id}` endpoint and `ProductUpdateRequest` DTO
- `feature/catalog/domain/CatalogRepository.kt` — add `updateProduct()` method
- `feature/catalog/data/CatalogRepositoryImpl.kt` — implement `updateProduct()`
- `feature/inventory/presentation/ProductEditScreen.kt` — replace hardcoded form with ViewModel-driven UI
- `navigation/AppNavHost.kt` — pass product ID to `ProductEditRoute` and inject ViewModel

**Approach**:
The `ProductEditViewModel` will:
1. Accept a `productId` navigation argument
2. Load product via `CatalogRepository.getProduct()`
3. Load variants via `listVariants()`
4. Allow editing name, description, price, status, category
5. Save via `updateProduct()` (new PATCH endpoint)
6. Update stock via existing stock endpoints
7. Validate input before saving
8. Emit success/error events

#### Phase 3 — Dashboard

**New files**:
- `feature/dashboard/data/DashboardApiService.kt` — Retrofit service for dashboard endpoints
- `feature/dashboard/data/DashboardRepositoryImpl.kt` — real repository implementation
- `feature/dashboard/data/DashboardMapper.kt` — DTO to domain mapping

**Modified files**:
- `feature/dashboard/data/DashboardModule.kt` — bind `DashboardRepositoryImpl` instead of `FakeDashboardRepository`
- `feature/dashboard/domain/DashboardRepository.kt` — may need to extend with chart data endpoints
- `feature/dashboard/presentation/DashboardViewModel.kt` — remove `isDevelopmentMode` flag
- `feature/dashboard/presentation/DashboardScreen.kt` — handle unsupported metrics gracefully

**Approach**:
1. Inspect backend for `/admin/dashboard` endpoint
2. Create `DashboardApiService` with GET `/api/v1/admin/dashboard`
3. Create response DTOs for KPI metrics, activity events, and chart data
4. Implement `DashboardRepositoryImpl` with `TokenRefreshManager`
5. Add `DashboardMapper` for DTO → domain mapping
6. Update `DashboardModule` to bind real implementation
7. Remove `FakeDashboardRepository`
8. Update `DashboardViewModel` to remove development mode flag
9. Handle missing metrics by reporting them as unsupported

#### Phase 4 — Remaining Modules

Each module follows the same pattern:

**Users Management**:
- New: `feature/users/data/UsersApiService.kt`, `UsersRepositoryImpl.kt`, `UsersMapper.kt`, `UsersModule.kt`
- New: `feature/users/domain/UsersRepository.kt`, domain models
- New: `feature/users/presentation/UsersViewModel.kt`
- Modified: `feature/users/presentation/UsersScreen.kt` — connect to ViewModel
- Endpoints: GET `/api/v1/admin/users`, GET `/api/v1/admin/users/{id}`, POST `/api/v1/admin/users/{id}/credits`, POST `/api/v1/admin/users/{id}/quota`, POST `/api/v1/admin/users/{id}/suspend`

**AI Usage Analytics**:
- New: `feature/aiusage/data/AiUsageApiService.kt`, `AiUsageRepositoryImpl.kt`, `AiUsageMapper.kt`, `AiUsageModule.kt`
- New: `feature/aiusage/domain/AiUsageRepository.kt`, domain models
- New: `feature/aiusage/presentation/AiUsageViewModel.kt`
- Modified: `feature/aiusage/presentation/AiUsageScreen.kt` — connect to ViewModel
- Endpoints: GET `/api/v1/admin/ai-usage`, GET `/api/v1/admin/ai-usage/history`

**Discovery Analytics**:
- New: `feature/discovery/data/DiscoveryApiService.kt`, `DiscoveryRepositoryImpl.kt`, `DiscoveryMapper.kt`, `DiscoveryModule.kt`
- New: `feature/discovery/domain/DiscoveryRepository.kt`, domain models
- New: `feature/discovery/presentation/DiscoveryViewModel.kt`
- Modified: `feature/discovery/presentation/DiscoveryScreen.kt` — connect to ViewModel
- Endpoints: GET `/api/v1/admin/discovery`, GET `/api/v1/admin/discovery/trace/{id}`

**System Health**:
- New: `feature/systemhealth/data/SystemHealthApiService.kt`, `SystemHealthRepositoryImpl.kt`, `SystemHealthMapper.kt`, `SystemHealthModule.kt`
- New: `feature/systemhealth/domain/SystemHealthRepository.kt`, domain models
- New: `feature/systemhealth/presentation/SystemHealthViewModel.kt`
- Modified: `feature/systemhealth/presentation/SystemHealthScreen.kt` — connect to ViewModel
- Endpoints: GET `/api/v1/admin/health`, GET `/api/v1/admin/health/incidents`

**Audit Logs**:
- New: `feature/auditlogs/data/AuditLogsApiService.kt`, `AuditLogsRepositoryImpl.kt`, `AuditLogsMapper.kt`, `AuditLogsModule.kt`
- New: `feature/auditlogs/domain/AuditLogsRepository.kt`, domain models
- New: `feature/auditlogs/presentation/AuditLogsViewModel.kt`
- Modified: `feature/auditlogs/presentation/AuditLogsScreen.kt` — connect to ViewModel
- Endpoints: GET `/api/v1/admin/audit-logs`, POST `/api/v1/admin/audit-logs/export`

### Architecture Diagram

``` mermaid
graph LR
    UI[Compose Screens] --> VM[ViewModels]
    VM --> RI[Repository Interfaces]
    RI --> RImpl[Repository Impls]
    RImpl --> TM[TokenRefreshManager]
    RImpl --> API[API Services / Retrofit]
    API --> BE[FastAPI Backend]
    RImpl --> M[Mapper Objects]
    M --> DM[Domain Models]
    DM --> VM
```

### File Structure

```
feature/
  inventory/presentation/
    + InventoryViewModel.kt
    ~ InventoryScreen.kt
    ~ ProductEditScreen.kt
    + ProductEditViewModel.kt
  dashboard/data/
    + DashboardApiService.kt
    + DashboardRepositoryImpl.kt
    + DashboardMapper.kt
    ~ DashboardModule.kt
    - FakeDashboardRepository.kt
  dashboard/domain/
    ~ DashboardRepository.kt
  dashboard/presentation/
    ~ DashboardViewModel.kt
  users/data/
    + UsersApiService.kt
    + UsersRepositoryImpl.kt
    + UsersMapper.kt
    + UsersModule.kt
  users/domain/
    + UsersRepository.kt
  users/presentation/
    + UsersViewModel.kt
    ~ UsersScreen.kt
  aiusage/data/
    + AiUsageApiService.kt
    + AiUsageRepositoryImpl.kt
    + AiUsageMapper.kt
    + AiUsageModule.kt
  aiusage/domain/
    + AiUsageRepository.kt
  aiusage/presentation/
    + AiUsageViewModel.kt
    ~ AiUsageScreen.kt
  discovery/data/
    + DiscoveryApiService.kt
    + DiscoveryRepositoryImpl.kt
    + DiscoveryMapper.kt
    + DiscoveryModule.kt
  discovery/domain/
    + DiscoveryRepository.kt
  discovery/presentation/
    + DiscoveryViewModel.kt
    ~ DiscoveryScreen.kt
  systemhealth/data/
    + SystemHealthApiService.kt
    + SystemHealthRepositoryImpl.kt
    + SystemHealthMapper.kt
    + SystemHealthModule.kt
  systemhealth/domain/
    + SystemHealthRepository.kt
  systemhealth/presentation/
    + SystemHealthViewModel.kt
    ~ SystemHealthScreen.kt
  auditlogs/data/
    + AuditLogsApiService.kt
    + AuditLogsRepositoryImpl.kt
    + AuditLogsMapper.kt
    + AuditLogsModule.kt
  auditlogs/domain/
    + AuditLogsRepository.kt
  auditlogs/presentation/
    + AuditLogsViewModel.kt
    ~ AuditLogsScreen.kt
  catalog/data/
    ~ CatalogApiService.kt (add PATCH product)
    ~ CatalogRepository.kt (add updateProduct)
    ~ CatalogRepositoryImpl.kt (add updateProduct)
navigation/
    ~ AppNavHost.kt (inject ViewModels)
```

+ = new file, ~ = modified file, - = removed file

### Risks

1. **Backend endpoints may not exist** — The README documents expected contracts, but the backend may not implement all endpoints. Each phase requires inspecting the actual backend routes. Missing endpoints will be explicitly reported.

2. **Product update endpoint** — The `CatalogApiService` has no PATCH/PUT for products. The backend needs to support product updates before Phase 2 can be completed.

3. **Dashboard metrics mapping** — The existing `DashboardRepository` domain model may not match the backend response. The mapper will need to handle partial data and report unsupported metrics.

4. **Large screen refactoring** — Some screens (AuditLogsScreen: 1335 lines, ProductEditScreen: 1243 lines, DiscoveryScreen: 1084 lines) are large. Refactoring to ViewModel-driven UI requires careful analysis to preserve existing UI behavior.

5. **Token refresh for new API services** — Each new API service needs to be wrapped with `TokenRefreshManager.refreshAndRetry{}` in the repository implementation.

# Testing

### Validation Approach

Follow the existing testing patterns:
- **ViewModel tests**: MockK for repository mocking, `runTest` with `UnconfinedTestDispatcher`, Turbine for StateFlow verification
- **Repository tests**: MockK for API service mocking, verify token refresh integration
- **Mapper tests**: Pure function tests verifying DTO → domain mapping
- **Pattern reference**: `OrdersListViewModelTest.kt`, `CatalogMapperTest.kt`, `DashboardViewModelTest.kt`

### Key Scenarios

#### Phase 1 — Inventory
- `InventoryViewModel` loads products with variants and stock on init
- Search filters products by name/SKU
- Status filter (active, reviewing, deprecated) works
- `receiveStock()` updates stock balance and refreshes UI
- `adjustStock()` updates stock balance with delta
- `getStockMovements()` loads paginated movement history
- Error states display properly (network error, auth error)
- Empty state displays when no products exist

#### Phase 2 — Product Editing
- `ProductEditViewModel` loads product details on init
- Form validation rejects blank name, blank description, invalid price
- `updateProduct()` saves changes and emits success event
- Stock update via existing endpoints works
- Error states display backend validation errors
- Product refreshes after successful update

#### Phase 3 — Dashboard
- `DashboardRepositoryImpl` fetches real data from backend
- KPI metrics map correctly from DTO to domain
- Activity events display with proper timestamps
- Unsupported metrics are reported as unavailable (not fabricated)
- Error states display on network failure
- Retry works after error

#### Phase 4 — Remaining Modules
- Each module loads data on init
- Pagination works (load more, no more data)
- Search and filtering work where applicable
- Admin actions (grant credits, suspend user, etc.) execute and refresh
- Error states display and retry works
- Empty states display when no data

### Edge Cases

- Network timeout during stock operations (idempotency)
- Token refresh during API call
- 403 Forbidden for non-admin users
- Empty product list with no variants
- Product with multiple variants and different stock levels
- Dashboard with partial metrics (some endpoints unavailable)
- Large audit log dataset with pagination
- Concurrent stock adjustments for the same variant

### Test Changes

**New tests**:
- `InventoryViewModelTest.kt` — loading, filtering, stock operations, error handling
- `ProductEditViewModelTest.kt` — loading, validation, saving, error handling
- `DashboardRepositoryImplTest.kt` — API call, mapping, error handling
- `DashboardMapperTest.kt` — DTO to domain mapping
- `UsersViewModelTest.kt` — loading, filtering, admin actions
- `AiUsageViewModelTest.kt` — loading, error handling
- `DiscoveryViewModelTest.kt` — loading, trace inspection
- `SystemHealthViewModelTest.kt` — loading, error handling
- `AuditLogsViewModelTest.kt` — loading, filtering, pagination

**Modified tests**:
- `DashboardViewModelTest.kt` — update for real repository (remove development mode tests)

**Regression tests**:
- Verify existing catalog tests still pass
- Verify existing orders tests still pass
- Verify existing auth tests still pass

# Delivery Steps

### ✓ Step 1: Phase 1 — Inventory Screen with ViewModel
Connect InventoryScreen to real stock endpoints via InventoryViewModel using existing CatalogRepository.

**New files**:
- `feature/inventory/presentation/InventoryViewModel.kt` — StateFlow with products, variants, stock data; search/filter; receiveStock/adjustStock/movements

**Modified files**:
- `feature/inventory/presentation/InventoryScreen.kt` — replace hardcoded SkuItem data with ViewModel-driven UI (loading, empty, error states)
- `navigation/AppNavHost.kt` — inject InventoryViewModel via hiltViewModel()

**Implementation details**:
- InventoryViewModel uses CatalogRepository.listProducts() with search and status filters
- For each product, loads variants via listVariants(productId)
- For each variant, loads stock via getStockAvailability(variantId)
- Exposes combined UI state: products with nested variants and stock levels
- receiveStock() and adjustStock() operations refresh the affected variant
- getStockMovements() loads paginated history for selected variant
- InventoryScreen refactored to collect uiState and display real data
- Follows OrdersListViewModel pattern for pagination and filtering

### ✓ Step 2: Phase 2 — Product Editing with ViewModel
Implement ProductEditScreen connected to real product update endpoints via ProductEditViewModel.

**New files**:
- `feature/inventory/presentation/ProductEditViewModel.kt` — load product, edit fields, validate, save changes, update stock

**Modified files**:
- `feature/catalog/data/CatalogApiService.kt` — add PATCH `/api/v1/products/{product_id}` endpoint and ProductUpdateRequest DTO
- `feature/catalog/domain/CatalogRepository.kt` — add updateProduct() method signature
- `feature/catalog/data/CatalogRepositoryImpl.kt` — implement updateProduct() with token refresh
- `feature/inventory/presentation/ProductEditScreen.kt` — replace hardcoded form with ViewModel-driven UI
- `navigation/AppNavHost.kt` — pass productId to ProductEditRoute, inject ProductEditViewModel

**Implementation details**:
- ProductEditViewModel accepts productId from navigation arguments
- Loads product via CatalogRepository.getProduct() on init
- Loads variants via listVariants() for stock display
- Form fields: name, description, price, status, category (editable); SKU via variant
- Validation: name/description required, price > 0, valid status
- Save via updateProduct() (PATCH endpoint)
- Stock updates via existing receiveStock/adjustStock endpoints
- Emit success/error events; refresh product after save
- ProductEditScreen collects uiState and displays form with real data

### ✓ Step 3: Phase 3 — Real Dashboard Repository
Replace FakeDashboardRepository with real repository connected to backend dashboard endpoints.

**New files**:
- `feature/dashboard/data/DashboardApiService.kt` — Retrofit service with GET `/api/v1/admin/dashboard`
- `feature/dashboard/data/DashboardRepositoryImpl.kt` — real implementation with TokenRefreshManager
- `feature/dashboard/data/DashboardMapper.kt` — DTO to domain mapping for KPI metrics, activity events, chart data

**Modified files**:
- `feature/dashboard/data/DashboardModule.kt` — bind DashboardRepositoryImpl instead of FakeDashboardRepository
- `feature/dashboard/domain/DashboardRepository.kt` — extend with chart data endpoints if needed
- `feature/dashboard/presentation/DashboardViewModel.kt` — remove isDevelopmentMode flag
- `feature/dashboard/presentation/DashboardScreen.kt` — handle unsupported metrics gracefully

**Removed files**:
- `feature/dashboard/data/FakeDashboardRepository.kt`

**Implementation details**:
- Inspect backend for /admin/dashboard endpoint and response schema
- Create DashboardApiService with GET endpoint and response DTOs
- DashboardRepositoryImpl wraps API calls with TokenRefreshManager
- DashboardMapper maps DTOs to KpiMetric, ActivityEvent, DashboardData domain models
- DashboardModule binds real implementation via @Binds
- DashboardViewModel removes isDevelopmentMode flag
- DashboardScreen displays "unsupported" for missing metrics instead of fabricated values
- Update DashboardViewModelTest for real repository pattern

### ✓ Step 3: Phase 3 — Real Dashboard Repository
### ✓ Step 4: Phase 4a — Users Management Module
Implement Users management with real backend integration following the established pattern.

**New files**:
- `feature/users/data/UsersApiService.kt` — Retrofit service for user endpoints
- `feature/users/data/UsersRepositoryImpl.kt` — real implementation with TokenRefreshManager
- `feature/users/data/UsersMapper.kt` — DTO to domain mapping
- `feature/users/data/UsersModule.kt` — Hilt module
- `feature/users/domain/UsersRepository.kt` — repository interface with domain models
- `feature/users/presentation/UsersViewModel.kt` — manages user listing, search, filters, admin actions

**Modified files**:
- `feature/users/presentation/UsersScreen.kt` — replace hardcoded UserItem data with ViewModel-driven UI
- `navigation/AppNavHost.kt` — inject UsersViewModel

**Implementation details**:
- UsersApiService: GET /api/v1/admin/users (paginated, search, filters), GET /api/v1/admin/users/{id}, POST /api/v1/admin/users/{id}/credits, POST /api/v1/admin/users/{id}/quota, POST /api/v1/admin/users/{id}/suspend
- Domain models: User, UserSubscription, PaginatedUsers
- UsersRepository interface with listUsers, getUser, grantCredits, resetQuota, suspendAccount
- UsersRepositoryImpl with TokenRefreshManager wrapping
- UsersViewModel with StateFlow for users list, search query, filters, admin action state
- UsersScreen collects uiState, displays real user list with loading/empty/error states
- Admin actions (grant credits, reset quota, suspend) execute via ViewModel and refresh
- Follows OrdersListViewModel pattern for pagination

### ✓ Step 6: Order Details Navigation and UI Fixes
Fixed Order Details back navigation and UI overlap issues.

**Modified files**:
- `MainActivity.kt` — Fixed BackHandler to use exact fully qualified route matching for bottom-nav tabs
- `BottomNavigationItem.kt` — Changed `popUpTo<OverviewRoute>` to `popUpTo<LoginRoute>` so sub-screens are properly cleared when switching tabs
- `OrderDetailsScreen.kt` — Fixed OrderInfoCard layout (vertical Column instead of SpaceBetween Row), removed InfoRow maxLines truncation, fixed status badge overlap, updated preview
- `OrderComponents.kt` — Added maxLines=1 to order ID text in OrderSummaryCard

**Root causes**:
- Navigation: `popUpTo<OverviewRoute>` destroyed the Orders destination when switching bottom-nav tabs
- UI: `Row(SpaceBetween)` with long UUID order IDs caused overlap; `InfoRow` with `maxLines=2` truncated long addresses

### ✓ Step 7: Order Details Duplicate Navigation Fix
Fixed the remaining Order Details back-navigation bug where pressing Back showed the same Order Details screen again instead of returning to the Orders list.

**Modified files**:
- `OrdersListScreen.kt` — Replaced `LaunchedEffect(event)` with a two-stage event consumption pattern using `pendingEvent` that is cleared after navigation, preventing duplicate navigation on recomposition
- `AppNavHost.kt` — Added `launchSingleTop = true` to the Order Details navigate call as defense-in-depth

**New tests**:
- `OrdersListViewModelTest.kt` — Added 2 regression tests: same order tapped twice, different orders navigated sequentially

**Root cause**:
- `OrdersListViewModel._event` is a `MutableStateFlow<OrdersListEvent?>` that was never cleared after emission. When returning from Order Details, `OrdersListScreen` recomposed, `collectAsStateWithLifecycle` re-emitted the stale `NavigateToOrder` event, and `LaunchedEffect(event)` navigated again — creating a duplicate `OrderDetailsRoute` on the back stack.

**Navigation stack before fix**:
1. LoginRoute → 2. OverviewRoute → 3. MoreRoute → 4. OrdersRoute → 5. OrderDetailsRoute(orderA)
- Press Back: pops OrderDetailsRoute → OrdersRoute recomposes → stale event fires → 6. OrderDetailsRoute(orderA) created again
- Press Back: pops to OrderDetailsRoute(orderA) again instead of OrdersRoute

**Navigation stack after fix**:
1. LoginRoute → 2. OverviewRoute → 3. MoreRoute → 4. OrdersRoute → 5. OrderDetailsRoute(orderA)
- Press Back: pops OrderDetailsRoute → OrdersRoute recomposes → event already consumed, no duplicate navigation
- OrdersRoute displayed correctly

###   Step 5: Phase 4b — AI Usage, Discovery, System Health, Audit Logs
Implement remaining four administration modules with real backend integration.

**New files (AI Usage)**:
- `feature/aiusage/data/AiUsageApiService.kt`, `AiUsageRepositoryImpl.kt`, `AiUsageMapper.kt`, `AiUsageModule.kt`
- `feature/aiusage/domain/AiUsageRepository.kt` with domain models
- `feature/aiusage/presentation/AiUsageViewModel.kt`

**New files (Discovery)**:
- `feature/discovery/data/DiscoveryApiService.kt`, `DiscoveryRepositoryImpl.kt`, `DiscoveryMapper.kt`, `DiscoveryModule.kt`
- `feature/discovery/domain/DiscoveryRepository.kt` with domain models
- `feature/discovery/presentation/DiscoveryViewModel.kt`

**New files (System Health)**:
- `feature/systemhealth/data/SystemHealthApiService.kt`, `SystemHealthRepositoryImpl.kt`, `SystemHealthMapper.kt`, `SystemHealthModule.kt`
- `feature/systemhealth/domain/SystemHealthRepository.kt` with domain models
- `feature/systemhealth/presentation/SystemHealthViewModel.kt`

**New files (Audit Logs)**:
- `feature/auditlogs/data/AuditLogsApiService.kt`, `AuditLogsRepositoryImpl.kt`, `AuditLogsMapper.kt`, `AuditLogsModule.kt`
- `feature/auditlogs/domain/AuditLogsRepository.kt` with domain models
- `feature/auditlogs/presentation/AuditLogsViewModel.kt`

**Modified files**:
- `feature/aiusage/presentation/AiUsageScreen.kt` — connect to AiUsageViewModel
- `feature/discovery/presentation/DiscoveryScreen.kt` — connect to DiscoveryViewModel
- `feature/systemhealth/presentation/SystemHealthScreen.kt` — connect to SystemHealthViewModel
- `feature/auditlogs/presentation/AuditLogsScreen.kt` — connect to AuditLogsViewModel
- `navigation/AppNavHost.kt` — inject all new ViewModels

**Implementation details**:
- Each module follows the same pattern: API Service → Repository Impl (with TokenRefreshManager) → Mapper → Domain Repository → ViewModel → Screen
- AI Usage: GET /api/v1/admin/ai-usage, GET /api/v1/admin/ai-usage/history — budget envelope, provider distribution, quota safeguards, telemetry
- Discovery: GET /api/v1/admin/discovery, GET /api/v1/admin/discovery/trace/{id} — pipeline telemetry, search feed, trace inspector
- System Health: GET /api/v1/admin/health, GET /api/v1/admin/health/incidents — service health, deployment info, incident journal
- Audit Logs: GET /api/v1/admin/audit-logs (paginated, filters), POST /api/v1/admin/audit-logs/export — chronological stream, filtering, export
- Each screen replaces hardcoded data with ViewModel-driven UI
- Proper loading, empty, error, and retry states
- Pagination for large datasets (audit logs, discovery history)
- All API calls wrapped with TokenRefreshManager for automatic token refresh