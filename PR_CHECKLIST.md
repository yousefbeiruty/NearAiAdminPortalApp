# Pull Request Checklist — NearAiAdminPortal

Use this checklist for every pull request to ensure compliance with the [Engineering Constitution](AGENTS.md).

## Architectural Boundaries

- [ ] Composables focus on rendering and UI interaction only — no business logic
- [ ] ViewModels own screen state and expose `StateFlow<UiState>`
- [ ] Repositories abstract data access (domain interface, data implementation)
- [ ] API DTOs are separate from domain models; mappers handle transformations
- [ ] No direct network calls inside Composables
- [ ] No Android Context in the domain layer
- [ ] Dependency direction respected: presentation → domain ← data/infrastructure

## SOLID Principles

- [ ] **SRP** — Each class has one clear responsibility
- [ ] **OCP** — Extension points exist where future variability is expected
- [ ] **LSP** — Implementations respect their interface contracts
- [ ] **ISP** — Interfaces are focused, not bloated
- [ ] **DIP** — High-level logic depends on abstractions; concretions are injected

## Code Quality

- [ ] No duplicated business logic
- [ ] No dead code or unused imports
- [ ] Naming and formatting follow existing conventions
- [ ] Functions and classes are focused and reasonably sized
- [ ] Nullable values and exceptions are handled explicitly
- [ ] No broad exception handling that hides failures

## String Resources & Localization

- [ ] All user-facing strings are in `res/values/strings.xml`
- [ ] No hardcoded text in Composables (labels, titles, errors, dialogs, snackbar messages)
- [ ] `stringResource()` / `pluralStringResource()` used appropriately
- [ ] Formatted string arguments use resource formatting (`%1$s`, `%1$d`)
- [ ] Accessibility labels and content descriptions use string resources

## Error Handling

- [ ] Uses `Result<T>` / `AppError` conventions consistently
- [ ] `TokenRefreshManager.refreshAndRetry { ... }` wraps authenticated API calls
- [ ] `ApiErrorParser.parse()` handles HTTP errors in repository implementations
- [ ] UI handles loading, empty, error, and success states

## Security

- [ ] No hardcoded credentials, tokens, or secrets
- [ ] Authorization headers are not logged
- [ ] Server-side authorization is respected; client-side visibility is not a security boundary
- [ ] Sensitive data (passwords, tokens) is not logged or exposed

## State Management

- [ ] Uses `StateFlow` and immutable `UiState` data classes
- [ ] Composables use `collectAsStateWithLifecycle()`
- [ ] ViewModels use `viewModelScope` for coroutines
- [ ] `LaunchedEffect` includes proper keys for recomposition control

## Navigation

- [ ] Uses type-safe sealed-interface routes in `AppNavHost`
- [ ] ViewModels are injected via `hiltViewModel()`
- [ ] No duplicate navigation; stale asynchronous responses are handled

## Testing

- [ ] ViewModel tests verify state transitions and error handling
- [ ] Repository tests verify API call mapping and error conversion
- [ ] Mapper tests verify DTO-to-domain transformations
- [ ] DTO serialization tests verify Kotlinx Serialization behavior
- [ ] Existing tests still pass
- [ ] No tests were weakened or removed

## Performance

- [ ] No N+1 query patterns or unnecessary network calls
- [ ] Composables are not performing heavy work on the main thread
- [ ] Image loading uses Coil with appropriate caching

## Dependency Management

- [ ] No new dependencies without justification
- [ ] Dependencies use versions from version catalog (`libs.*`)
- [ ] No transitive dependency conflicts introduced

## Documentation

- [ ] KDoc comments added for new public APIs
- [ ] Related documentation updated if architecture or contracts changed

## Backward Compatibility

- [ ] API contracts remain compatible unless explicitly changed
- [ ] Existing navigation routes are not broken
- [ ] Existing Hilt bindings are not disrupted

---

## Quick Reference — Gradle Quality Gates

Run these before submitting:

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
