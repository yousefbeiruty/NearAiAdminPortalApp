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
- **Development-only fake authentication** clearly marked for production replacement

### 2. Overview Dashboard
- **6-KPI metrics grid**: Total Users, DAU, AI Requests, AI Spend, Places Found, Est. Revenue
- **14-day user velocity Bezier curve** with Canvas-rendered gradient fill
- **24-hour AI inference load histogram** with retry spike highlighting
- **Monthly compute envelope** progress gauge with budget thresholds
- **Live activity stream** with real-time event cards
- **Cluster fleet status banner** with navigation to System Health
- **Fully ViewModel-driven** — no hardcoded data in production composables

### 3. User Management
- **Search and filter** users by name, email, ID, subscription tier, and status
- **User detail bottom sheet** with subscription info, AI credits balance gauge, and model invocation logs
- **Administrative actions**: Grant Credits, Reset Quota, Suspend Account
- **Flagged user indicators** and tier-based status pills

### 4. AI Usage & Credits
- **Budget envelope card** with 50%/75%/90% threshold markers
- **Provider/model distribution** breakdown (OpenAI, Anthropic, Self-Hosted)
- **Quota safeguards**: Free Tier, Burst Rate Limits, Circuit Breaker
- **Live ingestion telemetry** with 429 rate limit status

### 5. Places & Discovery
- **Pipeline telemetry grid**: Discovery Volume, Places Spend, Avg Latency, Cache Hit Ratio
- **Real-time ingestion feed** with location-tagged queries and latency metrics
- **Trace inspector** with semantic NLP intent extraction and pipeline latency waterfall
- **Discovered places carousel** with image loading, ratings, and match scores
- **Action buttons**: Replay Trace, Inspect Raw JSON

### 6. Products & Inventory
- **Product catalog** with SKU cards, status pills (Active, Reviewing, Deprecated)
- **Category filter tabs**: All Items, Subscriptions, AI Credits, Add-ons
- **Product creation/editing form** with billing classification, pricing, quota slider, and model routing
- **Lifecycle operations**: Archive SKU, Delete SKU with confirmation
- **Floating action button** for quick product creation

### 7. System Health
- **Fleet SLA cards**: 30-day uptime percentage and last incident tracking
- **5 infrastructure service cards**: FastAPI, PostgreSQL, AI Inference, Google Places API, Redis
- **Deployment pipeline** information with commit hash and build version
- **Incident journal** with 24-hour history
- **System controls**: Clear Edge Cache, Download Telemetry Logs, Maintenance Mode

### 8. Audit Logs & Settings
- **Chronological audit stream** with event cards (credit grants, config changes, suspensions)
- **Security & key management**: masked API keys, copy/rotate actions, service account verification
- **Governance policies**: MFA toggle, stale telemetry threshold, session timeout
- **Cryptographic bundle export** with SHA-256 fingerprint verification

### 9. More Menu
- **Hub screen** providing access to Products & Inventory, System Health, Audit Logs, and Settings

### 10. Settings
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
├── MainActivity.kt                        # @AndroidEntryPoint, navigation host
├── navigation/
│   ├── AppDestination.kt                  # 11 @Serializable route objects
│   ├── AppNavHost.kt                      # NavGraph with session-gated destinations
│   └── BottomNavigationItem.kt            # 5-tab bottom nav definition
├── core/
│   ├── common/
│   │   ├── Result.kt                      # Sealed interface: Success/Error/Loading
│   │   └── AppError.kt                    # Typed error categories
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
│   │   └── NetworkModule.kt              # Hilt @Module: Retrofit + OkHttp + auth interceptor
│   └── security/
│       ├── TokenStorage.kt               # EncryptedSharedPreferences for tokens
│       └── SessionManager.kt             # Session lifecycle StateFlow
└── feature/
    ├── auth/
    │   ├── domain/
    │   │   └── AuthRepository.kt          # Repository interface + domain models
    │   ├── data/
    │   │   ├── AuthApiService.kt          # Retrofit service interface
    │   │   ├── FakeAuthRepository.kt      # ⚠️ Development-only fake
    │   │   └── AuthModule.kt              # Hilt @Binds module
    │   └── presentation/
    │       ├── AuthViewModel.kt           # @HiltViewModel, StateFlow<AuthUiState>
    │       └── AdminSignInScreen.kt       # Zero-trust auth screen
    ├── dashboard/
    │   ├── domain/
    │   │   └── DashboardRepository.kt     # Repository interface + domain models
    │   ├── data/
    │   │   ├── FakeDashboardRepository.kt # ⚠️ Development-only fake
    │   │   └── DashboardModule.kt         # Hilt @Binds module
    │   └── presentation/
    │       ├── DashboardViewModel.kt      # @HiltViewModel, StateFlow<DashboardUiState>
    │       └── DashboardScreen.kt         # KPI grid, charts, activity stream
    ├── users/
    │   └── presentation/
    │       └── UsersScreen.kt             # Search, filter, user list, detail sheet
    ├── aiusage/
    │   └── presentation/
    │       └── AiUsageScreen.kt           # Budget envelope, provider distribution
    ├── discovery/
    │   └── presentation/
    │       └── DiscoveryScreen.kt         # Pipeline telemetry, trace inspector
    ├── inventory/
    │   └── presentation/
    │       ├── InventoryScreen.kt         # Product catalog, SKU cards
    │       └── ProductEditScreen.kt       # Product form: billing, pricing, quota
    ├── systemhealth/
    │   └── presentation/
    │       └── SystemHealthScreen.kt      # Fleet SLA, service cards, incidents
    ├── auditlogs/
    │   └── presentation/
    │       └── AuditLogsScreen.kt         # Audit stream, key management, policies
    ├── more/
    │   └── presentation/
    │       └── MoreScreen.kt              # Menu hub: Inventory, Health, Audit, Settings
    └── settings/
        └── presentation/
            └── SettingsScreen.kt          # Sign out with confirmation
