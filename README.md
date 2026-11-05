# NearAI Admin Portal

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange.svg)](https://dagger.dev/hilt)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-purple.svg)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

**NearAI Admin Portal** is a native Android administration application for managing the NearAI AI-powered nearby discovery platform. Built with Kotlin, Jetpack Compose, and Material 3, it provides a hardened executive operations and compute cluster control plane for administrators.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Navigation](#navigation)
- [Design System](#design-system)
- [Security](#security)
- [Getting Started](#getting-started)
- [Build Configuration](#build-configuration)
- [Testing](#testing)
- [Backend Integration](#backend-integration)
- [CI/CD](#cicd)
- [Engineering Constitution](#engineering-constitution)
- [Development Guidelines](#development-guidelines)
- [License](#license)

---

## Features

### 1. Authentication & Security
- **Zero-trust authentication** with multi-factor authentication (MFA) support
- **6-digit TOTP grid** with auto-advance focus
- **Biometrics and FIDO2/NFC** hardware key integration (pending backend)
- **Encrypted token storage** using AES256_GCM with Android Keystore
- **Session management** with automatic expiry detection and redirect
- **Automatic token refresh** — expired access tokens are silently refreshed via `TokenRefreshManager` with single-flight locking
- **Real backend integration** via `AuthApiService` and `RealAuthRepository`

### 2. Overview Dashboard
- **Real backend metrics** from `GET /api/v1/admin/dashboard` — no fabricated data
- **6 metric cards**: Total Users, Total Products, Total Categories, Total Orders, Pending Orders, Completed Orders
- **Revenue by currency** with currency-aware formatting
- **Recent orders** section with order IDs, customer names, and status
- **Recent registrations** section with user emails and dates
- **Low stock alerts** showing variants below threshold
- **Pull-to-refresh** support
- **Fully ViewModel-driven** with loading, empty, and error states

### 3. User Management
- **Search and filter** users by name, email, ID, subscription tier, and status
- **User detail bottom sheet** with subscription info, AI credits balance gauge, and model invocation logs
- **Administrative actions**: Grant Credits, Reset Quota, Suspend Account
- **Flagged user indicators** and tier-based status pills
- ⏳ UI implemented; real backend integration pending

### 4. AI Usage & Credits
- **Honest unavailable state** — no fabricated metrics displayed in production
- **Proper feature architecture**: `AiUsageRepository`, `AiUsageRepositoryImpl`, `AiUsageViewModel`
- **ViewModel-driven UI** with LoadingScreen/ErrorState/EmptyState states
- **9 unit tests** confirming unavailable state is displayed honestly
- ⏳ Backend AI usage API pending; UI will render real data when available

### 5. Places & Discovery
- **Pipeline telemetry grid**: Discovery Volume, Places Spend, Avg Latency, Cache Hit Ratio
- **Real-time ingestion feed** with location-tagged queries and latency metrics
- **Trace inspector** with semantic NLP intent extraction and pipeline latency waterfall
- **Discovered places carousel** with image loading, ratings, and match scores
- **Action buttons**: Replay Trace, Inspect Raw JSON
- ⏳ UI implemented; real backend integration pending

### 6. Products & Inventory
- **Product catalog listing** connected to real backend via `ProductsListViewModel`
- **Product creation** via `ProductCreateViewModel` with real backend integration
- **Product editing** via `ProductEditViewModel` with `PATCH /api/v1/products/{id}` endpoint
- **Inventory management** via `InventoryViewModel` with real stock data (availability, receive, adjust, movements)
- **Category management** via `CategoriesListViewModel`
- **Category filter tabs**: All Items, Subscriptions, AI Credits, Add-ons
- **SKU cards** with status pills (Active, Reviewing, Deprecated)
- **Stock operations**: receive stock, adjust stock, view movement history
- **Floating action button** for quick product creation

### 7. Orders Management
- **Order listing** via `OrdersListViewModel` with real backend data, search, and status filtering
- **Order details** showing customer info, items, pricing, shipping address, payment, and status timeline
- **Order status updates** via backend API
- **Proper navigation** with back-stack management and state preservation
- **Real backend integration** via `OrderApiService` and `OrderRepositoryImpl`

### 8. System Health
- **Fleet SLA cards**: 30-day uptime percentage and last incident tracking
- **5 infrastructure service cards**: FastAPI, PostgreSQL, AI Inference, Google Places API, Redis
- **Deployment pipeline** information with commit hash and build version
- **Incident journal** with 24-hour history
- **System controls**: Clear Edge Cache, Download Telemetry Logs, Maintenance Mode
- ⏳ UI implemented; real backend integration pending

### 9. Audit Logs & Settings
- **Chronological audit stream** with event cards (credit grants, config changes, suspensions)
- **Security & key management**: masked API keys, copy/rotate actions, service account verification
- **Governance policies**: MFA toggle, stale telemetry threshold, session timeout
- **Cryptographic bundle export** with SHA-256 fingerprint verification
- ⏳ UI implemented; real backend integration pending

### 10. More Menu
- **Hub screen** providing access to Orders, Categories, Products, Inventory, System Health, Audit Logs, and Settings

### 11. Settings
- **Account settings** and sign-out with confirmation dialog
- **Session cleanup** on logout

---

## Architecture

The application follows **Clean Architecture** with **MVVM** presentation pattern and **unidirectional data flow**:

```
Presentation (Compose + ViewModels) → Domain (Use cases + Repository interfaces) ← Data (Repository implementations + API)
```

### Key Principles
- **Feature-based package organization** — each feature contains its own presentation, domain, and data layers
- **Dependency inversion** — presentation depends on domain interfaces, never directly on Retrofit or API DTOs
- **Immutable UI state** — each screen exposes a read-only `StateFlow` of an immutable state model
- **Constructor injection** — all dependencies injected via Hilt; no service locators or global singletons
- **Testability** — repository interfaces enable easy mocking for unit tests

### Layer Responsibilities

| Layer | Responsibilities |
|---|---|
| **Presentation** | Compose screens, ViewModels, UI state models, user event handling |
| **Domain** | Business models, repository interfaces, use cases, domain validation |
| **Data** | Repository implementations, API services, DTOs, DTO-to-domain mapping |

---

## Technology Stack

| Category | Technology | Version |
|---|---|---|
| **Language** | Kotlin | 2.2.10 |
| **UI Framework** | Jetpack Compose + Material 3 | — |
| **Architecture** | MVVM + Clean Architecture | — |
| **Dependency Injection** | Hilt + KSP | 2.59.2 / 2.2.10-2.0.2 |
| **Networking** | Retrofit + OkHttp | 2.11 / 4.12 |
| **Serialization** | Kotlinx Serialization | 1.8 |
| **Concurrency** | Kotlin Coroutines + Flow | 1.10.1 |
| **Navigation** | Navigation Compose (type-safe) | 2.9 |
| **Security** | AndroidX Security Crypto | 1.1 |
| **Image Loading** | Coil | 2.7 |
| **Testing** | JUnit + MockK + Turbine | — |
| **Build System** | AGP + Gradle Kotlin DSL | 9.4.1 |
| **Min SDK** | Android 7.0 (API 24) | — |
| **Target SDK** | Android 13+ (API 37) | — |

---

## Project Structure

```
com.nearai.admin/
├── NearAiAdminApplication.kt              # @HiltAndroidApp entry point
├── MainActivity.kt                        # @AndroidEntryPoint, BackHandler for system back
├── navigation/
│   ├── AppDestination.kt                  # 18 @Serializable route objects
│   ├── AppNavHost.kt                      # NavGraph with session-gated destinations
│   └── BottomNavigationItem.kt            # 5-tab bottom nav with state preservation
├── core/
│   ├── common/
│   │   ├── Result.kt                      # Sealed interface: Success/Error/Loading
│   │   └── AppError.kt                    # Typed error categories (UnauthorizedError, etc.)
│   ├── designsystem/
│   │   ├── theme/
│   │   │   ├── Color.kt                   # 30+ named theme colors (all dynamic)
│   │   │   ├── Theme.kt                   # Material 3 light/dark schemes
│   │   │   └── Type.kt                    # Typography hierarchy
│   │   └── components/                    # 7 focused component files
│   │       ├── Statuses.kt                # StatusIndicator, StatusPill, ActivityStatus
│   │       ├── Inputs.kt                  # SearchBar, FilterChips
│   │       ├── Cards.kt                   # KpiCard, ActivityStreamCard
│   │       ├── Charts.kt                  # BudgetProgressBar, SimpleBarChart
│   │       ├── States.kt                  # LoadingScreen, EmptyState, ErrorState
│   │       ├── Dialogs.kt                 # ConfirmationDialog
│   │       └── Sections.kt                # SectionHeader
│   ├── network/
│   │   ├── NetworkModule.kt              # Hilt @Module: Retrofit + OkHttp + auth interceptor
│   │   └── ApiErrorParser.kt             # HttpException → AppError mapping (401, 403, etc.)
│   └── security/
│       ├── TokenStorage.kt               # EncryptedSharedPreferences for tokens
│       ├── TokenRefreshManager.kt        # Single-flight token refresh with retry
│       └── SessionManager.kt             # Session lifecycle StateFlow
└── feature/
    ├── auth/                              # ✅ Real backend integration
    │   ├── domain/
    │   │   └── AuthRepository.kt          # Repository interface + domain models
    │   ├── data/
    │   │   ├── AuthApiService.kt          # Retrofit service: sign-in, MFA, refresh
    │   │   ├── RealAuthRepository.kt      # Real implementation with TokenRefreshManager
    │   │   └── AuthModule.kt              # Hilt @Binds module
    │   └── presentation/
    │       ├── AuthViewModel.kt           # @HiltViewModel, StateFlow<AuthUiState>
    │       └── AdminSignInScreen.kt       # Zero-trust auth screen
    ├── dashboard/                         # ✅ Real backend integration
    │   ├── domain/
    │   │   └── DashboardRepository.kt     # Repository interface + domain models
    │   ├── data/
    │   │   ├── DashboardApiService.kt     # Retrofit service: GET /api/v1/admin/dashboard
    │   │   ├── DashboardRepositoryImpl.kt # Real implementation with TokenRefreshManager
    │   │   ├── DashboardMapper.kt         # DTO → domain mapping
    │   │   └── DashboardModule.kt         # Hilt @Binds module
    │   └── presentation/
    │       ├── DashboardViewModel.kt      # @HiltViewModel, StateFlow<DashboardUiState>
    │       └── DashboardScreen.kt         # Real metrics, orders, registrations, stock alerts
    ├── catalog/                           # ✅ Real backend integration
    │   ├── domain/
    │   │   ├── CatalogRepository.kt       # Repository interface (products, variants, stock)
    │   │   └── CatalogStatus.kt           # Product status enum
    │   ├── data/
    │   │   ├── CatalogApiService.kt       # Retrofit service: product CRUD, variants, stock
    │   │   ├── CatalogRepositoryImpl.kt   # Real implementation with TokenRefreshManager
    │   │   ├── CatalogMapper.kt           # DTO → domain mapping
    │   │   └── CatalogModule.kt           # Hilt @Binds module
    │   └── presentation/
    │       ├── ProductsListViewModel.kt   # Product listing with search and filters
    │       ├── ProductCreateViewModel.kt  # Product creation with validation
    │       ├── CategoriesListViewModel.kt # Category listing
    │       └── ProductDetailsViewModel.kt # Product details
    ├── inventory/                         # ✅ Real backend integration
    │   └── presentation/
    │       ├── InventoryViewModel.kt      # Product+variant+stock aggregation, stock operations
    │       ├── InventoryScreen.kt         # Real inventory with search, filter, stock actions
    │       ├── ProductEditViewModel.kt    # Product editing with validation and save
    │       └── ProductEditScreen.kt       # Product form connected to real backend
    ├── orders/                            # ✅ Real backend integration
    │   ├── domain/
    │   │   ├── OrderRepository.kt         # Repository interface + domain models
    │   │   └── OrderStatus.kt             # Order and fulfillment status enums
    │   ├── data/
    │   │   ├── OrderApiService.kt         # Retrofit service: order listing, details, status
    │   │   ├── OrderRepositoryImpl.kt     # Real implementation with TokenRefreshManager
    │   │   ├── OrderMapper.kt             # DTO → domain mapping
    │   │   └── OrderModule.kt             # Hilt @Binds module
    │   └── presentation/
    │       ├── OrdersListViewModel.kt     # Order listing with search and status filter
    │       ├── OrdersListScreen.kt        # Order list with event-based navigation
    │       ├── OrderDetailsViewModel.kt   # Order details loading and status updates
    │       ├── OrderDetailsScreen.kt      # Full order details with timeline
    │       └── OrderComponents.kt         # Reusable order UI components
    ├── users/                             # ⏳ UI only, backend integration pending
    │   └── presentation/
    │       └── UsersScreen.kt             # Search, filter, user list, detail sheet
    ├── aiusage/                           # ⏳ Backend pending, proper architecture in place
    │   ├── domain/
    │   │   └── AiUsageRepository.kt       # Repository interface + domain models
    │   ├── data/
    │   │   ├── AiUsageRepositoryImpl.kt   # Returns honest unavailable state
    │   │   └── AiUsageModule.kt           # Hilt @Binds module
    │   └── presentation/
    │       ├── AiUsageViewModel.kt        # @HiltViewModel, StateFlow<AiUsageUiState>
    │       └── AiUsageScreen.kt           # Loading/Error/Empty states, no fabricated data
    ├── discovery/                         # ⏳ UI only, backend integration pending
    │   └── presentation/
    │       └── DiscoveryScreen.kt         # Pipeline telemetry, trace inspector
    ├── systemhealth/                      # ⏳ UI only, backend integration pending
    │   └── presentation/
    │       └── SystemHealthScreen.kt      # Fleet SLA, service cards, incidents
    ├── auditlogs/                         # ⏳ UI only, backend integration pending
    │   └── presentation/
    │       └── AuditLogsScreen.kt         # Audit stream, key management, policies
    ├── more/
    │   └── presentation/
    │       └── MoreScreen.kt              # Menu hub: Orders, Catalog, Inventory, Health, Audit
    └── settings/
        └── presentation/
            └── SettingsScreen.kt          # Sign out with confirmation
```

---

## Navigation

### 18 Routes

| Route | Screen | Access |
|---|---|---|
| `LoginRoute` | AdminSignInScreen | Start destination, session expiry redirect |
| `OverviewRoute` | DashboardScreen | Bottom nav tab 1 |
| `UsersRoute` | UsersScreen | Bottom nav tab 2 |
| `AiUsageRoute` | AiUsageScreen | Bottom nav tab 3 |
| `DiscoveryRoute` | DiscoveryScreen | Bottom nav tab 4 |
| `MoreRoute` | MoreScreen | Bottom nav tab 5 |
| `OrdersRoute` | OrdersListScreen | From More menu |
| `OrderDetailsRoute` | OrderDetailsScreen | From Orders list |
| `CategoriesRoute` | CategoriesListScreen | From More menu |
| `ProductsRoute` | ProductsListScreen | From More menu |
| `ProductDetailsRoute` | ProductDetailsScreen | From Products list |
| `ProductCreateRoute` | ProductCreateScreen | From Products list |
| `InventoryRoute` | InventoryScreen | From More menu |
| `ProductEditRoute` | ProductEditScreen | From Inventory (Add/Edit SKU) |
| `SystemHealthRoute` | SystemHealthScreen | From Dashboard or More menu |
| `AuditLogsRoute` | AuditLogsScreen | From Dashboard or More menu |
| `SettingsRoute` | SettingsScreen | From More menu |

### 5 Bottom Navigation Tabs

| Tab | Icon | Route | Badge |
|---|---|---|---|
| Overview | Dashboard | OverviewRoute | — |
| Users | People | UsersRoute | — |
| AI Compute | Memory | AiUsageRoute | Indigo alert dot (71% budget) |
| Discovery | Explore | DiscoveryRoute | — |
| More | MoreVert | MoreRoute | — |

### Navigation Flow

```
Login → (authenticate) → Overview (Dashboard)
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
                 Users         AI Compute       Discovery
                    │               │               │
                    └───────────────┼───────────────┘
                                    ▼
                                  More
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
         Orders              Categories            Products
              │                     │                     │
              ▼                     ▼                     ▼
       Order Details          (standalone)         Product Details
                                                                       │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
         Inventory            System Health         Audit Logs
              │
              ▼
       Product Edit
```

### Navigation Behavior
- **Type-safe navigation** using `@Serializable` route objects with Navigation Compose 2.9
- **State preservation** — bottom nav tabs use `saveState`/`restoreState` to preserve scroll position
- **Back stack management** — `popUpTo<LoginRoute>` clears sub-screens when switching tabs
- **System back button** — `BackHandler` in `MainActivity` pops the navigation back stack for sub-screens
- **Single-top navigation** — `launchSingleTop = true` prevents duplicate destinations (e.g., Order Details)
- **Event consumption** — navigation events use two-stage pattern to prevent duplicate navigation on recomposition
- **Session-gated access** — unauthenticated users are redirected to Login; session expiry triggers immediate redirect
- **Bottom nav hidden** on authentication screens

---

## Design System

### Theme Colors

All colors are defined centrally in `Color.kt` with 30+ named colors organized by category:

- **Brand**: Navy (#1E1B4B), Indigo (#6366F1), IndigoLight, IndigoContainer
- **Backgrounds**: BackgroundLight/Dark, SurfaceLight/Dark, SurfaceVariantLight/Dark
- **Semantic**: Emerald (Success), Amber (Warning), Rose (Error), Purple (Info) — each with light/dark shades
- **Borders**: BorderSubtle, BorderDark, OutlineLight/Dark, OutlineVariantLight/Dark
- **Text**: TextPrimaryLight/Dark, TextSecondaryLight/Dark
- **Special Purpose**: DarkClusterBg, PrimaryContainerLight/Dark, SecondaryContainerLight

No hardcoded `Color(0xFF...)` values exist in feature screens or components — all colors reference the theme.

### Component Library

7 focused component files in `core/designsystem/components/`:

| File | Components |
|---|---|
| **Statuses.kt** | `StatusIndicator`, `StatusPill`, `StatusType` enum, `ActivityStatus` enum |
| **Inputs.kt** | `SearchBar`, `FilterChips`, `FilterChipItem` |
| **Cards.kt** | `KpiCard` (with trend), `ActivityStreamCard` |
| **Charts.kt** | `BudgetProgressBar` (with thresholds), `SimpleBarChart` |
| **States.kt** | `LoadingScreen`, `EmptyState`, `ErrorState` |
| **Dialogs.kt** | `ConfirmationDialog` |
| **Sections.kt** | `SectionHeader` |

### Theme Support
- Light and dark Material 3 color schemes
- Dynamic color support on Android 12+
- Edge-to-edge system insets
- 48dp minimum touch targets for accessibility
- All previews wrapped in `NearAiAdminTheme`

---

## Security

### Token Storage
- **EncryptedSharedPreferences** with AES256_GCM encryption backed by Android Keystore
- Access tokens and refresh tokens are never stored in plaintext
- Tokens are cleared on logout

### Automatic Token Refresh
- **TokenRefreshManager** handles expired access tokens transparently
- When an API call returns HTTP 401, the manager attempts to refresh the access token using the stored refresh token
- **Single-flight refresh** — concurrent 401 responses share one refresh request via `Mutex`
- **Single retry** — the original request is retried exactly once with the new access token
- **Session cleanup** — if refresh fails (expired/revoked refresh token), the session is cleared and the user is redirected to login
- **No refresh on 403** — authorization failures do not trigger token refresh
- **No infinite loops** — each request retries at most once

### Session Management
- **SessionManager** singleton manages session lifecycle via `StateFlow<SessionState>`
- States: `Unauthenticated` → `Authenticating` → `Authenticated` / `SessionExpired` / `NotAdmin`
- Automatic session restoration on app startup
- Automatic redirect to login on session expiry

### Authentication Flow
1. Submit credentials over HTTPS
2. Handle MFA challenge if required
3. Verify MFA code through backend
4. Store session credentials securely
5. Restore and validate session on app start
6. Automatic token refresh on 401 responses
7. Clear local session data on logout or refresh failure

### Error Handling
- **ApiErrorParser** maps HTTP errors to typed `AppError` categories:
  - HTTP 401 → `AppError.UnauthorizedError` (triggers token refresh)
  - HTTP 403 → `AppError.ForbiddenError` (admin-only endpoints)
  - HTTP 404 → `AppError.NotFoundError`
  - HTTP 409 → `AppError.ConflictError`
  - HTTP 422 → `AppError.ValidationError`
  - HTTP 5xx → `AppError.ServerError`
  - Network errors → `AppError.NetworkError`
- All repository implementations use `ApiErrorParser.parse()` for consistent error mapping

### Security Rules
- No passwords or tokens stored in ordinary preferences
- No hardcoded credentials or API keys
- No logging of sensitive data (tokens, passwords, MFA codes)
- BiometricPrompt used only for local re-authentication, not server authorization
- Backend enforces all permissions; client-side checks are UI conveniences only

---

## Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 17 or later
- Android SDK 37 (API 37)
- Gradle 8.13

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yousefbeiruty/NearAiAdminPortalApp.git
   cd NearAiAdminPortal
   ```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run with coverage
./gradlew jacocoTestDebugUnitTestReport
```

---

## Build Configuration

### Build Types
| Build Type | API Base URL | Use Case |
|---|---|---|
| **Debug** | `https://api-dev.nearai.dev/` | Development and testing |
| **Release** | `https://api.nearai.dev/` | Production |

### Dependencies
Key dependencies are managed via Gradle Version Catalog (`gradle/libs.versions.toml`):
- Hilt 2.59.2 with KSP 2.2.10-2.0.2
- Retrofit 2.11, OkHttp 4.12
- Navigation Compose 2.9
- Kotlinx Serialization 1.8
- Coroutines 1.10.1
- Coil 2.7 for image loading
- MockK 1.13.16, Turbine 1.2.0 for testing

---

## Testing

### Current Test Coverage

| Test File | Tests | Coverage |
|---|---|---|
| **AuthViewModelTest.kt** | 14 | Initial state, email/password validation, sign-in success/error, MFA flow, TOTP verification, session expiry, navigation events, dev sign-in |
| **DashboardViewModelTest.kt** | 17 | Loading, error handling, retry, refresh, currency formatting, empty data, all metric types |
| **DashboardMapperTest.kt** | 7 | DTO-to-domain mapping for revenue, orders, registrations, stock alerts |
| **DashboardRepositoryImplTest.kt** | 10 | API call, 401 → refresh + retry, 401 with failed refresh → session cleared, 403 → no refresh, 500 → no refresh, network error |
| **OrdersListViewModelTest.kt** | 12 | Order listing, search, status filter, pagination, error handling, duplicate navigation regression |
| **OrderDetailsViewModelTest.kt** | 10 | Order loading, error handling, status update, refresh |
| **OrderMapperTest.kt** | 8 | DTO-to-domain mapping for orders, items, status history |
| **CatalogMapperTest.kt** | 8 | Product, variant, stock balance, stock movement, pagination mapping |
| **ProductDetailsViewModelTest.kt** | 6 | Product loading, error handling |
| **AiUsageViewModelTest.kt** | 9 | Unavailable feature state, error display, retry, clear error |
| **TokenRefreshManagerTest.kt** | 8 | Single-flight refresh, retry logic, session cleanup, auth endpoint exclusion |
| **ApiErrorParserTest.kt** | 6 | HTTP 401, 403, 404, 422, 500, network error mapping |
| **ProductImageDtoSerializationTest.kt** | 6 | Image DTO serialization and deserialization |

**Total: 315 unit tests, all passing.**

### Testing Approach
- **ViewModel tests**: MockK for repository mocking, `runTest` with `UnconfinedTestDispatcher`, Turbine for StateFlow verification
- **Repository tests**: MockK for API service mocking, verify TokenRefreshManager integration and ApiErrorParser usage
- **Mapper tests**: Pure function tests verifying DTO → domain mapping
- **Test dispatchers** used for coroutine control
- **Regression tests** for navigation bugs (duplicate Order Details, stale events)

---

## Backend Integration

### Integration Status

| Module | Status | API Service | Repository | ViewModel |
|---|---|---|---|---|
| **Authentication** | ✅ Connected | `AuthApiService` | `RealAuthRepository` | `AuthViewModel` |
| **Dashboard** | ✅ Connected | `DashboardApiService` | `DashboardRepositoryImpl` | `DashboardViewModel` |
| **Catalog (Products)** | ✅ Connected | `CatalogApiService` | `CatalogRepositoryImpl` | `ProductsListViewModel`, `ProductCreateViewModel` |
| **Inventory** | ✅ Connected | `CatalogApiService` | `CatalogRepositoryImpl` | `InventoryViewModel` |
| **Product Editing** | ✅ Connected | `CatalogApiService` | `CatalogRepositoryImpl` | `ProductEditViewModel` |
| **Orders** | ✅ Connected | `OrderApiService` | `OrderRepositoryImpl` | `OrdersListViewModel`, `OrderDetailsViewModel` |
| **Users** | ⏳ UI only | — | — | — |
| **AI Usage** | ⏳ Backend pending | — | `AiUsageRepositoryImpl` | `AiUsageViewModel` |
| **Discovery** | ⏳ UI only | — | — | — |
| **System Health** | ⏳ UI only | — | — | — |
| **Audit Logs** | ⏳ UI only | — | — | — |

### Implemented Backend Endpoints

#### Authentication
| Endpoint | Method | Status |
|---|---|---|
| `/auth/sign-in` | POST | ✅ Connected |
| `/auth/verify-mfa` | POST | ✅ Connected |
| `/auth/refresh` | POST | ✅ Connected (via TokenRefreshManager) |

#### Dashboard
| Endpoint | Method | Status |
|---|---|---|
| `/admin/dashboard` | GET | ✅ Connected — returns metrics, revenue, orders, registrations, stock alerts |

#### Catalog (Products, Variants, Stock)
| Endpoint | Method | Status |
|---|---|---|
| `/admin/products` | GET | ✅ Connected — paginated listing with search and filters |
| `/admin/products` | POST | ✅ Connected — create product |
| `/admin/products/{id}` | GET | ✅ Connected — product details |
| `/admin/products/{id}` | PATCH | ✅ Connected — update product |
| `/admin/products/{id}` | DELETE | ⏳ Pending backend |
| `/admin/products/{id}/variants` | GET | ✅ Connected — list variants |
| `/admin/products/{id}/variants` | POST | ✅ Connected — create variant |
| `/admin/variants/{id}/stock` | GET | ✅ Connected — stock availability |
| `/admin/variants/{id}/stock/receive` | POST | ✅ Connected — receive stock |
| `/admin/variants/{id}/stock/adjust` | POST | ✅ Connected — adjust stock |
| `/admin/variants/{id}/movements` | GET | ✅ Connected — stock movement history |
| `/admin/categories` | GET | ✅ Connected — category listing |
| `/admin/categories` | POST | ✅ Connected — create category |

#### Orders
| Endpoint | Method | Status |
|---|---|---|
| `/admin/orders` | GET | ✅ Connected — paginated listing with search and status filter |
| `/admin/orders/{id}` | GET | ✅ Connected — order details with items and status history |
| `/admin/orders/{id}/status` | PATCH | ✅ Connected — update order status |

### Pending Backend Endpoints

#### Users
| Endpoint | Method | Description |
|---|---|---|
| `/admin/users` | GET | Paginated user list with search and filters |
| `/admin/users/{id}` | GET | User details, subscription, credits |
| `/admin/users/{id}/credits` | POST | Grant bonus credits |
| `/admin/users/{id}/quota` | POST | Reset daily quota |
| `/admin/users/{id}/suspend` | POST | Suspend/restore account |

#### AI Usage
| Endpoint | Method | Description |
|---|---|---|
| `/admin/ai-usage` | GET | Spending, provider distribution, quota status |
| `/admin/ai-usage/history` | GET | Usage history with pagination |

#### Discovery
| Endpoint | Method | Description |
|---|---|---|
| `/admin/discovery` | GET | Discovery request history with filters |
| `/admin/discovery/trace/{id}` | GET | Trace details with pipeline breakdown |

#### System Health
| Endpoint | Method | Description |
|---|---|---|
| `/admin/health` | GET | Service health status and metrics |
| `/admin/health/incidents` | GET | Incident history |

#### Audit Logs
| Endpoint | Method | Description |
|---|---|---|
| `/admin/audit-logs` | GET | Paginated audit history with filters |
| `/admin/audit-logs/export` | POST | Export cryptographic audit bundle |

---

## CI/CD

### GitHub Actions

The project uses GitHub Actions for automated quality gates on pull requests and pushes to `main`.

**Workflow**: `.github/workflows/build.yml`

| Job | Command | Purpose |
|---|---|---|
| **Gradle Wrapper Validation** | `gradlew --version` | Verify wrapper integrity |
| **Compile** | `compileDebugKotlin` | Kotlin compilation check |
| **Tests** | `testDebugUnitTest` | Run all unit tests (315 tests) |
| **Lint** | `lintDebug` | Android lint analysis |

**Features**:
- Triggered on pull requests and pushes to `main`
- Dependency caching for faster builds
- Test results and lint results uploaded as artifacts
- No credentials or secrets exposed

---

## Engineering Constitution

The project follows permanent engineering standards documented in version-controlled files:

| Document | Purpose |
|---|---|
| [`AGENTS.md`](AGENTS.md) | Engineering constitution: Clean Architecture, SOLID principles, Android-specific rules, testing mandates, mandatory workflow |
| [`PR_CHECKLIST.md`](PR_CHECKLIST.md) | Reusable PR checklist covering 13 categories (architecture, SOLID, code quality, string resources, error handling, security, testing, etc.) |
| [`DEFINITION_OF_DONE.md`](DEFINITION_OF_DONE.md) | 13 mandatory criteria every implementation must satisfy |
| [`ENGINEERING_CONSTITUTION_REPORT.md`](ENGINEERING_CONSTITUTION_REPORT.md) | Audit report documenting completed items, remaining gaps, and build status |

These documents are loaded automatically by coding agents and must be followed for every implementation, bug fix, refactoring, and code review.

---

## Development Guidelines

### Code Style
- Follow existing project conventions for indentation, naming, and import order
- Use KDoc comments for public APIs
- Keep composables stateless where possible, with state and callbacks supplied by ViewModel
- One-time navigation and user feedback via appropriate event patterns

### Architecture Rules
- No business logic in composables
- No network calls in composables
- No `GlobalScope` usage
- Use `viewModelScope` for ViewModel coroutines
- Use `collectAsStateWithLifecycle` for observing UI state
- Keep API DTOs separate from domain models

### Security Rules
- Never store plaintext passwords
- Never log credentials, tokens, or sensitive data
- Never hardcode API keys or secrets
- Use HTTPS for all production communication
- Treat client-side permissions as UI conveniences only

### Testing Rules
- Add tests for ViewModel state transitions
- Test loading, success, empty, and error states
- Test domain validation and business rules
- Test DTO mapping
- Test session expiry and logout behavior
- Use fakes or mocks for repository behavior

---

## Current Status

### Completed
- ✅ Full Clean Architecture + MVVM foundation
- ✅ Hilt DI with proper `@Module` and `@Binds` configuration
- ✅ Type-safe navigation with 18 routes and 5 bottom nav tabs
- ✅ All feature screens implemented with proper loading, empty, and error states
- ✅ All colors dynamic — 30+ named theme colors, zero hardcoded values
- ✅ Components reorganized into 7 focused files
- ✅ **Authentication**: Real backend integration with `AuthApiService`, `RealAuthRepository`, 14 tests
- ✅ **Dashboard**: Real backend integration via `GET /api/v1/admin/dashboard` with metrics, revenue, orders, registrations, stock alerts; 34 tests
- ✅ **Catalog (Products)**: Real backend integration with product CRUD, variants, categories; `CatalogApiService`, `CatalogRepositoryImpl`, `CatalogMapper`
- ✅ **Inventory**: Real stock data via `InventoryViewModel` with search, filtering, receive/adjust stock, movement history
- ✅ **Product Editing**: Real backend integration via `PATCH /api/v1/products/{id}` with `ProductEditViewModel`
- ✅ **Orders**: Full module with listing, details, status updates; `OrderApiService`, `OrderRepositoryImpl`, `OrderMapper`, 30 tests
- ✅ **AI Usage**: Proper feature architecture with `AiUsageRepository`, `AiUsageRepositoryImpl`, `AiUsageViewModel`; no fabricated data; 9 tests
- ✅ **Automatic token refresh** via `TokenRefreshManager` with single-flight locking and retry
- ✅ **Error handling** via `ApiErrorParser` mapping HTTP errors to typed `AppError` categories
- ✅ **Navigation fixes**: Proper back-stack management, state preservation, duplicate navigation prevention
- ✅ **String resources**: Hardcoded strings extracted from 7 screens into feature-specific `strings_*.xml` files
- ✅ **CI/CD**: GitHub Actions workflow with Gradle wrapper validation, compilation, tests, and lint
- ✅ **Engineering constitution**: `AGENTS.md`, `PR_CHECKLIST.md`, `DEFINITION_OF_DONE.md` for permanent standards
- ✅ Secure token storage with AES256_GCM encryption
- ✅ Session management with auto-redirect on expiry
- ✅ Material 3 design system with NearAI branding
- ✅ Build compiles, all 315 tests pass

### Pending (requires backend API contracts)
- ⏳ **Users Management**: Real backend integration (API service, repository, ViewModel)
- ⏳ **AI Usage**: Backend API integration (restore full UI with real data from ViewModel)
- ⏳ **Discovery**: Real backend integration (API service, repository, ViewModel)
- ⏳ **System Health**: Real backend integration (API service, repository, ViewModel)
- ⏳ **Audit Logs**: Real backend integration (API service, repository, ViewModel)
- ⏳ Product delete endpoint on backend
- ⏳ BiometricPrompt integration for local re-authentication
- ⏳ FIDO2/NFC hardware key integration
- ⏳ Comprehensive unit tests for remaining ViewModels
- ⏳ Remaining hardcoded string extraction in lower-priority screens

---

## Customer Android — Discovery Event Tracking

The companion Customer Android application (`NearAiDescovery`) includes a **Discovery Event Tracking** module that records customer behavioral events and uploads them to the FastAPI backend.

### Event Types Instrumented

| Event | Trigger | Metadata |
|---|---|---|
| `search_performed` | Customer submits a search query | query, category_id?, min_price?, max_price? |
| `search_results_returned` | Search results loaded successfully | query, result_count, latency_ms, has_products |
| `product_viewed` | Product detail screen opened (deduplicated) | product_id, variant_id?, referrer? |
| `category_viewed` | Category browsing screen opened (deduplicated) | category_id, filters_applied? |
| `product_added_to_cart` | Cart item added successfully via API | product_id, variant_id, quantity, price_amount, price_currency |
| `product_removed_from_cart` | Cart item removed successfully via API | product_id, variant_id, quantity |
| `checkout_started` | Customer initiates order placement | cart_total, item_count, currency |

`purchase_completed` is **not** client-submitted; it is derived from verified order and payment lifecycle changes on the backend.

### Architecture

The `feature-discovery` module follows the existing Clean Architecture + MVVM patterns:

- **Domain**: `DiscoveryTracker` (central entry point), `DiscoveryEventRepository` interface, `DiscoverySessionManager` interface, typed event DTOs
- **Data**: Room database for local event queue, Retrofit API service for `POST /api/v1/events/` and `POST /api/v1/events/batch`, WorkManager worker for periodic batch upload
- **DI**: Hilt module providing all dependencies and WorkManager scheduler
- **ViewModel Integration**: Search, Product Detail, Catalog, Cart, and Checkout ViewModels inject `DiscoveryTracker`

### Session Management

- UUID session ID created once via DataStore, persists across app restarts
- Same session reused when user authenticates; backend handles guest→auth transition via JWT
- No customer identity is supplied or overridden by the client

### Retry & Batching Strategy

- Batch upload via WorkManager every 5 minutes, max 50 events per batch
- Bounded exponential backoff: 1s, 2s, 4s, 8s, 16s, 30s (max), 5 max retries
- Events removed only after backend confirms acceptance
- Network failures retry; 4xx validation errors drop events; 5xx server errors retry
- Analytics failures never block customer-facing actions

### Privacy & Security

- No PII (emails, phone numbers, addresses, JWTs) in any event metadata
- All event metadata validated against backend schemas
- Analytics failures never prevent customers from searching, browsing, or checking out

---

## License

Private — NearAI Admin Portal is proprietary software for internal administrative use only.

---

## Contact

For questions or support, contact the NearAI development team.
