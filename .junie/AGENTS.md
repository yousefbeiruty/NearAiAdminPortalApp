# NearAiAdminPortal — Engineering Constitution

This file defines permanent engineering standards for the NearAiAdminPortal project (Admin Android application). These instructions apply to every implementation, feature, bug fix, refactoring, and code review.

**Related projects sharing these principles:**
- **NearAiDiscovery** — Customer Android application.
- **NearAiAdminPortal** — Admin Android application (this project).
- **NearAi-Back-End** — FastAPI backend.

---

## 1. Project Overview

**NearAiAdminPortal** is an Android admin application built with:
- Kotlin
- Jetpack Compose (Material 3)
- Clean Architecture
- MVVM
- Hilt dependency injection
- Retrofit with Kotlinx Serialization
- StateFlow for reactive UI state
- Type-safe navigation with sealed-interfaces

**Package structure:**
```
com.nearai.admin/
├── core/
│   ├── common/          # Result, AppError, shared utilities
│   ├── designsystem/    # Theme, colors, typography, reusable components
│   ├── network/         # NetworkModule, Retrofit, ApiErrorParser
│   └── security/        # TokenStorage, TokenRefreshManager, SessionManager
├── feature/
│   ├── auth/            # Authentication
│   ├── dashboard/       # Dashboard overview
│   ├── orders/          # Order management
│   ├── catalog/         # Category and product management
│   ├── inventory/       # Inventory and product editing
│   ├── users/           # User management
│   ├── discovery/       # Business analytics
│   ├── systemhealth/    # System health monitoring
│   ├── auditlogs/       # Audit log viewer
│   ├── aiusage/         # AI usage tracking
│   └── more/            # Additional settings
├── navigation/          # AppNavHost, destinations, bottom navigation
├── MainActivity.kt
└── NearAiAdminApplication.kt
```

Each feature module follows the pattern:
```
feature/<name>/
├── data/        # ApiService, RepositoryImpl, Mapper, Hilt module
├── domain/      # Repository interface, domain models
└── presentation/ # ViewModel, Screen composable, UiState
```

---

## 2. Shared NearAI Engineering Principles

### Clean Architecture

- Maintain clear separation of responsibilities across layers.
- Respect dependency direction: presentation → domain ← data/infrastructure.
- Business rules must not depend unnecessarily on framework or infrastructure implementations.
- Separate presentation, application/business logic, domain contracts, and infrastructure.
- Avoid circular dependencies.
- Keep modules cohesive.

### SOLID Principles

**SRP — Single Responsibility**
- Every class, service, and module should have one clear responsibility.
- Avoid God classes and unrelated responsibilities.

**OCP — Open/Closed**
- Design extension points where genuine future variability exists.
- Avoid modifying unrelated business logic when adding new providers or operations.

**LSP — Liskov Substitution**
- Implementations must respect their interface contracts.
- Substitutable implementations must preserve expected behavior.

**ISP — Interface Segregation**
- Keep interfaces focused.
- Avoid large interfaces with unrelated responsibilities.

**DIP — Dependency Inversion**
- High-level business logic must depend on abstractions at architectural boundaries.
- Concrete infrastructure implementations must be injected rather than constructed throughout business logic.

### Abstraction and Design Patterns

- Prefer composition over unnecessary inheritance.
- Introduce abstractions when they improve testability, extensibility, or dependency isolation.
- Reuse existing design patterns where appropriate.
- Avoid creating interfaces for every class.
- Avoid generic factories, base classes, or utility layers without a real requirement.
- Never introduce abstraction solely to satisfy a pattern or increase architectural complexity.

### Code Quality

- Follow existing naming and formatting conventions.
- Avoid duplicated business logic.
- Keep functions and classes focused.
- Avoid unnecessary nesting and complex conditionals.
- Remove dead code and unused imports.
- Prefer immutable state where appropriate.
- Handle nullable values and exceptions explicitly.
- Avoid broad exception handling that hides failures.
- Never use fake production data to make a feature appear functional.

---

## 3. Admin Android-Specific Rules

### Technology Stack