```

---

## Navigation

### 11 Routes

| Route | Screen | Access |
|---|---|---|
| `LoginRoute` | AdminSignInScreen | Start destination, session expiry redirect |
| `OverviewRoute` | DashboardScreen | Bottom nav tab 1 |
| `UsersRoute` | UsersScreen | Bottom nav tab 2 |
| `AiUsageRoute` | AiUsageScreen | Bottom nav tab 3 |
| `DiscoveryRoute` | DiscoveryScreen | Bottom nav tab 4 |
| `MoreRoute` | MoreScreen | Bottom nav tab 5 |
| `InventoryRoute` | InventoryScreen | From More menu |
| `SystemHealthRoute` | SystemHealthScreen | From Dashboard cluster banner |
| `AuditLogsRoute` | AuditLogsScreen | From Dashboard receipt icon |
| `SettingsRoute` | SettingsScreen | From More menu |
| `ProductEditRoute` | ProductEditScreen | From Inventory (Add/Edit SKU) |

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
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
               Inventory      System Health    Audit Logs
                    │               │               │
                    ▼               ▼               ▼
              Product Edit    (from Dashboard)  (from Dashboard)
```

### Session-Gated Access
- Unauthenticated users are automatically redirected to Login
- Session expiry triggers immediate redirect to Login
- Bottom navigation is hidden on authentication screens
- State preservation when switching between bottom nav tabs

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

### Session Management
- **SessionManager** singleton manages session lifecycle via `StateFlow<SessionState>`
- States: `Unauthenticated` → `Authenticating` → `Authenticated` / `SessionExpired`
- Automatic session restoration on app startup
- Automatic redirect to login on session expiry

### Authentication Flow
1. Submit credentials over HTTPS
2. Handle MFA challenge if required
3. Verify MFA code through backend
4. Store session credentials securely
5. Restore and validate session on app start
6. Handle refresh failure and session expiry
7. Clear local session data on logout

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
| **DashboardViewModelTest.kt** | 14 | Initial state, success loading, error handling, retry behavior, empty data, KPI mapping, activity event status types, auto-load on init, server errors, session expiry errors |

**Total: 28 unit tests, all passing.**

### Testing Approach
- ViewModel state transitions tested with Turbine
- Repository behavior tested with MockK
- Test dispatchers used for coroutine control
- Fake repositories used for isolated ViewModel testing

---

## Backend Integration

### Current Status
The application uses **development-only fake repositories** for all features. When the backend API is ready, replace the fake implementations with real ones via Hilt `@Binds`.

### Expected Backend Contracts

#### Authentication
| Endpoint | Method | Description |
|---|---|---|
| `/auth/sign-in` | POST | Submit email/password, returns tokens or MFA challenge |
| `/auth/verify-mfa` | POST | Verify TOTP code, returns access/refresh tokens |
| `/auth/refresh` | POST | Refresh access token using refresh token |

#### Dashboard
| Endpoint | Method | Description |
|---|---|---|
| `/admin/dashboard` | GET | KPI metrics, activity events, system health summary |

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

#### Inventory
| Endpoint | Method | Description |
|---|---|---|
| `/admin/products` | GET | Product/SKU listing with filters |
| `/admin/products` | POST | Create new product |
| `/admin/products/{id}` | PUT | Update product |
| `/admin/products/{id}` | DELETE | Delete product |

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
- ✅ Type-safe navigation with 11 routes and 5 bottom nav tabs
- ✅ 11 high-fidelity feature screens implemented
- ✅ All colors dynamic — 30+ named theme colors, zero hardcoded values
- ✅ Components reorganized into 7 focused files
- ✅ Authentication vertical slice: repository, ViewModel, fake impl, 14 tests
- ✅ Dashboard vertical slice: repository, ViewModel, fake impl, 14 tests, screen fully ViewModel-driven
- ✅ Network layer with Hilt `@Module`, auth interceptor, per-build-type base URL
- ✅ Secure token storage with AES256_GCM encryption
- ✅ Session management with auto-redirect on expiry
- ✅ Material 3 design system with NearAI branding
- ✅ Build compiles, all 28 tests pass

### Pending (requires backend API contracts)
- ⏳ Wire remaining feature screens to their ViewModels
- ⏳ Real API endpoint integration for all features
- ⏳ Data layer repository implementations replacing fakes
- ⏳ BiometricPrompt integration for local re-authentication
- ⏳ FIDO2/NFC hardware key integration
- ⏳ Comprehensive unit tests for remaining ViewModels
- ⏳ Feature-specific business logic (credit grants, product CRUD, user management)
- ⏳ Backend confirmation of all API endpoints

---

## License

Private — NearAI Admin Portal is proprietary software for internal administrative use only.

---

## Contact

For questions or support, contact the NearAI development team.
