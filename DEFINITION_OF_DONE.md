# Definition of Done — NearAiAdminPortal

Every implementation, feature, bug fix, and refactoring must satisfy all of the following criteria before being considered complete.

## 1. Architecture Preservation

- Existing Clean Architecture boundaries (presentation → domain ← data) are maintained
- No business logic placed in Composables
- No direct network calls in the UI layer
- Repository abstractions are used, not bypassed

## 2. SOLID Principles

- Single Responsibility: each new class or module has one clear purpose
- Open/Closed: extension points exist where future variability is expected
- Liskov Substitution: implementations fulfill their interface contracts
- Interface Segregation: interfaces remain focused
- Dependency Inversion: high-level logic depends on abstractions injected via Hilt

## 3. Appropriate Abstractions

- Mappers exist between DTOs and domain models
- No unnecessary design patterns or generic factories introduced
- Composition preferred over inheritance where applicable

## 4. No Unnecessary Duplication

- No duplicated business logic across features
- Existing components, mappers, and repository patterns are reused
- Design system components from `core/designsystem/` are reused

## 5. String Resources

- All user-facing strings are in `res/values/strings.xml`
- No hardcoded text in Composables (labels, titles, errors, dialogs, snackbar messages, accessibility descriptions)
- `stringResource()` is used for all string references
- Formatted strings use resource formatting arguments

## 6. Security and Authorization

- No hardcoded credentials, tokens, or secrets
- Authorization headers are not logged
- `TokenRefreshManager` is used for authenticated API calls
- `ApiErrorParser` handles HTTP errors consistently
- Server-side authorization is respected; client-side visibility is not a security boundary

## 7. Database and API Contracts

- No invented API endpoints or database fields
- API contracts remain backward compatible unless explicitly changed
- DTO serialization handles nullable fields and defaults correctly

## 8. Business Transaction Integrity

- Result/AppError conventions are used for error handling
- No fake production data introduced
- Dashboard, analytics, and system-health metrics come from actual backend responses

## 9. Tests Added

- ViewModel tests verify state transitions and error handling (Turbine + kotlinx-coroutines-test)
- Repository tests verify API call mapping and error conversion (mockk)
- Mapper tests verify DTO-to-domain transformations
- DTO serialization tests verify Kotlinx Serialization behavior for new types

## 10. Existing Tests Pass

- All existing unit tests pass (`./gradlew testDebugUnitTest`)
- No tests were weakened, removed, or suppressed to make the build pass

## 11. Linting and Compilation

- Code compiles without errors (`./gradlew compileDebugSources`)
- Lint passes without new warnings (`./gradlew lintDebug`)
- Code follows existing formatting and naming conventions

## 12. API Contract Compatibility

- Existing API endpoint paths are not changed
- Request/response schemas remain compatible
- Navigation routes remain functional

## 13. Documentation Updated

- KDoc comments added for new public APIs
- Related documentation (AGENTS.md, README) updated if architecture or contracts changed

---

## Verification Commands

```bash
# Compile check
./gradlew compileDebugSources

# Unit tests
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug

# Full check (compile + test + lint)
./gradlew check
```

## Related Documents

- [Engineering Constitution](AGENTS.md) — Permanent engineering standards
- [PR Checklist](PR_CHECKLIST.md) — Pull request review checklist