- Kotlin.
- Jetpack Compose (Material 3).
- Clean Architecture with MVVM.
- Hilt dependency injection.
- Retrofit with Kotlinx Serialization.
- StateFlow for reactive UI state.
- Type-safe navigation with sealed-interface routes.

### Mandatory Architecture Rules

1. **Composables must focus on rendering and UI interaction.** Business logic belongs in ViewModels.
2. **ViewModels own screen state and presentation orchestration.** They expose `StateFlow<UiState>` for the UI to collect.
3. **Business operations must not be implemented directly in Composables.** Delegate to ViewModels.
4. **Repositories abstract data access.** The domain layer defines repository interfaces; the data layer provides implementations.
5. **API DTOs must remain separate from domain models.** Never use DTOs directly in the presentation layer.
6. **Use mappers between DTOs and domain models.** Each feature has its own mapper (e.g., `OrderMapper`, `CatalogMapper`).
7. **Use existing `Result`/`AppError` conventions for error handling.** The sealed `Result<T>` interface (`Success`, `Error`, `Loading`) and `AppError` subtypes are the standard.
8. **Use the established navigation architecture.** Type-safe sealed-interface routes in `AppNavHost` with `hiltViewModel()` for ViewModel injection.
9. **Do not introduce direct network calls inside Composables.** All network access goes through repositories.
10. **Do not introduce Android Context dependencies into the domain layer.** Domain must remain platform-agnostic.
11. **Use lifecycle-aware StateFlow collection.** Use `collectAsStateWithLifecycle()` in Composables.
12. **Respect coroutine cancellation and lifecycle.** ViewModels use `viewModelScope`; Composables use `LaunchedEffect` with proper keys.

### Networking and Authentication

13. **Use the existing authenticated Retrofit configuration.** `NetworkModule` provides the shared `OkHttpClient` with authorization header interceptor.
14. **Use `TokenRefreshManager` for the established automatic token refresh flow.** Wrap repository API calls with `tokenRefreshManager.refreshAndRetry { ... }`.
15. **Use `ApiErrorParser` for consistent HTTP error handling.** Map `HttpException` to `AppError` subtypes via `ApiErrorParser.parse()`.
16. **Use `SessionManager` for session state management.** Session state changes drive navigation between login and authenticated screens.

### State Management

17. **Use StateFlow and immutable UiState.** Each feature defines its own immutable `UiState` data class.
18. **Keep administrative business logic out of Composables.** Composables render state and emit user intents.
19. **Ensure all UI data comes from actual backend responses.** Never fabricate dashboard, analytics, or system-health metrics.
20. **Respect server-side authorization; client-side visibility is not a security boundary.**
21. **Preserve currency separation in analytics.**
22. **Use pagination conventions consistently.**
23. **Protect against duplicate navigation and stale asynchronous responses.**

### String Resources

- Every user-facing string must be in Android string resources (`res/values/strings.xml`).
- Use `stringResource()` and `pluralStringResource()` appropriately.
- Use formatted resource arguments.
- Support localization readiness and RTL layouts.
- Do not hardcode user-facing text inside Composables.
- Do not extract API identifiers, JSON fields, enum values, or internal log messages into UI resources.
- Follow the existing resource organization.
- All visible UI text must use Android resources, including:
  - Dialogs.
  - Forms.
  - Validation messages.
  - Status labels.
  - Error messages.
  - Snackbar messages.
  - Filters.
  - Accessibility descriptions.

### UI and Design System

- Reuse existing design system components in `core/designsystem/components/`.
- Follow Material 3 conventions.
- Avoid duplicated components.
- Support different screen sizes.
- Handle loading, empty, error, and success states.
- Ensure accessibility labels and content descriptions are provided.
- Reuse existing components, mappers, and repository abstractions.

---

## 4. Testing is Mandatory

Every implementation must include appropriate tests.

**Test types required:**
- **ViewModel tests** — Verify state transitions, business logic, and error handling. Use `kotlinx-coroutines-test` and `Turbine` for StateFlow testing.
- **Repository tests** — Verify API call mapping, error conversion, and `TokenRefreshManager` integration. Use `mockk` for API service mocking.
- **Mapper tests** — Verify DTO-to-domain and domain-to-DTO transformations, including edge cases.
- **DTO serialization tests** — Verify Kotlinx Serialization handles real API payloads correctly, including nullable fields and defaults.

**Testing conventions:**
- Tests live in `app/src/test/java/com/nearai/admin/` mirroring the main source structure.
- Use `mockk` for mocking dependencies.
- Use `Turbine` for testing StateFlow emissions.
- Use `kotlinx-coroutines.test` for coroutine test scopes.
- Never weaken or remove existing tests simply to make a build pass.
- Do not claim success based only on compilation.

---

## 5. Mandatory Workflow for Every Task

### Before Making Changes

1. Read this `AGENTS.md` file.
2. Inspect the existing implementation in the relevant feature module.
3. Identify the relevant module and architecture boundaries.
4. Find existing reusable components, repositories, services, and patterns.
5. Identify affected tests and contracts.
6. Understand security and concurrency implications.

### During Implementation

1. Make the smallest appropriate change.
2. Follow established architecture (data/domain/presentation separation).
3. Reuse existing abstractions: `Result`, `AppError`, `TokenRefreshManager`, `ApiErrorParser`.
4. Avoid unnecessary dependencies.
5. Preserve backward compatibility.
6. Add tests for new behavior.
7. Update documentation when contracts or architecture change.

### After Implementation

1. Review the complete diff.
2. Run relevant formatting and lint checks.
3. Compile the affected module.
4. Run relevant tests.
5. Run the full available test suite where practical.
6. Report failures honestly.
7. Identify remaining technical debt.

---

## 6. Rules That Must Never Be Violated

- Do not rewrite entire modules when a targeted fix is sufficient.
- Do not invent APIs or database fields.
- Do not introduce fake production data.
- Do not bypass repository abstractions without architectural justification.
- Do not duplicate existing business logic.
- Do not change API contracts unexpectedly.
- Do not modify unrelated modules.
- Do not suppress errors instead of fixing their causes.
- Do not introduce unnecessary design patterns.
- Do not sacrifice transaction integrity or security for convenience.
- Never fabricate dashboard, analytics, or system-health metrics.
- Never bypass `TokenRefreshManager` for authenticated API calls.
- Never bypass `ApiErrorParser` for HTTP error handling.
- Never hardcode user-facing strings in Composables.

---

## 7. Architecture Decision Policy

When an implementation requires a new architectural pattern or abstraction:

First explain:
- The problem being solved.
- Why existing architecture is insufficient.
- Why the proposed abstraction is appropriate.
- Its impact on testing and maintainability.

Prefer the simplest solution that preserves architectural boundaries.

If an existing convention conflicts with a genuine technical requirement, document the reason and make the smallest justified exception.

---

## 8. Key Reference Files

Understanding these files is essential before making changes:

| File | Purpose |
|------|---------|
| `core/common/Result.kt` | Sealed `Result<T>` interface for consistent error handling |
| `core/common/AppError.kt` | Sealed `AppError` types and `Throwable.toAppError()` |
| `core/network/NetworkModule.kt` | Hilt module for Retrofit, OkHttp, Json, and `ApiErrorParser` |
| `core/security/TokenRefreshManager.kt` | Single-flight token refresh on 401 responses |
| `core/security/SessionManager.kt` | Session state management driving navigation |
| `core/security/TokenStorage.kt` | Encrypted token persistence |
| `navigation/AppNavHost.kt` | Root navigation graph with type-safe routes |
| `navigation/AppDestination.kt` | Sealed-interface route definitions |
| `feature/*/data/*RepositoryImpl.kt` | Repository implementations with `handleException()` pattern |
| `feature/*/domain/*Repository.kt` | Repository interface contracts |
| `feature/*/data/*Mapper.kt` | DTO-to-domain mappers |
| `feature/*/presentation/*ViewModel.kt` | ViewModel with StateFlow UiState |
| `feature/*/presentation/*Screen.kt` | Composable screen with `collectAsStateWithLifecycle()` |

---

## 9. Final Instruction

These rules are permanent engineering standards for this project. They must be applied automatically to:
- New features.
- Bug fixes.
- Refactoring.
- API integration.
- UI changes.
- Code reviews.
- Tests.

Do not wait for the user to repeat these requirements in individual prompts.
